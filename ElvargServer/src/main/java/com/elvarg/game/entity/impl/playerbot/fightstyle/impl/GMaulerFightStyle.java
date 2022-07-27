package com.elvarg.game.entity.impl.playerbot.fightstyle.impl;

import com.elvarg.game.content.PrayerHandler;
import com.elvarg.game.content.combat.CombatSpecial;
import com.elvarg.game.content.combat.magic.CombatSpells;
import com.elvarg.game.entity.impl.Mobile;
import com.elvarg.game.entity.impl.playerbot.PlayerBot;
import com.elvarg.game.entity.impl.playerbot.fightstyle.PlayerBotFightStyle;
import com.elvarg.game.entity.impl.playerbot.fightstyle.WeaponSwitch;
import com.elvarg.game.model.ItemInSlot;

import static com.elvarg.util.ItemIdentifiers.*;

public class GMaulerFightStyle extends PlayerBotFightStyle {

    @Override
    public int getMainWeaponId() {
        return MAGIC_SHORTBOW;
    }

    @Override
    public WeaponSwitch[] getWeaponSwitches() {
        return new WeaponSwitch[] {

                new WeaponSwitch(new ItemInSlot(GRANITE_MAUL, 5)) {
                    @Override
                    public boolean shouldSwitch(PlayerBot playerBot, Mobile enemy) {
                        return playerBot.getSpecialPercentage() >= 50 &&
                                // Don't switch to Melee if we're frozen
                                playerBot.getMovementQueue().canMove() &&
                                // Switch if the enemy has enabled protect from missles or has lowish health
                                (enemy.getPrayerActive()[PrayerHandler.PROTECT_FROM_MISSILES] || enemy.getHitpoints() < 60);
                    }

                    @Override
                    public void afterSwitch(PlayerBot playerBot) {
                        CombatSpecial.activate(playerBot);
                    }
                },

                // TODO: Switch to Rune crossbow and Dragonstone bolts - but why?
        };
    }

    @Override
    public boolean shouldEat() {
        return false;
    }

    @Override
    public ItemInSlot[] potions() {
        return new ItemInSlot[0];
    }

    @Override
    public int[] food() {
        return new int[] { COOKED_KARAMBWAN, SHARK };
    }
}
