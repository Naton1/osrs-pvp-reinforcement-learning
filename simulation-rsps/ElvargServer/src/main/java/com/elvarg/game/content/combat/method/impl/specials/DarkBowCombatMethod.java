package com.elvarg.game.content.combat.method.impl.specials;

import com.elvarg.game.content.combat.CombatFactory;
import com.elvarg.game.content.combat.CombatSpecial;
import com.elvarg.game.content.combat.hit.PendingHit;
import com.elvarg.game.content.combat.method.impl.RangedCombatMethod;
import com.elvarg.game.content.combat.ranged.RangedData;
import com.elvarg.game.content.combat.ranged.RangedData.Ammunition;
import com.elvarg.game.content.combat.ranged.RangedData.RangedWeapon;
import com.elvarg.game.content.combat.ranged.RangedData.RangedWeaponType;
import com.elvarg.game.entity.impl.Mobile;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.*;

public class DarkBowCombatMethod extends RangedCombatMethod {

	private static final Animation ANIMATION = new Animation(426, Priority.HIGH);
	private static final Graphic GRAPHIC = new Graphic(1100, GraphicHeight.HIGH, Priority.HIGH);

	@Override
	public PendingHit[] hits(Mobile character, Mobile target) {
		int distance = character.getLocation().getDistance(target.getLocation());
		int delay = RangedData.hitDelay(distance, RangedWeaponType.LONGBOW);

		final PendingHit firstHit = new PendingHit(character, target, this, delay);
		final PendingHit secondHit = new PendingHit(character, target, this, RangedData.dbowArrowDelay(distance));
		firstHit.setTotalDamage(Math.max(Math.min(firstHit.getTotalDamage(), 48), 8));
		secondHit.setTotalDamage(Math.max(Math.min(secondHit.getTotalDamage(), 48), 8));

		return new PendingHit[] { firstHit, secondHit };
	}

	@Override
	public boolean canAttack(Mobile character, Mobile target) {
		Player player = character.getAsPlayer();
		if (player.getCombat().getRangedWeapon() != RangedWeapon.DARK_BOW) {
			return false;
		}
		if (!CombatFactory.checkAmmo(player, 2)) {
			return false;
		}
		return true;
	}

	@Override
	public void start(Mobile character, Mobile target) {
		final Player player = character.getAsPlayer();
		CombatSpecial.drain(player, CombatSpecial.DARK_BOW.getDrainAmount());
		player.performAnimation(ANIMATION);
		int projectileId = 1099;
		if (player.getCombat().getAmmunition() != Ammunition.DRAGON_ARROW) {
			projectileId = 1101;
		}
		Projectile.sendProjectile(player, target, new Projectile(projectileId, 43, 31, 40, 70));
		Projectile.sendProjectile(character, target, new Projectile(projectileId, 48, 31, 33, 74));
		CombatFactory.decrementAmmo(player, target.getLocation(), 2);
	}

	@Override
	public int attackSpeed(Mobile character) {
		return super.attackSpeed(character) + 1;
	}

	@Override
	public void handleAfterHitEffects(PendingHit hit) {
		hit.getTarget().performGraphic(GRAPHIC);
	}
}