package com.elvarg.net.packet.impl;

import com.elvarg.game.World;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.net.packet.Packet;
import com.elvarg.net.packet.PacketConstants;
import com.elvarg.net.packet.PacketExecutor;
import com.elvarg.util.Misc;

import java.util.Optional;

/**
 * This packet listener is called when a player is doing something relative to
 * their friends or ignore list, such as adding or deleting a player from said
 * list.
 *
 * @author relex lawl
 */

public class PlayerRelationPacketListener implements PacketExecutor {

	@Override
	public void execute(Player player, Packet packet) {
		try {

			long username = packet.readLong();
			if (username < 0) {
				return;
			}

			switch (packet.getOpcode()) {
			case PacketConstants.ADD_FRIEND_OPCODE:
				player.getRelations().addFriend(username);
				break;
			case PacketConstants.ADD_IGNORE_OPCODE:
				player.getRelations().addIgnore(username);
				break;
			case PacketConstants.REMOVE_FRIEND_OPCODE:
				player.getRelations().deleteFriend(username);
				break;
			case PacketConstants.REMOVE_IGNORE_OPCODE:
				player.getRelations().deleteIgnore(username);
				break;
			case PacketConstants.SEND_PM_OPCODE:
			    int size = packet.getSize();
                byte[] message = packet.readBytes(size);
                Optional<Player> friend = World.getPlayerByName(Misc.formatText(Misc.longToString(username)).replaceAll("_", " "));
                if (friend.isPresent()) {
                    player.getRelations().message(friend.get(), message, size);
                } else {
                    player.getPacketSender().sendMessage("That player is offline.");
                }
				break;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
