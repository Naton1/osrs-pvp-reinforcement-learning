package com.elvarg.game.model.commands.impl;

import com.elvarg.game.content.combat.CombatFactory;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.SkullType;
import com.elvarg.game.model.commands.Command;

public class Skull implements Command {

    @Override
    public void execute(Player player, String command, String[] parts) {
        if (CombatFactory.inCombat(player)) {
            player.getPacketSender().sendMessage("You cannot change that during combat!");
            return;
        }
        if (parts[0].contains("red")) {
            CombatFactory.skull(player, SkullType.RED_SKULL, (60 * 30)); // Should be 30 mins
        } else {
            CombatFactory.skull(player, SkullType.WHITE_SKULL, 300); // Should be 5 mins
        }
    }

    @Override
    public boolean canUse(Player player) {
        return true;
    }
}
