package com.elvarg.game.content.combat.method.impl.npcs;

import com.elvarg.game.content.combat.CombatFactory;
import com.elvarg.game.content.combat.CombatType;
import com.elvarg.game.content.combat.hit.PendingHit;
import com.elvarg.game.content.combat.method.CombatMethod;
import com.elvarg.game.entity.impl.Mobile;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.*;
import com.elvarg.game.task.TaskManager;
import com.elvarg.game.task.impl.ForceMovementTask;
import com.elvarg.util.Misc;
import com.elvarg.util.timers.TimerKey;

/**
 * Handles Callisto's combat.
 *
 * @author Professor Oak
 */
public class CallistoCombatMethod extends CombatMethod {

	private static final Animation MELEE_ATTACK_ANIMATION = new Animation(4925);
	private static final Graphic END_PROJECTILE_GRAPHIC = new Graphic(359, GraphicHeight.HIGH);
	private static final Projectile MAGIC_PROJECILE = new Projectile(395, 31, 43, 40, 60);
	
	private SecondsTimer comboTimer = new SecondsTimer();
	private CombatType currentAttackType = CombatType.MELEE;

	@Override
	public CombatType type() {
		return currentAttackType;
	}

	@Override
	public PendingHit[] hits(Mobile character, Mobile target) {
		return new PendingHit[] { new PendingHit(character, target, this, 2) };
	}

	@Override
	public void start(Mobile character, Mobile target) {
	    character.performAnimation(MELEE_ATTACK_ANIMATION);
		if (currentAttackType == CombatType.MAGIC) {
			Projectile.sendProjectile(character, target, MAGIC_PROJECILE);
		}
	}

	@Override
	public int attackDistance(Mobile character) {
		return 4;
	}

	@Override
	public void finished(Mobile character, Mobile target) {
		currentAttackType = CombatType.MELEE;

		// Switch attack to magic randomly
		if (comboTimer.finished()) {
			if (Misc.getRandom(10) <= 2) {
				comboTimer.start(5);
				currentAttackType = CombatType.MAGIC;
				character.getCombat().performNewAttack(true);
			}
		}
	}

	@Override
	public void handleAfterHitEffects(PendingHit hit) {
		if (hit.getTarget() == null || !hit.getTarget().isPlayer()) {
			return;
		}

		final Player player = hit.getTarget().getAsPlayer();

		if (currentAttackType == CombatType.MAGIC) {
			player.performGraphic(END_PROJECTILE_GRAPHIC);
		}

		// Stun player 15% chance
		if (!player.getTimers().has(TimerKey.STUN) && Misc.getRandom(100) <= 10) {
			player.performAnimation(new Animation(3131));
			final Location toKnock = new Location(player.getLocation().getX() > 3325 ? -3 : 1 + Misc.getRandom(2),
					player.getLocation().getY() > 3834 && player.getLocation().getY() < 3843 ? 3 : -3);
			TaskManager.submit(new ForceMovementTask(player, 3,
					new ForceMovement(player.getLocation().clone(), toKnock, 0, 15, 0, 0)));
			CombatFactory.stun(player, 4, false);
		}
	}
}
