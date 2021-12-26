package com.elvarg.game.entity.impl.npc.impl;

import java.util.ArrayList;
import java.util.List;

import com.elvarg.game.World;
import com.elvarg.game.content.combat.hit.PendingHit;
import com.elvarg.game.entity.impl.npc.NPC;
import com.elvarg.game.model.Location;
import com.elvarg.util.NpcIdentifiers;
import com.elvarg.game.entity.impl.Mobile;

public class Vetion extends NPC {
	
	private boolean spawnedHellhounds;
	private int rebornTimer = 0;
	private List<VetionHellhound> hellhounds;
	
	public Vetion(int id, Location position) {
		super(id, position);
		hellhounds = new ArrayList<>();
		setNpcTransformationId(NpcIdentifiers.VETION);
	}
	
	@Override
	public void process() {
		super.process();
		
		Mobile target = getCombat().getTarget();
		if (target != null && getHitpoints() <= 125) {
			if (!spawnedHellhounds) {
				spawnHellhounds(target);
				spawnedHellhounds = true;
			}
		}
		
		if (getNpcTransformationId() == NpcIdentifiers.VETION_REBORN) {
			if (rebornTimer == 500) {
				spawnedHellhounds = true;
				setNpcTransformationId(NpcIdentifiers.VETION);
				rebornTimer = 0;
			}
			rebornTimer++;
		}
	}
	
	private void spawnHellhounds(Mobile target) {
		for (int i = 0; i < 2; i++) {
			int hellhoundId = NpcIdentifiers.VETION_HELLHOUND;
			if (getNpcTransformationId() == NpcIdentifiers.VETION_REBORN) {
				hellhoundId = NpcIdentifiers.GREATER_VETION_HELLHOUND;
			}
			VetionHellhound hellhound = (VetionHellhound) NPC.create(hellhoundId, getLocation());
			hellhound.setVetion(this);
			hellhounds.add(hellhound);
			World.getAddNPCQueue().add(hellhound);
		}
	}
	
	public void despawnHellhound(VetionHellhound hellhound) {
		hellhounds.remove(hellhound);
	}
	
	@Override
	public void appendDeath() {
		for (VetionHellhound npc : hellhounds) {
        	World.getRemoveNPCQueue().add(npc);
        }
        hellhounds.clear();
        spawnedHellhounds = false;
        
        if (getNpcTransformationId() != NpcIdentifiers.VETION_REBORN) {
        	setHitpoints(getDefinition().getHitpoints());
        	setNpcTransformationId(NpcIdentifiers.VETION_REBORN);
        	forceChat("Do it again!");
        	return;
        }
        
        super.appendDeath();
	}
	
	@Override
	public PendingHit manipulateHit(PendingHit hit) {
		if (spawnedHellhounds && hellhounds.size() > 0) {
			hit.setTotalDamage(0);
		}
		return hit;
	}
}
