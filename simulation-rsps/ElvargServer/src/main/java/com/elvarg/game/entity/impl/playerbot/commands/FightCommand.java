package com.elvarg.game.entity.impl.playerbot.commands;

import com.elvarg.game.World;
import com.elvarg.game.entity.impl.playerbot.PlayerBot;

import static com.elvarg.game.entity.impl.playerbot.commands.CommandType.*;

public class FightCommand implements BotCommand {

    @Override
    public String[] triggers() {
        return new String[] { "fight" };
    }

    @Override
    public void start(PlayerBot playerBot, String[] args) {
        if (args == null || args.length == 0 || args[0].equalsIgnoreCase("me")) {
            // Player just wants to fight bot.
            playerBot.getCombat().attack(playerBot.getInteractingWith());
            playerBot.getInteractingWith().getCombat().attack(playerBot);
            playerBot.sendChat("Sure, Good luck!");
            return;
        }

        String searchName = String.join(" ", args);

        if (searchName.equalsIgnoreCase(playerBot.getUsername()) || !World.getPlayerBots().containsKey(searchName)) {
            // Bot can't be found or has been instructed to attack itself
            playerBot.sendChat("Sorry, can't find " + searchName + "...");
            return;
        }

        PlayerBot targetBot = World.getPlayerBots().get(searchName);

        if (playerBot.getLocation().getDistance(targetBot.getLocation()) >= 40) {
            playerBot.sendChat("Sorry, " + searchName + " is too far away.");
            return;
        }

        playerBot.getCombat().attack(targetBot);
        targetBot.getCombat().attack(playerBot);
    }

    @Override
    public void stop(PlayerBot playerBot) {
        playerBot.getCombat().reset();
    }

    @Override
    public CommandType[] supportedTypes() {
        return new CommandType[] { PUBLIC_CHAT };
    }

}
