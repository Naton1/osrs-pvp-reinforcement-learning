package com.elvarg.game.content.combat.hit;

import com.elvarg.game.content.combat.CombatFactory;
import com.elvarg.game.content.combat.CombatType;
import com.elvarg.game.content.combat.formula.AccuracyFormulasDpsCalc;
import com.elvarg.game.content.combat.method.CombatMethod;
import com.elvarg.game.entity.impl.Mobile;
import com.elvarg.game.entity.impl.player.Player;

public class PendingHit {

    private final Mobile attacker;
    private final Mobile target;
    private final CombatMethod method;
    private final CombatType combatType;
    private HitDamage[] hits;
    private int totalDamage;
    private int delay;
    private boolean accurate;
    private boolean handleAfterHitEffects;

    public PendingHit(Mobile attacker, Mobile target, CombatMethod method) {
        this(attacker, target, method, true, 0);
    }
    
    public PendingHit(Mobile attacker, Mobile target, CombatMethod method, int delay) {
        this(attacker, target, method, true, delay);
    }
    
    public PendingHit(Mobile attacker, Mobile target, CombatMethod method, boolean rollAccuracy, int delay) {
    	this(attacker, target, method, rollAccuracy, 1, delay);
    }
    
    public PendingHit(Mobile attacker, Mobile target, CombatMethod method, boolean rollAccuracy, int hitAmount, int delay) {
        this.attacker = attacker;
        this.target = target;
        this.method = method;
        this.combatType = method.type();
        this.hits = prepareHits(hitAmount, rollAccuracy);
        this.delay = delay;
        this.handleAfterHitEffects = true;
    }

    private PendingHit(CombatMethod method, Mobile attacker, Mobile target) {
        this.attacker = attacker;
        this.target = target;
        this.method = method;
        this.combatType = method.type();   	
    }
    
    /**
     * New method added to allow the adding of pending damage of a pre-determined value at a specific delay.
     * @param attacker Attacking mobile entity.
     * @param target Recipient mobile entity of the damage.
     * @param method The combat method used.
     * @param damage The calculated damage amount.
     * @param delay 
     */
    public static PendingHit create(Mobile attacker, Mobile target, CombatMethod method, int damage, boolean accurate) {
    	PendingHit hit = new PendingHit(method, attacker, target);
    	
    	hit.hits = new HitDamage[] {new HitDamage(damage, damage == 0 ? HitMask.BLUE : HitMask.RED)};
        hit.delay = 0; //The delay before the hit (0 in most cases with current combat system design).
        hit.handleAfterHitEffects = true;
        hit.accurate = true;
    	hit.totalDamage += damage;
    	return hit;
    }
        
    public Mobile getAttacker() {
        return attacker;
    }

    public Mobile getTarget() {
        return target;
    }

    public CombatMethod getCombatMethod() {
        return method;
    }

    public HitDamage[] getHits() {
        return hits;
    }

    public int getAndDecrementDelay() {
        return delay--;
    }

    public int getExecutedInTicks() {
        return delay;
    }

    public int getTotalDamage() {
        return totalDamage;
    }

    public boolean isAccurate() {
        return accurate;
    }

    public void setTotalDamage(int damage) {
        for (HitDamage hit : hits) {
            hit.setDamage(damage);
        }
        updateTotalDamage();
    }

    public PendingHit setHandleAfterHitEffects(boolean handleAfterHitEffects) {
        this.handleAfterHitEffects = handleAfterHitEffects;
        return this;
    }

    public boolean handleAfterHitEffects() {
        return handleAfterHitEffects;
    }

    private HitDamage[] prepareHits(int hitAmount, boolean rollAccuracy) {
        // Check the hit amounts.
        if (hitAmount > 4) {
            throw new IllegalArgumentException(
                    "Illegal number of hits! The maximum number of hits per turn is 4.");
        } else if (hitAmount < 0) {
            throw new IllegalArgumentException(
                    "Illegal number of hits! The minimum number of hits per turn is 0.");
        }

        if (attacker == null || target == null) {
            return null;
        }

        HitDamage[] hits = new HitDamage[hitAmount];
        for (int i = 0; i < hits.length; i++) {
            accurate = !rollAccuracy || AccuracyFormulasDpsCalc.rollAccuracy(attacker, target, combatType);
            HitDamage damage = accurate ? CombatFactory.getHitDamage(attacker, target, combatType) : new HitDamage(0, HitMask.BLUE);
            totalDamage += damage.getDamage();
            hits[i] = damage;
        }
        return hits;
    }

    public void updateTotalDamage() {
        totalDamage = 0;
        for (int i = 0; i < hits.length; i++) {
            totalDamage += hits[i].getDamage();
        }
    }

    public int[] getSkills() {
        if (attacker.isNpc()) {
            return new int[]{};
        }
        return ((Player) attacker).getFightType().getStyle().skill(combatType);
    }

	public CombatType getCombatType() {
		return combatType;
	}
}
