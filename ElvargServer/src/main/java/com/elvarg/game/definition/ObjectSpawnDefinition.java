package com.elvarg.game.definition;

import com.elvarg.game.model.Location;

/**
 * Represents the definition of an object spawn.
 *
 * @author Professor Oak
 */
public class ObjectSpawnDefinition extends DefaultSpawnDefinition {

    public ObjectSpawnDefinition(int id, Location position) {
		super(id, position);
	}

	private int face = 0;
    private int type = 10;

    public int getFace() {
        return face;
    }

    public int getType() {
        return type;
    }
}
