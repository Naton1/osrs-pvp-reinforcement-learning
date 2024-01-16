package com.elvarg.game.model.commands.impl;

import com.elvarg.game.World;
import com.elvarg.game.entity.impl.npc.NPC;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.commands.Command;
import com.elvarg.game.model.rights.PlayerRights;

public class SpawnNPCCommand implements Command {

    @Override
    public void execute(Player player, String command, String[] parts) {
        NPC npc = NPC.create(Integer.parseInt(parts[1]), player.getLocation().clone());
        World.getAddNPCQueue().add(npc);
        if (player.getPrivateArea() != null) {
            player.getPrivateArea().add(npc);
        }
    }

    @Override
    public boolean canUse(Player player) {
        PlayerRights rights = player.getRights();
        return (rights == PlayerRights.OWNER || rights == PlayerRights.DEVELOPER);
    }

}
