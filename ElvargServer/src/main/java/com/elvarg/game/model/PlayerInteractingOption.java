package com.elvarg.game.model;

/**
 * Represents a player's privilege rights.
 *
 * @author relex lawl
 */

public enum PlayerInteractingOption {

    NONE,
    CHALLENGE,
    ATTACK;

    public static PlayerInteractingOption forName(String name) {
        if (name.toLowerCase().contains("null"))
            return NONE;
        for (PlayerInteractingOption option : PlayerInteractingOption.values()) {
            if (option.toString().equalsIgnoreCase(name)) {
                return option;
            }
        }
        return null;
    }
}
