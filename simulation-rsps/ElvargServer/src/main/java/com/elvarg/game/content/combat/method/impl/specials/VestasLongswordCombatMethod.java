package com.elvarg.game.content.combat.method.impl.specials;

import com.elvarg.game.content.combat.CombatSpecial;
import com.elvarg.game.content.combat.formula.AccuracyFormulasDpsCalc;
import com.elvarg.game.content.combat.formula.DamageFormulas;
import com.elvarg.game.content.combat.hit.PendingHit;
import com.elvarg.game.content.combat.method.impl.MeleeCombatMethod;
import com.elvarg.game.entity.impl.Mobile;
import com.elvarg.game.model.Animation;
import com.elvarg.game.model.Priority;
import com.elvarg.game.model.equipment.BonusManager;
import com.elvarg.util.Misc;

public class VestasLongswordCombatMethod extends MeleeCombatMethod {

	private static final Animation ANIMATION = new Animation(8145, Priority.HIGH);

	@Override
	public PendingHit[] hits(Mobile character, Mobile target) {
		// VLS does 20-120% of max damage (if accurate), and rolls against 25% of opponent defense

		final int attRoll = AccuracyFormulasDpsCalc.attackMeleeRoll(character);
		final int defRoll = AccuracyFormulasDpsCalc.defenseMeleeRoll(target, BonusManager.ATTACK_STAB);

		// Roll against 25% of opponent defense
		// (not sure if it means 25% of def roll, def level, def stats, etc.)
		final boolean accurate = AccuracyFormulasDpsCalc.rollAccuracy(attRoll, (int) (defRoll * 0.25));

		final PendingHit hit = new PendingHit(character, target, this, !accurate, 0);

		if (accurate) {
			// Roll hit in [0.2, 1.2] of max hit
			final int maxHit = DamageFormulas.calculateMaxMeleeHit(character);
			final int lowerRoll = (int) (0.2 * maxHit);
			final int upperRoll = maxHit + lowerRoll;
			final int newDamage = Misc.random(lowerRoll, upperRoll);
			hit.setTotalDamage(newDamage);
		}
		else {
			hit.setTotalDamage(0);
		}

		return new PendingHit[] { hit };
	}

	@Override
	public void start(Mobile character, Mobile target) {
		CombatSpecial.drain(character, CombatSpecial.VESTAS_LONGSWORD.getDrainAmount());
		character.performAnimation(ANIMATION);
	}
}