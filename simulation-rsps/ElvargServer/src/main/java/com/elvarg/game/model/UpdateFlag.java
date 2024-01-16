package com.elvarg.game.model;

import java.util.BitSet;

/**
 * Represents a character entity's update flags.
 *
 * @author relex lawl
 */

public class UpdateFlag {

    /**
     * A set containing the entity's update flags.
     */
    private BitSet flags = new BitSet();

    /**
     * Checks if {@code flag} is contained in the entity's flag set.
     *
     * @param flag The flag to check.
     * @return The flags set contains said flag.
     */
    public boolean flagged(Flag flag) {
        return flags.get(flag.ordinal());
    }

    /**
     * Checks if an update is required by checking if flags set is empty.
     *
     * @return Flags set is not empty.
     */
    public boolean isUpdateRequired() {
        return !flags.isEmpty();
    }

    /**
     * Puts a flag value into the flags set.
     *
     * @param flag Flag to put into the flags set.
     * @return The UpdateFlag instance.
     */
    public UpdateFlag flag(Flag flag) {
        flags.set(flag.ordinal(), true);
        return this;
    }

    /**
     * Removes every flag in the flags set.
     *
     * @return The UpdateFlag instance.
     */
    public UpdateFlag reset() {
        flags.clear();
        return this;
    }
}
