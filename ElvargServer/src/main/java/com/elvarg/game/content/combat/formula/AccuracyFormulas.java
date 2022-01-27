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

public class AccuracyFormulas {
    public static final SecureRandom srand = new SecureRandom();

    public static boolean rollAccuracy(Mobile entity, Mobile enemy, CombatType style) {
        double multiplier = 1.0;

        if (style == CombatType.MELEE) {
            if (CombatFactory.fullVeracs(entity)) {
                if (Misc.getRandom(4) == 1) {
                    return true;
                }
            }
        }

        // That is a bit more up to date.
        // TODO: Implement similar for mobs
        if (entity instanceof Player player && enemy instanceof Player enemyPlayer) {
            return AccuracyFormulasDpsCalc.rollAccuracy(player, enemyPlayer, style);
        }
/*
			S E T T I N G S

			S T A R T
		*/

        //attack stances
        int off_stance_bonus = 0;
        int def_stance_bonus = 0;
        if (entity.isPlayer()) {
            final var attackStyle = ((Player) entity).getFightType().getStyle();
            off_stance_bonus = attackStyle.equals(FightStyle.ACCURATE) ? 3 : attackStyle.equals(FightStyle.CONTROLLED) ? 1 : 0; //accurate, aggressive, controlled, defensive
            def_stance_bonus = attackStyle.equals(FightStyle.DEFENSIVE) ? 3 : attackStyle.equals(FightStyle.CONTROLLED) ? 1 : 0; //accurate, aggressive, controlled, defensive
        }

        //requirements
        int off_weapon_requirement = 1; //weapon attack level requirement
        int off_spell_requirement = 1; //spell magic level requirement

        //base levels
        int off_base_attack_level = 1;
        int off_base_ranged_level = 1;
        int off_base_magic_level = 1;
        int attackerWeaponId = -1;
        double twistedBowMultiplier = 1.0; // Defaults to no change (x1)
        double toktz_bonus = 1.0;

        if (entity.isPlayer()) {
            Player player = ((Player) entity);
            final var weapon = player.getEquipment().get(Equipment.WEAPON_SLOT);

            if (player.isSpecialActivated()) {
                multiplier = player.getCombatSpecial().getAccuracyMultiplier();
            }

            if (weapon != null) {
                attackerWeaponId = weapon.getId(); // Used below in Twisted bow computation.

                final var requiredLevels = weapon.getDefinition().getRequirements();
                if (requiredLevels != null) {
                    final Integer requiredLevel = requiredLevels[Skill.ATTACK.ordinal()];
                    if (requiredLevel != null)
                        off_weapon_requirement = requiredLevel;
                }
            }
            var attackLevel = player.getSkillManager().getMaxLevel(Skill.ATTACK);
            off_base_attack_level = (int) attackLevel + (attackLevel / 3);
            off_base_ranged_level = player.getSkillManager().getMaxLevel(Skill.RANGED);
            off_base_magic_level = player.getSkillManager().getMaxLevel(Skill.MAGIC);
        } else {
            NPC npc = ((NPC) entity);
            if (npc.getDefinition() != null && npc.getDefinition().getStats() != null) {
                off_base_attack_level = npc.getDefinition().getStats()[0];
                off_base_ranged_level = npc.getDefinition().getStats()[3];
                off_base_magic_level = npc.getDefinition().getStats()[4];
            } else {
//				logger.info("Npc id {} index {} name {} missing combat info or stats.", npc.getId(), npc.getIndex(), npc.getDefinition().getName());
            }
        }

        //current levels
        double off_current_attack_level = 1;
        double off_current_ranged_level = 1;
        double off_current_magic_level = 1;

        if (entity.isPlayer()) {
            Player player = ((Player) entity);
            off_current_attack_level = player.getSkillManager().getCurrentLevel(Skill.ATTACK);
            off_current_ranged_level = player.getSkillManager().getCurrentLevel(Skill.RANGED);
            off_current_magic_level = player.getSkillManager().getCurrentLevel(Skill.MAGIC);
        } else {
            NPC npc = ((NPC) entity);
            if (npc.getDefinition() != null && npc.getDefinition().getStats() != null) {
                off_current_attack_level = npc.getDefinition().getStats()[0];
                off_current_ranged_level = npc.getDefinition().getStats()[3];
                off_current_magic_level = npc.getDefinition().getStats()[4];
            }
        }

        off_current_attack_level *= 1.1;
        off_current_ranged_level *= 1.15;
        off_current_magic_level *= 1.15;

        double def_current_defence_level = 1;
        double def_current_magic_level = 1;

        if (enemy.isPlayer()) {
            Player opp = (Player) enemy;
            def_current_defence_level = opp.getSkillManager().getCurrentLevel(Skill.DEFENCE);
            def_current_magic_level = opp.getSkillManager().getCurrentLevel(Skill.MAGIC);

//			int hpmissing = opp.maxHp() - opp.hp();
//			if (hpmissing > 0 && Equipment.hasAmmyOfDamned(opp) && Equipment.fullTorag(opp)) {
//				// Calc % increase. 1% per 1hp missing.
//				double multi = 0.01D * hpmissing;
//				def_current_defence_level += def_current_defence_level * multi;
//			}
        } else {
            NPC npc = (NPC) enemy;
            if (npc.getDefinition() != null && npc.getDefinition().getStats() != null) {
                def_current_defence_level = npc.getDefinition().getStats()[2];
                def_current_magic_level = npc.getDefinition().getStats()[4];
            }
        }

//		if (entity.isPlayer()) {
//			if (attackerWeaponId == TWISTED_BOW) {
//				twistedBowMultiplier = twistedBowAccuracyMultiplier((int) def_current_magic_level);
//			}
//			if (CombatFormula.obbyArmour((Player) entity) && CombatFormula.hasObbyWeapon((Player) entity)) {
//				toktz_bonus = 1.10;
//			}
//		}

        //prayer bonuses
        double off_attack_prayer_bonus = 1.0;
        double off_ranged_prayer_bonus = 1.0;
        double off_magic_prayer_bonus = 1.0;
        double def_defence_prayer_bonus = 1.0;

        // Prayers
        if (entity.isPlayer()) {
            Player p = (Player) entity;
            if (PrayerHandler.isActivated(p, PrayerHandler.CLARITY_OF_THOUGHT))
                off_attack_prayer_bonus += 0.05; // 5% attack level boost
            else if (PrayerHandler.isActivated(p, PrayerHandler.IMPROVED_REFLEXES))
                off_attack_prayer_bonus += 0.10; // 10% attack level boost
            else if (PrayerHandler.isActivated(p, PrayerHandler.INCREDIBLE_REFLEXES))
                off_attack_prayer_bonus += 0.15; // 15% attack level boost
            else if (PrayerHandler.isActivated(p, PrayerHandler.CHIVALRY))
                off_attack_prayer_bonus += 0.15; // 15% attack level boost
            else if (PrayerHandler.isActivated(p, PrayerHandler.PIETY))
                off_attack_prayer_bonus += 0.20; // 20% attack level boost

            if (PrayerHandler.isActivated(p, PrayerHandler.SHARP_EYE))
                off_ranged_prayer_bonus += 0.05; // 5% range level boost
            else if (PrayerHandler.isActivated(p, PrayerHandler.HAWK_EYE))
                off_ranged_prayer_bonus += 0.10; // 10% range level boost
            else if (PrayerHandler.isActivated(p, PrayerHandler.EAGLE_EYE))
                off_ranged_prayer_bonus += 0.15; // 15% range level boost
            else if (PrayerHandler.isActivated(p, PrayerHandler.RIGOUR))
                off_ranged_prayer_bonus += 0.20; // 20% range level boost

            if (PrayerHandler.isActivated(p, PrayerHandler.MYSTIC_WILL))
                off_magic_prayer_bonus += 0.05; // 5% magic level boost
            else if (PrayerHandler.isActivated(p, PrayerHandler.MYSTIC_LORE))
                off_magic_prayer_bonus += 0.10; // 10% magic level boost
            else if (PrayerHandler.isActivated(p, PrayerHandler.MYSTIC_MIGHT))
                off_magic_prayer_bonus += 0.15; // 15% magic level boost
            else if (PrayerHandler.isActivated(p, PrayerHandler.AUGURY))
                off_magic_prayer_bonus += 0.25; // 25% magic level boost
        }

        if (enemy.isPlayer()) {
            Player p = (Player) enemy;

            if (PrayerHandler.isActivated(p, PrayerHandler.THICK_SKIN))
                def_defence_prayer_bonus += 0.05; // 5% def level boost
            else if (PrayerHandler.isActivated(p, PrayerHandler.ROCK_SKIN))
                def_defence_prayer_bonus += 0.10; // 10% def level boost
            else if (PrayerHandler.isActivated(p, PrayerHandler.STEEL_SKIN))
                def_defence_prayer_bonus += 0.15; // 15% def level boost

            if (PrayerHandler.isActivated(p, PrayerHandler.CHIVALRY))
                def_defence_prayer_bonus += 0.20; // 20% def level boost
            else if (PrayerHandler.isActivated(p, PrayerHandler.PIETY))
                def_defence_prayer_bonus += 0.25; // 25% def level boost
            else if (PrayerHandler.isActivated(p, PrayerHandler.RIGOUR))
                def_defence_prayer_bonus += 0.25; // 25% def level boost
            else if (PrayerHandler.isActivated(p, PrayerHandler.AUGURY))
                def_defence_prayer_bonus += 0.25; // 25% def level boost
        }

        //additional bonus
        double off_additional_bonus = multiplier;


        //if the player is using a slayer helm
        if (entity.isPlayer() && enemy.isNpc()) {
            final NPC npc = (NPC) enemy;
            final Player player = (Player) entity;
            final var helm = player.getEquipment().get(Equipment.HEAD_SLOT);

            if (helm != null && player.getSlayerTask() != null && Arrays.stream(player.getSlayerTask().getTask().getNpcNames()).anyMatch((name) -> name.equalsIgnoreCase(npc.getDefinition().getName()))) {
                if (helm.getId() == 11864 || helm.getId() == 19647 || helm.getId() == 19643 || helm.getId() == 19639 || (IntStream.range(8901, 8921).anyMatch(id -> id == helm.getId()))) {
                    off_additional_bonus += 0.125;
                }
            }
        }

        //Magic on lava dragons
        if (entity.isPlayer() && enemy.isNpc()) {
            final NPC npc = (NPC) enemy;

            if (npc.getId() == 6593 && style.equals(CombatType.MAGIC)) {
                off_additional_bonus += 0.500;
            }
        }

        //if the player is using a smoke battlestaff
        if (entity.isPlayer()) {
            if ((attackerWeaponId == ItemIdentifiers.SMOKE_BATTLESTAFF || attackerWeaponId == ItemIdentifiers.MYSTIC_SMOKE_STAFF) && style.equals(CombatType.MAGIC)) {
                off_additional_bonus += 0.10;
            }
        }

        //equipment bonuses
        int off_equipment_stab_attack = 0;
        int off_equipment_slash_attack = 0;
        int off_equipment_crush_attack = 0;
        int off_equipment_ranged_attack = 0;
        int off_equipment_magic_attack = 0;


        if (entity.isPlayer()) {
            final Player player = (Player) entity;
            off_equipment_stab_attack = player.getBonusManager().getAttackBonus()[BonusManager.ATTACK_STAB];
            off_equipment_slash_attack = player.getBonusManager().getAttackBonus()[BonusManager.ATTACK_SLASH];
            off_equipment_crush_attack = player.getBonusManager().getAttackBonus()[BonusManager.ATTACK_CRUSH];
            off_equipment_ranged_attack = player.getBonusManager().getAttackBonus()[BonusManager.ATTACK_RANGE];
            off_equipment_magic_attack = player.getBonusManager().getAttackBonus()[BonusManager.ATTACK_MAGIC];
        } else {
            // TODO: Figure out if in 317 mobs have attack bonuses
//            NPC npc = ((NPC) entity);
//            if (npc.getDefinition() != null && npc.getDefinition().getStats() != null) {
//                off_equipment_stab_attack = npc.getDefinition().getStats()[0];
//            }

        }

        int def_equipment_stab_defence = 0;
        int def_equipment_slash_defence = 0;
        int def_equipment_crush_defence = 0;
        int def_equipment_ranged_defence = 0;
        int def_equipment_magic_defence = 0;

        if (enemy.isPlayer()) {
            final Player player = (Player) enemy;
            def_equipment_stab_defence = player.getBonusManager().getAttackBonus()[BonusManager.DEFENCE_STAB];
            def_equipment_slash_defence = player.getBonusManager().getAttackBonus()[BonusManager.DEFENCE_SLASH];
            def_equipment_crush_defence = player.getBonusManager().getAttackBonus()[BonusManager.DEFENCE_CRUSH];
            def_equipment_ranged_defence = player.getBonusManager().getAttackBonus()[BonusManager.DEFENCE_RANGE];
            def_equipment_magic_defence = player.getBonusManager().getAttackBonus()[BonusManager.DEFENCE_MAGIC];
        } else {
            NPC npc = ((NPC) enemy);
            if (npc.getDefinition() != null && npc.getDefinition().getStats() != null) {
                def_equipment_stab_defence = npc.getDefinition().getStats()[10];
                def_equipment_slash_defence = npc.getDefinition().getStats()[11];
                def_equipment_crush_defence = npc.getDefinition().getStats()[12];
                def_equipment_ranged_defence = npc.getDefinition().getStats()[14];
                def_equipment_magic_defence = npc.getDefinition().getStats()[13];
            }
        }

//        if (enemy.isNpc()) {
//            Npc npc = (Npc) enemy;
//            if (npc.combatInfo() != null && npc.combatInfo().stats != null && npc.combatInfo().boss) {
//                def_equipment_ranged_defence -= (def_current_defence_level * 0.50); //I don't like this solution but this formula is fucked.
//            }
//        }

        //protect from * prayers
        boolean def_protect_from_melee = false;
        boolean def_protect_from_ranged = false;
        boolean def_protect_from_magic = false;

        if (entity.isNpc() && enemy.isPlayer()) {
            Player player = ((Player) enemy);
            def_protect_from_melee = PrayerHandler.isActivated(player, PrayerHandler.PROTECT_FROM_MELEE);
            def_protect_from_ranged = PrayerHandler.isActivated(player, PrayerHandler.PROTECT_FROM_MISSILES);
            def_protect_from_magic = PrayerHandler.isActivated(player, PrayerHandler.PROTECT_FROM_MAGIC);
        }

        //chance bonuses
        double off_special_attack_bonus = 1.0;
        double off_void_bonus = 1.0;

        if (entity.isPlayer()) {
            final Player player = (Player) entity;
            if (style.equals(CombatType.MELEE) && CombatEquipment.wearingVoid(player, CombatType.MELEE))
                off_void_bonus = 1.10;
            else if (style.equals(CombatType.RANGED) && CombatEquipment.wearingVoid(player, CombatType.RANGED))
                off_void_bonus = 1.10;
            else if (style.equals(CombatType.MAGIC) && CombatEquipment.wearingVoid(player, CombatType.MAGIC))
                off_void_bonus = 1.30;
        }

		/*
			S E T T I N G S

			E N D
		*/

        /*
			C A L C U L A T E D
			V A R I A B L E S

			S T A R T
		*/

        //experience bonuses
        double off_spell_bonus = 0;
        double off_weapon_bonus = 0;

        //effective levels
        double effective_attack = 0;
        double effective_magic = 0;
        double effective_defence = 0;

        //relevent equipment bonuses
        int off_equipment_bonus = 0;
        int def_equipment_bonus = 0;

        //augmented levels
        double augmented_attack = 0;
        double augmented_defence = 0;

        //hit chances
        double hit_chance = 0;
        double off_hit_chance = 0;
        double def_block_chance = 0;

		/*
			C A L C U L A T E D
			V A R I A B L E S

			E N D
		*/

        int off_style = -1;
        if (entity.isPlayer()) {
            off_style = ((Player) entity).getFightType().getBonusType();
        }

        //determine effective attack
        switch (style) {
            case MELEE:
                if (off_base_attack_level > off_weapon_requirement) {
                    off_weapon_bonus = (off_base_attack_level - off_weapon_requirement) * .3;
                }

                effective_attack = Math.floor((((off_current_attack_level * off_attack_prayer_bonus) * off_additional_bonus) + off_stance_bonus + off_weapon_bonus) * toktz_bonus);
                effective_defence = Math.floor((def_current_defence_level * def_defence_prayer_bonus) + def_stance_bonus);
                if (off_style != -1) {
                    switch (off_style) {
                        case BonusManager.ATTACK_STAB:
                            off_equipment_bonus = off_equipment_stab_attack;
                            def_equipment_bonus = def_equipment_stab_defence;
                            break;
                        case BonusManager.ATTACK_SLASH:
                            off_equipment_bonus = off_equipment_slash_attack;
                            def_equipment_bonus = def_equipment_slash_defence;
                            break;
                        case BonusManager.ATTACK_CRUSH:
                            off_equipment_bonus = off_equipment_crush_attack;
                            def_equipment_bonus = def_equipment_crush_defence;
                            break;
                        default:
                            off_equipment_bonus = Math.max(Math.max(off_equipment_stab_attack, off_equipment_slash_attack), off_equipment_crush_attack);
                            def_equipment_bonus = Math.max(Math.max(def_equipment_stab_defence, def_equipment_slash_defence), def_equipment_crush_defence);
                            break;
                    }
                } else {
                    off_equipment_bonus = Math.max(Math.max(off_equipment_stab_attack, off_equipment_slash_attack), off_equipment_crush_attack);
                    def_equipment_bonus = Math.max(Math.max(def_equipment_stab_defence, def_equipment_slash_defence), def_equipment_crush_defence);
                }
                break;
            case RANGED:
/*				if (off_base_ranged_level > off_weapon_requirement) {
					off_weapon_bonus = (off_base_ranged_level - off_weapon_requirement) * .3;
				}*/
                effective_attack = Math.floor((((off_current_ranged_level * off_ranged_prayer_bonus) * off_additional_bonus) + off_stance_bonus + off_weapon_bonus) * twistedBowMultiplier);
                effective_defence = Math.floor((def_current_defence_level * def_defence_prayer_bonus) + def_stance_bonus);
                off_equipment_bonus = off_equipment_ranged_attack;
                def_equipment_bonus = def_equipment_ranged_defence;
                break;
            case MAGIC:
                //if (off_base_magic_level > off_spell_requirement) {
                //	off_spell_bonus = (off_base_magic_level - off_spell_requirement) * .3;
                //	System.out.println(off_base_magic_level + ". " + off_spell_requirement + " " + off_spell_bonus);
                //}
                effective_attack = Math.floor(((off_current_magic_level * off_magic_prayer_bonus) * off_additional_bonus) + off_spell_bonus);
                effective_magic = Math.floor(def_current_magic_level * .7);
                effective_defence = Math.floor((def_current_defence_level * def_defence_prayer_bonus) * .3);
                effective_defence = effective_defence + effective_magic;
                off_equipment_bonus = off_equipment_magic_attack;
                def_equipment_bonus = def_equipment_magic_defence;
                break;
        }

        //determine augmented levels
        augmented_attack = Math.floor(((effective_attack + 8) * (off_equipment_bonus + 64.)));
        augmented_defence = Math.floor(((effective_defence + 8) * (def_equipment_bonus + 64.)));

        //determine hit chance
        if (augmented_attack < augmented_defence) {
            hit_chance = augmented_attack / ((augmented_defence + 1.) * 2.);
        } else {
            hit_chance = 1. - ((augmented_defence + 2.) / ((augmented_attack + 1.) * 2.));
        }

        switch (style) {
            case MELEE:
                if (def_protect_from_melee) {
                    off_hit_chance = Math.floor((((hit_chance * off_special_attack_bonus) * off_void_bonus) * .6) * 100.);
                    def_block_chance = Math.floor(101 - ((((hit_chance * off_special_attack_bonus) * off_void_bonus) * .6) * 100.));
                } else {
                    off_hit_chance = Math.floor(((hit_chance * off_special_attack_bonus) * off_void_bonus) * 100.);
                    def_block_chance = Math.floor(101 - (((hit_chance * off_special_attack_bonus) * off_void_bonus) * 100.));
                }
                break;
            case RANGED:
                if (def_protect_from_ranged) {
                    off_hit_chance = Math.floor((((hit_chance * off_special_attack_bonus) * off_void_bonus) * .6) * 100.);
                    def_block_chance = Math.floor(101 - ((((hit_chance * off_special_attack_bonus) * off_void_bonus) * .6) * 100.));
                } else {
                    off_hit_chance = Math.floor(((hit_chance * off_special_attack_bonus) * off_void_bonus) * 100.);
                    def_block_chance = Math.floor(101 - (((hit_chance * off_special_attack_bonus) * off_void_bonus) * 100.));
                }
                break;
            case MAGIC:
                if (def_protect_from_magic) {
                    off_hit_chance = Math.floor(((hit_chance * off_void_bonus) * .6) * 100.);
                    def_block_chance = Math.floor(101 - (((hit_chance * off_void_bonus) * .6) * 100.));
                } else {
                    off_hit_chance = Math.floor((hit_chance * off_void_bonus) * 100.);
                    def_block_chance = Math.floor(101 - ((hit_chance * off_void_bonus) * 100.));
                }
                break;
        }

//        System.out.println("\nYour chance to hit is: " + off_hit_chance + "%");
//        System.out.println("Your opponents chance to block is: " + def_block_chance + "%");
//        System.out.println("Your attack is " + augmented_attack + " and his def is " + augmented_defence);
//        System.out.println("Attack bonus " + off_equipment_bonus + ", atk lv " + effective_attack);
//        System.out.println("Def bonus " + def_equipment_bonus + ", atk lv " + effective_defence);
//        System.out.println("stab bonus " + def_equipment_stab_defence);
//        System.out.println("slash bonus " + def_equipment_slash_defence);
//        System.out.println("crash bonus " + def_equipment_crush_defence);
//        String msg = String.format("Atk %d v def %d. Bonus %d vs %d. Level %d vs %d. Relative %d%% hit > %d%% block%n",
//                (int) augmented_attack, (int) augmented_defence,
//                off_equipment_bonus, def_equipment_bonus, (int) effective_attack, (int) effective_defence, (int) off_hit_chance, (int) def_block_chance);
//        System.out.println(msg);
//            entity.message(msg);
        //System.out.println(targetBonuses);


        off_hit_chance = (int) (srand.nextFloat() * off_hit_chance);
        def_block_chance = (int) (srand.nextFloat() * def_block_chance);

        //print roll
        //System.out.println("\nYou rolled: " + (int) off_hit_chance);
        //System.out.println("Your opponent rolled: " + (int) def_block_chance);

        //determine hit
/*		if (DEBUG && entity.isPlayer()) {
			System.out.println("Success =  " + off_hit_chance + " > " + def_block_chance);
		}*/
        return off_hit_chance > def_block_chance;
    }
}
