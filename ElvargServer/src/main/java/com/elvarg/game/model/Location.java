package com.elvarg.game.model;

import com.elvarg.util.Misc;

/**
 * Represents a single world tile position.
 *
 * @author relex lawl
 */

public class Location {

    /**
     * The x coordinate of the position.
     */
    private int x;
    /**
     * The y coordinate of the position.
     */
    private int y;
    /**
     * The height level of the position.
     */
    private int z;

    /**
     * The Position constructor.
     *
     * @param x The x-type coordinate of the position.
     * @param y The y-type coordinate of the position.
     * @param z The height of the position.
     */
    public Location(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /**
     * The Position constructor.
     *
     * @param x The x-type coordinate of the position.
     * @param y The y-type coordinate of the position.
     */
    public Location(int x, int y) {
        this(x, y, 0);
    }

    /**
     * Gets the x coordinate of this position.
     *
     * @return The associated x coordinate.
     */
    public int getX() {
        return x;
    }

    /**
     * Sets the x coordinate of this position.
     *
     * @return The Position instance.
     */
    public Location setX(int x) {
        this.x = x;
        return this;
    }

    /**
     * Gets the y coordinate of this position.
     *
     * @return The associated y coordinate.
     */
    public int getY() {
        return y;
    }

    /**
     * Sets the y coordinate of this position.
     *
     * @return The Position instance.
     */
    public Location setY(int y) {
        this.y = y;
        return this;
    }

    /**
     * Gets the height level of this position.
     *
     * @return The associated height level.
     */
    public int getZ() {
        return z;
    }

    /**
     * Sets the height level of this position.
     *
     * @return The Position instance.
     */
    public Location setZ(int z) {
        this.z = z;
        return this;
    }

    /**
     * Sets the player's associated Position values.
     *
     * @param x The new x coordinate.
     * @param y The new y coordinate.
     * @param z The new height level.
     */
    public void set(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public void setAs(Location other) {
        this.x = other.x;
        this.y = other.y;
        this.z = other.z;
    }

    /**
     * Gets the local x coordinate relative to a specific region.
     *
     * @param position The region the coordinate will be relative to.
     * @return The local x coordinate.
     */
    public int getLocalX(Location position) {
        return x - 8 * position.getRegionX();
    }

    /**
     * Gets the local y coordinate relative to a specific region.
     *
     * @param position The region the coordinate will be relative to.
     * @return The local y coordinate.
     */
    public int getLocalY(Location position) {
        return y - 8 * position.getRegionY();
    }

    /**
     * Gets the local x coordinate relative to a specific region.
     *
     * @return The local x coordinate.
     */
    public int getLocalX() {
        return x - 8 * getRegionX();
    }

    /**
     * Gets the local y coordinate relative to a specific region.
     *
     * @return The local y coordinate.
     */
    public int getLocalY() {
        return y - 8 * getRegionY();
    }

    /**
     * Gets the region x coordinate.
     *
     * @return The region x coordinate.
     */
    public int getRegionX() {
        return (x >> 3) - 6;
    }

    /**
     * Gets the region y coordinate.
     *
     * @return The region y coordinate.
     */
    public int getRegionY() {
        return (y >> 3) - 6;
    }

    /**
     * Adds steps/coordinates to this position.
     */
    public Location add(int x, int y) {
        this.x += x;
        this.y += y;
        return this;
    }

    /**
     * Adds steps/coordinates to this position.
     */
    public Location add(int x, int y, int z) {
        this.x += x;
        this.y += y;
        this.z += z;
        return this;
    }

    public Location addX(int x) {
        this.x += x;
        return this;
    }

    public Location addY(int y) {
        this.y += y;
        return this;
    }
    
    public Location transform(int x, int y) {
        return clone().addX(x).addY(y);
    }
    
    public boolean isPerpendicularTo(Location other) {
        Location delta = Misc.delta(this, other);
        return delta.getX() != delta.getY() && delta.getX() == 0 || delta.getY() == 0;
    }

    /**
     * Checks if the position is within distance of another.
     *
     * @param other    The other position.
     * @param distance The distance.
     * @return {@code true} if so, {@code false} if not.
     */
    public boolean isWithinDistance(Location other, int distance) {
        if (z != other.getZ()) {
        	return false;
        }
        int deltaX = Math.abs(x - other.x);
        int deltaY = Math.abs(y - other.y);
        return deltaX <= distance && deltaY <= distance;
    }

    /**
     * Checks if this location is within interaction range of another.
     *
     * @param other The other location.
     * @return <code>true</code> if the location is in range,
     * <code>false</code> if not.
     */
    public boolean isWithinInteractionDistance(Location other) {
        if (z != other.z) {
            return false;
        }
        int deltaX = other.x - x, deltaY = other.y - y;
        return deltaX <= 2 && deltaX >= -3 && deltaY <= 2 && deltaY >= -3;
    }

    /**
     * Gets the distance between this position and another position. Only X and
     * Y are considered (i.e. 2 dimensions).
     *
     * @param other The other position.
     * @return The distance.
     */
    public int getDistance(Location other) {
        int deltaX = x - other.x;
        int deltaY = y - other.y;
        return (int) Math.ceil(Math.sqrt(deltaX * deltaX + deltaY * deltaY));
    }
    
	/**
	 * Increments the {@code X}, {@code Y}, and {@code Z} coordinate values
	 * within this container by {@code amountX}, {@code amountY}, and
	 * {@code amountZ}.
	 * @param position the position to gather the amount to increment the coordinate by.
	 * @return an instance of this position.
	 */
	public final Location move(Location position) {
		int x = (this.x + position.getX());
		int y = (this.y + position.getY());
		int z = (this.z + position.getZ());
		return new Location(x, y, z);
	}
	
	/**
	 * Increments the {@code X} and {@code Y} coordinate values within this
	 * container by deltas of the set {@code Direction}.
	 * @param direction the direction to move.
	 * @return an instance of this position.
	 */
	public final Location move(Direction direction) {
		return move(new Location(direction.getX(), direction.getY(), 0));
	}
    
	public static Location delta(Location a, Location b) {
		return new Location(b.x - a.x, b.y - a.y);
	}

    public double distanceToPoint(int pointX, int pointY) {
        return Math.sqrt(Math.pow(x - pointX, 2)
                + Math.pow(y - pointY, 2));
    }
    
    public int calculateDistance(Location other) {
        int xDiff = getX() - other.getX();
        int yDiff = getY() - other.getY();
        return (int)Math.sqrt(xDiff * xDiff + yDiff * yDiff);
    }
    
    public static int calculateDistance(Location[] tiles, Location[] otherTiles) {
        int lowestCount = Integer.MAX_VALUE;

        for (Location tile : tiles) {
            for (Location toTile : otherTiles) {

                if (tile == toTile) {
                    return 0;
                }

                int distance = tile.calculateDistance(toTile);
                if (distance < lowestCount) {
                    lowestCount = distance;
                }
            }
        }

        return lowestCount;
    }
    
    @Override
    public Location clone() {
        return new Location(x, y, z);
    }

    @Override
    public String toString() {
        return "Position values: [x, y, z] - [" + x + ", " + y + ", " + z + "].";
    }

    @Override
    public int hashCode() {
        return z << 30 | x << 15 | y;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof Location)) {
            return false;
        }
        Location position = (Location) other;
        return position.x == x && position.y == y && position.z == z;
    }

    public boolean isViewableFrom(Location other) {
        if (this.getZ() != other.getZ())
            return false;
        Location p = Misc.delta(this, other);
        return p.x <= 14 && p.x >= -15 && p.y <= 14 && p.y >= -15;
    }

}