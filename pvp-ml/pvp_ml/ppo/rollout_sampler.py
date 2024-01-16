import itertools
import time

import gymnasium.spaces
import numpy as np
import torch as th
from torch.utils.tensorboard import SummaryWriter

from pvp_ml.callback.callback import Callback
from pvp_ml.env.async_io_vec_env import AsyncIoVecEnv
from pvp_ml.env.pvp_env import PvpEnv
from pvp_ml.ppo.buffer import Buffer
from pvp_ml.ppo.ppo import PPO
from pvp_ml.util.running_mean_std import TensorRunningMeanStd


class RolloutSampler:
    def collect(
        self,
        env: AsyncIoVecEnv,
        ppo: PPO,
        steps: int,
        callback: Callback,
        eps_greedy: float = 0.0,
        gae_lambda: float = 0.95,
        gamma: float = 0.99,
        normalize_rewards: bool = False,
        novelty_reward_scale: float = 0.0,
        summary_writer: SummaryWriter | None = None,
    ) -> Buffer:
        start = time.time()

        buffer = self._sample_rollout(
            env,
            ppo,
            steps,
            callback=callback,
            eps_greedy=eps_greedy,
            gae_lambda=gae_lambda,
            gamma=gamma,
        )

        callback.on_rollout_sampling_end(raw_buffer=buffer)

        reward_normalizer: TensorRunningMeanStd | None = None
        if normalize_rewards:
            reward_norm_key = "reward_norm"
            if reward_norm_key not in ppo.meta.custom_data:
                ppo.meta.custom_data[reward_norm_key] = TensorRunningMeanStd(
                    shape=(), clip_lower=-10, clip_upper=10
                )
            reward_normalizer = ppo.meta.custom_data[reward_norm_key]

        finalize_start_time = time.time()
        buffer.finalize(ppo, reward_normalizer, novelty_reward_scale)
        finalize_duration = time.time() - finalize_start_time

        if summary_writer is not None:
            rollout_length = time.time() - start
            fps = buffer.buffer_size * buffer.n_envs / rollout_length

            summary_writer.add_scalar(
                "rollout/time", rollout_length, ppo.meta.trained_steps
            )
            summary_writer.add_scalar("rollout/fps", fps, ppo.meta.trained_steps)

            summary_writer.add_scalar(
                "rollout/buffer_finalize_time",
                finalize_duration,
                ppo.meta.trained_steps,
            )

            summary_writer.add_scalar(
                "rollout/num_episode_starts",
                np.sum(buffer.episode_starts),
                ppo.meta.trained_steps,
            )
            summary_writer.add_scalar(
                "rollout/num_truncates",
                np.sum(buffer.truncates),
                ppo.meta.trained_steps,
            )

            summary_writer.add_scalar(
                "rollout/value_mean", np.mean(buffer.values), ppo.meta.trained_steps
            )
            summary_writer.add_scalar(
                "rollout/advantage_mean",
                np.mean(buffer.advantages),
                ppo.meta.trained_steps,
            )
            summary_writer.add_scalar(
                "rollout/return_mean", np.mean(buffer.returns), ppo.meta.trained_steps
            )
            summary_writer.add_scalar(
                "rollout/step_reward_mean",
                np.mean(buffer.rewards),
                ppo.meta.trained_steps,
            )

            episode_lengths = list(itertools.chain(*buffer.episode_lengths))
            if episode_lengths:
                summary_writer.add_scalar(
                    "rollout/len/min_episode_length",
                    np.min(episode_lengths),
                    ppo.meta.trained_steps,
                )
                summary_writer.add_scalar(
                    "rollout/len/max_episode_length",
                    np.max(episode_lengths),
                    ppo.meta.trained_steps,
                )
                summary_writer.add_scalar(
                    "rollout/len/mean_episode_length",
                    np.mean(episode_lengths),
                    ppo.meta.trained_steps,
                )
                summary_writer.add_scalar(
                    "rollout/len/std_episode_length",
                    np.std(episode_lengths),
                    ppo.meta.trained_steps,
                )

            episode_rewards = list(itertools.chain(*buffer.episode_rewards))
            if episode_rewards:
                summary_writer.add_scalar(
                    "rollout/reward/min_episode_reward",
                    np.min(episode_rewards),
                    ppo.meta.trained_steps,
                )
                summary_writer.add_scalar(
                    "rollout/reward/max_episode_reward",
                    np.max(episode_rewards),
                    ppo.meta.trained_steps,
                )
                summary_writer.add_scalar(
                    "rollout/reward/mean_episode_reward",
                    np.mean(episode_rewards),
                    ppo.meta.trained_steps,
                )
                summary_writer.add_scalar(
                    "rollout/reward/std_episode_reward",
                    np.std(episode_rewards),
                    ppo.meta.trained_steps,
                )

            summary_writer.add_scalar(
                "rollout/num_episodes", len(episode_lengths), ppo.meta.trained_steps
            )

            # Extract meta for better metric names
            first_env = env.envs[0]
            assert isinstance(first_env, PvpEnv)
            env_meta = first_env.meta

            for action_idx in range(buffer.actions.shape[2]):
                action_key = env_meta.actions[action_idx].id
                action_data = buffer.actions[:, :, action_idx].flatten()
                summary_writer.add_histogram(
                    f"actions/action/{action_key}", action_data, ppo.meta.trained_steps
                )

            mask_offset = 0
            for action_idx, n in enumerate(buffer.action_space.nvec):
                action_key = env_meta.actions[action_idx].id
                mask_data = buffer.action_masks[:, :, mask_offset : mask_offset + n]
                available_actions = np.where(mask_data.flatten() == 1)[0] % n
                summary_writer.add_histogram(
                    f"actions/mask/{action_key}",
                    available_actions,
                    ppo.meta.trained_steps,
                )
                mask_offset += n

            partial_indices = env_meta.get_partially_observable_indices()
            for obs_idx in range(buffer.observation_space.shape[-1]):
                if obs_idx >= len(env_meta.observations):
                    # Handle 'critic' obs
                    real_obs_idx = partial_indices[obs_idx - len(env_meta.observations)]
                    obs_key = f"opponent_{env_meta.observations[real_obs_idx].id}"
                else:
                    obs_key = env_meta.observations[obs_idx].id
                data = buffer.observations[:, :, -1, obs_idx]
                summary_writer.add_histogram(
                    f"observations/{obs_key}", data, ppo.meta.trained_steps
                )

            summary_writer.add_scalar(
                "rollout/eps_greedy", eps_greedy, ppo.meta.trained_steps
            )
            summary_writer.add_scalar(
                "rollout/gae_lambda", gae_lambda, ppo.meta.trained_steps
            )
            summary_writer.add_scalar("rollout/gamma", gamma, ppo.meta.trained_steps)
            summary_writer.add_scalar("rollout/steps", steps, ppo.meta.trained_steps)
            summary_writer.add_scalar(
                "rollout/num_envs", buffer.n_envs, ppo.meta.trained_steps
            )

            if reward_normalizer is not None:
                summary_writer.add_scalar(
                    "rollout/running_reward_mean",
                    reward_normalizer.mean.item(),
                    ppo.meta.trained_steps,
                )
                summary_writer.add_scalar(
                    "rollout/running_reward_var",
                    reward_normalizer.var.item(),
                    ppo.meta.trained_steps,
                )
                summary_writer.add_scalar(
                    "rollout/running_reward_count",
                    reward_normalizer.count,
                    ppo.meta.trained_steps,
                )

                clip_count = np.count_nonzero(
                    (buffer.rewards == reward_normalizer.clip_upper)
                    | (buffer.rewards == reward_normalizer.clip_lower)
                )
                summary_writer.add_scalar(
                    "rollout/reward_clip_count", clip_count, ppo.meta.trained_steps
                )

            summary_writer.add_scalar(
                "rollout/novelty_reward_scale",
                novelty_reward_scale,
                ppo.meta.trained_steps,
            )
            if ppo.meta.trained_rollouts > 0:
                # Only add after 1 rollout, otherwise running observation stats won't be set
                # so novelties won't be accurate
                summary_writer.add_scalar(
                    "rollout/novelty_mean",
                    buffer.novelty.mean(),
                    ppo.meta.trained_steps,
                )
                summary_writer.add_scalar(
                    "rollout/novelty_std", buffer.novelty.std(), ppo.meta.trained_steps
                )
                summary_writer.add_scalar(
                    "rollout/novelty_min", buffer.novelty.min(), ppo.meta.trained_steps
                )
                summary_writer.add_scalar(
                    "rollout/novelty_max", buffer.novelty.max(), ppo.meta.trained_steps
                )
                summary_writer.add_histogram(
                    "rollout/novelty", buffer.novelty, ppo.meta.trained_steps
                )

        return buffer

    def _sample_rollout(
        self,
        env: AsyncIoVecEnv,
        ppo: PPO,
        steps: int,
        callback: Callback,
        eps_greedy: float = 0.0,
        gae_lambda: float = 0.95,
        gamma: float = 0.99,
    ) -> Buffer:
        env_action_space = env.action_space
        env_observation_space = env.observation_space
        # We only support these space types (which is what PvpEnv uses)
        assert isinstance(env_action_space, gymnasium.spaces.MultiDiscrete)
        assert isinstance(env_observation_space, gymnasium.spaces.Box)
        buffer = Buffer(
            buffer_size=steps,
            n_envs=env.num_envs,
            action_space=env_action_space,
            observation_space=env_observation_space,
            gamma=gamma,
            gae_lambda=gae_lambda,
        )

        env.reset_async()

        last_obs = np.zeros(
            (buffer.n_envs, *buffer.observation_space.shape), dtype=np.float32
        )
        last_episode_starts = np.ones(shape=(env.num_envs,), dtype=bool)
        available_indices = np.empty((0,), dtype=np.int32)

        while not buffer.is_full():
            if env.is_reset_waiting():
                indices, obs = env.poll_reset(wait=0.001)
                last_obs[indices] = obs
                available_indices = np.concatenate((available_indices, indices))

            if len(available_indices) > 0:
                action_masks = env.get_action_masks(indices=available_indices)
                actions, log_probs, _, values, *_ = ppo.predict(
                    th.as_tensor(last_obs[available_indices], device=ppo.device),
                    th.as_tensor(action_masks, device=ppo.device),
                    deterministic=0 < eps_greedy and eps_greedy > np.random.random(),
                    return_entropy=False,
                    return_actions=True,
                    return_values=True,
                    return_log_probs=True,
                )
                assert actions is not None
                assert log_probs is not None
                assert values is not None
                np_actions = actions.cpu().numpy()
                env.step_async(np_actions, available_indices)
                buffer.add_step_request(
                    available_indices,
                    np_actions,
                    values.cpu().numpy(),
                    log_probs.cpu().numpy(),
                    action_masks,
                )
                available_indices = np.empty((0,), dtype=np.int32)

            indices, (obs, reward, done, truncated, info) = env.poll_step(
                wait=0.001 if env.is_reset_waiting() else None
            )
            if len(indices) > 0:
                buffer.add_step_response(
                    indices,
                    last_obs[indices],
                    reward,
                    last_episode_starts[indices],
                    truncated,
                    obs,
                    done,
                    info,
                )
                last_obs[indices] = obs
                last_episode_starts[indices] = done
                available_indices = indices
                callback.on_step(indices, info)

        return buffer
