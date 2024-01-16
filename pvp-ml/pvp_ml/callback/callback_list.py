import numpy as np
from numpy.typing import NDArray
from torch.utils.tensorboard import SummaryWriter

from pvp_ml.callback.callback import Callback
from pvp_ml.ppo.buffer import Buffer
from pvp_ml.ppo.ppo import PPO, Meta


class CallbackList(Callback):
    def __init__(self, callbacks: list[Callback]):
        super().__init__()
        self._callbacks = callbacks

    def initialize(self, summary_writer: SummaryWriter | None, ppo: PPO) -> None:
        super().initialize(summary_writer, ppo)
        for callback in self._callbacks:
            callback.initialize(summary_writer, ppo)

    def on_training_start(self) -> None:
        for callback in self._callbacks:
            callback.on_training_start()

    def on_training_end(self) -> None:
        for callback in self._callbacks:
            callback.on_training_end()

    def on_rollout_start(self) -> None:
        for callback in self._callbacks:
            callback.on_rollout_start()

    def on_step(self, indices: NDArray[np.int32], infos: NDArray[np.object_]) -> None:
        for callback in self._callbacks:
            callback.on_step(indices, infos)

    def on_rollout_sampling_end(self, raw_buffer: Buffer) -> None:
        for callback in self._callbacks:
            callback.on_rollout_sampling_end(raw_buffer)

    def on_distributed_rollout_collection(self, distributed_meta: list[Meta]) -> None:
        for callback in self._callbacks:
            callback.on_distributed_rollout_collection(distributed_meta)

    def on_rollout_end(self, buffer: Buffer) -> None:
        for callback in self._callbacks:
            callback.on_rollout_end(buffer)

    def on_learn_end(self) -> None:
        for callback in self._callbacks:
            callback.on_learn_end()
