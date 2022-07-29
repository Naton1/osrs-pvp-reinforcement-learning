package com.elvarg.game.entity.impl.playerbot.fightstyle.impl;

import com.elvarg.game.content.combat.CombatSpecial;
import com.elvarg.game.entity.impl.Mobile;
import com.elvarg.game.entity.impl.playerbot.PlayerBot;
import com.elvarg.game.entity.impl.playerbot.fightstyle.PlayerBotFightLogic;
import com.elvarg.game.entity.impl.playerbot.fightstyle.WeaponSwitch;
import com.elvarg.game.model.ItemInSlot;

import static com.elvarg.util.ItemIdentifiers.*;

public class DDSPureRFightLogic extends PlayerBotFightLogic {

    private static final WeaponSwitch[] WEAPON_SWITCHES = {
            new WeaponSwitch(DRAGON_DAGGER_P_PLUS_PLUS_) {
                @Override
                public boolean shouldUse(PlayerBot playerBot, Mobile enemy) {
                    return playerBot.getSpecialPercentage() >= 25 &&
                            // Switch if the enemy has lowish health
                            enemy.getHitpoints() < 45;
                }

                @Override
                public void afterUse(PlayerBot playerBot) {
                    if(!playerBot.isSpecialActivated()) {
                        System.out.println("Activate special");
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
