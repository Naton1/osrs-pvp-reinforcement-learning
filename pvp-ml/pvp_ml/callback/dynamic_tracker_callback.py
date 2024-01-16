import abc
import logging
import time
from abc import ABC
from dataclasses import dataclass
from typing import Any, cast

import numpy as np

from pvp_ml.callback.callback import Callback
from pvp_ml.ppo.buffer import Buffer

logger = logging.getLogger(__name__)


def _get_nested_value(dct: dict[str, Any], keys: list[str]) -> Any | None:
    current = dct
    for key in keys:
        if key in current:
            current = current[key]
        else:
            return None
    return current


class Accumulator(ABC):
    @abc.abstractmethod
    def accumulate(self, values: list[Any]) -> float | int | None:
        pass


@dataclass(frozen=True)
class DynamicMetric:
    metric_name: str
    accumulator: Accumulator
    field_path: list[str]


class AverageAccumulator(Accumulator):
    def __init__(self) -> None:
        self._current_sum: float = 0.0
        self._current_count: int = 0

    def accumulate(self, values: list[Any]) -> float | int | None:
        self._current_sum += sum(values)
        self._current_count += len(values)
        if self._current_count == 0:
            return None
        return self._current_sum / self._current_count


class SumAccumulator(Accumulator):
    def __init__(self) -> None:
        self._current_sum: float = 0.0

    def accumulate(self, values: list[Any]) -> float | int | None:
        self._current_sum += sum(values)
        return self._current_sum


class StdAccumulator(Accumulator):
    def __init__(self) -> None:
        self._current_values: list[Any] = []

    def accumulate(self, values: list[Any]) -> float | None:
        self._current_values.extend(values)
        if not self._current_values:
            return None
        return cast(float, np.std(self._current_values).item())


class MinAccumulator(Accumulator):
    def __init__(self) -> None:
        self._min: Any = None

    def accumulate(self, values: list[float | int]) -> float | int | None:
        min_of_list = min(values)
        if self._min is None:
            self._min = min_of_list
        else:
            self._min = min(self._min, min_of_list)
        if self._min is None:
            return None
        return self._min


class MaxAccumulator(Accumulator):
    def __init__(self) -> None:
        self._max: Any = None

    def accumulate(self, values: list[float | int]) -> float | int | None:
        max_of_list = max(values)
        if self._max is None:
            self._max = max_of_list
        else:
            self._max = max(self._max, max_of_list)
        if self._max is None:
            return None
        return self._max


class CountAccumulator(Accumulator):
    def __init__(self) -> None:
        self._current_count: int = 0

    def accumulate(self, values: list[Any]) -> float | int | None:
        self._current_count += len(values)
        if self._current_count == 0:
            return None
        return self._current_count


class CountValueEqualsAccumulator(Accumulator):
    def __init__(self, value: Any):
        self._current_count: int = 0
        self._value = value

    def accumulate(self, values: list[Any]) -> float | int | None:
        self._current_count += sum(1 for value in values if value == self._value)
        if self._current_count == 0:
            return None
        return self._current_count


class CountValueNotEqualsAccumulator(Accumulator):
    def __init__(self, value: Any):
        self._current_count: int = 0
        self._value = value

    def accumulate(self, values: list[Any]) -> float | int | None:
        self._current_count += sum(1 for value in values if value != self._value)
        if self._current_count == 0:
            return None
        return self._current_count


class AverageValueEqualsAccumulator(Accumulator):
    def __init__(self, value: Any, raise_if_percentage_over: float | None = None):
        self._current_count: int = 0
        self._current_total_count: int = 0
        self._value = value
        self._raise_if_percentage_over = raise_if_percentage_over

    def accumulate(self, values: list[Any]) -> float | int | None:
        self._current_total_count += len(values)
        self._current_count += len([value for value in values if value == self._value])
        if self._current_total_count == 0:
            return None
        percentage = self._current_count / self._current_total_count
        if (
            self._raise_if_percentage_over is not None
            and percentage >= self._raise_if_percentage_over
        ):
            # Terminate training with failure if something exceeds this
            raise ValueError(
                f"Percent of {self._value} exceeded {self._raise_if_percentage_over} "
                f"({percentage} - {self._current_count}/{self._current_total_count})"
            )
        return percentage


class DynamicTrackerCallback(Callback):
    def on_rollout_end(self, buffer: Buffer) -> None:
        assert (
            self._summary_writer is not None
        ), "Summary writer is required to track metrics"
        assert self._ppo is not None

        infos = buffer.infos.flatten()
        start_time = time.time()

        tracked_metrics = 0

        metrics = self.__create_metrics()
        logger.info(
            f"Checking {len(metrics)} metrics to process over {len(infos)} step infos"
        )

        for metric in metrics:
            extracted_fields = [
                extracted_field
                for extracted_field in [
                    _get_nested_value(info, metric.field_path) for info in infos
                ]
                if extracted_field is not None
            ]
            if not extracted_fields:
                continue
            value = metric.accumulator.accumulate(extracted_fields)
            if value is not None:
                tracked_metrics += 1
                self._summary_writer.add_scalar(
                    metric.metric_name, value, self._ppo.meta.trained_steps
                )

        tracking_length = time.time() - start_time
        logger.info(
            f"Processed metrics in {tracking_length} seconds: {len(infos)} steps,"
            f" {len(metrics)} total metrics, {tracked_metrics} metrics tracked"
        )

    def __create_metrics(self) -> list[DynamicMetric]:
        return [
            DynamicMetric(
                metric_name="combat/damage/average_dealt",
                accumulator=AverageAccumulator(),
                field_path=["meta", "damageDealt"],
            ),
            DynamicMetric(
                metric_name="combat/damage/average_received",
                accumulator=AverageAccumulator(),
                field_path=["meta", "damageReceived"],
            ),
            DynamicMetric(
                metric_name="combat/damage/generated_average_dealt",
                accumulator=AverageAccumulator(),
                field_path=["meta", "damageGeneratedOnTargetScale"],
            ),
            DynamicMetric(
                metric_name="combat/damage/generated_average_received",
                accumulator=AverageAccumulator(),
                field_path=["meta", "damageGeneratedOnPlayerScale"],
            ),
            DynamicMetric(
                metric_name="combat/damage/extra_damage_dealt",
                accumulator=AverageAccumulator(),
                field_path=["meta", "extraDamageDealtOnTargetScale"],
            ),
            DynamicMetric(
                metric_name="combat/damage/extra_damage_received",
                accumulator=AverageAccumulator(),
                field_path=["meta", "extraDamageDealtOnPlayerScale"],
            ),
            DynamicMetric(
                metric_name="combat/eating/eat_from_average",
                accumulator=AverageAccumulator(),
                field_path=["meta", "eatAtFoodScale"],
            ),
            DynamicMetric(
                metric_name="combat/eating/eat_from_std",
                accumulator=StdAccumulator(),
                field_path=["meta", "eatAtFoodScale"],
            ),
            DynamicMetric(
                metric_name="combat/eating/eat_to_average",
                accumulator=AverageAccumulator(),
                field_path=["meta", "eatToFoodScale"],
            ),
            DynamicMetric(
                metric_name="combat/eating/eat_to_std",
                accumulator=StdAccumulator(),
                field_path=["meta", "eatToFoodScale"],
            ),
            DynamicMetric(
                metric_name="combat/eating/brew_from_average",
                accumulator=AverageAccumulator(),
                field_path=["meta", "eatAtBrewScale"],
            ),
            DynamicMetric(
                metric_name="combat/eating/brew_to_average",
                accumulator=AverageAccumulator(),
                field_path=["meta", "eatToBrewScale"],
            ),
            DynamicMetric(
                metric_name="combat/eating/average_wasted_food_scale",
                accumulator=AverageAccumulator(),
                field_path=["meta", "wastedFoodScale"],
            ),
            DynamicMetric(
                metric_name="combat/eating/total_wasted_food_scale",
                accumulator=SumAccumulator(),
                field_path=["meta", "wastedFoodScale"],
            ),
            DynamicMetric(
                metric_name="combat/eating/count_wasted_food",
                accumulator=CountValueNotEqualsAccumulator(value=0.0),
                field_path=["meta", "wastedFoodScale"],
            ),
            DynamicMetric(
                metric_name="combat/eating/average_wasted_brew_scale",
                accumulator=AverageAccumulator(),
                field_path=["meta", "wastedBrewScale"],
            ),
            DynamicMetric(
                metric_name="combat/eating/total_wasted_brew_scale",
                accumulator=SumAccumulator(),
                field_path=["meta", "wastedBrewScale"],
            ),
            DynamicMetric(
                metric_name="combat/eating/count_wasted_brew",
                accumulator=CountValueNotEqualsAccumulator(value=0.0),
                field_path=["meta", "wastedBrewScale"],
            ),
            DynamicMetric(
                metric_name="combat/eating/mean_player_food_on_death_scale",
                accumulator=AverageAccumulator(),
                field_path=["player_food_on_death"],
            ),
            DynamicMetric(
                metric_name="combat/eating/times_player_has_food_on_death",
                accumulator=CountValueNotEqualsAccumulator(value=0.0),
                field_path=["player_food_on_death"],
            ),
            DynamicMetric(
                metric_name="combat/eating/mean_target_food_on_death_scale",
                accumulator=AverageAccumulator(),
                field_path=["target_food_on_death"],
            ),
            DynamicMetric(
                metric_name="combat/eating/times_target_has_food_on_death",
                accumulator=CountValueNotEqualsAccumulator(value=0.0),
                field_path=["target_food_on_death"],
            ),
            DynamicMetric(
                metric_name="combat/eating/mean_player_brew_on_death_scale",
                accumulator=AverageAccumulator(),
                field_path=["player_brew_on_death"],
            ),
            DynamicMetric(
                metric_name="combat/eating/times_player_has_brew_on_death",
                accumulator=CountValueNotEqualsAccumulator(value=0.0),
                field_path=["player_brew_on_death"],
            ),
            DynamicMetric(
                metric_name="combat/eating/mean_target_brew_on_death_scale",
                accumulator=AverageAccumulator(),
                field_path=["target_brew_on_death"],
            ),
            DynamicMetric(
                metric_name="combat/eating/times_target_has_brew_on_death",
                accumulator=CountValueNotEqualsAccumulator(value=0.0),
                field_path=["target_brew_on_death"],
            ),
            DynamicMetric(
                metric_name="combat/eating/average_eaten_food_scale",
                accumulator=AverageAccumulator(),
                field_path=["meta", "eatenFoodScale"],
            ),
            DynamicMetric(
                metric_name="combat/eating/count_food_eaten",
                accumulator=CountValueNotEqualsAccumulator(value=0.0),
                field_path=["meta", "eatenFoodScale"],
            ),
            DynamicMetric(
                metric_name="terminal/win_percentage",
                accumulator=AverageValueEqualsAccumulator(value="WON"),
                field_path=["terminal_state"],
            ),
            DynamicMetric(
                metric_name="terminal/lose_percentage",
                accumulator=AverageValueEqualsAccumulator(value="LOST"),
                field_path=["terminal_state"],
            ),
            DynamicMetric(
                metric_name="terminal/tie_percentage",
                accumulator=AverageValueEqualsAccumulator(value="TIED"),
                field_path=["terminal_state"],
            ),
            DynamicMetric(
                metric_name="terminal/desync_percentage",
                accumulator=AverageValueEqualsAccumulator(
                    value="DESYNC", raise_if_percentage_over=0.05
                ),
                field_path=["terminal_state"],
            ),
            DynamicMetric(
                metric_name="terminal/terminal_state_count",
                accumulator=CountAccumulator(),
                field_path=["terminal_state"],
            ),
            DynamicMetric(
                metric_name="times/time_between_steps/min",
                accumulator=AverageAccumulator(),
                field_path=["episode", "min", "time_between_step"],
            ),
            DynamicMetric(
                metric_name="times/time_between_steps/max",
                accumulator=AverageAccumulator(),
                field_path=["episode", "max", "time_between_step"],
            ),
            DynamicMetric(
                metric_name="times/time_between_steps/mean",
                accumulator=AverageAccumulator(),
                field_path=["episode", "mean", "time_between_step"],
            ),
            DynamicMetric(
                metric_name="times/time_between_steps/std",
                accumulator=AverageAccumulator(),
                field_path=["episode", "std", "time_between_stes"],
            ),
            DynamicMetric(
                metric_name="times/process_step_times/min",
                accumulator=AverageAccumulator(),
                field_path=["episode", "min", "process_step_time"],
            ),
            DynamicMetric(
                metric_name="times/process_step_times/max",
                accumulator=AverageAccumulator(),
                field_path=["episode", "max", "process_step_time"],
            ),
            DynamicMetric(
                metric_name="times/process_step_times/mean",
                accumulator=AverageAccumulator(),
                field_path=["episode", "mean", "process_step_time"],
            ),
            DynamicMetric(
                metric_name="times/process_step_times/std",
                accumulator=AverageAccumulator(),
                field_path=["episode", "std", "process_step_time"],
            ),
            DynamicMetric(
                metric_name="times/average_desync",
                accumulator=AverageAccumulator(),
                field_path=["episode", "desync_ticks"],
            ),
            DynamicMetric(
                metric_name="combat/prayer/protected_prayer_correct",
                accumulator=AverageValueEqualsAccumulator(value=True),
                field_path=["protected_prayer"],
            ),
            DynamicMetric(
                metric_name="combat/prayer/protected_prayer_wrong",
                accumulator=AverageValueEqualsAccumulator(value=False),
                field_path=["protected_prayer"],
            ),
            DynamicMetric(
                metric_name="combat/prayer/hit_off_prayer",
                accumulator=AverageValueEqualsAccumulator(value=True),
                field_path=["hit_off_prayer"],
            ),
            DynamicMetric(
                metric_name="combat/prayer/hit_on_prayer",
                accumulator=AverageValueEqualsAccumulator(value=False),
                field_path=["hit_off_prayer"],
            ),
            DynamicMetric(
                metric_name="combat/prayer/protected_prior_prayer_correct",
                accumulator=AverageValueEqualsAccumulator(value=True),
                field_path=["protected_prior_prayer"],
            ),
            DynamicMetric(
                metric_name="combat/prayer/protected_prior_prayer_wrong",
                accumulator=AverageValueEqualsAccumulator(value=False),
                field_path=["protected_prior_prayer"],
            ),
            DynamicMetric(
                metric_name="combat/prayer/hit_off_prior_prayer",
                accumulator=AverageValueEqualsAccumulator(value=True),
                field_path=["hit_off_prior_prayer"],
            ),
            DynamicMetric(
                metric_name="combat/prayer/hit_on_prior_prayer",
                accumulator=AverageValueEqualsAccumulator(value=False),
                field_path=["hit_off_prior_prayer"],
            ),
            DynamicMetric(
                metric_name="combat/eating/remaining_food_count",
                accumulator=AverageAccumulator(),
                field_path=["episode", "remaining_food"],
            ),
            DynamicMetric(
                metric_name="combat/stats/mean_hitpoints",
                accumulator=AverageAccumulator(),
                field_path=["meta", "currentHealthPercent"],
            ),
            DynamicMetric(
                metric_name="combat/stats/mean_prayer",
                accumulator=AverageAccumulator(),
                field_path=["meta", "currentPrayerPercent"],
            ),
            DynamicMetric(
                metric_name="combat/stats/mean_attack",
                accumulator=AverageAccumulator(),
                field_path=["meta", "attackLevelScale"],
            ),
            DynamicMetric(
                metric_name="combat/stats/mean_strength",
                accumulator=AverageAccumulator(),
                field_path=["meta", "strengthLevelScale"],
            ),
            DynamicMetric(
                metric_name="combat/stats/mean_defence",
                accumulator=AverageAccumulator(),
                field_path=["meta", "defenceLevelScale"],
            ),
            DynamicMetric(
                metric_name="combat/stats/mean_ranged",
                accumulator=AverageAccumulator(),
                field_path=["meta", "rangedLevelScale"],
            ),
            DynamicMetric(
                metric_name="combat/stats/mean_magic",
                accumulator=AverageAccumulator(),
                field_path=["meta", "magicLevelScale"],
            ),
            DynamicMetric(
                metric_name="combat/stats/target_mean_hitpoints",
                accumulator=AverageAccumulator(),
                field_path=["meta", "currentTargetHealthPercent"],
            ),
            DynamicMetric(
                metric_name="terminal/truncate/time_desync",
                accumulator=CountValueEqualsAccumulator(value="TIME_DESYNC"),
                field_path=["desync_reason"],
            ),
            DynamicMetric(
                metric_name="terminal/truncate/tick_desync",
                accumulator=CountValueEqualsAccumulator(value="TICK_DESYNC"),
                field_path=["desync_reason"],
            ),
            DynamicMetric(
                metric_name="terminal/truncate/target_lost",
                accumulator=CountValueEqualsAccumulator(value="TARGET_LOST"),
                field_path=["desync_reason"],
            ),
            DynamicMetric(
                metric_name="combat/attack/hit_ranged",
                accumulator=AverageValueEqualsAccumulator(value="RANGED"),
                field_path=["meta", "attackTypeHit"],
            ),
            DynamicMetric(
                metric_name="combat/attack/hit_magic",
                accumulator=AverageValueEqualsAccumulator(value="MAGIC"),
                field_path=["meta", "attackTypeHit"],
            ),
            DynamicMetric(
                metric_name="combat/attack/hit_melee",
                accumulator=AverageValueEqualsAccumulator(value="MELEE"),
                field_path=["meta", "attackTypeHit"],
            ),
            DynamicMetric(
                metric_name="combat/attack/received_range",
                accumulator=AverageValueEqualsAccumulator(value="RANGED"),
                field_path=["meta", "attackTypeReceived"],
            ),
            DynamicMetric(
                metric_name="combat/attack/received_magic",
                accumulator=AverageValueEqualsAccumulator(value="MAGIC"),
                field_path=["meta", "attackTypeReceived"],
            ),
            DynamicMetric(
                metric_name="combat/attack/received_melee",
                accumulator=AverageValueEqualsAccumulator(value="MELEE"),
                field_path=["meta", "attackTypeReceived"],
            ),
            DynamicMetric(
                metric_name="combat/attack/melee_damage_dealt",
                accumulator=AverageAccumulator(),
                field_path=["meta", "meleeDamageDealt"],
            ),
            DynamicMetric(
                metric_name="combat/attack/ranged_damage_dealt",
                accumulator=AverageAccumulator(),
                field_path=["meta", "rangedDamageDealt"],
            ),
            DynamicMetric(
                metric_name="combat/attack/magic_damage_dealt",
                accumulator=AverageAccumulator(),
                field_path=["meta", "mageDamageDealt"],
            ),
            DynamicMetric(
                metric_name="combat/attack/melee_damage_received",
                accumulator=AverageAccumulator(),
                field_path=["meta", "meleeDamageReceived"],
            ),
            DynamicMetric(
                metric_name="combat/attack/ranged_damage_received",
                accumulator=AverageAccumulator(),
                field_path=["meta", "rangedDamageReceived"],
            ),
            DynamicMetric(
                metric_name="combat/attack/magic_damage_received",
                accumulator=AverageAccumulator(),
                field_path=["meta", "mageDamageReceived"],
            ),
            DynamicMetric(
                metric_name="combat/distance",
                accumulator=AverageAccumulator(),
                field_path=["meta", "distance"],
            ),
            DynamicMetric(
                metric_name="combat/prayer/player_pray_ranged",
                accumulator=AverageValueEqualsAccumulator(value="RANGED"),
                field_path=["meta", "playerPrayerType"],
            ),
            DynamicMetric(
                metric_name="combat/prayer/player_pray_magic",
                accumulator=AverageValueEqualsAccumulator(value="MAGIC"),
                field_path=["meta", "playerPrayerType"],
            ),
            DynamicMetric(
                metric_name="combat/prayer/player_pray_melee",
                accumulator=AverageValueEqualsAccumulator(value="MELEE"),
                field_path=["meta", "playerPrayerType"],
            ),
            DynamicMetric(
                metric_name="combat/prayer/target_pray_ranged",
                accumulator=AverageValueEqualsAccumulator(value="RANGED"),
                field_path=["meta", "targetPrayerType"],
            ),
            DynamicMetric(
                metric_name="combat/prayer/target_pray_magic",
                accumulator=AverageValueEqualsAccumulator(value="MAGIC"),
                field_path=["meta", "targetPrayerType"],
            ),
            DynamicMetric(
                metric_name="combat/prayer/target_pray_melee",
                accumulator=AverageValueEqualsAccumulator(value="MELEE"),
                field_path=["meta", "targetPrayerType"],
            ),
            DynamicMetric(
                metric_name="combat/frozen/player_frozen",
                accumulator=AverageAccumulator(),
                field_path=["meta", "playerFrozenTicks"],
            ),
            DynamicMetric(
                metric_name="combat/frozen/target_frozen",
                accumulator=AverageAccumulator(),
                field_path=["meta", "targetFrozenTicks"],
            ),
            DynamicMetric(
                metric_name="combat/prayer/hit_with_smite",
                accumulator=AverageValueEqualsAccumulator(value=True),
                field_path=["meta", "hitWithSmite"],
            ),
            DynamicMetric(
                metric_name="combat/prayer/smite_damage_dealt",
                accumulator=AverageAccumulator(),
                field_path=["meta", "smite_damage"],
            ),
            DynamicMetric(
                metric_name="combat/player_healed_scale",
                accumulator=AverageAccumulator(),
                field_path=["meta", "playerHealedScale"],
            ),
            DynamicMetric(
                metric_name="combat/target_healed_scale",
                accumulator=AverageAccumulator(),
                field_path=["meta", "targetHealedScale"],
            ),
            DynamicMetric(
                metric_name="combat/prayer/target_attacked_with_smite",
                accumulator=AverageValueEqualsAccumulator(value=True),
                field_path=["meta", "targetAttackedWithSmite"],
            ),
            DynamicMetric(
                metric_name="combat/prayer/smite_damage_received",
                accumulator=AverageAccumulator(),
                field_path=["meta", "smite_damage_received"],
            ),
            DynamicMetric(
                metric_name="combat/player_hit_attack_speed_avg",
                accumulator=AverageAccumulator(),
                field_path=["meta", "playerHitAttackSpeed"],
            ),
            DynamicMetric(
                metric_name="combat/target_hit_attack_speed_avg",
                accumulator=AverageAccumulator(),
                field_path=["meta", "targetHitAttackSpeed"],
            ),
        ]
