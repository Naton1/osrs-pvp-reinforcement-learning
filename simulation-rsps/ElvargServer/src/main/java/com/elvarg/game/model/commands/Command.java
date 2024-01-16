package com.elvarg.game.model.commands;

import com.elvarg.game.entity.impl.player.Player;

public interface Command {

    abstract void execute(Player player, String command, String[] parts);

    abstract boolean canUse(Player player);
}
