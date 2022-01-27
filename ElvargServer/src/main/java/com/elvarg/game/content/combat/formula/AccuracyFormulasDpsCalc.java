package com.elvarg.game.content.combat.formula;

import com.elvarg.game.content.PrayerHandler;
import com.elvarg.game.content.combat.CombatEquipment;
import com.elvarg.game.content.combat.CombatFactory;
import com.elvarg.game.content.combat.CombatType;
import com.elvarg.game.content.combat.FightStyle;
import com.elvarg.game.entity.impl.Mobile;
import com.elvarg.game.entity.impl.npc.NPC;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.Skill;
import com.elvarg.game.model.container.impl.Equipment;
import com.elvarg.game.model.equipment.BonusManager;
import com.elvarg.util.ItemIdentifiers;
import com.elvarg.util.Misc;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.stream.IntStream;

public class AccuracyFormulasDpsCalc {
    public static final SecureRandom srand = new SecureRandom();

    public static boolean rollAccuracy(Mobile entity, Mobile enemy, CombatType style) {
        if (style == CombatType.MELEE) {
            if (entity.isPlayer() && enemy.isPlayer()) {
                var p = (Player) entity;
                var e = (Player) enemy;
                int attRoll = attackRoll(p);
                int defRoll = defenseRoll(p, e);

                float hitChance = hitChance(attRoll, defRoll);
                System.out.println("\nYour chance to hit is: " + hitChance + "%");
                System.out.println("attRoll " + attRoll + "");
                System.out.println("defRoll " + defRoll + "");

                return hitChance > srand.nextFloat();
            }
        }
        return false;
    }

    private static float hitChance(int attRoll, int defRoll)
    {

        if (attRoll > defRoll)
        {
            return 1f - ((defRoll + 2f) / (2f * attRoll + 1f));
        }
        else
        {
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

    private static int attackRoll(Player player) {
        float attRoll = effectiveAttackLevel(player);

        int attStab =  player.getBonusManager().getAttackBonus()[BonusManager.ATTACK_STAB];
        int attSlash =  player.getBonusManager().getAttackBonus()[BonusManager.ATTACK_SLASH];
        int attCrush =  player.getBonusManager().getAttackBonus()[BonusManager.ATTACK_CRUSH];

        switch (player.getFightType().getBonusType())
        {
            case BonusManager.ATTACK_STAB:
                attRoll *= attStab + 64;
                break;
            case BonusManager.ATTACK_SLASH:
                attRoll *= attSlash + 64;
                break;
            case BonusManager.ATTACK_CRUSH:
                attRoll *=  attCrush + 64;
                break;
            default:
                int maxAtt = Math.max(attStab, Math.max(attCrush, attSlash));
                attRoll *= maxAtt + 64;
        }

        return (int)attRoll;
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

    private static int defenseRoll(Player player, Player enemy) {
        float defLevel = effectiveDefenseLevel(enemy);

        int defStab =  enemy.getBonusManager().getDefenceBonus()[BonusManager.DEFENCE_STAB];
        int defSlash =  enemy.getBonusManager().getDefenceBonus()[BonusManager.DEFENCE_SLASH];
        int defCrush =  enemy.getBonusManager().getDefenceBonus()[BonusManager.DEFENCE_CRUSH];

        switch (player.getFightType().getBonusType())
        {
            case BonusManager.ATTACK_STAB:
                defLevel *= defStab + 64;
                break;
            case BonusManager.ATTACK_SLASH:
                defLevel *= defSlash + 64;
                break;
            case BonusManager.ATTACK_CRUSH:
                defLevel *=  defCrush + 64;
                break;
            default:
                int maxDef = Math.max(defStab, Math.max(defCrush, defSlash));
                defLevel *= maxDef + 64;
        }

        return (int)defLevel;
    }
}
