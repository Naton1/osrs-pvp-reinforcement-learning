package com.elvarg.game.entity.impl.playerbot.fightstyle.impl;

import com.elvarg.game.content.PrayerHandler;
import com.elvarg.game.content.combat.magic.CombatSpell;
import com.elvarg.game.entity.impl.Mobile;
import com.elvarg.game.entity.impl.playerbot.PlayerBot;
import com.elvarg.game.entity.impl.playerbot.fightstyle.PlayerBotFightStyle;
import com.elvarg.game.entity.impl.playerbot.fightstyle.WeaponSwitch;
import com.elvarg.game.model.ItemInSlot;

import static com.elvarg.util.ItemIdentifiers.*;

public class ObbyMaulerFightStyle extends PlayerBotFightStyle {

    @Override
    public int getMainWeaponId() {
        return RUNE_KNIFE;
    }

    @Override
    public WeaponSwitch[] getWeaponSwitches()  {
        return new WeaponSwitch[] {

                new WeaponSwitch(new ItemInSlot(TZHAAR_KET_OM, 12)) {
                    @Override
                    public boolean shouldSwitch(PlayerBot playerBot, Mobile attacker) {
                        return playerBot.getInventory().getFreeSlots() > 0 &&
                                // Don't switch if we're frozen
                                playerBot.getMovementQueue().canMove() &&
                                // Switch if the enemy has enabled protect from missles or has low health
                                (attacker.getPrayerActive()[PrayerHandler.PROTECT_FROM_MISSILES] || attacker.getHitpoints() < 50);
                    }
                },

        };
    }

    @Override
    public boolean shouldEat() {
        return false;
    }

    @Override
    public ItemInSlot[] potions() {
        return new ItemInSlot[] {
            new ItemInSlot(SUPER_STRENGTH_4_, 0)
        };
    }

    @Override
    public int[] food() {
        return new int[] { COOKED_KARAMBWAN, SHARK };
    }
}
