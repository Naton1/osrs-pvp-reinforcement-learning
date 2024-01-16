import logging
import os
import platform
import subprocess
import time
from typing import Any

import psutil
from psutil import NoSuchProcess

from pvp_ml import package_root
from pvp_ml.util.socket_helper import is_port_taken

_default_path = os.path.abspath(
    os.path.join(package_root, "../simulation-rsps/ElvargServer")
)
logger = logging.getLogger(__name__)


class Simulation:
    def __init__(
        self,
        game_port: int = 43595,
        remote_env_port: int = 7070,
        server_path: str = _default_path,
        sync_training: bool = True,
        log_file_path: str | None = None,
    ):
        self.game_port = game_port
        self.remote_env_port = remote_env_port
        self.server_path = server_path
        self.log_file_path = log_file_path
        self.sync_training = sync_training
        self.process: subprocess.Popen[Any] | None = None
        self._psutil_process: psutil.Process | None = None

    def start(self) -> None:
        assert self.process is None, "Process already launched"
        assert not is_port_taken(self.game_port), f"Port {self.game_port} already taken"
        assert not is_port_taken(
            self.remote_env_port
        ), f"Port {self.remote_env_port} already taken"

        server_config = {
            "GAME_PORT": str(self.game_port),
            "REMOTE_ENV_PORT": str(self.remote_env_port),
            "SYNC_TRAINING": str(self.sync_training),
            "RUN_EVAL_BOTS": "false",
            "TRAIN": "true",
            "SHOW_ENV_DEBUGGER": "false",
        }
        if self.sync_training:
            server_config["TICK_RATE"] = "1"

        ext = ".bat" if platform.system() == "Windows" else ""
        process_args = [f"{self.server_path}/gradlew{ext}", "run", "--no-daemon"]

        env = {
            **server_config,
            # This is a trick to disable the gradle daemon, which saves some resources.
            # https://github.com/gradle/gradle/issues/11517. Unfortunately just '--no-daemon' doesn't work.
            "GRADLE_OPTS": "-XX:MaxMetaspaceSize=256m -XX:+HeapDumpOnOutOfMemoryError -Xmx512m",
            **os.environ,
        }

        if self.log_file_path:
            with open(self.log_file_path, "w") as output_file:
                output_file.truncate(0)
                self.process = subprocess.Popen(
                    process_args,
                    stdout=output_file,
                    stderr=output_file,
                    stdin=subprocess.DEVNULL,
                    cwd=self.server_path,
                    env=env,
                )
        else:
            self.process = subprocess.Popen(
                process_args,
                stdin=subprocess.DEVNULL,
                cwd=self.server_path,
                env=env,
            )

        self._psutil_process = psutil.Process(self.process.pid)

    def is_running(self) -> bool:
        return self.process is not None and self.process.poll() is None

    def is_loaded(self) -> bool:
        return self.is_running() and is_port_taken(self.remote_env_port)

    def wait_until_loaded(self, max_attempts: int = 300) -> None:
        assert self.process is not None, "Server not started"
        attempts = 0
        start_time = time.time()
        while not self.is_loaded():
            logger.info(
                f"Waiting for server to start on port {self.remote_env_port}..."
            )
            time.sleep(1)
            attempts += 1
            if attempts > max_attempts:
                raise ValueError(f"Server didn't start after {attempts} attempts")
            if not self.is_running():
                raise ValueError(
                    f"Simulation process terminated: {self.process.returncode}"
                )
        logger.info(
            f"Server successfully started up on port {self.remote_env_port} after waiting {time.time() - start_time} seconds"
        )

    def close(self) -> None:
        if self.process is None:
            return
        assert self._psutil_process is not None
        try:
            for child in self._psutil_process.children(recursive=True):
                try:
                    child.terminate()
                except NoSuchProcess:
                    pass
            self._psutil_process.terminate()
        except NoSuchProcess:
            # Sometimes killing a child ends up killing the parent
            # psutil then throws an exception when terminating
            pass
        self.process = None
        self._psutil_process = None

    def __enter__(self) -> "Simulation":
        self.start()
        return self

    def __exit__(self, *args: Any) -> None:
        self.close()
