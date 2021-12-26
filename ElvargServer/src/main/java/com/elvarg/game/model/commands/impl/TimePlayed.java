package com.elvarg.game.model.commands.impl;

import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.commands.Command;
import com.elvarg.util.Misc;

public class TimePlayed implements Command {

    @Override
    public void execute(Player player, String command, String[] parts) {
        player.forceChat("I've been playing for " + Misc.getFormattedPlayTime(player) + ".");
    }

    @Override
    public boolean canUse(Player player) {
        return true;
    }

}
