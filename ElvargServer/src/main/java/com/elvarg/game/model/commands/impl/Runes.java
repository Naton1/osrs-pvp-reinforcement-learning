package com.elvarg.game.model.commands.impl;

import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.commands.Command;
import com.elvarg.game.model.rights.PlayerRights;

public class Runes implements Command {

    @Override
    public void execute(Player player, String command, String[] parts) {
        int[] runes = new int[]{554, 555, 556, 557, 558, 559, 560, 561, 562, 563, 564, 565};
        for (int rune : runes) {
            player.getInventory().add(rune, 1000);
        }
    }

    @Override
    public boolean canUse(Player player) {
        PlayerRights rights = player.getRights();
        return (rights == PlayerRights.OWNER || rights == PlayerRights.DEVELOPER);
    }

}
