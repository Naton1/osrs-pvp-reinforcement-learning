import logging
from collections.abc import Callable

from pvp_ml.ppo.ppo import PPO, ModelExtension

logger = logging.getLogger(__name__)

WIN_RATE_EXTENSION = "winrate"


def manage_extension(
    extension_name: str,
    ppo: PPO,
    extension_fn: Callable[[], ModelExtension],
    add_extension: bool = True,
) -> None:
    if add_extension:
        if not ppo.has_extension(extension_name):
            ppo.register_extension(extension_name, extension_fn())
            logger.info(f"Registered '{extension_name}' extension")
    elif ppo.has_extension(extension_name):
        ppo.remove_extension(extension_name)
        logger.info(f"Removed '{extension_name}' extension")
