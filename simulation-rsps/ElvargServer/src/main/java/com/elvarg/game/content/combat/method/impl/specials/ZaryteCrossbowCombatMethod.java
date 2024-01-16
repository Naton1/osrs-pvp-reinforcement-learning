package com.elvarg.game.content.combat.method.impl.specials;

import com.elvarg.game.content.combat.CombatFactory;
import com.elvarg.game.content.combat.CombatSpecial;
import com.elvarg.game.content.combat.hit.PendingHit;
import com.elvarg.game.content.combat.method.impl.RangedCombatMethod;
import com.elvarg.game.content.combat.ranged.RangedData;
import com.elvarg.game.content.combat.ranged.RangedData.RangedWeapon;
import com.elvarg.game.entity.impl.Mobile;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.Animation;
import com.elvarg.game.model.Priority;
import com.elvarg.game.model.Projectile;

public class ZaryteCrossbowCombatMethod extends RangedCombatMethod {

	private static final Animation ANIMATION = new Animation(9166, Priority.HIGH);
	private static final Projectile PROJECTILE = new Projectile(301, 44, 35, 50, 70);

	@Override
	public PendingHit[] hits(Mobile character, Mobile target) {
		final int distance = character.getLocation().getDistance(target.getLocation());
		return new PendingHit[] { new PendingHit(character,
		                                         target,
		                                         this,
		                                         RangedData.hitDelay(distance, RangedData.RangedWeaponType.CROSSBOW)) };
	}

	@Override
	public boolean canAttack(Mobile character, Mobile target) {
		Player player = character.getAsPlayer();
		if (player.getCombat().getRangedWeapon() != RangedWeapon.ZARYTE_CROSSBOW) {
			return false;
		}
		if (!CombatFactory.checkAmmo(player, 1)) {
			return false;
		}
		return true;
	}

	@Override
	public void start(Mobile character, Mobile target) {
		final Player player = character.getAsPlayer();
		CombatSpecial.drain(player, CombatSpecial.ZARYTE_CROSSBOW.getDrainAmount());
		player.performAnimation(ANIMATION);
		Projectile.sendProjectile(character, target, PROJECTILE);
		CombatFactory.decrementAmmo(player, target.getLocation(), 1);
		// This also does a guaranteed enchanted bolt hit, see CombatFactory.applyExtraHitRolls()
	}
}