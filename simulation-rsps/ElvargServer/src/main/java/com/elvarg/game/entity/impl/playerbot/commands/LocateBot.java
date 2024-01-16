package com.elvarg.game.entity.impl.playerbot.commands;

import com.elvarg.game.entity.impl.playerbot.PlayerBot;
import com.elvarg.util.Misc;

import static com.elvarg.game.entity.impl.playerbot.commands.CommandType.*;

public class LocateBot implements BotCommand {
    @Override
    public String[] triggers() {
        return new String[] { "where are you" };
    }

    @Override
    public void start(PlayerBot playerBot, String[] args) {
        String message = "I'm currently in an unknown area...";

        if (playerBot.getArea() != null) {
            message = "I'm currently in " + playerBot.getArea().getName();
        }

        byte[] data = Misc.textPack(message);

        playerBot.getRelations().message(playerBot.getInteractingWith(), data, data.length);

        // Stop the command immediately so the bot goes into IDLE behaviour
        playerBot.stopCommand();
    }

    @Override
    public void stop(PlayerBot playerBot) {

    }

    @Override
    public CommandType[] supportedTypes() {
        return new CommandType[] { PRIVATE_CHAT };
    }
}
