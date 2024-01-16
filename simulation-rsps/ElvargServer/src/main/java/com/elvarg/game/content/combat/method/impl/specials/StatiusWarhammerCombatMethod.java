package com.elvarg.game.content.combat.method.impl.specials;

import com.elvarg.game.content.combat.CombatSpecial;
import com.elvarg.game.content.combat.formula.DamageFormulas;
import com.elvarg.game.content.combat.hit.PendingHit;
import com.elvarg.game.content.combat.method.impl.MeleeCombatMethod;
import com.elvarg.game.entity.impl.Mobile;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.Animation;
import com.elvarg.game.model.Priority;
import com.elvarg.game.model.Skill;
import com.elvarg.util.Misc;

public class StatiusWarhammerCombatMethod extends MeleeCombatMethod {

	private static final Animation ANIMATION = new Animation(1378, Priority.HIGH);

	@Override
	public PendingHit[] hits(Mobile character, Mobile target) {
		// Does 25-125% of max damage (if accurate), and lowers defense by 30% on accurate hits

		final PendingHit hit = new PendingHit(character, target, this);

		if (hit.isAccurate()) {
			// Roll hit in [0.25, 1.25] of max hit
			final int maxHit = DamageFormulas.calculateMaxMeleeHit(character);
			final int lowerRoll = (int) (0.25 * maxHit);
			final int upperRoll = maxHit + lowerRoll;
			final int newDamage = Misc.random(lowerRoll, upperRoll);
			hit.setTotalDamage(newDamage);
		}

		return new PendingHit[] { hit };
	}

	@Override
	public void start(Mobile character, Mobile target) {
		CombatSpecial.drain(character, CombatSpecial.STATIUS_WARHAMMER.getDrainAmount());
		character.performAnimation(ANIMATION);
	}

	@Override
	public void handleAfterHitEffects(PendingHit hit) {
		if (hit.isAccurate() && hit.getTotalDamage() > 0 && hit.getTarget().isPlayer()) {
			final Player target = hit.getTarget().getAsPlayer();
			final int currentDef = target.getSkillManager().getCurrentLevel(Skill.DEFENCE);
			final int reducedDef = (int) (currentDef * 0.7);
			target.getSkillManager().setCurrentLevel(Skill.DEFENCE, reducedDef);
		}
	}
}