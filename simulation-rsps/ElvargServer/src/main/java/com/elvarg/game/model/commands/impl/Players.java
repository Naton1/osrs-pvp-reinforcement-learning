package com.elvarg.game.model.commands.impl;

import com.elvarg.game.World;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.commands.Command;
import com.elvarg.game.model.rights.DonatorRights;

public class Players implements Command {

    @Override
    public void execute(Player player, String command, String[] parts) {
        player.getPacketSender().sendMessage("There are currently " + World.getPlayers().size() + " players online.");
    }

    @Override
    public boolean canUse(Player player) {
        return true;
    }
}
