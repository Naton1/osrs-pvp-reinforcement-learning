package com.elvarg.game.entity.impl.playerbot.commands;

import com.elvarg.game.content.presets.Presetable;
import com.elvarg.game.content.presets.Presetables;
import com.elvarg.game.entity.impl.playerbot.PlayerBot;
import com.elvarg.util.Misc;

import static com.elvarg.game.entity.impl.playerbot.commands.CommandType.PUBLIC_CHAT;

public class LoadPreset implements BotCommand {

    public static final int LOAD_PRESET_BUTTON_ID = 45064;

    @Override
    public String[] triggers() {
        return new String[] { "load preset" };
    }

    @Override
    public void start(PlayerBot playerBot, String[] args) {
        Presetable preset;
        if (args == null || args.length == 0 || args.length != 1 || Integer.parseInt(args[0]) == 0 || Integer.parseInt(args[0]) > Presetables.GLOBAL_PRESETS.length) {
            // Player hasn't specified a valid Preset ID
            preset = Presetables.GLOBAL_PRESETS[Misc.randomInclusive(0, Presetables.GLOBAL_PRESETS.length-1)];
        } else {
            preset = Presetables.GLOBAL_PRESETS[Integer.parseInt(args[0])-1 /* Player will specify 1-n */];
        }

        playerBot.setCurrentPreset(preset);
        Presetables.handleButton(playerBot, LOAD_PRESET_BUTTON_ID);

        playerBot.updateLocalPlayers();

        // Indicate the command is finished straight away
        playerBot.stopCommand();
    }

    @Override
    public void stop(PlayerBot playerBot) {
        // Command auto-stops
    }

    @Override
    public CommandType[] supportedTypes() {
        return new CommandType[] { PUBLIC_CHAT };
    }
}
