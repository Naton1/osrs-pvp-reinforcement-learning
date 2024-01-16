package com.elvarg.game.model.dialogues.entries;

import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.dialogues.DialogueAction;

public abstract class Dialogue {
    
    private final int index;

    // If specified, this action will execute instead of advancing to the next dialogue in the chain
    private DialogueAction continueAction;
    
    public Dialogue(int index) {
        this.index = index;
    }
    
    public abstract void send(Player player);

    public int getIndex() {
        return index;
    }

    public DialogueAction getContinueAction() {
        return this.continueAction;
    }

    public void setContinueAction(DialogueAction action) {
        this.continueAction = action;
    }
}
