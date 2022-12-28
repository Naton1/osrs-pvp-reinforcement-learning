package com.elvarg.game.entity.impl.playerbot.commands;

import com.elvarg.game.entity.impl.playerbot.PlayerBot;

import static com.elvarg.game.entity.impl.playerbot.commands.CommandType.PUBLIC_CHAT;

public class HoldItems implements BotCommand {
    @Override
    public String[] triggers() {
        return new String[] { "hold items", "hold my items", "hold my stuff", "keep my things", "store items", "store my items" };
    }

    @Override
    public void start(PlayerBot playerBot, String[] args) {
        if (playerBot.getTradingInteraction().holdingItems.containsKey(playerBot.getInteractingWith())) {
            // Player bots can only store one set of items for a given player
            playerBot.sendChat("Sorry, " +playerBot.getInteractingWith().getUsername() + ", I'm already holding items for you.");
            playerBot.stopCommand();
            return;
        }

        playerBot.sendChat("Sure, just trade me your items.");
        playerBot.getInteractingWith().getPacketSender().sendMessage("Warning: These items will be lost if the server restarts.");
        playerBot.getTrading().requestTrade(playerBot.getInteractingWith());
    }

    @Override
    public void stop(PlayerBot playerBot) {
    }

    @Override
    public CommandType[] supportedTypes() {
        return new CommandType[] { PUBLIC_CHAT };
    }
}
