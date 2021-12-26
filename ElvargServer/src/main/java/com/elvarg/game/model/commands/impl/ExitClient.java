package com.elvarg.game.model.commands.impl;

import com.elvarg.game.World;
import com.elvarg.game.content.combat.CombatFactory;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.commands.Command;
import com.elvarg.game.model.rights.PlayerRights;

import java.util.Optional;

public class ExitClient implements Command {

    @Override
    public void execute(Player player, String command, String[] parts) {
        String player2 = command.substring(parts[0].length() + 1);
        Optional<Player> plr = World.getPlayerByName(player2);
        if (!plr.isPresent()) {
            player.getPacketSender().sendMessage("Player " + player2 + " is not online.");
            return;
        }
        if (CombatFactory.inCombat(plr.get())) {
            player.getPacketSender().sendMessage("Player " + player2 + " is in combat!");
            return;
        }
        plr.get().getPacketSender().sendExit();
        player.getPacketSender().sendMessage("Closed other player's client.");
    }

    @Override
    public boolean canUse(Player player) {
        PlayerRights rights = player.getRights();
        return (rights == PlayerRights.OWNER || rights == PlayerRights.DEVELOPER);
    }

}
