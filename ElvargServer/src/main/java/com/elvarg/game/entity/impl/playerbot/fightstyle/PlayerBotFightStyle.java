package com.elvarg.game.entity.impl.playerbot.fightstyle;

import com.elvarg.game.content.combat.magic.CombatSpell;
import com.elvarg.game.content.combat.magic.CombatSpells;
import com.elvarg.game.entity.impl.playerbot.PlayerBot;
import com.elvarg.game.model.Item;
import com.elvarg.game.model.ItemInSlot;
import com.elvarg.game.model.Skill;
import com.elvarg.util.ItemIdentifiers;

public abstract class PlayerBotFightStyle {

    // The weapon the PlayerBot switches back to once none of the weapon switches apply
    public abstract int getMainWeaponId();

    public abstract WeaponSwitch[] getWeaponSwitches();

    // Determines whether a PlayerBot should eat or not on each of its turns
    public abstract boolean shouldEat();

    public abstract ItemInSlot[] potions();

    public abstract int[] food();
}
