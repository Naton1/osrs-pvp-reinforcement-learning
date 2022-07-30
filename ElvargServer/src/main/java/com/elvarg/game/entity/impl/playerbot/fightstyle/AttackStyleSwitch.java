package com.elvarg.game.entity.impl.playerbot.fightstyle;

import com.elvarg.game.content.combat.CombatType;

public class AttackStyleSwitch {
    private final CombatType combatType;
    private final CombatSwitch combatSwitch;
    private int maxHit;
    private int attackRoll;
    private int hitSpeed;

    public AttackStyleSwitch(CombatType combatType, CombatSwitch combatSwitch) {
        this.combatType = combatType;
        this.combatSwitch = combatSwitch;
        this.attackRoll = 9999999;
        this.maxHit = 120;
        this.hitSpeed = 4;
    }

    public CombatType getCombatType() {
        return combatType;
    }

    public CombatSwitch getCombatSwitch() {
        return combatSwitch;
    }

    public int getMaxHit() {
        return maxHit;
    }

    public void setMaxHit(int maxHit) {
        this.maxHit = maxHit;
    }

    public int getAttackRoll() {
        return attackRoll;
    }

    public void setAttackRoll(int attackRoll) {
        this.attackRoll = attackRoll;
    }

    public int getHitSpeed() {
        return hitSpeed;
    }

    public void setHitSpeed(int hitSpeed) {
        this.hitSpeed = hitSpeed;
    }
}
