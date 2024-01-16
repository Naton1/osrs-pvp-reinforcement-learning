package com.elvarg.net.packet.impl;

import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.net.ByteBufUtils;
import com.elvarg.net.packet.Packet;
import com.elvarg.net.packet.PacketConstants;
import com.elvarg.net.packet.PacketExecutor;

/**
 * This packet manages the input taken from chat box interfaces that allow
 * input, such as withdraw x, bank x, enter name of friend, etc.
 *
 * @author Gabriel Hannason
 */

public class EnterInputPacketListener implements PacketExecutor {

    @Override
    public void execute(Player player, Packet packet) {

        if (player == null || player.getHitpoints() <= 0) {
            return;
        }

        switch (packet.getOpcode()) {
        case PacketConstants.ENTER_SYNTAX_OPCODE:
            String name = ByteBufUtils.readString(packet.getBuffer());
            if (name == null)
                return;
            if (player.getEnteredSyntaxAction() != null) {
                player.getEnteredSyntaxAction().execute(name);
                player.setEnteredSyntaxAction(null);
            }
            break;
        case PacketConstants.ENTER_AMOUNT_OPCODE:
            int amount = packet.readInt();
            if (amount <= 0)
                return;
            if (player.getEnteredAmountAction() != null) {
                player.getEnteredAmountAction().execute(amount);
                player.setEnteredAmountAction(null);
            }
            break;
        }
    }
}
