package com.elvarg.game.entity.impl.playerbot.fightstyle;

import com.elvarg.game.content.presets.Presetable;

public interface FighterPreset {

    Presetable getItemPreset();

    CombatAction[] getCombatActions();

    default int eatAtPercent() {
        return 40;
    }

}
