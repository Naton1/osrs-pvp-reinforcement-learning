package com.elvarg.game.model.dialogues.entries.impl;

import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.dialogues.entries.Dialogue;

public class EndDialogue extends Dialogue {

    public EndDialogue(int index) {
        super(index);
    }

    @Override
    public void send(Player player) {
        player.getPacketSender().sendInterfaceRemoval();
    }
}
