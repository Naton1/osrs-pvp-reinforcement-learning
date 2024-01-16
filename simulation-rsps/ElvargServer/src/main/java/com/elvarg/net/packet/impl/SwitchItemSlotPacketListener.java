package com.elvarg.net.packet.impl;

import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.container.impl.Bank;
import com.elvarg.game.model.container.impl.Inventory;
import com.elvarg.net.packet.Packet;
import com.elvarg.net.packet.PacketExecutor;

/**
 * This packet listener is called when an item is dragged onto another slot.
 *
 * @author relex lawl
 */

public class SwitchItemSlotPacketListener implements PacketExecutor {

    @Override
    public void execute(Player player, Packet packet) {
        if (player.getHitpoints() <= 0)
            return;
        int interfaceId = packet.readInt();
        packet.readByteC();
        int fromSlot = packet.readLEShortA();
        int toSlot = packet.readLEShort();

        if (player == null || player.getHitpoints() <= 0) {
            return;
        }

        //Bank..
        if (interfaceId >= Bank.CONTAINER_START && interfaceId < Bank.CONTAINER_START + Bank.TOTAL_BANK_TABS) {

            final int tab = player.isSearchingBank() ? Bank.BANK_SEARCH_TAB_INDEX : interfaceId - Bank.CONTAINER_START;

            if (fromSlot >= 0 && fromSlot < player.getBank(tab).capacity() && toSlot >= 0 && toSlot < player.getBank(tab).capacity() && toSlot != fromSlot) {
                Bank.rearrange(player, player.getBank(tab), fromSlot, toSlot);
            }

            return;
        }

        switch (interfaceId) {
            case Inventory.INTERFACE_ID:
            case Bank.INVENTORY_INTERFACE_ID:
                if (fromSlot >= 0 && fromSlot < player.getInventory().capacity() && toSlot >= 0 && toSlot < player.getInventory().capacity() && toSlot != fromSlot) {
                    player.getInventory().swap(fromSlot, toSlot).refreshItems();
                }
                break;
        }
    }
}
