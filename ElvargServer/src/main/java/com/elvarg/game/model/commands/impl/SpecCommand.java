package com.elvarg.game.model.commands.impl;

import com.elvarg.game.content.combat.CombatSpecial;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.commands.Command;
import com.elvarg.game.model.rights.PlayerRights;

public class SpecCommand implements Command {

    @Override
    public void execute(Player player, String command, String[] parts) {
        int amt = 100;
        if (parts.length > 1)
            amt = Integer.parseInt(parts[1]);
        player.setSpecialPercentage(amt);
        CombatSpecial.updateBar(player);
    }

    @Override
    public boolean canUse(Player player) {
        PlayerRights rights = player.getRights();
        return (rights == PlayerRights.OWNER || rights == PlayerRights.DEVELOPER);
    }

}
