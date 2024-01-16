package com.elvarg.game.content.minigames;

import com.elvarg.game.entity.impl.object.GameObject;
import com.elvarg.game.entity.impl.player.Player;

public interface Minigame {
    boolean firstClickObject(Player player, GameObject object);
    boolean handleButtonClick(Player player, int button);
    void process();

    void init();
}
