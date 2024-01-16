package com.elvarg.game.model;

import java.util.HashMap;
import java.util.Map;

import com.elvarg.game.definition.ItemDefinition;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.util.ItemIdentifiers;

public enum BrokenItem {

    DRAGON_DEFENDER_BROKEN(12954, 20463),
    AVERNIC_DEFENDER_BROKEN(ItemIdentifiers.AVERNIC_DEFENDER, ItemIdentifiers.AVERNIC_DEFENDER_BROKEN_),
    FIRE_CAPE_BROKEN(6570, 20445),
    INFERNAL_CAPE_BROKEN(21295, 21287),
    FIGHTER_TORSO_BROKEN(10551, 20513),

    VOID_KNIGHT_TOP(8839, 20465),
    VOID_KNIGHT_ROBE(8840, 20469),
    VOID_KNIGHT_GLOVES(8842, 20475),
    VOID_KNIGHT_MAGE_HELM(11663, 20477),
    VOID_KNIGHT_RANGER_HELM(11664, 20479),
    VOID_KNIGHT_MELEE_HELM(11665, 20481),;

    //Original item value * this multiplier is the repair cost of all items.
    //Currently 3%
    private static final double REPAIR_COST_MULTIPLIER = 0.03;
    private static Map<Integer, BrokenItem> brokenItems = new HashMap<Integer, BrokenItem>();

    static {
        for (BrokenItem brokenItem : BrokenItem.values()) {
            brokenItems.put(brokenItem.getOriginalItem(), brokenItem);
        }
    }

    private final int originalItem;
    private final int brokenItem;

    BrokenItem(int originalItem, int brokenItem) {
        this.originalItem = originalItem;
        this.brokenItem = brokenItem;
    }

    /**
     * Gets the total cost of repairing a player's stuff.
     *
     * @param player
     * @param deleteEmblems
     * @return
     */
    public static int getRepairCost(Player player) {
        int cost = 0;
        for (BrokenItem b : BrokenItem.values()) {
            final int amt = player.getInventory().getAmount(b.getBrokenItem());
            if (amt > 0) {
                cost += ((int) (ItemDefinition.forId(b.getOriginalItem()).getBloodMoneyValue() * REPAIR_COST_MULTIPLIER) * amt);
            }
        }
        return cost;
    }

    /**
     * Gets amount of money player would need to pay if he dies
     *
     * @param player
     * @param deleteEmblems
     * @return
     */
    public static int getValueLoseOnDeath(Player player) {
        int cost = 0;
        for (BrokenItem b : BrokenItem.values()) {
            final int amt = player.getInventory().getAmount(b.getOriginalItem());
            if (amt > 0) {
                cost += ((int) (ItemDefinition.forId(b.getOriginalItem()).getBloodMoneyValue() * REPAIR_COST_MULTIPLIER) * amt);
            }
            final int amtEq = player.getEquipment().getAmount(b.getOriginalItem());
            if (amtEq > 0) {
                cost += ((int) (ItemDefinition.forId(b.getOriginalItem()).getBloodMoneyValue() * REPAIR_COST_MULTIPLIER) * amt);
            }
        }
        return cost;
    }

    /**
     * Repairs all broken stuff for a player.
     *
     * @param player
     */
    public static boolean repair(Player player) {
        var fullCost = getRepairCost(player);

        if (fullCost > player.getInventory().getAmount(ItemIdentifiers.BLOOD_MONEY)) {
            return false;
        }

        for (BrokenItem b : BrokenItem.values()) {
            final int amt = player.getInventory().getAmount(b.getBrokenItem());
            if (amt > 0) {
                final int cost = ((int) (ItemDefinition.forId(b.getOriginalItem()).getBloodMoneyValue() * REPAIR_COST_MULTIPLIER) * amt);
                if (player.getInventory().getAmount(ItemIdentifiers.BLOOD_MONEY) >= cost) {
                    player.getInventory().delete(ItemIdentifiers.BLOOD_MONEY, cost);
                    player.getInventory().delete(b.getBrokenItem(), amt);
                    player.getInventory().add(b.getOriginalItem(), amt);
                } else {
                    return false;
                }
            }
        }

        return true;
    }

    public static BrokenItem get(int originalId) {
        return brokenItems.get(originalId);
    }

    public int getOriginalItem() {
        return originalItem;
    }

    public int getBrokenItem() {
        return brokenItem;
    }
}
