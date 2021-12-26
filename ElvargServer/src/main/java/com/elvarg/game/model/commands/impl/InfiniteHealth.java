package com.elvarg.game.model.commands.impl;

import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.commands.Command;
import com.elvarg.game.model.rights.PlayerRights;

public class InfiniteHealth implements Command {

    @Override
    public void execute(Player player, String command, String[] parts) {
        player.setInfiniteHealth(!player.hasInfiniteHealth());
        player.getPacketSender().sendMessage("Invulnerable: " + String.valueOf(player.hasInfiniteHealth()));
    }

    @Override
    public boolean canUse(Player player) {
        PlayerRights rights = player.getRights();
        return rights == PlayerRights.OWNER || rights == PlayerRights.DEVELOPER;
    }
}
