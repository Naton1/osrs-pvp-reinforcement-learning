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

public class DragonKnifeCombatMethod extends RangedCombatMethod {

	private static final Animation ANIMATION = new Animation(8292, Priority.HIGH);
	private static final Projectile PROJECTILE = new Projectile(1629, 30, 60, 40, 36);

	@Override
	public PendingHit[] hits(Mobile character, Mobile target) {
		final int distance = character.getLocation().getDistance(target.getLocation());
		final int hitDelay = RangedData.hitDelay(distance, RangedData.RangedWeaponType.KNIFE);
		return new PendingHit[] { new PendingHit(character, target, this, hitDelay),
		                          new PendingHit(character, target, this, hitDelay) };
	}

	@Override
	public boolean canAttack(Mobile character, Mobile target) {
		Player player = character.getAsPlayer();
		if (player.getCombat().getRangedWeapon() != RangedData.RangedWeapon.DRAGON_KNIFE) {
			return false;
		}
		return true;
	}

	@Override
	public void start(Mobile character, Mobile target) {
		final Player player = character.getAsPlayer();
		CombatSpecial.drain(player, CombatSpecial.DRAGON_KNIFE.getDrainAmount());
		player.performAnimation(ANIMATION);
		Projectile.sendProjectile(character, target, PROJECTILE);
		CombatFactory.decrementAmmo(player, target.getLocation(), 1);
	}

}
