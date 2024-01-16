package com.elvarg.game.content.combat.method.impl.specials;

import com.elvarg.game.collision.RegionManager;
import com.elvarg.game.content.Dueling.DuelRule;
import com.elvarg.game.content.combat.CombatFactory;
import com.elvarg.game.content.combat.CombatSpecial;
import com.elvarg.game.content.combat.CombatType;
import com.elvarg.game.content.combat.hit.PendingHit;
import com.elvarg.game.content.combat.method.CombatMethod;
import com.elvarg.game.content.combat.method.impl.MeleeCombatMethod;
import com.elvarg.game.content.sound.Sound;
import com.elvarg.game.content.sound.SoundManager;
import com.elvarg.game.entity.impl.Mobile;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.Animation;
import com.elvarg.game.model.Direction;
import com.elvarg.game.model.Graphic;
import com.elvarg.game.model.GraphicHeight;
import com.elvarg.game.model.Location;
import com.elvarg.game.model.Priority;
import com.elvarg.game.model.dialogues.DialogueManager;
import com.elvarg.game.model.movement.MovementQueue.Point;
import com.elvarg.game.task.Task;
import com.elvarg.game.task.TaskManager;
import com.elvarg.util.timers.TimerKey;

/**
 * Special Attack shared between Dragon Spear/Dragon Hasta/Zamorakian Hasta.
 * 
 * It pushes an opponent back and stuns them for three seconds, consuming 25% of
 * the player's special attack energy.
 * 
 * @author Advocatus | https://www.rune-server.ee/members/119929-advocatus/
 *
 */
public class ShoveCombatMethod extends CombatMethod {

	private static final Animation ANIMATION = new Animation(1064, Priority.HIGH);
	private static final Graphic GRAPHIC = new Graphic(263, GraphicHeight.HIGH, Priority.HIGH);
	private static final Animation STUN_ANIMATION = new Animation(424);

	@Override
	public PendingHit[] hits(Mobile character, Mobile target) {
		/*
		 * Special attack does not show a hitsplat or deal damage.
		 */
		return null;
	}

	@Override
	public void start(Mobile character, Mobile target) {
		/*
		 * The effects of this special are non-stackable, meaning that players cannot
		 * use the spear's special attack on a target who is already stunned. Stunned
		 * players are also given a one tick (0.6 seconds) period of immunity after a
		 * stun wears off in which they cannot be stunned again.
		 */
		if (character.getTimers().has(TimerKey.STUN) || character.getTimers().has(TimerKey.STUN_IMMUNITY)) {
			// reset(character, target);
			return;
		}

		/*
		 * Safety check to not allow the special attack in a duel when movement is
		 * locked.
		 */
		if (character.isPlayer()) {
			Player player = character.getAsPlayer();
			if (player.getDueling().inDuel() && player.getDueling().getRules()[DuelRule.NO_MOVEMENT.ordinal()]) {
				player.sendMessage("This weapon's special attack cannot be used in this duel.");
				// DialogueManager.sendStatement(player, "This weapon's special attack cannot be used in this duel.");
				// reset(character, target);
				return;
			}
		}

		/*
		 * The special attack cannot be used against large monsters, such as giants,
		 * because they are too big to push back. If a player tries this, the special
		 * attack does not occur and no special attack energy is consumed.
		 */
		if (target.size() > 1) {
			character.sendMessage("That creature is too large to knock back!");
			// reset(character, target);
			return;
		}

		CombatSpecial.drain(character, CombatSpecial.SHOVE_SPECIAL.getDrainAmount());
		character.performAnimation(ANIMATION);
		character.performGraphic(GRAPHIC);
		target.performAnimation(STUN_ANIMATION);
		CombatFactory.stun(target, 3, true);

		/*
		 * In addition to this, as soon as the stun wears off, the stunned player is
		 * also granted a 5 tick (3.0 seconds) period of immunity against being bound in
		 * place by binding spells like Entangle or Ice Barrage.
		 */
		// stun immunity for a cycle after the stun ends.
		character.getTimers().register(TimerKey.STUN_IMMUNITY, 6);
		// freeze immunity for 5 ticks after stun ends.
		character.getTimers().register(TimerKey.FREEZE_IMMUNITY, 10);

		if (character.isPlayer())
			SoundManager.sendSound(character.getAsPlayer(), Sound.DRAGON_SPEAR_SPECIAL);

		final Direction dir = getDirection(character, target);
		if (dir != null) {
			Location dest = target.getLocation().transform(dir.getX(), dir.getY());
			if (RegionManager.canMove(target.getLocation(), dest, 1, 1, target.getPrivateArea())) {

				/*
				 * This is a workaround for movement queue being a mess. This is functionally
				 * identical without having to hack up existing code and move safety checks.
				 * 
				 * The logic here is that tasks with a time of 1 are processed the next cycle
				 * which occurs before combat/update tick just like normal calling of
				 * MovementQueue.process(), This also occurs after the walk information is
				 * cleared from the previous cycle.
				 */
				TaskManager.submit(new Task(1) {
					@Override
					public void execute() {
						target.setLocation(dest);
						target.setWalkingDirection(dir);
						if (target.isPlayer()) {
							target.getMovementQueue().handleRegionChange();
						}
						this.stop();
					}
				});
			}
		}

		/*
		 * Generic information to add combat delay and turn off the special attack bar.
		 */
		character.getTimers().register(TimerKey.COMBAT_ATTACK, character.getBaseAttackSpeed());
		character.setSpecialActivated(false);
		if (character.isPlayer()) {
			CombatSpecial.updateBar(character.getAsPlayer());
		}
	}

	/*
	 * Method that may be used for instances where the special attack fails to
	 * instead execute a normal melee attack. This is a placeholder and may not be
	 * needed.
	 */
	private void reset(Mobile character, Mobile target) {
		character.setSpecialActivated(false);
		if (character.isPlayer()) {
			Player p = character.getAsPlayer();
			CombatSpecial.updateBar(p);
		}
		character.getCombat().performNewAttack(false);
	}

	/*
	 * Gets the direction between two Mobile entities.
	 * 
	 * @author Arios530
	 */
	private Direction getDirection(Mobile character, Mobile target) {
		Direction dir = null;
		int vx = target.getLocation().getX();
		int vy = target.getLocation().getY();
		int sx = character.getLocation().getX();
		int sy = character.getLocation().getY();
		if (vx == sx && vy > sy) {
			dir = Direction.NORTH;
		} else if (vx == sx && vy < sy) {
			dir = Direction.SOUTH;
		} else if (vx > sx && vy == sy) {
			dir = Direction.EAST;
		} else if (vx < sx && vy == sy) {
			dir = Direction.WEST;
		} else if (vx > sx && vy > sy) {
			dir = Direction.NORTH_EAST;
		} else if (vx < sx && vy > sy) {
			dir = Direction.NORTH_WEST;
		} else if (vx > sx && vy < sy) {
			dir = Direction.SOUTH_EAST;
		} else if (vx < sx && vy < sy) {
			dir = Direction.SOUTH_WEST;
		}
		return dir;
	}

	@Override
	public CombatType type() {
		return CombatType.MELEE;
	}
}
