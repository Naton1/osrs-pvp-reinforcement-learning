import asyncio
import concurrent.futures
from asyncio import AbstractEventLoop
from collections.abc import Callable, Iterable
from typing import Any

import numpy as np
from numpy.typing import NDArray

from pvp_ml.env.async_io_env import AsyncIoEnv

VecEnvIndices = None | int | Iterable[int]
VecEnvStepReturn = tuple[
    NDArray[np.float32],
    NDArray[np.float32],
    NDArray[np.bool_],
    NDArray[np.bool_],
    NDArray[np.object_],
]

StepReturn = tuple[NDArray[np.float32], float, bool, bool, dict[str, Any]]
ResetReturn = tuple[NDArray[np.float32], dict[str, Any]]

# Based on SB3's VecEnvs, but with support for async methods
# This makes two assumptions (that could be changed if needed):
# 1. Act type is a numpy array of integers
# 2. Obs type is a numpy array of floats


class AsyncIoVecEnv:
    def __init__(
        self,
        env_fns: list[Callable[[], AsyncIoEnv[NDArray[np.float32], NDArray[np.int32]]]],
        loop: AbstractEventLoop,
        reset_options: dict[str, Any] = {},
    ):
        self._waiting_steps: dict[int, asyncio.futures.Future[StepReturn]] = {}
        self._waiting_resets: dict[int, asyncio.futures.Future[ResetReturn]] = {}

        self._loop = loop

        # Create environments on event loop incase they need the loop

        async def _async_create_env(
            env_fn: Callable[[], AsyncIoEnv[NDArray[np.float32], NDArray[np.int32]]]
        ) -> AsyncIoEnv[NDArray[np.float32], NDArray[np.int32]]:
            return env_fn()

        create_futures = [
            asyncio.run_coroutine_threadsafe(_async_create_env(env_fn), self._loop)
            for env_fn in env_fns
        ]
        concurrent.futures.wait(create_futures)
        self.envs = [future.result() for future in create_futures]

        self.num_envs = len(env_fns)
        self.action_space = self.envs[0].action_space
        self.observation_space = self.envs[0].observation_space
        self.reset_options = reset_options

        self._is_closed = False

    def reset_async(self, indices: VecEnvIndices = None) -> None:
        indices = self._get_indices(indices)
        for action_index, remote_index in enumerate(indices):
            assert remote_index not in self._waiting_steps
            assert remote_index not in self._waiting_resets
            env = self.envs[remote_index]

            future = asyncio.run_coroutine_threadsafe(
                env.reset_async(options=self.reset_options), self._loop
            )
            self._waiting_resets[remote_index] = asyncio.wrap_future(
                future, loop=self._loop
            )

    def is_reset_waiting(self) -> bool:
        return bool(self._waiting_resets)

    def poll_reset(
        self, wait: float | None = None, wait_for_all: bool = False
    ) -> tuple[NDArray[np.int32], NDArray[np.float32]]:
        future = asyncio.run_coroutine_threadsafe(
            self.poll_reset_async(wait, wait_for_all), self._loop
        )
        return future.result()

    async def poll_reset_async(
        self, wait: float | None = None, wait_for_all: bool = False
    ) -> tuple[NDArray[np.int32], NDArray[np.float32]]:
        obs_shape = self.observation_space.shape
        assert obs_shape is not None

        futures = list(self._waiting_resets.values())
        if not futures:
            return np.empty(shape=(0,), dtype=np.int32), np.empty(
                shape=obs_shape, dtype=np.float32
            )
        keys = list(self._waiting_resets.keys())

        completed_futures, _ = await asyncio.wait(
            futures,
            timeout=wait,
            return_when=asyncio.ALL_COMPLETED
            if wait_for_all
            else asyncio.FIRST_COMPLETED,
        )

        if not completed_futures:
            return np.empty(shape=(0,), dtype=np.int32), np.empty(
                obs_shape, dtype=np.float32
            )
        indices = [keys[futures.index(f)] for f in completed_futures]
        obs = [obs for obs, info in [future.result() for future in completed_futures]]

        for index in indices:
            del self._waiting_resets[index]
        return np.array(indices, dtype=np.int32), np.stack(obs)

    def step_async(
        self, actions: NDArray[np.int32], indices: VecEnvIndices = None
    ) -> None:
        indices = self._get_indices(indices)
        for action_index, remote_index in enumerate(indices):
            assert remote_index not in self._waiting_steps
            assert remote_index not in self._waiting_resets
            env = self.envs[remote_index]
            action = actions[action_index]

            future = asyncio.run_coroutine_threadsafe(
                AsyncIoVecEnv.__step(env, action, self.reset_options), self._loop
            )
            self._waiting_steps[remote_index] = asyncio.wrap_future(
                future, loop=self._loop
            )

    def poll_step(
        self, wait: float | None = None, wait_for_all: bool = False
    ) -> tuple[NDArray[np.int32], VecEnvStepReturn]:
        future = asyncio.run_coroutine_threadsafe(
            self.poll_step_async(wait, wait_for_all), self._loop
        )
        return future.result()

    async def poll_step_async(
        self, wait: float | None = None, wait_for_all: bool = False
    ) -> tuple[NDArray[np.int32], VecEnvStepReturn]:
        futures = list(self._waiting_steps.values())
        if not futures:
            return self._empty_step()
        keys = list(self._waiting_steps.keys())
        completed_futures, _ = await asyncio.wait(
            futures,
            timeout=wait,
            return_when=asyncio.ALL_COMPLETED
            if wait_for_all
            else asyncio.FIRST_COMPLETED,
        )
        if not completed_futures:
            return self._empty_step()
        indices = [keys[futures.index(f)] for f in completed_futures]
        results = [future.result() for future in completed_futures]
        observations, rewards, dones, truncates, infos = zip(*results)
        for index in indices:
            del self._waiting_steps[index]
        return np.array(indices, dtype=np.int32), (
            np.stack(observations),
            np.stack(rewards),
            np.stack(dones),
            np.stack(truncates),
            np.array(infos),
        )

    def close(self) -> None:
        if self._is_closed:
            return
        self._is_closed = True
        close_futures = [
            asyncio.run_coroutine_threadsafe(env.close_async(), self._loop)
            for env in self.envs
        ]
        concurrent.futures.wait(close_futures)
        # Handle all pending results/exceptions
        for _, step_future in self._waiting_steps.items():
            try:
                step_future.result()
            except Exception:
                pass
        self._waiting_steps.clear()
        # Handle all pending results/exceptions
        for _, reset_future in self._waiting_resets.items():
            try:
                reset_future.result()
            except Exception:
                pass
        self._waiting_resets.clear()

    def get_action_masks(self, indices: VecEnvIndices = None) -> NDArray[np.bool_]:
        return np.stack(
            [env.get_action_masks() for env in self._get_target_envs(indices)]
        )

    def is_closed(self) -> bool:
        return self._is_closed

    def _get_target_envs(
        self, indices: VecEnvIndices
    ) -> list[AsyncIoEnv[NDArray[np.float32], NDArray[np.int32]]]:
        indices = self._get_indices(indices)
        return [self.envs[i] for i in indices]

    def _get_indices(self, indices: VecEnvIndices) -> Iterable[int]:
        if indices is None:
            indices = range(self.num_envs)
        elif isinstance(indices, int):
            indices = [indices]
        return indices

    def _empty_step(self) -> tuple[NDArray[np.int32], VecEnvStepReturn]:
        obs_shape = self.observation_space.shape
        assert obs_shape is not None
        return (
            np.empty(shape=(0,), dtype=np.int32),
            (
                np.empty(obs_shape, dtype=np.float32),
                np.empty(shape=(0,), dtype=np.float32),
                np.empty(shape=(0,), dtype=bool),
                np.empty(shape=(0,), dtype=bool),
                np.empty(shape=(0,), dtype=object),
            ),
        )

    @staticmethod
    async def __step(
        env: AsyncIoEnv[NDArray[np.float32], NDArray[np.int32]],
        action: NDArray[np.int32],
        reset_options: dict[str, Any],
    ) -> StepReturn:
        # Wrap step to auto reset
        observation, reward, terminated, truncated, info = await env.step_async(action)
        if terminated or truncated:
            info["terminal_observation"] = observation
            observation, reset_info = await env.reset_async(options=reset_options)
            info["reset_info"] = reset_info
        return observation, reward, terminated, truncated, info
