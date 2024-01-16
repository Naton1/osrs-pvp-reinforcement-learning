from asyncio import AbstractEventLoop
from typing import Any

from pvp_ml.callback.additional_env_runner_callback import AdditionalEnvRunnerCallback
from pvp_ml.util.files import get_most_recent_model
from pvp_ml.util.remote_processor.remote_processor import RemoteProcessor
from pvp_ml.util.schedule import Schedule


class LatestSelfPlayCallback(AdditionalEnvRunnerCallback):
    def __init__(
        self,
        past_self_targets: dict[str, str],
        self_play_deterministic_percent: Schedule[float],
        train_on_latest_experiment: str,
        loop: AbstractEventLoop,
        remote_processor: RemoteProcessor,
        delay_chance: Schedule[float],
        env_kwargs: dict[str, Any],
        experiment_name: str,
    ) -> None:
        super(LatestSelfPlayCallback, self).__init__(
            deterministic_percent=self_play_deterministic_percent,
            env_kwargs=env_kwargs,
            loop=loop,
            remote_processor=remote_processor,
            use_vec_env=True,
            fight_mappings=past_self_targets,
            tracker_name="latest-self-play-envs",
            experiment_name=experiment_name,
            delay_chance=delay_chance,
        )
        self._train_on_latest_experiment = train_on_latest_experiment
        self._latest: str | None = None

    def on_rollout_start(self) -> None:
        self._latest = get_most_recent_model(self._train_on_latest_experiment)
        super().on_rollout_start()

    def _select_target_model(self, env_id: str | None) -> str:
        assert self._latest is not None
        return self._latest
