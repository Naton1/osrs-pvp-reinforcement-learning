package com.elvarg.net.packet.impl;

import com.elvarg.game.content.Dueling.DuelRule;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.Location;
import com.elvarg.game.model.movement.path.PathFinder;
import com.elvarg.game.model.movement.path.RS317PathFinder;
import com.elvarg.game.task.TaskManager;
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
        player.getMovementQueue().resetFollow();
        player.setCombatFollowing(null);
        player.setFollowing(null);
        player.setMobileInteraction(null);

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

        int absoluteX = packet.readShort();
        int absoluteY = packet.readShort();
        int plane = packet.readUnsignedByte();

        int distance = player.getLocation().getDistance(new Location(absoluteX, absoluteY));

        if (distance > 25) {
            /** Shouldn't be possible **/
            return;
        }

        if (plane < 0) {
            /** should never happen unless spoofed packets lol **/
            return;
        }

        if (player.getLocation().getZ() != plane) {
            /** Height check **/
            return;
        }

        if (absoluteX > Short.MAX_VALUE || absoluteX < 0 || absoluteY > Short.MAX_VALUE || absoluteY < 0) {
            /**
             * Will never be below 0.
             * Cannot be bigger than unsigned byte.max_value
             */
            return;
        }


        player.getMovementQueue().reset();

        PathFinder.calculateWalkRoute(player, absoluteX, absoluteY);
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
