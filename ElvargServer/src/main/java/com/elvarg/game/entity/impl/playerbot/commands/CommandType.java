package com.elvarg.game.entity.impl.playerbot.commands;

public enum CommandType {
    /**
     * A command delivered via public chat.
     * This ensures the bot is within listening distance of the player.
     */
    PUBLIC_CHAT("public chat"),

    /**
     * A command delivered via PM.
     * This means the bot could be far away from the player.
     */
    PRIVATE_CHAT("private chat"),

    /**
     * A command delivered via Clan Chat.
     * This ensures the bot has joined the same clan as the player.
     */
    CLAN_CHAT("clan chat");

    private String label;

    CommandType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return this.label;
    }
}
