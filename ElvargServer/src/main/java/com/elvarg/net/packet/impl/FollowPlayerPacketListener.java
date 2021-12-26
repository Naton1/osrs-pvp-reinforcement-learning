package com.elvarg.net.packet.impl;

import com.elvarg.game.World;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.net.packet.Packet;
import com.elvarg.net.packet.PacketExecutor;

/**
 * Handles the follow player packet listener Sets the player to follow when the
 * packet is executed
 *
 * @author Gabriel Hannason
 */
public class FollowPlayerPacketListener implements PacketExecutor {

    @Override
    public void execute(Player player, Packet packet) {

        if (player.busy()) {
            return;
        }

        int otherPlayersIndex = packet.readLEShort();
        if (otherPlayersIndex < 0 || otherPlayersIndex > World.getPlayers().capacity())
            return;
        Player leader = World.getPlayers().get(otherPlayersIndex);
        if (leader == null)
            return;
        player.setMobileInteraction(leader);
        player.setFollowing(leader);
    }

}
