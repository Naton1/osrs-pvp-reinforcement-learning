"""
This script will run fights of all agents in the '/reference' directory against each other to generate
ELO ratings for each agent. These ELO ratings can then be used to evaluate performance of (other) agents while training.
"""
import argparse
import asyncio
import logging
import os
import random
import sys
from functools import partial
from typing import Any

from pvp_ml.env.pvp_env import PvpEnv
from pvp_ml.env.simulation import Simulation
from pvp_ml.ppo.ppo import PPO
from pvp_ml.util.args_helper import strtobool
from pvp_ml.util.async_evaluator import AsyncEvaluator
from pvp_ml.util.contract_loader import get_env_types
from pvp_ml.util.elo_tracker import EloTracker, Outcome
from pvp_ml.util.match_outcome_tracker import MatchOutcomeTracker
from pvp_ml.util.reference_rating import (
    create_reference_elo_tracker,
    get_reference_agents_for_env,
    update_reference_rating,
)
from pvp_ml.util.remote_processor.remote_processor import (
    REMOTE_PROCESSOR_TYPES,
    THREAD_REMOTE_PROCESSOR,
    RemoteProcessor,
    create_remote_processor,
)

logger = logging.getLogger(__name__)


def update_model_ratings(elo_tracker: EloTracker) -> None:
    for player, rating in elo_tracker.list_ratings():
        if not elo_tracker.is_rating_frozen(player):
            update_reference_rating(player, rating)


async def run_match_fighters(
    fight_index: int,
    n_fights: int,
    env_type: str,
    elo_tracker: EloTracker,
    remote_processor: RemoteProcessor,
    simulation: Simulation,
    match_outcome_tracker: MatchOutcomeTracker,
) -> None:
    agents = list(get_reference_agents_for_env(env_type))

    for i in range(n_fights):
        selected_matchup = random.sample(agents, k=2)
        agent1 = selected_matchup[0]
        agent2 = selected_matchup[1]

        model1_meta = PPO.load_meta(agent1.model)
        model2_meta = PPO.load_meta(agent2.model)

        saved_env_kwargs1 = model1_meta.custom_data["env_kwargs"]
        saved_env_kwargs2 = model2_meta.custom_data["env_kwargs"]

        saved_env_kwargs1[PvpEnv.REMOTE_ENV_PORT_KEY] = simulation.remote_env_port
        saved_env_kwargs1[PvpEnv.REMOTE_ENV_HOST_KEY] = "localhost"
        saved_env_kwargs2[PvpEnv.REMOTE_ENV_PORT_KEY] = simulation.remote_env_port
        saved_env_kwargs2[PvpEnv.REMOTE_ENV_HOST_KEY] = "localhost"

        placement_value = random.randint(0, 1000)  # Account build placement
        player1_id = f"{fight_index}-{i}-1 {placement_value}"
        player2_id = f"{fight_index}-{i}-2 {placement_value}"

        env1 = PvpEnv(**saved_env_kwargs1, env_id=player1_id, target=player2_id)
        env2 = PvpEnv(**saved_env_kwargs2, env_id=player2_id, target=player1_id)

        match_done = False

        def _handle_done(
            model: str,
            reward: float,
            info: dict[str, Any],
            player_a: str,
            player_b: str,
        ) -> bool:
            nonlocal match_done
            if not match_done:
                match_done = True
                outcome = Outcome[info["terminal_state"]]
                player_a = os.path.basename(player_a)
                player_b = os.path.basename(player_b)
                elo_tracker.add_outcome(
                    player_a,
                    player_b,
                    outcome,
                )
                if outcome == Outcome.WON:
                    match_outcome_tracker.add_win(player_a)
                    match_outcome_tracker.add_loss(player_b)
                elif outcome == Outcome.LOST:
                    match_outcome_tracker.add_loss(player_a)
                    match_outcome_tracker.add_win(player_b)
                elif outcome == Outcome.TIED:
                    match_outcome_tracker.add_tie(player_a)
                    match_outcome_tracker.add_tie(player_b)
                logger.info(
                    f"Match finished - {info['id']} ({player_a}) {outcome.value} vs. {info['target']} ({player_b})"
                )
            return True

        logger.info(f"Running new match: {player1_id} vs. {player2_id}")
        await asyncio.gather(
            AsyncEvaluator.evaluate(
                env1,
                lambda: agent1.model,
                fight_index % remote_processor.get_pool_size(),
                remote_processor,
                deterministic=False,
                on_episode_complete=partial(
                    _handle_done, player_a=agent1.model, player_b=agent2.model
                ),
            ),
            AsyncEvaluator.evaluate(
                env2,
                lambda: agent2.model,
                fight_index % remote_processor.get_pool_size(),
                remote_processor,
                deterministic=False,
                on_episode_complete=partial(
                    _handle_done, player_a=agent2.model, player_b=agent1.model
                ),
            ),
        )


async def generate_reference_ratings(
    env_type: str,
    concurrent_fights: int,
    fresh_ratings: bool,
    fights_per_fighter: int,
    remote_processor_pool_size: int,
    remote_processor_type: str,
    device: str,
) -> tuple[EloTracker, MatchOutcomeTracker]:
    elo_tracker = create_reference_elo_tracker(
        env_type=env_type,
        freeze_all_ratings=False,
        reset_elo=fresh_ratings,
    )
    match_outcome_tracker = MatchOutcomeTracker()

    async with await create_remote_processor(
        pool_size=remote_processor_pool_size,
        device=device,
        processor_type=remote_processor_type,
    ) as remote_processor:
        with Simulation() as simulation:
            simulation.wait_until_loaded()
            await asyncio.gather(
                *[
                    run_match_fighters(
                        i,
                        fights_per_fighter,
                        env_type,
                        elo_tracker,
                        remote_processor,
                        simulation,
                        match_outcome_tracker,
                    )
                    for i in range(concurrent_fights)
                ]
            )

    return elo_tracker, match_outcome_tracker


def print_match_outcomes(match_outcome_tracker: MatchOutcomeTracker) -> None:
    outcomes = [
        f"{player} \t- {outcomes.wins} wins\t {outcomes.losses} losses\t {outcomes.ties} ties\t {outcomes.total_matches()} total"
        for player, outcomes in match_outcome_tracker.list_outcomes()
    ]
    outcome_lines = "\n".join(outcomes)
    logger.info(f"\n---- Match Outcomes ----\n{outcome_lines}")


def print_reference_ratings(elo_tracker: EloTracker) -> None:
    ratings = [
        f"{player} \t- {rating}" for player, rating in elo_tracker.list_ratings()
    ]
    rating_lines = "\n".join(ratings)
    logger.info(f"\n---- Reference Ratings ----\n{rating_lines}")


def update_rating(agent: str, rating: int) -> None:
    update_reference_rating(agent, rating if rating != -1 else None)


def main(argv: list[str]) -> None:
    envs = get_env_types()

    parser = argparse.ArgumentParser(
        description="Generate reference agent ratings to compare against during training"
    )
    parser.add_argument(
        "action",
        type=str,
        nargs="?",
        default="run",
        help="Action to run",
        choices=["run", "show", "update"],
    )
    parser.add_argument(
        "--env-name",
        type=str,
        help="Environment type name",
        default="NhEnv",
        choices=list(envs),
    )
    parser.add_argument(
        "--num-concurrent-fights",
        type=int,
        help="Number of concurrent fights (players = this * 2)",
        default=50,
    )
    parser.add_argument(
        "--num-fights-per-fighter",
        type=int,
        help="Number of fights that each concurrent fighter should perform",
        default=10,
    )
    parser.add_argument(
        "--remote-processor-pool-size",
        type=int,
        help="Remote processor pool size",
        default=10,
    )
    parser.add_argument(
        "--remote-processor-type",
        type=str,
        help="Remote processor type",
        choices=REMOTE_PROCESSOR_TYPES,
        default=THREAD_REMOTE_PROCESSOR,
    )
    parser.add_argument(
        "--device",
        type=str,
        help="Processor device",
        default="cpu",
    )
    parser.add_argument(
        "--dry-run",
        type=lambda x: bool(strtobool(x)),
        nargs="?",
        const=True,
        help="Don't perform any mutating operations (won't update the agent reference ratings)",
        default=False,
    )
    parser.add_argument(
        "--fresh-ratings",
        type=lambda x: bool(strtobool(x)),
        nargs="?",
        const=True,
        help="Train ratings from scratch - don't start from existing ratings",
        default=True,
    )
    parser.add_argument(
        "--agent",
        type=str,
        help="Update rating of the specific agent, when using update action",
        default="",
    )
    parser.add_argument(
        "--rating",
        type=int,
        help="Sets the rating of the specified agent, when using update action. Set to -1 to reset rating.",
        default=-1,
    )

    args = parser.parse_args(argv)

    if args.action == "show":
        print_reference_ratings(create_reference_elo_tracker(env_type=args.env_name))
        return

    if args.action == "update":
        assert not args.dry_run, "Dry-run not supported for 'update' action"
        update_rating(args.agent, args.rating)
        return

    logger.info(
        f"Generating reference ratings for {len(get_reference_agents_for_env(args.env_name))} agents"
        f" using {args.num_fights_per_fighter} matches per player, and {args.num_concurrent_fights} concurrent matches"
    )
    elo_tracker, match_outcome_tracker = asyncio.run(
        generate_reference_ratings(
            env_type=args.env_name,
            fights_per_fighter=args.num_fights_per_fighter,
            concurrent_fights=args.num_concurrent_fights,
            fresh_ratings=args.fresh_ratings,
            remote_processor_pool_size=args.remote_processor_pool_size,
            remote_processor_type=args.remote_processor_type,
            device=args.device,
        )
    )
    logger.info(
        f"Finished generating reference ratings for {len(get_reference_agents_for_env(args.env_name))} agents"
    )

    print_match_outcomes(match_outcome_tracker)
    print_reference_ratings(elo_tracker)

    if not args.dry_run:
        logger.info("Updating reference ratings")
        update_model_ratings(elo_tracker)


if __name__ == "__main__":
    main(sys.argv[1:])
