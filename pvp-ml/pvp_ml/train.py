"""
This script will train a PvP ML agent using reinforcement learning.
Also see `./run_train_job.py` to orchestrate the training process (including spinning up a simulation).
"""
import argparse
import asyncio
import json
import logging
import os
import shutil
import sys
import threading
from collections.abc import Callable
from typing import Any

import torch as th

from pvp_ml import package_root
from pvp_ml.callback.callback import EndTrainingException
from pvp_ml.callback.checkpoint_callback import CheckpointCallback
from pvp_ml.callback.dynamic_tracker_callback import DynamicTrackerCallback
from pvp_ml.callback.early_stopping_callback import EarlyStoppingCallback
from pvp_ml.callback.env_tracker_callback import EnvTrackerCallback
from pvp_ml.callback.episode_accumulator_callback import EpisodeAccumulatorCallback
from pvp_ml.callback.eval_callback import EvalCallback
from pvp_ml.callback.exploiter_callback import ExploiterCallback
from pvp_ml.callback.latest_meta_logger_callback import LatestMetaLoggerCallback
from pvp_ml.callback.latest_self_play_callback import LatestSelfPlayCallback
from pvp_ml.callback.logging_callback import LoggingCallback
from pvp_ml.callback.past_self_play_callback import PastSelfPlayCallback
from pvp_ml.callback.reference_rating_callback import ReferenceRatingCallback
from pvp_ml.callback.reward_tracker_callback import RewardTrackerCallback
from pvp_ml.callback.save_buffer_callback import SaveBufferCallback
from pvp_ml.callback.save_meta_callback import SaveMetaCallback
from pvp_ml.callback.target_self_play_callback import TargetSelfPlayCallback
from pvp_ml.env.async_io_vec_env import AsyncIoVecEnv
from pvp_ml.env.pvp_env import PvpEnv, ResetOptions
from pvp_ml.ppo.distributed.distributed_rollout_sampler import DistributedRolloutSampler
from pvp_ml.ppo.ext.win_rate_extension import WinRateExtension
from pvp_ml.ppo.ppo import PPO, PolicyParams
from pvp_ml.ppo.rollout_sampler import RolloutSampler
from pvp_ml.ppo.trainer import Trainer
from pvp_ml.util.args_helper import (
    replace_dash_with_underscore,
    strtobool,
    union_int_or_int_list,
)
from pvp_ml.util.contract_loader import get_env_types
from pvp_ml.util.files import (
    get_experiment_dir,
    get_experiment_models_dir,
    get_most_recent_model,
    tensorboard_dir,
)
from pvp_ml.util.json_encoders import GeneralizedObjectEncoder
from pvp_ml.util.mlp_helper import default_mlp_config
from pvp_ml.util.model_extensions import WIN_RATE_EXTENSION, manage_extension
from pvp_ml.util.noise_generator import noise_generator
from pvp_ml.util.remote_processor.remote_processor import (
    RAY_REMOTE_PROCESSOR,
    REMOTE_PROCESSOR_TYPES,
    THREAD_REMOTE_PROCESSOR,
    create_remote_processor,
)
from pvp_ml.util.scalar_tracking_summary_writer import ScalarTrackingSummaryWriter
from pvp_ml.util.schedule import ConstantSchedule, Schedule, schedule
from pvp_ml.util.server_debug_tracker import ServerDebugTracker
from pvp_ml.util.traceback_tracker import track_tracebacks

logger = logging.getLogger(__name__)


def train(
    train_rollouts: int | None,
    num_envs: Schedule[int],
    experiment_name: str,
    load_file: str | None,
    self_play_percent: Schedule[float],
    past_self_play_percent: Schedule[float],
    past_self_play_deterministic_percent: Schedule[float],
    past_self_play_experiment: str,
    past_self_play_learning_rate: Schedule[float],
    past_self_play_delay_chance: Schedule[float],
    past_self_override_kwargs: dict[str, Any],
    add_past_self_play_league_targets: bool,
    target_self_play_percent: Schedule[float],
    target_self_play_deterministic_percent: Schedule[float],
    latest_self_play_percent: Schedule[float],
    latest_self_play_experiment: str | None,
    latest_self_play_deterministic_percent: Schedule[float],
    latest_self_play_delay_chance: Schedule[float],
    latest_self_override_kwargs: dict[str, Any],
    targets: list[str],
    num_eval_agent: Schedule[int],
    eval_override_kwargs: dict[str, Any],
    eval_deterministic_percent: Schedule[float],
    remote_processor_pool_size: int,
    remote_processor_device: str,
    remote_processor_type: str,
    remote_processor_kwargs: dict[str, Any],
    num_rollout_steps: Schedule[int],
    env_kwargs: dict[str, Any],
    device: str,
    continue_training: bool,
    policy_kwargs: dict[str, Any],
    batch_size: Schedule[int],
    grad_accum: Schedule[int],
    checkpoint_frequency: Schedule[int],
    optimize_old_models: bool,
    eps_greedy: Schedule[float],
    epochs: Schedule[int],
    learning_rate: Schedule[float],
    gamma: Schedule[float],
    gae_lambda: Schedule[float],
    clip_coef: Schedule[float],
    value_coef: Schedule[float],
    entropy_coef: Schedule[float],
    max_grad_norm: Schedule[float],
    train_main_exploiter: bool,
    num_main_exploiters: int,
    main_exploiter_preset: str,
    main_exploiter_delay: int,
    train_league_exploiter: bool,
    num_league_exploiters: int,
    league_exploiter_preset: str,
    league_exploiter_delay: int,
    forward_distribution_to_exploiters: bool,
    allow_cleanup: bool,
    experiment_meta: dict[str, Any],
    remote_env_host: str,
    remote_env_port: int,
    early_stopping: Schedule[bool],
    normalize_advantages: bool,
    normalize_rewards: bool,
    normalize_observations: bool,
    save_buffer: bool,
    save_meta: bool,
    distributed_rollouts: bool,
    distributed_rollout_preset: str,
    num_distributed_rollouts: int,
    num_cpus_per_rollout: int,
    novelty_reward_scale: Schedule[float],
    bootstrap_from_experiment: str,
    num_reference_rating_envs: Schedule[int],
    enable_tracking_histograms: bool,
    add_win_rate_extension: bool,
) -> None:
    logger.info(f"Running experiment {experiment_name}")

    if past_self_play_experiment is None:
        past_self_play_experiment = experiment_name
    if latest_self_play_experiment is None:
        latest_self_play_experiment = experiment_name

    experiment_dir = get_experiment_dir(experiment_name)
    experiment_models_dir = get_experiment_models_dir(experiment_name)

    if continue_training:
        if load_file:
            logger.info(
                f"Skipping using latest save because an explicit save file was given: {load_file}"
            )
        else:
            load_from = (
                bootstrap_from_experiment
                if bootstrap_from_experiment
                else experiment_name
            )
            load_file = get_most_recent_model(load_from)
            if load_file:
                logger.info(f"Loading latest model for {load_from}: {load_file}")
            else:
                logger.info(
                    f"No latest save found for {load_from}, starting new experiment instead"
                )

    tensorboard_log_dir = f"{tensorboard_dir}/{experiment_name}"
    if not load_file:
        logger.info(
            f"Starting new experiment: {experiment_name}, cleaning up any leftover artifacts from previous experiments"
        )
        if os.path.exists(experiment_dir):
            assert (
                allow_cleanup
            ), f"Experiment directory for {experiment_name} already exists, must allow cleanup"
            logger.info(f"Cleaning up old experiment dir: {experiment_dir}")
            shutil.rmtree(experiment_dir)
        if os.path.exists(tensorboard_log_dir):
            assert (
                allow_cleanup
            ), f"Experiment metrics for {experiment_name} already exists, must allow cleanup"
            logger.info(
                f"Cleaning old tensorboard logs for experiment {experiment_name}: {tensorboard_log_dir}"
            )
            shutil.rmtree(tensorboard_log_dir)
    summary_writer = ScalarTrackingSummaryWriter(
        tensorboard_log_dir, enable_tracking_histograms=enable_tracking_histograms
    )

    os.makedirs(experiment_dir, exist_ok=True)

    track_tracebacks(f"{experiment_dir}/traceback-dump.txt", log_frequency_seconds=300)

    with open(f"{experiment_dir}/experiment-meta.json", "w") as f:
        # Save the experiment params to a file for future reference
        json.dump(experiment_meta, f, cls=GeneralizedObjectEncoder, indent=2)

    loop = asyncio.new_event_loop()

    def _run_event_loop() -> None:
        try:
            asyncio.set_event_loop(loop)
            loop.run_forever()
        except Exception:
            logger.exception("Error while running event loop")
        finally:
            logger.info("Event loop finished")

    loop_thread = threading.Thread(
        target=_run_event_loop,
        name="asyncio-loop-runner",
    )
    loop_thread.start()

    if distributed_rollouts:
        server_debugger_task = None
    else:
        server_debugger = ServerDebugTracker.run(
            host=remote_env_host, port=remote_env_port, experiment_name=experiment_name
        )
        server_debugger_task = asyncio.run_coroutine_threadsafe(server_debugger, loop)

    def create_vec_env_fn(trained_steps: int, trained_rollouts: int) -> AsyncIoVecEnv:
        if distributed_rollouts:
            # Create basic env for the metadata
            return AsyncIoVecEnv([lambda: PvpEnv(**env_kwargs)], loop=loop)

        current_self_play_percent = self_play_percent.value(trained_rollouts)
        current_past_self_play_percent = past_self_play_percent.value(trained_rollouts)
        current_target_self_play_percent = target_self_play_percent.value(
            trained_rollouts
        )
        current_latest_self_play_percent = latest_self_play_percent.value(
            trained_rollouts
        )
        current_num_envs = num_envs.value(trained_rollouts)

        assert (
            current_self_play_percent
            + current_past_self_play_percent
            + current_target_self_play_percent
            + current_latest_self_play_percent
            <= 1.0
        ), (
            f"Env percents are greater than 1, "
            f"{current_self_play_percent}, "
            f"{current_past_self_play_percent}, "
            f"{current_target_self_play_percent}, "
            f"{current_latest_self_play_percent}"
        )
        assert current_past_self_play_percent >= 0
        assert current_self_play_percent >= 0
        assert current_target_self_play_percent >= 0
        assert current_latest_self_play_percent >= 0
        assert current_num_envs > 0

        num_selfs = round(current_num_envs * current_self_play_percent)
        assert (
            num_selfs % 2 == 0
        ), f"Self play count must be even so everyone has a target: {num_selfs}"

        num_past_selfs = round(current_num_envs * current_past_self_play_percent)
        num_specific_models = round(current_num_envs * current_target_self_play_percent)
        num_latest_models = round(current_num_envs * current_latest_self_play_percent)

        num_baseline = (
            current_num_envs
            - num_selfs
            - num_past_selfs
            - num_specific_models
            - num_latest_models
        )

        logger.info(
            f"Training {num_selfs} environments on self, {num_past_selfs} on old versions of self,"
            f" {num_specific_models} on specific past models,"
            f" {num_latest_models} on the latest model of an experiment,"
            f" {num_baseline} on a baseline,"
            f" and {num_eval_agent} eval agents on a baseline"
        )

        summary_writer.add_scalar("env/num_envs", current_num_envs, trained_steps)
        summary_writer.add_scalar("env/num_self_play", num_selfs, trained_steps)
        summary_writer.add_scalar(
            "env/num_past_self_play", num_past_selfs, trained_steps
        )
        summary_writer.add_scalar(
            "env/num_specific_target_self_play", num_specific_models, trained_steps
        )
        summary_writer.add_scalar(
            "env/num_latest_target_self_play", num_latest_models, trained_steps
        )
        summary_writer.add_scalar("env/num_baseline", num_baseline, trained_steps)

        past_self_play_targets.clear()
        target_self_play_targets.clear()
        latest_self_play_targets.clear()

        def create_env_fn(index: int) -> Callable[[], PvpEnv]:
            env_id = str(index)
            if index < num_selfs:
                # Play against self
                target = f"{num_selfs - index - 1}"
            elif index < num_selfs + num_past_selfs:
                # Play against past self
                target = f"O {index}"
                past_self_play_targets[target] = env_id
            elif index < num_selfs + num_past_selfs + num_specific_models:
                # Play against target experiment
                target = f"M {index}"
                target_self_play_targets[target] = env_id
            elif (
                index
                < num_selfs + num_past_selfs + num_specific_models + num_latest_models
            ):
                # Play against latest model of experiment
                target = f"L {index}"
                latest_self_play_targets[target] = env_id
            else:
                # Play against baseline
                target = "baseline"

            def _create_env() -> PvpEnv:
                pvp_env = PvpEnv(
                    env_id=env_id, target=target, training=True, **env_kwargs
                )
                if index == 0:
                    # Log stats for a single env every rollout
                    pvp_env.log(trained_rollouts, trained_steps, summary_writer)
                return pvp_env

            return _create_env

        return AsyncIoVecEnv(
            env_fns=[create_env_fn(i) for i in range(0, current_num_envs)],
            loop=loop,
            reset_options=dict(
                ResetOptions(
                    trained_steps=trained_steps, trained_rollouts=trained_rollouts
                )
            ),
        )

    # Let's not have ray try to manage GPUs
    os.environ["RAY_EXPERIMENTAL_NOSET_CUDA_VISIBLE_DEVICES"] = "true"
    # Tell distributed jobs on same machine where the project to run is
    os.environ["REPO_DIR"] = os.path.dirname(package_root)

    # Initialize remote processor (and it's not needed in some cases, so optimize for that)
    remote_processor_task = (
        create_remote_processor(
            pool_size=remote_processor_pool_size,
            device=remote_processor_device,
            processor_type=remote_processor_type,
            remote_processor_additional_params=remote_processor_kwargs,
        )
        if remote_processor_pool_size > 0
        and (not distributed_rollouts or remote_processor_type == RAY_REMOTE_PROCESSOR)
        else create_remote_processor()
    )
    remote_processor = asyncio.run_coroutine_threadsafe(
        remote_processor_task, loop
    ).result()

    try:
        tmp_env = PvpEnv(**env_kwargs)
        if load_file:
            ppo = PPO.load(load_file, device=device, trainable=True)
        else:
            ppo = PPO.new_instance(
                policy_params=PolicyParams(
                    actor_input_size=tmp_env.partial_observation_space.shape[1],
                    critic_input_size=tmp_env.observation_space.shape[1],
                    action_head_sizes=tmp_env.action_space.nvec.tolist(),
                    max_sequence_length=tmp_env.observation_space.shape[0],
                    action_dependencies=tmp_env.action_dependencies,
                    **policy_kwargs,
                ),
                device=device,
                normalize_observations=normalize_observations,
            )

        summary_writer.set_model(ppo)

        with open(f"{experiment_dir}/arch.txt", "w") as f:
            f.write(str(ppo))

        manage_extension(
            WIN_RATE_EXTENSION,
            ppo,
            lambda: WinRateExtension(
                input_size=tmp_env.partial_observation_space.shape[1],
                max_sequence_length=tmp_env.partial_observation_space.shape[0],
            ),
            add_extension=add_win_rate_extension,
        )

        eval_env_kwargs = {
            **env_kwargs,
            **eval_override_kwargs,
        }

        # Save env info for eval
        save_env_kwarg_keys = eval_env_kwargs.keys() - {
            PvpEnv.REMOTE_ENV_HOST_KEY,
            PvpEnv.REMOTE_ENV_PORT_KEY,
        }
        ppo.meta.custom_data["env_kwargs"] = {
            k: eval_env_kwargs[k] for k in save_env_kwarg_keys
        }
        ppo.meta.custom_data["env_meta"] = tmp_env.meta

        past_self_play_targets: dict[str, str] = {}
        target_self_play_targets: dict[str, str] = {}
        latest_self_play_targets: dict[str, str] = {}

        callbacks = [
            LoggingCallback(),  # Always log first
            CheckpointCallback(  # Save model before launching old selfs (so we have at least 1)
                save_path=experiment_models_dir,
                frequency=checkpoint_frequency,
                make_old_models_untrainable=optimize_old_models,
            ),
            EpisodeAccumulatorCallback(),
            DynamicTrackerCallback(),
            RewardTrackerCallback(),
            EnvTrackerCallback(experiment_name=experiment_name),
            LatestMetaLoggerCallback(experiment_name=experiment_name),
            PastSelfPlayCallback(
                past_self_targets=past_self_play_targets,
                past_self_play_deterministic_percent=past_self_play_deterministic_percent,
                past_self_play_learning_rate=past_self_play_learning_rate,
                experiment_name=experiment_name,
                train_on_experiment_name=past_self_play_experiment,
                loop=loop,
                remote_processor=remote_processor,
                env_kwargs={
                    **env_kwargs,
                    **past_self_override_kwargs,
                },
                delay_chance=past_self_play_delay_chance,
                include_new_targets_in_league=add_past_self_play_league_targets,
            ),
            TargetSelfPlayCallback(
                model_choices=targets,
                past_self_targets=target_self_play_targets,
                self_play_deterministic_percent=target_self_play_deterministic_percent,
                loop=loop,
                remote_processor=remote_processor,
                env_kwargs=env_kwargs,
                experiment_name=experiment_name,
            ),
            LatestSelfPlayCallback(
                past_self_targets=latest_self_play_targets,
                self_play_deterministic_percent=latest_self_play_deterministic_percent,
                train_on_latest_experiment=latest_self_play_experiment,
                delay_chance=latest_self_play_delay_chance,
                loop=loop,
                remote_processor=remote_processor,
                env_kwargs={
                    **env_kwargs,
                    **latest_self_override_kwargs,
                },
                experiment_name=experiment_name,
            ),
            EvalCallback(
                num_eval_envs=ConstantSchedule(0)
                if distributed_rollouts
                else num_eval_agent,
                experiment_name=experiment_name,
                eval_deterministic_percent=eval_deterministic_percent,
                loop=loop,
                remote_processor=remote_processor,
                env_kwargs=eval_env_kwargs,
            ),
            ExploiterCallback(
                experiment_name=experiment_name,
                train_main_exploiter=train_main_exploiter,
                num_main_exploiters=num_main_exploiters,
                main_exploiter_preset=main_exploiter_preset,
                main_exploiter_delay=main_exploiter_delay,
                train_league_exploiter=train_league_exploiter,
                league_exploiter_preset=league_exploiter_preset,
                league_exploiter_delay=league_exploiter_delay,
                num_league_exploiters=num_league_exploiters,
                distribute=distributed_rollouts and forward_distribution_to_exploiters,
                num_distributed_rollouts=num_distributed_rollouts,
            ),
            ReferenceRatingCallback(
                experiment_name=experiment_name,
                num_reference_eval_agents=ConstantSchedule(0)
                if distributed_rollouts
                else num_reference_rating_envs,
                loop=loop,
                remote_processor=remote_processor,
                env_kwargs=env_kwargs,
            ),
            # Save last so all is processed
            *(
                [SaveBufferCallback(experiment_name=experiment_name)]
                if save_buffer
                else []
            ),
            *([SaveMetaCallback(experiment_name=experiment_name)] if save_meta else []),
            EarlyStoppingCallback(  # Check stopping last so all other callbacks run
                stopping_condition=early_stopping
            ),
        ]

        try:
            session_trained_rollouts = 0
            while train_rollouts is None or session_trained_rollouts < train_rollouts:
                logger.info("Running train cycle")
                env = create_vec_env_fn(
                    trained_steps=ppo.meta.trained_steps,
                    trained_rollouts=ppo.meta.trained_rollouts,
                )
                trainer = Trainer()
                if not distributed_rollouts:
                    rollout_sampler = RolloutSampler()
                else:
                    rollout_sampler = DistributedRolloutSampler(
                        experiment_name,
                        distributed_rollout_preset,
                        num_tasks=num_distributed_rollouts,
                        cpus_per_rollout=num_cpus_per_rollout,
                        include_additional_experiments={
                            latest_self_play_experiment,
                            past_self_play_experiment,
                        },
                    )
                try:
                    trainer.train(
                        ppo,
                        env,
                        rollout_sampler,
                        batch_size=batch_size,
                        n_steps=num_rollout_steps.value(ppo.meta.trained_rollouts),
                        n_rollouts=1,
                        callbacks=callbacks,
                        summary_writer=summary_writer,
                        grad_accum=grad_accum,
                        eps_greedy=eps_greedy,
                        num_updates=epochs,
                        gamma=gamma,
                        gae_lambda=gae_lambda,
                        learning_rate=learning_rate,
                        clip_coef=clip_coef,
                        value_coef=value_coef,
                        entropy_coef=entropy_coef,
                        max_grad_norm=max_grad_norm,
                        normalize_advantages=normalize_advantages,
                        normalize_rewards=normalize_rewards,
                        novelty_reward_scale=novelty_reward_scale,
                    )
                    summary_writer.flush()
                    session_trained_rollouts += 1
                except EndTrainingException as e:
                    logger.info(f"Ending training early: {e}")
                    break
                except Exception:
                    # Log separately incase closing environment gets stuck
                    logger.exception("Train threw exception")
                    raise
                finally:
                    env.close()
        finally:
            logger.info("Cleaning up callbacks")
            # Do callback cleanup
            for c in callbacks:
                try:
                    c.on_training_end()
                except EndTrainingException as e:
                    logger.debug(
                        f"Ignoring EndTrainingException by '{c}' because training is ending: {e}"
                    )
    except Exception:
        # Log separately incase cleaning up gets stuck
        logger.exception("Exception thrown while training")
        raise
    finally:
        # Try cleanup no matter what
        logger.info("Cleaning up environments")
        try:
            cleanup_task = asyncio.run_coroutine_threadsafe(
                remote_processor.close(), loop
            )
            cleanup_task.result()
            if server_debugger_task is not None:
                server_debugger_task.cancel()
                try:
                    # Wait to finish
                    server_debugger_task.result()
                except Exception:
                    pass
            loop.call_soon_threadsafe(loop.stop)
            loop_thread.join()
            summary_writer.close()
            logger.info("Training complete")
        except Exception:
            logger.exception("Exception while cleaning up")


def main(argv: list[str]) -> None:
    envs = get_env_types()
    default_experiment_name = "test-experiment"

    parser = argparse.ArgumentParser(description="Training script for PvP models")
    parser.add_argument(
        "--env-name",
        type=str,
        help="Environment type name",
        default="NhEnv",
        choices=list(envs),
    )
    parser.add_argument(
        "--num-envs",
        type=schedule,
        help="Number of environments",
        default=ConstantSchedule(10),
    )
    parser.add_argument(
        "--experiment-name",
        type=str,
        help="Experiment name",
        default=default_experiment_name,
    )
    parser.add_argument(
        "--experiment-description",
        type=str,
        help="Experiment description to be saved in meta file for future reference",
        default="",
    )
    parser.add_argument("--load-file", type=str, help="Load file", default="")
    parser.add_argument(
        "--self-play-percent",
        type=schedule,
        help="Self play percent",
        default=ConstantSchedule(1.0),
    )
    parser.add_argument(
        "--past-self-play-percent",
        type=schedule,
        help="Past self play percent",
        default=ConstantSchedule(0.0),
    )
    parser.add_argument(
        "--past-self-play-learning-rate",
        type=schedule,
        help="Learning rate for past self play",
        default=ConstantSchedule(0.01),
    )
    parser.add_argument(
        "--past-self-play-delay-chance",
        type=schedule,
        help="Delay chance for past self agents",
        default=ConstantSchedule(0.0),
    )
    parser.add_argument(
        "--past-self-play-deterministic-percent",
        type=schedule,
        help="Past self play deterministic percent",
        default=ConstantSchedule(0.0),
    )
    parser.add_argument(
        "--past-self-play-experiment",
        type=str,
        help="Name of experiment models to train on. Defaults to the current experiment.",
        default=None,
    )
    parser.add_argument(
        "--add-past-self-play-league-targets",
        type=lambda x: bool(strtobool(x)),
        nargs="?",
        const=True,
        help="Add new models to the past self play league",
        default=True,
    )
    parser.add_argument(
        "--past-self-override-env-kwargs",
        type=json.loads,
        help="Past self overrides for env kwargs",
        default={},
    )
    parser.add_argument(
        "--target-self-play-percent",
        type=schedule,
        help="Target self play percent",
        default=ConstantSchedule(0.0),
    )
    parser.add_argument(
        "--target",
        action="append",
        help="Self play against specific targets (randomly selected)",
        default=[],
    )
    parser.add_argument(
        "--target-self-play-deterministic-percent",
        type=schedule,
        help="Target self play deterministic percent",
        default=ConstantSchedule(0.0),
    )
    parser.add_argument(
        "--latest-self-play-percent",
        type=schedule,
        help="Latest self play percent",
        default=ConstantSchedule(0.0),
    )
    parser.add_argument(
        "--latest-self-play-experiment",
        type=str,
        help="Experiment name to always play against the latest model",
        default=None,
    )
    parser.add_argument(
        "--latest-self-play-delay-chance",
        type=schedule,
        help="Delay chance for latest target agents",
        default=ConstantSchedule(0.0),
    )
    parser.add_argument(
        "--latest-self-play-deterministic-percent",
        type=schedule,
        help="Latest self play deterministic percent",
        default=ConstantSchedule(0.0),
    )
    parser.add_argument(
        "--latest-self-override-env-kwargs",
        type=json.loads,
        help="Latest self overrides for env kwargs",
        default={},
    )
    parser.add_argument(
        "--num-eval-agent",
        type=schedule,
        help="Number of eval agents",
        default=ConstantSchedule(0),
    )
    parser.add_argument(
        "--eval-deterministic-percent",
        type=schedule,
        help="Eval deterministic percent",
        default=ConstantSchedule(0.0),
    )
    parser.add_argument(
        "--remote-processor-pool-size",
        type=int,
        help="Remote processor pool size",
        default=0,
    )
    parser.add_argument(
        "--num-rollout-steps",
        type=schedule,
        help="Number of steps to sample for each environment per rollout",
        default=ConstantSchedule(256),
    )
    parser.add_argument(
        "--env-kwargs",
        type=json.loads,
        help="Keyword arguments to pass when initializing the environments",
        default={},
    )
    parser.add_argument(
        "--eval-override-env-kwargs",
        type=json.loads,
        help="Eval overrides for env kwargs",
        default={},
    )
    parser.add_argument(
        "--remote-processor-device",
        type=str,
        help="Remote processor device",
        default="cuda" if th.cuda.is_available() else "cpu",
    )
    parser.add_argument(
        "--remote-processor-type",
        type=str,
        help="Remote processor type",
        choices=REMOTE_PROCESSOR_TYPES,
        default=THREAD_REMOTE_PROCESSOR,
    )
    parser.add_argument(
        "--remote-processor-kwargs",
        type=json.loads,
        help="Remote processor kwargs",
        default={},
    )
    parser.add_argument(
        "--device",
        type=str,
        help="Training device",
        default="cuda" if th.cuda.is_available() else "cpu",
    )
    parser.add_argument(
        "--continue-training",
        type=lambda x: bool(strtobool(x)),
        nargs="?",
        const=True,
        help="Helper argument to keep training the latest model for the given experiment",
        default=False,
    )
    parser.add_argument(
        "--remote-env-port",
        type=int,
        help="Remote environment port for the simulation to train on",
        default=7070,
    )
    parser.add_argument(
        "--remote-env-host",
        type=str,
        help="Remote environment host for the simulation to train on",
        default="localhost",
    )
    parser.add_argument(
        "--policy-kwargs",
        type=json.loads,
        help="Keyword arguments to pass when initializing the policy",
        default={},
    )
    parser.add_argument(
        "--stack-frames",
        type=union_int_or_int_list,
        help="Number of frames to stack, or frame indexes to stack (if a list). Index 0 is the current frame, 1 is the last frame, and so on.",
        default=1,
    )
    parser.add_argument(
        "--batch-size",
        type=schedule,
        help="Number of items in each minibatch",
        default=ConstantSchedule(256),
    )
    parser.add_argument(
        "--grad-accum",
        type=schedule,
        help="Number of batches to accumulate and average gradients",
        default=ConstantSchedule(1),
    )
    parser.add_argument(
        "--checkpoint-frequency",
        type=schedule,
        help="Number of model training calls between each save",
        default=ConstantSchedule(1),
    )
    parser.add_argument(
        "--optimize-old-models",
        type=lambda x: bool(strtobool(x)),
        nargs="?",
        const=True,
        help="Optimize old model checkpoints by making them non-trainable",
        default=True,
    )
    parser.add_argument(
        "--eps-greedy",
        type=schedule,
        help="Chance of deterministic sampling when collecting rollouts",
        default=ConstantSchedule(0.0),
    )
    parser.add_argument(
        "--epochs",
        type=schedule,
        help="Number of times to train on sampled rollout data",
        default=ConstantSchedule(1),
    )
    parser.add_argument(
        "--learning-rate",
        type=schedule,
        help="Optimizer learning rate",
        default=ConstantSchedule(0.0003),
    )
    parser.add_argument(
        "--gae-lambda",
        type=schedule,
        help="GAE lambda paramater",
        default=ConstantSchedule(0.95),
    )
    parser.add_argument(
        "--gamma", type=schedule, help="Gamma parameter", default=ConstantSchedule(0.99)
    )
    parser.add_argument(
        "--clip-coef",
        type=schedule,
        help="Policy clip coefficient",
        default=ConstantSchedule(0.2),
    )
    parser.add_argument(
        "--value-coef",
        type=schedule,
        help="Value loss coefficient",
        default=ConstantSchedule(0.5),
    )
    parser.add_argument(
        "--entropy-coef",
        type=schedule,
        help="Entropy loss coefficient",
        default=ConstantSchedule(0.0),
    )
    parser.add_argument(
        "--max-grad-norm",
        type=schedule,
        help="Max gradient norm per update",
        default=ConstantSchedule(0.5),
    )
    parser.add_argument(
        "--train-rollouts",
        type=int,
        help="Number of rollouts to train for, None=forever",
        default=None,
    )
    parser.add_argument(
        "--train-main-exploiter",
        type=lambda x: bool(strtobool(x)),
        nargs="?",
        const=True,
        help="Train exploiter model against the latest main model",
        default=False,
    )
    parser.add_argument(
        "--num-main-exploiters",
        type=int,
        help="Number of main exploiters to train, if enabled",
        default=1,
    )
    parser.add_argument(
        "--main-exploiter-preset",
        type=str,
        help="Config preset name for training main exploiter",
        default=None,
    )
    parser.add_argument(
        "--main-exploiter-delay",
        type=int,
        help="Number of rollouts to wait before training main exploiter",
        default=0,
    )
    parser.add_argument(
        "--train-league-exploiter",
        type=lambda x: bool(strtobool(x)),
        nargs="?",
        const=True,
        help="Train exploiter model against all models in this experiment",
        default=False,
    )
    parser.add_argument(
        "--num-league-exploiters",
        type=int,
        help="Number of league exploiters to train, if enabled",
        default=1,
    )
    parser.add_argument(
        "--league-exploiter-preset",
        type=str,
        help="Config preset name for training league exploiter",
        default=None,
    )
    parser.add_argument(
        "--league-exploiter-delay",
        type=int,
        help="Number of rollouts to wait before training league exploiter",
        default=0,
    )
    parser.add_argument(
        "--forward-distribution-to-exploiters",
        type=lambda x: bool(strtobool(x)),
        nargs="?",
        const=True,
        help="Forward distributed rollouts to exploiters",
        default=False,
    )
    parser.add_argument(
        "--allow-cleanup",
        type=lambda x: bool(strtobool(x)),
        nargs="?",
        const=True,
        help="Allow cleaning up previous experiment. Failsafe to not delete experiments. "
        "Skipped if the experiment name is the default (test experiment).",
        default=False,
    )
    parser.add_argument(
        "--death-match",
        type=lambda x: bool(strtobool(x)),
        nargs="?",
        help="Death match flag",
        default=True,
    )
    parser.add_argument(
        "--penalize-food-on-death",
        type=lambda x: bool(strtobool(x)),
        nargs="?",
        const=True,
        help="Penalize food on death",
        default=True,
    )
    parser.add_argument(
        "--reward-target-food-on-death",
        type=lambda x: bool(strtobool(x)),
        nargs="?",
        const=True,
        help="Reward target food on death",
        default=True,
    )
    parser.add_argument(
        "--default-reward",
        type=schedule,
        help="Default reward",
        default=ConstantSchedule(0.0),
    )
    parser.add_argument(
        "--win-reward", type=schedule, help="Win reward", default=ConstantSchedule(1.0)
    )
    parser.add_argument(
        "--lose-reward",
        type=schedule,
        help="Lose reward",
        default=ConstantSchedule(-1.0),
    )
    parser.add_argument(
        "--tie-reward", type=schedule, help="Tie reward", default=ConstantSchedule(-0.2)
    )
    parser.add_argument(
        "--safe-penalty",
        type=schedule,
        help="Safe penalty",
        default=ConstantSchedule(0.0),
    )
    parser.add_argument(
        "--damage-received-reward-scale",
        type=schedule,
        help="Damage received reward scale",
        default=ConstantSchedule(0.0),
    )
    parser.add_argument(
        "--damage-dealt-reward-scale",
        type=schedule,
        help="Damage dealt reward scale",
        default=ConstantSchedule(0.0),
    )
    parser.add_argument(
        "--target-frozen-tick-reward",
        type=schedule,
        help="Target frozen tick reward",
        default=ConstantSchedule(0.0),
    )
    parser.add_argument(
        "--player-frozen-tick-reward",
        type=schedule,
        help="Player frozen tick reward",
        default=ConstantSchedule(0.0),
    )
    parser.add_argument(
        "--protected-correct-prayer-reward",
        type=schedule,
        help="Protected correct prayer reward",
        default=ConstantSchedule(0.0),
    )
    parser.add_argument(
        "--protected-wrong-prayer-reward",
        type=schedule,
        help="Protected wrong prayer reward",
        default=ConstantSchedule(0.0),
    )
    parser.add_argument(
        "--attacked-correct-prayer-reward",
        type=schedule,
        help="Attacked correct prayer reward",
        default=ConstantSchedule(0.0),
    )
    parser.add_argument(
        "--attacked-wrong-prayer-reward",
        type=schedule,
        help="Attacked wrong prayer reward",
        default=ConstantSchedule(0.0),
    )
    parser.add_argument(
        "--protected-previous-correct-prayer-reward",
        type=schedule,
        help="Protected correct prior prayer reward",
        default=ConstantSchedule(0.0),
    )
    parser.add_argument(
        "--protected-previous-wrong-prayer-reward",
        type=schedule,
        help="Protected wrong prior prayer reward",
        default=ConstantSchedule(0.0),
    )
    parser.add_argument(
        "--attacked-previous-correct-prayer-reward",
        type=schedule,
        help="Attacked correct prior prayer reward",
        default=ConstantSchedule(0.0),
    )
    parser.add_argument(
        "--attacked-previous-wrong-prayer-reward",
        type=schedule,
        help="Attacked wrong prior prayer reward",
        default=ConstantSchedule(0.0),
    )
    parser.add_argument(
        "--attack-level-scale-reward",
        type=schedule,
        help="Attack level scale reward",
        default=ConstantSchedule(0.0),
    )
    parser.add_argument(
        "--strength-level-scale-reward",
        type=schedule,
        help="Strength level scale reward",
        default=ConstantSchedule(0.0),
    )
    parser.add_argument(
        "--defense-level-scale-reward",
        type=schedule,
        help="Defense level scale reward",
        default=ConstantSchedule(0.0),
    )
    parser.add_argument(
        "--ranged-level-scale-reward",
        type=schedule,
        help="Ranged level scale reward",
        default=ConstantSchedule(0.0),
    )
    parser.add_argument(
        "--magic-level-scale-reward",
        type=schedule,
        help="Magic level scale reward",
        default=ConstantSchedule(0.0),
    )
    parser.add_argument(
        "--no-prayer-tick-reward",
        type=schedule,
        help="Reward (or penalty) for having no prayer for a tick",
        default=ConstantSchedule(0.0),
    )
    parser.add_argument(
        "--reward-on-hit-with-boost-scale",
        type=schedule,
        help="Reward on hit with boost scale",
        default=ConstantSchedule(0.0),
    )
    parser.add_argument(
        "--action-mask-override",
        type=schedule,
        help="Action mask override",
        default=None,
    )
    parser.add_argument(
        "--share-feature-extractor",
        type=lambda x: bool(strtobool(x)),
        nargs="?",
        const=True,
        help="Share feature extractor between actor and critic networks",
        default=False,
    )
    parser.add_argument(
        "--penalize-wasted-food",
        type=lambda x: bool(strtobool(x)),
        nargs="?",
        const=True,
        help="Penalize eating food over max hp",
        default=True,
    )
    parser.add_argument(
        "--reward-on-damage-generated",
        type=lambda x: bool(strtobool(x)),
        nargs="?",
        const=True,
        help="Reward/penalize damage when it is generated instead of when it appears",
        default=True,
    )
    parser.add_argument(
        "--reward-heals",
        type=lambda x: bool(strtobool(x)),
        nargs="?",
        const=True,
        help="Reward heals from damage (ex. Blood Barrage)",
        default=True,
    )
    parser.add_argument(
        "--penalize-target-heals",
        type=lambda x: bool(strtobool(x)),
        nargs="?",
        const=True,
        help="Penalize target heals from damage (ex. Blood Barrage)",
        default=True,
    )
    parser.add_argument(
        "--noise-generator", type=noise_generator, help="Noise generator", default=None
    )
    parser.add_argument(
        "--feature-extractor",
        type=json.loads,
        help="Feature extractor configuration",
        default={},
    )
    parser.add_argument(
        "--actor",
        type=json.loads,
        help="Actor hidden configuration",
        default=default_mlp_config([64, 64]),
    )
    parser.add_argument(
        "--action-heads",
        type=json.loads,
        help="Action head hidden configuration",
        default={},
    )
    parser.add_argument(
        "--critic",
        type=json.loads,
        help="Critic hidden configuration",
        default=default_mlp_config([64, 64]),
    )
    parser.add_argument(
        "--early-stopping",
        type=schedule,
        help="Early stopping configuration",
        default=None,
    )
    parser.add_argument(
        "--normalize-advantages",
        type=lambda x: bool(strtobool(x)),
        nargs="?",
        const=True,
        help="Normalize advantages",
        default=True,
    )
    parser.add_argument(
        "--normalize-rewards",
        type=lambda x: bool(strtobool(x)),
        nargs="?",
        const=True,
        help="Normalize rewards using a running std of cumulative rewards",
        default=True,
    )
    parser.add_argument(
        "--normalize-observations",
        type=lambda x: bool(strtobool(x)),
        nargs="?",
        const=True,
        help="Normalize observations using a running mean/std. Only can be set on experiment start.",
        default=True,
    )
    parser.add_argument(
        "--smite-damage-dealt-reward-multiplier",
        type=schedule,
        help="Reward multiplier for dealing damage with smite up, multiplies the damage dealt reward",
        default=ConstantSchedule(0.0),
    )
    parser.add_argument(
        "--smite-damage-received-reward-multiplier",
        type=schedule,
        help="Reward multiplier for taking damage with target's smite up "
        "multiplies the damage received reward",
        default=ConstantSchedule(0.0),
    )
    parser.add_argument(
        "--include-target-obs-in-critic",
        type=lambda x: bool(strtobool(x)),
        nargs="?",
        const=True,
        help="Include observations of the opponent in critic network",
        default=False,
    )
    parser.add_argument(
        "--save-latest-buffer",
        type=lambda x: bool(strtobool(x)),
        nargs="?",
        const=True,
        help="Save latest buffer to a file",
        default=False,
    )
    parser.add_argument(
        "--save-latest-meta",
        type=lambda x: bool(strtobool(x)),
        nargs="?",
        const=True,
        help="Save latest meta to a file",
        default=False,
    )
    parser.add_argument(
        "--distributed-rollouts",
        type=lambda x: bool(strtobool(x)),
        nargs="?",
        const=True,
        help="Use a distributed rollout sampling system across a fleet of CPUs",
        default=False,
    )
    parser.add_argument(
        "--distributed-rollout-preset",
        type=str,
        help="Preset to use for rollout sampling (this overrides many settings here),"
        "required if using distributed rollouts",
        default="",
    )
    parser.add_argument(
        "--num-distributed-rollouts",
        type=int,
        help="Number of parallel distributed rollouts to run, "
        "defaults to filling available resources (which won't autoscale)",
        default=0,
    )
    parser.add_argument(
        "--num-cpus-per-rollout",
        type=int,
        help="Number of CPUs to allocate to each rollout (if distributing rollouts)",
        default=2 if th.cuda.is_available() else 4,
    )
    parser.add_argument(
        "--custom-reward-function",
        type=schedule,
        help="Custom reward function, various environment information is passed as input parameters",
        default=ConstantSchedule(0.0),
    )
    parser.add_argument(
        "--player-died-with-food-multiplier",
        type=schedule,
        help="Reward multiplier for player/target dying with food/brews remaining "
        "multiplies the damage received reward",
        default=ConstantSchedule(1.0),
    )
    parser.add_argument(
        "--player-wasted-food-multiplier",
        type=schedule,
        help="Reward multiplier for player wasting food/brews "
        "multiplies the damage received reward",
        default=ConstantSchedule(1.0),
    )
    parser.add_argument(
        "--novelty-reward-scale",
        type=schedule,
        help="Novelty reward scale",
        default=ConstantSchedule(0.0),
    )
    parser.add_argument(
        "--bootstrap-from-experiment",
        type=str,
        help="Experiment name to start training from, using latest model",
        default=None,
    )
    parser.add_argument(
        "--num-reference-rating-envs",
        type=schedule,
        help="Number of environments to generate ratings on reference agents",
        default=ConstantSchedule(0),
    )
    parser.add_argument(
        "--enable-tracking-histograms",
        type=lambda x: bool(strtobool(x)),
        nargs="?",
        const=True,
        help="Enable tracking histograms in PyTorch's SummaryWriter. Enabling gives a performance hit when viewing in TensorBoard.",
        default=False,
    )
    parser.add_argument(
        "--add-win-rate-extension",
        type=lambda x: bool(strtobool(x)),
        nargs="?",
        const=True,
        help="Adds a model extension to predict win probability",
        default=False,
    )

    args = parser.parse_args(argv)

    env_kwargs = args.env_kwargs
    env_kwargs = replace_dash_with_underscore(env_kwargs)

    env_kwargs[PvpEnv.ENV_NAME_KEY] = args.env_name
    env_kwargs["stack_frames"] = args.stack_frames
    env_kwargs["default_reward"] = args.default_reward
    env_kwargs["win_reward"] = args.win_reward
    env_kwargs["lose_reward"] = args.lose_reward
    env_kwargs["tie_reward"] = args.tie_reward
    env_kwargs["safe_penalty"] = args.safe_penalty
    env_kwargs["damage_received_reward_scale"] = args.damage_received_reward_scale
    env_kwargs["damage_dealt_reward_scale"] = args.damage_dealt_reward_scale
    env_kwargs["target_frozen_tick_reward"] = args.target_frozen_tick_reward
    env_kwargs["player_frozen_tick_reward"] = args.player_frozen_tick_reward
    env_kwargs["protected_correct_prayer_reward"] = args.protected_correct_prayer_reward
    env_kwargs["protected_wrong_prayer_reward"] = args.protected_wrong_prayer_reward
    env_kwargs["attacked_correct_prayer_reward"] = args.attacked_correct_prayer_reward
    env_kwargs["attacked_wrong_prayer_reward"] = args.attacked_wrong_prayer_reward
    env_kwargs[
        "protected_previous_correct_prayer_reward"
    ] = args.protected_previous_correct_prayer_reward
    env_kwargs[
        "protected_previous_wrong_prayer_reward"
    ] = args.protected_previous_wrong_prayer_reward
    env_kwargs[
        "attacked_previous_correct_prayer_reward"
    ] = args.attacked_previous_correct_prayer_reward
    env_kwargs[
        "attacked_previous_wrong_prayer_reward"
    ] = args.attacked_previous_wrong_prayer_reward
    env_kwargs["attack_level_scale_reward"] = args.attack_level_scale_reward
    env_kwargs["strength_level_scale_reward"] = args.strength_level_scale_reward
    env_kwargs["defense_level_scale_reward"] = args.defense_level_scale_reward
    env_kwargs["ranged_level_scale_reward"] = args.ranged_level_scale_reward
    env_kwargs["magic_level_scale_reward"] = args.magic_level_scale_reward
    env_kwargs["death_match"] = args.death_match
    env_kwargs["penalize_food_on_death"] = args.penalize_food_on_death
    env_kwargs["reward_target_food_on_death"] = args.reward_target_food_on_death
    env_kwargs["action_mask_override"] = args.action_mask_override
    env_kwargs["noise_generator"] = args.noise_generator
    env_kwargs["reward_on_damage_generated"] = args.reward_on_damage_generated
    env_kwargs["penalize_wasted_food"] = args.penalize_wasted_food
    env_kwargs["reward_on_hit_with_boost_scale"] = args.reward_on_hit_with_boost_scale
    env_kwargs[PvpEnv.REMOTE_ENV_PORT_KEY] = args.remote_env_port
    env_kwargs[PvpEnv.REMOTE_ENV_HOST_KEY] = args.remote_env_host
    env_kwargs["reward_heals"] = args.reward_heals
    env_kwargs["penalize_target_heals"] = args.penalize_target_heals
    env_kwargs[
        "smite_damage_dealt_reward_multiplier"
    ] = args.smite_damage_dealt_reward_multiplier
    env_kwargs[
        "smite_damage_received_reward_multiplier"
    ] = args.smite_damage_received_reward_multiplier
    env_kwargs["include_target_obs_in_critic"] = args.include_target_obs_in_critic
    env_kwargs["custom_reward_fn"] = args.custom_reward_function
    env_kwargs[
        "player_died_with_food_multiplier"
    ] = args.player_died_with_food_multiplier
    env_kwargs["no_prayer_tick_reward"] = args.no_prayer_tick_reward
    env_kwargs["player_wasted_food_multiplier"] = args.player_wasted_food_multiplier

    policy_kwargs = args.policy_kwargs
    policy_kwargs = replace_dash_with_underscore(policy_kwargs)

    policy_kwargs["share_feature_extractor"] = args.share_feature_extractor
    policy_kwargs["feature_extractor_config"] = args.feature_extractor
    policy_kwargs["actor_config"] = args.actor
    policy_kwargs["action_head_configs"] = args.action_heads
    policy_kwargs["critic_config"] = args.critic

    meta = {**vars(args), "command": argv}

    eval_override_kwargs = args.eval_override_env_kwargs
    eval_override_kwargs = replace_dash_with_underscore(eval_override_kwargs)

    past_self_override_kwargs = args.past_self_override_env_kwargs
    past_self_override_kwargs = replace_dash_with_underscore(past_self_override_kwargs)

    latest_self_override_kwargs = args.latest_self_override_env_kwargs
    latest_self_override_kwargs = replace_dash_with_underscore(
        latest_self_override_kwargs
    )

    remote_processor_kwargs = args.remote_processor_kwargs
    remote_processor_kwargs = replace_dash_with_underscore(remote_processor_kwargs)

    allow_cleanup = (
        args.allow_cleanup or args.experiment_name == default_experiment_name
    )

    train(
        train_rollouts=args.train_rollouts,
        num_envs=args.num_envs,
        experiment_name=args.experiment_name,
        load_file=args.load_file,
        self_play_percent=args.self_play_percent,
        past_self_play_percent=args.past_self_play_percent,
        past_self_play_deterministic_percent=args.past_self_play_deterministic_percent,
        past_self_play_experiment=args.past_self_play_experiment,
        past_self_play_learning_rate=args.past_self_play_learning_rate,
        past_self_play_delay_chance=args.past_self_play_delay_chance,
        past_self_override_kwargs=past_self_override_kwargs,
        add_past_self_play_league_targets=args.add_past_self_play_league_targets,
        target_self_play_percent=args.target_self_play_percent,
        target_self_play_deterministic_percent=args.target_self_play_deterministic_percent,
        latest_self_play_percent=args.latest_self_play_percent,
        latest_self_play_experiment=args.latest_self_play_experiment,
        latest_self_play_deterministic_percent=args.latest_self_play_deterministic_percent,
        latest_self_play_delay_chance=args.latest_self_play_delay_chance,
        latest_self_override_kwargs=latest_self_override_kwargs,
        targets=args.target,
        num_eval_agent=args.num_eval_agent,
        eval_override_kwargs=eval_override_kwargs,
        eval_deterministic_percent=args.eval_deterministic_percent,
        remote_processor_pool_size=args.remote_processor_pool_size,
        remote_processor_device=args.remote_processor_device,
        remote_processor_type=args.remote_processor_type,
        remote_processor_kwargs=remote_processor_kwargs,
        num_rollout_steps=args.num_rollout_steps,
        env_kwargs=env_kwargs,
        device=args.device,
        continue_training=args.continue_training,
        policy_kwargs=policy_kwargs,
        batch_size=args.batch_size,
        grad_accum=args.grad_accum,
        checkpoint_frequency=args.checkpoint_frequency,
        optimize_old_models=args.optimize_old_models,
        eps_greedy=args.eps_greedy,
        epochs=args.epochs,
        learning_rate=args.learning_rate,
        gamma=args.gamma,
        gae_lambda=args.gae_lambda,
        clip_coef=args.clip_coef,
        value_coef=args.value_coef,
        entropy_coef=args.entropy_coef,
        max_grad_norm=args.max_grad_norm,
        train_main_exploiter=args.train_main_exploiter,
        num_main_exploiters=args.num_main_exploiters,
        main_exploiter_preset=args.main_exploiter_preset,
        main_exploiter_delay=args.main_exploiter_delay,
        train_league_exploiter=args.train_league_exploiter,
        num_league_exploiters=args.num_league_exploiters,
        league_exploiter_preset=args.league_exploiter_preset,
        league_exploiter_delay=args.league_exploiter_delay,
        forward_distribution_to_exploiters=args.forward_distribution_to_exploiters,
        allow_cleanup=allow_cleanup,
        experiment_meta=meta,
        remote_env_host=args.remote_env_host,
        remote_env_port=args.remote_env_port,
        early_stopping=args.early_stopping,
        normalize_advantages=args.normalize_advantages,
        normalize_rewards=args.normalize_rewards,
        normalize_observations=args.normalize_observations,
        save_buffer=args.save_latest_buffer,
        save_meta=args.save_latest_meta,
        distributed_rollouts=args.distributed_rollouts,
        distributed_rollout_preset=args.distributed_rollout_preset,
        num_distributed_rollouts=args.num_distributed_rollouts,
        num_cpus_per_rollout=args.num_cpus_per_rollout,
        novelty_reward_scale=args.novelty_reward_scale,
        bootstrap_from_experiment=args.bootstrap_from_experiment,
        num_reference_rating_envs=args.num_reference_rating_envs,
        enable_tracking_histograms=args.enable_tracking_histograms,
        add_win_rate_extension=args.add_win_rate_extension,
    )


if __name__ == "__main__":
    main(sys.argv[1:])
