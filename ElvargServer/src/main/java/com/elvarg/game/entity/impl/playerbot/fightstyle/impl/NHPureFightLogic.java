package com.elvarg.game.entity.impl.playerbot.fightstyle.impl;

import com.elvarg.game.content.PrayerHandler;
import com.elvarg.game.content.combat.CombatSpecial;
import com.elvarg.game.content.combat.magic.CombatSpells;
import com.elvarg.game.entity.impl.Mobile;
import com.elvarg.game.entity.impl.playerbot.PlayerBot;
import com.elvarg.game.entity.impl.playerbot.fightstyle.PlayerBotFightLogic;
import com.elvarg.game.entity.impl.playerbot.fightstyle.WeaponSwitch;
import com.elvarg.game.model.ItemInSlot;
import com.elvarg.game.model.movement.MovementQueue;
import com.elvarg.game.task.Task;
import com.elvarg.game.task.TaskManager;
import com.elvarg.util.timers.TimerKey;

import static com.elvarg.util.ItemIdentifiers.*;

public class NHPureFightLogic extends PlayerBotFightLogic {

    private static final WeaponSwitch[] WEAPON_SWITCHES = {
            new WeaponSwitch(CombatSpells.ICE_BARRAGE.getSpell()) {
                @Override
                public boolean shouldUse(PlayerBot playerBot, Mobile enemy) {
                    // Freeze the player if they can move
                    return enemy.getMovementQueue().canMove() && !enemy.getTimers().has(TimerKey.FREEZE_IMMUNITY);
                }

                @Override
                public void afterUse(PlayerBot playerBot) {
                    TaskManager.submit(new Task(2, this, false) {
                        @Override
                        public void execute() {
                            if (playerBot.getMovementQueue().size()> 0) {
                                return;
                            }
                            playerBot.setFollowing(null);
                            MovementQueue.randomClippedStep(playerBot, 1);
                            this.stop();
                        }
                    });

                }
            },

            new WeaponSwitch(DRAGON_DAGGER_P_PLUS_PLUS_) {
                @Override
                public boolean shouldUse(PlayerBot playerBot, Mobile enemy) {
                    return playerBot.getSpecialPercentage() >= 25 &&
                            // Don't switch to Melee if we're frozen
                            playerBot.getMovementQueue().canMove() &&
                            // Switch if the enemy has enabled protect from missles or has low health
                            (enemy.getPrayerActive()[PrayerHandler.PROTECT_FROM_MAGIC] || enemy.getHitpoints() < 50);
                }

                @Override
                public void afterUse(PlayerBot playerBot) {
                    if (!playerBot.isSpecialActivated()) {
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
