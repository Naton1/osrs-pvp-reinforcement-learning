import abc
import dataclasses
import logging
import os
import threading
import time
from dataclasses import dataclass, field
from typing import Any, cast

import numpy as np
import torch as th
import torch.optim as optim
from torch.utils.tensorboard import SummaryWriter

from pvp_ml.ppo.buffer import Buffer
from pvp_ml.ppo.policy import Policy
from pvp_ml.util.contract_loader import ActionDependencies, EnvironmentMeta
from pvp_ml.util.mlp_helper import MlpConfig, default_mlp_config
from pvp_ml.util.running_mean_std import TensorRunningMeanStd


@dataclass(frozen=True)
class PolicyParams:
    max_sequence_length: int
    actor_input_size: int
    critic_input_size: int
    action_head_sizes: list[int]
    feature_extractor_config: MlpConfig = field(default_factory=lambda: MlpConfig())
    share_feature_extractor: bool = False
    critic_config: MlpConfig = field(
        default_factory=lambda: default_mlp_config([64, 64])
    )
    actor_config: MlpConfig = field(
        default_factory=lambda: default_mlp_config([128, 128, 128])
    )
    action_head_configs: MlpConfig | list[MlpConfig] | None = None
    action_dependencies: ActionDependencies = field(default_factory=dict)
    autoregressive_actions: bool = True
    append_future_action_masks: bool = False
    normalize_autoregressive_actions: bool = True


@dataclass
class Meta:
    running_observation_stats: TensorRunningMeanStd
    normalized_observations: bool
    trained_steps: int = 0
    trained_rollouts: int = 0
    num_updates: int = 0
    custom_data: dict[str, Any] = field(default_factory=dict)


class ModelExtension(abc.ABC):
    @abc.abstractmethod
    def run_extension(self, obs: th.Tensor) -> Any:
        pass

    def learn(
        self, buffer: Buffer, meta: Meta, summary_writer: SummaryWriter | None
    ) -> None:
        pass

    def state_dict(self) -> dict[str, Any]:
        return {}

    def eval(self) -> None:
        pass

    def to(self, device: str) -> None:
        pass

    @classmethod
    def optimize_for_inference(cls, state_dict: dict[str, Any]) -> None:
        pass


logger = logging.getLogger(__name__)
# th.jit.compile seems to not be threadsafe
# ex. 'RuntimeError: Can't redefine method: forward on class:' ...
_jit_lock = threading.Lock()
_JIT_EVAL_POLICY = os.getenv("TORCH_SCRIPT_INFERENCE", "true").lower() == "true"


class PPO:
    def __init__(
        self,
        policy_params: PolicyParams,
        meta: Meta,
        device: str = "cpu",
        trainable: bool = True,
        policy_state: dict[str, Any] | None = None,
        optimizer_state: dict[str, Any] | None = None,
        extensions: dict[str, ModelExtension] = {},
    ):
        # Note: don't call constructor directly, use one of the static factory methods to load or create a new instance
        self._policy_params = policy_params
        self.device = device
        self.meta = meta
        self._policy: Policy | None = Policy(**dataclasses.asdict(policy_params))
        self._policy.to(device=th.device(device))
        self._policy.eval()
        self._extensions = extensions
        if policy_state is not None:
            self._policy.load_state_dict(policy_state)
        for extension in self._extensions.values():
            extension.to(device)
        if _JIT_EVAL_POLICY:
            with _jit_lock:
                self._eval_policy = th.jit.freeze(th.jit.script(self._policy))
        else:
            self._eval_policy = self._policy
        self._optimizer: optim.Adam | None
        if trainable:
            self._optimizer = optim.Adam(self._policy.parameters(), eps=1e-5)
            if optimizer_state is not None:
                self._optimizer.load_state_dict(optimizer_state)
        else:
            self._policy = None
            self._optimizer = None
            for extension in self._extensions.values():
                extension.eval()

    def predict(
        self,
        obs: th.Tensor,
        action_masks: th.Tensor,
        deterministic: bool | th.Tensor = False,
        return_device: str | None = None,
        return_actions: bool = True,
        return_log_probs: bool = True,
        return_entropy: bool = True,
        return_values: bool = True,
        return_probs: bool = False,
        extensions: list[str] = [],
    ) -> tuple[
        th.Tensor | None,
        th.Tensor | None,
        th.Tensor | None,
        th.Tensor | None,
        th.Tensor | None,
        list[Any],
    ]:
        with th.inference_mode():
            obs = obs.to(self.device)
            action_masks = action_masks.to(self.device)

            if deterministic is True:
                deterministic = th.ones(
                    len(self._policy_params.action_head_sizes),
                    dtype=th.bool,
                    device=self.device,
                )
            elif deterministic is False:
                deterministic = th.zeros(
                    len(self._policy_params.action_head_sizes),
                    dtype=th.bool,
                    device=self.device,
                )

            if self.meta.normalized_observations:
                obs = self.meta.running_observation_stats.normalize(obs, clip=True)

            actions, log_probs, entropy, values, probs = self._eval_policy(
                obs,
                action_masks,
                sample_deterministic=deterministic,
                return_actions=return_actions,
                return_entropy=return_entropy,
                return_log_probs=return_log_probs,
                return_values=return_values,
                return_probs=return_probs,
            )

            extension_results = [
                self._extensions[extension].run_extension(obs)
                for extension in extensions
            ]

            if return_device is not None:
                if actions is not None:
                    actions = actions.to(return_device)
                if log_probs is not None:
                    log_probs = log_probs.to(return_device)
                if entropy is not None:
                    entropy = entropy.to(return_device)
                if values is not None:
                    values = values.to(return_device)
                if probs is not None:
                    probs = probs.to(return_device)

            return actions, log_probs, entropy, values, probs, extension_results

    def learn(
        self,
        buffer: Buffer,
        summary_writer: SummaryWriter | None = None,
        num_updates: int = 5,
        batch_size: int = 64,
        clip_coef: float = 0.2,
        vf_coef: float = 0.5,
        entropy_coef: float = 0.0,
        max_grad_norm: float = 0.5,
        grad_accum: int = 1,
        learning_rate: float = 0.0003,
        normalize_advantages: bool = True,
    ) -> None:
        assert self.is_trainable(), "PPO instance not trainable"
        assert self._optimizer is not None
        assert buffer.is_full(), "Buffer is not full"
        assert self._policy is not None
        for param_group in self._optimizer.param_groups:
            param_group["lr"] = learning_rate

        if self.meta.trained_rollouts == 0:
            logger.info(
                "Skipping training on first rollout to accumulate observation statistics"
            )
            # Skip training on first rollout to collect observation statistics, since normalizations may change
            num_updates = 0

        self._policy.train()

        start_time = time.time()
        start_updates = self.meta.num_updates
        entropy_losses = []
        pg_losses = []
        value_losses = []
        clip_fractions = []
        approx_kls = []
        losses = []
        grad_norms = []
        action_entropy_losses = []

        accumulated_gradients = 0
        for _ in range(num_updates):
            for batch in buffer.generate_batches(batch_size, device=self.device):
                observations = batch.observations
                if self.meta.normalized_observations:
                    observations = self.meta.running_observation_stats.normalize(
                        observations, clip=True
                    )

                _, new_log_probs, individual_entropies, new_values, _ = self._policy(
                    observations,
                    batch.action_masks,
                    input_actions=batch.actions,
                    return_entropy=True,
                    return_values=True,
                    return_log_probs=True,
                )

                old_log_probs = batch.old_log_prob
                log_prob_ratios = new_log_probs - old_log_probs
                prob_ratios = th.exp(log_prob_ratios)

                advantages = batch.advantages
                if normalize_advantages and len(advantages) > 1:
                    advantages = (advantages - advantages.mean()) / (
                        advantages.std() + 1e-8
                    )

                surrogate1 = prob_ratios * advantages
                surrogate2 = (
                    th.clamp(prob_ratios, 1 - clip_coef, 1 + clip_coef) * advantages
                )
                policy_loss = -th.mean(th.min(surrogate1, surrogate2))

                pg_losses.append(policy_loss.item())
                clip_fraction = th.mean((th.abs(prob_ratios - 1) > clip_coef).float())
                clip_fractions.append(clip_fraction.item())

                approx_kl = (prob_ratios - 1) - log_prob_ratios
                approx_kls.append(approx_kl.mean().item())

                entropy_loss = -th.mean(individual_entropies.sum(dim=1))
                entropy_losses.append(entropy_loss.item())
                individual_entropy_losses = (
                    -individual_entropies.mean(dim=0).detach().cpu().numpy()
                )
                action_entropy_losses.append(individual_entropy_losses)

                value_loss = th.nn.functional.mse_loss(
                    new_values.squeeze(), batch.returns
                )
                value_losses.append(value_loss.item())

                loss = policy_loss + entropy_coef * entropy_loss + value_loss * vf_coef
                losses.append(loss.item())

                loss = loss / grad_accum
                loss.backward()
                accumulated_gradients += 1

                if accumulated_gradients == grad_accum:
                    grad_norm = th.nn.utils.clip_grad_norm_(
                        self._policy.parameters(), max_grad_norm
                    )
                    grad_norms.append(th.mean(grad_norm).item())
                    self._optimizer.step()
                    self._optimizer.zero_grad()
                    accumulated_gradients = 0
                    self.meta.num_updates += 1

        self._optimizer.zero_grad()

        flattened_obs = buffer.observations.reshape(-1, buffer.observations.shape[-1])
        self.meta.running_observation_stats.update(
            th.as_tensor(flattened_obs, device=self.device)
        )
        flattened_actions = buffer.actions.reshape(-1, buffer.actions.shape[-1])
        self._policy.actor.update_action_normalization(
            th.as_tensor(flattened_actions, dtype=th.float32, device=self.device)
        )

        if summary_writer is not None:
            # If we have env metadata stored, use that for better obs names
            env_meta: EnvironmentMeta | None = self.meta.custom_data.get("env_meta")

            train_time = time.time() - start_time
            summary_writer.add_scalar(
                "train/total_steps", self.meta.trained_steps, self.meta.trained_rollouts
            )
            summary_writer.add_scalar(
                "train/epochs", num_updates, self.meta.trained_steps
            )
            summary_writer.add_scalar("train/time", train_time, self.meta.trained_steps)
            summary_writer.add_scalar(
                "train/entropy_coef", entropy_coef, self.meta.trained_steps
            )
            summary_writer.add_scalar(
                "train/clip_coef", clip_coef, self.meta.trained_steps
            )
            summary_writer.add_scalar("train/vf_coef", vf_coef, self.meta.trained_steps)
            summary_writer.add_scalar(
                "train/num_updates",
                self.meta.num_updates - start_updates,
                self.meta.trained_steps,
            )
            summary_writer.add_scalar(
                "train/max_grad_norm", max_grad_norm, self.meta.trained_steps
            )
            summary_writer.add_scalar(
                "train/batch_size", batch_size, self.meta.trained_steps
            )
            summary_writer.add_scalar(
                "train/grad_accum", grad_accum, self.meta.trained_steps
            )
            summary_writer.add_scalar(
                "train/learning_rate", learning_rate, self.meta.trained_steps
            )
            if num_updates > 0:
                y_pred = buffer.values.flatten()
                y_true = buffer.returns.flatten()
                var_y = np.var(y_true)
                explained_var = (
                    np.nan if var_y == 0 else 1 - np.var(y_true - y_pred) / var_y
                )
                summary_writer.add_scalar(
                    "train/explained_variance", explained_var, self.meta.trained_steps
                )
                summary_writer.add_scalar(
                    "train/entropy_loss",
                    np.mean(entropy_losses),
                    self.meta.trained_steps,
                )
                summary_writer.add_scalar(
                    "train/policy_gradient_loss",
                    np.mean(pg_losses),
                    self.meta.trained_steps,
                )
                summary_writer.add_scalar(
                    "train/value_loss", np.mean(value_losses), self.meta.trained_steps
                )
                summary_writer.add_scalar(
                    "train/clip_fraction",
                    np.mean(clip_fractions),
                    self.meta.trained_steps,
                )
                summary_writer.add_scalar(
                    "train/grad_norm", np.mean(grad_norms), self.meta.trained_steps
                )
                summary_writer.add_scalar(
                    "train/loss", np.mean(losses), self.meta.trained_steps
                )
                summary_writer.add_scalar(
                    "train/kl", np.mean(approx_kls), self.meta.trained_steps
                )
                for i, entropy_loss in enumerate(
                    np.mean(action_entropy_losses, axis=0)
                ):
                    action_key = env_meta.actions[i].id if env_meta is not None else i
                    summary_writer.add_scalar(
                        f"train/entropy_loss/{action_key}",
                        entropy_loss,
                        self.meta.trained_steps,
                    )

            summary_writer.add_scalar(
                "observations/stats_count",
                self.meta.running_observation_stats.count,
                self.meta.trained_steps,
            )

            for i in range(buffer.observations.shape[-1]):
                if env_meta is not None:
                    if i >= len(env_meta.observations):
                        # Handle 'critic' obs
                        partial_indices = env_meta.get_partially_observable_indices()
                        real_obs_idx = partial_indices[i - len(env_meta.observations)]
                        obs_key = f"opponent_{env_meta.observations[real_obs_idx].id}"
                    else:
                        obs_key = env_meta.observations[i].id
                else:
                    obs_key = f"{i}"
                key_obs = buffer.observations[..., i]
                summary_writer.add_scalar(
                    f"observations/{obs_key}_rollout_mean",
                    np.mean(key_obs),
                    self.meta.trained_steps,
                )
                summary_writer.add_scalar(
                    f"observations/{obs_key}_rollout_std",
                    np.std(key_obs),
                    self.meta.trained_steps,
                )
                summary_writer.add_scalar(
                    f"observations/{obs_key}_rollout_min",
                    np.min(key_obs),
                    self.meta.trained_steps,
                )
                summary_writer.add_scalar(
                    f"observations/{obs_key}_rollout_max",
                    np.max(key_obs),
                    self.meta.trained_steps,
                )
                summary_writer.add_scalar(
                    f"observations/{obs_key}_running_mean",
                    self.meta.running_observation_stats.mean[i],
                    self.meta.trained_steps,
                )
                summary_writer.add_scalar(
                    f"observations/{obs_key}_running_var",
                    self.meta.running_observation_stats.var[i],
                    self.meta.trained_steps,
                )

        self.meta.trained_steps += buffer.buffer_size * buffer.n_envs
        self.meta.trained_rollouts += 1
        self._policy.eval()

        if _JIT_EVAL_POLICY:
            self._eval_policy = th.jit.freeze(th.jit.script(self._policy))

        for extension_name, extension in self._extensions.items():
            logger.info(f"Training '{extension_name}' extension")
            start_extension_time = time.time()
            extension.learn(buffer, self.meta, summary_writer)
            train_extension_duration = time.time() - start_extension_time
            logger.info(
                f"Finished training '{extension_name}' extension in {train_extension_duration} seconds"
            )
            if summary_writer is not None:
                summary_writer.add_scalar(
                    f"train/ext/{extension_name}_duration",
                    train_extension_duration,
                    self.meta.trained_steps,
                )

    def is_trainable(self) -> bool:
        return self._optimizer is not None

    def has_extension(self, name: str) -> bool:
        return name in self._extensions

    def register_extension(self, name: str, extension: ModelExtension) -> None:
        if name in self._extensions:
            raise ValueError(f"Extension {name} already exists")
        extension.to(self.device)
        if not self.is_trainable():
            extension.eval()
        self._extensions[name] = extension

    def remove_extension(self, name: str) -> bool:
        return self._extensions.pop(name, None) is not None

    def save(self, save_path: str) -> None:
        assert self.is_trainable(), "Can't save non-trainable model"
        assert self._policy is not None
        assert self._optimizer is not None
        # Create directory if needed
        save_dir = os.path.dirname(save_path)
        if save_dir:
            os.makedirs(save_dir, exist_ok=True)
        # Save model weights
        th.save(
            {
                "policy": self._policy.state_dict(),
                "optimizer": self._optimizer.state_dict(),
                "policy_params": self._policy_params,
                "meta": self.meta,
                "extensions": [
                    {
                        "name": name,
                        "params": extension.state_dict(),
                        "type": extension.__class__,
                    }
                    for name, extension in self._extensions.items()
                ],
            },
            save_path,
        )

    @staticmethod
    def load(
        load_path: str, device: str = "cpu", trainable: bool | None = None
    ) -> "PPO":
        if not os.path.exists(load_path):
            raise ValueError(f"{load_path} not found")
        checkpoint = th.load(load_path, map_location=device)
        # Ensure the loaded model is actually trainable, if requested
        if trainable is None:
            trainable = "optimizer" in checkpoint
        assert (
            not trainable or "optimizer" in checkpoint
        ), f"Cannot load non-trainable model as trainable: {load_path}"
        return PPO(
            policy_params=checkpoint["policy_params"],
            meta=checkpoint["meta"],
            device=device,
            trainable=trainable,
            policy_state=checkpoint["policy"],
            optimizer_state=checkpoint.get("optimizer"),
            extensions={
                saved_extension["name"]: saved_extension["type"](
                    **saved_extension["params"]
                )
                for saved_extension in checkpoint.get(
                    "extensions", []
                )  # Backwards compatibility
            },
        )

    @staticmethod
    def load_meta(load_path: str) -> Meta:
        # Optimized version of load, to just load the model meta
        if not os.path.exists(load_path):
            raise ValueError(f"{load_path} not found")
        checkpoint = th.load(load_path, map_location="cpu")
        return cast(Meta, checkpoint["meta"])

    @staticmethod
    def save_meta(save_path: str, meta: Meta) -> None:
        if not os.path.exists(save_path):
            raise ValueError(f"{save_path} not found")
        checkpoint = th.load(save_path, map_location="cpu")
        checkpoint["meta"] = meta
        th.save(checkpoint, save_path)

    @staticmethod
    def optimize_for_inference(model_path: str) -> None:
        # Optimize the model for deployment by removing unnecessary information (ex. optimizer state)
        checkpoint = th.load(model_path, map_location="cpu")
        checkpoint.pop("optimizer", None)
        for extension in checkpoint.get("extensions", []):
            extension["type"].optimize_for_inference(extension["params"])
        th.save(checkpoint, model_path)

    @staticmethod
    def new_instance(
        policy_params: PolicyParams,
        device: str = "cpu",
        normalize_observations: bool = False,
    ) -> "PPO":
        return PPO(
            policy_params=policy_params,
            meta=Meta(
                normalized_observations=normalize_observations,
                running_observation_stats=TensorRunningMeanStd(
                    shape=(
                        max(
                            policy_params.actor_input_size,
                            policy_params.critic_input_size,
                        ),
                    ),
                    device=device,
                ),
            ),
            device=device,
        )

    def __str__(self) -> str:
        return str(self._policy)
