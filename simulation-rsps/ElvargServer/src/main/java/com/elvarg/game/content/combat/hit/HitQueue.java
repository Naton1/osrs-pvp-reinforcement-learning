package com.elvarg.game.content.combat.hit;

import com.elvarg.game.content.combat.CombatFactory;
import com.elvarg.game.entity.impl.Mobile;
import com.elvarg.game.event.EventDispatcher;
import com.elvarg.game.event.events.HitAppliedEvent;
import com.elvarg.game.event.events.HitCalculatedEvent;
import com.elvarg.game.model.Flag;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Represents a "hitqueue", processing pending hits aswell as pending damage.
 *
 * @author Professor Oak
 */
public class HitQueue {

	// Our list containing all our incoming hits waiting to be processed.
	private List<PendingHit> pendingHits = new ArrayList<PendingHit>();

	// Our queue of current damage waiting to be dealt.
	private Queue<HitDamage> pendingDamage = new ConcurrentLinkedQueue<HitDamage>();

	public void process(Mobile character) {

		// If we are dead, clear all pending and current hits.
		if (character.getHitpoints() <= 0) {
			pendingHits.clear();
			pendingDamage.clear();
			return;
		}

		// Process the pending hits..
		Iterator<PendingHit> iterator = pendingHits.iterator();
		while (iterator.hasNext()) {
			PendingHit hit = iterator.next();
			// Make sure we only process the hit if it should be processed.
			// For example - if attacker died or target is untargetable, don't process.
			if (hit == null || hit.getTarget() == null || hit.getAttacker() == null || hit.getTarget().isUntargetable()
					|| hit.getAttacker().getHitpoints() <= 0) {
				iterator.remove();
				continue;
			}

			if (hit.getAndDecrementDelay() <= 0) {
				CombatFactory.executeHit(hit);
				iterator.remove();
			}
		}

		// Process damage.
		// Make sure our hits queue isn't empty and that we aren't dead...
		if (!pendingDamage.isEmpty()) {

			// Update the single hit for this entity.
			if (!character.getUpdateFlag().flagged(Flag.SINGLE_HIT)) {

				// Attempt to fetch a first hit.
				HitDamage firstHit = pendingDamage.poll();

				// Check if it's present
				if (!Objects.isNull(firstHit)) {

					// Update entity hit data and deal the actual damage.
					character.setPrimaryHit(character.decrementHealth(firstHit));
					character.getUpdateFlag().flag(Flag.SINGLE_HIT);
				}
			}

			// Update the secondary hit for this entity.
			if (!character.getUpdateFlag().flagged(Flag.DOUBLE_HIT)) {

				// Attempt to fetch a second hit.
				HitDamage secondHit = pendingDamage.poll();

				// Check if it's present
				if (!Objects.isNull(secondHit)) {

					// Update entity hit data and deal the actual damage.
					character.setSecondaryHit(character.decrementHealth(secondHit));
					character.getUpdateFlag().flag(Flag.DOUBLE_HIT);
				}
			}
		}
	}

	/**
	 * Add a pending hit to our queue.
	 *
	 * @param c_h
	 */
	public void addPendingHit(PendingHit c_h) {
		pendingHits.add(c_h);
		EventDispatcher.getGlobal().dispatch(new HitCalculatedEvent(c_h));
	}

	/**
	 * Add pending damage to our queue.
	 *
	 * @param hits
	 */
	public void addPendingDamage(HitDamage... hits) {
		Arrays.stream(hits).filter(h -> !Objects.isNull(h)).forEach(h -> pendingDamage.add(h));
		for (HitDamage hit : hits) {
			if (hit == null || hit.getMetadata() == null) {
				continue;
			}
			EventDispatcher.getGlobal().dispatch(new HitAppliedEvent(hit));
		}
	}

	public int getAccumulatedDamage() {
		var hitDmg = this.pendingHits.stream().filter(pd -> pd.getExecutedInTicks() < 2).mapToInt(PendingHit::getTotalDamage).sum();
		var dmg = this.pendingDamage.stream().mapToInt(HitDamage::getDamage).sum();

		return hitDmg + dmg;
	}

	public int getAllAccumulatedDamage() {
		var hitDmg = this.pendingHits.stream().mapToInt(PendingHit::getTotalDamage).sum();
		var dmg = this.pendingDamage.stream().mapToInt(HitDamage::getDamage).sum();

		return hitDmg + dmg;
	}

	public int getTicksUntilNextHit() {
		// -1 if no hits pending, 0 if processed next combat process tick, and so on
		if (!this.pendingDamage.isEmpty()) {
			return 0;
		}
		return this.pendingHits.stream()
				.mapToInt(PendingHit::getExecutedInTicks)
				.findFirst()
				.orElse(-1);
	}

	/***
	 * Checks if the pending hit queue is empty, except from the specified
	 * {@link Mobile}. Used for anti-pjing.
	 *
	 * @param exception
	 * @return
	 */
	public boolean isEmpty(Mobile exception) {
		for (PendingHit hit : pendingHits) {
			if (hit == null) {
				continue;
			}
			if (hit.getAttacker() != null) {
				if (!hit.getAttacker().equals(exception)) {
					return false;
				}
			}
		}
		return true;
	}
}
