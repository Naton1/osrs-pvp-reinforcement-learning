package com.elvarg.net.packet.impl;

import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.net.packet.Packet;
import com.elvarg.net.packet.PacketExecutor;

/**
 * Represents a packet used for handling dialogues. This specific packet
 * currently handles the action for clicking the "next" option during a
 * dialogue.
 *
 * @author Professor Oak
 */

public class DialoguePacketListener implements PacketExecutor {

	@Override
	public void execute(Player player, Packet packet) {
	    player.getDialogueManager().advance();
	}
}
