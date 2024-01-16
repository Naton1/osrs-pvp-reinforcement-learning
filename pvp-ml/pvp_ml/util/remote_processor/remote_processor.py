import abc
from abc import ABC
from typing import Any

import torch as th

PROCESS_REMOTE_PROCESSOR = "process"
THREAD_REMOTE_PROCESSOR = "thread"
RAY_REMOTE_PROCESSOR = "ray"

REMOTE_PROCESSOR_TYPES = [
    PROCESS_REMOTE_PROCESSOR,
    THREAD_REMOTE_PROCESSOR,
    RAY_REMOTE_PROCESSOR,
]


class RemoteProcessor(ABC):
    @abc.abstractmethod
    async def predict(
        self,
        process_id: int,
        model_path: str,
        observation: th.Tensor | None = None,
        action_masks: th.Tensor | None = None,
        deterministic: bool | th.Tensor = False,
        return_device: str | None = None,
        return_actions: bool = True,
        return_log_probs: bool = False,
        return_entropy: bool = False,
        return_values: bool = False,
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
        pass

    @abc.abstractmethod
    async def close(self) -> None:
        pass

    @abc.abstractmethod
    def get_pool_size(self) -> int:
        pass

    @abc.abstractmethod
    def get_device(self) -> str:
        pass

    async def __aenter__(self) -> "RemoteProcessor":
        return self

    async def __aexit__(self, *args: Any) -> None:
        await self.close()


async def create_remote_processor(
    pool_size: int = 1,
    processor_type: str = THREAD_REMOTE_PROCESSOR,
    device: str = "cpu",
    remote_processor_additional_params: dict[str, Any] = {},
) -> RemoteProcessor:
    if processor_type == PROCESS_REMOTE_PROCESSOR:
        from pvp_ml.util.remote_processor.process_remote_processor import (
            ExternalProcessor,
        )

        external_processor = ExternalProcessor(
            pool_size=pool_size, device=device, **remote_processor_additional_params
        )
        await external_processor.initialize()
        return external_processor
    elif processor_type == THREAD_REMOTE_PROCESSOR:
        from pvp_ml.util.remote_processor.thread_remote_processor import (
            ThreadedProcessor,
        )

        return ThreadedProcessor(
            pool_size=pool_size, device=device, **remote_processor_additional_params
        )
    elif processor_type == RAY_REMOTE_PROCESSOR:
        from pvp_ml.util.remote_processor.ray_remote_processor import RayProcessor

        return RayProcessor(
            pool_size=pool_size, device=device, **remote_processor_additional_params
        )
    raise ValueError(processor_type)
