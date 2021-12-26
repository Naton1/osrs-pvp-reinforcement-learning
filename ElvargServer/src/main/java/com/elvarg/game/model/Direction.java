package com.elvarg.game.model;

import java.util.EnumSet;
import java.util.Optional;

import com.elvarg.util.Misc;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

/**
 * The enumerated type whose elements represent the movement directions.
 * @author Artem Batutin <artembatutin@gmail.com>
 */
public enum Direction {
	
	/**
	 * North movement.
	 */
	NORTH(1, 0, 1, 6),
	
	/**
	 * North east movement.
	 */
	NORTH_EAST(2, 1, 1, 5),
	
	/**
	 * East movement.
	 */
	EAST(4, 1, 0, 3),
	
	/**
	 * South east movement.
	 */
	SOUTH_EAST(7, 1, -1, 0),
	
	/**
	 * South movement.
	 */
	SOUTH(6, 0, -1, 1),
	
	/**
	 * South west movement.
	 */
	SOUTH_WEST(5, -1, -1, 2),
	
	/**
	 * West movement.
	 */
	WEST(3, -1, 0, 4),
	
	/**
	 * North west movement.
	 */
	NORTH_WEST(0, -1, 1, 7),
	
	/**
	 * No movement.
	 */
	NONE(-1, 0, 0, -1);
	
	/**
	 * The identification of this direction.
	 */
	private final int id;
	
	/**
	 * The {@code x} movement of this direction.
	 */
	private final int x;
	
	/**
	 * The {@code y} movement of this direction.
	 */
	private final int y;
	
	/**
	 * The opposite {@link #id} direction of the current direction.
	 */
	private final int opposite;
	
	/**
	 * Flag if this direction is diagonal.
	 */
	private final boolean diagonal;
	
	/**
	 * Caches our enumerated values.
	 */
	public static final ImmutableSet<Direction> VALUES = Sets.immutableEnumSet(EnumSet.allOf(Direction.class));
	
	/**
	 * Creates a new {@link Direction}.
	 * @param id       the identification of this direction.
	 * @param x        the {@code x} movement of this direction.
	 * @param y        the {@code y} movement of this direction.
	 * @param opposite the opposite {@link #id} direction.
	 */
	Direction(int id, int x, int y, int opposite) {
		this.id = id;
		this.x = x;
		this.y = y;
		this.opposite = opposite;
		this.diagonal = name().contains("_");
	}
	
	/**
	 * Gets the identification of this direction.
	 * @return the identification of this direction.
	 */
	public final int getId() {
		return id;
	}
	
	/**
	 * Gets the {@code x} movement of this direction.
	 * @return the {@code x} movement of this direction.
	 */
	public final int getX() {
		return x;
	}
	
	/**
	 * Gets the {@code y} movement of this direction.
	 * @return the {@code y} movement of this direction.
	 */
	public final int getY() {
		return y;
	}
	
	/**
	 * Gets the {@code opposite} direction of this direction.
	 * @return the {@code opposite} direction of this direction.
	 */
	public final int getOpposite() {
		return opposite;
	}
	
	/**
	 * Gets the {@code diagonal} flag from this direction.
	 * @return diagonal flag.
	 */
	public final boolean isDiagonal() {
		return diagonal;
	}
	
	/**
	 * Returns a {@link Direction} wrapped in an {@link Optional}
	 * for the specified {@code id}.
	 * @param id The game object orientation id.
	 * @return The optional game object orientation.
	 */
	public static Direction valueOf(int id) {
		if(id == 0)
			return NORTH_WEST;
		if(id == 1)
			return NORTH;
		if(id == 2)
			return NORTH_EAST;
		if(id == 3)
			return WEST;
		if(id == 4)
			return EAST;
		if(id == 5)
			return SOUTH_WEST;
		if(id == 6)
			return SOUTH;
		if(id == 7)
			return SOUTH_EAST;
		return NONE;
	}
	
	/**
	 * Gets a random {@link Direction}.
	 * @return random direction.
	 */
	public static Direction random() {
		return valueOf(Misc.inclusive(0, 7));
	}
	
	/**
	 * Creates a direction from the differences between X and Y.
	 * @param dx The difference between two X coordinates.
	 * @param dy The difference between two Y coordinates.
	 * @return The direction.
	 */
	public static Direction fromDeltas(int dx, int dy) {
		if(dx < 0) {
			if(dy < 0) {
				return SOUTH_WEST;
			} else if(dy > 0) {
				return NORTH_WEST;
			} else {
				return WEST;
			}
		} else if(dx > 0) {
			if(dy < 0) {
				return SOUTH_EAST;
			} else if(dy > 0) {
				return NORTH_EAST;
			} else {
				return EAST;
			}
		} else {
			if(dy < 0) {
				return SOUTH;
			} else if(dy > 0) {
				return NORTH;
			} else {
				return NONE;
			}
		}
	}
	
	/**
	 * Creates a direction from the differences between X and Y.
	 * @param delta The delta position
	 * @return The direction.
	 */
	public static Direction fromDeltas(Location delta) {
		int dx = delta.getX();
		int dy = delta.getY();
		if(dx < 0) {
			if(dy < 0) {
				return SOUTH_WEST;
			} else if(dy > 0) {
				return NORTH_WEST;
			} else {
				return WEST;
			}
		} else if(dx > 0) {
			if(dy < 0) {
				return SOUTH_EAST;
			} else if(dy > 0) {
				return NORTH_EAST;
			} else {
				return EAST;
			}
		} else {
			if(dy < 0) {
				return SOUTH;
			} else if(dy > 0) {
				return NORTH;
			} else {
				return NONE;
			}
		}
	}
}