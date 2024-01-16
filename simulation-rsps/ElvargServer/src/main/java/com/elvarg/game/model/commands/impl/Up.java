package com.elvarg.game.model.commands.impl;

import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.Location;
import com.elvarg.game.model.commands.Command;
import com.elvarg.game.model.rights.PlayerRights;

public class Up implements Command {

    @Override
    public void execute(Player player, String command, String[] parts) {
        Location newLocation = player.getLocation().clone().setZ(player.getLocation().getZ() + 1);
        player.moveTo(newLocation);
    }

    @Override
    public boolean canUse(Player player) {
        return (player.getRights() == PlayerRights.OWNER || player.getRights() == PlayerRights.DEVELOPER);
    }
}
