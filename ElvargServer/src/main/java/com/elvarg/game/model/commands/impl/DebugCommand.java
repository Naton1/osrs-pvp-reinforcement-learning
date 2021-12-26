package com.elvarg.game.model.commands.impl;

import com.elvarg.game.collision.RegionManager;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.commands.Command;
import com.elvarg.game.model.dialogues.builders.impl.NieveDialogue;
import com.elvarg.game.model.rights.PlayerRights;

public class DebugCommand implements Command {

    @Override
    public void execute(Player player, String command, String[] parts) {
        System.out.println(RegionManager.wallsExist(player.getLocation().clone(), player.getPrivateArea()));
    }

    @Override
    public boolean canUse(Player player) {
        return (player.getRights() == PlayerRights.DEVELOPER);
    }

}
