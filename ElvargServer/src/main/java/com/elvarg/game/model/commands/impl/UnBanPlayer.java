package com.elvarg.game.model.commands.impl;

import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.commands.Command;
import com.elvarg.game.model.rights.PlayerRights;
import com.elvarg.util.PlayerPunishment;

import static com.elvarg.game.GameConstants.PLAYER_PERSISTENCE;

public class UnBanPlayer implements Command {

    @Override
    public void execute(Player player, String command, String[] parts) {
        String player2 = command.substring(parts[0].length() + 1);

        if (!PLAYER_PERSISTENCE.exists(player2)) {
            player.getPacketSender().sendMessage("Player " + player2 + " is not online.");
            return;
        }

        if (!PlayerPunishment.banned(player2)) {
            player.getPacketSender().sendMessage("Player " + player2 + " is not banned!");
            return;
        }

    }

    @Override
    public boolean canUse(Player player) {
        PlayerRights rights = player.getRights();
        return (rights == PlayerRights.OWNER || rights == PlayerRights.DEVELOPER);
    }

}
