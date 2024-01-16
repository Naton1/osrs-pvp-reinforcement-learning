from collections.abc import Iterable
from dataclasses import dataclass, field


@dataclass
class MatchOutcome:
    wins: int = 0
    losses: int = 0
    ties: int = 0

    def total_matches(self) -> int:
        return self.wins + self.losses + self.ties


@dataclass(frozen=True)
class MatchOutcomeTracker:
    outcomes: dict[str, MatchOutcome] = field(default_factory=dict)

    def list_outcomes(self) -> Iterable[tuple[str, MatchOutcome]]:
        return self.outcomes.items()

    def reset(self) -> None:
        self.outcomes.clear()

    def get_player_outcomes(self, player: str) -> MatchOutcome:
        if player not in self.outcomes:
            self.outcomes[player] = MatchOutcome()
        return self.outcomes[player]

    def add_win(self, player: str) -> None:
        self.get_player_outcomes(player).wins += 1

    def add_loss(self, player: str) -> None:
        self.get_player_outcomes(player).losses += 1

    def add_tie(self, player: str) -> None:
        self.get_player_outcomes(player).ties += 1


def merge_match_outcomes(trackers: list[MatchOutcomeTracker]) -> MatchOutcomeTracker:
    accumulator = MatchOutcomeTracker()

    for tracker in trackers:
        for model, outcome in tracker.list_outcomes():
            outcomes = accumulator.get_player_outcomes(model)
            outcomes.wins += outcome.wins
            outcomes.losses += outcome.losses
            outcomes.ties += outcome.ties

    return accumulator
