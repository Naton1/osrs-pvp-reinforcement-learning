from collections import defaultdict
from collections.abc import Callable
from typing import Any

import numpy as np

from pvp_ml.callback.callback import Callback
from pvp_ml.ppo.buffer import Buffer


# Accumulates min/max/mean/sum/std for all numeric values over each episode
# It knows an episode ends when an info has a 'episode' key, and it injects the aggregations into info['episode']
class EpisodeAccumulatorCallback(Callback):
    def on_rollout_end(self, buffer: Buffer) -> None:
        for env in buffer.infos:
            current_values: dict[str, Any] = defaultdict(list)

            for step in env:
                # Track values for every numeric value
                current_values = self._accumulate_dict(step, current_values)
                # Inject the values into the episode key
                if "episode" in step:
                    step["episode"]["sum"] = self._create_stat_dict(
                        current_values, np.sum
                    )
                    step["episode"]["mean"] = self._create_stat_dict(
                        current_values, np.mean
                    )
                    step["episode"]["min"] = self._create_stat_dict(
                        current_values, np.min
                    )
                    step["episode"]["max"] = self._create_stat_dict(
                        current_values, np.max
                    )
                    step["episode"]["std"] = self._create_stat_dict(
                        current_values, np.std
                    )
                    current_values = defaultdict(list)

    def _accumulate_dict(
        self, target_dict: dict[str, Any], current_values: dict[str, Any]
    ) -> dict[str, Any]:
        for key, value in target_dict.items():
            if isinstance(value, (int, float)):
                current_values[key].append(value)
            if isinstance(value, dict):
                current_values[key] = self._accumulate_dict(
                    value, current_values.get(key, defaultdict(list))
                )
        return current_values

    def _create_stat_dict(
        self, input_dict: dict[str, Any], accumulator_func: Callable[[list[Any]], Any]
    ) -> dict[str, Any]:
        stat_dict = {}
        for key, values in input_dict.items():
            if isinstance(values, list):
                if values:
                    stat_dict[key] = accumulator_func(values)
            elif isinstance(values, dict):
                stat_dict[key] = self._create_stat_dict(values, accumulator_func)
        return stat_dict
