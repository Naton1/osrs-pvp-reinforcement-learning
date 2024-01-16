import abc
import asyncio
import concurrent
import csv
import logging
import os
import random
import time
from asyncio import AbstractEventLoop
from collections.abc import Callable, Generator
from typing import Any, TypedDict, TypeVar, cast

import numpy as np

from pvp_ml.callback.callback import Callback
from pvp_ml.callback.env_tracker_callback import EnvTrackerCallback
from pvp_ml.env.async_io_vec_env import AsyncIoVecEnv
from pvp_ml.env.pvp_env import PvpEnv, ResetOptions
from pvp_ml.ppo.buffer import Buffer
from pvp_ml.ppo.ppo import Meta
from pvp_ml.util.async_evaluator import AsyncEvaluator
from pvp_ml.util.distributed_helper import merge_meta_values
from pvp_ml.util.elo_tracker import EloTracker, Outcome
from pvp_ml.util.files import get_experiment_dir, get_file_name_pattern
from pvp_ml.util.match_outcome_tracker import MatchOutcomeTracker, merge_match_outcomes
from pvp_ml.util.remote_processor.remote_processor import RemoteProcessor
from pvp_ml.util.schedule import ConstantSchedule, Schedule

logger = logging.getLogger(__name__)
_latest_model_pattern = get_file_name_pattern()

T = TypeVar("T")


def _chunk(lst: list[T], n: int) -> Generator[list[T], None, None]:
    for i in range(0, len(lst), n):
        yield lst[i : i + n]


# Callback to manage environments outside the main training loop (example use cases: evaluation on a baseline)
class AdditionalEnvRunnerCallback(Callback, abc.ABC):
    def __init__(
        self,
        deterministic_percent: Schedule[float],
        loop: AbstractEventLoop,
        remote_processor: RemoteProcessor,
        env_kwargs: dict[str, Any],
        experiment_name: str,
        tracker_name: str,
        fight_mappings: dict[str, str],
        delay_chance: Schedule[float] = ConstantSchedule(0.0),
        use_vec_env: bool = False,
        track_match_outcomes: bool = False,
        track_elo: bool = False,
    ):
        super(AdditionalEnvRunnerCallback, self).__init__()
        self._deterministic_percent = deterministic_percent
        self._loop = loop
        self._remote_processor = remote_processor
        self._envs: list[PvpEnv] = []
        self._task_future: concurrent.futures.Future[None] | None = None
        self._env_kwargs = env_kwargs
        self._use_vec_env = use_vec_env
        self._vec_env: AsyncIoVecEnv | None = None
        self._fight_mappings = fight_mappings
        self._episodes: list[tuple[float, dict[str, Any]]] = []
        self._env_type_name = tracker_name
        self._delay_chance = delay_chance
        self._track_match_outcomes = track_match_outcomes
        self._tracker = EnvTrackerCallback(
            experiment_name=experiment_name, file_name=tracker_name
        )
        self._match_outcome_key = f"{self._env_type_name}_match_outcome"
        self._experiment_name = experiment_name
        self._elo_key = f"{self._env_type_name}_elo"
        self._latest_elo_key = "latest"
        self._track_elo = track_elo

    def on_rollout_start(self) -> None:
        self._tracker.on_rollout_start()
        if self._track_match_outcomes:
            self._get_match_outcomes().reset()
        self.__launch_environments()

    def on_step(self, *args: Any, **kwargs: Any) -> None:
        if self._envs:
            assert self._task_future is not None
            if self._task_future.done():
                result = (
                    self._task_future.result()
                )  # This will raise an exception if an exception was thrown
                raise ValueError(
                    f"Environments terminated early: {self._task_future}, result: {result} ({self._env_type_name})"
                )

    def on_distributed_rollout_collection(self, metas: list[Meta]) -> None:
        assert self._ppo is not None
        super().on_distributed_rollout_collection(metas)
        merge_meta_values(
            metas, self._match_outcome_key, self._ppo.meta, merge_match_outcomes
        )

    def on_rollout_sampling_end(self, buffer: Buffer) -> None:
        self._tracker.on_rollout_sampling_end(buffer)
        self.__end_environments()
        # Process episodes asap so if single distributed rollout, the data can be propagated back
        self._process_episodes(self._episodes)
        self._episodes.clear()

    def on_rollout_end(self, buffer: Buffer) -> None:
        self._tracker.on_rollout_end(buffer)
        self.__end_environments()
        if self._track_match_outcomes:
            self._write_win_rates_to_file()
        if self._track_elo:
            self._recalculate_elo()

    def on_training_end(self) -> None:
        self._tracker.on_training_end()
        self.__end_environments()

    def __launch_environments(self) -> None:
        assert not self._task_future
        assert not self._envs

        start_time = time.time()
        logger.debug(
            f"Launching environments of {self._env_type_name}: {self._env_kwargs}"
        )

        self._envs.extend(
            asyncio.run_coroutine_threadsafe(self.__create_envs(), self._loop).result()
        )

        if self._use_vec_env and self._envs:

            def _identity_lambda(env: PvpEnv) -> Callable[[], PvpEnv]:
                return lambda: env

            self._vec_env = AsyncIoVecEnv(
                env_fns=[_identity_lambda(e) for e in self._envs],
                loop=self._loop,
                reset_options=dict(ResetOptions(agent=self._select_target_model(None))),
            )

        coro = self.__run_vec_envs() if self._use_vec_env else self.__run_envs()

        self._task_future = asyncio.run_coroutine_threadsafe(coro, self._loop)
        self._task_future.add_done_callback(
            lambda f: logger.debug(
                f"Env task terminated: {self._env_type_name} - (exception={f.exception()})"
            )
        )

        if self._envs:
            logger.info(
                f"Launched {len(self._envs)} environments of {self._env_type_name}"
                f" in {time.time() - start_time} seconds"
            )

    def __end_environments(self) -> None:
        if self._task_future is None:
            logger.debug(f"Skipping clean up, no tasks running ({self._env_type_name})")
            return
        logger.debug(
            f"Cleaning up env handler: {len(self._envs)} environments ({self._env_type_name})"
        )
        num_envs = len(self._envs)
        start_time = time.time()
        close_futures = [
            asyncio.run_coroutine_threadsafe(env.close_async(), self._loop)
            for env in self._envs
        ]
        concurrent.futures.wait(close_futures)
        self._envs.clear()
        try:
            self._task_future.result()
        except Exception:
            # Ignore any exceptions that may have occurred while
            # it can happen during cleanup/if target env is dead
            pass
        if self._vec_env is not None:
            self._vec_env.close()
            self._vec_env = None
        self._task_future = None
        self._is_task_done = False
        if num_envs > 0:
            logger.info(
                f"Cleaned up {num_envs} environments in took {time.time() - start_time} for {self._env_type_name}"
            )

    async def __run_vec_envs(self) -> None:
        assert self._ppo is not None
        if self._vec_env is None:
            assert not self._envs
            # No environments
            return
        # VecEnv will only use a single model, and deterministic percent must be binary
        model_path = self._vec_env.reset_options["agent"]
        deterministic_percent = self._deterministic_percent.value(
            self._ppo.meta.trained_rollouts
        )
        assert (
            deterministic_percent == 0 or deterministic_percent == 1
        ), "Vec env deterministic percent must be 0 or 1"
        await AsyncEvaluator.vec_evaluate(
            self._vec_env,
            model_path,
            [
                self._delay_chance.value(self._ppo.meta.trained_rollouts)
                for _ in range(self._vec_env.num_envs)
            ],
            np.random.randint(0, self._remote_processor.get_pool_size()),
            self._remote_processor,
            bool(deterministic_percent),
            self._on_step,
            self._on_episode_end,
        )

    async def __run_envs(self) -> None:
        assert self._ppo is not None
        tasks = []
        deterministic_threshold = len(self._envs) * self._deterministic_percent.value(
            self._ppo.meta.trained_rollouts
        )
        logger.debug(
            f"Launching {deterministic_threshold}/{len(self._envs)} as deterministic target environment runners"
            f" ({self._env_type_name})"
        )
        for i, env in enumerate(self._envs):
            task = AsyncEvaluator.evaluate(
                env,
                lambda: self._select_target_model(env.env_id),
                i % self._remote_processor.get_pool_size(),
                self._remote_processor,
                i < deterministic_threshold,
                self._delay_chance.value(self._ppo.meta.trained_rollouts),
                self._on_step,
                self._on_episode_end,
            )
            tasks.append(task)
        await asyncio.gather(*tasks)

    async def __create_envs(self) -> list[PvpEnv]:
        return [
            await self._create_env(env_id, target)
            for env_id, target in self._fight_mappings.items()
        ]

    async def _create_env(self, env_id: str, target: str) -> PvpEnv:
        return PvpEnv(env_id=env_id, target=target, **self._env_kwargs)

    @abc.abstractmethod
    def _select_target_model(self, env_id: str | None) -> str:
        pass

    def _process_episodes(self, episodes: list[tuple[float, dict[str, Any]]]) -> None:
        pass

    def _on_episode_end(self, model: str, reward: float, info: dict[str, Any]) -> bool:
        self._episodes.append((reward, info))
        if self._track_match_outcomes:
            self._track_match_outcome(model, info)
        return False

    def _on_step(self, info: dict[str, Any]) -> None:
        self._tracker.on_step(np.array([]), np.array([info]))

    def _track_match_outcome(self, model: str, info: dict[str, Any]) -> None:
        player_name = os.path.basename(model)
        match_outcomes = self._get_match_outcomes()
        # We are tracking outcomes for the other player here, so invert results
        if info["terminal_state"] == "WON":
            match_outcomes.add_loss(player_name)
        elif info["terminal_state"] == "LOST":
            match_outcomes.add_win(player_name)
        elif info["terminal_state"] == "TIED":
            match_outcomes.add_tie(player_name)

    def _get_match_outcomes(self) -> MatchOutcomeTracker:
        assert self._track_match_outcomes
        assert self._ppo is not None
        if self._match_outcome_key not in self._ppo.meta.custom_data:
            self._ppo.meta.custom_data[
                self._match_outcome_key
            ] = self._create_match_outcome_tracker()
            logger.info(f"Created new match outcome tracker for {self._env_type_name}")
        return cast(
            MatchOutcomeTracker, self._ppo.meta.custom_data[self._match_outcome_key]
        )

    def _create_match_outcome_tracker(self) -> MatchOutcomeTracker:
        return MatchOutcomeTracker()

    def _write_win_rates_to_file(self) -> None:
        class _WinRateLeaderboard(TypedDict):
            model: str
            win_rate: float | str
            wins: int
            losses: int
            ties: int
            total_matches: int

        win_rates = sorted(
            [
                _WinRateLeaderboard(
                    model=model,
                    win_rate=(
                        (outcomes.wins + (outcomes.ties * 0.5))
                        / outcomes.total_matches()
                    )
                    if outcomes.total_matches() > 0
                    else "-",
                    wins=outcomes.wins,
                    losses=outcomes.losses,
                    ties=outcomes.ties,
                    total_matches=outcomes.total_matches(),
                )
                for model, outcomes in self._get_match_outcomes().list_outcomes()
            ],
            key=lambda x: x["total_matches"],
            reverse=True,
        )
        if not win_rates:
            return
        logger.info(f"Completed {len(win_rates)} matches for {self._env_type_name}")
        with open(
            f"{get_experiment_dir(self._experiment_name)}/{self._env_type_name}-win-rates.csv",
            "w",
            newline="",
        ) as output_file:
            dict_writer = csv.DictWriter(output_file, win_rates[0].keys())
            dict_writer.writeheader()
            dict_writer.writerows(win_rates)

    def _get_elo_tracker(self) -> EloTracker:
        assert self._track_elo
        assert self._ppo is not None
        if self._elo_key not in self._ppo.meta.custom_data:
            self._ppo.meta.custom_data[self._elo_key] = self._create_elo_tracker()
            logger.info(f"Created new elo tracker for {self._env_type_name}")
        return cast(EloTracker, self._ppo.meta.custom_data[self._elo_key])

    def _create_elo_tracker(self) -> EloTracker:
        return EloTracker()

    def _recalculate_elo(self) -> None:
        assert self._ppo is not None
        elo_tracker = self._get_elo_tracker()
        latest_player_rating = elo_tracker.get_player_rating(self._latest_elo_key)
        aggregated_outcomes = []
        average_match_elo = []
        for opponent, outcomes in self._get_match_outcomes().list_outcomes():
            if not elo_tracker.contains_player(opponent):
                if _latest_model_pattern.match(opponent):
                    # Use same skill as latest model, if adding a main model (likely just saved a new version)
                    elo_tracker.add_player(opponent, latest_player_rating)
                else:
                    # Otherwise, add as new player with defaults (such as an exploiter)
                    elo_tracker.add_player(opponent)
            average_match_elo.append(
                (elo_tracker.get_player_rating(opponent), outcomes.total_matches())
            )
            for _ in range(outcomes.wins):
                aggregated_outcomes.append(
                    (self._latest_elo_key, opponent, Outcome.WON)
                )
            for _ in range(outcomes.losses):
                aggregated_outcomes.append(
                    (self._latest_elo_key, opponent, Outcome.LOST)
                )
            for _ in range(outcomes.ties):
                aggregated_outcomes.append(
                    (self._latest_elo_key, opponent, Outcome.TIED)
                )
        if average_match_elo and self._summary_writer is not None:
            average_match_elos, average_match_elo_weights = zip(*average_match_elo)
            self._summary_writer.add_scalar(
                f"rollout/elo/average-match-{self._env_type_name}",
                np.average(average_match_elos, weights=average_match_elo_weights),
                self._ppo.meta.trained_steps,
            )
        # Process in randomized chunks so ELOs will gradually adjust to the right location,
        # and so order doesn't matter - all matches are treated similarly.
        # All matches in a rollout are using the same policy.
        random.shuffle(aggregated_outcomes)
        for batch in _chunk(aggregated_outcomes, 10):
            elo_tracker.add_outcomes(batch)
        self._write_elo_ratings()
        if self._summary_writer is not None:
            self._summary_writer.add_scalar(
                f"rollout/elo/latest-{self._env_type_name}",
                elo_tracker.get_player_rating(self._latest_elo_key),
                self._ppo.meta.trained_steps,
            )
        logger.info(
            f"Player rating went from {latest_player_rating} -> {elo_tracker.get_player_rating(self._latest_elo_key)}"
            f"for {self._env_type_name} after latest rollout"
        )

    def _write_elo_ratings(self) -> None:
        class _EloRatingLeaderboard(TypedDict):
            player: str
            rating: float

        leaderboard = sorted(
            [
                _EloRatingLeaderboard(
                    player=k,
                    rating=v,
                )
                for k, v in self._get_elo_tracker().list_ratings()
            ],
            key=lambda x: x["rating"],
            reverse=True,
        )
        if not leaderboard:
            return
        with open(
            f"{get_experiment_dir(self._experiment_name)}/{self._env_type_name}-elo-ratings.csv",
            "w",
            newline="",
        ) as output_file:
            dict_writer = csv.DictWriter(output_file, leaderboard[0].keys())
            dict_writer.writeheader()
            dict_writer.writerows(leaderboard)
