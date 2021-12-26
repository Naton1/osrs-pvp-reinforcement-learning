package com.elvarg.net.packet.impl;

import com.elvarg.game.content.Dueling.DuelRule;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.Location;
import com.elvarg.game.model.movement.path.RS317PathFinder;
import com.elvarg.net.packet.Packet;
import com.elvarg.net.packet.PacketConstants;
import com.elvarg.net.packet.PacketExecutor;
import com.elvarg.util.timers.TimerKey;

/**
 * This packet listener is called when a player has clicked on either the
 * mini-map or the actual game map to move around.
 *
 * @author Gabriel Hannason
 */
public class MovementPacketListener implements PacketExecutor {

    @Override
    public void execute(Player player, Packet packet) {

        if (player.getHitpoints() <= 0) {
            return;
        }

        player.getCombat().setCastSpell(null);
        player.getCombat().reset();
        player.getSkillManager().stopSkillable();
        player.setWalkToTask(null);

        if (packet.getOpcode() != PacketConstants.COMMAND_MOVEMENT_OPCODE) {

        }

        if (!checkReqs(player, packet.getOpcode())) {
            return;
        }

        // Close all interfaces except for the floating
        // world map.
        if (player.getInterfaceId() != 54000) {
            player.getPacketSender().sendInterfaceRemoval();
        }
        
        // exclude one byte for the plane
        int size = packet.getSize() - 1;
        /*if (packet.getOpcode() == 248) {
            size -= 14;
        }*/
        final int steps = (size - 5) / 2;
        
        if (steps < 0) {
            return;
        }
        
        final int plane = packet.readUnsignedByte();
        final int firstStepX = packet.readLEShortA();
        final int[][] path = new int[steps][2];
        for (int i = 0; i < steps; i++) {
            path[i][0] = packet.readByte();
            path[i][1] = packet.readByte();
        }
        final int firstStepY = packet.readLEShort();
        final Location[] positions = new Location[steps + 1];
        positions[0] = new Location(firstStepX, firstStepY, plane);
        for (int i = 0; i < steps; i++) {
            positions[i + 1] = new Location(path[i][0] + firstStepX, path[i][1] + firstStepY, plane);
        }
        final Location end = positions[positions.length - 1];

        if (end.getZ() != player.getLocation().getZ()) {
            return;
        }
        if (player.getLocation().getDistance(end) >= 64) {
            return;
        }        
        if (player.getLocation().equals(end)) {
            return;
        }        
        
        RS317PathFinder.findPath(player, end.getX(), end.getY(), false, 1, 1);
        /*

        // Add walking points to movement queue..
        if (player.getMovementQueue().addFirstStep(positions[0])) {
            for (int i = 1; i < positions.length; i++) {
                player.getMovementQueue().addStep(positions[i]);
            }
        }*/
    }

    public boolean checkReqs(Player player, int opcode) {
        if (player.getTimers().has(TimerKey.FREEZE)) {
        	player.getPacketSender().sendMessage("A magical spell has made you unable to move.");
            return false;
        }

        if (!player.getTrading().getButtonDelay().finished() || !player.getDueling().getButtonDelay().finished()) {
            player.getPacketSender().sendMessage("You cannot do that right now.");
            return false;
        }

        // Duel, disabled movement?
        if (player.getDueling().inDuel() && player.getDueling().getRules()[DuelRule.NO_MOVEMENT.ordinal()]) {
            if (opcode != PacketConstants.COMMAND_MOVEMENT_OPCODE) {
                //DialogueManager.sendStatement(player, "Movement has been disabled in this duel!");
            }
            return false;
        }

        // Stun
        if (player.getTimers().has(TimerKey.STUN)) {
            player.getPacketSender().sendMessage("You're stunned!");
            return false;
        }

        if (player.isNeedsPlacement() || player.getMovementQueue().isMovementBlocked()) {
            return false;
        }
        
        return true;
    }
}
