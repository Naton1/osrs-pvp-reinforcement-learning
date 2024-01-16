package com.elvarg.game.content.combat;

public class CombatConstants {

    /**
     * The amount of time it takes for cached damage to timeout.
     */
    // Damage cached for currently 60 seconds will not be accounted for.
    public static final long DAMAGE_CACHE_TIMEOUT = 60000;

    /**
     * The amount of damage that will be drained by combat protection prayer.
     */
    public static final double PRAYER_DAMAGE_REDUCTION_AGAINST_PLAYERS = .6; //40% damage reduction
    public static final double PRAYER_DAMAGE_REDUCTION_AGAINST_NPCS = 0; //100% damage reduction
    public static final double ELYSIAN_DAMAGE_REDUCTION = .75; // 25% damage reduction

    /**
     * The rate at which accuracy will be reduced by combat protection prayer.
     */
    public static final double PRAYER_ACCURACY_REDUCTION_AGAINST_PLAYERS = .30; //-70% accuracy
    public static final double PRAYER_ACCURACY_REDUCTION_AGAINST_NPCS = .20; //-80% accuracy

    /**
     * The amount of hitpoints the redemption prayer will heal.
     */
    // Currently at .25 meaning hitpoints will be healed by 25% of the remaining
    // prayer points when using redemption.
    public static final double REDEMPTION_PRAYER_HEAL = .25;

    /**
     * The maximum amount of damage inflicted by retribution.
     */
    // Damage between currently 0-15 will be inflicted if in the specified
    // radius when the retribution prayer effect is activated.
    public static final int MAXIMUM_RETRIBUTION_DAMAGE = 15;

    /**
     * The radius that retribution will hit players in.
     */

    // All players within currently 3 squares will get hit by the retribution
    // effect.
    public static final int RETRIBUTION_RADIUS = 3;

    /**
     * The absolute maximum attack distance possible.
     */
    public static final int MAX_ATTACK_DISTANCE = 10;
}
