package com.elvarg.net.packet.impl;

import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.Location;
import com.elvarg.game.model.rights.PlayerRights;
import com.elvarg.game.model.teleportation.TeleportHandler;
import com.elvarg.game.model.teleportation.Teleportable;
import com.elvarg.net.packet.Packet;
import com.elvarg.net.packet.PacketExecutor;

/**
 * This packet listener handles the action when pressing a teleport menu in the
 * chatbox teleport interface.
 *
 * @author Professor Oak
 */

public class TeleportPacketListener implements PacketExecutor {

	@Override
	public void execute(Player player, Packet packet) {
		if (player.getHitpoints() <= 0)
			return;
		int type = packet.readByte();
		int index = packet.readByte();

		if (!player.isTeleportInterfaceOpen()) {
			player.getPacketSender().sendInterfaceRemoval();
			return;
		}

		if (player.getRights() == PlayerRights.DEVELOPER) {
			player.getPacketSender().sendMessage(
					"Selected a teleport. Type: " + Integer.toString(type) + ", index: " + Integer.toString(index) + ".");
		}

		for (Teleportable teleport : Teleportable.values()) {
			if (teleport.getType() == type && teleport.getIndex() == index) {
				Location teleportPosition = teleport.getPosition();
				if (TeleportHandler.checkReqs(player, teleportPosition)) {
					player.getPreviousTeleports().put(teleport.getTeleportButton(), teleportPosition);
					TeleportHandler.teleport(player, teleportPosition, player.getSpellbook().getTeleportType(), true);
				}
				break;
			}
		}
	}
}
