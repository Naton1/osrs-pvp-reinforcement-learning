package com.elvarg.game.model.commands.impl;

import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.commands.Command;

public class Store implements Command {

    @Override
    public void execute(Player player, String command, String[] parts) {
        player.getPacketSender().sendURL("http://www.deadlypkers.net");
    }

    @Override
    public boolean canUse(Player player) {
        return true;
    }

}
