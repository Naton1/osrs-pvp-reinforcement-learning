import pytest

from pvp_ml.env.simulation import Simulation
from pvp_ml.evaluate import main
from pvp_ml.util.files import models_dir


def test_eval_model_baseline(caplog: pytest.LogCaptureFixture) -> None:
    with Simulation(game_port=10000, remote_env_port=11000) as simulation:
        simulation.wait_until_loaded()
        main(
            [
                "--model-path",
                f"{models_dir}/GeneralizedNh.zip",
                "--target",
                "baseline",
                "--run-simulation",
                "false",
                "--num-episodes",
                "3",
                "--remote-env-port",
                str(simulation.remote_env_port),
            ]
        )
    assert sum([1 for m in caplog.messages if "episode complete" in m]) == 3


def test_eval_plugin_baseline(caplog: pytest.LogCaptureFixture) -> None:
    with Simulation(game_port=10001, remote_env_port=11001) as simulation:
        simulation.wait_until_loaded()
        main(
            [
                "--plugin",
                "stayalive",
                "--target",
                "baseline",
                "--run-simulation",
                "false",
                "--num-episodes",
                "3",
                "--remote-env-port",
                str(simulation.remote_env_port),
            ]
        )
    assert sum([1 for m in caplog.messages if "episode complete" in m]) == 3


def test_eval_model_baseline_new_simulation(caplog: pytest.LogCaptureFixture) -> None:
    main(
        [
            "--model-path",
            f"{models_dir}/GeneralizedNh.zip",
            "--target",
            "baseline",
            "--num-episodes",
            "3",
            "--simulation-port",
            str(10002),
            "--remote-env-port",
            str(11002),
        ]
    )
    assert sum([1 for m in caplog.messages if "episode complete" in m]) == 3
