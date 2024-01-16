package com.elvarg.game.entity.impl.npc.impl;

import com.elvarg.game.entity.impl.npc.NPC;
import com.elvarg.game.model.God;
import com.elvarg.game.model.Location;

public class GodwarsFollower extends NPC {
	
	private final God god;

	public GodwarsFollower(int id, Location position, God god) {
		super(id, position);
		this.god = god;
	}

	public God getGod() {
		return god;
	}
}
