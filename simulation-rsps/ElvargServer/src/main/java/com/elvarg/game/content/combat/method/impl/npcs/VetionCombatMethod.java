package com.elvarg.game.content.combat.method.impl.npcs;

import java.util.ArrayList;
import java.util.List;

import com.elvarg.game.content.combat.CombatType;
import com.elvarg.game.content.combat.hit.HitDamage;
import com.elvarg.game.content.combat.hit.HitMask;
import com.elvarg.game.content.combat.hit.PendingHit;
import com.elvarg.game.content.combat.method.CombatMethod;
import com.elvarg.game.entity.impl.Mobile;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.Animation;
import com.elvarg.game.model.Graphic;
import com.elvarg.game.model.Location;
import com.elvarg.game.model.Projectile;
import com.elvarg.game.task.Task;
import com.elvarg.game.task.TaskManager;
import com.elvarg.util.Misc;
import com.elvarg.util.timers.TimerKey;

public class VetionCombatMethod extends CombatMethod {

	private CombatType attack = CombatType.MELEE;
	private static final Graphic MAGIC_END_GFX = new Graphic(281);
	private static final Projectile MAGIC_PROJECTILE = new Projectile(280, 31, 43, 40, 80);

	@Override
	public boolean canAttack(Mobile character, Mobile target) {
		return true;
	}

	@Override
	public PendingHit[] hits(Mobile character, Mobile target) {
		if (attack == CombatType.MAGIC) {
			return null;
		}
		return new PendingHit[] { new PendingHit(character, target, this, 2) };
	}

	@Override
	public void start(Mobile character, Mobile target) {
		if (!character.isNpc() || !target.isPlayer())
			return;
		character.performAnimation(new Animation(character.getAttackAnim()));
		
		if (target.getLocation().getDistance(character.getLocation()) < 2 && Misc.getRandom(1) == 0) {
			attack = CombatType.MELEE;
		} else {
			attack = CombatType.MAGIC;
		}

		if (attack == CombatType.MAGIC) {
			Location targetPos = target.getLocation();
			List<Location> attackPositions = new ArrayList<>();
			attackPositions.add(targetPos);
			for (int i = 0; i < 2; i++) {
				attackPositions.add(new Location((targetPos.getX() - 1) + Misc.getRandom(3),
						(targetPos.getY() - 1) + Misc.getRandom(3)));
			}
			for (Location pos : attackPositions) {
				Projectile.sendProjectile(character, pos, MAGIC_PROJECTILE);
			}
			TaskManager.submit(new Task(4) {
				@Override
				public void execute() {
					for (Location pos : attackPositions) {
						target.getAsPlayer().getPacketSender().sendGlobalGraphic(MAGIC_END_GFX, pos);
						for (Player player : character.getAsNpc().getPlayersWithinDistance(10)) {
							if (player.getLocation().equals(pos)) {
								player.getCombat().getHitQueue()
										.addPendingDamage(new HitDamage(Misc.getRandom(25), HitMask.RED));
							}
						}
					}
					finished(character, target);
					stop();
				}
			});
			character.getTimers().register(TimerKey.COMBAT_ATTACK, 5);
		}
	}

	@Override
	public int attackDistance(Mobile character) {
		if (attack == CombatType.MELEE) {
			return 2;
		}
		return 8;
	}
	
	@Override
	public CombatType type() {
		return attack;
	}
}
