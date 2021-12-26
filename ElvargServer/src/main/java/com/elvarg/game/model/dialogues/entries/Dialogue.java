package com.elvarg.game.model.dialogues.entries;

import com.elvarg.game.entity.impl.player.Player;

public abstract class Dialogue {
    
    private final int index;
    
    public Dialogue(int index) {
        this.index = index;
    }
    
    public abstract void send(Player player);

    public int getIndex() {
        return index;
    }
}
