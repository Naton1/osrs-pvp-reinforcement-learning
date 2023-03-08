package com.elvarg.game.model;

import java.util.EnumSet;
import java.util.Optional;

import com.elvarg.util.Misc;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

public enum Direction {

	/**
	 * North west movement.
	 */
	NORTH_WEST(0, -1, 1),
	/**
	 * North movement.
	 */
	NORTH(1, 0, 1),
	
	/**
	 * North east movement.
	 */
	NORTH_EAST(2, 1, 1),

	/**
	 * West movement.
	 */
	WEST(3, -1, 0),

	/**
	 * East movement.
	 */
	EAST(4, 1, 0),

	/**
	 * South west movement.
	 */
	SOUTH_WEST(5, -1, -1),

	/**
	 * South movement.
	 */
	SOUTH(6, 0, -1),

	/**
	 * South east movement.
	 */
	SOUTH_EAST(7, 1, -1),
	/**
	 * No movement.
	 */
	NONE(-1, 0, 0);
	
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
	 * Creates a new {@link Direction}.
	 * @param id       the identification of this direction.
	 * @param x        the {@code x} movement of this direction.
	 * @param y        the {@code y} movement of this direction.
	 */
	Direction(int id, int x, int y) {
		this.id = id;
		this.x = x;
		this.y = y;
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