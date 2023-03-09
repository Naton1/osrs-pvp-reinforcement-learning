package com.elvarg.game.content.combat.method.impl.npcs;

import com.elvarg.game.content.combat.CombatType;
import com.elvarg.game.content.combat.hit.PendingHit;
import com.elvarg.game.content.combat.method.CombatMethod;
import com.elvarg.game.entity.impl.Mobile;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.*;
import com.elvarg.util.Misc;

/**
 * Handles Venenatis' combat.
 *
 * @author Professor Oak
 */
public class VenenatisCombatMethod extends CombatMethod {

	private static final Animation MELEE_ATTACK_ANIMATION = new Animation(5319);
	private static final Animation MAGIC_ATTACK_ANIMATION = new Animation(5322);
	private static final Graphic DRAIN_PRAYER_GRAPHIC = new Graphic(172, GraphicHeight.MIDDLE);
	private static final Projectile MAGIC_PROJECTILE = new Projectile(165, 31, 43, 40, 55);

	private CombatType currentAttackType = CombatType.MELEE;

	@Override
	public CombatType type() {
		return currentAttackType;
	}

	@Override
	public PendingHit[] hits(Mobile character, Mobile target) {
		return new PendingHit[] { new PendingHit(character, target, this, 1) };
	}

	@Override
	public void start(Mobile character, Mobile target) {
		if (currentAttackType == CombatType.MAGIC) {
		    character.performAnimation(MAGIC_ATTACK_ANIMATION);
		    Projectile.sendProjectile(character, target, MAGIC_PROJECTILE);
		} else if (currentAttackType == CombatType.MELEE) {
		    character.performAnimation(MELEE_ATTACK_ANIMATION);
		}
	}

	@Override
	public int attackDistance(Mobile character) {
		return 4;
	}

	@Override
	public void finished(Mobile character, Mobile target) {
		// Switch attack type after each attack
		if (currentAttackType == CombatType.MAGIC) {
			currentAttackType = CombatType.MELEE;
		} else {
			currentAttackType = CombatType.MAGIC;

			// Have a chance of comboing with magic by reseting combat delay.
			if (Misc.getRandom(10) <= 3) {
				character.getCombat().performNewAttack(true);
			}
		}
	}

	@Override
	public void handleAfterHitEffects(PendingHit hit) {
		if (!hit.isAccurate() || hit.getTarget() == null || !hit.getTarget().isPlayer()) {
			return;
		}

		// Drain prayer randomly 15% chance
		if (Misc.getRandom(100) <= 15) {
			Player player = hit.getTarget().getAsPlayer();
			hit.getTarget().performGraphic(DRAIN_PRAYER_GRAPHIC);
			player.getSkillManager().decreaseCurrentLevel(Skill.PRAYER, (int) (hit.getTotalDamage() * 0.35), 0);
			player.getPacketSender().sendMessage("Venenatis drained your prayer!");
		}
	}
}
