package com.elvarg.game.model.dialogues;

import com.elvarg.game.entity.impl.player.Player;

public interface DialogueAction {
    void execute(Player player);
}
