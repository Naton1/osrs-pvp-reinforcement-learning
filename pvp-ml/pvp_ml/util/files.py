import os
import re
from collections.abc import Callable

from pvp_ml import package_root
from pvp_ml.ppo.ppo import PPO, Meta

models_dir: str = f"{package_root}/models"
experiments_dir: str = f"{package_root}/experiments"
tensorboard_dir: str = f"{package_root}/tensorboard"
reference_dir: str = f"{package_root}/reference"


def get_experiment_dir(experiment_name: str) -> str:
    return f"{experiments_dir}/{experiment_name}"


def get_tensorboard_dir(experiment_name: str) -> str:
    return f"{tensorboard_dir}/{experiment_name}"


def get_experiment_models_dir(experiment_name: str) -> str:
    return f"{experiments_dir}/{experiment_name}/models"


def get_file_name_pattern(model_prefix: str = "main") -> re.Pattern[str]:
    return re.compile(f"{model_prefix}-(\\d+)-steps.zip")


def get_model_file_name(model_prefix: str, trained_steps: int) -> str:
    return f"{model_prefix}-{trained_steps}-steps.zip"


def get_most_recent_model(
    experiment_name: str, model_prefix: str = "main"
) -> str | None:
    train_models_dir = get_experiment_models_dir(experiment_name)
    save_file_pattern = f"{model_prefix}-(\\d+)-steps.zip"
    pattern = re.compile(save_file_pattern)
    if not os.path.exists(train_models_dir):
        return None
    matches = [pattern.match(s) for s in os.listdir(train_models_dir)]
    possible = [m for m in matches if m]
    if not possible:
        return None
    latest = max(possible, key=lambda x: int(x.group(1)))
    return f"{train_models_dir}/{latest.group(0)}"


def get_model_files(
    experiment_name: str, name_search_pattern: str = ".*?"
) -> list[str]:
    train_models_dir = get_experiment_models_dir(experiment_name)
    save_file_pattern = f"{name_search_pattern}-(\\d+)-steps.zip"
    pattern = re.compile(save_file_pattern)
    if not os.path.exists(train_models_dir):
        return []
    models_file_possible_matches = [
        pattern.match(s) for s in os.listdir(train_models_dir)
    ]
    models_file_matches = [m for m in models_file_possible_matches if m]
    if not models_file_matches:
        return []
    models_file_matches.sort(key=lambda m: int(m.group(1)))
    return [f"{train_models_dir}/{m.group(0)}" for m in models_file_matches]


def modify_models(
    experiment_name: str,
    transform_fn: Callable[[PPO], bool],
    name_search_pattern: str = ".*?",
) -> None:
    for model_file in get_model_files(experiment_name, name_search_pattern):
        model = PPO.load(model_file, device="cpu")
        if transform_fn(model):
            model.save(model_file)


def modify_model_meta(
    experiment_name: str,
    transform_fn: Callable[[Meta], bool],
    name_search_pattern: str = ".*?",
) -> None:
    for model_file in get_model_files(experiment_name, name_search_pattern):
        meta = PPO.load_meta(model_file)
        if transform_fn(meta):
            PPO.save_meta(model_file, meta)
