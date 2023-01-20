package com.elvarg.game.model.commands.impl;

import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.commands.Command;
import com.elvarg.game.model.rights.DonatorRights;
import com.elvarg.game.model.rights.PlayerRights;

public class Donator implements Command {

    @Override
    public void execute(Player player, String command, String[] parts) {
        player.setDonatorRights(DonatorRights.REGULAR_DONATOR);
        player.getPacketSender().sendRights();
    }

    @Override
    public boolean canUse(Player player) {
        PlayerRights rights = player.getRights();
        return (rights == PlayerRights.OWNER || rights == PlayerRights.DEVELOPER);
    }

}
