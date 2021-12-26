package com.elvarg.game.model.commands.impl;

import com.elvarg.game.World;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.commands.Command;
import com.elvarg.game.model.rights.PlayerRights;

public class SaveAll implements Command {

    @Override
    public void execute(Player player, String command, String[] parts) {
        World.savePlayers();
        player.getPacketSender().sendMessage("Saved all players.");
    }

    @Override
    public boolean canUse(Player player) {
        return (player.getRights() == PlayerRights.DEVELOPER || player.getRights() == PlayerRights.OWNER || player.getRights() == PlayerRights.ADMINISTRATOR);
    }
}
