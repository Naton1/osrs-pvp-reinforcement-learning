package com.elvarg.game.event.events;

import com.elvarg.game.content.combat.hit.HitDamage;
import com.elvarg.game.event.Event;

public class HitAppliedEvent implements Event {

	private final HitDamage hitDamage;

	public HitAppliedEvent(HitDamage hitDamage) {
		this.hitDamage = hitDamage;
	}

	public HitDamage getHitDamage() {
		return hitDamage;
	}

}
