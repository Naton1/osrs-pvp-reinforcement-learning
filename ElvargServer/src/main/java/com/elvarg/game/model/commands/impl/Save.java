package com.elvarg.game.model.commands.impl;

import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.entity.impl.player.PlayerSaving;
import com.elvarg.game.model.commands.Command;
import com.elvarg.game.model.rights.PlayerRights;

public class Save implements Command {

    @Override
    public void execute(Player player, String command, String[] parts) {
        PlayerSaving.save(player);
        player.getPacketSender().sendMessage("Saved player.");
    }

    @Override
    public boolean canUse(Player player) {
        return (player.getRights() == PlayerRights.DEVELOPER || player.getRights() == PlayerRights.OWNER);
    }
}
