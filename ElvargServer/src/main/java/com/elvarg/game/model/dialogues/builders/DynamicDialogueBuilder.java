package com.elvarg.game.model.dialogues.builders;

import com.elvarg.game.entity.impl.player.Player;

public abstract class DynamicDialogueBuilder extends DialogueBuilder {
    public abstract void build(Player player);
}
