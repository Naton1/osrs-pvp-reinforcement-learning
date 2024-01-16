package com.github.naton1.rl.env.nh;

import static com.elvarg.util.ItemIdentifiers.AMULET_OF_BLOOD_FURY;
import static com.elvarg.util.ItemIdentifiers.ANCIENT_CHAPS;
import static com.elvarg.util.ItemIdentifiers.ANCIENT_DHIDE;
import static com.elvarg.util.ItemIdentifiers.ARCHERS_RING_I_;
import static com.elvarg.util.ItemIdentifiers.BARROWS_GLOVES;
import static com.elvarg.util.ItemIdentifiers.BERSERKER_RING_I_;
import static com.elvarg.util.ItemIdentifiers.DRAGONSTONE_DRAGON_BOLTS_E_;
import static com.elvarg.util.ItemIdentifiers.DRAGON_CLAWS;
import static com.elvarg.util.ItemIdentifiers.DRAGON_SCIMITAR;
import static com.elvarg.util.ItemIdentifiers.FIRE_CAPE;
import static com.elvarg.util.ItemIdentifiers.IMBUED_GUTHIX_CAPE;
import static com.elvarg.util.ItemIdentifiers.INFERNAL_CAPE;
import static com.elvarg.util.ItemIdentifiers.INFINITY_BOOTS;
import static com.elvarg.util.ItemIdentifiers.INFINITY_BOTTOMS;
import static com.elvarg.util.ItemIdentifiers.INFINITY_TOP;
import static com.elvarg.util.ItemIdentifiers.KODAI_WAND;
import static com.elvarg.util.ItemIdentifiers.MAGES_BOOK;
import static com.elvarg.util.ItemIdentifiers.OCCULT_NECKLACE;
import static com.elvarg.util.ItemIdentifiers.OPAL_DRAGON_BOLTS_E_;
import static com.elvarg.util.ItemIdentifiers.RANGING_POTION_1_;
import static com.elvarg.util.ItemIdentifiers.RANGING_POTION_2_;
import static com.elvarg.util.ItemIdentifiers.RANGING_POTION_3_;
import static com.elvarg.util.ItemIdentifiers.RANGING_POTION_4_;
import static com.elvarg.util.ItemIdentifiers.RUNE_DEFENDER;
import static com.elvarg.util.ItemIdentifiers.SARADOMIN_BREW_4_;
import static com.elvarg.util.ItemIdentifiers.SEERS_RING_I_;
import static com.elvarg.util.ItemIdentifiers.SHARK;
import static com.elvarg.util.ItemIdentifiers.SLAYER_HELMET_I_;
import static com.elvarg.util.ItemIdentifiers.SPIRIT_SHIELD;
import static com.elvarg.util.ItemIdentifiers.SUPER_COMBAT_POTION_1_;
import static com.elvarg.util.ItemIdentifiers.SUPER_COMBAT_POTION_2_;
import static com.elvarg.util.ItemIdentifiers.SUPER_COMBAT_POTION_3_;
import static com.elvarg.util.ItemIdentifiers.SUPER_COMBAT_POTION_4_;
import static com.elvarg.util.ItemIdentifiers.SUPER_RESTORE_4_;
import static com.elvarg.util.ItemIdentifiers.VOLATILE_NIGHTMARE_STAFF;
import static com.elvarg.util.ItemIdentifiers.ZARYTE_CROSSBOW;

import com.elvarg.game.content.PrayerHandler;
import com.elvarg.game.model.MagicSpellbook;
import com.github.naton1.rl.env.Loadout;
import java.util.List;

public class NhZerkLoadout extends DynamicNhLoadout {

    @Override
    public int[] getRangedGear() {
        return new int[] {
            ZARYTE_CROSSBOW,
            ANCIENT_DHIDE,
            ANCIENT_CHAPS,
            AMULET_OF_BLOOD_FURY,
            SPIRIT_SHIELD,
            SLAYER_HELMET_I_,
            BARROWS_GLOVES,
            INFINITY_BOOTS,
            SEERS_RING_I_,
            OPAL_DRAGON_BOLTS_E_,
            INFERNAL_CAPE
        };
    }

    @Override
    public int[] getMageGear() {
        return new int[] {
            KODAI_WAND,
            INFINITY_TOP,
            INFINITY_BOTTOMS,
            OCCULT_NECKLACE,
            SPIRIT_SHIELD,
            SLAYER_HELMET_I_,
            BARROWS_GLOVES,
            INFINITY_BOOTS,
            SEERS_RING_I_,
            OPAL_DRAGON_BOLTS_E_,
            IMBUED_GUTHIX_CAPE
        };
    }

    @Override
    public int[] getMeleeGear() {
        return new int[] {
            DRAGON_SCIMITAR,
            ANCIENT_DHIDE,
            ANCIENT_CHAPS,
            AMULET_OF_BLOOD_FURY,
            RUNE_DEFENDER,
            SLAYER_HELMET_I_,
            BARROWS_GLOVES,
            INFINITY_BOOTS,
            SEERS_RING_I_,
            OPAL_DRAGON_BOLTS_E_,
            INFERNAL_CAPE
        };
    }

    @Override
    public int[] getMeleeSpecGear() {
        return new int[] {
            DRAGON_CLAWS,
            ANCIENT_DHIDE,
            ANCIENT_CHAPS,
            SLAYER_HELMET_I_,
            INFERNAL_CAPE,
            BARROWS_GLOVES,
            INFINITY_BOOTS,
            SEERS_RING_I_,
            OPAL_DRAGON_BOLTS_E_,
            AMULET_OF_BLOOD_FURY
        };
    }

    @Override
    public PrayerHandler.PrayerData[] getRangedPrayers() {
        return new PrayerHandler.PrayerData[] {PrayerHandler.PrayerData.EAGLE_EYE, PrayerHandler.PrayerData.STEEL_SKIN};
    }

    @Override
    public PrayerHandler.PrayerData[] getMagePrayers() {
        return new PrayerHandler.PrayerData[] {
            PrayerHandler.PrayerData.MYSTIC_MIGHT, PrayerHandler.PrayerData.STEEL_SKIN
        };
    }

    @Override
    public PrayerHandler.PrayerData[] getMeleePrayers() {
        return new PrayerHandler.PrayerData[] {
            PrayerHandler.PrayerData.INCREDIBLE_REFLEXES,
            PrayerHandler.PrayerData.ULTIMATE_STRENGTH,
            PrayerHandler.PrayerData.STEEL_SKIN
        };
    }

    @Override
    public Loadout.CombatStats getCombatStats() {
        return Loadout.CombatStats.builder()
                .attackLevel(60)
                .strengthLevel(99)
                .defenceLevel(45)
                .hitpointsLevel(99)
                .magicLevel(99)
                .rangedLevel(99)
                .prayerLevel(55)
                .build();
    }

    @Override
    protected void applyRandomization(final RandomizerContext randomizerContext) {
        // 20% chance to use nightmare staff
        if (randomizerContext.getRandom().nextInt(5) == 1) {
            randomizerContext.swapMage(KODAI_WAND, VOLATILE_NIGHTMARE_STAFF);
        }
        // 10% chance to archer ring
        if (randomizerContext.getRandom().nextInt(10) == 1) {
            randomizerContext.swapAll(SEERS_RING_I_, ARCHERS_RING_I_);
        }
        // 10% chance to use zerker ring
        if (randomizerContext.getRandom().nextInt(10) == 1) {
            randomizerContext.swapAll(SEERS_RING_I_, BERSERKER_RING_I_);
        }
        // 10% chance to use no occult
        if (randomizerContext.getRandom().nextInt(10) == 1) {
            randomizerContext.swapAll(OCCULT_NECKLACE, AMULET_OF_BLOOD_FURY);
        }
        // 10% chance to use no fire cape
        if (randomizerContext.getRandom().nextInt(10) == 1) {
            randomizerContext.swapAll(INFERNAL_CAPE, IMBUED_GUTHIX_CAPE);
            randomizerContext.swapAll(FIRE_CAPE, IMBUED_GUTHIX_CAPE);
        }
        // 10% chance to use mage's book
        if (randomizerContext.getRandom().nextInt(10) == 1) {
            randomizerContext.swapMage(SPIRIT_SHIELD, MAGES_BOOK);
        }
        // 5% chance to use dragon bolts
        if (randomizerContext.getRandom().nextInt(20) == 1) {
            randomizerContext.swapAll(OPAL_DRAGON_BOLTS_E_, DRAGONSTONE_DRAGON_BOLTS_E_);
        }
        // 5% chance to have no super combat
        if (randomizerContext.getRandom().nextInt(20) == 1) {
            randomizerContext.swapInventoryQuantity(SUPER_COMBAT_POTION_4_, 0);
        }
        // 5% chance to vary super combat doses
        else if (randomizerContext.getRandom().nextInt(20) == 1) {
            final List<Integer> itemIds = List.of(
                    SUPER_COMBAT_POTION_4_, SUPER_COMBAT_POTION_3_, SUPER_COMBAT_POTION_2_, SUPER_COMBAT_POTION_1_);
            final int index = randomizerContext.getRandom().nextInt(itemIds.size());
            randomizerContext.swapInventory(SUPER_COMBAT_POTION_4_, itemIds.get(index));
        }
        // 5% chance to have no ranged potion
        if (randomizerContext.getRandom().nextInt(20) == 1) {
            randomizerContext.swapInventoryQuantity(RANGING_POTION_4_, 0);
        }
        // 5% chance to vary super ranged pot doses
        else if (randomizerContext.getRandom().nextInt(20) == 1) {
            final List<Integer> itemIds =
                    List.of(RANGING_POTION_4_, RANGING_POTION_3_, RANGING_POTION_2_, RANGING_POTION_1_);
            final int index = randomizerContext.getRandom().nextInt(itemIds.size());
            randomizerContext.swapInventory(RANGING_POTION_4_, itemIds.get(index));
        }
        // 20% chance to randomize brew count
        if (randomizerContext.getRandom().nextInt(5) == 1) {
            // Now randomize brews from 0 to 8, and adjust potions accordingly
            final int brewCount = randomizerContext.getRandom().nextInt(9);
            final int restoreCount = Math.max(2, brewCount / 2 + 1);
            randomizerContext.swapInventoryQuantity(SARADOMIN_BREW_4_, brewCount);
            randomizerContext.swapInventoryQuantity(SUPER_RESTORE_4_, restoreCount);
        }
        // 10% chance to use veng
        if (randomizerContext.getRandom().nextInt(10) == 1) {
            randomizerContext.setSpellbookOverride(MagicSpellbook.LUNAR);
        }
        // 10% chance to use sharks
        if (randomizerContext.getRandom().nextInt(10) == 1) {
            randomizerContext.setFillItemOverride(SHARK);
        }
    }
}
