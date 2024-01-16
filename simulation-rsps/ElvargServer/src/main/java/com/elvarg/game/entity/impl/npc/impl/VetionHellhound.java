package com.elvarg.game.entity.impl.npc.impl;

import com.elvarg.game.entity.impl.npc.NPC;
import com.elvarg.game.model.Location;
import com.elvarg.game.entity.impl.Mobile;

public class VetionHellhound extends NPC {

	private Vetion vetion;
	private int timer = 0;
	
	public VetionHellhound(int id, Location position) {
		super(id, position);
	}

	@Override
	public void process() {
		super.process();

		if (vetion != null) {
			Mobile target = vetion.getCombat().getTarget();
			if (target == null) {
				target = vetion.getCombat().getAttacker();
			}

			if (target != null) {
				if (getCombat().getTarget() != target) {
					getCombat().attack(target);
				}
				return;
			}
		}
		
		if (timer == 500) {
			appendDeath();
		}
		timer++;
		
	}

	@Override
	public void appendDeath() {
		super.appendDeath();
		if (vetion != null) {
			vetion.despawnHellhound(this);
		}
	}

	public Vetion getVetion() {
		return vetion;
	}

	public void setVetion(Vetion vetion) {
		this.vetion = vetion;
	}
}
