import logging
import time

import numpy as np
from numpy.typing import NDArray

from pvp_ml.callback.callback import Callback
from pvp_ml.ppo.buffer import Buffer
from pvp_ml.ppo.ppo import Meta

logger = logging.getLogger(__name__)


class LoggingCallback(Callback):
    def __init__(self) -> None:
        super(LoggingCallback, self).__init__()
        self._rollout_start_time: float = time.time()
        self._training_start_time: float = time.time()
        self._n_calls: int = 0
        self._total_steps: int = 0

    def on_rollout_start(self) -> None:
        logger.info("Rollout starting")
        self._rollout_start_time = time.time()
        self._n_calls = 0
        self._total_steps = 0

    def on_rollout_sampling_end(self, _: Buffer) -> None:
        logger.info("Rollout sampling ended")

    def on_rollout_end(self, _: Buffer) -> None:
        rollout_seconds = time.time() - self._rollout_start_time
        logger.info(f"Rollout ending - took {rollout_seconds} seconds")

    def on_training_start(self) -> None:
        logger.info("Training starting")
        self._training_start_time = time.time()

    def on_learn_end(self) -> None:
        logger.info("Learning complete")

    def on_training_end(self) -> None:
        logger.info(
            f"Training ending - took {time.time() - self._training_start_time} seconds"
        )

    def on_distributed_rollout_collection(self, meta: list[Meta]) -> None:
        logger.info(f"Finished distributed rollout with {len(meta)} results")

    def on_step(self, indices: NDArray[np.int32], infos: NDArray[np.object_]) -> None:
        elapsed_time = time.time() - self._rollout_start_time
        self._n_calls += 1
        steps = len(indices)
        self._total_steps += steps
        rollout_fps = self._total_steps / elapsed_time
        logger.info(
            f"Stepping {steps} envs ({self._n_calls} unique step calls, "
            f"{self._total_steps} total env steps, "
            f"{rollout_fps} fps, "
            f"{elapsed_time} elapsed seconds)"
        )
