package com.elvarg.game.model;

public enum God {

	ARMADYL(new int[] {}),
	BANDOS(new int[] {}),
	SARADOMIN(new int[] {}),
	ZAMORAK(new int[] {}),
	;
	
	private final int[] items;
	
	God(int[] items) {
		this.items = items;
	}

	public int[] getItems() {
		return items;
	}
}
