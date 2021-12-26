package com.elvarg.net.packet.impl;

import com.elvarg.game.World;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.movement.WalkToAction;
import com.elvarg.net.packet.Packet;
import com.elvarg.net.packet.PacketConstants;
import com.elvarg.net.packet.PacketExecutor;


/**
 * This packet listener is called when a player has clicked on another player's
 * menu actions.
 *
 * @author relex lawl
 */

public class PlayerOptionPacketListener implements PacketExecutor {


    private static void attack(Player player, Packet packet) {
        int index = packet.readLEShort();
        if (index > World.getPlayers().capacity() || index < 0)
            return;
        final Player attacked = World.getPlayers().get(index);

        if (attacked == null || attacked.getHitpoints() <= 0 || attacked.equals(player)) {
            player.getMovementQueue().reset();
            return;
        }

        player.getCombat().attack(attacked);
    }

    /**
     * Manages the first option click on a player option menu.
     *
     * @param player The player clicking the other entity.
     * @param packet The packet to read values from.
     */
    private static void option1(Player player, Packet packet) {
        int id = packet.readShort() & 0xFFFF;
        if (id < 0 || id > World.getPlayers().capacity())
            return;
        Player player2 = World.getPlayers().get(id);
        if (player2 == null)
            return;
        player.setFollowing(player2);
        player.setWalkToTask(new WalkToAction(player) {
            @Override
            public void execute() {
                if (player.getArea() != null) {
                    player.getArea().onPlayerRightClick(player, player2, 1);
                }
            }
            
            @Override
            public boolean inDistance() {
                return player.calculateDistance(player2) == 1;
            }
        });
    }

    /**
     * Manages the second option click on a player option menu.
     *
     * @param player The player clicking the other entity.
     * @param packet The packet to read values from.
     */
    private static void option2(Player player, Packet packet) {
        int id = packet.readShort() & 0xFFFF;
        if (id < 0 || id > World.getPlayers().capacity())
            return;
        Player player2 = World.getPlayers().get(id);
        if (player2 == null)
            return;
        player.setFollowing(player2);
        player.setWalkToTask(new WalkToAction(player) {
            @Override
            public void execute() {
                if (player.getArea() != null) {
                    player.getArea().onPlayerRightClick(player, player2, 2);
                }
            }
            
            @Override
            public boolean inDistance() {
                return player.calculateDistance(player2) == 1;
            }
        });
    }

    /**
     * Manages the third option click on a player option menu.
     *
     * @param player The player clicking the other entity.
     * @param packet The packet to read values from.
     */
    private static void option3(Player player, Packet packet) {
        int id = packet.readLEShortA() & 0xFFFF;
        if (id < 0 || id > World.getPlayers().capacity())
            return;
        Player player2 = World.getPlayers().get(id);
        if (player2 == null)
            return;
        player.setFollowing(player2);
        player.setWalkToTask(new WalkToAction(player) {
            @Override
            public void execute() {
                if (player.getArea() != null) {
                    player.getArea().onPlayerRightClick(player, player2, 3);
                }
            }
            @Override
            public boolean inDistance() {
                return player.calculateDistance(player2) == 1;
            }
        });
    }

    @Override
    public void execute(Player player, Packet packet) {

        if (player == null || player.getHitpoints() <= 0) {
            return;
        }

        if (player.busy()) {
            return;
        }
        switch (packet.getOpcode()) {
            case PacketConstants.ATTACK_PLAYER_OPCODE:
                attack(player, packet);
                break;
            case PacketConstants.PLAYER_OPTION_1_OPCODE:
                option1(player, packet);
                break;
            case PacketConstants.PLAYER_OPTION_2_OPCODE:
                option2(player, packet);
                break;
            case PacketConstants.PLAYER_OPTION_3_OPCODE:
                option3(player, packet);
                break;
        }
    }
}
