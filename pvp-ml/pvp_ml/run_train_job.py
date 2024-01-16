"""
This script orchestrates the training process by wiring together and starting
the RSPS and underlying training script, as well as handling preset configurations and tracking job lifecycle.
It also starts TensorBoard, if not already running.
"""
import argparse
import atexit
import collections
import json
import logging
import os
import queue
import shutil
import subprocess
import sys
import threading
import time
from typing import Any, TypedDict, cast

import psutil
import yaml
from filelock import FileLock
from psutil import NoSuchProcess, TimeoutExpired

from pvp_ml import package_root
from pvp_ml.env.simulation import Simulation
from pvp_ml.util.socket_helper import is_port_taken

logger = logging.getLogger(__name__)

RSPS_ROOT_DIR = os.path.abspath(
    os.path.join(package_root, "../simulation-rsps/ElvargServer")
)
LOG_DIR = os.path.abspath(os.path.join(package_root, "./logs"))
PRESETS_DIR = os.path.abspath(os.path.join(package_root, "./config"))
EXPERIMENT_TRACKER = os.path.abspath(os.path.join(package_root, "./train_jobs.json"))

BASE_GAME_PORT = 43595
BASE_REMOTE_ENV_PORT = 7070

DEFAULT_EXPERIMENT_NAME = "Experiment"

FILE_LOCK = FileLock(os.path.join(package_root, ".train_init.lock"))

CLEANUP_ALL = "all"


class TrainProcess(TypedDict):
    pid: int
    create_time: float
    process_type: str


class TrainJob(TypedDict):
    name: str
    processes: list[TrainProcess]


Tracker = dict[str, TrainJob]


def is_tensorboard_running() -> bool:
    for process in psutil.process_iter(["pid", "name"]):
        assert hasattr(process, "info")  # Make MyPy happy, info is dynamically added
        if "tensorboard" in process.info["name"]:
            return True
    return False


def run_tensorboard(root_dir: str) -> None:
    if is_tensorboard_running():
        logger.info("TensorBoard is already running")
        return
    logger.info("Running TensorBoard")
    with open(os.devnull, "w") as devnull:
        tensorboard_path = shutil.which("tensorboard")
        assert tensorboard_path, "No tensorboard path found"
        command = [
            tensorboard_path,
            "--logdir",
            "./tensorboard",
            "--bind_all",
        ]
        tb_process = subprocess.Popen(
            command,
            stdout=devnull,
            stderr=devnull,
            stdin=subprocess.DEVNULL,
            cwd=root_dir,
        )
        logger.info(
            f"Started TensorBoard, PID: {tb_process.pid}, Command: {' '.join(command)} ({root_dir})"
        )


def register_task_cleanup() -> None:
    def cleanup() -> None:
        parent = psutil.Process()
        children = parent.children(recursive=True)
        for child in children:
            try:
                child.kill()
            except NoSuchProcess:
                pass

    atexit.register(cleanup)


def find_available_experiment_id() -> int:
    # Start searching at ID 1 - leave 0 open so manual testing on same machine works by default
    possible_id = 1
    while True:
        remote_env_port = BASE_GAME_PORT + possible_id
        if is_id_taken(possible_id) or is_port_taken(remote_env_port):
            possible_id += 1
            continue  # Taken
        break
    logger.info(f"Found available experiment ID: {possible_id}")
    return possible_id


def load_experiment_preset(args: argparse.Namespace) -> tuple[str, list[str]]:
    experiment_name = args.name
    training_params = args.override if args.override else []
    if args.preset is not None:
        presets = load_presets()
        if args.preset not in presets:
            raise ValueError(
                f"Error: Unknown preset {args.preset} not found in {presets.keys()}"
            )
        selected_preset = merge_presets(presets, args.preset)
        training_params = apply_preset_values(selected_preset, training_params)
        if experiment_name == DEFAULT_EXPERIMENT_NAME:
            experiment_name = args.preset
    if args.distribute != -1:
        assert args.preset is not None, "Preset must be provided to distribute"
        training_params.append("--distributed-rollouts")
        training_params.append("true")
        training_params.append("--distributed-rollout-preset")
        training_params.append(args.preset)
        training_params.append("--num-distributed-rollouts")
        training_params.append(str(args.distribute))
    return experiment_name, training_params


def merge_presets(presets: dict[str, dict[str, Any]], preset: str) -> dict[str, Any]:
    selected_preset = presets[preset]
    import_values = selected_preset.pop("import", [])
    for import_preset in import_values:
        # Allow overriding imports. Precedence goes: preset, 1st import, 2nd import, ...
        selected_preset = {**merge_presets(presets, import_preset), **selected_preset}
    return selected_preset


def apply_preset_values(
    selected_preset: dict[str, Any], training_params: list[str]
) -> list[str]:
    for key, value in selected_preset.items():
        key_arg = f"--{key}"
        if key_arg in training_params:
            # Don't change the value of anything already set as an override
            continue
        training_params.append(key_arg)
        if value is not None:
            training_params.append(
                json.dumps(value) if not isinstance(value, str) else value
            )
    return training_params


def start_rsps(
    rsps_output: str, rsps_root_dir: str, game_port: int, remote_env_port: int
) -> Simulation:
    logger.info("Starting RSPS Server")
    simulation = Simulation(
        game_port=game_port,
        remote_env_port=remote_env_port,
        server_path=rsps_root_dir,
        log_file_path=rsps_output,
    )
    simulation.start()
    return simulation


def start_training(
    train_output: str,
    pvp_root_dir: str,
    experiment_name: str,
    remote_env_port: int,
    train_params: list[str],
) -> subprocess.Popen[Any]:
    logger.info("Starting PvP Training Script")
    with open(train_output, "w") as output_file:
        output_file.truncate(0)
        command = [
            sys.executable,
            f"{pvp_root_dir}/pvp_ml/train.py",
            "--remote-env-port",
            str(remote_env_port),
            "--experiment-name",
            experiment_name,
            *train_params,
        ]
        train_process = subprocess.Popen(
            command,
            stdout=output_file,
            stderr=output_file,
            stdin=subprocess.DEVNULL,
            cwd=pvp_root_dir,
        )
        logger.info(
            f"Started PvP Training Script, PID: {train_process.pid}, Command: {' '.join(command)} ({pvp_root_dir})"
        )
    return train_process


def tail_logs(
    file_name: str,
    log_queue: queue.Queue[str],
    log_name: str,
    process: subprocess.Popen[Any],
) -> None:
    with open(file_name, "r") as f:
        while process.poll() is None:
            line = f.readline()
            if line:
                log_queue.put(f"{log_name} - {line}")
            else:
                time.sleep(0.1)  # sleep if file hasn't been updated
        logger.info(
            f"Process {log_name} terminated ({process.pid}): {process.returncode}"
        )


def propagate_logs(
    rsps_output: str,
    rsps_process: subprocess.Popen[Any] | None,
    train_output: str,
    train_process: subprocess.Popen[Any],
) -> None:
    log_queue: queue.Queue[str] = queue.Queue()
    files = {
        **({"rsps": (rsps_output, rsps_process)} if rsps_process is not None else {}),
        "train": (train_output, train_process),
    }
    threads = []
    for name, (output_file, process) in files.items():
        thread = threading.Thread(
            target=tail_logs,
            args=(output_file, log_queue, name, process),
            name=f"tail-thread-{name}",
        )
        thread.start()
        threads.append(thread)
    logger.info(f"Propagating logs for {files.keys()}")
    while any(t.is_alive() for t in threads):
        log_message = log_queue.get(timeout=1)
        if log_message:
            logger.info(log_message)


def wait_for_training_to_end(
    train_process: subprocess.Popen[Any],
    rsps_process: subprocess.Popen[Any] | None,
    train_log_file: str,
    experiment_id: int,
) -> None:
    logger.info(f"Waiting for training to complete {train_process.pid}")
    return_code = train_process.wait()
    if return_code != 0:
        logger.info(
            f"Train process ({experiment_id}) failed: {return_code}. printing last 50 lines."
        )
        print_last_n_lines(train_log_file, 50)
        raise ValueError(f"Return code for {experiment_id}: {return_code}")
    if rsps_process is not None:
        for child in psutil.Process(rsps_process.pid).children(recursive=True):
            child.terminate()
        rsps_process.terminate()


def load_presets() -> dict[str, dict[str, Any]]:
    presets = {}
    for root, dirs, files in os.walk(PRESETS_DIR):
        for filename in files:
            if filename.endswith(".yml"):
                filepath = os.path.join(root, filename)
                with open(filepath, "r") as f:
                    data = yaml.safe_load(f)
                    presets.update(data)
    return presets


def try_create_experiment_tracker() -> None:
    if not os.path.exists(EXPERIMENT_TRACKER):
        save_tracker({})


def load_tracker() -> Tracker:
    with open(EXPERIMENT_TRACKER, "r") as f:
        return cast(Tracker, json.load(f))


def save_tracker(tracker: Tracker) -> None:
    with open(EXPERIMENT_TRACKER, "w") as f:
        json.dump(tracker, f, indent=2)


def get_running_process(pid: int, create_time: float) -> psutil.Process | None:
    try:
        process = psutil.Process(pid=pid)
        if process.is_running() and process.create_time() == create_time:
            return process
        return None
    except NoSuchProcess:
        return None


def cleanup_process(process_meta: TrainProcess, wait: bool = False) -> None:
    process = get_running_process(process_meta["pid"], process_meta["create_time"])
    if process is not None:
        logger.info(
            f"Terminating running process: {process.pid} - {process_meta['process_type']}"
        )
        try:
            for child in process.children(recursive=True):
                try:
                    child.terminate()
                except NoSuchProcess:
                    pass
            process.terminate()
            if wait:
                try:
                    process.wait(timeout=5)
                except TimeoutExpired:
                    process.kill()
                    process.wait(timeout=5)
        except NoSuchProcess:
            # Sometimes killing a child ends up killing the parent
            # psutil throws an exception if you try to terminate it then
            pass


def cleanup_previous_experiments() -> None:
    tracker = load_tracker()

    cleaned_tracker = tracker.copy()
    for key, value in cleaned_tracker.copy().items():
        process_metas = value["processes"]
        # If there is no processes, or any process is dead, clean up the experiment
        process_is_dead = not process_metas or any(
            get_running_process(meta["pid"], meta["create_time"]) is None
            for meta in process_metas
        )

        if process_is_dead:
            for process_meta in process_metas:
                cleanup_process(process_meta)
            logger.info(f"Deleting experiment: {key}")
            del cleaned_tracker[key]

    save_tracker(cleaned_tracker)


def register_new_experiment(experiment_id: int, name: str) -> None:
    tracker = load_tracker()
    assert (
        f"{experiment_id}" not in tracker
    ), f"Experiment already exists: {experiment_id}"
    tracker[f"{experiment_id}"] = TrainJob(name=name, processes=[])
    save_tracker(tracker)


def track_process_launch(experiment_id: int, pid: int, process_type: str) -> None:
    p = psutil.Process(pid)
    create_time = p.create_time()

    tracker = load_tracker()

    tracker[f"{experiment_id}"]["processes"].append(
        TrainProcess(pid=pid, create_time=create_time, process_type=process_type)
    )

    save_tracker(tracker)


def print_experiments() -> None:
    tracker = load_tracker()
    logger.info(
        f"\n ---- Running Experiments ----\n{yaml.dump(tracker, default_flow_style=False)}"
    )


def is_id_taken(experiment_id: int) -> bool:
    return f"{experiment_id}" in load_tracker()


def clean_up(experiment_name: str) -> None:
    with open(EXPERIMENT_TRACKER, "r") as f:
        tracker = json.load(f)
        for key, experiment in tracker.items():
            if experiment["name"] == experiment_name or experiment_name == CLEANUP_ALL:
                for process_meta in experiment["processes"]:
                    cleanup_process(process_meta, wait=True)
    cleanup_previous_experiments()


def print_last_n_lines(filename: str, n: int) -> None:
    with open(filename) as f:
        for line in collections.deque(f, n):
            print(line, end="")


def validate_processes(
    rsps_output: str,
    rsps_process: subprocess.Popen[Any] | None,
    train_output: str,
    train_process: subprocess.Popen[Any],
) -> bool:
    logger.info("Validating process health...")
    for i in range(0, 15):
        if train_process.poll() is not None:
            logger.info(f"Train process terminated: {train_process.returncode}")
            print_last_n_lines(train_output, 50)
            return False
        if rsps_process is not None and rsps_process.poll() is not None:
            logger.info(f"RSPS process terminated: {rsps_process.returncode}")
            print_last_n_lines(rsps_output, 50)
            return False
        logger.info(f"Processes are alive (attempt {i})...")
        time.sleep(1)
    logger.info("Process health validated")
    return True


def parse_args(argv: list[str]) -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description="Start RSPS server and PvP Training script"
    )
    parser.add_argument(
        "action",
        type=str,
        nargs="?",
        default="train",
        help="Action to run",
        choices=["train", "cleanup", "tensorboard", "show"],
    )
    parser.add_argument("--id", type=int, help="Experiment ID")
    parser.add_argument(
        "--name", type=str, default=DEFAULT_EXPERIMENT_NAME, help="Experiment name"
    )
    parser.add_argument("--preset", type=str, help="Preset configuration name")
    parser.add_argument(
        "--wait", action="store_true", help="Wait for the processes to end"
    )
    parser.add_argument(
        "--log", action="store_true", help="Log the output of the processes"
    )
    parser.add_argument(
        "--validate",
        action="store_true",
        help="Validate that the processes run for some time without failure",
    )
    parser.add_argument(
        "--tensorboard",
        action=argparse.BooleanOptionalAction,
        default=True,
        help="Run tensorboard in addition to training",
    )
    parser.add_argument("--skip-rsps", action="store_true", help="Skip launching RSPS")
    parser.add_argument(
        "--distribute",
        type=int,
        nargs="?",
        const=0,
        default=-1,
        help="Distribute rollout collection to a cluster (and the number of actors). "
        "Note this is a shortcut for a few params, and those could be defined inside a preset.",
    )

    parser.add_argument(
        "--override",
        nargs=argparse.REMAINDER,
        help="Extra training parameters for train script",
    )

    return parser.parse_args(argv)


def main(argv: list[str]) -> None:
    os.makedirs(LOG_DIR, exist_ok=True)

    rsps_root_dir = RSPS_ROOT_DIR
    pvp_root_dir = package_root

    args = parse_args(argv)

    logger.info(f"Running action: {args.action}")

    if args.action == "tensorboard":
        run_tensorboard(pvp_root_dir)
        return

    if args.action == "cleanup":
        with FILE_LOCK:
            clean_up(args.name)
        return

    if args.action == "show":
        with FILE_LOCK:
            cleanup_previous_experiments()
            print_experiments()
        return

    if args.wait:
        register_task_cleanup()

    experiment_name, train_params = load_experiment_preset(args)

    with FILE_LOCK:
        try_create_experiment_tracker()
        cleanup_previous_experiments()

        experiment_id = args.id
        if experiment_id is None:
            experiment_id = find_available_experiment_id()

        logger.info(f"Starting experiment: {experiment_name} ({experiment_id})")

        register_new_experiment(experiment_id, experiment_name)

        game_port = BASE_GAME_PORT + experiment_id
        remote_env_port = BASE_REMOTE_ENV_PORT + experiment_id

        rsps_output = f"{LOG_DIR}/{experiment_id}-rsps.log"
        train_output = f"{LOG_DIR}/{experiment_id}-train.log"

        if is_port_taken(game_port):
            raise ValueError(
                f"Error: game port {game_port} is already taken (likely already an experiment with id {experiment_id})"
            )
        if is_port_taken(remote_env_port):
            raise ValueError(
                f"Error: remove env port {remote_env_port} is already taken (likely already an experiment with id {experiment_id})"
            )

        if "--distributed-rollouts" in train_params:
            logger.info("Skipping RSPS launch because distributing training")
            rsps_process = None
        elif args.skip_rsps:
            logger.info("Skipping RSPS launch because given --skip-rsps arg")
            rsps_process = None
        else:
            simulation = start_rsps(
                rsps_output, rsps_root_dir, game_port, remote_env_port
            )
            rsps_process = simulation.process
            assert rsps_process is not None
            track_process_launch(experiment_id, rsps_process.pid, "rsps")
            simulation.wait_until_loaded()

        train_process = start_training(
            train_output, pvp_root_dir, experiment_name, remote_env_port, train_params
        )
        track_process_launch(experiment_id, train_process.pid, "train")

        if args.tensorboard:
            run_tensorboard(pvp_root_dir)

    if args.validate:
        validate_processes(rsps_output, rsps_process, train_output, train_process)

    if args.log:
        propagate_logs(rsps_output, rsps_process, train_output, train_process)

    if args.wait:
        wait_for_training_to_end(
            train_process, rsps_process, train_output, experiment_id
        )

    with FILE_LOCK:
        # Cleanup anything that's finished
        cleanup_previous_experiments()

    logger.info(f"Launch script complete: {args.action}")


def main_entry_point() -> None:
    main(sys.argv[1:])


if __name__ == "__main__":
    main_entry_point()
