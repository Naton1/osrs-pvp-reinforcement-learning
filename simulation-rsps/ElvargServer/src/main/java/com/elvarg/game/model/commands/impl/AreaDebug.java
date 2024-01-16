package com.elvarg.game.model.commands.impl;

import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.commands.Command;
import com.elvarg.game.model.rights.PlayerRights;

public class AreaDebug implements Command {

	@Override
	public void execute(Player player, String command, String[] parts) {
		if (player.getArea() != null) {
			player.getPacketSender().sendMessage("");
			player.getPacketSender().sendMessage("Area: " + player.getArea().getClass().getName());
			// player.getPacketSender().sendMessage("Players in this area: " +
			// player.getArea().players.size() +", npcs in this area:
			// "+player.getArea().npcs.size());
		} else {
			player.getPacketSender().sendMessage("No area found for your coordinates.");
		}
	}

	@Override
	public boolean canUse(Player player) {
		PlayerRights rights = player.getRights();
		return (rights == PlayerRights.OWNER || rights == PlayerRights.DEVELOPER);
	}

}
