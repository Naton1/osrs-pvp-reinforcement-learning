from asyncio import AbstractEventLoop
from collections import defaultdict
from typing import Any

import numpy as np

from pvp_ml.callback.additional_env_runner_callback import AdditionalEnvRunnerCallback
from pvp_ml.ppo.ppo import Meta
from pvp_ml.util.files import get_most_recent_model
from pvp_ml.util.remote_processor.remote_processor import RemoteProcessor
from pvp_ml.util.schedule import Schedule


class EvalCallback(AdditionalEnvRunnerCallback):
    WIN_PERCENT_METRIC = "eval/win_percentage"
    LOSE_PERCENT_METRIC = "eval/lose_percentage"
    TIE_PERCENT_METRIC = "eval/tie_percentage"
    DESYNC_PERCENT_METRIC = "eval/desync_percentage"
    TERMINAL_STATE_COUNT_METRIC = "eval/terminal_state_count"
    MEAN_EP_REW_METRIC = "eval/mean_episode_reward"
    AVG_EP_LEN_METRIC = "eval/average_episode_length"

    def __init__(
        self,
        num_eval_envs: Schedule[int],
        experiment_name: str,
        eval_deterministic_percent: Schedule[float],
        loop: AbstractEventLoop,
        remote_processor: RemoteProcessor,
        env_kwargs: dict[str, Any],
    ):
        self._eval_targets: dict[str, str] = {}
        super(EvalCallback, self).__init__(
            deterministic_percent=eval_deterministic_percent,
            env_kwargs=env_kwargs,
            loop=loop,
            remote_processor=remote_processor,
            fight_mappings=self._eval_targets,
            use_vec_env=True,
            tracker_name="eval-envs",
            experiment_name=experiment_name,
        )
        self._num_eval_envs = num_eval_envs
        self._target_model: str | None = None

    def on_rollout_start(self) -> None:
        assert self._ppo is not None
        self._target_model = get_most_recent_model(self._experiment_name)
        self._eval_targets.clear()
        for i in range(self._num_eval_envs.value(self._ppo.meta.trained_rollouts)):
            self._eval_targets[f"E {i}"] = "baseline"
        super().on_rollout_start()

    def _select_target_model(self, env_id: str | None) -> str:
        assert self._target_model is not None
        return self._target_model

    def _process_episodes(self, episodes: list[tuple[float, dict[str, Any]]]) -> None:
        assert self._ppo is not None
        if self._summary_writer is not None:
            wins = 0
            loses = 0
            ties = 0
            desyncs = 0
            total_reward = 0.0
            eval_count = 0
            episode_lengths = []
            for reward, info in episodes:
                eval_count += 1
                total_reward += reward
                episode_lengths.append(info["episode"]["length"])
                if info["terminal_state"] == "WON":
                    wins += 1
                elif info["terminal_state"] == "LOST":
                    loses += 1
                elif info["terminal_state"] == "TIED":
                    ties += 1
                elif info["terminal_state"] == "DESYNC":
                    desyncs += 1
                else:
                    raise ValueError(
                        f"Unknown terminal state: {info['terminal_state']}"
                    )
            if eval_count > 0:
                metrics = [
                    (self.WIN_PERCENT_METRIC, wins / eval_count),
                    (self.LOSE_PERCENT_METRIC, loses / eval_count),
                    (self.TIE_PERCENT_METRIC, ties / eval_count),
                    (self.DESYNC_PERCENT_METRIC, desyncs / eval_count),
                    (self.MEAN_EP_REW_METRIC, total_reward / eval_count),
                    (self.TERMINAL_STATE_COUNT_METRIC, eval_count),
                    (self.AVG_EP_LEN_METRIC, np.mean(episode_lengths)),
                ]
                for key, value in metrics:
                    self._summary_writer.add_scalar(
                        key, value, self._ppo.meta.trained_steps
                    )
        super()._process_episodes(episodes)

    def on_distributed_rollout_collection(self, metas: list[Meta]) -> None:
        if self._summary_writer is None:
            return
        assert self._ppo is not None

        # Begin accumulation
        accumulator = defaultdict(list)

        # Collect stats - this expects TensorBoard scalars to be propagated to stats
        for meta in metas:
            meta_stats = meta.custom_data.get("stats", {})
            terminal_state_count = meta_stats.get("eval/terminal_state_count", 0)
            for key, value in meta_stats.items():
                if key.startswith("eval/"):
                    accumulator[key].append((value, terminal_state_count))

        for key, values in accumulator.items():
            # Weight each metric based on the number of terminal states associated with it
            total_terminal_states = sum(weight for _, weight in values)

            if key in [
                self.WIN_PERCENT_METRIC,
                self.LOSE_PERCENT_METRIC,
                self.TIE_PERCENT_METRIC,
                self.DESYNC_PERCENT_METRIC,
                self.MEAN_EP_REW_METRIC,
                self.AVG_EP_LEN_METRIC,
            ]:
                weighted_sum = sum(val * weight for val, weight in values)
                value = weighted_sum / total_terminal_states
            elif key == self.TERMINAL_STATE_COUNT_METRIC:
                value = total_terminal_states
            else:
                raise ValueError(f"Unknown key for accumulation: {key}")

            self._summary_writer.add_scalar(key, value, self._ppo.meta.trained_steps)
