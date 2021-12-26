package com.elvarg.game.content.combat;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import com.elvarg.game.content.combat.hit.HitDamageCache;
import com.elvarg.game.content.combat.hit.HitQueue;
import com.elvarg.game.content.combat.hit.PendingHit;
import com.elvarg.game.content.combat.magic.CombatSpell;
import com.elvarg.game.content.combat.method.CombatMethod;
import com.elvarg.game.content.combat.method.impl.specials.GraniteMaulCombatMethod;
import com.elvarg.game.content.combat.ranged.RangedData.Ammunition;
import com.elvarg.game.content.combat.ranged.RangedData.RangedWeapon;
import com.elvarg.game.entity.impl.Mobile;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.SecondsTimer;
import com.elvarg.util.Stopwatch;
import com.elvarg.util.timers.TimerKey;

public class Combat {
    
    private final Mobile character;
    private final HitQueue hitQueue;
	private final Map<Player, HitDamageCache> damageMap = new HashMap<>();
	private final Stopwatch lastAttack = new Stopwatch();
	private final SecondsTimer poisonImmunityTimer = new SecondsTimer();
	private final SecondsTimer fireImmunityTimer = new SecondsTimer();
	private final SecondsTimer teleblockTimer = new SecondsTimer();
	private final SecondsTimer prayerBlockTimer = new SecondsTimer();
	public RangedWeapon rangedWeapon;
	public Ammunition rangeAmmoData;
	private Mobile target;
	private Mobile attacker;
	private CombatMethod method;
	private CombatSpell castSpell;
	private CombatSpell autoCastSpell;
	private CombatSpell previousCast;

	public Combat(Mobile character) {
		this.character = character;
		this.hitQueue = new HitQueue();
	}

	/**
	 * Attacks an entity by updating our current target.
	 *
	 * @param target
	 *            The target to attack.
	 */
	public void attack(Mobile target) {
		// Update the target
		setTarget(target);

		// Start facing the target
		character.setMobileInteraction(target);

		// Start following the target
		character.setFollowing(target);
	}

	/**
	 * Processes combat.
	 */
	public void process() {
		// Process the hit queue
		hitQueue.process(character);

		// Handle attacking
		performNewAttack(false);

		// Reset attacker if we haven't been attacked in 6 seconds.
		if (lastAttack.elapsed(6000)) {
			setUnderAttack(null);
		}
	}

	/**
	 * Attempts to perform a new attack.
	 */
	public void performNewAttack(boolean instant) {
		if (target != null) {

			// Fetch the combat method the character will be attacking with
			method = CombatFactory.getMethod(character);

			// Follow target
			character.setFollowing(target);

			// Check if the character can reach the target before attempting attack
			if (CombatFactory.canReach(character, method, target)) {

				// Granite maul special attack, make sure we disregard delay
				// and that we do not reset the attack timer.
				boolean graniteMaulSpecial = (method instanceof GraniteMaulCombatMethod);
				if (graniteMaulSpecial) {
					instant = true;
				}

				// Make sure attack timer is <= 0
				if (!character.getTimers().has(TimerKey.COMBAT_ATTACK) || instant) {

					// Check if the character can perform the attack
					if (CombatFactory.canAttack(character, method, target)) {

						// Face target
						character.setMobileInteraction(target);

						method.start(character, target);
						PendingHit[] hits = method.hits(character, target);
						if (hits == null)
							return;
						for (PendingHit hit : hits) {
							CombatFactory.addPendingHit(hit);
						}
						method.finished(character, target);

						// Reset attack timer
						if (!graniteMaulSpecial) {
						    int speed = method.attackSpeed(character);
							character.getTimers().register(TimerKey.COMBAT_ATTACK, speed);
						}
						instant = false;
					}
				}
			}
		}
	}

	/**
	 * Resets combat for the {@link Mobile}.
	 */
	public void reset() {
		target = null;
		character.setFollowing(null);
		character.setMobileInteraction(null);
	}

	/**
	 * Adds damage to the damage map, as long as the argued amount of damage is
	 * above 0 and the argued entity is a player.
	 *
	 * @param entity
	 *            the entity to add damage for.
	 * @param amount
	 *            the amount of damage to add for the argued entity.
	 */
	public void addDamage(Mobile entity, int amount) {

		if (amount <= 0 || entity.isNpc()) {
			return;
		}

		Player player = (Player) entity;
		if (damageMap.containsKey(player)) {
			damageMap.get(player).incrementDamage(amount);
			return;
		}

		damageMap.put(player, new HitDamageCache(amount));
	}

	/**
	 * Performs a search on the <code>damageMap</code> to find which {@link Player}
	 * dealt the most damage on this controller.
	 *
	 * @param clearMap
	 *            <code>true</code> if the map should be discarded once the killer
	 *            is found, <code>false</code> if no data in the map should be
	 *            modified.
	 * @return the player who killed this entity, or <code>null</code> if an npc or
	 *         something else killed this entity.
	 */
	public Optional<Player> getKiller(boolean clearMap) {

		// Return null if no players killed this entity.
		if (damageMap.size() == 0) {
			return Optional.empty();
		}

		// The damage and killer placeholders.
		int damage = 0;
		Optional<Player> killer = Optional.empty();

		for (Entry<Player, HitDamageCache> entry : damageMap.entrySet()) {

			// Check if this entry is valid.
			if (entry == null) {
				continue;
			}

			// Check if the cached time is valid.
			long timeout = entry.getValue().getStopwatch().elapsed();
			if (timeout > CombatConstants.DAMAGE_CACHE_TIMEOUT) {
				continue;
			}

			// Check if the key for this entry has logged out.
			Player player = entry.getKey();
			if (!player.isRegistered()) {
				continue;
			}

			// If their damage is above the placeholder value, they become the
			// new 'placeholder'.
			if (entry.getValue().getDamage() > damage) {
				damage = entry.getValue().getDamage();
				killer = Optional.of(entry.getKey());
			}
		}

		// Clear the damage map if needed.
		if (clearMap)
			damageMap.clear();

		// Return the killer placeholder.
		return killer;
	}

	public boolean damageMapContains(Player player) {
		return damageMap.containsKey(player);
	}

	/**
	 * Getters and setters
	 **/

	public Mobile getCharacter() {
		return character;
	}

	public Mobile getTarget() {
		return target;
	}

	public void setTarget(Mobile target) {
		this.target = target;
	}

	public HitQueue getHitQueue() {
		return hitQueue;
	}

	public Mobile getAttacker() {
		return attacker;
	}

	public void setUnderAttack(Mobile attacker) {
		this.attacker = attacker;
		this.lastAttack.reset();
	}

	public CombatSpell getCastSpell() {
		return castSpell;
	}

	public void setCastSpell(CombatSpell castSpell) {
		this.castSpell = castSpell;
	}

	public CombatSpell getAutocastSpell() {
		return autoCastSpell;
	}

	public void setAutocastSpell(CombatSpell autoCastSpell) {
		this.autoCastSpell = autoCastSpell;
	}

	public CombatSpell getPreviousCast() {
		return previousCast;
	}

	public void setPreviousCast(CombatSpell previousCast) {
		this.previousCast = previousCast;
	}

	public RangedWeapon getRangedWeapon() {
		return rangedWeapon;
	}

	public void setRangedWeapon(RangedWeapon rangedWeapon) {
		this.rangedWeapon = rangedWeapon;
	}

	public Ammunition getAmmunition() {
		return rangeAmmoData;
	}

	public void setAmmunition(Ammunition rangeAmmoData) {
		this.rangeAmmoData = rangeAmmoData;
	}

	public SecondsTimer getPoisonImmunityTimer() {
		return poisonImmunityTimer;
	}

	public SecondsTimer getFireImmunityTimer() {
		return fireImmunityTimer;
	}

	public SecondsTimer getTeleBlockTimer() {
		return teleblockTimer;
	}

	public SecondsTimer getPrayerBlockTimer() {
		return prayerBlockTimer;
	}

	public Stopwatch getLastAttack() {
		return lastAttack;
	}
}
