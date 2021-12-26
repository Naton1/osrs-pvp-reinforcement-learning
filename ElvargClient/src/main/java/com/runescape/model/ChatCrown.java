package com.runescape.model;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public enum ChatCrown {
	
	MOD_CROWN("@cr1@", 618),
	ADMIN_CROWN("@cr2@", 619),
	OWNER_CROWN("@cr3@", 620),
	DEVELOPER("@cr4@", 621),
		
	// Donators
	DONATOR("@cr5@", 622),
	SUPER_DONATOR("@cr6@", 623),
	UBER_DONATOR("@cr7@", 624),
	
	// Ironman
	REGULAR_IRONMAN("@cr8@", 627),
	HARDCORE_IRONMAN("@cr9@", 628),
	
	// Extras
	YOUTUBER("@cr10@", 625),
	
	;

	private final String identifier;
	private final int spriteId;
	
	ChatCrown(String identifier, int spriteId) {
		this.identifier = identifier;
		this.spriteId = spriteId;
	}

	public String getIdentifier() {
		return identifier;
	}

	public int getSpriteId() {
		return spriteId;
	}
	
	private static final Set<ChatCrown> STAFF = EnumSet.of(MOD_CROWN, ADMIN_CROWN, OWNER_CROWN, DEVELOPER);
	
	public boolean isStaff() {
		return STAFF.contains(this);
	}
		
    public static List<ChatCrown> get(int rights, int donatorRights) {
    	List<ChatCrown> crowns = new ArrayList<>();
    	
    	PlayerRights playerRights = PlayerRights.get(rights);
    	if (playerRights != PlayerRights.NONE
    			&& playerRights.getCrown() != null) {
    		crowns.add(playerRights.getCrown());
    	}
    	
    	DonatorRights donorRights = DonatorRights.get(donatorRights);
    	if (donorRights != DonatorRights.NONE
    			&& donorRights.getCrown() != null) {
    		crowns.add(donorRights.getCrown());
    	}
    	
    	return crowns;
    }
    
}
