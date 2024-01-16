package com.github.naton1.rl.env.nh;

import static com.elvarg.util.ItemIdentifiers.ABYSSAL_TENTACLE;
import static com.elvarg.util.ItemIdentifiers.AMULET_OF_BLOOD_FURY;
import static com.elvarg.util.ItemIdentifiers.ANCESTRAL_ROBE_BOTTOM;
import static com.elvarg.util.ItemIdentifiers.ANCESTRAL_ROBE_TOP;
import static com.elvarg.util.ItemIdentifiers.ARMADYL_CHAINSKIRT;
import static com.elvarg.util.ItemIdentifiers.ARMADYL_CHESTPLATE;
import static com.elvarg.util.ItemIdentifiers.ARMADYL_GODSWORD;
import static com.elvarg.util.ItemIdentifiers.AVERNIC_DEFENDER;
import static com.elvarg.util.ItemIdentifiers.BARROWS_GLOVES;
import static com.elvarg.util.ItemIdentifiers.BLESSED_SPIRIT_SHIELD;
import static com.elvarg.util.ItemIdentifiers.BOOTS_OF_BRIMSTONE;
import static com.elvarg.util.ItemIdentifiers.DRAGONSTONE_DRAGON_BOLTS_E_;
import static com.elvarg.util.ItemIdentifiers.DRAGON_CLAWS;
import static com.elvarg.util.ItemIdentifiers.GUTHANS_HELM;
import static com.elvarg.util.ItemIdentifiers.IMBUED_GUTHIX_CAPE;
import static com.elvarg.util.ItemIdentifiers.INFERNAL_CAPE;
import static com.elvarg.util.ItemIdentifiers.KARILS_LEATHERSKIRT_100;
import static com.elvarg.util.ItemIdentifiers.KARILS_LEATHERTOP_100;
import static com.elvarg.util.ItemIdentifiers.MAGES_BOOK;
import static com.elvarg.util.ItemIdentifiers.OCCULT_NECKLACE;
import static com.elvarg.util.ItemIdentifiers.OPAL_DRAGON_BOLTS_E_;
import static com.elvarg.util.ItemIdentifiers.RANGING_POTION_1_;
import static com.elvarg.util.ItemIdentifiers.RANGING_POTION_2_;
import static com.elvarg.util.ItemIdentifiers.RANGING_POTION_3_;
import static com.elvarg.util.ItemIdentifiers.RANGING_POTION_4_;
import static com.elvarg.util.ItemIdentifiers.SARADOMIN_BREW_4_;
import static com.elvarg.util.ItemIdentifiers.SEERS_RING_I_;
import static com.elvarg.util.ItemIdentifiers.SHARK;
import static com.elvarg.util.ItemIdentifiers.STAFF_OF_LIGHT;
import static com.elvarg.util.ItemIdentifiers.SUPER_COMBAT_POTION_1_;
import static com.elvarg.util.ItemIdentifiers.SUPER_COMBAT_POTION_2_;
import static com.elvarg.util.ItemIdentifiers.SUPER_COMBAT_POTION_3_;
import static com.elvarg.util.ItemIdentifiers.SUPER_COMBAT_POTION_4_;
import static com.elvarg.util.ItemIdentifiers.SUPER_RESTORE_4_;
import static com.elvarg.util.ItemIdentifiers.TOXIC_STAFF_OF_THE_DEAD;
import static com.elvarg.util.ItemIdentifiers.VERACS_PLATESKIRT_100;
import static com.elvarg.util.ItemIdentifiers.VOLATILE_NIGHTMARE_STAFF;
import static com.elvarg.util.ItemIdentifiers.ZARYTE_CROSSBOW;

import com.elvarg.game.content.PrayerHandler;
import com.elvarg.game.model.MagicSpellbook;
import com.github.naton1.rl.env.Loadout;
import java.util.List;

public class NhMedLoadout extends DynamicNhLoadout {

    @Override
    public int[] getRangedGear() {
        return new int[] {
            ZARYTE_CROSSBOW,
            ARMADYL_CHESTPLATE,
            VERACS_PLATESKIRT_100,
            GUTHANS_HELM,
            INFERNAL_CAPE,
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
            ABYSSAL_TENTACLE,
            ARMADYL_CHESTPLATE,
            VERACS_PLATESKIRT_100,
            AMULET_OF_BLOOD_FURY,
            BARROWS_GLOVES,
            OPAL_DRAGON_BOLTS_E_,
            GUTHANS_HELM,
            INFERNAL_CAPE,
            SEERS_RING_I_,
            AVERNIC_DEFENDER,
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
            INFERNAL_CAPE,
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
    public Loadout.CombatStats getCombatStats() {
        return Loadout.CombatStats.builder()
                .attackLevel(75)
                .strengthLevel(99)
                .defenceLevel(70)
                .hitpointsLevel(99)
                .magicLevel(99)
                .rangedLevel(99)
                .prayerLevel(77)
                .build();
    }

    @Override
    protected void applyRandomization(final RandomizerContext randomizerContext) {
        // 10% chance to use mage's book
        if (randomizerContext.getRandom().nextInt(10) == 1) {
            randomizerContext.swapMage(BLESSED_SPIRIT_SHIELD, MAGES_BOOK);
        }
        // 10% chance to use dragon claws over ags
        if (randomizerContext.getRandom().nextInt(10) == 1) {
            randomizerContext.swapSpec(ARMADYL_GODSWORD, DRAGON_CLAWS);
        }
        // 10% chance to use armadyl chainskirt
        if (randomizerContext.getRandom().nextInt(10) == 1) {
            randomizerContext.swapAll(VERACS_PLATESKIRT_100, ARMADYL_CHAINSKIRT);
        }
        // 10% chance to use karils top
        if (randomizerContext.getRandom().nextInt(10) == 1) {
            randomizerContext.swapAll(ARMADYL_CHESTPLATE, KARILS_LEATHERTOP_100);
        }
        // 10% chance to use karils bottom
        if (randomizerContext.getRandom().nextInt(10) == 1) {
            randomizerContext.swapAll(ARMADYL_CHESTPLATE, KARILS_LEATHERSKIRT_100);
        }
        // 10% chance to use nightmare staff
        if (randomizerContext.getRandom().nextInt(10) == 1) {
            randomizerContext.swapMage(TOXIC_STAFF_OF_THE_DEAD, VOLATILE_NIGHTMARE_STAFF);
        }
        // 10% chance to use staff of light
        if (randomizerContext.getRandom().nextInt(10) == 1) {
            randomizerContext.swapMage(TOXIC_STAFF_OF_THE_DEAD, STAFF_OF_LIGHT);
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
