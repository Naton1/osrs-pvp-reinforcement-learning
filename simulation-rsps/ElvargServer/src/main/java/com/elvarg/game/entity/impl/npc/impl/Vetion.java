package com.elvarg.game.entity.impl.npc.impl;

import java.util.ArrayList;
import java.util.List;

import com.elvarg.game.World;
import com.elvarg.game.content.combat.hit.PendingHit;
import com.elvarg.game.content.combat.method.CombatMethod;
import com.elvarg.game.content.combat.method.impl.npcs.ChaosFanaticCombatMethod;
import com.elvarg.game.content.combat.method.impl.npcs.VetionCombatMethod;
import com.elvarg.game.entity.impl.npc.NPC;
import com.elvarg.game.model.Ids;
import com.elvarg.game.model.Location;
import com.elvarg.game.entity.impl.Mobile;

import static com.elvarg.util.NpcIdentifiers.*;

@Ids({VETION, VETION_REBORN})
public class Vetion extends NPC {

	private static final CombatMethod COMBAT_METHOD = new VetionCombatMethod();

	private boolean spawnedHellhounds;
	private int rebornTimer = 0;
	private List<VetionHellhound> hellhounds;

	/**
	 * Constructs a new npc.
	 *
	 * @param id       The npc id.
	 * @param position
	 */
	public Vetion(int id, Location position) {
		super(id, position);
		hellhounds = new ArrayList<>();
		setNpcTransformationId(VETION);
	}

	@Override
	public CombatMethod getCombatMethod() {
		return COMBAT_METHOD;
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
		
		if (getNpcTransformationId() == VETION_REBORN) {
			if (rebornTimer == 500) {
				spawnedHellhounds = true;
				setNpcTransformationId(VETION);
				rebornTimer = 0;
			}
			rebornTimer++;
		}
	}
	
	private void spawnHellhounds(Mobile target) {
		for (int i = 0; i < 2; i++) {
			int hellhoundId = VETION_HELLHOUND;
			if (getNpcTransformationId() == VETION_REBORN) {
				hellhoundId = GREATER_VETION_HELLHOUND;
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
        
        if (getNpcTransformationId() != VETION_REBORN) {
        	setHitpoints(getDefinition().getHitpoints());
        	setNpcTransformationId(VETION_REBORN);
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
