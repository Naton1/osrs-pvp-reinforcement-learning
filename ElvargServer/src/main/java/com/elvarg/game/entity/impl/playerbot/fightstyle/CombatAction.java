package com.elvarg.game.entity.impl.playerbot.fightstyle;

import com.elvarg.game.entity.impl.Mobile;
import com.elvarg.game.entity.impl.playerbot.PlayerBot;

public interface CombatAction {

    boolean shouldPerform(PlayerBot playerBot, Mobile enemy);

    void perform(PlayerBot playerBot, Mobile enemy);

    default boolean stopAfter() {
        return true;
    }
}
