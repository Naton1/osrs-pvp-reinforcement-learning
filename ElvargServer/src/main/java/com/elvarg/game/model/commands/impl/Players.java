package com.elvarg.game.model.commands.impl;

import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.commands.Command;
import com.elvarg.game.model.rights.DonatorRights;

public class Players implements Command {

    @Override
    public void execute(Player player, String command, String[] parts) {
        //	DialogueManager.sendStatement(player, "There are currently " + World.getPlayers().size() + " players online.");
        player.setDonatorRights(DonatorRights.REGULAR_DONATOR);

        player.getPacketSender().sendRights();
    }

    @Override
    public boolean canUse(Player player) {
        return true;
    }
}
