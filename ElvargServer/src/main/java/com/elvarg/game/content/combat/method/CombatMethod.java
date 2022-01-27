package com.elvarg.game.content.combat.method;

import com.elvarg.game.content.combat.CombatType;
import com.elvarg.game.content.combat.hit.PendingHit;
import com.elvarg.game.entity.impl.Mobile;

/**
 * Represents a combat method.
 */
public abstract class CombatMethod {
    
    public void start(Mobile character, Mobile target) {
    }
    
    public void finished(Mobile character, Mobile target) {
    }
    
    public void handleAfterHitEffects(PendingHit hit) {
    }
    
    public boolean canAttack(Mobile character, Mobile target) {
        return true;
    }
    
    public int attackSpeed(Mobile character) {
        return character.getBaseAttackSpeed();
    }
    
    public int attackDistance(Mobile character) {
        return 1;
    }

    public abstract CombatType type();
    public abstract PendingHit[] hits(Mobile character, Mobile target);
}