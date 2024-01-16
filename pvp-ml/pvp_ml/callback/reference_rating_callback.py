import logging
import math
import random
from asyncio import AbstractEventLoop
from typing import Any

from pvp_ml.callback.additional_env_runner_callback import AdditionalEnvRunnerCallback
from pvp_ml.callback.callback_list import CallbackList
from pvp_ml.env.pvp_env import PvpEnv
from pvp_ml.ppo.ppo import PPO
from pvp_ml.util.elo_tracker import EloTracker
from pvp_ml.util.files import get_most_recent_model, reference_dir
from pvp_ml.util.match_outcome_tracker import MatchOutcomeTracker
from pvp_ml.util.reference_rating import create_reference_elo_tracker
from pvp_ml.util.remote_processor.remote_processor import RemoteProcessor
from pvp_ml.util.schedule import ConstantSchedule, Schedule

logger = logging.getLogger(__name__)


def _choose_opponent(elo_tracker: EloTracker, player: str, scale: int = 400) -> str:
    # Weight opponents based on elo difference to player - prefer closer elo
    potential_opponents = [
        target for target, _ in elo_tracker.list_ratings() if target != player
    ]
    weights = [
        math.exp(
            -abs(
                elo_tracker.get_player_rating(player)
                - elo_tracker.get_player_rating(target)
            )
            / scale
        )
        for target in potential_opponents
    ]
    return random.choices(potential_opponents, weights=weights, k=1)[0]


class _ReferenceTargetCallback(AdditionalEnvRunnerCallback):
    def __init__(
        self,
        experiment_name: str,
        env_kwargs: dict[str, Any],
        loop: AbstractEventLoop,
        remote_processor: RemoteProcessor,
        eval_deterministic_percent: Schedule[float] = ConstantSchedule(0.0),
    ):
        self._model_selections: dict[str, str] = {}
        super(_ReferenceTargetCallback, self).__init__(
            deterministic_percent=eval_deterministic_percent,
            env_kwargs=env_kwargs,
            loop=loop,
            remote_processor=remote_processor,
            fight_mappings={},
            use_vec_env=False,
            tracker_name="reference-agent-envs",
            experiment_name=experiment_name,
            track_match_outcomes=True,
            track_elo=True,
        )

    def get_reference_outcomes(self) -> MatchOutcomeTracker:
        return self._get_match_outcomes()

    def register_fight_mappings(self, fight_mappings: dict[str, str]) -> None:
        self._fight_mappings.clear()
        self._fight_mappings.update(fight_mappings)
        self._model_selections.clear()
        # Update any existing elo tracker with new reference agents, if available
        for player, rating in self._create_elo_tracker().list_ratings():
            self._get_elo_tracker().add_player(player, rating)
        # Generate model selections here, so we have the models when creating environments to load kwargs
        for key in self._fight_mappings.keys():
            selected_opponent = _choose_opponent(
                self._get_elo_tracker(), self._latest_elo_key
            )
            self._model_selections[key] = f"{reference_dir}/{selected_opponent}"
        if self._model_selections:
            logger.info(
                f"Generated target selections"
                f" at {self._get_elo_tracker().get_player_rating(self._latest_elo_key)} elo"
                f" for {self._env_type_name}: {self._model_selections}"
            )

    def _create_elo_tracker(self) -> EloTracker:
        return create_reference_elo_tracker(self._env_kwargs[PvpEnv.ENV_NAME_KEY])

    def _select_target_model(self, env_id: str | None) -> str:
        # Note this always uses the same target for an environment to maintain that target's env configuration
        # Env params can't be swapped out for an env mid-rollout (at the moment)
        assert env_id is not None
        return self._model_selections[env_id]

    async def _create_env(self, env_id: str, target: str) -> PvpEnv:
        # Use saved env kwargs for the corresponding model instead of the current experiment kwargs
        meta = PPO.load_meta(self._model_selections[env_id])
        saved_env_kwargs = meta.custom_data["env_kwargs"]
        saved_env_kwargs[PvpEnv.REMOTE_ENV_PORT_KEY] = self._env_kwargs[
            PvpEnv.REMOTE_ENV_PORT_KEY
        ]
        saved_env_kwargs[PvpEnv.REMOTE_ENV_HOST_KEY] = self._env_kwargs[
            PvpEnv.REMOTE_ENV_HOST_KEY
        ]
        return PvpEnv(env_id=env_id, target=target, **saved_env_kwargs)


class _ReferencePlayerCallback(AdditionalEnvRunnerCallback):
    def __init__(
        self,
        experiment_name: str,
        loop: AbstractEventLoop,
        remote_processor: RemoteProcessor,
        env_kwargs: dict[str, Any],
        eval_deterministic_percent: Schedule[float] = ConstantSchedule(0.0),
    ):
        self._target_model: str | None = None
        super(_ReferencePlayerCallback, self).__init__(
            deterministic_percent=eval_deterministic_percent,
            env_kwargs=env_kwargs,
            loop=loop,
            remote_processor=remote_processor,
            fight_mappings={},
            use_vec_env=True,
            tracker_name="reference-rating-envs",
            experiment_name=experiment_name,
        )

    def register_fight_mappings(self, fight_mappings: dict[str, str]) -> None:
        self._fight_mappings.clear()
        self._fight_mappings.update(fight_mappings)
        self._target_model = get_most_recent_model(self._experiment_name)

    def _select_target_model(self, env_id: str | None) -> str:
        assert self._target_model is not None
        return self._target_model


class ReferenceRatingCallback(CallbackList):
    def __init__(
        self,
        num_reference_eval_agents: Schedule[int],
        experiment_name: str,
        loop: AbstractEventLoop,
        remote_processor: RemoteProcessor,
        env_kwargs: dict[str, Any],
    ):
        self._num_reference_eval_agents = num_reference_eval_agents
        self._reference_player_callback = _ReferencePlayerCallback(
            experiment_name=experiment_name,
            loop=loop,
            remote_processor=remote_processor,
            env_kwargs=env_kwargs,
        )
        self._reference_target_callback = _ReferenceTargetCallback(
            experiment_name=experiment_name,
            loop=loop,
            remote_processor=remote_processor,
            env_kwargs=env_kwargs,
        )
        super().__init__(
            [self._reference_player_callback, self._reference_target_callback]
        )

    def on_rollout_start(self) -> None:
        assert self._ppo is not None
        num_agents = self._num_reference_eval_agents.value(
            self._ppo.meta.trained_rollouts
        )
        fight_mappings = {f"RP {i}": f"RT {i}" for i in range(num_agents)}
        reversed_fight_mappings = {value: key for key, value in fight_mappings.items()}
        self._reference_player_callback.register_fight_mappings(fight_mappings)
        self._reference_target_callback.register_fight_mappings(reversed_fight_mappings)
        super().on_rollout_start()
