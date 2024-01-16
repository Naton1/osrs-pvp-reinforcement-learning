package com.elvarg.net.packet.impl;

import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.net.packet.Packet;
import com.elvarg.net.packet.PacketExecutor;


public class CreationMenuPacketListener implements PacketExecutor {

    @Override
    public void execute(Player player, Packet packet) {
        int itemId = packet.readInt();
        int amount = packet.readUnsignedByte();
        if (player.getCreationMenu() != null) {
            player.getCreationMenu().execute(itemId, amount);
        }
    }
}
