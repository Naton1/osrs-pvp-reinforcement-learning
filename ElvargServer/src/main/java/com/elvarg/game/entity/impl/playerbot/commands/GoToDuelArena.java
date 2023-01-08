package com.elvarg.game.entity.impl.playerbot.commands;

import com.elvarg.game.entity.impl.playerbot.PlayerBot;
import com.elvarg.game.model.teleportation.TeleportHandler;
import com.elvarg.game.model.teleportation.TeleportType;
import com.elvarg.game.task.Task;
import com.elvarg.game.task.TaskManager;
import static com.elvarg.game.entity.impl.playerbot.commands.CommandType.*;
import static com.elvarg.game.model.teleportation.Teleportable.DUEL_ARENA;

public class GoToDuelArena implements BotCommand {

    @Override
    public String[] triggers() {
        return new String[] { "duel arena" };
    }

    @Override
    public void start(PlayerBot playerBot, String[] args) {

        playerBot.sendChat("Going to Duel Arena - see ya soon!");

        TaskManager.submit(new Task(5, playerBot.getIndex(), false) {
            @Override
            public void execute() {
                // Telport to Duel Arena
                TeleportHandler.teleport(playerBot, DUEL_ARENA.getPosition(), TeleportType.NORMAL, false);
                stop();
            }
        });

        // Stop the command immediately so the bot goes into IDLE behaviour
        playerBot.stopCommand();
    }

    @Override
    public void stop(PlayerBot playerBot) {
    }

    @Override
    public CommandType[] supportedTypes() {
        return new CommandType[] { PUBLIC_CHAT, PRIVATE_CHAT, CLAN_CHAT };
    }
}

