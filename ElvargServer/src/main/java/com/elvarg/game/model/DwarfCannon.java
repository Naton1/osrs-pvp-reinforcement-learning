package com.elvarg.game.model;

import com.elvarg.game.entity.impl.object.GameObject;

/**
 * @author Gabriel Hannason
 */

public class DwarfCannon {

    private int ownerIndex;
    private GameObject object;
    private int cannonballs = 0;
    private boolean cannonFiring = false;
    private int rotations = 0;
    public DwarfCannon(int ownerIndex, GameObject object) {
        this.ownerIndex = ownerIndex;
        this.object = object;
    }

    public int getOwnerIndex() {
        return this.ownerIndex;
    }

    public GameObject getObject() {
        return this.object;
    }

    public int getCannonballs() {
        return this.cannonballs;
    }

    public void setCannonballs(int cannonballs) {
        this.cannonballs = cannonballs;
    }

    public boolean cannonFiring() {
        return this.cannonFiring;
    }

    public void setCannonFiring(boolean firing) {
        this.cannonFiring = firing;
    }

    public int getRotations() {
        return this.rotations;
    }

    public void setRotations(int rotations) {
        this.rotations = rotations;
    }

    public void addRotation(int amount) {
        this.rotations += amount;
    }

}
