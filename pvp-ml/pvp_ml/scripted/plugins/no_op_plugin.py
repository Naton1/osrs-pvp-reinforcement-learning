from typing import Any

from pvp_ml.scripted.script_plugin import ScriptPlugin


class NoOpPlugin(ScriptPlugin):
    def predict(self, **kwargs: Any) -> dict[str, str]:
        return {}
