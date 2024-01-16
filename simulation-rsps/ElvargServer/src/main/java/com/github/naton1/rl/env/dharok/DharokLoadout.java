package com.github.naton1.rl.env.dharok;

import static com.elvarg.util.ItemIdentifiers.ABYSSAL_TENTACLE;
import static com.elvarg.util.ItemIdentifiers.AMULET_OF_STRENGTH;
import static com.elvarg.util.ItemIdentifiers.ANGLERFISH;
import static com.elvarg.util.ItemIdentifiers.ASTRAL_RUNE;
import static com.elvarg.util.ItemIdentifiers.BARROWS_GLOVES;
import static com.elvarg.util.ItemIdentifiers.COOKED_KARAMBWAN;
import static com.elvarg.util.ItemIdentifiers.DEATH_RUNE;
import static com.elvarg.util.ItemIdentifiers.DHAROKS_GREATAXE;
import static com.elvarg.util.ItemIdentifiers.DHAROKS_HELM;
import static com.elvarg.util.ItemIdentifiers.DHAROKS_PLATEBODY;
import static com.elvarg.util.ItemIdentifiers.DHAROKS_PLATELEGS;
import static com.elvarg.util.ItemIdentifiers.DRAGON_BOOTS;
import static com.elvarg.util.ItemIdentifiers.DRAGON_DEFENDER;
import static com.elvarg.util.ItemIdentifiers.EARTH_RUNE;
import static com.elvarg.util.ItemIdentifiers.FIRE_CAPE;
import static com.elvarg.util.ItemIdentifiers.GRANITE_MAUL;
import static com.elvarg.util.ItemIdentifiers.RING_OF_RECOIL;
import static com.elvarg.util.ItemIdentifiers.SARADOMIN_BREW_4_;
import static com.elvarg.util.ItemIdentifiers.SUPER_COMBAT_POTION_4_;
import static com.elvarg.util.ItemIdentifiers.SUPER_RESTORE_4_;

import com.elvarg.game.model.Item;
import com.elvarg.game.model.MagicSpellbook;
import com.github.naton1.rl.env.Loadout;

public class DharokLoadout implements Loadout {

    @Override
    public Item[] getInventory() {
        return new Item[] {
            new Item(DHAROKS_GREATAXE),
            new Item(GRANITE_MAUL),
            new Item(SUPER_COMBAT_POTION_4_),
            new Item(SARADOMIN_BREW_4_),
            new Item(SARADOMIN_BREW_4_),
            new Item(SUPER_RESTORE_4_),
            new Item(SUPER_RESTORE_4_),
            new Item(SUPER_RESTORE_4_),
            new Item(COOKED_KARAMBWAN),
            new Item(COOKED_KARAMBWAN),
            new Item(COOKED_KARAMBWAN),
            new Item(ANGLERFISH),
            new Item(ANGLERFISH),
            new Item(ANGLERFISH),
            new Item(ANGLERFISH),
            new Item(ANGLERFISH),
            new Item(ANGLERFISH),
            new Item(ANGLERFISH),
            new Item(ANGLERFISH),
            new Item(ANGLERFISH),
            new Item(ANGLERFISH),
            new Item(ANGLERFISH),
            new Item(ANGLERFISH),
            new Item(ANGLERFISH),
            new Item(ANGLERFISH),
            new Item(DEATH_RUNE, 10000),
            new Item(ASTRAL_RUNE, 10000),
            new Item(EARTH_RUNE, 10000),
        };
    }

    @Override
    public Item[] getEquipment() {
        return new Item[] {
            new Item(DHAROKS_PLATEBODY),
            new Item(DHAROKS_PLATELEGS),
            new Item(DHAROKS_HELM),
            new Item(AMULET_OF_STRENGTH),
            new Item(DRAGON_BOOTS),
            new Item(FIRE_CAPE),
            new Item(ABYSSAL_TENTACLE),
            new Item(DRAGON_DEFENDER),
            new Item(RING_OF_RECOIL),
            new Item(BARROWS_GLOVES)
        };
    }

    @Override
    public CombatStats getCombatStats() {
        return CombatStats.builder()
                .attackLevel(99)
                .strengthLevel(99)
                .defenceLevel(99)
                .hitpointsLevel(99)
                .rangedLevel(99)
                .magicLevel(99)
                .prayerLevel(99)
                .build();
    }

    @Override
    public MagicSpellbook getMagicSpellbook() {
        return MagicSpellbook.LUNAR;
    }
}
