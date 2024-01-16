package com.elvarg.game.model.dialogues.builders;

import java.util.HashMap;
import java.util.Map;

import com.elvarg.game.model.dialogues.entries.Dialogue;

public abstract class DialogueBuilder {
    
    private final Map<Integer, Dialogue> dialogues;

    public DialogueBuilder() {
        this.dialogues = new HashMap<>();
    }

    public DialogueBuilder add(Dialogue... dialogues) {
        for (Dialogue dialogue : dialogues) {
            this.dialogues.put(dialogue.getIndex(), dialogue);
        }
        return this;
    }
    
    public Map<Integer, Dialogue> getDialogues() {
        return dialogues;
    }
}
