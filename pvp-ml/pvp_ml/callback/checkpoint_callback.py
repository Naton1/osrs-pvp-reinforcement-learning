import logging
import os

from torch.utils.tensorboard import SummaryWriter

from pvp_ml.callback.callback import Callback
from pvp_ml.ppo.ppo import PPO
from pvp_ml.util.files import get_model_file_name
from pvp_ml.util.schedule import ConstantSchedule, Schedule

logger = logging.getLogger(__name__)


class CheckpointCallback(Callback):
    def __init__(
        self,
        save_path: str,
        name_prefix: str = "main",
        frequency: Schedule[int] = ConstantSchedule(1),
        make_old_models_untrainable: bool = True,
    ):
        super(CheckpointCallback, self).__init__()
        self._save_path = save_path
        self._name_prefix = name_prefix
        self._frequency_schedule = frequency
        self._learn_count = 0
        self._frequency = 0
        self._initial_load = False
        self._make_old_models_untrainable = make_old_models_untrainable
        self._last_save: str | None = None

    def initialize(self, summary_writer: SummaryWriter | None, ppo: PPO) -> None:
        super().initialize(summary_writer, ppo)
        if not self._initial_load:
            self._save()  # Save on load so there's always a saved model
            self._initial_load = True
        self._frequency = self._frequency_schedule.value(ppo.meta.trained_rollouts)

    def on_learn_end(self) -> None:
        assert self._ppo is not None
        self._learn_count += 1
        if self._learn_count % self._frequency == 0:
            self._learn_count = 0
            self._save()
            self._frequency = self._frequency_schedule.value(
                self._ppo.meta.trained_rollouts
            )

    def _save(self) -> None:
        assert self._ppo is not None
        # Save latest model
        model_path = os.path.join(
            self._save_path,
            get_model_file_name(self._name_prefix, self._ppo.meta.trained_steps),
        )
        self._ppo.save(model_path)
        # Check if we should make old model version non-trainable
        # Making non-trainable saves disk space by removing optimizer state
        if self._make_old_models_untrainable and self._last_save:
            logger.info(
                f"Converting previously-saved model to be non-trainable: {self._last_save}"
            )
            PPO.optimize_for_inference(self._last_save)
            logger.info(
                f"Updated previously-saved model to be non-trainable: {self._last_save}"
            )
        self._last_save = model_path
