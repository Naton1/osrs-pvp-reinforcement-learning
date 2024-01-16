from functools import partial
from typing import Dict, List, Optional, Tuple

import numpy as np
import torch as th
import torch.nn as nn

from pvp_ml.util.contract_loader import ActionDependencies
from pvp_ml.util.mlp_helper import (
    MlpConfig,
    create_mlp,
    default_mlp_config,
    init_weights,
)


class Actor(nn.Module):
    action_dependencies: Dict[int, Dict[int, Dict[str, List[Tuple[int, int]]]]]

    def __init__(
        self,
        input_size: int,
        action_head_sizes: list[int],
        config: MlpConfig = MlpConfig(),
        action_dependencies: ActionDependencies = {},
        head_configs: MlpConfig | list[MlpConfig] | None = None,
        autoregressive_actions: bool = True,
        append_future_action_masks: bool = False,
        normalize_autoregressive_actions: bool = True,
    ):
        super(Actor, self).__init__()
        self.autoregressive_actions = autoregressive_actions
        self.normalize_autoregressive_actions = normalize_autoregressive_actions
        self.append_future_action_masks = append_future_action_masks

        hidden_mlp, hidden_size = create_mlp(config, input_size)

        self.hidden = hidden_mlp
        self.hidden.apply(partial(init_weights, gain=np.sqrt(2)))

        action_stats_shape = (sum(action_head_sizes),)
        self.register_buffer("action_mean", th.zeros(action_stats_shape))
        self.register_buffer("action_var", th.ones(action_stats_shape))
        self.register_buffer("action_count", th.tensor([1e-4]))

        # Explicit types for type-checking
        self.action_mean: th.Tensor
        self.action_var: th.Tensor
        self.action_count: th.Tensor

        def _create_head(index: int) -> nn.Module:
            head_config = (
                (
                    head_configs[index]
                    if isinstance(head_configs, list)
                    else head_configs
                )
                if head_configs is not None
                else MlpConfig()
            )

            autoregressive_size = (
                sum(action_head_sizes[:index]) if autoregressive_actions else 0
            )
            future_masks_size = (
                sum(action_head_sizes[index + 1 :]) if append_future_action_masks else 0
            )
            head_input_size = hidden_size + autoregressive_size + future_masks_size

            mlp, mlp_output_size = create_mlp(head_config, head_input_size)
            head = nn.Linear(mlp_output_size, action_head_sizes[index])

            mlp.apply(partial(init_weights, gain=np.sqrt(2)))
            head.apply(partial(init_weights, gain=0.01))

            return nn.Sequential(mlp, head)

        self.heads = nn.ModuleList(
            [_create_head(i) for i in range(len(action_head_sizes))]
        )

        self.action_head_sizes = th.tensor(action_head_sizes, dtype=th.long)

        self.action_dependencies = action_dependencies
        self._float32_eps = th.finfo(th.float32).eps

    def forward(
        self,
        x: th.Tensor,
        flattened_action_masks: th.Tensor,
        sample_deterministic: Optional[th.Tensor] = None,
        input_actions: Optional[th.Tensor] = None,
        return_log_probs: bool = True,
        return_entropy: bool = True,
        return_probs: bool = False,
    ) -> Tuple[
        th.Tensor, Optional[th.Tensor], Optional[th.Tensor], Optional[th.Tensor]
    ]:
        # action masks should be a flattened array of the action masks, so convert to action space dims
        action_head_sizes: List[int] = self.action_head_sizes.tolist()
        action_masks = th.split(flattened_action_masks, action_head_sizes, dim=1)
        actor_hidden = th.relu(self.hidden(x))

        actions: List[th.Tensor] = []
        log_probs: List[th.Tensor] = []
        one_hot_actions: List[th.Tensor] = []
        entropy: List[th.Tensor] = []
        probabilities: List[th.Tensor] = []

        for i, head in enumerate(self.heads):
            current_actor_hidden = actor_hidden

            if self.autoregressive_actions and i > 0:
                current_actions = th.cat(one_hot_actions, dim=-1)
                if self.normalize_autoregressive_actions:
                    current_actions = self._normalize(current_actions)
                current_actor_hidden = th.cat(
                    [current_actor_hidden, current_actions], dim=-1
                )

            if self.append_future_action_masks and i < len(self.heads) - 1:
                offset = sum(action_head_sizes[:i]) + 1
                current_actor_hidden = th.cat(
                    [current_actor_hidden, flattened_action_masks[..., offset:]], dim=-1
                )

            action_mask = action_masks[i]
            dependency_mask = self._get_action_dependency_mask(
                actions, i, x.shape[0], device=x.device
            )
            mask = action_mask & dependency_mask

            # If no actions are available, default to action 0 (the no-op action)
            no_action_mask = ~mask.any(dim=-1)
            mask[no_action_mask, 0] = True

            logits = head(current_actor_hidden)
            masked_logits = logits - ((~mask) * 1e8)
            probs = th.softmax(masked_logits, dim=-1)

            if input_actions is None:
                action = (
                    probs.argmax(dim=-1)
                    if sample_deterministic is not None and sample_deterministic[i]
                    else th.multinomial(probs, 1).squeeze(-1)
                )
            else:
                action = input_actions[:, i].long()

            actions.append(action)
            one_hot_actions.append(
                th.nn.functional.one_hot(action.detach(), action_head_sizes[i])
            )

            if return_log_probs:
                log_probs.append(self._log_prob(probs, action))

            if return_entropy:
                entropy.append(self._entropy(probs))

            if return_probs:
                probabilities.append(probs)

        combined_log_probs: Optional[th.Tensor] = None
        if return_log_probs:
            combined_log_probs = th.stack(log_probs, dim=1).sum(dim=1)

        combined_entropy: Optional[th.Tensor] = None
        if return_entropy:
            combined_entropy = th.stack(entropy, dim=1)

        combined_probs: Optional[th.Tensor] = None
        if return_probs:
            combined_probs = th.cat(probabilities, dim=1)

        return (
            th.stack(actions, dim=1),
            combined_log_probs,
            combined_entropy,
            combined_probs,
        )

    def _get_action_dependency_mask(
        self,
        previous_actions: List[th.Tensor],
        action_index: int,
        batch_size: int,
        device: th.device,
    ) -> th.Tensor:
        """
        Parses config such as the following, into a set of action dependencies.
        This is useful for enabling/disabling actions based on previous actions, such as action parameterization.

        action_dependencies = {
            6: {
                0: {
                    'require_all': [(0, 1)]
                },
                1: {
                    'require_any': [(0, 1)]
                },
                2: {
                    'require_none': [(0, 1)]
                }
            }
        }
        """

        action_head_size = int(self.action_head_sizes[action_index].item())
        mask = th.ones(
            size=(batch_size, action_head_size), dtype=th.bool, device=device
        )

        if action_index not in self.action_dependencies:
            return mask

        action_dependencies = self.action_dependencies[action_index]

        for single_action_index, action_config in action_dependencies.items():
            single_mask = th.ones((batch_size,), dtype=th.bool, device=device)

            if "require_all" in action_config:
                for action_head_idx, action in action_config["require_all"]:
                    single_mask = single_mask & (
                        previous_actions[action_head_idx] == action
                    )

            if "require_any" in action_config:
                require_any_mask = th.zeros((batch_size,), dtype=th.bool)
                for action_head_idx, action in action_config["require_any"]:
                    require_any_mask = require_any_mask | (
                        previous_actions[action_head_idx] == action
                    )
                single_mask = single_mask & require_any_mask

            if "require_none" in action_config:
                for action_head_idx, action in action_config["require_none"]:
                    single_mask = single_mask & (
                        previous_actions[action_head_idx] != action
                    )

            mask[:, single_action_index] = single_mask

        return mask

    def _log_prob(self, probs: th.Tensor, value: th.Tensor) -> th.Tensor:
        clamped_probs = probs.clamp(min=self._float32_eps, max=1 - self._float32_eps)
        logits = th.log(clamped_probs)
        value = value.long().unsqueeze(-1)
        value, log_pmf = th.broadcast_tensors(value, logits)
        assert isinstance(value, th.Tensor)
        assert isinstance(log_pmf, th.Tensor)
        value = value[..., :1]
        return log_pmf.gather(-1, value).squeeze(-1)

    def _entropy(self, probs: th.Tensor) -> th.Tensor:
        clamped_probs = probs.clamp(min=self._float32_eps, max=1 - self._float32_eps)
        return -th.sum(probs * th.log(clamped_probs), dim=-1)

    def _normalize(self, actions: th.Tensor) -> th.Tensor:
        mean = self.action_mean[..., : actions.shape[-1]]
        var = self.action_var[..., : actions.shape[-1]]
        actions = actions - mean
        actions = actions / th.sqrt(var + 1e-8)
        actions = th.clamp(actions, -5, 5)
        return actions

    def update_action_normalization(self, actions: th.Tensor) -> None:
        assert len(actions.shape) == 2
        # Convert list of selected actions ints into one-hot-encoded
        tensors = []
        for i in range(0, len(self.action_head_sizes)):
            tensors.append(
                th.nn.functional.one_hot(
                    actions[..., i].to(th.int64), int(self.action_head_sizes[i].item())
                )
            )
        actions = th.cat(tensors, dim=-1).to(th.float32)
        batch_mean = th.mean(actions, dim=0)
        batch_var = th.var(actions, dim=0, unbiased=False)
        batch_count = actions.shape[0]
        self._update_from_moments(batch_mean, batch_var, batch_count)

    def _update_from_moments(
        self, batch_mean: th.Tensor, batch_var: th.Tensor, batch_count: int
    ) -> None:
        delta = batch_mean - self.action_mean
        tot_count = self.action_count + batch_count

        new_mean = self.action_mean + delta * batch_count / tot_count
        m_a = self.action_var * self.action_count
        m_b = batch_var * batch_count
        m_2 = (
            m_a
            + m_b
            + th.square(delta)
            * self.action_count
            * batch_count
            / (self.action_count + batch_count)
        )
        new_var = m_2 / (self.action_count + batch_count)

        new_count = batch_count + self.action_count

        self.action_mean = new_mean
        self.action_var = new_var
        self.action_count = new_count


class Critic(nn.Module):
    def __init__(self, input_size: int, config: MlpConfig = MlpConfig()):
        super(Critic, self).__init__()
        hidden_mlp, hidden_size = create_mlp(config, input_size)
        self.hidden = hidden_mlp
        self.head = nn.Linear(hidden_size, 1)
        self.hidden.apply(partial(init_weights, gain=np.sqrt(2)))
        self.head.apply(partial(init_weights, gain=1))

    def forward(self, x: th.Tensor) -> th.Tensor:
        critic_hidden = th.relu(self.hidden(x))
        value: th.Tensor = self.head(critic_hidden)
        return value.squeeze(-1)


class Policy(nn.Module):
    def __init__(
        self,
        max_sequence_length: int,
        actor_input_size: int,
        critic_input_size: int,
        action_head_sizes: list[int],
        feature_extractor_config: MlpConfig = MlpConfig(),
        share_feature_extractor: bool = False,
        critic_config: MlpConfig = default_mlp_config([64, 64]),
        actor_config: MlpConfig = default_mlp_config([128, 128, 128]),
        action_head_configs: MlpConfig | list[MlpConfig] | None = None,
        action_dependencies: ActionDependencies = {},
        autoregressive_actions: bool = True,
        append_future_action_masks: bool = False,
        normalize_autoregressive_actions: bool = True,
    ):
        super(Policy, self).__init__()
        self._max_sequence_length = max_sequence_length
        self._share_feature_extractor = share_feature_extractor
        self._actor_obs_size = actor_input_size
        self._critic_obs_size = critic_input_size
        self.feature_extractor: nn.Module | None
        self.actor_feature_extractor: nn.Module | None
        self.critic_feature_extractor: nn.Module | None
        if share_feature_extractor:
            assert (
                actor_input_size == critic_input_size
            ), "Actor/critic input sizes must equal for shared layers"
            feature_extractor, hidden_size = create_mlp(
                feature_extractor_config, max_sequence_length * actor_input_size
            )
            actor_input_size = hidden_size
            critic_input_size = hidden_size
            self.feature_extractor = feature_extractor
            self.actor_feature_extractor = None
            self.critic_feature_extractor = None
            self.feature_extractor.apply(partial(init_weights, gain=np.sqrt(2)))
        else:
            self.feature_extractor = None
            actor_feature_extractor, actor_input_size = create_mlp(
                feature_extractor_config, max_sequence_length * actor_input_size
            )
            critic_feature_extractor, critic_input_size = create_mlp(
                feature_extractor_config, max_sequence_length * critic_input_size
            )
            self.actor_feature_extractor = actor_feature_extractor
            self.critic_feature_extractor = critic_feature_extractor
            self.actor_feature_extractor.apply(partial(init_weights, gain=np.sqrt(2)))
            self.critic_feature_extractor.apply(partial(init_weights, gain=np.sqrt(2)))

        self._actor_input_size = actor_input_size
        self._critic_input_size = critic_input_size

        self.actor = Actor(
            actor_input_size,
            action_head_sizes,
            actor_config,
            action_dependencies,
            action_head_configs,
            autoregressive_actions=autoregressive_actions,
            append_future_action_masks=append_future_action_masks,
            normalize_autoregressive_actions=normalize_autoregressive_actions,
        )
        self.critic = Critic(critic_input_size, critic_config)

    def forward(
        self,
        x: th.Tensor,
        action_masks: th.Tensor,
        sample_deterministic: Optional[th.Tensor] = None,
        input_actions: Optional[th.Tensor] = None,
        return_actions: bool = True,
        return_values: bool = True,
        return_entropy: bool = True,
        return_log_probs: bool = True,
        return_probs: bool = False,
    ) -> Tuple[
        Optional[th.Tensor],
        Optional[th.Tensor],
        Optional[th.Tensor],
        Optional[th.Tensor],
        Optional[th.Tensor],
    ]:
        assert x.dim() == 3  # (batch_size, max_sequence_length, num_features)
        assert (
            x.shape[1] <= self._max_sequence_length
        ), f"Got {x.shape[1]} when expecting at most {self._max_sequence_length} for shape[1]"

        if return_actions:
            assert (
                x.shape[2] >= self._actor_obs_size
            ), f"Got {x.shape[2]} when expecting >= {self._actor_obs_size} for shape[2]"
        if return_values:
            assert (
                x.shape[2] >= self._critic_obs_size
            ), f"Got {x.shape[2]} when expecting >= {self._critic_obs_size} for shape[2]"

        x = x.reshape(x.size(0), -1)  # Flatten frame stacked input

        actor_features: Optional[th.Tensor] = None
        critic_features: Optional[th.Tensor] = None
        if return_actions or return_values:
            if self._share_feature_extractor:
                assert self.feature_extractor is not None
                actor_features = critic_features = self.feature_extractor(
                    x[..., : self._actor_input_size]
                )
            else:
                if return_actions:
                    assert self.actor_feature_extractor is not None
                    actor_features = self.actor_feature_extractor(
                        x[..., : self._actor_input_size]
                    )
                if return_values:
                    assert self.critic_feature_extractor is not None
                    critic_features = self.critic_feature_extractor(
                        x[..., : self._critic_input_size]
                    )

        actions: Optional[th.Tensor] = None
        log_probs: Optional[th.Tensor] = None
        entropy: Optional[th.Tensor] = None
        probs: Optional[th.Tensor] = None
        if return_actions:
            assert actor_features is not None
            actions, log_probs, entropy, probs = self.actor(
                actor_features,
                action_masks,
                sample_deterministic=sample_deterministic,
                input_actions=input_actions,
                return_entropy=return_entropy,
                return_log_probs=return_log_probs,
                return_probs=return_probs,
            )

        values: Optional[th.Tensor] = None
        if return_values:
            assert critic_features is not None
            values = self.critic(critic_features)

        return actions, log_probs, entropy, values, probs
