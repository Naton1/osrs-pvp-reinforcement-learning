import enum
from collections import defaultdict
from collections.abc import Iterable
from dataclasses import dataclass, field

DEFAULT_ELO = 1500.0


class Outcome(enum.Enum):
    WON = 1.0
    LOST = 0.0
    TIED = 0.5


def _expected_score(rating_a: float, rating_b: float) -> float:
    return 1 / (1 + 10 ** ((rating_b - rating_a) / 400))


def _update_ratings(
    rating_a: float, rating_b: float, a_score_factor: float, k_factor: int = 16
) -> tuple[float, float]:
    expected_a = _expected_score(rating_a, rating_b)
    new_rating_a = rating_a + k_factor * (a_score_factor - expected_a)
    new_rating_b = rating_b + k_factor * (1 - a_score_factor - (1 - expected_a))
    return new_rating_a, new_rating_b


@dataclass(frozen=True)
class EloTracker:
    ratings: dict[str, float] = field(default_factory=dict)
    frozen_ratings: set[str] = field(default_factory=set)

    def freeze_rating(self, player: str) -> None:
        self.frozen_ratings.add(player)

    def is_rating_frozen(self, player: str) -> bool:
        return player in self.frozen_ratings

    def list_ratings(self) -> Iterable[tuple[str, float]]:
        return self.ratings.items()

    def contains_player(self, player: str) -> bool:
        return player in self.ratings

    def add_player(self, player: str, rating: float = DEFAULT_ELO) -> None:
        self.ratings[player] = rating

    def get_player_rating(self, player: str) -> float:
        if not self.contains_player(player):
            self.add_player(player)
        return self.ratings[player]

    def add_outcome(self, player1: str, player2: str, outcome: Outcome) -> None:
        self.add_outcomes([(player1, player2, outcome)])

    def add_outcomes(self, outcomes: list[tuple[str, str, Outcome]]) -> None:
        rating_changes: dict[str, float] = defaultdict(lambda: 0.0)

        for player1, player2, outcome in outcomes:
            player1_rating = self.get_player_rating(player1)
            player2_rating = self.get_player_rating(player2)

            a_score_factor = outcome.value

            new_rating_a, new_rating_b = _update_ratings(
                player1_rating, player2_rating, a_score_factor
            )

            rating_change_a = new_rating_a - player1_rating
            rating_change_b = new_rating_b - player2_rating

            rating_changes[player1] += rating_change_a
            rating_changes[player2] += rating_change_b

        for player, change in rating_changes.items():
            if not self.is_rating_frozen(player):
                self.ratings[player] += change
