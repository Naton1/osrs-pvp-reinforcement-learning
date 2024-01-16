import logging
import os
import sys

_LOG_LEVEL_OVERRIDE_KEY = "LOG_LEVEL"
_DEFAULT_LOG_LEVEL = "INFO"

logging.basicConfig(
    level=logging.getLevelName(os.getenv(_LOG_LEVEL_OVERRIDE_KEY, _DEFAULT_LOG_LEVEL)),
    stream=sys.stdout,
    format="[%(asctime)s.%(msecs)03d] [%(process)d] %(levelname)-8s | %(threadName)-20s [%(name)s.%(funcName)s:%(lineno)d] %(message)s",
    datefmt="%Y-%b-%d %H:%M:%S",
    force=True,
)

# Points to the root directory (which contains the setup.py file for example)
package_root = os.path.dirname(os.path.dirname(__file__))
