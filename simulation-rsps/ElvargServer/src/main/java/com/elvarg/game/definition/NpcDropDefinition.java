package com.elvarg.game.definition;

import com.elvarg.game.model.Item;
import com.elvarg.util.RandomGen;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Handles drop definitions.
 *
 * @author Professor Oak
 */
public class NpcDropDefinition {

    /**
     * The map containing all our {@link NpcDropDefinition}s.
     */
    public static Map<Integer, NpcDropDefinition> definitions = new HashMap<Integer, NpcDropDefinition>();
    /**
     * The npcs which share this {@link NpcDropDefinition}.
     */
    private int[] npcIds;
    /**
     * The chance for this {@link NpcDropDefinition} to hit the
     * rare drop table.
     */
    private int rdtChance;
    /**
     * The possible drop tables for this {@link NpcDropDefinition}.
     */
    private NPCDrop[] alwaysDrops;
    private NPCDrop[] commonDrops;
    private NPCDrop[] uncommonDrops;
    private NPCDrop[] rareDrops;
    private NPCDrop[] veryRareDrops;
    private NPCDrop[] specialDrops;

    /**
     * Gets the {@link NpcDropDefinition} for the specified npc id.
     *
     * @param npcId
     * @return
     */
    public static Optional<NpcDropDefinition> get(int npcId) {
        NpcDropDefinition drop = definitions.get(npcId);
        if (drop != null) {
            return Optional.of(drop);
        }
        return Optional.empty();
    }

    public int[] getNpcIds() {
        return npcIds;
    }

    public int getRdtChance() {
        return rdtChance;
    }

    public NPCDrop[] getAlwaysDrops() {
        return alwaysDrops;
    }

    public NPCDrop[] getCommonDrops() {
        return commonDrops;
    }

    public NPCDrop[] getUncommonDrops() {
        return uncommonDrops;
    }

    public NPCDrop[] getRareDrops() {
        return rareDrops;
    }

    public NPCDrop[] getVeryRareDrops() {
        return veryRareDrops;
    }

    public NPCDrop[] getSpecialDrops() {
        return specialDrops;
    }

    /**
     * Represents a drop table and the random
     * required to hit it.
     */
    public enum DropTable {
        COMMON(90),
        UNCOMMON(40),
        RARE(6),
        VERY_RARE(0.6),
        SPECIAL(-1), //Separately handled
        ;

        private final double randomRequired;

        DropTable(double randomRequired) {
            this.randomRequired = randomRequired;
        }

        public double getRandomRequired() {
            return randomRequired;
        }
    }

    public static enum RDT {
        LAW_RUNE(563, 45, 64),
        DEATH_RUNE(560, 45, 64),
        NATURE_RUNE(561, 67, 43),
        STEEL_ARROW(886, 150, 64),
        RUNE_ARROW(886, 42, 64),
        UNCUT_SAPPHIRE(1623, 1, 1),
        UNCUT_EMERALD(1621, 1, 20),
        UNCUT_RUBY(1619, 1, 20),
        UNCUT_DIAMOND(1617, 1, 64),
        DRAGONSTONE(1631, 1, 64),
        RUNITE_BAR(2363, 1, 20),
        SILVER_ORE(443, 100, 64),
        COINS(995, 3000, 1),
        CHAOS_TALISMAN(1452, 1, 1),
        NATURE_TALISMAN(1462, 1, 20),
        LOOP_HALF_OF_KEY(987, 6, 1),
        TOOTH_HALF_OF_KEY(985, 6, 1),
        ADAMANT_JAVELIN(829, 20, 64),
        RUNE_JAVELIN(830, 5, 33),
        RUNE_2H_SWORD(1319, 1, 43),
        RUNE_BATTLEAXE(1373, 1, 43),
        RUNE_SQUARE_SHIELD(1185, 1, 64),
        RUNE_KITE_SHIELD(1201, 1, 128),
        DRAGON_MED_HELM(1149, 1, 128),
        RUNE_SPEAR(1247, 1, 137),
        SHIELD_LEFT_HALF(2366, 1, 273),
        DRAGON_SPEAR(1249, 1, 364);

        private final int itemId;
        private final int amount;
        private final int chance;

        private RDT(int itemId, int amount, int chance) {
            this.itemId = itemId;
            this.amount = amount;
            this.chance = chance;
        }

        public int getItemId() {
            return itemId;
        }

        public int getAmount() {
            return amount;
        }

        public int getChance() {
            return chance;
        }
    }

    public static final class NPCDrop {
        /**
         * The in-game item id of this drop.
         */
        private final int itemId;

        /**
         * The minimum amount of the item that will be dropped.
         */
        private final int minAmount;

        /**
         * The maximum amount of the item that will be dropped.
         */
        private final int maxAmount;

        /**
         * The chance that this item will be dropped.
         */
        private final int chance;

        public NPCDrop(int itemId, int minAmount, int maxAmount) {
            this.itemId = itemId;
            this.minAmount = minAmount;
            this.maxAmount = maxAmount;
            this.chance = -1;
        }

        public NPCDrop(int itemId, int minAmount, int maxAmount, int chance) {
            this.itemId = itemId;
            this.minAmount = minAmount;
            this.maxAmount = maxAmount;
            this.chance = chance;
        }

        public int getItemId() {
            return itemId;
        }

        public int getMinAmount() {
            return minAmount;
        }

        public int getMaxAmount() {
            return maxAmount;
        }

        public Item toItem(RandomGen random) {
            return new Item(itemId, random.inclusive(minAmount, maxAmount));
        }

        public int getChance() {
            return chance;
        }
    }
}
