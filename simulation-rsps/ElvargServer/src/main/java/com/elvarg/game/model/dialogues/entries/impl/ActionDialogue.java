package com.elvarg.game.model.dialogues.entries.impl;

import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.Action;
import com.elvarg.game.model.dialogues.entries.Dialogue;

public class ActionDialogue extends Dialogue {

    private final Action action;
    
    public ActionDialogue(int index, Action action) {
        super(index);
        this.action = action;
    }

    @Override
    public void send(Player player) {
        action.execute();
    }
}
