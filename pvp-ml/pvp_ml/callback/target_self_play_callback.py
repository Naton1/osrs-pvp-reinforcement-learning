import random
from asyncio import AbstractEventLoop
from typing import Any

from pvp_ml.callback.additional_env_runner_callback import AdditionalEnvRunnerCallback
from pvp_ml.util.remote_processor.remote_processor import RemoteProcessor
from pvp_ml.util.schedule import Schedule


class TargetSelfPlayCallback(AdditionalEnvRunnerCallback):
    def __init__(
        self,
        model_choices: list[str],
        past_self_targets: dict[str, str],
        self_play_deterministic_percent: Schedule[float],
        loop: AbstractEventLoop,
        remote_processor: RemoteProcessor,
        env_kwargs: dict[str, Any],
        experiment_name: str,
    ):
        super(TargetSelfPlayCallback, self).__init__(
            deterministic_percent=self_play_deterministic_percent,
            env_kwargs=env_kwargs,
            loop=loop,
            remote_processor=remote_processor,
            fight_mappings=past_self_targets,
            tracker_name="target-self-play-envs",
            experiment_name=experiment_name,
            use_vec_env=len(model_choices) == 1,
        )
        self._model_choices = model_choices
        self._past_self_targets = past_self_targets
        self._loop = loop

    def _select_target_model(self, env_id: str | None) -> str:
        return random.choice(self._model_choices)
