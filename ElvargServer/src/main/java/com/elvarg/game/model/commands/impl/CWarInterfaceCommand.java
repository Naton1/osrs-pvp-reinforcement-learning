package com.elvarg.game.model.commands.impl;

import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.commands.Command;
import com.elvarg.game.model.rights.PlayerRights;

/**
 * @author Ynneh | 06/01/2023 - 21:08
 * <https://github.com/drhenny>
 */
public class CWarInterfaceCommand implements Command {
    @Override
    public void execute(Player player, String command, String[] parts) {
        try {
            player.getPacketSender().sendInterface(11169);
            Integer x = Integer.valueOf(parts[1]);
            Integer y = Integer.valueOf(parts[2]);
            player.getPacketSender().sendInterfaceComponentMoval(x, y, 11332);
            player.getPacketSender().sendMessage("Sending RedX to X="+x+", Y="+y);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean canUse(Player player) {
        return player.getRights() == PlayerRights.DEVELOPER;
    }
}
