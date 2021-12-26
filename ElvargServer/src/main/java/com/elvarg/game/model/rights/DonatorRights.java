package com.elvarg.game.model.rights;

public enum DonatorRights {
	
    NONE(-1, -1, ""),
    REGULAR_DONATOR(622, 40, "[Donator]"),
    SUPER_DONATOR(623, 25, "[Super Donator]"),
    UBER_DONATOR(624, 10, "[Uber Donator]"),
    ;

	private final int spriteId, yellDelay;
	private final String yellTag;
	
	DonatorRights(int spriteId, int yellDelay, String yellTag) {
		this.spriteId = spriteId;
		this.yellDelay = yellDelay;
		this.yellTag = yellTag;	
	}

	public int getSpriteId() {
		return spriteId;
	}

	public int getYellDelay() {
		return yellDelay;
	}

	public String getYellTag() {
		return yellTag;
	}
}
