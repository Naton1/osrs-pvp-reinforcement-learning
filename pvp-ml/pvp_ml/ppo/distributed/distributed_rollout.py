import json
import os
import shutil
import subprocess
import sys
import time
from pathlib import Path
from typing import cast

import ray
from ray.actor import ActorClass

from pvp_ml.util.compression_helper import unzip

# Mark distributed experiments, so we can clean them up later if abandoned
_MARKER_FILE_NAME = "distributed.marker"
_ONE_DAY_SECONDS = 24 * 60 * 60


@ray.remote
class _RolloutActor:
    def __init__(self, experiment_name: str, preset: str):
        self._repo_dir = self._determine_repo_dir()
        self._experiment_name = experiment_name
        self._experiment_dir = f"{self._repo_dir}/pvp-ml/experiments/{experiment_name}"
        self._preset = preset
        self._tensorboard_dir = (
            f"{self._repo_dir}/pvp-ml/tensorboard/{self._experiment_name}"
        )

    def sync_experiment(self, experiment_dir_zip: bytes, experiment_name: str) -> None:
        print(
            f"Syncing experiment {experiment_name} for {self._experiment_name} - {len(experiment_dir_zip)} bytes"
        )

        experiment_dir = f"{self._repo_dir}/pvp-ml/experiments/{experiment_name}"

        if os.path.exists(experiment_dir):
            shutil.rmtree(experiment_dir)

        unzip(experiment_dir_zip, experiment_dir)

        tensorboard_dir = f"{self._repo_dir}/pvp-ml/tensorboard/{experiment_name}"

        if os.path.exists(tensorboard_dir):
            shutil.rmtree(tensorboard_dir, ignore_errors=True)

        # Create empty marker file
        with open(f"{experiment_dir}/{_MARKER_FILE_NAME}", "w") as _:
            pass

        print(f"Synced experiment {experiment_name} for {self._experiment_name}")

    def collect_rollout(self) -> tuple[bytes, bytes]:
        try:
            self._clean_abandoned_experiments()
            self._run_rollout()
            return self._collect_artifacts()
        finally:
            self._cleanup()

    def _run_rollout(self) -> None:
        print(f"Starting rollout {self._experiment_name}")
        early_stopping = {
            "type": "expression",
            "expression": "phase == 'on_rollout_sampling_end'",
            "defaults": {"phase": "NONE"},
        }
        self._run_train_job(
            "--name",
            self._experiment_name,
            "--preset",
            self._preset,
            "--wait",
            "--no-tensorboard",
            "--override",
            "--early-stopping",
            json.dumps(early_stopping),
            "--save-latest-buffer",
            "--save-latest-meta",
            # Disable training adversary. Otherwise, if the config has adversaries, it spawns them for each job.
            "--train-main-exploiter",
            "false",
            "--train-league-exploiter",
            "false",
        )
        print(f"Finished rollout {self._experiment_name}")

    def _collect_artifacts(self) -> tuple[bytes, bytes]:
        rollout_file = f"{self._experiment_dir}/last_rollout_buffer.zip"
        with open(rollout_file, "rb") as f:
            rollout = f.read()

        meta_file = f"{self._experiment_dir}/last_rollout_meta.zip"
        with open(meta_file, "rb") as f:
            meta = f.read()

        print(
            f"Collected rollout ({len(rollout)} bytes), meta ({len(meta)} bytes)"
            f" for {self._experiment_name}"
        )

        return rollout, meta

    def _cleanup(self) -> None:
        print(f"Running cleanup for {self._experiment_name}")
        self._run_train_job("cleanup")
        shutil.rmtree(self._experiment_dir, ignore_errors=True)
        shutil.rmtree(self._tensorboard_dir, ignore_errors=True)

    def _run_train_job(self, *args: str) -> None:
        env = {
            **os.environ,
            # Tell the job to use this cluster for ray work
            "RAY_ADDRESS": ray.get_runtime_context().gcs_address,
        }
        subprocess.run(
            [
                sys.executable,
                f"{self._repo_dir}/pvp-ml/pvp_ml/run_train_job.py",
                *args,
            ],
            cwd=f"{self._repo_dir}/pvp-ml",
            check=True,
            env=env,
        )

    def _clean_abandoned_experiments(self) -> None:
        # Cleans up 'abandoned' distributed experiments by checking for a marker file created at least a day ago
        experiments_dir = f"{self._repo_dir}/pvp-ml/experiments/"
        for experiment_name in os.listdir(experiments_dir):
            experiment_dir = f"{experiments_dir}/{experiment_name}"
            marker_file = f"{experiment_dir}/{_MARKER_FILE_NAME}"
            if (
                os.path.exists(marker_file)
                and time.time() - os.path.getctime(marker_file) > _ONE_DAY_SECONDS
            ):
                print(f"Cleaning up abandoned experiment {experiment_name}")
                shutil.rmtree(experiment_dir, ignore_errors=True)
                tensorboard_dir = (
                    f"{self._repo_dir}/pvp-ml/tensorboard/{experiment_name}"
                )
                shutil.rmtree(tensorboard_dir, ignore_errors=True)

    @staticmethod
    def _determine_repo_dir() -> str:
        # Check env var first, so that distributing on same machine can run from anywhere.
        # The main driver process will set this env var.
        env_exp_dir = os.getenv("REPO_DIR")
        if env_exp_dir and os.path.exists(env_exp_dir):
            return env_exp_dir
        # We expect that this repo is cloned at <user-home>/osrs-pvp-reinforcement-learning
        # and that the Ray job is running in the conda env already.
        expected_repo_dir = f"{str(Path.home())}/osrs-pvp-reinforcement-learning"
        if os.path.exists(expected_repo_dir):
            return expected_repo_dir
        raise ValueError("Unable to determine repo directory for rollout")


# Ray type hints are incorrect for actors with ray.remote
RolloutActor: ActorClass = cast(ActorClass, _RolloutActor)
