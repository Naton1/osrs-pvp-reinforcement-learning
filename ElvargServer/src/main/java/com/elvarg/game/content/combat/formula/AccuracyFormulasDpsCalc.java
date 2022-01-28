package com.elvarg.game.content.combat.formula;

import com.elvarg.game.content.PrayerHandler;
import com.elvarg.game.content.combat.CombatEquipment;
import com.elvarg.game.content.combat.CombatType;
import com.elvarg.game.content.combat.FightStyle;
import com.elvarg.game.entity.impl.Mobile;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.Skill;
import com.elvarg.game.model.equipment.BonusManager;

import java.security.SecureRandom;

public class AccuracyFormulasDpsCalc {
    public static final SecureRandom srand = new SecureRandom();

    public static boolean rollAccuracy(Player player, Player enemy, CombatType style) {
        if (style == CombatType.MELEE) {
            int attRoll = attackMeleeRoll(player);
            int defRoll = defenseMeleeRoll(player, enemy);

            float hitChance = hitChance(attRoll, defRoll);
            return hitChance > srand.nextFloat();

        } else if (style == CombatType.RANGED) {
            int attRoll = attackRangedRoll(player);
            int defRoll = defenseRangedRoll(player, enemy);

            float hitChance = hitChance(attRoll, defRoll);
            return hitChance > srand.nextFloat();
        } else if (style == CombatType.MAGIC) {
            int attRoll = attackMagicRoll(player);
            int defRoll = defenseMagicRoll(player, enemy);

            float hitChance = hitChance(attRoll, defRoll);
            return hitChance > srand.nextFloat();
        }
        return false;
    }

    private static float hitChance(int attRoll, int defRoll) {

        if (attRoll > defRoll) {
            return 1f - ((defRoll + 2f) / (2f * attRoll + 1f));
        } else {
            return attRoll / (2f * defRoll + 1f);
        }
    }

    private static float effectiveAttackLevel(Player player) {
        float att = player.getSkillManager().getCurrentLevel(Skill.ATTACK);

        double prayerBonus = 1;

        // Prayer additions
        if (PrayerHandler.isActivated(player, PrayerHandler.CLARITY_OF_THOUGHT)) {
            prayerBonus = 1.05;
        } else if (PrayerHandler.isActivated(player, PrayerHandler.IMPROVED_REFLEXES)) {
            prayerBonus = 1.10;
        } else if (PrayerHandler.isActivated(player, PrayerHandler.INCREDIBLE_REFLEXES)) {
            prayerBonus = 1.15;
        } else if (PrayerHandler.isActivated(player, PrayerHandler.CHIVALRY)) {
            prayerBonus = 1.15;
        } else if (PrayerHandler.isActivated(player, PrayerHandler.PIETY)) {
            prayerBonus = 1.20;
        }

        att *= prayerBonus;

        FightStyle fightStyle = player.getFightType().getStyle();
        if (fightStyle == FightStyle.ACCURATE)
            att += 3;
        else if (fightStyle == FightStyle.CONTROLLED)
            att += 1;
        att += 8;

        if (CombatEquipment.wearingVoid(player, CombatType.MELEE))
            att = (att * 1.1f);

        // Special attack
        if (player.isSpecialActivated()) {
            att *= player.getCombatSpecial().getAccuracyMultiplier();
        }

        return att;
    }

    private static int attackMeleeRoll(Player player) {
        float attRoll = effectiveAttackLevel(player);

        int attStab = player.getBonusManager().getAttackBonus()[BonusManager.ATTACK_STAB];
        int attSlash = player.getBonusManager().getAttackBonus()[BonusManager.ATTACK_SLASH];
        int attCrush = player.getBonusManager().getAttackBonus()[BonusManager.ATTACK_CRUSH];

        switch (player.getFightType().getBonusType()) {
            case BonusManager.ATTACK_STAB:
                attRoll *= attStab + 64;
                break;
            case BonusManager.ATTACK_SLASH:
                attRoll *= attSlash + 64;
                break;
            case BonusManager.ATTACK_CRUSH:
                attRoll *= attCrush + 64;
                break;
            default:
                int maxAtt = Math.max(attStab, Math.max(attCrush, attSlash));
                attRoll *= maxAtt + 64;
        }

        return (int) attRoll;
    }

    private static float effectiveDefenseLevel(Player player) {
        float def = player.getSkillManager().getCurrentLevel(Skill.DEFENCE);

        double prayerBonus = 1;

        // Prayer additions
        if (PrayerHandler.isActivated(player, PrayerHandler.THICK_SKIN)) {
            prayerBonus = 1.05;
        } else if (PrayerHandler.isActivated(player, PrayerHandler.ROCK_SKIN)) {
            prayerBonus = 1.10;
        } else if (PrayerHandler.isActivated(player, PrayerHandler.STEEL_SKIN)) {
            prayerBonus = 1.15;
        } else if (PrayerHandler.isActivated(player, PrayerHandler.CHIVALRY)) {
            prayerBonus = 1.20;
        } else if (PrayerHandler.isActivated(player, PrayerHandler.PIETY)) {
            prayerBonus = 1.25;
        } else if (PrayerHandler.isActivated(player, PrayerHandler.RIGOUR)) {
            prayerBonus = 1.25;
        } else if (PrayerHandler.isActivated(player, PrayerHandler.AUGURY)) {
            prayerBonus = 1.25;
        }

        def *= prayerBonus;

        FightStyle fightStyle = player.getFightType().getStyle();
        if (fightStyle == FightStyle.DEFENSIVE)
            def += 3;
        else if (fightStyle == FightStyle.CONTROLLED)
            def += 1;
        def += 8;

        if (CombatEquipment.wearingVoid(player, CombatType.MELEE))
            def = (int) (def * 1.1f);

        return def;
    }

    private static int defenseMeleeRoll(Player player, Player enemy) {
        float defLevel = effectiveDefenseLevel(enemy);

        int defStab = enemy.getBonusManager().getDefenceBonus()[BonusManager.DEFENCE_STAB];
        int defSlash = enemy.getBonusManager().getDefenceBonus()[BonusManager.DEFENCE_SLASH];
        int defCrush = enemy.getBonusManager().getDefenceBonus()[BonusManager.DEFENCE_CRUSH];

        switch (player.getFightType().getBonusType()) {
            case BonusManager.ATTACK_STAB:
                defLevel *= defStab + 64;
                break;
            case BonusManager.ATTACK_SLASH:
                defLevel *= defSlash + 64;
                break;
            case BonusManager.ATTACK_CRUSH:
                defLevel *= defCrush + 64;
                break;
            default:
                int maxDef = Math.max(defStab, Math.max(defCrush, defSlash));
                defLevel *= maxDef + 64;
        }

        return (int) defLevel;
    }

    // Ranged

    private static int defenseRangedRoll(Player player, Player enemy) {
        float defLevel = effectiveDefenseLevel(enemy);

        int defRange = enemy.getBonusManager().getDefenceBonus()[BonusManager.DEFENCE_RANGE];

        defLevel *= defRange + 64;

        return (int) defLevel;
    }


    private static float effectiveRangedAttack(Player player) {
        float rngStrength = player.getSkillManager().getCurrentLevel(Skill.RANGED);

        // Prayers
        float prayerMod = 1.0f;
        if (PrayerHandler.isActivated(player, PrayerHandler.SHARP_EYE)) {
            prayerMod = 1.05f;
        } else if (PrayerHandler.isActivated(player, PrayerHandler.HAWK_EYE)) {
            prayerMod = 1.10f;
        } else if (PrayerHandler.isActivated(player, PrayerHandler.EAGLE_EYE)) {
            prayerMod = 1.15f;
        } else if (PrayerHandler.isActivated(player, PrayerHandler.RIGOUR)) {
            prayerMod = 1.23f;
        }
        rngStrength = (rngStrength * prayerMod);

        FightStyle fightStyle = player.getFightType().getStyle();
        if (fightStyle == FightStyle.ACCURATE)
            rngStrength += 3;
        rngStrength += 8;

        if (CombatEquipment.wearingVoid(player, CombatType.RANGED)) {
            rngStrength = (rngStrength * 1.125f);
        }

//        if (dragonHunter(input))
//            rngStrength = (int) (rngStrength * 1.3f);

        return rngStrength;
    }

    public static int attackRangedRoll(Player player) {
        var accuracyBonus = player.getBonusManager().getAttackBonus()[BonusManager.ATTACK_RANGE];

        float attRoll = effectiveRangedAttack(player);
        attRoll *= (accuracyBonus + 64);

        return (int) attRoll;
    }

    // Magic

    private static float effectiveMagicLevel(Player player) {
        float mag = player.getSkillManager().getCurrentLevel(Skill.MAGIC);

        double prayerBonus = 1;

        // Prayer additions
        if (PrayerHandler.isActivated(player, PrayerHandler.MYSTIC_WILL)) {
            prayerBonus = 1.05;
        } else if (PrayerHandler.isActivated(player, PrayerHandler.MYSTIC_LORE)) {
            prayerBonus = 1.10;
        } else if (PrayerHandler.isActivated(player, PrayerHandler.MYSTIC_MIGHT)) {
            prayerBonus = 1.15;
        } else if (PrayerHandler.isActivated(player, PrayerHandler.AUGURY)) {
            prayerBonus = 1.25;
        }

        mag *= prayerBonus;

        FightStyle fightStyle = player.getFightType().getStyle();
        if (fightStyle == FightStyle.ACCURATE)
            mag += 3;
        else if (fightStyle == FightStyle.DEFENSIVE)
            mag += 1;

        mag += 8;

        if (CombatEquipment.wearingVoid(player, CombatType.MAGIC))
            mag = (int) (mag * 1.45f);

        return mag;
    }

    private static int defenseMagicRoll(Player player, Player enemy) {
        float defLevel = effectiveMagicLevel(enemy);

        int defRange = enemy.getBonusManager().getDefenceBonus()[BonusManager.DEFENCE_MAGIC];

        defLevel *= defRange + 64;

        return (int) defLevel;
    }

    public static int attackMagicRoll(Player player) {
        var accuracyBonus = player.getBonusManager().getAttackBonus()[BonusManager.ATTACK_MAGIC];

        float attRoll = effectiveMagicLevel(player);
        attRoll *= (accuracyBonus + 64);

        return (int) attRoll;
    }
}
