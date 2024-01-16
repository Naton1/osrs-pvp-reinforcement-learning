package com.elvarg.net.packet.impl;

import com.elvarg.game.content.sound.Sound;
import com.elvarg.game.content.sound.SoundManager;
import com.elvarg.game.content.PetHandler;
import com.elvarg.game.entity.impl.grounditem.ItemOnGroundManager;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.Item;
import com.elvarg.game.model.areas.impl.WildernessArea;
import com.elvarg.game.model.container.impl.Inventory;
import com.elvarg.game.model.rights.PlayerRights;
import com.elvarg.net.packet.Packet;
import com.elvarg.net.packet.PacketExecutor;

/**
 * This packet listener is called when a player drops an item they have placed
 * in their inventory.
 *
 * @author relex lawl
 */

public class DropItemPacketListener implements PacketExecutor {

    public static void destroyItemInterface(Player player, Item item) {// Destroy item created by Remco
        player.setDestroyItem(item.getId());
        String[][] info = { // The info the dialogue gives
                {"Are you sure you want to discard this item?", "14174"}, {"Yes.", "14175"}, {"No.", "14176"},
                {"", "14177"}, {"This item will vanish once it hits the floor.", "14182"},
                {"You cannot get it back if discarded.", "14183"}, {item.getDefinition().getName(), "14184"}};
        player.getPacketSender().sendItemOnInterface(14171, item.getId(), 0, item.getAmount());
        for (int i = 0; i < info.length; i++)
            player.getPacketSender().sendString(Integer.parseInt(info[i][1]), info[i][0]);
        player.getPacketSender().sendChatboxInterface(14170);
    }

    @Override
    public void execute(Player player, Packet packet) {
        int id = packet.readUnsignedShortA();
        int interface_id = packet.readUnsignedShort();
        int itemSlot = packet.readUnsignedShortA();

        if (player == null || player.getHitpoints() <= 0) {
            return;
        }

        if (interface_id != Inventory.INTERFACE_ID) {
            return;
        }

        if (player.getHitpoints() <= 0)
            return;
        
        if (itemSlot < 0 || itemSlot >= player.getInventory().capacity())
            return;

        if (player.busy()) {
        	player.getPacketSender().sendInterfaceRemoval();
        }

        Item item = player.getInventory().getItems()[itemSlot];
        if (item == null)
            return;
        if (item.getId() != id || item.getAmount() <= 0) {
            return;
        }

        if (player.getRights() == PlayerRights.DEVELOPER) {
            player.getPacketSender().sendMessage("Drop item: " + Integer.toString(item.getId()) + ".");
        }

        player.getPacketSender().sendInterfaceRemoval();

        // Stop skilling..
        player.getSkillManager().stopSkillable();

        // Check if we're dropping a pet..
        if (PetHandler.drop(player, id, false)) {
            SoundManager.sendSound(player, Sound.DROP_ITEM);
            return;
        }

        if (item.getDefinition().isDropable()) {
        	/** Overrideables for untradables ect **/
            Item toFloor = new Item(item.getIdOnDropOrDeath(), item.getAmount());

        	// Items dropped in the Wilderness should go global immediately.
            if (player.getArea() instanceof WildernessArea) {
            	ItemOnGroundManager.registerGlobal(player, toFloor);
            } else {
            	ItemOnGroundManager.register(player, toFloor);
            }
            
            player.getInventory().setItem(itemSlot, new Item(-1, 0)).refreshItems();

            SoundManager.sendSound(player, Sound.DROP_ITEM);
        } else {
            destroyItemInterface(player, item);
        }
    }
}
