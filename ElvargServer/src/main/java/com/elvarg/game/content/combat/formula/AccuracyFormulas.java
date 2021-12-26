package com.elvarg.game.content.combat.formula;

import com.elvarg.game.content.PrayerHandler;
import com.elvarg.game.content.combat.*;
import com.elvarg.game.entity.impl.Mobile;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.Skill;
import com.elvarg.game.model.equipment.BonusManager;
import com.elvarg.util.Misc;

public class AccuracyFormulas {

	public static boolean rollAccuracy(Mobile attacker, Mobile victim, CombatType type) {
		if (attacker == null || victim == null) {
			return false;
		}

		boolean veracEffect = false;

		if (type == CombatType.MELEE) {
			if (CombatFactory.fullVeracs(attacker)) {
				if (Misc.getRandom(3) == 1) {
					veracEffect = true;
				}
			}
		}

		double prayerMod = 1;
		double voidMod = 1;
		double equipmentBonus = 1;
		double specialBonus = 1;
		int styleBonus = 2;
		int bonusType = -1;
		if (attacker.isPlayer()) {
			Player player = (Player) attacker;

			if (player.isSpecialActivated()) {
				if (player.getCombatSpecial().getCombatMethod().type() == type) {

					// Dark bow special should always hit at least 8's.
					if (player.getCombatSpecial() == CombatSpecial.DARK_BOW) {
						return true;
					}

					specialBonus = player.getCombatSpecial().getAccuracyBonus();
				}
			}

			equipmentBonus = type == CombatType.MAGIC
					? player.getBonusManager().getAttackBonus()[BonusManager.ATTACK_MAGIC]
					: player.getBonusManager().getAttackBonus()[player.getFightType().getBonusType()];

			bonusType = player.getFightType().getCorrespondingBonus();

			if (type == CombatType.MELEE) {
				if (PrayerHandler.isActivated(player, PrayerHandler.CLARITY_OF_THOUGHT)) {
					prayerMod = 1.05;
				} else if (PrayerHandler.isActivated(player, PrayerHandler.IMPROVED_REFLEXES)) {
					prayerMod = 1.10;
				} else if (PrayerHandler.isActivated(player, PrayerHandler.INCREDIBLE_REFLEXES)) {
					prayerMod = 1.15;
				} else if (PrayerHandler.isActivated(player, PrayerHandler.CHIVALRY)) {
					prayerMod = 1.15;
				} else if (PrayerHandler.isActivated(player, PrayerHandler.PIETY)) {
					prayerMod = 1.20;
				}
			} else if (type == CombatType.RANGED) {
				if (PrayerHandler.isActivated(player, PrayerHandler.SHARP_EYE)) {
					prayerMod = 1.05;
				} else if (PrayerHandler.isActivated(player, PrayerHandler.HAWK_EYE)) {
					prayerMod = 1.10;
				} else if (PrayerHandler.isActivated(player, PrayerHandler.EAGLE_EYE)) {
					prayerMod = 1.15;
				} else if (PrayerHandler.isActivated(player, PrayerHandler.RIGOUR)) {
					prayerMod = 1.20;
				}
			} else if (type == CombatType.MAGIC) {
				if (PrayerHandler.isActivated(player, PrayerHandler.MYSTIC_WILL)) {
					prayerMod = 1.05;
				} else if (PrayerHandler.isActivated(player, PrayerHandler.MYSTIC_LORE)) {
					prayerMod = 1.10;
				} else if (PrayerHandler.isActivated(player, PrayerHandler.MYSTIC_MIGHT)) {
					prayerMod = 1.15;
				} else if (PrayerHandler.isActivated(player, PrayerHandler.AUGURY)) {
					prayerMod = 1.25;
				}
			}

			if (player.getFightType().getStyle() == FightStyle.ACCURATE) {
				styleBonus = 3;
			} else if (player.getFightType().getStyle() == FightStyle.CONTROLLED) {
				styleBonus = 1;
			}

			// Void effects for accuracy
			if (CombatEquipment.wearingVoid(player, type)) {
				if (type == CombatType.MELEE || type == CombatType.RANGED) {
					voidMod = 1.10;
				} else if (type == CombatType.MAGIC) {
					voidMod = 1.30;
				}
			}
		}

		double attackAccuracy = Math.floor(equipmentBonus + attacker.getBaseAttack(type)) + 8;

		attackAccuracy *= prayerMod;
		attackAccuracy *= specialBonus;
		attackAccuracy *= voidMod;

		// Cheaphax to boost magic accuracy
		if (type == CombatType.MAGIC) {
			// 25% more accuracy
			attackAccuracy *= 1.25;
		}

		// Style bonus shouldn't affect magic right now because
		// We don't have defensive magic casting for def xp
		if (type != CombatType.MAGIC) {
			attackAccuracy += styleBonus;

			if (equipmentBonus < -67) {
				attackAccuracy = Misc.getRandom(8) == 0 ? attackAccuracy : 0;
			}

		} else {
			if (equipmentBonus <= -67) {
				attackAccuracy = 0;
			}
		}

		equipmentBonus = 1;
		prayerMod = 1;
		styleBonus = 2;
		if (victim.isPlayer()) {
			Player player = (Player) victim;

			if (bonusType == -1) {
				equipmentBonus = type == CombatType.MAGIC
						? player.getBonusManager().getDefenceBonus()[BonusManager.DEFENCE_MAGIC]
						: player.getSkillManager().getCurrentLevel(Skill.DEFENCE);
			} else {
				equipmentBonus = type == CombatType.MAGIC
						? player.getBonusManager().getDefenceBonus()[BonusManager.DEFENCE_MAGIC]
						: player.getBonusManager().getDefenceBonus()[bonusType];
			}

			if (PrayerHandler.isActivated(player, PrayerHandler.THICK_SKIN)) {
				prayerMod += 0.05;
			} else if (PrayerHandler.isActivated(player, PrayerHandler.ROCK_SKIN)) {
				prayerMod += 0.10;
			} else if (PrayerHandler.isActivated(player, PrayerHandler.STEEL_SKIN)) {
				prayerMod += 0.15;
			} else if (PrayerHandler.isActivated(player, PrayerHandler.CHIVALRY)) {
				prayerMod += 0.20;
			} else if (PrayerHandler.isActivated(player, PrayerHandler.PIETY)
					|| PrayerHandler.isActivated(player, PrayerHandler.RIGOUR)
					|| PrayerHandler.isActivated(player, PrayerHandler.AUGURY)) {
				prayerMod += 0.25;
			}

			// Check for enemy magic defence
			if (type == CombatType.MAGIC) {
				if (PrayerHandler.isActivated(player, PrayerHandler.MYSTIC_WILL)) {
					prayerMod += 0.05;
				} else if (PrayerHandler.isActivated(player, PrayerHandler.MYSTIC_LORE)) {
					prayerMod += 0.1;
				} else if (PrayerHandler.isActivated(player, PrayerHandler.MYSTIC_MIGHT)) {
					prayerMod += 0.15;
				} else if (PrayerHandler.isActivated(player, PrayerHandler.AUGURY)) {
					prayerMod += 0.25;
				}
			}

			// PROTECTION PRAYERS DEFENSIVE
			if (PrayerHandler.isActivated(player, PrayerHandler.getProtectingPrayer(type))) {
				if (attacker.isPlayer()) {
					prayerMod += CombatConstants.PRAYER_ACCURACY_REDUCTION_AGAINST_PLAYERS;
				} else if (attacker.isNpc()) {
					prayerMod += CombatConstants.PRAYER_ACCURACY_REDUCTION_AGAINST_NPCS;
				}
			}

			if (player.getFightType().getStyle() == FightStyle.DEFENSIVE) {
				styleBonus = 3;
			} else if (player.getFightType().getStyle() == FightStyle.CONTROLLED) {
				styleBonus = 1;
			}
		}

		double defenceCalc = Math.floor(equipmentBonus + victim.getBaseDefence(type)) + 8;
		defenceCalc *= prayerMod;

		if (type != CombatType.MAGIC) {
			defenceCalc += styleBonus;

			if (equipmentBonus < -67) {
				defenceCalc = Misc.getRandom(8) == 0 ? defenceCalc : 0;
			}

		} else {
			if (equipmentBonus <= -67) {
				defenceCalc = 0;
			}
		}

		// Veracs was triggered (25% chance)
		// Ignore all defences (armour and protection prayers)
		if (veracEffect) {
			defenceCalc = 0;
		}

		double A = Math.floor(attackAccuracy);
		double D = Math.floor(defenceCalc);
		double hitSucceed = A < D ? (A - 1.0) / (2.0 * D) : 1.0 - (D + 1.0) / (2.0 * A);
		hitSucceed = hitSucceed >= 1.0 ? 0.99 : hitSucceed <= 0.0 ? 0.01 : hitSucceed;
		return hitSucceed >= Misc.getRandomDouble();
	}
}
