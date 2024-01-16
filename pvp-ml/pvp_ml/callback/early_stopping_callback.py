import logging
from typing import Any

from pvp_ml.callback.callback import Callback, EndTrainingException
from pvp_ml.util.schedule import Schedule

logger = logging.getLogger(__name__)


class EarlyStoppingCallback(Callback):
    def __init__(self, stopping_condition: Schedule[bool]):
        super().__init__()
        self._stopping_condition = stopping_condition

    def on_training_start(self) -> None:
        self._check(phase="on_training_start")

    def on_training_end(self) -> None:
        self._check(phase="on_training_end")

    def on_rollout_start(self) -> None:
        self._check(phase="on_rollout_start")

    def on_rollout_sampling_end(self, *args: Any, **kwargs: Any) -> None:
        self._check(phase="on_rollout_sampling_end")

    def on_rollout_end(self, *args: Any, **kwargs: Any) -> None:
        self._check(phase="on_rollout_end")

    def on_learn_end(self) -> None:
        self._check(phase="on_learn_end")

    def _check(self, phase: str) -> None:
        assert self._ppo is not None
        if self._stopping_condition is None:
            return
        # Replace / with _ because / expression won't parse (it will think it's division)
        variables = {
            key.replace("/", "_"): value
            for key, value in self._ppo.meta.custom_data.get("stats", {}).items()
        }
        should_stop = self._stopping_condition.value(
            self._ppo.meta.trained_rollouts,
            **variables,
            phase=phase,
        )
        if not should_stop:
            return
        logger.info(
            f"Early stopping condition reached. "
            f"Terminating training via exception. "
            f"{self._stopping_condition} = {should_stop}"
        )
        raise EndTrainingException(
            f"Stopping condition reached at "
            f"{self._ppo.meta.trained_rollouts} rollouts: "
            f"{self._stopping_condition} = {should_stop}"
        )
