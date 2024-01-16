import json
import os.path
from dataclasses import dataclass, field
from functools import lru_cache

import dacite
import numpy as np
from dacite import Config
from gymnasium import spaces

from pvp_ml import package_root

contracts = f"{os.path.dirname(package_root)}/contracts"

# Type aliases for TorchScript compilation, if needed - see get_action_dependency_config()
DependentActions = list[tuple[int, int]]
ActionConfig = dict[str, DependentActions]
ActionHeadConfig = dict[int, ActionConfig]
ActionDependencies = dict[int, ActionHeadConfig]


@dataclass(frozen=True)
class Observation:
    id: str
    description: str
    partial: bool = False
    constant: bool = False


@dataclass(frozen=True)
class ActionDependencyConfig:
    require_all: list[str] = field(default_factory=list)
    require_any: list[str] = field(default_factory=list)
    require_none: list[str] = field(default_factory=list)


@dataclass(frozen=True)
class Action:
    id: str
    description: str
    dependencies: ActionDependencyConfig | None = None


@dataclass(frozen=True)
class ActionHead:
    id: str
    description: str
    actions: list[Action]


@dataclass(frozen=True)
class EnvironmentMeta:
    actions: list[ActionHead]
    observations: list[Observation]

    def get_action_space(self) -> spaces.MultiDiscrete:
        return spaces.MultiDiscrete(
            [len(action_head.actions) for action_head in self.actions], dtype=np.int32
        )

    def get_observation_space(self) -> spaces.Box:
        return spaces.Box(
            low=-np.inf, high=np.inf, shape=(len(self.observations),), dtype=np.float32
        )

    def get_partially_observable_indices(self) -> list[int]:
        return [i for i, o in enumerate(self.observations) if o.partial]

    def get_non_constant_indices(self) -> list[int]:
        return [i for i, o in enumerate(self.observations) if not o.constant]

    def get_action_dependency_config(self) -> ActionDependencies:
        # Parse action dependencies into format the Policy is expecting (raw dicts/lists for TorchScript)
        # and convert id references to index tuples
        action_dependencies: ActionDependencies = {}
        action_indices: dict[str, tuple[int, int]] = {}
        for action_head_idx, action_head in enumerate(self.actions):
            action_dependencies[action_head_idx] = {}
            for action_idx, action in enumerate(action_head.actions):
                # Track current action indices so it can be referenced later
                action_indices[action.id] = (action_head_idx, action_idx)
                if not action.dependencies:
                    continue
                # Map dependencies from IDs into action indices
                action_dependencies[action_head_idx][action_idx] = {
                    key: value
                    for key, value in {
                        "require_all": [
                            action_indices[dep_id]
                            for dep_id in action.dependencies.require_all
                        ],
                        "require_any": [
                            action_indices[dep_id]
                            for dep_id in action.dependencies.require_any
                        ],
                        "require_none": [
                            action_indices[dep_id]
                            for dep_id in action.dependencies.require_none
                        ],
                    }.items()
                    if value
                }
        return action_dependencies


_custom_registry: dict[str, str] = {}


@lru_cache
def load_environment_contract(environment_name: str) -> EnvironmentMeta:
    if environment_name in _custom_registry:
        contract_file = _custom_registry[environment_name]
    else:
        contract_file = f"{contracts}/environments/{environment_name}.json"

    if not os.path.exists(contract_file):
        raise ValueError(
            f"Unknown environment: {environment_name}, supported environments: {get_env_types()}"
        )

    with open(contract_file, "r") as f:
        env_meta = json.load(f)

    return dacite.from_dict(EnvironmentMeta, env_meta, Config(strict=True))


def register_environment_contract(
    environment_name: str, contract_file_path: str
) -> None:
    if environment_name in _custom_registry:
        raise ValueError(
            f"{environment_name} already registered to {_custom_registry[environment_name]}"
        )
    _custom_registry[environment_name] = contract_file_path


def get_env_types() -> list[str]:
    return list(
        {
            *[
                f.replace(".json", "")
                for f in os.listdir(f"{contracts}/environments/")
                if f.endswith(".json")
            ],
            *_custom_registry.keys(),
        }
    )
