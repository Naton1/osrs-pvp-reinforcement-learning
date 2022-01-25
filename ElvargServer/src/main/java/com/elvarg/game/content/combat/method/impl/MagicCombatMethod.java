package com.elvarg.game.content.combat.method.impl;

import com.elvarg.game.content.combat.CombatType;
import com.elvarg.game.content.combat.hit.PendingHit;
import com.elvarg.game.content.combat.magic.CombatSpell;
import com.elvarg.game.content.combat.method.CombatMethod;
import com.elvarg.game.entity.impl.Mobile;
import com.elvarg.game.model.Graphic;
import com.elvarg.game.model.GraphicHeight;

/**
 * Represents the combat method for magic attacks.
 *
 * @author Professor Oak
 */
public class MagicCombatMethod extends CombatMethod {

	public static final Graphic SPLASH_GRAPHIC = new Graphic(85, GraphicHeight.MIDDLE);

	@Override
	public CombatType type() {
		return CombatType.MAGIC;
	}

	@Override
	public PendingHit[] hits(Mobile character, Mobile target) {
		return new PendingHit[] { new PendingHit(character, target, this, 3) };
	}

	@Override
	public boolean canAttack(Mobile character, Mobile target) {
		if (character.isNpc()) {
			return true;
		}

		// Set the current spell to the autocast spell if it's null.
		if (character.getCombat().getCastSpell() == null) {
			character.getCombat().setCastSpell(character.getCombat().getAutocastSpell());
		}

		// Character didn't have autocast spell either.
		if (character.getCombat().getCastSpell() == null) {
			return false;
		}

		return character.getCombat().getCastSpell().canCast(character.getAsPlayer(), true);
	}

	@Override
	public void start(Mobile character, Mobile target) {
		CombatSpell spell = character.getCombat().getCastSpell();
		
		if (spell == null)
			spell = character.getCombat().getAutocastSpell();

		if (spell != null) {
			spell.startCast(character, target);
		}
	}

	@Override
	public int attackSpeed(Mobile character) {

		if (character.getCombat().getPreviousCast() != null) {
			return character.getCombat().getPreviousCast().getAttackSpeed();
		}

		return super.attackSpeed(character);
	}

	@Override
	public int attackDistance(Mobile character) {
		return 10;
	}

	@Override
	public void finished(Mobile character, Mobile target) {

		// Reset the castSpell to autocastSpell
		// Update previousCastSpell so effects can be handled.

		final CombatSpell current = character.getCombat().getCastSpell();

		character.getCombat().setCastSpell(null);

		if (character.getCombat().getAutocastSpell() == null) {
			character.getCombat().reset();
			character.setMobileInteraction(target);
			character.getMovementQueue().reset();
		}

		character.getCombat().setPreviousCast(current);
	}

	@Override
	public void handleAfterHitEffects(PendingHit hit) {
		Mobile attacker = hit.getAttacker();
		Mobile target = hit.getTarget();
		boolean accurate = hit.isAccurate();
		int damage = hit.getTotalDamage();

		if (attacker.getHitpoints() <= 0 || target.getHitpoints() <= 0) {
			return;
		}

		CombatSpell previousSpell = attacker.getCombat().getPreviousCast();

		if (previousSpell != null) {

			if (accurate) {

				// Send proper end graphics for the spell because it was accurate
				previousSpell.endGraphic().ifPresent(target::performGraphic);

			} else {

				// Send splash graphics for the spell because it wasn't accurate
				target.performGraphic(SPLASH_GRAPHIC);
			}

			previousSpell.finishCast(attacker, target, accurate, damage);

		}
	}
}
