import logging
import os
import shutil
import subprocess
import sys
import threading
import time

import numpy as np

from pvp_ml import package_root
from pvp_ml.ppo.ppo import PPO
from pvp_ml.util.files import (
    get_experiment_dir,
    get_model_files,
    get_most_recent_model,
    tensorboard_dir,
)

logger = logging.getLogger(__name__)


class AdversaryTrainer:
    """
    Simplified version of DeepMind's Starcraft 2 League
    """

    def run_main_exploiter_trainer(
        self,
        experiment_name: str,
        preset: str | None,
        index: int,
        distribute: bool,
        num_distributed_rollouts: int,
    ) -> threading.Thread:
        job_runner = threading.Thread(
            target=self._run_main_exploiter_trainer,
            daemon=True,
            name=f"main-exploiter-{index}",
            args=(experiment_name, preset, index, distribute, num_distributed_rollouts),
        )
        job_runner.start()
        return job_runner

    def run_league_exploiter_trainer(
        self,
        experiment_name: str,
        preset: str | None,
        index: int,
        distribute: bool,
        num_distributed_rollouts: int,
    ) -> threading.Thread:
        job_runner = threading.Thread(
            target=self._run_league_exploiter_trainer,
            daemon=True,
            name=f"league-exploiter-{index}",
            args=(experiment_name, preset, index, distribute, num_distributed_rollouts),
        )
        job_runner.start()
        return job_runner

    def _run_main_exploiter_trainer(
        self,
        experiment_name: str,
        preset: str | None,
        index: int,
        distribute: bool,
        num_distributed_rollouts: int,
    ) -> None:
        self._run_exploiter_trainer(
            experiment_name,
            preset,
            exploiter_name=f"main-exploiter-{index}",
            bootstrap_source_model_experiment=experiment_name,
            distribute=distribute,
            num_distributed_rollouts=num_distributed_rollouts,
            extra_params=[
                "--latest-self-play-percent",
                "1.0",
                "--latest-self-play-experiment",
                experiment_name,
            ],
        )

    def _run_league_exploiter_trainer(
        self,
        experiment_name: str,
        preset: str | None,
        index: int,
        distribute: bool,
        num_distributed_rollouts: int,
    ) -> None:
        self._run_exploiter_trainer(
            experiment_name,
            preset,
            exploiter_name=f"league-exploiter-{index}",
            bootstrap_source_model_experiment=experiment_name,
            distribute=distribute,
            num_distributed_rollouts=num_distributed_rollouts,
            extra_params=[
                "--past-self-play-percent",
                "1.0",
                "--past-self-play-experiment",
                experiment_name,
            ],
        )

    def _run_exploiter_trainer(
        self,
        experiment_name: str,
        preset: str | None,
        exploiter_name: str,
        bootstrap_source_model_experiment: str | None,
        extra_params: list[str] = [],
        continue_training_model: str | None = None,
        select_latest_bootstrap_source: bool = True,
        distribute: bool = False,
        num_distributed_rollouts: int = 1,
        num_rollouts: int = 25,
        reset_probability: float = 0.25,
    ) -> None:
        logger.info(f"Initializing exploiter: {exploiter_name}")
        adversary_experiment_name = f"{experiment_name}-{exploiter_name}"
        # Clean up any previously-existing artifacts
        shutil.rmtree(get_experiment_dir(adversary_experiment_name), ignore_errors=True)
        shutil.rmtree(
            f"{tensorboard_dir}/{adversary_experiment_name}", ignore_errors=True
        )

        while True:
            # Setup params
            logger.info(f"Starting adversary {adversary_experiment_name}...")

            source_model: str | None
            if continue_training_model:
                source_model = continue_training_model
            elif bootstrap_source_model_experiment is not None:
                if select_latest_bootstrap_source:
                    source_model = get_most_recent_model(
                        bootstrap_source_model_experiment
                    )
                else:
                    possible_targets = get_model_files(
                        bootstrap_source_model_experiment,
                        name_search_pattern="main",
                    )
                    source_model = (
                        np.random.choice(possible_targets) if possible_targets else None
                    )
            else:
                source_model = None

            command = [
                sys.executable,
                f"{package_root}/pvp_ml/run_train_job.py",
                "--wait",
                "--name",
                adversary_experiment_name,
                *(["--preset", preset] if preset else []),
                *(
                    ["--distribute", str(num_distributed_rollouts)]
                    if distribute
                    else []
                ),
                "--override",
                *(["--load-file", source_model] if source_model else []),
                "--train-rollouts",
                str(num_rollouts),
                "--self-play-percent",
                "0.0",  # Defaults to 1.0
                *extra_params,
                "--allow-cleanup",  # This is all automated so always allow cleanup if needed
            ]

            # Run training
            env = {
                **os.environ,
            }

            if "ray" in sys.modules or distribute:
                # Tell the job to use this cluster for ray work
                from pvp_ml.util import ray_helper

                ray_helper.init()
                import ray

                gcs_address = ray.get_runtime_context().gcs_address
                env["RAY_ADDRESS"] = gcs_address
                logger.info(
                    f"Running adversary '{exploiter_name}' in existing ray cluster: {gcs_address}"
                )
            self.__run_adversary(command, env)

            # Move trained model to pool - add time to model to prevent conflicts
            trained_adversary = get_most_recent_model(adversary_experiment_name)
            logger.info(
                f"Found latest adversary model for {adversary_experiment_name}: {trained_adversary}"
            )
            assert trained_adversary is not None
            # Replace directory first, then replace file name
            destination_model = trained_adversary.replace(
                adversary_experiment_name, experiment_name, 1
            ).replace("main", f"{exploiter_name}-{int(time.time())}", 1)
            shutil.copyfile(trained_adversary, destination_model)
            PPO.optimize_for_inference(destination_model)
            logger.info(
                f"Injected model into current experiment: {trained_adversary} -> {destination_model}"
            )

            # Clean up
            if np.random.random() < reset_probability:
                logger.info(f"Resetting adversary {adversary_experiment_name}")
                shutil.rmtree(get_experiment_dir(adversary_experiment_name))
                shutil.rmtree(f"{tensorboard_dir}/{adversary_experiment_name}")
                continue_training_model = None
            else:
                logger.info(
                    f"Continuing training adversary model {adversary_experiment_name}"
                )
                continue_training_model = trained_adversary

    def __run_adversary(self, command: list[str], env: dict[str, str]) -> None:
        logger.info(f"Running adversary: {command}")

        completed_process = subprocess.run(
            command, env=env, cwd=package_root, check=True
        )

        logger.info(f"Adversary finished: {command} - {completed_process.returncode}")
