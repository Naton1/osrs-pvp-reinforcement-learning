package com.elvarg.game.model.dialogues.builders.impl;

import com.elvarg.game.model.dialogues.builders.DialogueBuilder;
import com.elvarg.game.model.dialogues.entries.impl.NpcDialogue;
import com.elvarg.game.model.dialogues.entries.impl.PlayerDialogue;

public class TestStaticDialogue extends DialogueBuilder {

    public TestStaticDialogue() {
        add(new PlayerDialogue(0, "Well this works just fine."), new PlayerDialogue(1, "Second test"),
                new NpcDialogue(2, 6797, "okay great."));
    }
}
