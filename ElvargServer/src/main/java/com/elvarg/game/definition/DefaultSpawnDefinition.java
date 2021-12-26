package com.elvarg.game.definition;

import com.elvarg.game.model.Location;

/**
 * Represents a definition for a basic entity-spawn, such as
 * for an npc or object.
 *
 * @author Professor Oak
 */
public class DefaultSpawnDefinition {
	
	public DefaultSpawnDefinition(int id, Location position) {
		this.id = id;
		this.position = position;
	}

    private int id;
    private Location position;

    public int getId() {
        return id;
    }

    public Location getPosition() {
        return position;
    }
}
