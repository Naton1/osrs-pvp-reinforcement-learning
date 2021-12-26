package com.runescape.model;

import java.util.HashMap;
import java.util.Map;

public enum DonatorRights {
	
    NONE(null),
    REGULAR_DONATOR(ChatCrown.DONATOR),
    SUPER_DONATOR(ChatCrown.SUPER_DONATOR),
    UBER_DONATOR(ChatCrown.UBER_DONATOR),
    ;

	private final ChatCrown crown;
	
	DonatorRights(ChatCrown crown) {
		this.crown = crown;
	}
	
	public ChatCrown getCrown() {
		return crown;
	}
	
	private static Map<Integer, DonatorRights> rights = new HashMap<>();
	static {
		for (DonatorRights r : DonatorRights.values()) {
			rights.put(r.ordinal(), r);
		}
	}
	
	public static DonatorRights get(int ordinal) {
		return rights.getOrDefault(ordinal, DonatorRights.NONE);
	}
}
