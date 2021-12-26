package com.elvarg.game.content.combat.hit;

import com.elvarg.util.Stopwatch;

public class HitDamageCache {

    /**
     * The stopwatch to time how long the damage is cached.
     */
    private final Stopwatch stopwatch;
    /**
     * The amount of cached damage.
     */
    private int damage;

    /**
     * Create a new {@link CombatDamageCache}.
     *
     * @param damage the amount of cached damage.
     */
    public HitDamageCache(int damage) {
        this.damage = damage;
        this.stopwatch = new Stopwatch().reset();
    }

    /**
     * Gets the amount of cached damage.
     *
     * @return the amount of cached damage.
     */
    public int getDamage() {
        return damage;
    }

    /**
     * Increments the amount of cached damage.
     *
     * @param damage the amount of cached damage to add.
     */
    public void incrementDamage(int damage) {
        this.damage += damage;
        this.stopwatch.reset();
    }

    /**
     * Gets the stopwatch to time how long the damage is cached.
     *
     * @return the stopwatch to time how long the damage is cached.
     */
    public Stopwatch getStopwatch() {
        return stopwatch;
    }

}
