package com.elvarg.net.packet.impl;

import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.Item;
import com.elvarg.game.model.PlayerStatus;
import com.elvarg.game.model.container.impl.Bank;
import com.elvarg.net.packet.Packet;
import com.elvarg.net.packet.PacketExecutor;

/**
 * This packet listener is called when an item is dragged onto a bank tab.
 *
 * @author Professor Oak
 */

public class BankTabCreationPacketListener implements PacketExecutor {

    @Override
    public void execute(Player player, Packet packet) {
        int interfaceId = packet.readInt();
        int fromSlot = packet.readShort();
        int to_tab = packet.readLEShort();

        int fromBankTab = interfaceId - Bank.CONTAINER_START;
        if (fromBankTab >= 0 && fromBankTab < Bank.TOTAL_BANK_TABS) {
            if (player.getStatus() == PlayerStatus.BANKING && player.getInterfaceId() == 5292) {

                if (player.isSearchingBank()) {
                    fromBankTab = Bank.BANK_SEARCH_TAB_INDEX;
                }

                Item item = player.getBank(fromBankTab).getItems()[fromSlot].clone();
                if (fromBankTab != Bank.getTabForItem(player, item.getId())) {
                    return;
                }

                //Let's move the item to the new tab
                int slot = player.getBank(fromBankTab).getSlotForItemId(item.getId());
                if (slot != fromSlot) {
                    return;
                }

                //Temporarily disable note whilst we do switch
                final boolean note = player.withdrawAsNote();
                player.setNoteWithdrawal(false);

                //Make the item switch
                player.getBank(fromBankTab).switchItem(player.getBank(to_tab), item, slot,
                        false, false);

                //Re-set the note var
                player.setNoteWithdrawal(note);

                //Update all tabs
                Bank.reconfigureTabs(player);

                //Refresh items in our current tab
                if (!player.isSearchingBank()) {
                    player.getBank(player.getCurrentBankTab()).refreshItems();
                }
            }
        }
    }
}
