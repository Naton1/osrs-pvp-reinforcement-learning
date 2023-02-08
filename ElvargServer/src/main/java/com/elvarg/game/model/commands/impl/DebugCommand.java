package com.elvarg.game.model.commands.impl;

import com.elvarg.game.collision.RegionManager;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.commands.Command;
import com.elvarg.game.model.dialogues.builders.impl.NieveDialogue;
import com.elvarg.game.model.rights.PlayerRights;

public class DebugCommand implements Command {

    @Override
    public void execute(Player player, String command, String[] parts) {
       // System.out.println(RegionManager.wallsExist(player.getLocation().clone(), player.getPrivateArea()));
        for (int i = 0; i < 4; i++) {
            player.getPacketSender().sendString("Dead", 21111+i);

        }
        player.getPacketSender().sendString("10000", 21115);
        player.getPacketSender().sendString("5", 21116);
        player.getPacketSender().sendString("Time remaining: 5mins", 21117);
    }

    @Override
    public boolean canUse(Player player) {
        return true;
    }

}
