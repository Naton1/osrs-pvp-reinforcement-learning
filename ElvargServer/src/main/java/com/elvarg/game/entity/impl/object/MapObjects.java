package com.elvarg.game.entity.impl.object;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.elvarg.game.collision.RegionManager;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.Location;
import com.elvarg.game.model.areas.Area;
import com.elvarg.game.model.areas.impl.PrivateArea;
import com.elvarg.game.model.rights.PlayerRights;

/**
 * Map objects are objects that are in the maps. These are loaded when the maps
 * are so that we can verify that an object exists when a player tries to
 * interact with it.
 *
 * @author Professor Oak
 */
public class MapObjects {

    /**
     * A map which holds all of our map objects.
     */
    public static final Map<Long, ArrayList<GameObject>> mapObjects = new HashMap<Long, ArrayList<GameObject>>();

    /**
     * Attempts to get an object with the given id and position.
     *
     * @param id
     * @param position
     */
    public static GameObject get(Player player, int id, Location location) {
        GameObject object = get(id, location, player.getPrivateArea());
        
        if (object == null && player.getRights() == PlayerRights.DEVELOPER) {
            player.getPacketSender().sendMessage("@red@Object with id " + id + " does not exist.");
            object = new GameObject(id, location, 10, 0, player.getPrivateArea());
        }
        
        return object;
    }
    
    /**
     * Attempts to get an object with the given id and position.
     *
     * @param id
     * @param location
     */
    public static GameObject get(int id, Location location, PrivateArea privateArea) {
        // Check instanced objects..
        if (privateArea != null) {
            for (GameObject object : privateArea.getObjects()) {
                if (object.getId() == id && object.getLocation().equals(location)) {
                    return object;
                }
            }
        }
        
        // Load region..
        RegionManager.loadMapFiles(location.getX(), location.getY());

        // Get hash..
        if (location.getZ() >= 4) {
            location = location.clone().setZ(0);
        }
        long hash = getHash(location.getX(), location.getY(), location.getZ());

        // Check if the map contains the hash..
        if (!mapObjects.containsKey(hash)) {
            return null;
        }

        // Go through the objects in the list..
        ArrayList<GameObject> list = mapObjects.get(hash);
        if (list != null) {
            Iterator<GameObject> it = list.iterator();
            for (; it.hasNext(); ) {
                GameObject o = it.next();
                if (o.getId() == id && o.getLocation().equals(location)) {
                    return o;
                }
            }
        }
        return null;
    }
    
    public static GameObject get(Location location, int type, PrivateArea privateArea) {
        // Check instanced objects..
        if (privateArea != null) {
            for (GameObject object : privateArea.getObjects()) {
                if (object.getType() == type && object.getLocation().equals(location)) {
                    return object;
                }
            }
        }
        
        // Load region..
        RegionManager.loadMapFiles(location.getX(), location.getY());

        // Get hash..
        if (location.getZ() >= 4) {
            location = location.clone().setZ(0);
        }
        long hash = getHash(location.getX(), location.getY(), location.getZ());

        // Check if the map contains the hash..
        if (!mapObjects.containsKey(hash)) {
            return null;
        }

        // Go through the objects in the list..
        ArrayList<GameObject> list = mapObjects.get(hash);
        if (list != null) {
            Iterator<GameObject> it = list.iterator();
            for (; it.hasNext(); ) {
                GameObject o = it.next();
                if (o.getType() == type && o.getLocation().equals(location)) {
                    return o;
                }
            }
        }
        return null;
    }
    
    /**
     * Checks if an gameobject exists.
     *
     * @param object
     * @return
     */
    public static boolean exists(GameObject object) {
        return get(object.getId(), object.getLocation(), object.getPrivateArea()) == object;
    }

    /**
     * Attempts to add a new object to our map of mapobjects.
     *
     * @param object
     */
    public static void add(GameObject object) {     
        // Register the object if it's not registered in a private area..
        if (object.getPrivateArea() == null) {
            
            // Get hash for object..
            long hash = getHash(object.getLocation().getX(), object.getLocation().getY(), object.getLocation().getZ());
            
            if (mapObjects.containsKey(hash)) {
                // Check if object already exists in this list..
                boolean exists = false;
                List<GameObject> list = mapObjects.get(hash);
                Iterator<GameObject> it = list.iterator();
                for (; it.hasNext(); ) {
                    GameObject o = it.next();
                    if (o.equals(object)) {
                        exists = true;
                        break;
                    }
                }
                // If it didn't exist, add it.
                if (!exists) {
                    mapObjects.get(hash).add(object);
                }
            } else {
                ArrayList<GameObject> list = new ArrayList<GameObject>();
                list.add(object);
                mapObjects.put(hash, list);
            }
        }

        // Add clipping for object.
        RegionManager.addObjectClipping(object);
    }

    /**
     * Attempts to remove the given object from our map of mapobjects.
     *
     * @param object
     */
    public static void remove(GameObject object) {
        // Get hash for object..
        long hash = getHash(object.getLocation().getX(), object.getLocation().getY(), object.getLocation().getZ());

        // Attempt to delete..
        if (mapObjects.containsKey(hash)) {
            Iterator<GameObject> it = mapObjects.get(hash).iterator();
            while (it.hasNext()) {
                GameObject o = it.next();
                if (o.getId() == object.getId() && o.getLocation().equals(object.getLocation())) {
                    it.remove();
                }
            }
        }

        // Remove clipping from this area..
        RegionManager.removeObjectClipping(object);
    }

    /**
     * Removes all objects in this position.
     *
     * @param position
     */
    public static void clear(Location position, int clipShift) {
        // Get hash for pos..
        long hash = getHash(position.getX(), position.getY(), position.getZ());

        // Attempt to delete..
        if (mapObjects.containsKey(hash)) {
            Iterator<GameObject> it = mapObjects.get(hash).iterator();
            while (it.hasNext()) {
                GameObject o = it.next();
                if (o.getLocation().equals(position)) {
                    it.remove();
                }
            }
        }

        // Remove clipping from this area..
        RegionManager.removeClipping(position.getX(), position.getY(), position.getZ(), clipShift, null);
    }

    /**
     * Gets the hash for a map object.
     *
     * @param x
     * @param y
     * @param z
     * @return
     */
    public static long getHash(int x, int y, int z) {
        return (z + ((long) x << 24) + ((long) y << 48));
    }
}
