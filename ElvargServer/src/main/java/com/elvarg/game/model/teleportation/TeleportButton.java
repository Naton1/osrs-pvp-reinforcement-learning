package com.elvarg.game.model.teleportation;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a button related to teleports.
 * 
 * @author Professor Oak
 */
public enum TeleportButton {
	
	HOME(-1, 19210, 21741, 19210),
	TRAINING(-1, 1164, 13035, 30064),
	MINIGAME(2, 1167, 13045, 30075),
	WILDERNESS(0, 1170, 13053, 30083),
	SLAYER(-1, 1174, 13061, 30114),
	CITY(-1, 1540, 13079, 30146),
	SKILLS(3, 1541, 13069, 30106),
	BOSSES(1, 7455, 13087, 30138),
	
	;

	public final int menu;
	public final int[] ids;

	TeleportButton(int menu, int... ids) {
		this.ids = ids;
		this.menu = menu;
	}

	public static final Map<Integer, TeleportButton> teleports = new HashMap<>();
	static {
		for (TeleportButton b : TeleportButton.values()) {
			for (int i : b.ids) {
				teleports.put(i, b);
			}
		}
	}

	public static TeleportButton get(int buttonId) {
		return teleports.get(buttonId);
	}
}
