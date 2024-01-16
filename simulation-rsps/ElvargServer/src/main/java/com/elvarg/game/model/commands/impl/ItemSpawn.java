package com.elvarg.game.model.commands.impl;

import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.Item;
import com.elvarg.game.model.commands.Command;
import com.elvarg.game.model.rights.PlayerRights;

public class ItemSpawn implements Command {

    @Override
    public void execute(Player player, String command, String[] parts) {
        int amount = 1;
        if (parts.length > 2) {
            amount = Integer.parseInt(parts[2]);
        }
        player.getInventory().add(new Item(Integer.parseInt(parts[1]), amount));
    }

    @Override
    public boolean canUse(Player player) {
        PlayerRights rights = player.getRights();
        return (rights == PlayerRights.OWNER || rights == PlayerRights.DEVELOPER);
    }

}
