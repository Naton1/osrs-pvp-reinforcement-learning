import logging
import os

logger = logging.getLogger(__name__)


def init() -> None:
    import ray

    if not ray.is_initialized():
        logger.info(f"Initializing ray - RAY_ADDRESS={os.getenv('RAY_ADDRESS')}")
        ray.init(namespace="pvp-ml")
