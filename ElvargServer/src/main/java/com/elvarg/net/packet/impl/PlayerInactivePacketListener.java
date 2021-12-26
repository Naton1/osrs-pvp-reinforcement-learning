package com.elvarg.net.packet.impl;

import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.net.packet.Packet;
import com.elvarg.net.packet.PacketExecutor;

public class PlayerInactivePacketListener implements PacketExecutor {

    //CALLED EVERY 3 MINUTES OF INACTIVITY

    @Override
    public void execute(Player player, Packet packet) {
    }
}
