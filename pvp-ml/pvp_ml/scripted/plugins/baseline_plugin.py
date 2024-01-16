import random
from typing import Any

from pvp_ml.scripted.script_plugin import ScriptPlugin


class BaselinePlugin(ScriptPlugin):
    def predict(
        self,
        eat_primary_food: bool = False,
        eat_karambwan: bool = False,
        player_health_percent: float = 0.0,
        use_restore_potion: bool = False,
        use_combat_potion: bool = False,
        use_ranged_potion: bool = False,
        use_brew: bool = False,
        mage_prayer: bool = False,
        ranged_prayer: bool = False,
        melee_prayer: bool = False,
        target_melee_prayer: bool = False,
        target_ranged_prayer: bool = False,
        target_magic_prayer: bool = False,
        target_using_melee: bool = False,
        target_using_ranged: bool = False,
        target_using_mage: bool = False,
        prayer_points: float = False,
        strength_level: float = False,
        ranged_level: float = False,
        mage_attack: bool = False,
        ranged_attack: bool = False,
        melee_attack: bool = False,
        **kwargs: Any,
    ) -> dict[str, str]:
        # Note: this plugin is not complete at the moment.
        # The long-term goal would be to remove 'baseline' logic from the simulation, and have it scripted
        # via plugins.
        actions = {}
        if player_health_percent < 0.6 and eat_primary_food:
            actions["food"] = "eat_primary_food"
        if eat_karambwan and (
            player_health_percent < 0.4
            or (player_health_percent < 0.6 and not eat_primary_food)
        ):
            actions["karambwan"] = "eat_karambwan"

        if prayer_points < 0.6 and use_restore_potion:
            actions["potion"] = "use_restore_potion"
        elif ranged_level < 0.95 and use_ranged_potion:
            actions["potion"] = "use_ranged_potion"
        elif strength_level < 0.95 and use_combat_potion:
            actions["potion"] = "use_combat_potion"

        if target_using_mage and mage_prayer:
            actions["prayer"] = "mage_prayer"
        elif target_using_ranged and ranged_prayer:
            actions["prayer"] = "ranged_prayer"
        elif target_using_melee and melee_prayer:
            actions["prayer"] = "melee_prayer"

        possible_attacks = {
            key: value
            for key, value in {
                "mage_attack": mage_attack and not target_magic_prayer,
                "ranged_attack": ranged_attack and not target_ranged_prayer,
                "melee_attack": melee_attack and not target_melee_prayer,
            }.items()
            if value
        }
        if not possible_attacks and ranged_attack:
            # Default to range, if no attacks available, even if target is praying range
            possible_attacks["ranged_attack"] = True

        if possible_attacks:
            selected_attack = random.choice(list(possible_attacks.keys()))
            actions["attack"] = selected_attack

            if selected_attack == "melee_attack":
                actions["melee_attack_type"] = "basic_melee_attack"
            elif selected_attack == "ranged_attack":
                actions["ranged_attack_type"] = "basic_ranged_attack"
            elif selected_attack == "mage_attack":
                actions["mage_attack_type"] = "use_ice_spell"

        return actions
