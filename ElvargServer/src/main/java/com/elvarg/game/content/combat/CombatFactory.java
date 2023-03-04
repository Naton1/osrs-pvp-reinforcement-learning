package com.elvarg.game.content.combat;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import com.elvarg.game.content.minigames.impl.pestcontrol.PestControl;
import com.elvarg.game.content.sound.Sound;
import com.elvarg.game.content.sound.SoundManager;
import com.elvarg.game.collision.RegionManager;
import com.elvarg.game.content.PrayerHandler;
import com.elvarg.game.content.Dueling.DuelRule;
import com.elvarg.game.content.combat.WeaponInterfaces.WeaponInterface;
import com.elvarg.game.content.combat.formula.DamageFormulas;
import com.elvarg.game.content.combat.hit.HitDamage;
import com.elvarg.game.content.combat.hit.HitMask;
import com.elvarg.game.content.combat.hit.PendingHit;
import com.elvarg.game.content.combat.method.CombatMethod;
import com.elvarg.game.content.combat.method.impl.MagicCombatMethod;
import com.elvarg.game.content.combat.method.impl.MeleeCombatMethod;
import com.elvarg.game.content.combat.method.impl.RangedCombatMethod;
import com.elvarg.game.content.combat.ranged.RangedData;
import com.elvarg.game.content.combat.ranged.RangedData.Ammunition;
import com.elvarg.game.content.combat.ranged.RangedData.RangedWeapon;
import com.elvarg.game.content.combat.ranged.RangedData.RangedWeaponType;
import com.elvarg.game.entity.impl.Mobile;
import com.elvarg.game.entity.impl.npc.NPC;
import com.elvarg.game.entity.impl.npc.NPCMovementCoordinator.CoordinateState;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.entity.impl.playerbot.PlayerBot;
import com.elvarg.game.model.Animation;
import com.elvarg.game.model.EffectTimer;
import com.elvarg.game.model.Flag;
import com.elvarg.game.model.Graphic;
import com.elvarg.game.model.GraphicHeight;
import com.elvarg.game.model.Item;
import com.elvarg.game.model.Location;
import com.elvarg.game.model.Skill;
import com.elvarg.game.model.SkullType;
import com.elvarg.game.model.areas.AreaManager;
import com.elvarg.game.model.areas.impl.WildernessArea;
import com.elvarg.game.model.container.impl.Equipment;
import com.elvarg.game.model.movement.MovementQueue;
import com.elvarg.game.model.movement.path.PathFinder;
import com.elvarg.game.model.rights.PlayerRights;
import com.elvarg.game.task.Task;
import com.elvarg.game.task.TaskManager;
import com.elvarg.game.task.impl.CombatPoisonEffect;
import com.elvarg.game.task.impl.CombatPoisonEffect.CombatPoisonData;
import com.elvarg.game.task.impl.CombatPoisonEffect.PoisonType;
import com.elvarg.util.ItemIdentifiers;
import com.elvarg.util.Misc;
import com.elvarg.util.NpcIdentifiers;
import com.elvarg.util.RandomGen;
import com.elvarg.util.timers.TimerKey;

/**
 * Acts as a utility class for combat.
 *
 * @author Professor Oak
 */
public class CombatFactory {
	private static final RandomGen RANDOM = new RandomGen();

	public enum CanAttackResponse {
		INVALID_TARGET,
		ALREADY_UNDER_ATTACK,
		CANT_ATTACK_IN_AREA,
		COMBAT_METHOD_NOT_ALLOWED,
		LEVEL_DIFFERENCE_TOO_GREAT,
		NOT_ENOUGH_SPECIAL_ENERGY,
		STUNNED,
		DUEL_NOT_STARTED_YET,
		DUEL_MELEE_DISABLED,
		DUEL_RANGED_DISABLED,
		DUEL_MAGIC_DISABLED,
		DUEL_WRONG_OPPONENT,
		TARGET_IS_IMMUNE,
		CAN_ATTACK,
		CASTLE_WARS_FRIENDLY_FIRE,
	}

	/**
	 * The default melee combat method.
	 */
	public static final CombatMethod MELEE_COMBAT = new MeleeCombatMethod();

	/**
	 * The default ranged combat method
	 */
	public static final CombatMethod RANGED_COMBAT = new RangedCombatMethod();

	/**
	 * The default magic combat method
	 */
	public static final CombatMethod MAGIC_COMBAT = new MagicCombatMethod();

	/**
	 * Gets a character's combat method.
	 *
	 * @param attacker
	 *            The character to get the combat method for.
	 * @return
	 */
	public static CombatMethod getMethod(Mobile attacker) {
		if (attacker.isPlayer()) {
			Player p = attacker.getAsPlayer();

			// Update player data..
			// Update ranged ammo / weapon
			p.getCombat().setAmmunition(Ammunition.getFor(p));
			p.getCombat().setRangedWeapon(RangedWeapon.getFor(p));

			// Check if player is maging..
			if (p.getCombat().getCastSpell() != null ||
					// Ensure player needs staff equipped to use autocast
					(p.getCombat().getAutocastSpell() != null && p.getEquipment().hasStaffEquipped())) {
				return MAGIC_COMBAT;
			}

			// Check special attacks..
			if (p.getCombatSpecial() != null) {
				if (p.isSpecialActivated()) {
					return p.getCombatSpecial().getCombatMethod();
				}
			}

			// Check if player is ranging..
			if (p.getCombat().getRangedWeapon() != null) {
				return RANGED_COMBAT;
			}

		} else if (attacker.isNpc()) {
			return attacker.getAsNpc().getCombatMethod();
		}

		// Return melee by default
		return MELEE_COMBAT;
	}

	/**
	 * Generates a random {@link HitDamage} based on the argued entity's stats.
	 *
	 * @param entity
	 *            the entity to generate the random hit for.
	 * @param victim
	 *            the victim being attacked.
	 * @param type
	 *            the combat type being used.
	 * @return the HitDamage.
	 */
	public static HitDamage getHitDamage(Mobile entity, Mobile victim, CombatType type) {
		
		//calculate the multiplier that will be used when calculating protection prayers.
		double damageMultiplier = entity.isNpc() ? CombatConstants.PRAYER_DAMAGE_REDUCTION_AGAINST_NPCS : CombatConstants.PRAYER_DAMAGE_REDUCTION_AGAINST_PLAYERS;
		
		int damage = 0;

		if (type == CombatType.MELEE) {
			damage = Misc.inclusive(0, DamageFormulas.calculateMaxMeleeHit(entity));

			// Do melee effects with the calculated damage..
			if (victim.getPrayerActive()[PrayerHandler.PROTECT_FROM_MELEE]) {
				damage *= damageMultiplier;
			}

		} else if (type == CombatType.RANGED) {
			damage = Misc.inclusive(0, DamageFormulas.calculateMaxRangedHit(entity));

			if (victim.getPrayerActive()[PrayerHandler.PROTECT_FROM_MISSILES]) {
				damage *= damageMultiplier;
			}

			// Do ranged effects with the calculated damage..
			if (entity.isPlayer()) {

				Player player = entity.getAsPlayer();

				// Check if player is using dark bow and set damage to minimum 8, maxmimum 48 if
				// that's the case...
				if (player.getAsPlayer().isSpecialActivated()
						&& player.getAsPlayer().getCombatSpecial() == CombatSpecial.DARK_BOW) {
					if (damage < 8) {
						damage = 8;
					} else if (damage > 48) {
						damage = 48;
					}
				}

				// Handle bolt special effects for a player whose using crossbow
				if (player.getWeapon() == WeaponInterface.CROSSBOW && Misc.getRandom(10) == 1) {
					double multiplier = RangedData.getSpecialEffectsMultiplier(player, victim, damage);
					damage *= multiplier;
				}
			}
		} else if (type == CombatType.MAGIC) {
			damage = Misc.inclusive(0, DamageFormulas.getMagicMaxhit(entity));
			if (victim.getPrayerActive()[PrayerHandler.PROTECT_FROM_MAGIC]) {
				damage *= damageMultiplier;
			}

			// Do magic effects with the calculated damage..
		}

		// We've got our damage. We can now create a HitDamage
		// instance.
		HitDamage hitDamage = new HitDamage(damage, damage == 0 ? HitMask.BLUE : HitMask.RED);

		// Check elysian spirit shield damage reduction
		if (victim.isPlayer() && Misc.getRandom(100) <= 70) {
			if (victim.getAsPlayer().getEquipment().getItems()[Equipment.SHIELD_SLOT].getId() == 12817) {
				hitDamage.multiplyDamage(CombatConstants.ELYSIAN_DAMAGE_REDUCTION);
				victim.performGraphic(new Graphic(321, 40)); // Elysian spirit shield effect gfx
			}
		}

		// Return our hitDamage that may have been modified slightly.
		return hitDamage;
	}

	/**
	 * Checks if an entity is a valid target.
	 *
	 * @param attacker
	 * @param target
	 * @return
	 */
	public static boolean validTarget(Mobile attacker, Mobile target) {
	    if (attacker == null || target == null) {
	        return false;
	    }
	    
		if (!target.isRegistered() || !attacker.isRegistered() || attacker.getHitpoints() <= 0
				|| target.getHitpoints() <= 0 || attacker.isUntargetable()) {
			return false;
		}

		if (attacker.getLocation().getDistance(target.getLocation()) >= 40) {
			return false;
		}

		if (attacker.isNpc() && target.isPlayer()) {
			if (attacker.getAsNpc().getOwner() != null && attacker.getAsNpc().getOwner() != target.getAsPlayer()) {
				return false;
			}
		} else if (attacker.isPlayer() && target.isNpc()) {
			if (target.getAsNpc().getOwner() != null && target.getAsNpc().getOwner() != attacker.getAsPlayer()) {
				attacker.getAsPlayer().getPacketSender().sendMessage("This npc was not spawned for you.");
				return false;
			}
		}

		return true;
	}

	/**
	 * Checks if an entity can reach a target.
	 * If false, combat is reset for the attacker.
	 *
	 * @param attacker
	 *            The entity which wants to attack.
/	 * @param cb_type
	 *            The combat type the attacker is using.
	 * @param target
	 *            The victim.
	 * @return True if attacker has the proper distance to attack, otherwise false.
	 */	
	public static boolean canReach(Mobile attacker, CombatMethod method, Mobile target) {
		if (!validTarget(attacker, target)) {
		    attacker.getCombat().reset();
			return true;
		}

		boolean isMoving = target.getMovementQueue().isMoving();

		// Walk back if npc is too far away from spawn position.
		if (attacker.isNpc()) {
			NPC npc = attacker.getAsNpc();
			if (npc.getCurrentDefinition().doesRetreat()) {
				if (npc.getMovementCoordinator().getCoordinateState() == CoordinateState.RETREATING) {
					npc.getCombat().reset();
					return false;
				}
				if (npc.getLocation().getDistance(npc.getSpawnPosition()) >= npc.getCurrentDefinition().getCombatFollowDistance()) {
					npc.getCombat().reset();
					npc.getMovementCoordinator().setCoordinateState(CoordinateState.RETREATING);
					return false;
				}
			}
		}
		
		final Location attackerPosition = attacker.getLocation();
		final Location targetPosition = target.getLocation();
		
		if (attackerPosition.equals(targetPosition)) {
			if (!attacker.getTimers().has(TimerKey.STEPPING_OUT)) {
				MovementQueue.clippedStep(attacker);
				attacker.getTimers().register(TimerKey.STEPPING_OUT, 2);
			}
		    return false;
		}

		int requiredDistance = method.attackDistance(attacker);		
        int distance = attacker.calculateDistance(target);
        
        // Standing under the target
        if (distance == 0) {
            if (attacker.isPlayer()) {
                return false;
            }
            if (attacker.isNpc() && attacker.size() == 0) {
                return false;
            }
        }

		if (method.type() == CombatType.MELEE && isMoving && attacker.getMovementQueue().isMoving()) {
			// If we're using Melee and either player is moving, increase required distance
			requiredDistance++;
		}

        // Too far away from the target
		if (distance > requiredDistance) {
		    return false;
		}

		// Don't allow diagonal attacks for smaller entities
		if (method.type() == CombatType.MELEE && attacker.size() == 1 && target.size() == 1 && !isMoving && !target.getMovementQueue().isMoving()) {
			if (PathFinder.isDiagonalLocation(attacker, target)) {
				stepOut(attacker, target);
				return false;
			}
		}

        // Make sure we the path is clear for projectiles..
        if (attacker.useProjectileClipping() && !RegionManager.canProjectileAttack(attacker, target)) {
			return false;
		}

		return true;
	}


	private static void stepOut(Mobile attacker, Mobile target) {
		List<Location> tiles = Arrays.asList(
				new Location(target.getLocation().getX() - 1, target.getLocation().getY()),
				new Location(target.getLocation().getX() + 1, target.getLocation().getY()),
				new Location(target.getLocation().getX(), target.getLocation().getY() + 1),
				new Location(target.getLocation().getX(), target.getLocation().getY() - 1));
		/** If a tile is present it will step out **/
		tiles.stream().filter(t -> !RegionManager.blocked(t, attacker.getPrivateArea())).min(Comparator.comparing(attacker.getLocation()::getDistance)).ifPresent(tile ->
				PathFinder.calculateWalkRoute(attacker, tile.getX(), tile.getY()));
	}

	/**
	 * Checks if an entity can attack a target.
	 *
	 * @param attacker
	 *            The entity which wants to attack.
//	 * @param cb_type
	 *            The combat type the attacker is using.
	 * @param target
	 *            The victim.
	 * @return True if attacker has the requirements to attack, otherwise false.
	 */
	public static CanAttackResponse canAttack(Mobile attacker, CombatMethod method, Mobile target) {
		if (!validTarget(attacker, target)) {
			return CanAttackResponse.INVALID_TARGET;
		}

		// Here we check if we are already in combat with another entity.
		// Only check if we aren't in multi.
		if (!(AreaManager.inMulti(attacker) && AreaManager.inMulti(target))) {
			if (isBeingAttacked(attacker) && attacker.getCombat().getAttacker() != target
					&& attacker.getCombat().getAttacker().getHitpoints() > 0
					|| !attacker.getCombat().getHitQueue().isEmpty(target)) {

				return CanAttackResponse.ALREADY_UNDER_ATTACK;
			}

			// Here we check if we are already in combat with another entity.
			if (isBeingAttacked(target) && target.getCombat().getAttacker() != attacker
					|| !target.getCombat().getHitQueue().isEmpty(attacker)) {
				return CanAttackResponse.ALREADY_UNDER_ATTACK;
			}
		}

		// Check if we can attack in this area
		CanAttackResponse areaResponse = AreaManager.canAttack(attacker, target);
		if (areaResponse != CanAttackResponse.CAN_ATTACK) {
			return areaResponse;
		}

		if (!method.canAttack(attacker, target)) {
			return CanAttackResponse.COMBAT_METHOD_NOT_ALLOWED;
		}

		// Check special attack
		if (attacker.isPlayer()) {
			Player p = attacker.getAsPlayer();

			// Check if we're using a special attack..
			if (p.isSpecialActivated() && p.getCombatSpecial() != null) {
				// Check if we have enough special attack percentage.
				// If not, reset special attack.
				if (p.getSpecialPercentage() < p.getCombatSpecial().getDrainAmount()) {
					return CanAttackResponse.NOT_ENOUGH_SPECIAL_ENERGY;
				}
			}

			if (p.getTimers().has(TimerKey.STUN)) {
				return CanAttackResponse.STUNNED;
			}
			
			// Duel rules
            if (p.getDueling().inDuel()) {
                String errorStatement = null;
                if (method.type() == CombatType.MELEE && p.getDueling().getRules()[DuelRule.NO_MELEE.ordinal()]) {
					return CanAttackResponse.DUEL_MELEE_DISABLED;
                } else if (method.type() == CombatType.RANGED && p.getDueling().getRules()[DuelRule.NO_RANGED.ordinal()]) {
					return CanAttackResponse.DUEL_RANGED_DISABLED;
                } else if (method.type() == CombatType.MAGIC && p.getDueling().getRules()[DuelRule.NO_MAGIC.ordinal()]) {
					return CanAttackResponse.DUEL_MAGIC_DISABLED;
                }
            }
		}

		// Check immune npcs..
		if (target.isNpc()) {
			NPC npc = (NPC) target;
			if (npc.getTimers().has(TimerKey.ATTACK_IMMUNITY)) {
				return CanAttackResponse.TARGET_IS_IMMUNE;
			}
		}

		return CanAttackResponse.CAN_ATTACK;
	}

	/**
	 * Adds a hit to a target's queue.
	 *
	 * @param qHit
	 */
	public static void addPendingHit(PendingHit qHit) {
		Mobile attacker = qHit.getAttacker();
		Mobile target = qHit.getTarget();
		if (target.getHitpoints() <= 0) {
			return;
		}

		if (target.isUntargetable() || target.isNeedsPlacement()) {
			// If target is teleporting or needs placement, don't register the hit
			return;
		}

		if (attacker.isPlayer()) {
			// Reward the player experience for this attack..
			rewardExp(attacker.getAsPlayer(), qHit);

			if (attacker.getAsPlayer().getArea() != null) {
				attacker.getAsPlayer().getArea().onPlayerDealtDamage(attacker.getAsPlayer(), target, qHit);
			}

			// Check if the player should be skulled for making this attack..
			if (target.isPlayer()) {
				handleSkull(attacker.getAsPlayer(), target.getAsPlayer());
			}
		}

		// Add this hit to the target's hitQueue
		target.getCombat().getHitQueue().addPendingHit(qHit);
	}

	/**
	 * Executes a hit that has been ticking until now.
	 *
	 * @param qHit
	 *            The QueueableHit to execute.
	 */
	public static void executeHit(PendingHit qHit) {
		final Mobile attacker = qHit.getAttacker();
		final Mobile target = qHit.getTarget();
		final CombatMethod method = qHit.getCombatMethod();
		final CombatType combatType = qHit.getCombatType();
		final int damage = qHit.getTotalDamage();

		// If target/attacker is dead, don't continue.
		if (target.getHitpoints() <= 0 || attacker.getHitpoints() <= 0) {
			return;
		}

		// If target is teleporting or needs placement
		// Don't continue to add the hit.
		if (target.isUntargetable() || target.isNeedsPlacement()) {
			return;
		}

		// Before target takes damage, manipulate the hit to handle
		// last-second effects
		qHit = target.manipulateHit(qHit);

		// Do block animation
		target.performAnimation(new Animation(target.getBlockAnim()));

		// Do other stuff for players..
		if (target.isPlayer()) {
			final Player p_ = target.getAsPlayer();
			SoundManager.sendSound(p_, Sound.FEMALE_GETTING_HIT);

			// Close their current interface
			if (p_.getRights() != PlayerRights.DEVELOPER && p_.busy()) {
				p_.getPacketSender().sendInterfaceRemoval();
			}

			// Prayer effects
			if (qHit.isAccurate()) {

				if (PrayerHandler.isActivated(p_, PrayerHandler.REDEMPTION)) {
					handleRedemption(attacker, p_, damage);
				}

				if (PrayerHandler.isActivated(attacker, PrayerHandler.SMITE)) {
					handleSmite(attacker, p_, damage);
				}
			}
		}

		// Here, we take the damage.
		// BUT, don't take damage if the attack was a magic splash by a player.
		boolean magic_splash = (combatType == CombatType.MAGIC && !qHit.isAccurate());
		if (!(magic_splash && attacker.isPlayer())) {
			target.getCombat().getHitQueue().addPendingDamage(qHit.getHits());
		}

		// Make sure to let the combat method know we finished the attack
		// Only if this isn't custom hit (handleAfterHitEffects() will be false then)
		if (qHit.handleAfterHitEffects()) {
			if (method != null) {
				method.handleAfterHitEffects(qHit);
			}
		}

		// Check for poisonous weapons..
		// And do other effects, such as barrows effects..
		if (attacker.isPlayer()) {
			Player p_ = attacker.getAsPlayer();

			// Randomly apply poison if poisonous weapon is equipped.
			if (damage > 0 && Misc.getRandom(20) <= 5) { // 1/4

				Optional<PoisonType> poison = Optional.empty();
				boolean isRanged = false;

				if (combatType == CombatType.MELEE || p_.getWeapon() == WeaponInterface.DART
						|| p_.getWeapon() == WeaponInterface.KNIFE
						|| p_.getWeapon() == WeaponInterface.THROWNAXE
						|| p_.getWeapon() == WeaponInterface.JAVELIN) {
					poison = CombatPoisonData.getPoisonType(p_.getEquipment().get(Equipment.WEAPON_SLOT));
				} else if (combatType == CombatType.RANGED) {
					isRanged = true;
					poison = CombatPoisonData.getPoisonType(p_.getEquipment().get(Equipment.AMMUNITION_SLOT));
				}

				if (poison.isPresent() && (!isRanged || Misc.getRandom(10) <= 5)) { // Range 1/8
					CombatFactory.poisonEntity(target, poison.get());
				}
			}

			// Handle barrows effects if damage is more than zero.
			if (qHit.getTotalDamage() > 0) {
				if (Misc.getRandom(10) >= 8) {

					// Apply Guthan's effect..
					if (fullGuthans(p_)) {
						handleGuthans(p_, target, qHit.getTotalDamage());
					}

					// Other barrows effects here..
				}
			}
		} else if (attacker.isNpc()) {
			NPC npc = attacker.getAsNpc();
			if (npc.getCurrentDefinition().isPoisonous()) {
				if (Misc.getRandom(10) <= 5) {
					CombatFactory.poisonEntity(target, PoisonType.SUPER);
				}
			}
		}

		// Handle ring of recoil for target
		// Also handle vengeance for target
		if (qHit.getTotalDamage() > 0) {
			if (target.isPlayer()) {
				Player player = target.getAsPlayer();
				if (player.getEquipment().get(Equipment.RING_SLOT).getId() == ItemIdentifiers.RING_OF_RECOIL) {
					handleRecoil(player, attacker, qHit.getTotalDamage());
				}
			}
			if (target.hasVengeance()) {
				handleVengeance(target, attacker, qHit.getTotalDamage());
			}
		}

		// Auto retaliate if needed
		handleRetaliation(attacker, target);

		// Set under attack
		target.getCombat().setUnderAttack(attacker);

		// Add damage to target damage map
		target.getCombat().addDamage(attacker, qHit.getTotalDamage());

		if (target instanceof PlayerBot) {
			((PlayerBot) target).getCombatInteraction().takenDamage(qHit.getTotalDamage(), attacker);
		}
	}

	/**
	 * Rewards a player with experience in respective skills based on how much
	 * damage they've dealt.
	 *
	 * @param player
	 *            The player.
	 * @param hit
	 *            The damage dealt.
	 */
	public static void rewardExp(Player player, PendingHit hit) {

		// Add magic exp, even if total damage is 0.
		// Since spells have a base exp reward
		if (hit.getCombatType() == CombatType.MAGIC) {
			if (player.getCombat().getPreviousCast() != null) {
				if (hit.isAccurate()) {
					player.getSkillManager().addExperience(Skill.MAGIC,
							(int) (hit.getTotalDamage())/* + player.getCombat().getPreviousCast().baseExperience() */);
				} else {
					// Splash should only give 52 exp..
					player.getSkillManager().addExperience(Skill.MAGIC, 52, false);
				}
			}
		}

		// Don't add any exp to other skills if total damage is 0.
		if (hit.getTotalDamage() <= 0) {
			return;
		}

		// Add hp xp
		player.getSkillManager().addExperience(Skill.HITPOINTS, (int) ((hit.getTotalDamage() * .70)));

		// Magic xp was already added
		if (hit.getCombatType() == CombatType.MAGIC) {
			return;
		}

		// Add all other skills xp
		final int[] exp = hit.getSkills();
		for (int i : exp) {
			Skill skill = Skill.values()[i];
			player.getSkillManager().addExperience(skill, (int) (((hit.getTotalDamage()) / exp.length)));
		}
	}

	/**
	 * Checks if a character is currently attacking.
	 *
	 * @param character
	 *            The character to check for.
	 * @return true if character is attacking, false otherwise.
	 */
	public static boolean isAttacking(Mobile character) {
		return character.getCombat().getTarget() != null;
	}

	/**
	 * Checks if a character is currently under attack.
	 *
	 * @param character
	 *            The character to check for.
	 * @return true if character is being attacked, false otherwise.
	 */
	public static boolean isBeingAttacked(Mobile character) {
		return character.getCombat().getAttacker() != null;
	}

	/**
	 * Checks if a character is currently in combat.
	 *
	 * @param character
	 *            The character to check for.
	 * @return true if character is in combat, false otherwise.
	 */
	public static boolean inCombat(Mobile character) {
		return isAttacking(character) || isBeingAttacked(character);
	}

	/**
	 * Attempts to poison the argued {@link Mobile} with the argued
	 * {@link PoisonType}. This method will have no effect if the entity is already
	 * poisoned.
	 *
	 * @param entity
	 *            the entity that will be poisoned, if not already.
	 * @param poisonType
	 *            the poison type that this entity is being inflicted with.
	 */
	public static void poisonEntity(Mobile entity, PoisonType poisonType) {

		// We are already poisoned or the poison type is invalid, do nothing.
		if (entity.isPoisoned()) {
			return;
		}

		// If the entity is a player, we check for poison immunity. If they have
		// no immunity then we send them a message telling them that they are
		// poisoned.
		if (entity.isPlayer()) {
			Player player = (Player) entity;
			if (!player.getCombat().getPoisonImmunityTimer().finished()) {
				return;
			}
			player.getPacketSender().sendMessage("You have been poisoned!");
			if (poisonType == PoisonType.VENOM) {
				player.getPacketSender().sendPoisonType(2);
			} else {
				player.getPacketSender().sendPoisonType(1);
			}
		}

		entity.setPoisonDamage(poisonType.getDamage());
		TaskManager.submit(new CombatPoisonEffect(entity));
	}

	/**
	 * Disables protection prayers for a player.
	 *
	 * @param player
	 *            The player to disable protecetion prayers for.
	 */
	public static void disableProtectionPrayers(Player player) {
		// Player has already been prayer-disabled
		if (!player.getCombat().getPrayerBlockTimer().finished()) {
			return;
		}

		player.getCombat().getPrayerBlockTimer().start(200);
		PrayerHandler.resetPrayers(player, PrayerHandler.PROTECTION_PRAYERS);
		player.getPacketSender().sendMessage("You have been disabled and can no longer use protection prayers.");
	}

	/**
	 * Handles the item "Ring of Recoil" for a player. The item returns damage to
	 * the attacker.
	 *
	 * @param player
	 * @param attacker
	 * @param damage
	 */
	public static void handleRecoil(Player player, Mobile attacker, int damage) {
		if (damage == 0) {
			return;
		}
		final double RECOIL_DMG_MULTIPLIER = 0.1;
		int returnDmg = RANDOM.inclusive(1, 3) == 2 ? 0 : (int) (damage * RECOIL_DMG_MULTIPLIER) + 1;

		// Increase recoil damage for a player.
		player.setRecoilDamage(player.getRecoilDamage() + returnDmg);

		// Deal damage back to attacker
		attacker.getCombat().getHitQueue().addPendingDamage(new HitDamage(returnDmg, HitMask.RED));

		// Degrading ring of recoil for a player.
		if (player.getRecoilDamage() >= 40) {
			player.getEquipment().set(Equipment.RING_SLOT, new Item(-1));
			player.getEquipment().refreshItems();
			player.getPacketSender().sendMessage("Your ring of recoil has degraded.");
			player.setRecoilDamage(0);
		}
	}

	/**
	 * Handles the spell "Vengeance" for a player. The spell returns damage to the
	 * attacker.
	 *
//	 * @param player
	 * @param attacker
	 * @param damage
	 */
	public static void handleVengeance(Mobile character, Mobile attacker, int damage) {
		int returnDmg = (int) (damage * 0.75);
		if (returnDmg <= 0) {
			return;
		}

		attacker.getCombat().getHitQueue().addPendingDamage(new HitDamage(returnDmg, HitMask.RED));
		character.forceChat("Taste Vengeance!");
		character.setHasVengeance(false);
	}

	/**
	 * Handles the Guthan's set effect for a player. Wearing full guthan's has a
	 * small chance of healing the player.
	 *
	 * @param player
	 * @param target
	 * @param damage
	 */
	public static void handleGuthans(Player player, Mobile target, int damage) {
		target.performGraphic(new Graphic(398));
		player.heal(damage);
	}

	/**
	 * Checks if a player should be skulled or not.
	 *
	 * @param attacker
	 * @param target
	 */
	public static void handleSkull(Player attacker, Player target) {

		if (attacker.isSkulled()) {
			return;
		}

		if (!(attacker.getArea() instanceof WildernessArea)) {
			return;
		}

		// We've probably already been skulled by this player.
		if (target.getCombat().damageMapContains(attacker) || attacker.getCombat().damageMapContains(target)) {
			return;
		}

		if (target.getCombat().getAttacker() != null && target.getCombat().getAttacker() == attacker) {
			return;
		}

		if (attacker.getCombat().getAttacker() != null && attacker.getCombat().getAttacker() == target) {
			return;
		}

		skull(attacker, SkullType.WHITE_SKULL, 300);
	}

	/**
	 * Skulls the specified player
	 *
	 * @param player
	 * @param type
	 * @param seconds
	 */
	public static void skull(Player player, SkullType type, int seconds) {
		player.setSkullType(type);
		player.setSkullTimer(Misc.getTicks(seconds));
		player.getUpdateFlag().flag(Flag.APPEARANCE);
		if (type == SkullType.RED_SKULL) {
			player.getPacketSender().sendMessage(
					"@bla@You have received a @red@red skull@bla@! You can no longer use the Protect item prayer!");
			PrayerHandler.deactivatePrayer(player, PrayerHandler.PROTECT_ITEM);
		} else if (type == SkullType.WHITE_SKULL) {
			player.getPacketSender().sendMessage("You've been skulled!");
		}
	}

	/**
	 * Stuns a character for the specified seconds.
	 *
//	 * @param player
	 * @param seconds
	 */
	public static void stun(Mobile character, int seconds, boolean force) {
		if (!force) {
			if (character.getTimers().has(TimerKey.STUN)) {
				return;
			}
		}

		character.getTimers().register(TimerKey.STUN, Misc.getTicks(seconds));
		character.getCombat().reset();
		character.getMovementQueue().reset();
		character.performGraphic(new Graphic(348, GraphicHeight.HIGH));

		if (character.isPlayer()) {
			character.getAsPlayer().getPacketSender().sendMessage("You've been stunned!");
		}
	}

	/**
	 * Handles retalation for a character.
	 *
	 * @param attacker
	 * @param target
	 */
	public static void handleRetaliation(Mobile attacker, Mobile target) {
		if (!CombatFactory.isAttacking(target)) {

			boolean auto_ret = false;

			if (target.isPlayer()) {
				auto_ret = target.getAsPlayer().autoRetaliate() && !target.getMovementQueue().isMoving();
			} else if (target.isNpc()) {
				auto_ret = target.getAsNpc().getMovementCoordinator().getCoordinateState() == CoordinateState.HOME;
			}

			if (!auto_ret) {
				return;
			}

			// Start a task, don't autoretaliate immediately
			TaskManager.submit(new Task(1, target, false) {
				@Override
				protected void execute() {
					target.getCombat().attack(attacker);
					stop();
				}
			});
		}
	}

	/**
	 * Freezes a character.
	 *
	 * @param character
	 * @param ticks The number of ticks to freeze.
	 */
	public static void freeze(Mobile character, int ticks) {
		if (character.getTimers().has(TimerKey.FREEZE) || character.getTimers().has(TimerKey.FREEZE_IMMUNITY)) {
			return;
		}

		// Add check for npc: Only small npcs should be freeze-able
		if (character.size() > 2) {
			return;
		}

		character.getTimers().register(TimerKey.FREEZE, ticks);
		character.getTimers().register(TimerKey.FREEZE_IMMUNITY, ticks + 5);
		character.getMovementQueue().reset();

		if (character.isPlayer()) {

			// Send message and effect timer to client
			character.getAsPlayer().getPacketSender().sendMessage("You have been frozen!").sendEffectTimer(Misc.getSeconds(ticks),
					EffectTimer.FREEZE);
		}
	}

	/**
	 * Handles the redemption prayer effect
	 *
	 * @param attacker
	 * @param victim
	 */
	private static void handleRedemption(Mobile attacker, Player victim, int damage) {
		if ((victim.getHitpoints() - damage) <= (victim.getSkillManager().getMaxLevel(Skill.HITPOINTS) / 10)) {
			int amountToHeal = (int) (victim.getSkillManager().getMaxLevel(Skill.PRAYER) * .25);
			victim.performGraphic(new Graphic(436));
			victim.getSkillManager().setCurrentLevel(Skill.PRAYER, 0);
			victim.getSkillManager().setCurrentLevel(Skill.HITPOINTS, victim.getHitpoints() + amountToHeal);
			victim.getPacketSender().sendMessage("You've run out of prayer points!");
			PrayerHandler.deactivatePrayers(victim);
		}
	}

	/**
	 * Handles the smite prayer effect
	 *
	 * @param attacker
	 * @param victim
	 * @param damage
	 */
	private static void handleSmite(Mobile attacker, Player victim, int damage) {
		victim.getSkillManager().decreaseCurrentLevel(Skill.PRAYER, (damage / 4), 0);
	}

	/**
	 * Handles the retribution prayer effect
	 *
	 * @param killed
	 * @param killer
	 */
	public static void handleRetribution(Player killed, Player killer) {
		killed.performGraphic(new Graphic(437));
		if (killer.getLocation().isWithinDistance(killer.getLocation(), CombatConstants.RETRIBUTION_RADIUS)) {
			killer.getCombat().getHitQueue().addPendingDamage(
					new HitDamage(Misc.getRandom(CombatConstants.MAXIMUM_RETRIBUTION_DAMAGE), HitMask.RED));
		}
	}

	/**
	 * Checks if a player has enough ammo to perform a ranged attack
	 *
	 * @param player
	 *            The player to run the check for
	 * @return True if player has ammo, false otherwise
	 */
	public static boolean checkAmmo(Player player, int amountRequired) {
		// Get the ranged weapon data
		final RangedWeapon rangedWeapon = player.getCombat().getRangedWeapon();

		// Get the ranged ammo data
		final Ammunition ammoData = player.getCombat().getAmmunition();

		if (rangedWeapon == null) {
			player.getCombat().reset();
			return false;
		}

		if (rangedWeapon == RangedWeapon.TOXIC_BLOWPIPE) {
			if (player.getBlowpipeScales() <= 0) {
				player.getPacketSender().sendMessage("You must recharge your Toxic blowpipe using some Zulrah scales.");
				player.getCombat().reset();
				return false;
			}
			return true;
		}

		if (ammoData == null) {
			player.getPacketSender().sendMessage("You don't have any ammunition to fire.");
			player.getCombat().reset();
			return false;
		}

		if (rangedWeapon.getType() == RangedWeaponType.KNIFE || rangedWeapon.getType() == RangedWeaponType.DART
				|| rangedWeapon.getType() == RangedWeaponType.TOKTZ_XIL_UL) {
			return true;
		}

		Item ammoSlotItem = player.getEquipment().getItems()[Equipment.AMMUNITION_SLOT];
		if (ammoSlotItem.getId() == -1 || ammoSlotItem.getAmount() < amountRequired) {
			player.getPacketSender().sendMessage("You don't have the required amount of ammunition to fire that.");
			player.getCombat().reset();
			return false;
		}

		boolean properReq = false;

		// BAD LOOP
		for (Ammunition d : rangedWeapon.getAmmunitionData()) {
			if (d == ammoData) {
				if (d.getItemId() == ammoSlotItem.getId()) {
					properReq = true;
					break;
				}
			}
		}

		if (!properReq) {
			String ammoName = ammoSlotItem.getDefinition().getName(),
					weaponName = player.getEquipment().getItems()[Equipment.WEAPON_SLOT].getDefinition().getName(),
					add = !ammoName.endsWith("s") && !ammoName.endsWith("(e)") ? "s" : "";
			player.getPacketSender().sendMessage("You can not use " + ammoName + "" + add + " with "
					+ Misc.anOrA(weaponName) + " " + weaponName + ".");
			player.getCombat().reset();
			return false;
		}

		return true;
	}

	/**
	 * Decrements the amount ammo the {@link Player} currently has equipped.
	 *
	 * @param player
	 *            the player to decrement ammo for.
	 */
	public static void decrementAmmo(Player player, Location pos, int amount) {

		// Get the ranged weapon data
		final RangedWeapon rangedWeapon = player.getCombat().getRangedWeapon();

		// Determine which slot we are decrementing ammo from.
		int slot = Equipment.AMMUNITION_SLOT;

		// Is the weapon using a throw weapon?
		// The ammo should be dropped from the weapon slot.
		if (rangedWeapon.getType() == RangedWeaponType.KNIFE || rangedWeapon.getType() == RangedWeaponType.DART
				|| rangedWeapon.getType() == RangedWeaponType.TOKTZ_XIL_UL) {
			slot = Equipment.WEAPON_SLOT;
		}

		boolean accumalator = player.getEquipment().get(Equipment.CAPE_SLOT).getId() == 10499;
		if (accumalator) {
			if (Misc.getRandom(12) <= 9) {
				return;
			}
		}

		if (rangedWeapon == RangedWeapon.TOXIC_BLOWPIPE) {
			if (player.decrementAndGetBlowpipeScales() <= 0) {
				player.getPacketSender().sendMessage("Your Toxic blowpipe has run out of scales!");
				player.getCombat().reset();
			}
			return;
		}

		// Decrement the ammo in the selected slot.
		player.getEquipment().get(slot).decrementAmountBy(amount);

		// Drop arrows if the player isn't using an accumalator
		if (player.getCombat().getAmmunition().dropOnFloor()) {
			if (!accumalator) {
				/*
				 * for(int i = 0; i < amount; i++) { GroundItemManager.spawnGroundItem(player,
				 * new GroundItem(new Item(player.getEquipment().get(slot).getId()), pos,
				 * player.getUsername(), false, 120, true, 120)); }
				 */
			}
		}

		// If we are at 0 ammo remove the item from the equipment completely.
		if (player.getEquipment().get(slot).getAmount() == 0) {
			player.getPacketSender().sendMessage("You have run out of ammunition!");
			player.getEquipment().set(slot, new Item(-1));

			if (slot == Equipment.WEAPON_SLOT) {
				WeaponInterfaces.assign(player);
				player.getUpdateFlag().flag(Flag.APPEARANCE);
			}
		}

		// Refresh the equipment interface.
		player.getEquipment().refreshItems();
	}

	/**
	 * Determines if the entity is wearing full veracs.
	 *
	 * @param entity
	 *            the entity to determine this for.
	 * @return true if the player is wearing full veracs.
	 */
	public static boolean fullVeracs(Mobile entity) {
		return entity.isNpc() ? entity.getAsNpc().getId() == NpcIdentifiers.VERAC_THE_DEFILED
				: entity.getAsPlayer().getEquipment().containsAll(4753, 4757, 4759, 4755);
	}

	/**
	 * Determines if the entity is wearing full dharoks.
	 *
	 * @param entity
	 *            the entity to determine this for.
	 * @return true if the player is wearing full dharoks.
	 */
	public static boolean fullDharoks(Mobile entity) {
		return entity.isNpc() ? entity.getAsNpc().getId() == NpcIdentifiers.DHAROK_THE_WRETCHED
				: entity.getAsPlayer().getEquipment().containsAll(4716, 4720, 4722, 4718);
	}

	/**
	 * Determines if the entity is wearing full karils.
	 *
	 * @param entity
	 *            the entity to determine this for.
	 * @return true if the player is wearing full karils.
	 */
	public static boolean fullKarils(Mobile entity) {
		return entity.isNpc() ? entity.getAsNpc().getId() == NpcIdentifiers.KARIL_THE_TAINTED
				: entity.getAsPlayer().getEquipment().containsAll(4732, 4736, 4738, 4734);
	}

	/**
	 * Determines if the entity is wearing full ahrims.
	 *
	 * @param entity
	 *            the entity to determine this for.
	 * @return true if the player is wearing full ahrims.
	 */
	public static boolean fullAhrims(Mobile entity) {
		return entity.isNpc() ? entity.getAsNpc().getId() == NpcIdentifiers.AHRIM_THE_BLIGHTED
				: entity.getAsPlayer().getEquipment().containsAll(4708, 4712, 4714, 4710);
	}

	/**
	 * Determines if the entity is wearing full torags.
	 *
	 * @param entity
	 *            the entity to determine this for.
	 * @return true if the player is wearing full torags.
	 */
	public static boolean fullTorags(Mobile entity) {
		return entity.isNpc() ? entity.getAsNpc().getDefinition().getName().equals("Torag the Corrupted")
				: entity.getAsPlayer().getEquipment().containsAll(4745, 4749, 4751, 4747);
	}

	/**
	 * Determines if the entity is wearing full guthans.
	 *
	 * @param entity
	 *            the entity to determine this for.
	 * @return true if the player is wearing full guthans.
	 */
	public static boolean fullGuthans(Mobile entity) {
		return entity.isNpc() ? entity.getAsNpc().getDefinition().getName().equals("Guthan the Infested")
				: entity.getAsPlayer().getEquipment().containsAll(4724, 4728, 4730, 4726);
	}

	/**
	 * Calculates the combat level difference for wilderness player vs. player
	 * combat.
	 *
	 * @param combatLevel
	 *            the combat level of the first person.
	 * @param otherCombatLevel
	 *            the combat level of the other person.
	 * @return the combat level difference.
	 */
	public static int combatLevelDifference(int combatLevel, int otherCombatLevel) {
		if (combatLevel > otherCombatLevel) {
			return (combatLevel - otherCombatLevel);
		} else if (otherCombatLevel > combatLevel) {
			return (otherCombatLevel - combatLevel);
		} else {
			return 0;
		}
	}
}