from collections import defaultdict
from dataclasses import dataclass, field
from typing import cast

import numpy as np
from numpy.typing import NDArray
from torch.utils.tensorboard import SummaryWriter


@dataclass(frozen=True)
class League:
    """
    A prioritized self-play opponent sampling system
    from the paper 'Dota 2 with Large Scale Deep Reinforcement Learning'
    """

    qualities: dict[str, float] = field(default_factory=lambda: {})

    def contains_opponent(self, opponent: str) -> bool:
        return opponent in self.qualities

    def add_opponent(self, opponent: str) -> None:
        assert opponent not in self.qualities, f"Opponent already exists: {opponent}"
        self.qualities[opponent] = (
            max(self.qualities.values()) if self.qualities else 1.0
        )

    def remove_opponent(self, opponent: str) -> bool:
        return self.qualities.pop(opponent, None) is not None

    def add_win(self, opponent: str, learning_rate: float = 0.01) -> None:
        assert opponent in self.qualities, f"Unknown opponent: {opponent}"
        idx = list(self.qualities.keys()).index(opponent)
        softmax = self._get_softmax_distribution()
        self.qualities[opponent] -= learning_rate / (len(self.qualities) * softmax[idx])

    def sample_opponent(self) -> str:
        assert self.qualities, "No opponents available"
        keys = list(self.qualities.keys())
        softmax = self._get_softmax_distribution()
        return cast(str, np.random.choice(keys, p=softmax))

    def _get_softmax_distribution(self) -> NDArray[np.float32]:
        x = np.fromiter(self.qualities.values(), dtype=np.float32)
        x_max = np.amax(x, keepdims=True)
        exp_x_shifted = np.exp(x - x_max)
        return cast(
            NDArray[np.float32], exp_x_shifted / np.sum(exp_x_shifted, keepdims=True)
        )

    def log_stats(
        self, log_prefix: str, global_step: int, summary_writer: SummaryWriter
    ) -> None:
        if not self.qualities:
            return
        summary_writer.add_scalar(
            f"{log_prefix}_num_opponents", len(self.qualities), global_step
        )
        values = list(self.qualities.values())
        summary_writer.add_scalar(
            f"{log_prefix}_mean_quality", np.mean(values), global_step
        )
        summary_writer.add_scalar(
            f"{log_prefix}_min_quality", np.min(values), global_step
        )
        summary_writer.add_scalar(
            f"{log_prefix}_max_quality", np.max(values), global_step
        )
        summary_writer.add_scalar(
            f"{log_prefix}_std_quality", np.std(values), global_step
        )
        summary_writer.add_scalar(
            f"{log_prefix}_median_quality", np.median(values), global_step
        )
        summary_writer.add_histogram(
            f"{log_prefix}_quality_histogram", np.array(values), global_step
        )
        softmax = self._get_softmax_distribution()
        summary_writer.add_scalar(
            f"{log_prefix}_mean_softmax", np.mean(softmax), global_step
        )
        summary_writer.add_scalar(
            f"{log_prefix}_min_softmax", np.min(softmax), global_step
        )
        summary_writer.add_scalar(
            f"{log_prefix}_max_softmax", np.max(softmax), global_step
        )
        summary_writer.add_scalar(
            f"{log_prefix}_std_softmax", np.std(softmax), global_step
        )
        summary_writer.add_scalar(
            f"{log_prefix}_median_softmax", np.median(softmax), global_step
        )
        summary_writer.add_histogram(
            f"{log_prefix}_softmax_histogram", softmax, global_step
        )
        summary_writer.add_scalar(
            f"{log_prefix}_entropy_softmax",
            -np.sum(softmax * np.log2(softmax + 1e-10)),
            global_step,
        )


def merge_leagues(leagues: list[League]) -> League:
    accumulator = defaultdict(list)

    for league in leagues:
        for key, value in league.qualities.items():
            accumulator[key].append(value)

    avg_qualities = {
        key: sum(values) / len(values) for key, values in accumulator.items()
    }

    return League(avg_qualities)
