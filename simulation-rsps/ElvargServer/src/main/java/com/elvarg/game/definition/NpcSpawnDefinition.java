package com.elvarg.game.definition;

import com.elvarg.game.model.Direction;
import com.elvarg.game.model.Location;

public class NpcSpawnDefinition extends DefaultSpawnDefinition {

    public NpcSpawnDefinition(int id, Location position, Direction facing, int radius) {
		super(id, position);
		this.facing = facing;
		this.radius = radius;
	}

    public NpcSpawnDefinition(int id, Location position, Direction facing, int radius, String descripton) {
        super(id, position);
        this.facing = facing;
        this.radius = radius;
        this.description = descripton;
    }

	private Direction facing;
    private int radius;
    private String description;

    public Direction getFacing() {
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
