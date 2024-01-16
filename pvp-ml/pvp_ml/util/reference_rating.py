import logging
import os
from dataclasses import dataclass
from functools import cache

from pvp_ml.ppo.ppo import PPO
from pvp_ml.util.elo_tracker import DEFAULT_ELO, EloTracker
from pvp_ml.util.files import reference_dir

logger = logging.getLogger(__name__)

_REFERENCE_RATING_KEY = "reference_rating"


@dataclass(frozen=True)
class ReferenceAgent:
    model: str
    env_type: str
    rating: int
    freeze_rating: bool = False


@cache
def get_reference_agents() -> list[ReferenceAgent]:
    reference_agents = []
    for reference_agent in os.listdir(reference_dir):
        agent_path = f"{reference_dir}/{reference_agent}"
        meta = PPO.load_meta(agent_path)
        reference_rating = meta.custom_data.get(_REFERENCE_RATING_KEY, DEFAULT_ELO)
        reference_agents.append(
            ReferenceAgent(
                model=agent_path,
                env_type=meta.custom_data["env_kwargs"]["env_name"],
                rating=reference_rating,
                freeze_rating=False,
            )
        )
    return reference_agents


def get_reference_agents_for_env(env_type: str) -> list[ReferenceAgent]:
    return [agent for agent in get_reference_agents() if agent.env_type == env_type]


def update_reference_rating(reference_agent: str, new_rating: float | None) -> None:
    agent_path = f"{reference_dir}/{reference_agent}"
    meta = PPO.load_meta(agent_path)
    current_rating = meta.custom_data.get(_REFERENCE_RATING_KEY, None)
    logger.info(
        f"Updating rating of {reference_agent}: {current_rating} -> {new_rating}"
    )
    if new_rating is not None:
        meta.custom_data[_REFERENCE_RATING_KEY] = new_rating
    elif _REFERENCE_RATING_KEY in meta.custom_data:
        del meta.custom_data[_REFERENCE_RATING_KEY]
    PPO.save_meta(agent_path, meta)
    # Clear cache after saving
    get_reference_agents.cache_clear()


def create_reference_elo_tracker(
    env_type: str,
    freeze_all_ratings: bool = True,
    reset_elo: bool = False,
) -> EloTracker:
    elo_tracker = EloTracker()
    for reference_agent in get_reference_agents_for_env(env_type):
        relative_model_name = os.path.basename(reference_agent.model)
        if not reset_elo:
            elo_tracker.add_player(relative_model_name, reference_agent.rating)
        else:
            # Reset to default
            elo_tracker.add_player(relative_model_name)
        # Should freeze all ratings when evaluating against the references
        # Otherwise, just freeze any 'baseline' references (ex. random policy) when generating reference ratings
        if freeze_all_ratings or reference_agent.freeze_rating:
            elo_tracker.freeze_rating(relative_model_name)
    return elo_tracker
