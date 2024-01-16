import json
import os.path
import shutil
from contextlib import contextmanager
from typing import Iterator

import pytest

from pvp_ml.ppo.ppo import PPO
from pvp_ml.run_train_job import EXPERIMENT_TRACKER, main
from pvp_ml.util.files import (
    get_experiment_dir,
    get_most_recent_model,
    get_tensorboard_dir,
)


@contextmanager
def experiment(experiment_name: str, ensure_dirs_created: bool = True) -> Iterator[str]:
    test_experiment_dir = get_experiment_dir(experiment_name)
    test_tensorboard_dir = get_tensorboard_dir(experiment_name)
    shutil.rmtree(test_experiment_dir, ignore_errors=True)
    shutil.rmtree(test_tensorboard_dir, ignore_errors=True)
    try:
        yield experiment_name
        if ensure_dirs_created:
            assert os.path.exists(test_experiment_dir)
            assert os.path.exists(test_tensorboard_dir)
    finally:
        shutil.rmtree(test_experiment_dir, ignore_errors=True)
        shutil.rmtree(test_tensorboard_dir, ignore_errors=True)


def test_simple_train() -> None:
    with experiment("test-simple-train") as experiment_name:
        main(
            [
                "train",
                "--name",
                experiment_name,
                "--preset",
                "Test",
                "--wait",
                "--override",
                "--early-stopping",
                json.dumps(
                    {
                        "type": "expression",
                        "expression": "t == 2",
                    }
                ),
            ]
        )
        trained_model = get_most_recent_model(experiment_name)
        assert trained_model
        ppo_meta = PPO.load_meta(trained_model)
        assert ppo_meta.trained_rollouts == 2
        assert ppo_meta.trained_steps == 2560
        assert ppo_meta.num_updates == 20


def test_distribute_train() -> None:
    with experiment("test-distribute-train") as experiment_name:
        main(
            [
                "train",
                "--name",
                experiment_name,
                "--preset",
                "Test",
                "--distribute",
                "2",
                "--wait",
                "--override",
                "--num-cpus-per-rollout",
                "0",
                "--early-stopping",
                json.dumps(
                    {
                        "type": "expression",
                        "expression": "t == 2",
                    }
                ),
            ]
        )

        trained_model = get_most_recent_model(experiment_name)
        assert trained_model
        ppo_meta = PPO.load_meta(trained_model)
        assert ppo_meta.trained_rollouts == 2
        assert ppo_meta.trained_steps == 5120
        assert ppo_meta.num_updates == 40


def test_train_cleanup() -> None:
    with experiment("test-cleanup", ensure_dirs_created=False) as experiment_name:
        # Run experiment
        main(
            [
                "train",
                "--name",
                experiment_name,
                "--preset",
                "Test",
            ]
        )
        # Ensure train job registered
        with open(EXPERIMENT_TRACKER, "r") as f:
            file_content = f.read()
        assert experiment_name in file_content
        # Cleanup experiment
        main(
            [
                "cleanup",
                "--name",
                experiment_name,
            ]
        )
        # Ensure train job cleaned up
        with open(EXPERIMENT_TRACKER, "r") as f:
            file_content = f.read()
        assert experiment_name not in file_content


def test_show(caplog: pytest.LogCaptureFixture) -> None:
    main(
        [
            "show",
        ]
    )
    assert "Running Experiments" in caplog.text
