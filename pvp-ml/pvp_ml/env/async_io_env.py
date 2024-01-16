import asyncio
from abc import ABC, abstractmethod
from asyncio import AbstractEventLoop
from typing import Any, TypeVar

import gymnasium as gym
import numpy as np
from numpy.typing import NDArray

ObsType = TypeVar("ObsType")
ActType = TypeVar("ActType")


class AsyncIoEnv(gym.Env[ObsType, ActType], ABC):
    def __init__(self, loop: AbstractEventLoop | None = None):
        self._loop = loop

    def reset(
        self,
        *args: Any,
        **kwargs: Any,
    ) -> tuple[ObsType, dict[str, Any]]:
        assert self._loop is not None, "Event loop must be provided for sync methods"
        return asyncio.run_coroutine_threadsafe(
            self.reset_async(*args, **kwargs), self._loop
        ).result()

    def step(
        self, action: ActType
    ) -> tuple[ObsType, float, bool, bool, dict[str, Any]]:
        assert self._loop is not None, "Event loop must be provided for sync methods"
        return asyncio.run_coroutine_threadsafe(
            self.step_async(action), self._loop
        ).result()

    def close(self) -> None:
        assert self._loop is not None, "Event loop must be provided for sync methods"
        return asyncio.run_coroutine_threadsafe(self.close_async(), self._loop).result()

    @abstractmethod
    async def reset_async(
        self,
        *,
        seed: int | None = None,
        options: dict[str, Any] | None = None,
    ) -> tuple[ObsType, dict[str, Any]]:
        pass

    @abstractmethod
    async def step_async(
        self, action: ActType
    ) -> tuple[ObsType, float, bool, bool, dict[str, Any]]:
        pass

    @abstractmethod
    async def close_async(self) -> None:
        pass

    @abstractmethod
    def get_action_masks(self) -> NDArray[np.bool_]:
        pass

    def _assert_calling_from_loop(self) -> None:
        assert (
            self._loop is None or self._loop is asyncio.get_event_loop()
        ), "Async methods must be called from provided event loop"
