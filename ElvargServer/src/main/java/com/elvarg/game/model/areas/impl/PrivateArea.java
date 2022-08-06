package com.elvarg.game.model.areas.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.elvarg.game.World;
import com.elvarg.game.entity.Entity;
import com.elvarg.game.entity.impl.Mobile;
import com.elvarg.game.entity.impl.grounditem.ItemOnGround;
import com.elvarg.game.entity.impl.grounditem.ItemOnGroundManager;
import com.elvarg.game.entity.impl.npc.NPC;
import com.elvarg.game.entity.impl.object.GameObject;
import com.elvarg.game.entity.impl.object.ObjectManager;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.Boundary;
import com.elvarg.game.model.Location;
import com.elvarg.game.model.areas.Area;

public abstract class PrivateArea extends Area {

    public final List<Entity> entities;
    private final Map<Location, Integer> clips;
    private boolean destroyed;
    
    public PrivateArea(List<Boundary> boundaries) {
        super(boundaries);
        entities = new ArrayList<>();
        clips = new HashMap<>();
    }

    @Override
    public void postLeave(Mobile mobile, boolean logout) {
        remove(mobile);
        if (getPlayers().isEmpty()) {
            destroy();
        }
    }

    @Override
    public void postEnter(Mobile mobile) {
        add(mobile);
    }

    public void remove(Entity entity) {
        Iterator<Entity> it = entities.iterator();
        while (it.hasNext()) {
            Entity e = it.next();
            if (e.equals(entity)) {
                it.remove();
            }
        }
        entity.setArea(null);
    }

    public void add(Entity entity) {
        if (!entities.contains(entity)) {
            entities.add(entity);
        }
        entity.setArea(this);
    }

    public void destroy() {
        if (destroyed) {
            return;
        }
        for (NPC npc : getNpcs()) {
            if (npc.isRegistered()) {
                World.getRemoveNPCQueue().add(npc);
            }
        }
        for (GameObject object : getObjects()) {
            ObjectManager.deregister(object, false);
        }
        for (ItemOnGround item : World.getItems()) {
            if (item.getPrivateArea() == this) {
                ItemOnGroundManager.deregister(item);
            }
        }
        entities.clear();
        clips.clear();
        destroyed = true;
    }

    public List<GameObject> getObjects() {
        List<GameObject> objects = new ArrayList<>();
        for (Entity entity : entities) {
            if (entity instanceof GameObject) {
                objects.add((GameObject) entity);
            }
        }
        return objects;
    }
    
    public void setClip(Location location, int mask) {
        clips.put(location, mask);
    }
    
    public void removeClip(Location location) {
        Iterator<Location> it = clips.keySet().iterator();
        while (it.hasNext()) {
            Location clipLocation = it.next();
            if (clipLocation.equals(location)) {
                it.remove();
            }
        }
    }
    
    public int getClip(Location location) {
        return clips.getOrDefault(location, 0);
    }
    
    public boolean isDestroyed() {
        return destroyed;
    }
}
