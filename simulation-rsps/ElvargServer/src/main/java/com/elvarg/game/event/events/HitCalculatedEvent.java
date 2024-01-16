package com.elvarg.game.event.events;

import com.elvarg.game.content.combat.hit.PendingHit;
import com.elvarg.game.event.Event;

public class HitCalculatedEvent implements Event {

	private final PendingHit pendingHit;

	public HitCalculatedEvent(PendingHit pendingHit) {
		this.pendingHit = pendingHit;
	}

	public PendingHit getPendingHit() {
		return pendingHit;
	}

}
