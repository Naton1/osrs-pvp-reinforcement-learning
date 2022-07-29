package com.elvarg.game.entity.impl.playerbot.fightstyle.impl;

import com.elvarg.game.content.PrayerHandler;
import com.elvarg.game.content.combat.CombatSpecial;
import com.elvarg.game.entity.impl.Mobile;
import com.elvarg.game.entity.impl.playerbot.PlayerBot;
import com.elvarg.game.entity.impl.playerbot.fightstyle.PlayerBotFightLogic;
import com.elvarg.game.entity.impl.playerbot.fightstyle.WeaponSwitch;
import com.elvarg.game.model.ItemInSlot;

import static com.elvarg.util.ItemIdentifiers.*;

public class GMaulerFightLogic extends PlayerBotFightLogic {

    private static final WeaponSwitch[] WEAPON_SWITCHES = {
            new WeaponSwitch(GRANITE_MAUL) {
                @Override
                public boolean shouldUse(PlayerBot playerBot, Mobile enemy) {
                    return playerBot.getSpecialPercentage() >= 50 &&
                            // Don't switch to Melee if we're frozen
                            playerBot.getMovementQueue().canMove() &&
                            // Switch if the enemy has enabled protect from missles or has lowish health
                            (enemy.getPrayerActive()[PrayerHandler.PROTECT_FROM_MISSILES] || enemy.getHitpoints() < 45);
                }

                @Override
                public void afterUse(PlayerBot playerBot) {
                    if(!playerBot.isSpecialActivated()) {
                        CombatSpecial.activate(playerBot);
                    }
                }
            },
    };

    @Override
    public int getMainWeaponId() {
        return MAGIC_SHORTBOW;
    }

    @Override
    public WeaponSwitch[] getWeaponSwitches() {
        return WEAPON_SWITCHES;
    }
}
