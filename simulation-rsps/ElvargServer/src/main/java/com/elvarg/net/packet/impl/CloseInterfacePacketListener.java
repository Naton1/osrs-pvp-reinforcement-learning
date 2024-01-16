package com.elvarg.net.packet.impl;

import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.net.packet.Packet;
import com.elvarg.net.packet.PacketExecutor;

public class CloseInterfacePacketListener implements PacketExecutor {

    @Override
    public void execute(Player player, Packet packet) {
        player.getPacketSender().sendInterfaceRemoval();
    }
}
