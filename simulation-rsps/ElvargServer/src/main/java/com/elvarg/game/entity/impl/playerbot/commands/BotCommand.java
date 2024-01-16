package com.elvarg.game.entity.impl.playerbot.commands;

import com.elvarg.game.entity.impl.playerbot.PlayerBot;

public abstract interface BotCommand {

    // The chat commands that will trigger this interaction
    public abstract String[] triggers();

    public abstract void start(PlayerBot playerBot, String[] args);

    public abstract void stop(PlayerBot playerBot);

    public abstract CommandType[] supportedTypes();
}
