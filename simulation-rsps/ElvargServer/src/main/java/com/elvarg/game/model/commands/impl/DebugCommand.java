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
        player.getPacketSender().sendTabInterface(Integer.valueOf(parts[1]), Integer.valueOf(parts[2]));//962 42500 - green = unlocked - 5449
        player.choosingMusic = true;
        player.getPacketSender().sendTabInterface(11, player.choosingMusic  ? 962 : 42500);
        player.getPacketSender().sendMessage("sending inter..");
    }

    @Override
    public boolean canUse(Player player) {
        return true;
    }

}
