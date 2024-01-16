import dataclasses
import json
from typing import Any

from pvp_ml.callback.callback import Callback
from pvp_ml.util.files import get_experiment_dir
from pvp_ml.util.json_encoders import GeneralizedObjectEncoder


class LatestMetaLoggerCallback(Callback):
    # Logs the model meta to a json file in a readable format for visibility into what is being saved

    def __init__(self, experiment_name: str):
        super().__init__()
        self._experiment_name = experiment_name

    def on_rollout_end(self, *args: Any, **kwargs: Any) -> None:
        self._save()

    def on_learn_end(self) -> None:
        self._save()

    def _save(self) -> None:
        assert self._ppo is not None
        experiment_dir = get_experiment_dir(self._experiment_name)
        with open(f"{experiment_dir}/latest-model-meta.json", "w") as output_file:
            json.dump(
                dataclasses.asdict(self._ppo.meta),
                output_file,
                cls=GeneralizedObjectEncoder,
                indent=2,
            )
