package com.elvarg.game.entity.impl.object;

import com.elvarg.game.World;
import com.elvarg.game.definition.ObjectDefinition;
import com.elvarg.game.entity.Entity;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.Animation;
import com.elvarg.game.model.Graphic;
import com.elvarg.game.model.Location;
import com.elvarg.game.model.areas.impl.PrivateArea;

/**
 * This file manages a game object entity on the globe.
 *
 * @author Relex lawl / iRageQuit2012
 */

public class GameObject extends Entity {
    
    /**
     * The object's id.
     */
    private final int id;

    /**
     * The object's type (default=10).
     */
    private int type;

    /**
     * The object's current direction to face.
     */
    private int face;

    private String owner;

    public String getOwner() {
        return owner;
    }

    /**
     * GameObject constructor to call upon a new game object.
     *
     * @param id       The new object's id.
     * @param position The new object's position on the globe.
     * @param type     The new object's type.
     * @param face     The new object's facing position.
     */
    public GameObject(int id, Location position, int type, int face, String owner, PrivateArea privateArea) {
        super(position);
        this.id = id;
        this.type = type;
        this.face = face;
        this.owner = owner;
        if (privateArea != null) {
            privateArea.add(this);
        }
    }

    public GameObject(int id, Location position, int type, int face, PrivateArea privateArea) {
        super(position);
        this.id = id;
        this.type = type;
        this.face = face;
        if (privateArea != null) {
            privateArea.add(this);
        }
    }

    /**
     * Gets the object's id.
     *
     * @return id.
     */
    public int getId() {
        return id;
    }

    /**
     * Gets the object's type.
     *
     * @return type.
     */
    public int getType() {
        return type;
    }

    /**
     * Sets the object's type.
     *
     * @param type New type value to assign.
     */
    public void setType(int type) {
        this.type = type;
    }

    /**
     * Gets the object's current face direction.
     *
     * @return face.
     */
    public int getFace() {
        return face;
    }

    /**
     * Sets the object's face direction.
     *
     * @param face Face value to which object will face.
     */
    public void setFace(int face) {
        this.face = face;
    }

    /**
     * Gets the object's definition.
     *
     * @return definition.
     */
    public ObjectDefinition getDefinition() {
        return ObjectDefinition.forId(id);
    }

    @Override
    public void performAnimation(Animation animation) {
        for (Player player : World.getPlayers()) {
            if (player == null)
                continue;
            if (player.getPrivateArea() != getPrivateArea()) {
                continue;
            }
            if (!player.getLocation().isViewableFrom(getLocation())) {
                continue;
            }
            player.getPacketSender().sendObjectAnimation(this, animation);
        }
    }

    @Override
    public void performGraphic(Graphic graphic) {
        for (Player player : World.getPlayers()) {
            if (player == null)
                continue;
            if (player.getPrivateArea() != getPrivateArea()) {
                continue;
            }
            if (!player.getLocation().isViewableFrom(getLocation())) {
                continue;
            }
            player.getPacketSender().sendGraphic(graphic, getLocation());
        }
    }

    @Override
    public int size() {
        ObjectDefinition definition = getDefinition();
        if (definition == null)
            return 1;
        return (definition.getSizeX() + definition.getSizeY()) - 1;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof GameObject))
            return false;
        GameObject object = (GameObject) o;
        return object.getLocation().equals(getLocation()) && object.getId() == getId() && object.getFace() == getFace()
                && object.getType() == getType() && object.getPrivateArea() == getPrivateArea();
    }

    @Override
    public GameObject clone() {
        return new GameObject(getId(), getLocation(), getType(), getFace(), getPrivateArea());
    }
}
