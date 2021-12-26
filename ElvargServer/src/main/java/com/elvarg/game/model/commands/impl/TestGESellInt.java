package com.elvarg.game.model.commands.impl;

import com.elvarg.game.definition.ItemDefinition;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.commands.Command;
import com.elvarg.game.model.rights.PlayerRights;

public class TestGESellInt implements Command {

    @Override
    public void execute(Player player, String command, String[] parts) {
        player.getPacketSender().
                sendItemOnInterface(24780, Integer.valueOf(parts[1]), 1).
                sendString(24769, ItemDefinition.forId(Integer.valueOf(parts[1])).getName()).
                sendString(24770, ItemDefinition.forId(Integer.valueOf(parts[1])).getExamine());
    }

    @Override
    public boolean canUse(Player player) {
        PlayerRights rights = player.getRights();
        return (rights == PlayerRights.OWNER || rights == PlayerRights.DEVELOPER);
    }

}
