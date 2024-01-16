package com.elvarg.game.model.commands.impl;

import com.elvarg.game.World;
import com.elvarg.game.definition.loader.impl.NpcSpawnDefinitionLoader;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.commands.Command;
import com.elvarg.game.model.rights.PlayerRights;

public class ReloadNPCSpawns implements Command {

    @Override
    public void execute(Player player, String command, String[] parts) {
        try {
            World.getNpcs().clear();
			new NpcSpawnDefinitionLoader().load();
			player.getPacketSender().sendConsoleMessage("Reloaded npc spawns.");
		} catch (Throwable e) {
			e.printStackTrace();
			player.getPacketSender().sendMessage("Error reloading npc spawns.");
		}
    }

    @Override
    public boolean canUse(Player player) {
        PlayerRights rights = player.getRights();
        return (rights == PlayerRights.OWNER || rights == PlayerRights.DEVELOPER);
    }

}
