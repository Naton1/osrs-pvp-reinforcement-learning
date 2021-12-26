package com.elvarg.game.model.commands.impl;

import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.commands.Command;
import com.elvarg.game.model.rights.PlayerRights;
import com.elvarg.game.task.TaskManager;

public class TaskDebug implements Command {

	@Override
	public void execute(Player player, String command, String[] parts) {
		player.getPacketSender().sendMessage("Active tasks : " +  Integer.toString(TaskManager.getTaskAmount()) + ".");
	}

	@Override
	public boolean canUse(Player player) {
		PlayerRights rights = player.getRights();
		return (rights == PlayerRights.OWNER || rights == PlayerRights.DEVELOPER);
	}

}
