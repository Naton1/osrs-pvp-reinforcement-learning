import csv
import datetime
import logging
import time
from typing import TypedDict

import numpy as np
from numpy.typing import NDArray

from pvp_ml.callback.callback import Callback
from pvp_ml.util.files import get_experiment_dir

logger = logging.getLogger(__name__)


class TableRow(TypedDict):
    env_id: str
    target: str
    last_step: str
    episodes: int
    total_steps: int


class EnvTrackerCallback(Callback):
    def __init__(
        self,
        experiment_name: str,
        update_time_seconds: float = 20.0,
        file_name: str = "environments",
    ):
        super(EnvTrackerCallback, self).__init__()
        self._experiment_name = experiment_name
        self._update_time_seconds = update_time_seconds
        self._file_name = file_name
        self._last_update_time: float = time.time()
        self._table_data: dict[str, TableRow] = {}
        self._is_data_stale: bool = False

    def on_step(self, indices: NDArray[np.int32], infos: NDArray[np.object_]) -> None:
        step_time = datetime.datetime.now().isoformat()
        for info in infos:
            assert isinstance(info, dict)
            previous_row = self._table_data.get(info["id"])
            self._table_data[info["id"]] = TableRow(
                env_id=info["id"],
                target=info["target"],
                last_step=step_time,
                episodes=(previous_row["episodes"] if previous_row is not None else 0)
                + (1 if "episode" in info else 0),
                total_steps=(
                    previous_row["total_steps"] if previous_row is not None else 0
                )
                + 1,
            )
            self._is_data_stale = True
        self._try_flush()

    def _try_flush(self) -> None:
        if time.time() > self._last_update_time + self._update_time_seconds:
            self._last_update_time = time.time()
            self._flush()

    def _flush(self) -> None:
        if not self._table_data:
            # No tracked data yet
            return
        if not self._is_data_stale:
            # Nothing new
            return
        start = time.time()
        sorted_envs = [
            self._table_data[key]
            for key in sorted(
                self._table_data.keys(), key=lambda x: float(x) if x.isdigit() else x
            )
        ]
        experiment_dir = get_experiment_dir(self._experiment_name)
        with open(
            f"{experiment_dir}/{self._file_name}.csv", "w", newline=""
        ) as output_file:
            dict_writer = csv.DictWriter(output_file, sorted_envs[0].keys())
            dict_writer.writeheader()
            dict_writer.writerows(sorted_envs)
        self._is_data_stale = False
        logger.info(f"Updated env tracker (took {time.time() - start} seconds)")
