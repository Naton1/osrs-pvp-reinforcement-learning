from typing import Any

from pvp_ml.scripted.script_plugin import ScriptPlugin


class StayAliveInCombatPlugin(ScriptPlugin):
    def predict(
        self,
        ranged_attack: bool = False,
        eat_primary_food: bool = False,
        player_health_percent: float = 0.0,
        **kwargs: Any,
    ) -> dict[str, str]:
        action = {}
        if ranged_attack:
            action["attack"] = "ranged_attack"
            action["ranged_attack_type"] = "basic_ranged_attack"
        if player_health_percent < 0.6 and eat_primary_food:
            action["food"] = "eat_primary_food"
        return action
