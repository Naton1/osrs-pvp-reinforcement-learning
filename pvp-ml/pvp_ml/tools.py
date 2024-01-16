"""
This script contains various miscellaneous utility CLI commands, such as optimizing all models for deployment.
"""
import argparse
import logging
import os
import sys

logger = logging.getLogger(__name__)


def optimize_for_deployment(model_file_path: str = "") -> None:
    from pvp_ml.ppo.ppo import PPO
    from pvp_ml.util.files import models_dir, reference_dir

    if model_file_path:
        PPO.optimize_for_inference(model_file_path)
    else:
        for dir_name in [models_dir, reference_dir]:
            for file_name in os.listdir(dir_name):
                file_path = f"{dir_name}/{file_name}"
                logger.info(f"Optimizing model '{file_path}' for inference")
                PPO.optimize_for_inference(file_path)


def main(argv: list[str]) -> None:
    parser = argparse.ArgumentParser(description="Contains utility tools")
    subparsers = parser.add_subparsers(required=True)

    optimize_parser = subparsers.add_parser("optimize")
    optimize_parser.set_defaults(command_runner=optimize_for_deployment)
    optimize_parser.add_argument(
        "--model-file-path",
        type=str,
        default="",
        help="Model file to optimize. If none provided, will optimize all in /models and /references",
    )

    args = parser.parse_args(argv)
    parameters = vars(args)
    parameters.pop("command_runner")(**parameters)


if __name__ == "__main__":
    main(sys.argv[1:])
