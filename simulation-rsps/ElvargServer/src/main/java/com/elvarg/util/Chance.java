package com.elvarg.util;

/**
 * Represents different type of chances.
 *
 * @author lare96
 */
public enum Chance {
    ALWAYS(100),

    VERY_COMMON(90),

    COMMON(75),

    SOMETIMES(50),

    UNCOMMON(35),

    VERY_UNCOMMON(10),

    EXTREMELY_RARE(5),

    ALMOST_IMPOSSIBLE(1);

    /**
     * The percentage of this constant.
     */
    private int percentage;

    /**
     * Creates a new {@link Chance}.
     *
     * @param percentage the percentage of this constant.
     */
    Chance(int percentage) {
        this.percentage = percentage;
    }

    /**
     * Calculates success based on the underlying chance.
     *
     * @return true if it was successful.
     */
    public boolean success() {
        return (Misc.getRandom(100)) <= percentage;
    }

    /**
     * Gets the percentage of this constant.
     *
     * @return the percentage.
     */
    public int getPercentage() {
        return percentage;
    }
}