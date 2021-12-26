package com.elvarg.game.definition;

import com.elvarg.game.model.FacingDirection;
import com.elvarg.game.model.Location;

public class NpcSpawnDefinition extends DefaultSpawnDefinition {

    public NpcSpawnDefinition(int id, Location position, FacingDirection facing, int radius) {
		super(id, position);
		this.facing = facing;
		this.radius = radius;
	}

	private FacingDirection facing;
    private int radius;

    public FacingDirection getFacing() {
        return facing;
    }

    public int getRadius() {
        return radius;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof NpcSpawnDefinition))
            return false;
        NpcSpawnDefinition def = (NpcSpawnDefinition) o;
        return def.getPosition().equals(getPosition())
                && def.getId() == getId()
                && def.getFacing() == getFacing()
                && def.getRadius() == getRadius();
    }
}
