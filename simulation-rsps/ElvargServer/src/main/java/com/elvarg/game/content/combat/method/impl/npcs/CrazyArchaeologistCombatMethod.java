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
import com.elvarg.game.model.GraphicHeight;
import com.elvarg.game.model.Location;
import com.elvarg.game.model.Projectile;
import com.elvarg.game.task.Task;
import com.elvarg.game.task.TaskManager;
import com.elvarg.util.Misc;
import com.elvarg.util.timers.TimerKey;

public class CrazyArchaeologistCombatMethod extends CombatMethod {

    private static final String[] QUOTES = { "I'm Bellock - respect me!", "Get off my site!",
            "No-one messes with Bellock's dig!", "These ruins are mine!", "Taste my knowledge!",
            "You belong in a museum!", };

	private static enum Attack {
		SPECIAL_ATTACK, DEFAULT_RANGED_ATTACK, DEFAULT_MELEE_ATTACK;
	}

	private Attack attack = Attack.DEFAULT_RANGED_ATTACK;
	private static final Graphic RANGED_END_GFX = new Graphic(305, GraphicHeight.HIGH);
	private static final Graphic MAKE_IT_RAIN_START_GFX = new Graphic(157, GraphicHeight.MIDDLE);
	private static final Animation MELEE_ATTACK_ANIM = new Animation(423);
	private static final Animation RANGED_ATTACK_ANIM = new Animation(3353);
	private static final Projectile SPECIAL_PROJECTILE = new Projectile(1260, 31, 43, 40, 80);
	private static final Projectile RANGED_PROJECILE = new Projectile(1259, 31, 43, 40, 65);

	@Override
	public PendingHit[] hits(Mobile character, Mobile target) {
		if (attack == Attack.SPECIAL_ATTACK) {
			return null;
		}
		int delay = 2;
		if (attack == Attack.DEFAULT_MELEE_ATTACK) {
			delay = 0;
		}
		return new PendingHit[] { new PendingHit(character, target, this, delay) };
	}

	@Override
	public void start(Mobile character, Mobile target) {
		if (!character.isNpc() || !target.isPlayer())
			return;

		attack = Attack.DEFAULT_RANGED_ATTACK;

		if (target.getLocation().getDistance(character.getLocation()) < 2 && Misc.getRandom(1) == 0) {
			attack = Attack.DEFAULT_MELEE_ATTACK;
		}

		if (Misc.getRandom(10) < 3) {
			attack = Attack.SPECIAL_ATTACK;
		}
		
		character.forceChat(QUOTES[Misc.getRandom(QUOTES.length - 1)]);

		if (attack == Attack.DEFAULT_RANGED_ATTACK) {
			character.performAnimation(RANGED_ATTACK_ANIM);
			Projectile.sendProjectile(character, target, RANGED_PROJECILE);
			TaskManager.submit(new Task(3, target, false) {
				@Override
				public void execute() {
					target.performGraphic(RANGED_END_GFX);
					stop();
				}
			});
		} else if (attack == Attack.SPECIAL_ATTACK) {
			character.performAnimation(RANGED_ATTACK_ANIM);
			character.forceChat("Rain of Knowledge!");
			Location targetPos = target.getLocation();
			List<Location> attackPositions = new ArrayList<>();
			attackPositions.add(targetPos);
			for (int i = 0; i < 2; i++) {
				attackPositions.add(new Location((targetPos.getX() - 1) + Misc.getRandom(3),
						(targetPos.getY() - 1) + Misc.getRandom(3)));
			}
			for (Location pos : attackPositions) {
				Projectile.sendProjectile(character, pos, SPECIAL_PROJECTILE);
			}
			TaskManager.submit(new Task(4) {
				@Override
				public void execute() {
					for (Location pos : attackPositions) {
						target.getAsPlayer().getPacketSender().sendGlobalGraphic(MAKE_IT_RAIN_START_GFX, pos);
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
		} else if (attack == Attack.DEFAULT_MELEE_ATTACK) {
			character.performAnimation(MELEE_ATTACK_ANIM);
		}
	}

	@Override
	public int attackSpeed(Mobile character) {
		if (attack == Attack.DEFAULT_MELEE_ATTACK) {
			return 3;
		}
		return super.attackSpeed(character);
	}

	@Override
	public int attackDistance(Mobile character) {
		if (attack == Attack.DEFAULT_MELEE_ATTACK) {
			return 1;
		}
		if (attack == Attack.SPECIAL_ATTACK) {
			return 8;
		}
		return 6;
	}

	@Override
	public CombatType type() {
		if (attack == Attack.DEFAULT_MELEE_ATTACK) {
			return CombatType.MELEE;
		}
		return CombatType.RANGED;
	}
}
