package com.elvarg.game.entity.impl.playerbot.fightstyle.impl;

import com.elvarg.game.content.PrayerHandler;
import com.elvarg.game.content.combat.CombatSpecial;
import com.elvarg.game.entity.impl.Mobile;
import com.elvarg.game.entity.impl.playerbot.PlayerBot;
import com.elvarg.game.entity.impl.playerbot.fightstyle.PlayerBotFightLogic;
import com.elvarg.game.entity.impl.playerbot.fightstyle.WeaponSwitch;
import com.elvarg.game.model.ItemInSlot;

import static com.elvarg.util.ItemIdentifiers.*;

public class ObbyMaulerFightLogic extends PlayerBotFightLogic {

    private static final WeaponSwitch[] WEAPON_SWITCHES = {
            new WeaponSwitch(TZHAAR_KET_OM) {
                @Override
                public boolean shouldUse(PlayerBot playerBot, Mobile attacker) {
                    return playerBot.getInventory().getFreeSlots() > 0 &&
                            // Don't switch if we're frozen
                            playerBot.getMovementQueue().canMove() &&
                            // Switch if the enemy has enabled protect from missles or has low health
                            (attacker.getPrayerActive()[PrayerHandler.PROTECT_FROM_MISSILES] || attacker.getHitpoints() < 50);
                }
            },
    };

    @Override
    public int getMainWeaponId() {
        return RUNE_KNIFE;
    }

    @Override
    public WeaponSwitch[] getWeaponSwitches() {
        return WEAPON_SWITCHES;
    }
}
