package com.elvarg.game.entity.impl.playerbot.fightstyle;

import com.elvarg.game.content.combat.magic.CombatSpell;
import com.elvarg.game.entity.impl.Mobile;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.entity.impl.playerbot.PlayerBot;
import com.elvarg.game.model.ItemInSlot;

public abstract class WeaponSwitch {

    private ItemInSlot itemInSlot;

    private CombatSpell combatSpell;

    public WeaponSwitch(CombatSpell combatSpell) {
        this.combatSpell = combatSpell;
    }

    public WeaponSwitch(ItemInSlot itemInSlot) {
        this.itemInSlot = itemInSlot;
    }

    public ItemInSlot getItemInSlot() {
        return this.itemInSlot;
    }

    public CombatSpell getCombatSpell() {
        return this.combatSpell;
    }

    /**
     * Should the player switch
     */
    public abstract boolean shouldSwitch(PlayerBot playerBot, Mobile enemy);

    /**
     * Called before weapon is switched
     */
    public void beforeSwitch(PlayerBot playerBot) {
    };

    /**
     * Called after weapon is switched
     */
    public void afterSwitch(PlayerBot playerBot) {
    };
}
