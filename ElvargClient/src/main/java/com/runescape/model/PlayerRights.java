package com.runescape.model;

import java.util.HashMap;
import java.util.Map;

public enum PlayerRights {
    NONE(null),
    MODERATOR(ChatCrown.MOD_CROWN),
    ADMINISTRATOR(ChatCrown.ADMIN_CROWN),
    OWNER(ChatCrown.OWNER_CROWN),
    DEVELOPER(ChatCrown.DEVELOPER),
    ;
	
	private final ChatCrown crown;
	
	PlayerRights(ChatCrown crown) {
		this.crown = crown;
	}
	
	public ChatCrown getCrown() {
		return crown;
	}
	
	private static Map<Integer, PlayerRights> rights = new HashMap<>();
	static {
		for (PlayerRights r : PlayerRights.values()) {
			rights.put(r.ordinal(), r);
		}
	}
	
	public static PlayerRights get(int ordinal) {
		return rights.getOrDefault(ordinal, PlayerRights.NONE);
	}
}