package com.elvarg.net.packet.impl;

import com.elvarg.game.content.clan.ClanChatManager;
import com.elvarg.game.content.presets.Presetables;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.container.impl.Bank;
import com.elvarg.game.model.teleportation.TeleportHandler;
import com.elvarg.net.packet.Packet;
import com.elvarg.net.packet.PacketExecutor;

public class InterfaceActionClickOpcode implements PacketExecutor {

	@Override
	public void execute(Player player, Packet packet) {
		int interfaceId = packet.readInt();
		int action = packet.readByte();

		if (player == null || player.getHitpoints() <= 0
				|| player.isTeleporting()) {
			return;
		}

		if (Bank.handleButton(player, interfaceId, action)) {
			return;
		}
		
		if (ClanChatManager.handleButton(player, interfaceId, action)) {
			return;
		}
		
		if (Presetables.handleButton(player, interfaceId)) {
			return;
		}
		
		if (TeleportHandler.handleButton(player, interfaceId, action)) {
			return;
		}
	}
}
