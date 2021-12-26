package com.elvarg.game.model;

/**
 * Represents a player's prayer book.
 *
 * @author relex lawl
 */

public enum Prayerbook {

    NORMAL(5608, "You sense a surge of purity flow through your body!"),
    CURSES(32500, "You sense a surge of power flow through your body!");

    /**
     * The interface id to switch prayer tab to.
     */
    private final int interfaceId;
    /**
     * The message received upon switching prayers.
     */
    private final String message;

    /**
     * The PrayerBook constructor.
     *
     * @param interfaceId The interface id to switch prayer tab to.
     * @param message     The message received upon switching prayers.
     */
    private Prayerbook(int interfaceId, String message) {
        this.interfaceId = interfaceId;
        this.message = message;
    }

    /**
     * Gets the PrayerBook instance for said id.
     *
     * @param id The id to match to prayer book's ordinal.
     * @return The prayerbook who's ordinal is equal to id.
     */
    public static Prayerbook forId(int id) {
        for (Prayerbook book : Prayerbook.values()) {
            if (book.ordinal() == id) {
                return book;
            }
        }
        return NORMAL;
    }

    /**
     * Gets the interface id to set prayer tab to.
     *
     * @return The new prayer tab interface id.
     */
    public int getInterfaceId() {
        return interfaceId;
    }

    /**
     * Gets the message received when switching to said prayer book.
     *
     * @return The message player will receive.
     */
    public String getMessage() {
        return message;
    }
}
