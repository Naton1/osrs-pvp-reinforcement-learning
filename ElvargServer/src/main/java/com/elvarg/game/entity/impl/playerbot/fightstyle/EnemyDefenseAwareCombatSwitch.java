package com.elvarg.game.entity.impl.playerbot.fightstyle;

import com.elvarg.game.content.PrayerHandler;
import com.elvarg.game.content.combat.CombatType;
import com.elvarg.game.content.combat.formula.AccuracyFormulasDpsCalc;
import com.elvarg.game.content.combat.formula.DamageFormulas;
import com.elvarg.game.entity.impl.Mobile;
import com.elvarg.game.entity.impl.playerbot.PlayerBot;
import com.elvarg.game.model.ItemInSlot;
import com.elvarg.game.model.equipment.BonusManager;
import com.elvarg.net.packet.impl.EquipPacketListener;

public abstract class EnemyDefenseAwareCombatSwitch implements CombatAction {

    private final AttackStyleSwitch[] styleSwitches;

    public EnemyDefenseAwareCombatSwitch(AttackStyleSwitch[] styleSwitches) {
        this.styleSwitches = styleSwitches;
    }

    @Override
    public final void perform(PlayerBot playerBot, Mobile enemy) {
        AttackStyleSwitch bestSwitch = null;
        float bestDps = 0.0f;

        for (var styleSwitch : styleSwitches) {
            if (!styleSwitch.getCombatSwitch().shouldPerform(playerBot, enemy)) {
                continue;
            }
            int defenseRoll = 1;
            int maxHit = styleSwitch.getMaxHit();

            if (styleSwitch.getCombatType() == CombatType.MELEE) {
                // TODO: logic to pick correct bonus
                defenseRoll = AccuracyFormulasDpsCalc.defenseMeleeRoll(enemy, BonusManager.ATTACK_SLASH);
                if (enemy.getPrayerActive()[PrayerHandler.PROTECT_FROM_MELEE]) {
                    maxHit *= 0.7;
                }
            } else if (styleSwitch.getCombatType() == CombatType.RANGED) {
                defenseRoll = AccuracyFormulasDpsCalc.defenseRangedRoll(enemy);
                if (enemy.getPrayerActive()[PrayerHandler.PROTECT_FROM_MISSILES]) {
                    maxHit *= 0.7;
                }
            } else if (styleSwitch.getCombatType() == CombatType.MAGIC) {
                defenseRoll = AccuracyFormulasDpsCalc.defenseMagicRoll(enemy);
                if (enemy.getPrayerActive()[PrayerHandler.PROTECT_FROM_MAGIC]) {
                    maxHit *= 0.7;
                }
            }

            float hitChance = AccuracyFormulasDpsCalc.hitChance(styleSwitch.getAttackRoll(), defenseRoll);

            float hitsPerSec = 1000.f / (600 * styleSwitch.getHitSpeed());

            float dps = hitChance * maxHit * hitsPerSec;

            if (dps > bestDps) {
                bestDps = dps;
                bestSwitch = styleSwitch;
            }
        }

        if (bestSwitch == null) {
            return;
        }

        bestSwitch.getCombatSwitch().perform(playerBot, enemy);
        bestSwitch.setHitSpeed(playerBot.getBaseAttackSpeed());

        if (bestSwitch.getCombatType() == CombatType.MELEE) {
            bestSwitch.setAttackRoll(AccuracyFormulasDpsCalc.attackMeleeRoll(playerBot));
            bestSwitch.setMaxHit(DamageFormulas.calculateMaxMeleeHit(playerBot));
        } else if (bestSwitch.getCombatType() == CombatType.RANGED) {
            bestSwitch.setAttackRoll(AccuracyFormulasDpsCalc.attackRangedRoll(playerBot));
            bestSwitch.setMaxHit(DamageFormulas.calculateMaxRangedHit(playerBot));
        } else if (bestSwitch.getCombatType() == CombatType.MAGIC) {
            bestSwitch.setAttackRoll(AccuracyFormulasDpsCalc.attackMagicRoll(playerBot));
            bestSwitch.setMaxHit(DamageFormulas.getMagicMaxhit(playerBot));
        }
    }


}
