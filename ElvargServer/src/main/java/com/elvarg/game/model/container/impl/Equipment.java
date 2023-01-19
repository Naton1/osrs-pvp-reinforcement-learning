package com.elvarg.game.model.container.impl;

import com.elvarg.game.content.combat.WeaponInterfaces;
import com.elvarg.game.definition.ItemDefinition;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.Item;
import com.elvarg.game.model.container.ItemContainer;
import com.elvarg.game.model.container.StackType;

/**
 * Represents a player's equipment item container.
 *
 * @author Gabriel Hannason
 */

public class Equipment extends ItemContainer {

    /**
     * The equipment inventory interface id.
     */
    public static final int INVENTORY_INTERFACE_ID = 1688;
    /**
     * The equipment screen interface id
     */
    public static final int EQUIPMENT_SCREEN_INTERFACE_ID = 15106;
    /**
     * The helmet slot.
     */
    public static final int HEAD_SLOT = 0;
    /**
     * The cape slot.
     */
    public static final int CAPE_SLOT = 1;
    /**
     * The amulet slot.
     */
    public static final int AMULET_SLOT = 2;
    /**
     * The weapon slot.
     */
    public static final int WEAPON_SLOT = 3;
    /**
     * The chest slot.
     */
    public static final int BODY_SLOT = 4;
    /**
     * The shield slot.
     */
    public static final int SHIELD_SLOT = 5;
    /**
     * The bottoms slot.
     */
    public static final int LEG_SLOT = 7;
    /**
     * The gloves slot.
     */
    public static final int HANDS_SLOT = 9;
    /**
     * The boots slot.
     */
    public static final int FEET_SLOT = 10;
    /**
     * The rings slot.
     */
    public static final int RING_SLOT = 12;
    /**
     * The arrows slot.
     */
    public static final int AMMUNITION_SLOT = 13;
    /**
     * Item which can be reused to unequip items from any slot.
     */
    public static final Item NO_ITEM = new Item(-1);

    /**
     * The Equipment constructor.
     *
     * @param player The player who's equipment is being represented.
     */
    public Equipment(Player player) {
        super(player);
    }

    /**
     * Gets the amount of item of a type a player has, for example, gets how many Zamorak items a player is wearing for GWD
     *
     * @param p The player
     * @param s The item type to search for
     * @return The amount of item with the type that was found
     */
    public static int getItemCount(Player p, String s, boolean inventory) {
        int count = 0;
        for (Item t : p.getEquipment().getItems()) {
            if (t == null || t.getId() < 1 || t.getAmount() < 1)
                continue;
            if (t.getDefinition().getName().toLowerCase().contains(s.toLowerCase()))
                count++;
        }
        if (inventory) {
            for (Item t : p.getInventory().getItems()) {
                if (t == null || t.getId() < 1 || t.getAmount() < 1)
                    continue;
                if (t.getDefinition().getName().toLowerCase().contains(s.toLowerCase()))
                    count++;
            }
        }
        return count;
    }

    @Override
    public int capacity() {
        return 14;
    }

    @Override
    public StackType stackType() {
        return StackType.DEFAULT;
    }

    @Override
    public ItemContainer refreshItems() {
        getPlayer().getPacketSender().sendItemContainer(this, INVENTORY_INTERFACE_ID);
        return this;
    }

    @Override
    public Equipment full() {
        return this;
    }

    public boolean wearingNexAmours() {
        int head = getPlayer().getEquipment().getItems()[HEAD_SLOT].getId();
        int body = getPlayer().getEquipment().getItems()[BODY_SLOT].getId();
        int legs = getPlayer().getEquipment().getItems()[LEG_SLOT].getId();
        boolean torva = head == 14008 && body == 14009 && legs == 14010;
        boolean pernix = head == 14011 && body == 14012 && legs == 14013;
        boolean virtus = head == 14014 && body == 14015 && legs == 14016;
        return torva || pernix || virtus;
    }

    public boolean wearingHalberd() {
        ItemDefinition def = ItemDefinition.forId(getPlayer().getEquipment().getItems()[Equipment.WEAPON_SLOT].getId());
        return def != null && def.getName().toLowerCase().endsWith("halberd");
    }

    public boolean properEquipmentForWilderness() {
        int count = 0;
        for (Item item : getValidItems()) {
            if (item != null && item.getDefinition().isTradeable())
                count++;
        }
        return count >= 3;
    }

    public boolean hasStaffEquipped() {
        Item staff = get(Equipment.WEAPON_SLOT);
        return (staff != null && (getPlayer().getWeapon() == WeaponInterfaces.WeaponInterface.STAFF
                || getPlayer().getWeapon() == WeaponInterfaces.WeaponInterface.ANCIENT_STAFF));
    }

    public Item getWeapon() {
        return get(Equipment.WEAPON_SLOT);
    }

    public boolean hasCastleWarsBracelet() {
        Item hands = get(HANDS_SLOT);
        return hands != null && hands.getId() >= 11079 && hands.getId() <= 11083;
    }

    public boolean hasGodsword() {
        return get(WEAPON_SLOT) != null && get(WEAPON_SLOT).getDefinition().getName().toLowerCase().contains("godsword");
    }
}
