package com.runescape.model;

import java.util.List;

/**
 * Represents a chat message.
 *
 * @author Professor Oak
 */
public class ChatMessage {

    private String message;
    private final String name;
    private final int type;
    private final int rights;
    private final List<ChatCrown> crowns;
    
    public ChatMessage(String message, String name, int type, int rights, List<ChatCrown> crowns) {
        this.message = message;
        this.name = name;
        this.type = type;
        this.rights = rights;
        this.crowns = crowns;
    }

    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }

    public String getName() {
        return name;
    }

    public int getType() {
        return type;
    }

    public int getRights() {
        return rights;
    }

    public List<ChatCrown> getCrowns() {
        return crowns;
    }
}
