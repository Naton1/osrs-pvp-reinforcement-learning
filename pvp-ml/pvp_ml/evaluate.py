"""
This script will evaluate a trained model or plugin on a remote environment.
"""
import argparse
import asyncio
import json
import logging
import os
import sys
from collections.abc import Callable, Iterator
from contextlib import contextmanager

import numpy as np
import torch as th

from pvp_ml.env.pvp_env import PvpEnv
from pvp_ml.env.simulation import Simulation
from pvp_ml.ppo.ppo import PPO
from pvp_ml.scripted.script_plugin_registry import get_scripted_plugin
from pvp_ml.util.args_helper import replace_dash_with_underscore, strtobool
from pvp_ml.util.files import models_dir

logger = logging.getLogger(__name__)


async def eval_model(
    env: PvpEnv,
    predictor: Callable[[th.Tensor, th.Tensor], th.Tensor],
    num_episodes: int | None = None,
) -> None:
    logger.info(
        f"Evaluating environment: {env}{f' for {num_episodes} episodes' if num_episodes is not None else ''}"
    )
    try:
        episodes_completed = 0
        while not env.is_closed():
            obs, reset_info = await env.reset_async()
            total_reward = 0.0
            num_steps = 0
            while not env.is_closed():
                action_masks = env.get_action_masks()
                action = predictor(
                    th.as_tensor(obs[np.newaxis, :], device="cpu"),
                    th.as_tensor(action_masks[np.newaxis, :], device="cpu"),
                )
                np_action = action.cpu().numpy()[0]
                logger.info(
                    f"Running action: {np_action} ({num_steps} steps) ({episodes_completed} episodes completed)"
                )
                obs, reward, done, truncated, info = await env.step_async(np_action)
                total_reward += reward
                num_steps += 1
                if done:
                    logger.info(
                        f"Evaluation episode complete: {info} - total reward {total_reward}"
                    )
                    episodes_completed += 1
                    break
            if num_episodes is not None and episodes_completed >= num_episodes:
                logger.info(f"Completed evaluating {num_episodes} episodes")
                break
        logger.info(f"Evaluation sequence completed {env}")
    except Exception:
        logger.exception(f"Evaluation session threw exception {env}")
        raise
    finally:
        await env.close_async()
        logger.info(f"Evaluation session cleaned up {env}")


def _create_ppo_predictor(
    ppo: PPO, deterministic: bool = True
) -> Callable[[th.Tensor, th.Tensor], th.Tensor]:
    def predict(obs: th.Tensor, action_masks: th.Tensor) -> th.Tensor:
        action, *_ = ppo.predict(
            obs=obs,
            deterministic=deterministic,
            action_masks=action_masks,
            return_actions=True,
            return_log_probs=False,
            return_entropy=False,
            return_values=False,
        )
        assert action is not None
        return action

    return predict


@contextmanager
def _maybe_run_simulation(
    run_simulation: bool,
    run_simulation_game_port: int,
    default_host: str,
    default_env_port: int,
) -> Iterator[tuple[str, int]]:
    # Start a new simulation if specified, otherwise use provided host/port to connect to existing simulation
    if run_simulation:
        logger.info(
            f"Running new simulation for evaluation session on {run_simulation_game_port}"
        )
        with Simulation(
            game_port=run_simulation_game_port,
            remote_env_port=default_env_port,
            sync_training=False,
        ) as simulation:
            simulation.wait_until_loaded()
            yield "localhost", simulation.remote_env_port
    else:
        yield default_host, default_env_port


def main(argv: list[str]) -> None:
    parser = argparse.ArgumentParser(description="Evaluate a model/script")
    # Model args
    parser.add_argument(
        "--model-path",
        type=str,
        help="Model path",
        default=f"models/{next(iter(os.listdir(models_dir)), None)}",
    )
    parser.add_argument(
        "--deterministic",
        type=lambda x: bool(strtobool(x)),
        nargs="?",
        const=True,
        help="Deterministic prediction",
        default=False,
    )
    parser.add_argument(
        "--device",
        type=str,
        help="Processor device",
        default="cpu",
    )

    # Script args
    parser.add_argument("--plugin", type=str, help="Plugin name")

    # Shared args
    parser.add_argument(
        "--target", type=str, help="Evaluation target", default="attacker"
    )
    parser.add_argument(
        "--num-episodes", type=int, help="Num episodes to evaluate", default=None
    )
    parser.add_argument(
        "--run-simulation",
        type=lambda x: bool(strtobool(x)),
        nargs="?",
        const=True,
        help="Start (and manage) a new simulation for the evaluation session",
        default=True,
    )
    parser.add_argument(
        "--simulation-port",
        type=int,
        help="Game port for the simulation to evaluate on (if running new simulation)",
        default=43595,
    )
    parser.add_argument(
        "--remote-env-port",
        type=int,
        help="Remote environment port for the simulation to evaluate on (if not running new simulation)",
        default=7070,
    )
    parser.add_argument(
        "--remote-env-host",
        type=str,
        help="Remote environment host for the simulation to evaluate on (if not running new simulation)",
        default="localhost",
    )
    parser.add_argument(
        "--env-kwargs",
        type=json.loads,
        help="Keyword arguments to pass when initializing the environments",
        default={},
    )

    args = parser.parse_args(argv)

    with _maybe_run_simulation(
        run_simulation=args.run_simulation,
        run_simulation_game_port=args.simulation_port,
        default_host=args.remote_env_host,
        default_env_port=args.remote_env_port,
    ) as (remote_env_host, remote_env_port):
        env_kwargs = args.env_kwargs
        env_kwargs = replace_dash_with_underscore(env_kwargs)
        env_kwargs[PvpEnv.REMOTE_ENV_PORT_KEY] = remote_env_port
        env_kwargs[PvpEnv.REMOTE_ENV_HOST_KEY] = remote_env_host

        predictor: Callable[[th.Tensor, th.Tensor], th.Tensor]
        if args.plugin:
            plugin = get_scripted_plugin(args.plugin)
            predictor = plugin.predict
            env_kwargs[PvpEnv.ENV_NAME_KEY] = plugin.get_env_name()
            env_kwargs[PvpEnv.ENV_ID_KEY] = f"eval-{args.plugin}"
            logger.info(f"Evaluating plugin: {args.plugin} on {plugin.get_env_name()}")
        else:
            ppo = PPO.load(args.model_path, trainable=False, device=args.device)
            predictor = _create_ppo_predictor(ppo, args.deterministic)
            model_name = os.path.splitext(os.path.basename(args.model_path))[0]
            env_kwargs = {
                **ppo.meta.custom_data["env_kwargs"],
                **env_kwargs,
                PvpEnv.ENV_ID_KEY: f"eval-{model_name}",
            }
            logger.info(f"Evaluating model: {args.model_path}")

        env = PvpEnv(
            **env_kwargs,
            target=args.target,
        )

        asyncio.run(
            eval_model(
                env=env,
                predictor=predictor,
                num_episodes=args.num_episodes,
            )
        )


def main_entry_point() -> None:
    main(sys.argv[1:])


if __name__ == "__main__":
    main_entry_point()
