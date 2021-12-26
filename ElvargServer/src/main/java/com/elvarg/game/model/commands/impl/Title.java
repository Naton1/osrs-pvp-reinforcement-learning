package com.elvarg.game.model.commands.impl;

import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.commands.Command;

import java.util.Arrays;
import java.util.List;

public class Title implements Command {

    private static final List<String> INAPPROPRIATE_TITLES = Arrays.asList("nigger", "ass", "boobs");

    @Override
    public void execute(Player player, String command, String[] parts) {
        if (INAPPROPRIATE_TITLES.stream().anyMatch(title -> parts[1].toLowerCase().contains(title))) {
            player.getPacketSender().sendMessage("You're not allowed to have that in your title.");
            return;
        }
        player.setLoyaltyTitle("@blu@" + parts[1]);
    }

    @Override
    public boolean canUse(Player player) {
        return true;
    }

}
