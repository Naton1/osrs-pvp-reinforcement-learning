package com.github.naton1.rl.env;

import com.elvarg.game.content.presets.Presetable;
import com.elvarg.game.entity.impl.playerbot.fightstyle.CombatAction;
import com.elvarg.game.entity.impl.playerbot.fightstyle.FighterPreset;
import com.elvarg.game.model.Item;
import com.elvarg.game.model.MagicSpellbook;
import com.elvarg.util.Misc;
import lombok.Builder;
import lombok.Value;

public interface Loadout {

    Item[] getInventory();

    Item[] getEquipment();

    CombatStats getCombatStats();

    MagicSpellbook getMagicSpellbook();

    default Presetable asPreset() {
        // Remove 'Loadout', and add spaces between each word
        final String rawLoadoutName = getClass().getSimpleName().replace("Loadout", "");
        final String spacedLoadoutName = rawLoadoutName.replaceAll("(?<!^)(?=[A-Z])", " ");
        return new Presetable(
                spacedLoadoutName,
                getInventory(),
                getEquipment(),
                getCombatStats().toArray(),
                getMagicSpellbook(),
                true);
    }

    default FighterPreset asDummyFighterPreset() {
        // We don't care about the fighter preset since we override it all, but some places need one
        // to integrate
        // with the RSPS.
        return new FighterPreset() {
            @Override
            public Presetable getItemPreset() {
                return asPreset();
            }

            @Override
            public CombatAction[] getCombatActions() {
                return new CombatAction[0];
            }
        };
    }

    default int getAmount(int itemId) {
        int count = 0;
        for (Item item : Misc.concat(getInventory(), getEquipment())) {
            if (item == null) {
                continue;
            }
            if (item.getId() == itemId) {
                count += item.getAmount();
            }
        }
        return count;
    }

    @Value
    @Builder
    public static class CombatStats {
        private final int attackLevel;
        private final int strengthLevel;
        private final int defenceLevel;
        private final int hitpointsLevel;
        private final int magicLevel;
        private final int rangedLevel;
        private final int prayerLevel;

        public int[] toArray() {
            return new int[] {
                attackLevel, defenceLevel, strengthLevel, hitpointsLevel, rangedLevel, prayerLevel, magicLevel
            };
        }
    }
}
