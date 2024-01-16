import json
import logging
import os.path
from asyncio import AbstractEventLoop
from functools import cache
from typing import Any, cast

import numpy as np

from pvp_ml.callback.additional_env_runner_callback import AdditionalEnvRunnerCallback
from pvp_ml.ppo.buffer import Buffer
from pvp_ml.ppo.ppo import PPO, Meta
from pvp_ml.util.distributed_helper import merge_meta_values
from pvp_ml.util.files import (
    get_experiment_dir,
    get_experiment_models_dir,
    get_model_files,
)
from pvp_ml.util.json_encoders import GeneralizedObjectEncoder
from pvp_ml.util.league import League, merge_leagues
from pvp_ml.util.remote_processor.remote_processor import RemoteProcessor
from pvp_ml.util.schedule import Schedule

logger = logging.getLogger(__name__)


class PastSelfPlayCallback(AdditionalEnvRunnerCallback):
    def __init__(
        self,
        past_self_targets: dict[str, str],
        past_self_play_deterministic_percent: Schedule[float],
        past_self_play_learning_rate: Schedule[float],
        delay_chance: Schedule[float],
        experiment_name: str,
        train_on_experiment_name: str,
        loop: AbstractEventLoop,
        remote_processor: RemoteProcessor,
        env_kwargs: dict[str, Any],
        include_new_targets_in_league: bool = True,
    ):
        super(PastSelfPlayCallback, self).__init__(
            deterministic_percent=past_self_play_deterministic_percent,
            env_kwargs=env_kwargs,
            loop=loop,
            remote_processor=remote_processor,
            fight_mappings=past_self_targets,
            tracker_name="past-self-play-envs",
            experiment_name=experiment_name,
            delay_chance=delay_chance,
            track_match_outcomes=True,
            track_elo=True,
        )
        self._train_on_experiment_name = train_on_experiment_name
        self._past_self_targets = past_self_targets
        self._past_self_play_learning_rate = past_self_play_learning_rate
        self._meta_key = "past_self_play_league"
        self._include_new_targets_in_league = include_new_targets_in_league

    def on_rollout_start(self) -> None:
        if self._include_new_targets_in_league:
            self.__load_new_players()
        super().on_rollout_start()

    def on_rollout_end(self, buffer: Buffer) -> None:
        assert self._ppo is not None
        if self._summary_writer is not None:
            self._get_league().log_stats(
                "selfplay/past", self._ppo.meta.trained_steps, self._summary_writer
            )
        with open(
            f"{get_experiment_dir(self._experiment_name)}/past-self-play-league.json",
            "w",
        ) as f:
            # Save the league to a file for visibility
            json.dump(self._get_league(), f, cls=GeneralizedObjectEncoder, indent=2)
        self._calculate_average_target_version()
        super().on_rollout_end(buffer)

    def on_distributed_rollout_collection(self, metas: list[Meta]) -> None:
        assert self._ppo is not None
        super().on_distributed_rollout_collection(metas)
        merge_meta_values(metas, self._meta_key, self._ppo.meta, merge_leagues)

    def _select_target_model(self, env_id: str | None) -> str:
        while True:
            opponent = self._get_league().sample_opponent()
            model = f"{get_experiment_models_dir(self._train_on_experiment_name)}/{opponent}"
            if not os.path.exists(model):
                if self._get_league().remove_opponent(opponent):
                    logger.warning(f"Removed missing league opponent: {opponent}")
                continue
            return model

    def _on_episode_end(self, model: str, reward: float, info: dict[str, Any]) -> bool:
        assert self._ppo is not None
        super_result = super()._on_episode_end(model, reward, info)
        model = os.path.basename(model)
        # If this model lost against the main model, the main model won so lower quality score
        if info["terminal_state"] == "LOST":
            logger.debug(f"Model lost: {model}, dropping quality")
            self._get_league().add_win(
                model,
                learning_rate=self._past_self_play_learning_rate.value(
                    self._ppo.meta.trained_rollouts
                ),
            )
        return super_result

    def _get_league(self) -> League:
        assert self._ppo is not None
        if self._meta_key not in self._ppo.meta.custom_data:
            self._ppo.meta.custom_data[self._meta_key] = League()
        return cast(League, self._ppo.meta.custom_data[self._meta_key])

    def _calculate_average_target_version(self) -> None:
        if self._summary_writer is None:
            return
        assert self._ppo is not None
        model_dir = get_experiment_models_dir(self._train_on_experiment_name)
        current_rollouts = self._ppo.meta.trained_rollouts
        average_rollouts_since_model_snapshot = []
        average_snapshot_rollouts = []
        for player, outcomes in self._get_match_outcomes().list_outcomes():
            total_matches = outcomes.total_matches()
            if not total_matches:
                continue
            target_rollouts = self._get_trained_rollouts(f"{model_dir}/{player}")
            for _ in range(total_matches):
                average_rollouts_since_model_snapshot.append(
                    current_rollouts - target_rollouts
                )
                average_snapshot_rollouts.append(target_rollouts)
        if average_snapshot_rollouts:
            self._summary_writer.add_scalar(
                "selfplay/average-target-rollouts", np.mean(average_snapshot_rollouts)
            )
            self._summary_writer.add_scalar(
                "selfplay/average-target-rollout-difference",
                np.mean(average_rollouts_since_model_snapshot),
            )

    @cache
    def _get_trained_rollouts(self, model: str) -> int:
        model_meta = PPO.load_meta(model)
        return model_meta.trained_rollouts

    def __load_new_players(self) -> None:
        # Get adversaries + main models
        for model_file in get_model_files(
            self._train_on_experiment_name,
        ):
            model_name = os.path.basename(model_file)
            if not self._get_league().contains_opponent(model_name):
                logger.debug(f"Adding new player to league: {model_name}")
                self._get_league().add_opponent(model_name)
