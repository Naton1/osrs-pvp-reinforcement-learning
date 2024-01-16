package com.elvarg.game.content.combat.hit;

import com.elvarg.game.entity.impl.Mobile;
import lombok.Builder;
import lombok.Value;

/**
 * A hit done by an entity onto a target.
 *
 * @author Gabriel Hannason
 */
public class HitDamage {

	private int damage;
	private HitMask hitmask;
    private HitMask startHitmask;

	private Metadata metadata;

	public HitDamage(int damage, HitMask hitmask) {
		this.damage = damage;
		this.hitmask = hitmask;
		this.startHitmask = hitmask;
		update();
	}

	public HitDamage withMetadata(Metadata metadata) {
		this.metadata = metadata;
		return this;
	}

	public Metadata getMetadata() {
		return this.metadata;
	}

	public int getDamage() {
		return damage;
	}

	public void setDamage(int damage) {
		this.damage = damage;
		update();
	}

	public void incrementDamage(int damage) {
		this.damage += damage;
		update();
	}

	public void multiplyDamage(double mod) {
		this.damage *= mod;
		update();
	}

	public void update() {
	    if (damage > 0) {
            hitmask = startHitmask == HitMask.BLUE ? HitMask.RED : startHitmask;
        } else {
            damage = 0;
            hitmask = HitMask.BLUE;
        }
	}

	public HitMask getHitmask() {
		return hitmask;
	}

	public void setHitmask(HitMask hitmask) {
		this.hitmask = hitmask;
	}

	@Builder
	@Value
	public static class Metadata {
		private final Mobile attacker;
		private final Mobile target;
		private final PendingHit associatedPendingHit;
	}
}
