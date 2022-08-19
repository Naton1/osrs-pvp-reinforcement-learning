package com.elvarg.game.model.commands;

import com.elvarg.game.entity.impl.grounditem.ItemOnGroundManager;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.Item;
import com.elvarg.game.model.rights.PlayerRights;

/**
 * @author Ynneh | 16/08/2022 - 16:26
 * <https://github.com/drhenny>
 */
public class GroundItemCommand implements Command {

    @Override
    public void execute(Player player, String command, String[] parts) {
        ItemOnGroundManager.register(player, new Item(995, 10000), player.getLocation());
        player.getPacketSender().sendMessage("Spawned ground item..");
    }

    @Override
    public boolean canUse(Player player) {
        return player.getRights().equals(PlayerRights.OWNER);
    }
}
