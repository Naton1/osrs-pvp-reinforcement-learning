import asyncio
import csv
import logging
from typing import Any

from pvp_ml.env.remote_env_connector import RemoteEnvConnector
from pvp_ml.util.files import get_experiment_dir

logger = logging.getLogger(__name__)


class ServerDebugTracker:
    @staticmethod
    async def run(
        host: str,
        port: int,
        experiment_name: str,
        update_frequency: int = 5 * 60,
        output_file_name: str = "server-debug",
    ) -> None:
        async with RemoteEnvConnector(
            env_id="debugger", host=host, port=port
        ) as connector:
            experiment_dir = get_experiment_dir(experiment_name)
            tracker_file = f"{experiment_dir}/{output_file_name}.csv"

            logger.info(
                f"Running server debug tracker every {update_frequency} seconds to {tracker_file}"
            )

            while True:
                debug_content: list[dict[str, Any]] = await connector.send("debug")
                if debug_content:
                    with open(tracker_file, "w", newline="") as f:
                        dict_writer = csv.DictWriter(f, debug_content[0].keys())
                        dict_writer.writeheader()
                        dict_writer.writerows(debug_content)
                await asyncio.sleep(update_frequency)
