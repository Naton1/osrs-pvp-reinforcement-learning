package com.elvarg.game.entity.impl.playerbot.commands;

import com.elvarg.game.content.minigames.impl.CastleWars;
import com.elvarg.game.entity.impl.playerbot.PlayerBot;
import com.elvarg.game.model.Animation;
import com.elvarg.game.model.container.impl.Equipment;
import com.elvarg.game.task.Task;
import com.elvarg.game.task.TaskManager;

import static com.elvarg.game.entity.impl.playerbot.commands.CommandType.*;

public class PlayCastleWars implements BotCommand {

    private static final Animation WAVE_ANIM = new Animation(863);

    @Override
    public String[] triggers() {
        return new String[] { "castlewars", " cw" };
    }

    @Override
    public void start(PlayerBot playerBot, String[] args) {
        // Remove head and cape
        playerBot.getEquipment().set(Equipment.CAPE_SLOT, Equipment.NO_ITEM);
        playerBot.getEquipment().set(Equipment.HEAD_SLOT, Equipment.NO_ITEM);

        playerBot.updateLocalPlayers();

        playerBot.sendChat("Going to play Castlewars, BRB!");

        playerBot.performAnimation(WAVE_ANIM);

        TaskManager.submit(new Task(5, playerBot.getIndex(), false) {
            @Override
            public void execute() {
                CastleWars.addToWaitingRoom(playerBot, CastleWars.Team.GUTHIX);
                stop();
                playerBot.stopCommand();
            }
        });
    }

    @Override
    public void stop(PlayerBot playerBot) {
    }

    @Override
    public CommandType[] supportedTypes() {
        return new CommandType[] { PUBLIC_CHAT, PRIVATE_CHAT, CLAN_CHAT };
    }
}
