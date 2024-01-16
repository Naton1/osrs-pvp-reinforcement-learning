package com.elvarg.game.entity;

import com.elvarg.game.GameConstants;
import com.elvarg.game.model.Animation;
import com.elvarg.game.model.Graphic;
import com.elvarg.game.model.Location;
import com.elvarg.game.model.areas.Area;
import com.elvarg.game.model.areas.impl.PrivateArea;

public abstract class Entity {

    /**
     * Represents the {@link Location} of this {@link Entity}.
     */
    private Location location = GameConstants.DEFAULT_LOCATION.clone();    
    
    /**
     * Represents the {@link Area} this {@link Entity} is currently in.
     */
    private Area area;
    
    /**
     * The Entity constructor.
     *
     * @param position The position the entity is currently in.
     */
    public Entity(Location position) {
        this.location = position;
    }
    
    /**
     * Performs an {@link Animation}.
     * @param animation
     */
    public abstract void performAnimation(Animation animation);
    
    /**
     * Performs a {@link Graphic}.
     * @param animation
     */
    public abstract void performGraphic(Graphic graphic);
    
    /**
     * Returns the size of this {@link Entity}.
     */
    public abstract int size();
    
    /**
     * Gets the entity position.
     *
     * @return the entity's world position
     */
    public Location getLocation() {
        return location;
    }

    /**
     * Sets the entity position
     *
     * @param location the world position
     */
    public Entity setLocation(Location location) {
        this.location = location;
        return this;
    }
    
    public void setArea(Area area) {
        this.area = area;
    }

    public Area getArea() {
        return area;
    }

    public PrivateArea getPrivateArea() {
        return (area instanceof PrivateArea ? ((PrivateArea) area) : null);
    }
}
