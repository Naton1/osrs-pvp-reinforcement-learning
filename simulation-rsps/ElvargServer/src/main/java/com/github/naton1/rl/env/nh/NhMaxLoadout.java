package com.github.naton1.rl.env.nh;

import static com.elvarg.util.ItemIdentifiers.AMULET_OF_BLOOD_FURY;
import static com.elvarg.util.ItemIdentifiers.ANCESTRAL_ROBE_BOTTOM;
import static com.elvarg.util.ItemIdentifiers.ANCESTRAL_ROBE_TOP;
import static com.elvarg.util.ItemIdentifiers.ARMADYL_CHESTPLATE;
import static com.elvarg.util.ItemIdentifiers.ARMADYL_GODSWORD;
import static com.elvarg.util.ItemIdentifiers.BARROWS_GLOVES;
import static com.elvarg.util.ItemIdentifiers.BLESSED_SPIRIT_SHIELD;
import static com.elvarg.util.ItemIdentifiers.BOOTS_OF_BRIMSTONE;
import static com.elvarg.util.ItemIdentifiers.GUTHANS_HELM;
import static com.elvarg.util.ItemIdentifiers.IMBUED_GUTHIX_CAPE;
import static com.elvarg.util.ItemIdentifiers.OCCULT_NECKLACE;
import static com.elvarg.util.ItemIdentifiers.OPAL_DRAGON_BOLTS_E_;
import static com.elvarg.util.ItemIdentifiers.SEERS_RING_I_;
import static com.elvarg.util.ItemIdentifiers.TOXIC_STAFF_OF_THE_DEAD;
import static com.elvarg.util.ItemIdentifiers.VERACS_PLATESKIRT_100;
import static com.elvarg.util.ItemIdentifiers.ZARYTE_CROSSBOW;

import com.elvarg.game.content.PrayerHandler;

public class NhMaxLoadout extends DynamicNhLoadout {

    @Override
    public int[] getRangedGear() {
        return new int[] {
            ZARYTE_CROSSBOW,
            ARMADYL_CHESTPLATE,
            VERACS_PLATESKIRT_100,
            GUTHANS_HELM,
            IMBUED_GUTHIX_CAPE,
            AMULET_OF_BLOOD_FURY,
            BLESSED_SPIRIT_SHIELD,
            BARROWS_GLOVES,
            BOOTS_OF_BRIMSTONE,
            SEERS_RING_I_,
            OPAL_DRAGON_BOLTS_E_
        };
    }

    @Override
    public int[] getMageGear() {
        return new int[] {
            TOXIC_STAFF_OF_THE_DEAD,
            ANCESTRAL_ROBE_TOP,
            GUTHANS_HELM,
            IMBUED_GUTHIX_CAPE,
            ANCESTRAL_ROBE_BOTTOM,
            BLESSED_SPIRIT_SHIELD,
            OPAL_DRAGON_BOLTS_E_,
            OCCULT_NECKLACE,
            SEERS_RING_I_,
            BOOTS_OF_BRIMSTONE,
            BARROWS_GLOVES
        };
    }

    @Override
    public int[] getMeleeGear() {
        return new int[] {
            ARMADYL_GODSWORD,
            ARMADYL_CHESTPLATE,
            VERACS_PLATESKIRT_100,
            AMULET_OF_BLOOD_FURY,
            BARROWS_GLOVES,
            OPAL_DRAGON_BOLTS_E_,
            GUTHANS_HELM,
            IMBUED_GUTHIX_CAPE,
            SEERS_RING_I_,
            BOOTS_OF_BRIMSTONE
        };
    }

    @Override
    public int[] getMeleeSpecGear() {
        return new int[] {
            ARMADYL_GODSWORD,
            ARMADYL_CHESTPLATE,
            VERACS_PLATESKIRT_100,
            AMULET_OF_BLOOD_FURY,
            BARROWS_GLOVES,
            OPAL_DRAGON_BOLTS_E_,
            GUTHANS_HELM,
            IMBUED_GUTHIX_CAPE,
            SEERS_RING_I_,
            BOOTS_OF_BRIMSTONE
        };
    }

    @Override
    public PrayerHandler.PrayerData[] getRangedPrayers() {
        return new PrayerHandler.PrayerData[] {PrayerHandler.PrayerData.RIGOUR};
    }

    @Override
    public PrayerHandler.PrayerData[] getMagePrayers() {
        return new PrayerHandler.PrayerData[] {PrayerHandler.PrayerData.AUGURY};
    }

    @Override
    public PrayerHandler.PrayerData[] getMeleePrayers() {
        return new PrayerHandler.PrayerData[] {PrayerHandler.PrayerData.PIETY};
    }

    @Override
    public CombatStats getCombatStats() {
        return CombatStats.builder()
                .attackLevel(99)
                .strengthLevel(99)
                .defenceLevel(99)
                .hitpointsLevel(99)
                .magicLevel(99)
                .rangedLevel(99)
                .prayerLevel(99)
                .build();
    }

    @Override
    protected void applyRandomization(final RandomizerContext randomizerContext) {}
}
