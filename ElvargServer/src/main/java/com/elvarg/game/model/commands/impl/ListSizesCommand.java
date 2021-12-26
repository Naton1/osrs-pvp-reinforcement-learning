package com.elvarg.game.model.commands.impl;

import com.elvarg.game.World;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.commands.Command;
import com.elvarg.game.model.rights.PlayerRights;

public class ListSizesCommand implements Command {

    @Override
    public void execute(Player player, String command, String[] parts) {
        player.getPacketSender().sendMessage("Players: " + World.getPlayers().size() + ", NPCs: " + World.getNpcs().size() + ", Objects: " + World.getObjects().size() + ", GroundItems: " + World.getItems().size() + ".");
    }

    @Override
    public boolean canUse(Player player) {
        return (player.getRights() == PlayerRights.DEVELOPER || player.getRights() == PlayerRights.OWNER);
    }
}
