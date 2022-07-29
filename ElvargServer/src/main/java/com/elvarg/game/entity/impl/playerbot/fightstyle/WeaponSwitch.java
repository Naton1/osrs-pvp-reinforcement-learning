package com.elvarg.game.entity.impl.playerbot.fightstyle;

import com.elvarg.game.content.combat.magic.CombatSpell;
import com.elvarg.game.entity.impl.Mobile;
import com.elvarg.game.entity.impl.playerbot.PlayerBot;

public abstract class WeaponSwitch {

    private int itemId;

    private CombatSpell combatSpell;

    public WeaponSwitch(CombatSpell combatSpell) {
        this.combatSpell = combatSpell;
    }

    public WeaponSwitch(int itemId) {
        this.itemId = itemId;
    }

    public int getItemId() {
        return this.itemId;
    }

    public CombatSpell getCombatSpell() {
        return this.combatSpell;
    }

    /**
     * Should the player switch
     */
    public abstract boolean shouldUse(PlayerBot playerBot, Mobile enemy);

    /**
     * Called before weapon is switched
     */
    public void beforeUse(PlayerBot playerBot) {
    };

    /**
     * Called after weapon is switched
     */
    public void afterUse(PlayerBot playerBot) {
    };
}
