package com.elvarg.game.content.combat.formula;

import com.elvarg.game.content.PrayerHandler;
import com.elvarg.game.content.combat.CombatEquipment;
import com.elvarg.game.content.combat.CombatFactory;
import com.elvarg.game.content.combat.CombatType;
import com.elvarg.game.content.combat.FightStyle;
import com.elvarg.game.entity.impl.Mobile;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.Skill;
import com.elvarg.game.model.equipment.BonusManager;
import com.elvarg.util.Misc;

import java.security.SecureRandom;

public class AccuracyFormulasDpsCalc {
    public static final SecureRandom srand = new SecureRandom();

    public static boolean rollMeleeAccuracy(Mobile entity, Mobile enemy, int attRoll) {
    	int defRoll = defenseMeleeRoll(entity, enemy);
    	float hitChance = hitChance(attRoll, defRoll);
    	return hitChance > srand.nextFloat();
    }

    public static boolean rollAccuracy(Mobile entity, Mobile enemy, CombatType style) {
        int attRoll;
        int defRoll;
        if (style == CombatType.MELEE) {
            attRoll = attackMeleeRoll(entity);
            defRoll = defenseMeleeRoll(entity, enemy);
        }
        else if (style == CombatType.RANGED) {
            attRoll = attackRangedRoll(entity);
            defRoll = defenseRangedRoll(enemy);
        }
        else if (style == CombatType.MAGIC) {
            attRoll = attackMagicRoll(entity);
            defRoll = defenseMagicRoll(enemy);
        }
        else {
            return false;
        }
        return rollAccuracy(attRoll, defRoll);
    }

    public static boolean rollAccuracy(int attRoll, int defRoll) {
        float hitChance = hitChance(attRoll, defRoll);
        return hitChance > srand.nextFloat();
    }

    public static float hitChance(int attRoll, int defRoll) {

        if (attRoll > defRoll) {
            return 1f - ((defRoll + 2f) / (2f * (attRoll + 1f)));
        }
        else {
            return attRoll / (2f * (defRoll + 1f));
        }
    }

    private static int effectiveAttackLevel(Mobile entity) {

        if (entity.isNpc()) {
            return entity.getAsNpc().getCurrentDefinition().getStats()[0] + 8;
        }

        Player player = entity.getAsPlayer();

        float att = player.getSkillManager().getCurrentLevel(Skill.ATTACK);

        float prayerBonus = 1;

        // Prayer additions
        if (PrayerHandler.isActivated(player, PrayerHandler.CLARITY_OF_THOUGHT)) {
            prayerBonus = 1.05f;
        }
        else if (PrayerHandler.isActivated(player, PrayerHandler.IMPROVED_REFLEXES)) {
            prayerBonus = 1.10f;
        }
        else if (PrayerHandler.isActivated(player, PrayerHandler.INCREDIBLE_REFLEXES)) {
            prayerBonus = 1.15f;
        }
        else if (PrayerHandler.isActivated(player, PrayerHandler.CHIVALRY)) {
            prayerBonus = 1.15f;
        }
        else if (PrayerHandler.isActivated(player, PrayerHandler.PIETY)) {
            prayerBonus = 1.20f;
        }

        att *= prayerBonus;
        att = (float) Math.floor(att);

        FightStyle fightStyle = player.getFightType().getStyle();
        if (fightStyle == FightStyle.ACCURATE) {
            att += 3;
        }
        else if (fightStyle == FightStyle.CONTROLLED) {
            att += 1;
        }

        att += 8;

        if (CombatEquipment.wearingVoid(player, CombatType.MELEE)) {
            att *= 1.1;
        }

        // Special attack
        if (player.isSpecialActivated()) {
            att *= player.getCombatSpecial().getAccuracyMultiplier();
        }

        return (int) Math.floor(att);
    }

    public static int attackMeleeRoll(Mobile entity) {
        int attRoll = effectiveAttackLevel(entity);

        if (entity.isNpc()) {
            // NPC's don't currently have stab/slash/crush bonuses
            attRoll *= 64;
            return attRoll;
        }

        Player player = entity.getAsPlayer();

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
                // Should throw exception? this shouldn't happen
                int maxAtt = Math.max(attStab, Math.max(attCrush, attSlash));
                attRoll *= maxAtt + 64;
        }

        // Include any target specific gear here if/when supported (ex. salve amulet)

        return attRoll;
    }

    private static int effectiveDefenseLevel(Mobile enemy) {

        if (enemy.isNpc()) {
            return enemy.getAsNpc().getCurrentDefinition().getStats()[2] + 9;
        }

        Player player = enemy.getAsPlayer();
        float def = player.getSkillManager().getCurrentLevel(Skill.DEFENCE);

        float prayerBonus = 1;

        // Prayer additions
        if (PrayerHandler.isActivated(enemy, PrayerHandler.THICK_SKIN)) {
            prayerBonus = 1.05f;
        }
        else if (PrayerHandler.isActivated(enemy, PrayerHandler.ROCK_SKIN)) {
            prayerBonus = 1.10f;
        }
        else if (PrayerHandler.isActivated(enemy, PrayerHandler.STEEL_SKIN)) {
            prayerBonus = 1.15f;
        }
        else if (PrayerHandler.isActivated(enemy, PrayerHandler.CHIVALRY)) {
            prayerBonus = 1.20f;
        }
        else if (PrayerHandler.isActivated(enemy, PrayerHandler.PIETY)) {
            prayerBonus = 1.25f;
        }
        else if (PrayerHandler.isActivated(enemy, PrayerHandler.RIGOUR)) {
            prayerBonus = 1.25f;
        }
        else if (PrayerHandler.isActivated(enemy, PrayerHandler.AUGURY)) {
            prayerBonus = 1.25f;
        }

        def *= prayerBonus;
        def = (float) Math.floor(def);

        FightStyle fightStyle = player.getFightType().getStyle();
        if (fightStyle == FightStyle.DEFENSIVE) {
            def += 3;
        }
        else if (fightStyle == FightStyle.CONTROLLED) {
            def += 1;
        }
        def += 8;

        return (int) Math.floor(def);
    }

    public static int defenseMeleeRoll(Mobile entity, Mobile enemy) {
        int bonusType = (entity.isNpc() ? 3 /* Default case */ : entity.getAsPlayer().getFightType().getBonusType());

        return defenseMeleeRoll(enemy, bonusType);
    }

    public static int defenseMeleeRoll(Mobile enemy, int bonusType) {
        int defLevel = effectiveDefenseLevel(enemy);

        Player enemyPlayer = enemy.getAsPlayer();

        // NPCs don't have defence bonuses currently
        int defStab = (enemy.isNpc() ? 0 : enemyPlayer.getBonusManager().getDefenceBonus()[BonusManager.DEFENCE_STAB]);
        int defSlash = (enemy.isNpc() ? 0 : enemyPlayer.getBonusManager().getDefenceBonus()[BonusManager.DEFENCE_SLASH]);
        int defCrush = (enemy.isNpc() ? 0 : enemyPlayer.getBonusManager().getDefenceBonus()[BonusManager.DEFENCE_CRUSH]);

        switch (bonusType) {
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

        return defLevel;
    }

    // Ranged

    public static int defenseRangedRoll(Mobile enemy) {
        int defLevel = effectiveDefenseLevel(enemy);

        int defRange = enemy.isPlayer() ?
                enemy.getAsPlayer().getBonusManager().getDefenceBonus()[BonusManager.DEFENCE_RANGE]
                : 0;

        defLevel *= defRange + 64;

        return defLevel;
    }


    private static int effectiveRangedAttack(Mobile entity) {

        if (entity.isNpc()) {
            // Prayer bonuses don't apply to NPCs (yet)
            return entity.getAsNpc().getCurrentDefinition().getStats()[3] + 8;
        }

        Player player = entity.getAsPlayer();
        float rangeAccuracy = player.getSkillManager().getCurrentLevel(Skill.RANGED);

        // Prayers
        float prayerMod = 1.0f;
        if (PrayerHandler.isActivated(player, PrayerHandler.SHARP_EYE)) {
            prayerMod = 1.05f;
        }
        else if (PrayerHandler.isActivated(player, PrayerHandler.HAWK_EYE)) {
            prayerMod = 1.10f;
        }
        else if (PrayerHandler.isActivated(player, PrayerHandler.EAGLE_EYE)) {
            prayerMod = 1.15f;
        }
        else if (PrayerHandler.isActivated(player, PrayerHandler.RIGOUR)) {
            prayerMod = 1.2f;
        }

        rangeAccuracy *= prayerMod;
        rangeAccuracy = (float) Math.floor(rangeAccuracy);

        FightStyle fightStyle = player.getFightType().getStyle();
        if (fightStyle == FightStyle.ACCURATE) {
            rangeAccuracy += 3;
        }

        rangeAccuracy += 8;

        if (CombatEquipment.wearingVoid(player, CombatType.RANGED)) {
            rangeAccuracy *= 1.1;
        }

        return (int) Math.floor(rangeAccuracy);
    }

    public static int attackRangedRoll(Mobile entity) {
        int accuracyBonus =
                entity.isNpc() ? 0 : entity.getAsPlayer().getBonusManager().getAttackBonus()[BonusManager.ATTACK_RANGE];

        int attLevel = effectiveRangedAttack(entity);
        int attRoll = attLevel * (accuracyBonus + 64);

        // Salve amulet/twisted bow bonus if/when added

        return attRoll;
    }

    // Magic

    private static int effectiveMagicLevel(Mobile entity, boolean defensive) {

        if (entity.isNpc()) {
            // Prayer bonuses don't apply to NPCs (yet)
            return entity.getAsNpc().getCurrentDefinition().getStats()[4] + 9;
        }

        Player player = entity.getAsPlayer();
        float mag = player.getSkillManager().getCurrentLevel(Skill.MAGIC);

        float prayerBonus = 1;

        // Prayer additions
        if (PrayerHandler.isActivated(player, PrayerHandler.MYSTIC_WILL)) {
            prayerBonus = 1.05f;
        }
        else if (PrayerHandler.isActivated(player, PrayerHandler.MYSTIC_LORE)) {
            prayerBonus = 1.10f;
        }
        else if (PrayerHandler.isActivated(player, PrayerHandler.MYSTIC_MIGHT)) {
            prayerBonus = 1.15f;
        }
        else if (PrayerHandler.isActivated(player, PrayerHandler.AUGURY)) {
            prayerBonus = 1.25f;
        }

        mag *= prayerBonus;
        mag = (float) Math.floor(mag);

        // Offensive magic level checks a few extra things
        if (!defensive) {
            if (CombatEquipment.wearingVoid(player, CombatType.MAGIC)) {
                mag = (int) (mag * 1.45f);
            }

            // Accurate trident should add +2 when supported

            mag += 9;
        }

        return (int) Math.floor(mag);
    }

    public static int defenseMagicRoll(Mobile enemy) {
        // Logic is different depending on npc vs player
        if (enemy.isNpc()) {
            int npcMagicLevel = enemy.getAsNpc().getCurrentDefinition().getStats()[4];
            int npcMagicDefence = 0; // always 0 right now
            return (9 + npcMagicLevel) * (npcMagicDefence + 64);
        }
        int magicLevelPart = (int) Math.floor(effectiveMagicLevel(enemy, true) * 0.7);
        int defLevelPart = (int) Math.floor(effectiveDefenseLevel(enemy) * 0.3);

        int defLevel = magicLevelPart + defLevelPart;
        int defBonus = enemy.getAsPlayer().getBonusManager().getDefenceBonus()[BonusManager.DEFENCE_MAGIC];

        return defLevel * (defBonus + 64);
    }

    public static int attackMagicRoll(Mobile entity) {
        int accuracyBonus = (entity.isNpc() ? 0 :
                           entity.getAsPlayer().getBonusManager().getAttackBonus()[BonusManager.ATTACK_MAGIC]);

        int attLevel = effectiveMagicLevel(entity, false);
        int attRoll = attLevel * (accuracyBonus + 64);

        // If/when supported: multiply by 1.15 if wearing a slayer helm on task or killing undead monsters with an
        // imbued salve amulet.
        if (entity.getCombat().getCastSpell() != null) {
            attRoll = (int) Math.floor(attRoll * entity.getCombat().getCastSpell().getAccuracyMultiplier(entity));
        }

        return attRoll;
    }
}
