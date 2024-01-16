package com.elvarg.game.content.combat.method.impl.specials;

import com.elvarg.game.content.combat.CombatFactory;
import com.elvarg.game.content.combat.CombatSpecial;
import com.elvarg.game.content.combat.hit.HitDamage;
import com.elvarg.game.content.combat.hit.HitMask;
import com.elvarg.game.content.combat.hit.PendingHit;
import com.elvarg.game.content.combat.method.impl.RangedCombatMethod;
import com.elvarg.game.content.combat.ranged.RangedData;
import com.elvarg.game.entity.impl.Mobile;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.Animation;
import com.elvarg.game.model.Priority;
import com.elvarg.game.model.Projectile;
import com.elvarg.game.task.Task;
import com.elvarg.game.task.TaskManager;

public class MorrigansJavelinCombatMethod extends RangedCombatMethod {

	private static final Animation ANIMATION = new Animation(806, Priority.HIGH);
	private static final Projectile PROJECTILE = new Projectile(1622, 30, 60, 40, 36);

	@Override
	public PendingHit[] hits(Mobile character, Mobile target) {
		final int distance = character.getLocation().getDistance(target.getLocation());
		final int hitDelay = RangedData.hitDelay(distance, RangedData.RangedWeaponType.MORRIGANS_JAVELIN);
		return new PendingHit[] { new PendingHit(character, target, this, hitDelay) };
	}

	@Override
	public boolean canAttack(Mobile character, Mobile target) {
		if (!target.isPlayer()) {
			return false;
		}
		Player player = character.getAsPlayer();
		if (player.getCombat().getRangedWeapon() != RangedData.RangedWeapon.MORRIGANS_JAVELIN) {
			return false;
		}
		return true;
	}

	@Override
	public void start(Mobile character, Mobile target) {
		final Player player = character.getAsPlayer();
		CombatSpecial.drain(player, CombatSpecial.MORRIGANS_JAVELIN.getDrainAmount());
		player.performAnimation(ANIMATION);
		Projectile.sendProjectile(character, target, PROJECTILE);
		CombatFactory.decrementAmmo(player, target.getLocation(), 1);
	}

	@Override
	public void handleAfterHitEffects(PendingHit hit) {
		// Handle bleed effect
		TaskManager.submit(new Task() {
			int processed = 0;
			boolean first = true;
			int dealt = 0;

			@Override
			protected void execute() {
				if (hit.getAttacker().getAsPlayer().isDying() || hit.getTarget().getAsPlayer().isDying()) {
					stop();
					return;
				}
				processed++;
				if (processed % 3 == 0) {
					final int damageToDeal = Math.min(5, hit.getTotalDamage() - dealt);
					if (damageToDeal <= 0) {
						stop();
						return;
					}
					dealt += damageToDeal;
					final HitDamage.Metadata hitMeta = HitDamage.Metadata.builder()
					                                                     .attacker(hit.getAttacker())
					                                                     .target(hit.getTarget())
					                                                     .build();
					hit.getTarget()
					   .getCombat()
					   .getHitQueue()
					   .addPendingDamage(new HitDamage(damageToDeal, HitMask.RED).withMetadata(hitMeta));
					if (first) {
						hit.getTarget().sendMessage("You start to bleed as a result of the javelin strike.");
					}
					else {
						hit.getTarget().sendMessage("You continue to bleed as a result of the javelin strike.");
					}
					first = false;
				}
			}
		});
	}

}
