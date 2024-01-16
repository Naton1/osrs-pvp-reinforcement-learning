import faulthandler
import logging
import threading
import time

logger = logging.getLogger(__name__)


def _track_tracebacks(out_file: str, log_frequency_seconds: int) -> None:
    while True:
        time.sleep(log_frequency_seconds)
        with open(out_file, "w") as f:
            logger.info(f"Writing traceback to {out_file}")
            faulthandler.dump_traceback(f)
            logger.info(
                f"Wrote traceback, waiting {log_frequency_seconds} seconds before next attempt"
            )


def track_tracebacks(out_file: str, log_frequency_seconds: int = 300) -> None:
    # Logs a full process traceback to a file every log_frequency_seconds, useful for debugging deadlocks
    threading.Thread(
        target=_track_tracebacks,
        args=(out_file, log_frequency_seconds),
        daemon=True,
        name="traceback-tracker",
    ).start()
