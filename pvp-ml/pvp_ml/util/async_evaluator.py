import logging
from collections.abc import Callable
from typing import Any

import numpy as np
import torch as th

from pvp_ml.env.async_io_vec_env import AsyncIoVecEnv
from pvp_ml.env.pvp_env import PvpEnv, ResetOptions
from pvp_ml.scripted.script_plugin_registry import (
    get_scripted_plugin,
    is_scripted_plugin,
)
from pvp_ml.util.remote_processor.remote_processor import RemoteProcessor

logger = logging.getLogger(__name__)


class AsyncEvaluator:
    @staticmethod
    async def evaluate(
        env: PvpEnv,
        get_model_path: Callable[[], str],
        process_id: int,
        remote_processor: RemoteProcessor,
        deterministic: bool = True,
        delay_chance: float = 0.0,
        on_step: Callable[[dict[str, Any]], None] | None = None,
        on_episode_complete: Callable[[str, float, dict[str, Any]], bool] | None = None,
    ) -> None:
        """
        Evaluates an environment using models given by the get_model_path function.
        If a given model is a scripted baseline, that will be used instead of loading a model via remote processor.
        """
        logger.info(f"Evaluating environment: {env}")
        try:
            while not env.is_closed():
                episode_model = get_model_path()
                if not is_scripted_plugin(episode_model):
                    # preload model in remote process
                    await remote_processor.predict(
                        process_id=process_id, model_path=episode_model
                    )
                obs, reset_info = await env.reset_async(
                    options=ResetOptions(
                        agent=episode_model,
                    )
                )
                total_reward = 0.0
                while not env.is_closed():
                    if np.random.random() < delay_chance:
                        # Simulate reaction, do no-op, and keep old obs for next tick
                        np_action = np.zeros(env.action_space.shape, dtype=np.int32)
                        _, reward, done, truncated, info = await env.step_async(
                            np_action
                        )
                    else:
                        th_obs = th.as_tensor(obs[np.newaxis, :], device="cpu")
                        action_masks = th.as_tensor(
                            env.get_action_masks()[np.newaxis, :], device="cpu"
                        )
                        if not is_scripted_plugin(episode_model):
                            action, *_ = await remote_processor.predict(
                                model_path=episode_model,
                                process_id=process_id,
                                observation=th_obs,
                                deterministic=deterministic,
                                action_masks=action_masks,
                                return_device="cpu",
                            )
                            assert action is not None
                        else:
                            action = get_scripted_plugin(episode_model).predict(
                                th_obs, action_masks
                            )
                        obs, reward, done, truncated, info = await env.step_async(
                            action.cpu().numpy()[0]
                        )
                    total_reward += reward
                    if on_step is not None:
                        on_step(info)
                    if done:
                        logger.debug(
                            f"Evaluation episode complete: {info} - total reward {total_reward}"
                        )
                        if on_episode_complete is not None:
                            stop_evaluating = on_episode_complete(
                                episode_model, total_reward, info
                            )
                            if stop_evaluating:
                                await env.close_async()
                        break
            logger.info(f"Evaluation sequence completed {env}")
        except Exception:
            if env.is_closed():
                logger.debug(f"Evaluation session threw exception while closed {env}")
            else:
                # Exception is expected if env is closed while calling step/reset
                logger.exception(f"Evaluation session threw exception {env}")
            raise
        finally:
            await env.close_async()
            logger.debug(f"Evaluation session cleaned up {env}")

    @staticmethod
    async def vec_evaluate(
        env: AsyncIoVecEnv,
        model_path: str,
        delay_chances: list[float],
        process_id: int,
        remote_processor: RemoteProcessor,
        deterministic: bool = True,
        on_step: Callable[[dict[str, Any]], None] | None = None,
        on_episode_complete: Callable[[str, float, dict[str, Any]], bool] | None = None,
    ) -> None:
        env.reset_async()

        obs_shape = env.observation_space.shape
        assert obs_shape is not None
        last_obs = np.zeros((env.num_envs, *obs_shape), dtype=np.float32)
        last_episode_starts = np.ones(shape=(env.num_envs,))
        episode_rewards = np.zeros(shape=(env.num_envs,))
        available_indices = np.empty((0,), dtype=np.int32)
        delay_chances_np = np.array(delay_chances)

        while not env.is_closed():
            if env.is_reset_waiting():
                indices, obs = await env.poll_reset_async(wait=0.001)
                last_obs[indices] = obs
                available_indices = np.concatenate((available_indices, indices))

            if len(available_indices) > 0:
                delays = np.random.random() < delay_chances_np[available_indices]
                delay_indices = available_indices[delays]
                num_delays = np.sum(delays)
                if num_delays > 0:
                    env_shape = env.action_space.shape
                    assert env_shape is not None
                    action = np.zeros(env_shape, dtype=np.int32)
                    np_actions = np.repeat(
                        np.expand_dims(action, axis=0), num_delays, axis=0
                    )
                    env.step_async(np_actions, delay_indices)
                    available_indices = available_indices[~delays]

            if len(available_indices) > 0:
                action_masks = env.get_action_masks(indices=available_indices)
                actions, *_ = await remote_processor.predict(
                    model_path=model_path,
                    process_id=process_id,
                    observation=th.as_tensor(last_obs[available_indices], device="cpu"),
                    deterministic=deterministic,
                    action_masks=th.as_tensor(action_masks, device="cpu"),
                    return_device="cpu",
                )
                assert actions is not None
                np_actions = actions.cpu().numpy()
                env.step_async(np_actions, available_indices)
                available_indices = np.empty((0,), dtype=np.int32)

            indices, (obs, reward, done, truncated, info) = await env.poll_step_async(
                wait=0.001 if env.is_reset_waiting() else None
            )

            if len(indices) > 0:
                last_obs[indices] = obs
                last_episode_starts[indices] = done
                episode_rewards[indices] += reward
                available_indices = indices

                if on_step is not None:
                    for i, env_index in enumerate(indices):
                        on_step(info[i])

                if on_episode_complete is not None:
                    for i, env_index in enumerate(indices):
                        if done[i]:
                            end_training = on_episode_complete(
                                model_path, episode_rewards[env_index].item(), info[i]
                            )
                            if end_training:
                                env.close()
                            episode_rewards[env_index] = 0
