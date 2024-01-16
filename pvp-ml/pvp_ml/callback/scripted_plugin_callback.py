from asyncio import AbstractEventLoop
from typing import Any

from pvp_ml.callback.additional_env_runner_callback import AdditionalEnvRunnerCallback
from pvp_ml.util.remote_processor.remote_processor import RemoteProcessor
from pvp_ml.util.schedule import ConstantSchedule, Schedule


class ScriptedPluginCallback(AdditionalEnvRunnerCallback):
    def __init__(
        self,
        plugin: str,
        targets: dict[str, str],
        loop: AbstractEventLoop,
        remote_processor: RemoteProcessor,
        delay_chance: Schedule[float],
        env_kwargs: dict[str, Any],
        experiment_name: str,
    ):
        super(ScriptedPluginCallback, self).__init__(
            deterministic_percent=ConstantSchedule(1),
            env_kwargs=env_kwargs,
            loop=loop,
            remote_processor=remote_processor,
            use_vec_env=False,
            fight_mappings=targets,
            tracker_name=f"scripted-plugin-{plugin}",
            experiment_name=experiment_name,
            delay_chance=delay_chance,
        )
        self._plugin = plugin

    def _select_target_model(self, env_id: str | None) -> str:
        return self._plugin
