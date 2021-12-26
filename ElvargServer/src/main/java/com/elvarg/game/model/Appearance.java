package com.elvarg.game.model;

import com.elvarg.game.entity.impl.player.Player;

/**
 * This file manages a player's appearance and properties, such as head hints, gender, prayer head hints, etc.
 *
 * @author relex lawl
 */

public class Appearance {

    /**
     * The index of said body part color in the look array.
     */
    public static final int HAIR_COLOUR = 8, TORSO_COLOUR = 9, LEG_COLOUR = 10, FEET_COLOUR = 11, SKIN_COLOUR = 12,
            HEAD = 1, CHEST = 2, ARMS = 3, HANDS = 4, LEGS = 5, FEET = 6, BEARD = 7, GENDER = 0;
    /**
     * Can the player change appearance right now?
     */
    private boolean canChangeAppearance = false;
    /**
     * The player's head icon hint.
     */
    private int headHint = -1;
    /**
     * The player's bounty hunter skull.
     */
    private int bountyHunterSkull = -1;
    /**
     * The player's current character clothing.
     */
    private int[] look = new int[13];
    /**
     * The associated player.
     */
    private Player player;

    /**
     * The Appearance constructor, also sets
     * the player's default clothing.
     *
     * @param player The associated player.
     */
    public Appearance(Player player) {
        this.player = player;
        set();
    }

    /**
     * Gets the player's current head hint index.
     *
     * @return The player's head hint.
     */
    public int getHeadHint() {
        return headHint;
    }

    /**
     * Sets the player's head icon hint.
     *
     * @param headHint The hint index to use.
     * @return The Appearance instance.
     */
    public Appearance setHeadHint(int headHint) {
        this.headHint = headHint;
        player.getUpdateFlag().flag(Flag.APPEARANCE);
        return this;
    }

    /**
     * Gets the player's current bounty hunter skull.
     *
     * @return The player's skull hint.
     */
    public int getBountyHunterSkull() {
        return bountyHunterSkull;
    }

    /**
     * Sets the player's bounty hunter skull.
     *
     * @param skullHint The skull hint index to use.
     * @return The Appearance instance.
     */
    public Appearance setBountyHunterSkull(int skullHint) {
        this.bountyHunterSkull = skullHint;
        player.getUpdateFlag().flag(Flag.APPEARANCE);
        return this;
    }

    /**
     * Checks if a player can change appearance right now
     *
     * @return the canChangeAppearance value
     */
    public boolean canChangeAppearance() {
        return canChangeAppearance;
    }

    /**
     * Sets if a player can change appearance right now
     *
     * @param l The value to set
     */
    public void setCanChangeAppearance(boolean l) {
        this.canChangeAppearance = l;
    }

    /**
     * Gets the look array, which is an array with 13 elements describing the
     * look of a player.
     *
     * @return The look array.
     */
    public int[] getLook() {
        return look;
    }

    /**
     * Sets the look array.
     *
     * @param look The look array.
     * @throws IllegalArgumentException if the array length is not 12.
     */
    public void set(int[] look) {
        if (look.length < 12) {
            throw new IllegalArgumentException("Array length must be 12.");
        }
        this.look = look;
        player.getUpdateFlag().flag(Flag.APPEARANCE);
    }

    /**
     * Sets a specific look.
     *
     * @param index Array index to set.
     * @param look  Value to change look[index] to.
     */
    public void set(int index, int look) {
        this.look[index] = look;
        player.getUpdateFlag().flag(Flag.APPEARANCE);
    }

    /**
     * Sets the player's default clothing.
     */
    public void set() {
        if (isMale()) {
            look[HEAD] = 3;
            look[CHEST] = 18;
            look[ARMS] = 26;
            look[HANDS] = 34;
            look[LEGS] = 38;
            look[FEET] = 42;
            look[BEARD] = 14;
        } else {
            look[HEAD] = 48;
            look[CHEST] = 57;
            look[ARMS] = 65;
            look[HANDS] = 68;
            look[LEGS] = 77;
            look[FEET] = 80;
            look[BEARD] = 57;
        }
        look[HAIR_COLOUR] = 2;
        look[TORSO_COLOUR] = 14;
        look[LEG_COLOUR] = 5;
        look[FEET_COLOUR] = 4;
        look[SKIN_COLOUR] = 0;
        player.getUpdateFlag().flag(Flag.APPEARANCE);
    }

    public boolean isMale() {
        return look[GENDER] == 0;
    }
}
