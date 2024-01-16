import asyncio
import itertools
import logging
import time
from asyncio import AbstractEventLoop
from collections import deque
from dataclasses import dataclass, field
from typing import Any, TypedDict, cast
from uuid import uuid4

import numpy as np
from gymnasium import spaces
from numpy.typing import NDArray

from pvp_ml.env.async_io_env import AsyncIoEnv
from pvp_ml.env.remote_env_connector import RemoteEnvConnector
from pvp_ml.util.contract_loader import (
    ActionDependencies,
    EnvironmentMeta,
    load_environment_contract,
)
from pvp_ml.util.noise_generator import NoiseGenerator
from pvp_ml.util.schedule import ConstantSchedule, Schedule

logger = logging.getLogger(__name__)


class ResetOptions(TypedDict, total=False):
    trained_steps: int
    trained_rollouts: int
    agent: str


@dataclass
class _EpisodeContext:
    episode_id: str = field(default_factory=lambda: str(uuid4()))

    last_tick_start: float = field(default_factory=lambda: time.time())
    last_tick_end: float = field(default_factory=lambda: time.time())

    desynced_ticks: int = 0

    action_masks: NDArray[np.bool_] = field(
        default_factory=lambda: np.empty((), dtype=bool)
    )
    previous_meta: dict[str, Any] = field(default_factory=lambda: {})

    steps: int = 0
    total_reward: float = 0

    trained_steps: int = 0
    trained_rollouts: int = 0
    agent: str = ""

    last_obs: NDArray[np.float32] | None = None
    frame_history: deque[NDArray[np.float32]] | None = None


class _TruncateException(Exception):
    def __init__(self, message: str, reason: str):
        super().__init__(message)
        self.reason = reason


class PvpEnv(AsyncIoEnv[NDArray[np.float32], NDArray[np.int32]]):
    """
    This provides a traditional gym interface for the game simulation, which it connects to via a TCP socket.
    Step and reset requests will be sent to the remote environment. It offers async alternatives for optimized handling.
    """

    REMOTE_ENV_HOST_KEY = "remote_environment_host"
    REMOTE_ENV_PORT_KEY = "remote_environment_port"
    ENV_NAME_KEY = "env_name"
    ENV_ID_KEY = "env_id"

    meta: EnvironmentMeta
    # Gymnasium MultiDiscrete types are bad so it doesn't work with a typed Env, so ignore
    action_space: spaces.MultiDiscrete  # type: ignore[assignment]
    observation_space: spaces.Box
    partial_observation_space: spaces.Box
    action_dependencies: ActionDependencies

    def __init__(
        self,
        env_name: str,
        env_id: str = "PvpEnv",
        default_reward: Schedule[float] = ConstantSchedule(0.0),
        win_reward: Schedule[float] = ConstantSchedule(1.0),
        lose_reward: Schedule[float] = ConstantSchedule(-1.0),
        tie_reward: Schedule[float] = ConstantSchedule(-0.2),
        safe_penalty: Schedule[float] = ConstantSchedule(
            0.0
        ),  # common: (health ** 2) * 0.1
        death_match: bool = True,
        penalize_food_on_death: bool = True,
        reward_target_food_on_death: bool = True,
        penalize_wasted_food: bool = True,
        reward_on_damage_generated: bool = True,
        reward_heals: bool = True,
        penalize_target_heals: bool = True,
        target: str = "baseline",
        desync_tolerance_seconds: float = 0.0,  # should depend on if sync is used
        truncate_on_desync: bool = True,
        damage_received_reward_scale: Schedule[float] = ConstantSchedule(0.0),
        damage_dealt_reward_scale: Schedule[float] = ConstantSchedule(0.0),
        stack_frames: int | list[int] = 1,
        target_frozen_tick_reward: Schedule[float] = ConstantSchedule(0.0),
        player_frozen_tick_reward: Schedule[float] = ConstantSchedule(0.0),
        protected_correct_prayer_reward: Schedule[float] = ConstantSchedule(0.0),
        protected_wrong_prayer_reward: Schedule[float] = ConstantSchedule(0.0),
        attacked_correct_prayer_reward: Schedule[float] = ConstantSchedule(0.0),
        attacked_wrong_prayer_reward: Schedule[float] = ConstantSchedule(0.0),
        protected_previous_correct_prayer_reward: Schedule[float] = ConstantSchedule(
            0.0
        ),
        protected_previous_wrong_prayer_reward: Schedule[float] = ConstantSchedule(0.0),
        attacked_previous_correct_prayer_reward: Schedule[float] = ConstantSchedule(
            0.0
        ),
        attacked_previous_wrong_prayer_reward: Schedule[float] = ConstantSchedule(0.0),
        attack_level_scale_reward: Schedule[float] = ConstantSchedule(0.0),
        strength_level_scale_reward: Schedule[float] = ConstantSchedule(0.0),
        defense_level_scale_reward: Schedule[float] = ConstantSchedule(0.0),
        ranged_level_scale_reward: Schedule[float] = ConstantSchedule(0.0),
        magic_level_scale_reward: Schedule[float] = ConstantSchedule(0.0),
        reward_on_hit_with_boost_scale: Schedule[float] = ConstantSchedule(0.0),
        smite_damage_dealt_reward_multiplier: Schedule[float] = ConstantSchedule(0.0),
        smite_damage_received_reward_multiplier: Schedule[float] = ConstantSchedule(
            0.0
        ),
        player_died_with_food_multiplier: Schedule[float] = ConstantSchedule(1.0),
        player_wasted_food_multiplier: Schedule[float] = ConstantSchedule(1.0),
        custom_reward_fn: Schedule[float] = ConstantSchedule(0.0),
        no_prayer_tick_reward: Schedule[float] = ConstantSchedule(0.0),
        desync_tick_threshold: int = 2,
        action_mask_override: Schedule[NDArray[np.bool_]] | None = None,
        remote_environment_host: str = "localhost",
        remote_environment_port: int = 7070,
        noise_generator: NoiseGenerator | None = None,
        reset_params: dict[str, Any] = {},
        include_target_obs_in_critic: bool = False,
        training: bool = False,
        loop: AbstractEventLoop | None = None,
    ):
        super().__init__(loop)
        self._env_id = env_id
        self._target = target
        self._default_reward = default_reward
        self._win_reward = win_reward
        self._lose_reward = lose_reward
        self._tie_reward = tie_reward
        self._safe_penalty = safe_penalty
        self._death_match = death_match
        self._penalize_food_on_death = penalize_food_on_death
        self._reward_target_food_on_death = reward_target_food_on_death
        self._penalize_wasted_food = penalize_wasted_food
        self._reward_on_damage_generated = reward_on_damage_generated
        self._desync_tolerance_seconds = desync_tolerance_seconds
        self._damage_received_reward_scale = damage_received_reward_scale
        self._damage_dealt_reward_scale = damage_dealt_reward_scale
        self._protected_correct_prayer_reward = protected_correct_prayer_reward
        self._protected_wrong_prayer_reward = protected_wrong_prayer_reward
        self._attacked_correct_prayer_reward = attacked_correct_prayer_reward
        self._attacked_wrong_prayer_reward = attacked_wrong_prayer_reward
        self._protected_previous_correct_prayer_reward = (
            protected_previous_correct_prayer_reward
        )
        self._protected_previous_wrong_prayer_reward = (
            protected_previous_wrong_prayer_reward
        )
        self._attacked_previous_correct_prayer_reward = (
            attacked_previous_correct_prayer_reward
        )
        self._attacked_previous_wrong_prayer_reward = (
            attacked_previous_wrong_prayer_reward
        )
        self._target_frozen_tick_reward = target_frozen_tick_reward
        self._player_frozen_tick_reward = player_frozen_tick_reward
        self._desync_tick_threshold = desync_tick_threshold
        self._truncate_on_desync = truncate_on_desync
        self._action_mask_override = action_mask_override
        self._remote_environment_host = remote_environment_host
        self._remote_environment_port = remote_environment_port
        self._attack_level_scale_reward = attack_level_scale_reward
        self._strength_level_scale_reward = strength_level_scale_reward
        self._defense_level_scale_reward = defense_level_scale_reward
        self._ranged_level_scale_reward = ranged_level_scale_reward
        self._magic_level_scale_reward = magic_level_scale_reward
        self._reward_on_hit_with_boost_scale = reward_on_hit_with_boost_scale
        self._noise_generator = noise_generator
        self._reset_params = reset_params
        self._reward_heals = reward_heals
        self._penalize_target_heals = penalize_target_heals
        self._smite_damage_dealt_reward_multiplier = (
            smite_damage_dealt_reward_multiplier
        )
        self._smite_damage_received_reward_multiplier = (
            smite_damage_received_reward_multiplier
        )
        self._player_died_with_food_multiplier = player_died_with_food_multiplier
        self._player_wasted_food_multiplier = player_wasted_food_multiplier
        self._no_prayer_tick_reward = no_prayer_tick_reward
        self._custom_reward_fn = custom_reward_fn
        self._include_target_obs_in_critic = include_target_obs_in_critic
        self._training = training
        self._env_name = env_name

        if isinstance(stack_frames, int):
            stack_frames = list(range(stack_frames))
        assert isinstance(stack_frames, list)
        assert all(isinstance(frame, int) for frame in stack_frames)
        stack_frames.sort()
        self._stack_frames = stack_frames

        self.meta = load_environment_contract(self._env_name)

        self.action_space = self.meta.get_action_space()
        self.observation_space = self.meta.get_observation_space()
        self.partial_observation_space = self.observation_space
        self.action_dependencies = self.meta.get_action_dependency_config()
        self._partially_observable_indices = (
            self.meta.get_partially_observable_indices()
        )

        if include_target_obs_in_critic:
            # Double observation space to include target observations
            assert len(
                self.observation_space.shape
            ), f"Observation space not flat: {self.observation_space.shape}"

            def _double_obs(observation_space: spaces.Box) -> spaces.Box:
                new_low = np.concatenate(
                    [
                        observation_space.low,
                        observation_space.low[self._partially_observable_indices],
                    ]
                )
                new_high = np.concatenate(
                    [
                        observation_space.high,
                        observation_space.high[self._partially_observable_indices],
                    ]
                )
                assert observation_space.dtype is not None
                np_type = cast(
                    type[np.floating[Any]] | type[np.integer[Any]],
                    observation_space.dtype.type,
                )
                return spaces.Box(low=new_low, high=new_high, dtype=np_type)

            # Add to main observation space only, actor only sees partial
            self.observation_space = _double_obs(self.observation_space)

        def _stack_frames(observation_space: spaces.Box) -> spaces.Box:
            low = np.repeat(
                observation_space.low[np.newaxis, ...], len(self._stack_frames), axis=0
            )
            high = np.repeat(
                observation_space.high[np.newaxis, ...], len(self._stack_frames), axis=0
            )
            assert observation_space.dtype is not None
            np_type = cast(
                type[np.floating[Any]] | type[np.integer[Any]],
                observation_space.dtype.type,
            )
            return spaces.Box(low=low, high=high, dtype=np_type)

        self.partial_observation_space = _stack_frames(self.partial_observation_space)
        self.observation_space = _stack_frames(self.observation_space)

        self._remote_env_connector: RemoteEnvConnector = RemoteEnvConnector(
            env_id=env_id,
            port=self._remote_environment_port,
            host=self._remote_environment_host,
        )

        self._logged_in: bool = False
        self._closed: bool = False
        self._episode_context: _EpisodeContext | None = None

        self._lock = asyncio.Lock()  # Synchronize logging in/out

        logger.debug(f"Created environment: {self._env_id}")

    @property
    def env_id(self) -> str:
        return self._env_id

    async def step_async(
        self, action: NDArray[np.int32]
    ) -> tuple[NDArray[np.float32], float, bool, bool, dict[str, Any]]:
        self._assert_calling_from_loop()
        assert self._episode_context is not None, "No reset performed before stepping"
        assert self._logged_in, "Not logged in - must reset first"
        assert not self._closed, "Env already closed"

        try:
            return await self.__step(action)
        except _TruncateException as e:
            if not self._truncate_on_desync:
                raise
            assert (
                self._episode_context.last_obs is not None
            ), "Desynced without previous obs"
            # Treat de-syncing from the remote environment as a truncation
            # to make system as robust as possible
            logger.debug(f"Episode timed out for {self._env_id}: {e}")
            return (
                self._episode_context.last_obs,
                0,
                True,
                True,
                {
                    "terminal_state": "DESYNC",
                    "id": self._env_id,
                    "target": self._target,
                    "desync_reason": e.reason,
                    "agent": self._episode_context.agent,
                    "episode_id": self._episode_context.episode_id,
                },
            )

    async def reset_async(
        self,
        *,
        seed: int | None = None,
        options: ResetOptions | dict[str, Any] | None = None,
    ) -> tuple[NDArray[np.float32], dict[str, Any]]:
        assert seed is None, "Random seed not supported"
        self._assert_calling_from_loop()
        logger.debug(f"Resetting environment: {self._env_id}")

        async with self._lock:
            assert not self._closed, "Env already closed"
            await self.__login()

        self._episode_context = _EpisodeContext()

        response = await self.__reset()
        if options is not None:
            self._episode_context.trained_steps = options.get(
                "trained_steps", self._episode_context.trained_steps
            )
            self._episode_context.trained_rollouts = options.get(
                "trained_rollouts", self._episode_context.trained_rollouts
            )
            self._episode_context.agent = options.get(
                "agent", self._episode_context.agent
            )

        self._episode_context.action_masks = response["actionMasks"]
        return (
            self.__process_obs(response["obs"], response["meta"].get("targetObs")),
            {},
        )

    def render(self) -> None:
        raise NotImplementedError

    def get_action_masks(self) -> NDArray[np.bool_]:
        assert (
            self._episode_context is not None
        ), "No reset performed before trying to get action masks"
        masks = self._episode_context.action_masks
        if self._action_mask_override is not None:
            masks = masks & self._action_mask_override.value(
                self._episode_context.trained_rollouts
            )
        return masks

    async def close_async(self) -> None:
        self._assert_calling_from_loop()
        async with self._lock:
            if self._closed:
                logger.debug(f"Environment already closed: {self._env_id}")
                return
            self._closed = True
            logger.debug(f"Closing environment: {self._env_id}")

            # Closing is a bit of a hack.
            # We create a new connection so that it doesn't conflict with an existing active request
            # then logout, and close both connections. This will cause an active request in the main connection to end.
            # This also makes sure it logs out even if the socket had disconnected.
            tmp = self._remote_env_connector
            self._remote_env_connector = RemoteEnvConnector(
                env_id=self._env_id,
                port=self._remote_environment_port,
                host=self._remote_environment_host,
            )
            try:
                await self.__logout()
            finally:
                await self._remote_env_connector.close()
                await tmp.close()

    def is_closed(self) -> bool:
        return self._closed

    def log(
        self,
        trained_rollouts: int,
        trained_steps: int,
        summary_writer: Any,
    ) -> None:
        # Don't import SummaryWriter (and take dependency on pytorch) unless needed
        # Could extract this logic out somehow (ex. return dict of info) and log outside
        from torch.utils.tensorboard import SummaryWriter

        assert isinstance(summary_writer, SummaryWriter)
        summary_writer.add_scalar(
            "env/pvp/default_reward",
            self._default_reward.value(trained_rollouts),
            trained_steps,
        )
        summary_writer.add_scalar(
            "env/pvp/win_reward",
            self._win_reward.value(trained_rollouts),
            trained_steps,
        )
        summary_writer.add_scalar(
            "env/pvp/lose_reward",
            self._lose_reward.value(trained_rollouts),
            trained_steps,
        )
        summary_writer.add_scalar(
            "env/pvp/tie_reward",
            self._tie_reward.value(trained_rollouts),
            trained_steps,
        )
        summary_writer.add_scalar(
            "env/pvp/target_frozen_tick_reward",
            self._target_frozen_tick_reward.value(trained_rollouts),
            trained_steps,
        )
        summary_writer.add_scalar(
            "env/pvp/player_frozen_tick_reward",
            self._player_frozen_tick_reward.value(trained_rollouts),
            trained_steps,
        )
        summary_writer.add_scalar(
            "env/pvp/protected_correct_prayer_reward",
            self._protected_correct_prayer_reward.value(trained_rollouts),
            trained_steps,
        )
        summary_writer.add_scalar(
            "env/pvp/protected_wrong_prayer_reward",
            self._protected_wrong_prayer_reward.value(trained_rollouts),
            trained_steps,
        )
        summary_writer.add_scalar(
            "env/pvp/attacked_correct_prayer_reward",
            self._attacked_correct_prayer_reward.value(trained_rollouts),
            trained_steps,
        )
        summary_writer.add_scalar(
            "env/pvp/attacked_wrong_prayer_reward",
            self._attacked_wrong_prayer_reward.value(trained_rollouts),
            trained_steps,
        )
        summary_writer.add_scalar(
            "env/pvp/protected_previous_correct_prayer_reward",
            self._protected_previous_correct_prayer_reward.value(trained_rollouts),
            trained_steps,
        )
        summary_writer.add_scalar(
            "env/pvp/protected_previous_wrong_prayer_reward",
            self._protected_previous_wrong_prayer_reward.value(trained_rollouts),
            trained_steps,
        )
        summary_writer.add_scalar(
            "env/pvp/attacked_previous_correct_prayer_reward",
            self._attacked_previous_correct_prayer_reward.value(trained_rollouts),
            trained_steps,
        )
        summary_writer.add_scalar(
            "env/pvp/attacked_previous_wrong_prayer_reward",
            self._attacked_previous_wrong_prayer_reward.value(trained_rollouts),
            trained_steps,
        )
        summary_writer.add_scalar(
            "env/pvp/attack_level_scale_reward",
            self._attack_level_scale_reward.value(trained_rollouts),
            trained_steps,
        )
        summary_writer.add_scalar(
            "env/pvp/strength_level_scale_reward",
            self._strength_level_scale_reward.value(trained_rollouts),
            trained_steps,
        )
        summary_writer.add_scalar(
            "env/pvp/defense_level_scale_reward",
            self._defense_level_scale_reward.value(trained_rollouts),
            trained_steps,
        )
        summary_writer.add_scalar(
            "env/pvp/ranged_level_scale_reward",
            self._ranged_level_scale_reward.value(trained_rollouts),
            trained_steps,
        )
        summary_writer.add_scalar(
            "env/pvp/magic_level_scale_reward",
            self._magic_level_scale_reward.value(trained_rollouts),
            trained_steps,
        )
        summary_writer.add_scalar(
            "env/pvp/damage_received_reward_scale",
            self._damage_received_reward_scale.value(trained_rollouts),
            trained_steps,
        )
        summary_writer.add_scalar(
            "env/pvp/damage_dealt_reward_scale",
            self._damage_dealt_reward_scale.value(trained_rollouts),
            trained_steps,
        )
        summary_writer.add_scalar(
            "env/pvp/reward_on_hit_with_boost_scale",
            self._reward_on_hit_with_boost_scale.value(trained_rollouts),
            trained_steps,
        )
        summary_writer.add_scalar(
            "env/pvp/smite_dealt_reward_scale",
            self._smite_damage_dealt_reward_multiplier.value(trained_rollouts),
            trained_steps,
        )
        summary_writer.add_scalar(
            "env/pvp/smite_received_reward_scale",
            self._smite_damage_received_reward_multiplier.value(trained_rollouts),
            trained_steps,
        )
        summary_writer.add_scalar(
            "env/pvp/player_died_with_food_scale",
            self._player_died_with_food_multiplier.value(trained_rollouts),
            trained_steps,
        )
        summary_writer.add_scalar(
            "env/pvp/player_wasted_food_scale",
            self._player_wasted_food_multiplier.value(trained_rollouts),
            trained_steps,
        )

    async def __login(self) -> None:
        if self._logged_in:
            return
        # Set it right away, since we could 'log in' even if the request seems to fail
        self._logged_in = True
        start_time = time.time()
        logger.debug(f"Logging in: {self._env_id}")
        body = {
            "agentType": self._env_name,
        }
        await self._remote_env_connector.send(action="login", body=body)
        logger.debug(f"Logged in: {self._env_id} in {time.time() - start_time} seconds")

    async def __logout(self) -> None:
        if not self._logged_in:
            logger.debug(f"Skipped logout - we are not logged in, env: {self._env_id}")
            return
        start_time = time.time()
        logger.debug(f"Logging out: {self._env_id}")
        await self._remote_env_connector.send(action="logout")
        logger.debug(
            f"Logged out: {self._env_id} in {time.time() - start_time} seconds"
        )
        self._logged_in = False

    async def __reset(self) -> dict[str, Any]:
        assert self._episode_context is not None
        body = {
            "target": self._target,
            "training": self._training,
            "maintainTargetEnvironment": self._include_target_obs_in_critic
            and self._training,
            "deathMatch": self._death_match,
            "resetParams": {
                "agent": self._env_id,
                "target": self._target,
                "episodeId": self._episode_context.episode_id,
                **self._reset_params,
            },
        }
        start_time = time.time()
        logger.debug(f"Sending reset request to remote agent {self._env_id}: {body}")
        res_json = await self._remote_env_connector.send(action="reset", body=body)
        logger.debug(
            f"Received response from remote agent {self._env_id} in {time.time() - start_time} seconds"
        )
        self.__transform_response(res_json)
        return cast(dict[str, Any], res_json)

    async def __step(
        self, action: NDArray[np.int32]
    ) -> tuple[NDArray[np.float32], float, bool, bool, dict[str, Any]]:
        assert self._episode_context is not None

        start_info = self.__log_step_start(action)

        response = await self.__send_step(body={"action": action.astype(int).tolist()})
        self._episode_context.action_masks = response["actionMasks"]

        terminated = "terminalState" in response
        info = {
            "meta": response["meta"],
            "id": self._env_id,
            "target": self._target,
            "agent": self._episode_context.agent,
            "episode_id": self._episode_context.episode_id,
            **start_info,
        }
        obs = response["obs"]
        reward = self.__generate_reward(response, info)

        self._episode_context.steps += 1
        self._episode_context.total_reward += reward

        desynced_ticks = response["meta"]["episodeTicks"] - self._episode_context.steps
        desync_change = desynced_ticks - self._episode_context.desynced_ticks
        self._episode_context.desynced_ticks = desynced_ticks
        info["desync"] = {
            "since_last_tick": desync_change,
            "total_desync": desynced_ticks,
        }
        # Tick 1 will always 'desync' because resetting takes awhile
        if (
            self._episode_context.steps > 1
            and desync_change > self._desync_tick_threshold
        ):
            raise _TruncateException(
                f"Desynced by {desynced_ticks} ticks (this tick: {desync_change}) between steps",
                "TICK_DESYNC",
            )

        obs = self.__process_obs(obs, response["meta"].get("targetObs"))

        info.update(self.__log_step_end())

        logger.debug(
            f"Step Result. "
            f"Reward: {reward}, "
            f"Obs: {obs}, "
            f"Masks: {self._episode_context.action_masks}, "
            f"Done: {terminated}"
        )

        self._episode_context.previous_meta = response["meta"]

        if terminated:
            info["episode"] = {
                "length": self._episode_context.steps,
                "reward": self._episode_context.total_reward,
                "desync_ticks": response["meta"]["episodeTicks"]
                - self._episode_context.steps,
                "remaining_food": response["meta"]["foodCount"],
            }
            self._episode_context = None

        return obs, reward, terminated, False, info

    def __generate_reward(
        self, response: dict[str, Any], info: dict[str, Any]
    ) -> float:
        assert self._episode_context is not None

        # Ideally this parsing is doable by the dynamic tracker callback, but helper parsing logic here for now
        response["meta"]["meleeDamageDealt"] = (
            response["meta"]["damageGeneratedOnTargetScale"]
            if response["meta"].get("attackTypeHit") == "MELEE"
            else 0
        )
        response["meta"]["rangedDamageDealt"] = (
            response["meta"]["damageGeneratedOnTargetScale"]
            if response["meta"].get("attackTypeHit") == "RANGED"
            else 0
        )
        response["meta"]["mageDamageDealt"] = (
            response["meta"]["damageGeneratedOnTargetScale"]
            if response["meta"].get("attackTypeHit") == "MAGIC"
            else 0
        )
        response["meta"]["meleeDamageReceived"] = (
            response["meta"]["damageGeneratedOnPlayerScale"]
            if response["meta"].get("attackTypeReceived") == "MELEE"
            else 0
        )
        response["meta"]["rangedDamageReceived"] = (
            response["meta"]["damageGeneratedOnPlayerScale"]
            if response["meta"].get("attackTypeReceived") == "RANGED"
            else 0
        )
        response["meta"]["mageDamageReceived"] = (
            response["meta"]["damageGeneratedOnPlayerScale"]
            if response["meta"].get("attackTypeReceived") == "MAGIC"
            else 0
        )
        if self._reward_on_damage_generated:
            # Reward on when the hit is generated (exp drop), instead of when the hitsplat appears
            damage_received = response["meta"]["damageGeneratedOnPlayerScale"]
            damage_dealt = response["meta"]["damageGeneratedOnTargetScale"]
        else:
            damage_received = response["meta"]["damageReceived"]
            damage_dealt = response["meta"]["damageDealt"]

        if response["meta"].get("hitWithSmite"):
            response["meta"]["smite_damage"] = damage_dealt
        if response["meta"].get("targetAttackedWithSmite"):
            response["meta"]["smite_damage_received"] = damage_received

        info["rewards"] = {}
        reward = 0.0

        def _add_reward(
            add_reward: float, message_reason: str, tracker_key: str
        ) -> None:
            nonlocal reward, info
            if add_reward != 0:
                # Add some observability into the rewards earned
                info["rewards"][tracker_key] = add_reward
                reward += add_reward
                logger.debug(
                    f"{self._env_id} rewarded for '{message_reason}': {add_reward}"
                )

        _add_reward(
            self._default_reward.value(self._episode_context.trained_rollouts),
            "Default step reward",
            "default_step_reward",
        )

        _add_reward(
            self._custom_reward_fn.value(
                self._episode_context.trained_rollouts, **response["meta"]
            ),
            "Custom reward function",
            "custom_reward_fn",
        )

        damage_received_reward = self._damage_received_reward_scale.value(
            self._episode_context.trained_rollouts
        )
        damage_dealt_reward = self._damage_dealt_reward_scale.value(
            self._episode_context.trained_rollouts
        )

        _add_reward(damage_dealt_reward * damage_dealt, "Damage dealt", "damage_dealt")
        _add_reward(
            damage_received_reward * damage_received,
            "Damage received",
            "damage_received",
        )

        if "extraDamageDealtOnTargetScale" in response["meta"]:
            _add_reward(
                damage_dealt_reward * response["meta"]["extraDamageDealtOnTargetScale"],
                "Extra damage dealt",
                "extra_damage_dealt",
            )
        if "extraDamageDealtOnPlayerScale" in response["meta"]:
            _add_reward(
                damage_received_reward
                * response["meta"]["extraDamageDealtOnPlayerScale"],
                "Extra damage received",
                "extra_damage_received",
            )

        smite_damage_received_reward_multiplier = (
            self._smite_damage_received_reward_multiplier.value(
                self._episode_context.trained_rollouts
            )
        )
        smite_damage_dealt_reward_multiplier = (
            self._smite_damage_dealt_reward_multiplier.value(
                self._episode_context.trained_rollouts
            )
        )
        if response["meta"].get("hitWithSmite"):
            _add_reward(
                smite_damage_dealt_reward_multiplier
                * damage_dealt_reward
                * damage_dealt,
                "Smite damage dealt",
                "smite_damage_dealt",
            )
        if response["meta"].get("targetAttackedWithSmite"):
            _add_reward(
                smite_damage_received_reward_multiplier
                * damage_received_reward
                * damage_received,
                "Smite damage received",
                "smite_damage_received",
            )

        if self._reward_heals and "playerHealedScale" in response["meta"]:
            _add_reward(
                -1 * damage_received_reward * response["meta"]["playerHealedScale"],
                "Player healed",
                "player_healed",
            )

        if self._penalize_target_heals and "targetHealedScale" in response["meta"]:
            _add_reward(
                -1 * damage_dealt_reward * response["meta"]["targetHealedScale"],
                "Target healed",
                "target_healed",
            )

        if "eatAtFoodScale" in response["meta"]:
            _add_reward(
                -self._safe_penalty.value(
                    self._episode_context.trained_rollouts,
                    eat_at_food_scale=response["meta"]["eatAtFoodScale"],
                ),
                "Safe penalty",
                "safe_penalty",
            )

        if (
            "currentPrayerPercent" in response["meta"]
            and response["meta"]["currentPrayerPercent"] == 0
        ):
            _add_reward(
                self._no_prayer_tick_reward.value(
                    self._episode_context.trained_rollouts
                ),
                "No prayer",
                "no_prayer",
            )

        attack_speed_scale_factor = 5  # Scale around average weapon attack speed
        if "protectedPrayer" in response["meta"]:
            info["protected_prayer"] = response["meta"]["protectedPrayer"]
            attack_speed_scale = (
                response["meta"]["targetHitAttackSpeed"] / attack_speed_scale_factor
            )
            if response["meta"]["protectedPrayer"]:
                _add_reward(
                    self._protected_correct_prayer_reward.value(
                        self._episode_context.trained_rollouts
                    )
                    * attack_speed_scale,
                    "Protected correct prayer",
                    "protected_correct_prayer",
                )
            else:
                _add_reward(
                    self._protected_wrong_prayer_reward.value(
                        self._episode_context.trained_rollouts
                    )
                    * attack_speed_scale,
                    "Protected wrong prayer",
                    "protected_wrong_prayer",
                )

        if "hitOffPrayer" in response["meta"]:
            info["hit_off_prayer"] = response["meta"]["hitOffPrayer"]
            attack_speed_scale = (
                response["meta"]["playerHitAttackSpeed"] / attack_speed_scale_factor
            )
            if response["meta"]["hitOffPrayer"]:
                _add_reward(
                    self._attacked_correct_prayer_reward.value(
                        self._episode_context.trained_rollouts
                    )
                    * attack_speed_scale,
                    "Hit off prayer",
                    "hit_off_prayer",
                )
            else:
                _add_reward(
                    self._attacked_wrong_prayer_reward.value(
                        self._episode_context.trained_rollouts
                    )
                    * attack_speed_scale,
                    "Hit on prayer",
                    "hit_on_prayer",
                )

        if (
            "attackTypeHit" in response["meta"]
            and "currentTargetPrayerType" in self._episode_context.previous_meta
        ):
            hit_off_prior_prayer = (
                response["meta"]["attackTypeHit"]
                != self._episode_context.previous_meta["currentTargetPrayerType"]
            )
            info["hit_off_prior_prayer"] = hit_off_prior_prayer
            attack_speed_scale = (
                response["meta"]["playerHitAttackSpeed"] / attack_speed_scale_factor
            )
            if hit_off_prior_prayer:
                _add_reward(
                    attack_speed_scale
                    * self._attacked_previous_correct_prayer_reward.value(
                        self._episode_context.trained_rollouts
                    ),
                    "Hit off prior prayer",
                    "hit_off_prior_prayer",
                )
            else:
                _add_reward(
                    attack_speed_scale
                    * self._attacked_previous_wrong_prayer_reward.value(
                        self._episode_context.trained_rollouts
                    ),
                    "Hit on prior prayer",
                    "hit_on_prior_prayer",
                )

        if (
            response["meta"].get("attackTypeReceived")
            and "playerPrayerType" in response["meta"]
            and "targetAttackStyleType" in self._episode_context.previous_meta
        ):
            prayed_correct_prior_prayer = (
                response["meta"]["playerPrayerType"]
                == self._episode_context.previous_meta["targetAttackStyleType"]
            )
            info["protected_prior_prayer"] = prayed_correct_prior_prayer
            attack_speed_scale = (
                response["meta"]["targetHitAttackSpeed"] / attack_speed_scale_factor
            )
            if prayed_correct_prior_prayer:
                _add_reward(
                    self._protected_previous_correct_prayer_reward.value(
                        self._episode_context.trained_rollouts
                    )
                    * attack_speed_scale,
                    "Protected correct prior prayer",
                    "protected_correct_prior_prayer",
                )
            else:
                _add_reward(
                    self._protected_previous_wrong_prayer_reward.value(
                        self._episode_context.trained_rollouts
                    )
                    * attack_speed_scale,
                    "Protected wrong prior prayer",
                    "protected_wrong_prior_prayer",
                )

        if (
            "playerFrozenTicks" in response["meta"]
            and "playerFrozenTicks" in self._episode_context.previous_meta
            and response["meta"]["playerFrozenTicks"]
            > self._episode_context.previous_meta["playerFrozenTicks"]
        ):
            difference = (
                response["meta"]["playerFrozenTicks"]
                - self._episode_context.previous_meta["playerFrozenTicks"]
            )
            _add_reward(
                difference
                * self._player_frozen_tick_reward.value(
                    self._episode_context.trained_rollouts
                ),
                "Player frozen",
                "player_frozen",
            )

        if (
            "targetFrozenTicks" in response["meta"]
            and "targetFrozenTicks" in self._episode_context.previous_meta
            and response["meta"]["targetFrozenTicks"]
            > self._episode_context.previous_meta["targetFrozenTicks"]
        ):
            difference = (
                response["meta"]["targetFrozenTicks"]
                - self._episode_context.previous_meta["targetFrozenTicks"]
            )
            _add_reward(
                difference
                * self._target_frozen_tick_reward.value(
                    self._episode_context.trained_rollouts
                ),
                "Target frozen",
                "target_frozen",
            )

        if "attackLevelScale" in response["meta"]:
            _add_reward(
                (response["meta"]["attackLevelScale"] - 1)
                * self._attack_level_scale_reward.value(
                    self._episode_context.trained_rollouts
                ),
                "Attack level scale",
                "attack_level_scale",
            )

        if "strengthLevelScale" in response["meta"]:
            _add_reward(
                (response["meta"]["strengthLevelScale"] - 1)
                * self._strength_level_scale_reward.value(
                    self._episode_context.trained_rollouts
                ),
                "Strength level scale",
                "strength_level_scale",
            )

            if (
                response["meta"].get("attackTypeHit") == "MELEE"
                and response["meta"]["strengthLevelScale"] > 1
            ):
                boost_reward = (
                    response["meta"]["strengthLevelScale"] - 1
                ) * self._reward_on_hit_with_boost_scale.value(
                    self._episode_context.trained_rollouts,
                    level=response["meta"]["strengthLevelScale"],
                )
                _add_reward(boost_reward, "Hit melee with boost", "melee_boost_hit")

        if "defenceLevelScale" in response["meta"]:
            _add_reward(
                (response["meta"]["defenceLevelScale"] - 1)
                * self._defense_level_scale_reward.value(
                    self._episode_context.trained_rollouts
                ),
                "Defence level scale",
                "defence_level_scale",
            )

        if "rangedLevelScale" in response["meta"]:
            _add_reward(
                (response["meta"]["rangedLevelScale"] - 1)
                * self._ranged_level_scale_reward.value(
                    self._episode_context.trained_rollouts
                ),
                "Ranged level scale",
                "ranged_level_scale",
            )

            if (
                response["meta"].get("attackTypeHit") == "RANGED"
                and response["meta"]["rangedLevelScale"] > 1
            ):
                boost_reward = (
                    response["meta"]["rangedLevelScale"] - 1
                ) * self._reward_on_hit_with_boost_scale.value(
                    self._episode_context.trained_rollouts,
                    level=response["meta"]["rangedLevelScale"],
                )
                _add_reward(boost_reward, "Hit ranged with boost", "ranged_boost_hit")

        if "magicLevelScale" in response["meta"]:
            _add_reward(
                (response["meta"]["magicLevelScale"] - 1)
                * self._magic_level_scale_reward.value(
                    self._episode_context.trained_rollouts
                ),
                "Magic level scale",
                "magic_level_scale",
            )

            if (
                response["meta"].get("attackTypeHit") == "MAGIC"
                and response["meta"]["magicLevelScale"] > 1
            ):
                boost_reward = (
                    response["meta"]["magicLevelScale"] - 1
                ) * self._reward_on_hit_with_boost_scale.value(
                    self._episode_context.trained_rollouts,
                    level=response["meta"]["magicLevelScale"],
                )
                _add_reward(boost_reward, "Hit magic with boost", "magic_boost_hit")

        if self._penalize_wasted_food:
            # Penalize based on the expected damage for wasting the food
            wasted_food_multiplier = self._player_wasted_food_multiplier.value(
                self._episode_context.trained_rollouts,
                scale=response["meta"].get("wastedFoodScale", 0)
                + response["meta"].get("wastedBrewScale", 0),
            )
            if "wastedFoodScale" in response["meta"]:
                _add_reward(
                    damage_received_reward
                    * response["meta"]["wastedFoodScale"]
                    * wasted_food_multiplier,
                    "Wasted food",
                    "wasted_food",
                )
            if "wastedBrewScale" in response["meta"]:
                _add_reward(
                    damage_received_reward
                    * response["meta"]["wastedBrewScale"]
                    * wasted_food_multiplier,
                    "Wasted brews",
                    "wasted_brews",
                )

        if "terminalState" in response:
            info["terminal_state"] = response["terminalState"]
            if response["terminalState"] == "WON":
                _add_reward(
                    self._win_reward.value(self._episode_context.trained_rollouts),
                    "Won fight",
                    "win",
                )
                info["target_food_on_death"] = response["meta"][
                    "targetRemainingFoodScale"
                ]
                info["target_brew_on_death"] = response["meta"][
                    "targetRemainingBrewScale"
                ]
                if self._reward_target_food_on_death:
                    # Reward based on the expected damage if the food was used
                    food_multiplier = self._player_died_with_food_multiplier.value(
                        self._episode_context.trained_rollouts,
                        scale=response["meta"]["targetRemainingFoodScale"]
                        + response["meta"]["targetRemainingBrewScale"],
                    )
                    _add_reward(
                        damage_dealt_reward
                        * response["meta"]["targetRemainingFoodScale"]
                        * food_multiplier,
                        "Target killed before food finished",
                        "target_food_on_death",
                    )
                    _add_reward(
                        damage_dealt_reward
                        * response["meta"]["targetRemainingBrewScale"]
                        * food_multiplier,
                        "Target killed before brews finished",
                        "target_brews_on_death",
                    )
            elif response["terminalState"] == "LOST":
                _add_reward(
                    self._lose_reward.value(self._episode_context.trained_rollouts),
                    "Lost fight",
                    "lost",
                )
                info["player_food_on_death"] = response["meta"]["remainingFoodScale"]
                info["player_brew_on_death"] = response["meta"]["remainingBrewScale"]
                if self._penalize_food_on_death:
                    # Penalize based on the expected damage if the food was used
                    food_multiplier = self._player_died_with_food_multiplier.value(
                        self._episode_context.trained_rollouts,
                        scale=response["meta"]["remainingFoodScale"]
                        + response["meta"]["remainingBrewScale"],
                    )
                    _add_reward(
                        damage_received_reward
                        * response["meta"]["remainingFoodScale"]
                        * food_multiplier,
                        "Player killed before food finished",
                        "player_food_on_death",
                    )
                    _add_reward(
                        damage_received_reward
                        * response["meta"]["remainingBrewScale"]
                        * food_multiplier,
                        "Player killed before brews finished",
                        "player_brews_on_death",
                    )
            elif response["terminalState"] == "TIED":
                _add_reward(
                    self._tie_reward.value(self._episode_context.trained_rollouts),
                    "Tied fight",
                    "tied",
                )
            elif response["terminalState"] == "TARGET_LOST":
                raise _TruncateException("Lost target", "TARGET_LOST")
            else:
                raise ValueError(f"Unknown terminal state: {response['terminalState']}")

        return reward

    async def __send_step(self, body: dict[str, Any]) -> dict[str, Any]:
        start_time = time.time()
        logger.debug(f"Sending step request to remote agent {self._env_id}: {body}")
        res_json = await self._remote_env_connector.send(action="step", body=body)
        logger.debug(
            f"Received response from remote agent {self._env_id} in {time.time() - start_time} seconds"
        )
        self.__transform_response(res_json)
        return cast(dict[str, Any], res_json)

    def __transform_response(self, res_json: dict[str, Any]) -> None:
        res_json["obs"] = np.array(res_json["obs"], dtype=np.float32)
        flattened_action_masks = list(
            itertools.chain.from_iterable(res_json["actionMasks"])
        )
        res_json["actionMasks"] = np.array(flattened_action_masks, dtype=bool)
        if "meta" in res_json and "targetObs" in res_json["meta"]:
            res_json["meta"]["targetObs"] = np.array(
                res_json["meta"]["targetObs"], dtype=np.float32
            )

    def __process_obs(
        self,
        obs: NDArray[np.float32],
        target_obs: NDArray[np.float32] | None,
    ) -> NDArray[np.float32]:
        assert self._episode_context is not None
        if self._noise_generator is not None:
            self._noise_generator.add_noise(obs, self._episode_context.trained_rollouts)
        if self._include_target_obs_in_critic:
            if self._training:
                assert target_obs is not None, "No target obs provided"
                obs = np.concatenate(
                    [obs, target_obs[..., self._partially_observable_indices]],
                    dtype=obs.dtype,
                )
            else:
                obs = np.concatenate(
                    [obs, np.zeros(len(self._partially_observable_indices))],
                    dtype=obs.dtype,
                )

        # Stack the frames such that the most recent frame is first
        if self._episode_context.steps == 0:
            # Last frame index is the highest, so we need to track up to that many frames
            history_size = self._stack_frames[-1] + 1
            self._episode_context.frame_history = deque(
                [np.zeros_like(obs) for _ in range(history_size)],
                maxlen=history_size,
            )
        assert self._episode_context.frame_history is not None
        self._episode_context.frame_history.appendleft(obs)
        assert len(self._episode_context.frame_history) == self._stack_frames[-1] + 1
        obs = np.stack(self._episode_context.frame_history)[self._stack_frames]

        self._episode_context.last_obs = obs
        return obs

    def __log_step_start(self, action: NDArray[np.int32]) -> dict[str, Any]:
        assert self._episode_context is not None
        current_time = time.time()
        time_since_last_start = current_time - self._episode_context.last_tick_start
        self._episode_context.last_tick_start = current_time
        time_between = (
            self._episode_context.last_tick_start - self._episode_context.last_tick_end
        )
        info = {}
        if self._episode_context.steps > 0:
            # Skip the first step since we won't have a previous time
            info["between_step_time"] = time_between
        logger.debug(
            f"Stepping environment {self._env_id}: {action}."
            f" Time since last tick: {time_since_last_start}."
            f" Current step: {self._episode_context.steps + 1}."
            f" Total reward: {self._episode_context.total_reward}."
        )
        if 0 < self._desync_tolerance_seconds <= time_since_last_start:
            # De-synced
            raise _TruncateException(
                f"Took too long to process tick. Likely de-synced. "
                f"Time since last tick: {time_since_last_start}, "
                f"env: {self._env_id}, "
                f"time between ticks: {time_between}",
                "TIME_DESYNC",
            )
        return info

    def __log_step_end(self) -> dict[str, Any]:
        assert self._episode_context is not None
        info = {}
        current_time = time.time()
        cycle_time = current_time - self._episode_context.last_tick_end
        self._episode_context.last_tick_end = time.time()
        time_to_process = (
            self._episode_context.last_tick_end - self._episode_context.last_tick_start
        )
        info["process_step_time"] = time_to_process
        logger.debug(
            f"Stepped environment {self._env_id}."
            f" Time to process: {time_to_process}."
            f" Time since last step end: {cycle_time}."
        )
        return info

    def __str__(self) -> str:
        return f"{self._env_name}: {self._env_id} vs. {self._target}"
