package com.elvarg.net.packet.impl;

import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.Location;
import com.elvarg.game.model.movement.MovementQueue.Mobility;
import com.elvarg.game.model.movement.path.PathFinder;
import com.elvarg.net.packet.Packet;
import com.elvarg.net.packet.PacketExecutor;

/**
 * This packet listener is called when a player has clicked on either the
 * mini-map or the actual game map to move around.
 *
 * @author Gabriel Hannason
 */
public class MovementPacketListener implements PacketExecutor {

    private static final int FLOATING_WORLD_MAP_INTERFACE = 54000;

    @Override
    public void execute(Player player, Packet packet) {
        if (player.getHitpoints() <= 0) {
            return;
        }

        Mobility mobility = player.getMovementQueue().getMobility();
        if (!mobility.canMove()) {
            mobility.sendMessage(player);
            return;
        }

        int absoluteX = packet.readShort();
        int absoluteY = packet.readShort();
        int plane = packet.readUnsignedByte();

        Location destination = new Location(absoluteX, absoluteY, plane);

        if (!player.getMovementQueue().checkDestination(destination)) {
            return;
        }

        if (player.getInterfaceId() != FLOATING_WORLD_MAP_INTERFACE) {
            // Close all interfaces except for floating world map
            player.getPacketSender().sendInterfaceRemoval();
        }

        // Make sure to reset any previous movement steps
        player.getMovementQueue().reset();

        player.getMovementQueue().walkToReset();

        PathFinder.calculateWalkRoute(player, absoluteX, absoluteY);
    }

}
