package com.elvarg.game.entity.impl.object;

import com.elvarg.game.World;
import com.elvarg.game.collision.RegionManager;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.Location;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

/**
 * A simple object manager used to manage {@link GameObject}s which are spawned
 * by the server.
 * <p>
 * For client/map-objects, see {@link MapObjects}.
 *
 * @author Professor Oak
 */
public class ObjectManager {

    /**
     * Handles what happens when a player enters a new region. We need to send all
     * the objects related to that region.
     *
     * @param player The player whose changing region.
     */
    public static void onRegionChange(Player player) {
        World.getObjects().forEach((o) -> perform(o, OperationType.SPAWN));
        World.getRemovedObjects().stream().forEach((o) -> player.getPacketSender().sendObjectRemoval(o));
    }

    /**
     * Registers a {@link GameObject} to the world.
     *
     * @param object       The object being registered.
     * @param playerUpdate Should the send object packet be sent to nearby players?
     */
    public static void register(GameObject object, boolean playerUpdate) {
        // Check for matching object on this tile.
        Iterator<GameObject> iterator = World.getObjects().iterator();
        for (; iterator.hasNext();) {
            GameObject o = iterator.next();
            if (o.getLocation().equals(object.getLocation()) && object.getPrivateArea() == o.getPrivateArea()) {
                iterator.remove();
            }
        }
        List<GameObject> matchingObjects = World.getRemovedObjects().stream().filter(o -> o.getType() == object.getType() && o.getLocation().equals(object.getLocation())).toList();
        matchingObjects.forEach(RegionManager::removeObjectClipping);
        matchingObjects.forEach(World.getRemovedObjects()::remove);

        World.getObjects().add(object);
        if (playerUpdate) {
            perform(object, OperationType.SPAWN);
        }
    }

    /**
     * Deregisters a {@link GameObject} from the world.
     *
     * @param object       The object to deregister.
     * @param playerUpdate Should the object removal packet be sent to nearby players?
     */
    public static void deregister(GameObject object, boolean playerUpdate) {
        World.getObjects().removeIf(o -> o.equals(object));
        perform(object, OperationType.DESPAWN);

        World.getRemovedObjects().add(object);
    }

    /**
     * Performs the given {@link OperationType} on the given {@link GameObject}.
     * Used for spawning and despawning objects. If the object has an owner, it will
     * only be spawned for them. Otherwise, it will act as global.
     *
     * @param object
     * @param type
     */
    public static void perform(GameObject object, OperationType type) {
        if (object.getId() == -1) {
            type = OperationType.DESPAWN;
        }
        /**
         * We add/remove to/from mapobjects aswell. This is because the server handles
         * clipping via the map objects and also checks for cheatclients via them.
         */
        switch (type) {
        case SPAWN:
            MapObjects.add(object);
            break;
        case DESPAWN:
            MapObjects.remove(object);
            break;
        }

        /**
         * Send the object to nearby players.
         */
        switch (type) {
        case SPAWN:
        case DESPAWN:
            for (Player player : World.getPlayers()) {
                if (player == null)
                    continue;
                if (player.getPrivateArea() != object.getPrivateArea()) {
                    continue;
                }
                if (!player.getLocation().isWithinDistance(object.getLocation(), 64)) {
                    continue;
                }
                if (type == OperationType.SPAWN) {
                    player.getPacketSender().sendObject(object);
                } else {
                    player.getPacketSender().sendObjectRemoval(object);
                }
            }
            break;
        }
    }

    /**
     * Checks if a {@link GameObject} exists at the given location.
     *
     * @param position
     * @return
     */
    public static boolean exists(Location position) {
        Iterator<GameObject> iterator = World.getObjects().iterator();
        for (; iterator.hasNext(); ) {
            GameObject object = iterator.next();
            if (object.getLocation().equals(position)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if a {@link GameObject} exists at the given location with the given
     * id.
     *
     * @param position
     * @return
     */
    public static boolean exists(int id, Location position) {
        Iterator<GameObject> iterator = World.getObjects().iterator();
        for (; iterator.hasNext(); ) {
            GameObject object = iterator.next();
            if (object.getLocation().equals(position)) {
                if (object.getId() == id) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * The possible operation types.
     */
    public enum OperationType {
        SPAWN, DESPAWN;
    }
}