package com.elvarg.game.entity.impl.playerbot.commands;

import com.elvarg.game.entity.impl.playerbot.PlayerBot;
import com.elvarg.net.packet.impl.FollowPlayerPacketListener;

import static com.elvarg.game.entity.impl.playerbot.commands.CommandType.PUBLIC_CHAT;

public class FollowPlayer implements BotCommand {

    @Override
    public String[] triggers() {
        return new String[] { "follow me" };
    }

    @Override
    public void start(PlayerBot playerBot, String[] args) {
        FollowPlayerPacketListener.follow(playerBot, playerBot.getInteractingWith());
    }

    @Override
    public void stop(PlayerBot playerBot) {
        playerBot.getMovementQueue().walkToReset();
        playerBot.setMobileInteraction(null);
        playerBot.updateLocalPlayers();
    }

    @Override
    public CommandType[] supportedTypes() {
        return new CommandType[] { PUBLIC_CHAT };
    }
}
