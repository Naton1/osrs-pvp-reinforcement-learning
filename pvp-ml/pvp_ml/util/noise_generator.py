import json
from dataclasses import dataclass
from typing import Any

import numpy as np
from numpy.typing import NDArray

from pvp_ml.util.schedule import Schedule, schedule


@dataclass(frozen=True)
class NoiseAdder:
    indices: list[int]
    value: Schedule[float]


class NoiseGenerator:
    def __init__(self, adders: list[NoiseAdder]):
        self._adders = adders

    def add_noise(self, data: NDArray[np.floating[Any]], step: int) -> None:
        for adder in self._adders:
            data[..., adder.indices] += adder.value.value(step)


def noise_generator(arg: str) -> NoiseGenerator:
    """
    Utility function to parse a json config into a noise generator, where the json config looks like
    {
      'adders': [
        {
          'indices': [1, 2, 3],
          'value': <any-schedule-config>
        },
        {
          'indices': [1, "3:6"],
          'value': <another-schedule-config>
        }
      ]
    }
    :param arg: the json config
    :return: a NoiseGenerator based on the json config
    """
    params = json.loads(arg)
    adders = []
    for adder_config in params["adders"]:
        # Indices can be specified as a list of numbers, or a string range, ex. "1:5"
        indices: list[int] = []
        for index in adder_config["indices"]:
            if isinstance(index, str):
                start, end = map(int, index.split(":"))
                indices.extend(range(start, end))
            else:
                indices.append(index)
        value: Schedule[float] = schedule(json.dumps(adder_config["value"]))
        adder = NoiseAdder(indices=indices, value=value)
        adders.append(adder)
    return NoiseGenerator(adders)
