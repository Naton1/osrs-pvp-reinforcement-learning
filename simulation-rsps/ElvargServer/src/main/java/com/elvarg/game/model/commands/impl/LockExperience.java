package com.elvarg.game.model.commands.impl;

import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.commands.Command;

public class LockExperience implements Command {

    @Override
    public void execute(Player player, String command, String[] parts) {
        player.setExperienceLocked(!player.experienceLocked());
        player.getPacketSender().sendMessage("Lock: " + player.experienceLocked());
    }

    @Override
    public boolean canUse(Player player) {
        return true;
    }

}
