package com.elvarg.net.packet.impl;

import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.commands.Command;
import com.elvarg.game.model.commands.CommandManager;
import com.elvarg.net.packet.Packet;
import com.elvarg.net.packet.PacketExecutor;

/**
 * This packet listener manages commands a player uses by using the command
 * console prompted by using the "`" char.
 *
 * @author Gabriel Hannason
 */
public class CommandPacketListener implements PacketExecutor {

    public static final int OP_CODE = 103;

    @Override
    public void execute(Player player, Packet packet) {
        if (player.getHitpoints() <= 0) {
            return;
        }
        String command = packet.readString();
        String[] parts = command.split(" ");
        parts[0] = parts[0].toLowerCase();

        Command c = CommandManager.commands.get(parts[0]);
        if (c != null) {

            if (c.canUse(player)) {
                c.execute(player, command, parts);
            } else {
            }

        } else {
            player.getPacketSender().sendMessage("This command does not exist.");
        }

    }
}
