package com.elvarg.game.entity.impl.playerbot.commands;

import com.elvarg.game.entity.impl.playerbot.PlayerBot;

public class FollowPlayer implements BotCommand {

    @Override
    public String[] triggers() {
        return new String[] { "follow me" };
    }

    @Override
    public void start(PlayerBot playerBot, String[] args) {
        playerBot.setFollowing(playerBot.getInteractingWith());
    }

    @Override
    public void stop(PlayerBot playerBot) {
        playerBot.setFollowing(null);
    }
}
