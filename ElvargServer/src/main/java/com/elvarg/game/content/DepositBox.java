package com.elvarg.game.content;

import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.Item;
import com.elvarg.game.model.container.impl.Bank;

/**
 * @author Ynneh | 19/08/2022 - 17:50
 * <https://github.com/drhenny>
 */
public class DepositBox {

    /**
     * Deposit Box Interface ID
     */
    private static final int INTERFACE_ID = 4465;

    /**
     * Opens the Deposit Box Interface
     * @param player
     */
    public static void open(Player player) {
        player.getPacketSender().sendInterface(INTERFACE_ID);
        refresh(player);
    }

    /**
     * Refreshes container/inter upon request
     * @param player
     */
    private static void refresh(Player player) {
        player.getPacketSender().clearItemOnInterface(7423);
        player.getPacketSender().sendItemContainer(player.getInventory(), 7423);
        player.getPacketSender().sendInterfaceSet(4465, 192);
    }

    public static void deposit(Player player, int slotId, int itemId, int amount) {
        /** Item in requested Slot **/
        Item inventoryItem = player.getInventory().forSlot(slotId);

        if (inventoryItem == null) {
            /** No Item for slot **/
            return;
        }

        /** When depositing over your current item amount **/
        if (player.getInventory().getAmount(itemId) < amount) {
            amount = player.getInventory().getAmount(itemId);
        }

        final int item_for_slot = inventoryItem.getId();

        if (item_for_slot != itemId) {
            /** Contains a different itemId **/
            System.err.println("different item. invItem="+item_for_slot+" itemIdOnInter="+itemId);
            return;
        }

        /** If containers 0 amount of the item **/
        if (amount <= 0)
            return;

        /** Adds item to the bank **/
        Bank.deposit(player, itemId, slotId, amount);
        /** Refreshes interface / Inventory **/
        refresh(player);
    }
}
