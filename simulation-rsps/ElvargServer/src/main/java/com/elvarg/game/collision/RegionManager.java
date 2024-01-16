package com.elvarg.game.collision;

import com.elvarg.game.GameConstants;
import com.elvarg.game.definition.ObjectDefinition;
import com.elvarg.game.entity.impl.Mobile;
import com.elvarg.game.entity.impl.object.GameObject;
import com.elvarg.game.entity.impl.object.MapObjects;
import com.elvarg.game.model.Direction;
import com.elvarg.game.model.Location;
import com.elvarg.game.model.areas.impl.PrivateArea;
import com.elvarg.util.CompressionUtil;
import com.elvarg.util.FileUtil;

import javax.naming.OperationNotSupportedException;
import java.io.File;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * This manager handles all regions and their related functions, such as
 * clipping.
 *
 * @author Professor Oak
 */
public class RegionManager {

    public static final int PROJECTILE_NORTH_WEST_BLOCKED = 0x200;
    public static final int PROJECTILE_NORTH_BLOCKED = 0x400;
    public static final int PROJECTILE_NORTH_EAST_BLOCKED = 0x800;
    public static final int PROJECTILE_EAST_BLOCKED = 0x1000;
    public static final int PROJECTILE_SOUTH_EAST_BLOCKED = 0x2000;
    public static final int PROJECTILE_SOUTH_BLOCKED = 0x4000;
    public static final int PROJECTILE_SOUTH_WEST_BLOCKED = 0x8000;
    public static final int PROJECTILE_WEST_BLOCKED = 0x10000;
    public static final int PROJECTILE_TILE_BLOCKED = 0x20000;
    public static final int UNKNOWN = 0x80000;
    public static final int BLOCKED_TILE = 0x200000;
    public static final int UNLOADED_TILE = 0x1000000;
    public static final int OCEAN_TILE = 2097152;

    /**
     * The map with all of our regions.
     */
    public static Map<Integer, Region> regions = new HashMap<Integer, Region>();

    /**
     * Loads the client's map_index file and constructs new regions based on the
     * data it holds.
     *
     * @throws Exception
     */
    public static void init() throws Exception {
        // Load object definitions..
        ObjectDefinition.init();
        // Load regions..
        File map_index = new File(GameConstants.CLIPPING_DIRECTORY + "map_index");
        if (!map_index.exists()) {
            throw new OperationNotSupportedException("map_index was not found!");
        }
        byte[] data = Files.readAllBytes(map_index.toPath());
        Buffer stream = new Buffer(data);
        int size = stream.readUShort();
        for (int i = 0; i < size; i++) {
            int regionId = stream.readUShort();
            int terrainFile = stream.readUShort();
            int objectFile = stream.readUShort();
            RegionManager.regions.put(regionId, new Region(regionId, terrainFile, objectFile));
        }
    }

    /**
     * Attempts to get a {@link Region} based on an id.
     *
     * @param regionId
     * @return
     */
    public static Optional<Region> getRegion(int regionId) {
        return Optional.ofNullable(regions.get(regionId));
    }

    /**
     * Attempts to get a {@link Region} based on coordinates.
     *
     * @param x
     * @param y
     * @return
     */
    public static Optional<Region> getRegion(int x, int y) {
        loadMapFiles(x, y);
        return getRegion(calculateRegionId(x, y));
    }


    /**
     * Calculates the region id for a absolute x and y coordinates.
     *
     * @param x
     * @param y
     * @return
     */
    public static int calculateRegionId(int x, int y) {
        int regionX = x >> 3;
        int regionY = y >> 3;
        return ((regionX / 8) << 8) + (regionY / 8);
    }

    /**
     * Attempts to add clipping for a variable object.
     *
     * @param x
     * @param y
     * @param height
     * @param type
     * @param direction
     * @param tall
     */
    private static void addClippingForVariableObject(int x, int y, int height, int type, int direction, boolean tall, PrivateArea privateArea) {
        if (type == 0) {
            if (direction == 0) {
                addClipping(x, y, height, 128, privateArea);
                addClipping(x - 1, y, height, 8, privateArea);
            } else if (direction == 1) {
                addClipping(x, y, height, 2, privateArea);
                addClipping(x, y + 1, height, 32, privateArea);
            } else if (direction == 2) {
                addClipping(x, y, height, 8, privateArea);
                addClipping(x + 1, y, height, 128, privateArea);
            } else if (direction == 3) {
                addClipping(x, y, height, 32, privateArea);
                addClipping(x, y - 1, height, 2, privateArea);
            }
        } else if (type == 1 || type == 3) {
            if (direction == 0) {
                addClipping(x, y, height, 1, privateArea);
                addClipping(x - 1, y, height, 16, privateArea);
            } else if (direction == 1) {
                addClipping(x, y, height, 4, privateArea);
                addClipping(x + 1, y + 1, height, 64, privateArea);
            } else if (direction == 2) {
                addClipping(x, y, height, 16, privateArea);
                addClipping(x + 1, y - 1, height, 1, privateArea);
            } else if (direction == 3) {
                addClipping(x, y, height, 64, privateArea);
                addClipping(x - 1, y - 1, height, 4, privateArea);
            }
        } else if (type == 2) {
            if (direction == 0) {
                addClipping(x, y, height, 130, privateArea);
                addClipping(x - 1, y, height, 8, privateArea);
                addClipping(x, y + 1, height, 32, privateArea);
            } else if (direction == 1) {
                addClipping(x, y, height, 10, privateArea);
                addClipping(x, y + 1, height, 32, privateArea);
                addClipping(x + 1, y, height, 128, privateArea);
            } else if (direction == 2) {
                addClipping(x, y, height, 40, privateArea);
                addClipping(x + 1, y, height, 128, privateArea);
                addClipping(x, y - 1, height, 2, privateArea);
            } else if (direction == 3) {
                addClipping(x, y, height, 160, privateArea);
                addClipping(x, y - 1, height, 2, privateArea);
                addClipping(x - 1, y, height, 8, privateArea);
            }
        }
        if (tall) {
            // If an object is tall, it blocks projectiles too
            if (type == 0) {
                if (direction == 0) {
                    addClipping(x, y, height, 65536, privateArea);
                    addClipping(x - 1, y, height, 4096, privateArea);
                } else if (direction == 1) {
                    addClipping(x, y, height, 1024, privateArea);
                    addClipping(x, y + 1, height, 16384, privateArea);
                } else if (direction == 2) {
                    addClipping(x, y, height, 4096, privateArea);
                    addClipping(x + 1, y, height, 65536, privateArea);
                } else if (direction == 3) {
                    addClipping(x, y, height, 16384, privateArea);
                    addClipping(x, y - 1, height, 1024, privateArea);
                }
            }
            if (type == 1 || type == 3) {
                if (direction == 0) {
                    addClipping(x, y, height, 512, privateArea);
                    addClipping(x - 1, y + 1, height, 8192, privateArea);
                } else if (direction == 1) {
                    addClipping(x, y, height, 2048, privateArea);
                    addClipping(x + 1, y + 1, height, 32768, privateArea);
                } else if (direction == 2) {
                    addClipping(x, y, height, 8192, privateArea);
                    addClipping(x + 1, y + 1, height, 512, privateArea);
                } else if (direction == 3) {
                    addClipping(x, y, height, 32768, privateArea);
                    addClipping(x - 1, y - 1, height, 2048, privateArea);
                }
            } else if (type == 2) {
                if (direction == 0) {
                    addClipping(x, y, height, 66560, privateArea);
                    addClipping(x - 1, y, height, 4096, privateArea);
                    addClipping(x, y + 1, height, 16384, privateArea);
                } else if (direction == 1) {
                    addClipping(x, y, height, 5120, privateArea);
                    addClipping(x, y + 1, height, 16384, privateArea);
                    addClipping(x + 1, y, height, 65536, privateArea);
                } else if (direction == 2) {
                    addClipping(x, y, height, 20480, privateArea);
                    addClipping(x + 1, y, height, 65536, privateArea);
                    addClipping(x, y - 1, height, 1024, privateArea);
                } else if (direction == 3) {
                    addClipping(x, y, height, 81920, privateArea);
                    addClipping(x, y - 1, height, 1024, privateArea);
                    addClipping(x - 1, y, height, 4096, privateArea);
                }
            }
        }
    }

    /**
     * Attempts to remove clipping for a variable object.
     *
     * @param x
     * @param y
     * @param height
     * @param type
     * @param direction
     * @param tall
     */
    private static void removeClippingForVariableObject(int x, int y, int height, int type, int direction,
                                                        boolean tall, PrivateArea privateArea) {
        if (type == 0) {
            if (direction == 0) {
                removeClipping(x, y, height, 128, privateArea);
                removeClipping(x - 1, y, height, 8, privateArea);
            } else if (direction == 1) {
                removeClipping(x, y, height, 2, privateArea);
                removeClipping(x, y + 1, height, 32, privateArea);
            } else if (direction == 2) {
                removeClipping(x, y, height, 8, privateArea);
                removeClipping(x + 1, y, height, 128, privateArea);
            } else if (direction == 3) {
                removeClipping(x, y, height, 32, privateArea);
                removeClipping(x, y - 1, height, 2, privateArea);
            }
        } else if (type == 1 || type == 3) {
            if (direction == 0) {
                removeClipping(x, y, height, 1, privateArea);
                removeClipping(x - 1, y, height, 16, privateArea);
            } else if (direction == 1) {
                removeClipping(x, y, height, 4, privateArea);
                removeClipping(x + 1, y + 1, height, 64, privateArea);
            } else if (direction == 2) {
                removeClipping(x, y, height, 16, privateArea);
                removeClipping(x + 1, y - 1, height, 1, privateArea);
            } else if (direction == 3) {
                removeClipping(x, y, height, 64, privateArea);
                removeClipping(x - 1, y - 1, height, 4, privateArea);
            }
        } else if (type == 2) {
            if (direction == 0) {
                removeClipping(x, y, height, 130, privateArea);
                removeClipping(x - 1, y, height, 8, privateArea);
                removeClipping(x, y + 1, height, 32, privateArea);
            } else if (direction == 1) {
                removeClipping(x, y, height, 10, privateArea);
                removeClipping(x, y + 1, height, 32, privateArea);
                removeClipping(x + 1, y, height, 128, privateArea);
            } else if (direction == 2) {
                removeClipping(x, y, height, 40, privateArea);
                removeClipping(x + 1, y, height, 128, privateArea);
                removeClipping(x, y - 1, height, 2, privateArea);
            } else if (direction == 3) {
                removeClipping(x, y, height, 160, privateArea);
                removeClipping(x, y - 1, height, 2, privateArea);
                removeClipping(x - 1, y, height, 8, privateArea);
            }
        }
        if (tall) {
            // If an object is tall, it blocks projectiles too
            if (type == 0) {
                if (direction == 0) {
                    removeClipping(x, y, height, 65536, privateArea);
                    removeClipping(x - 1, y, height, 4096, privateArea);
                } else if (direction == 1) {
                    removeClipping(x, y, height, 1024, privateArea);
                    removeClipping(x, y + 1, height, 16384, privateArea);
                } else if (direction == 2) {
                    removeClipping(x, y, height, 4096, privateArea);
                    removeClipping(x + 1, y, height, 65536, privateArea);
                } else if (direction == 3) {
                    removeClipping(x, y, height, 16384, privateArea);
                    removeClipping(x, y - 1, height, 1024, privateArea);
                }
            }
            if (type == 1 || type == 3) {
                if (direction == 0) {
                    removeClipping(x, y, height, 512, privateArea);
                    removeClipping(x - 1, y + 1, height, 8192, privateArea);
                } else if (direction == 1) {
                    removeClipping(x, y, height, 2048, privateArea);
                    removeClipping(x + 1, y + 1, height, 32768, privateArea);
                } else if (direction == 2) {
                    removeClipping(x, y, height, 8192, privateArea);
                    removeClipping(x + 1, y + 1, height, 512, privateArea);
                } else if (direction == 3) {
                    removeClipping(x, y, height, 32768, privateArea);
                    removeClipping(x - 1, y - 1, height, 2048, privateArea);
                }
            } else if (type == 2) {
                if (direction == 0) {
                    removeClipping(x, y, height, 66560, privateArea);
                    removeClipping(x - 1, y, height, 4096, privateArea);
                    removeClipping(x, y + 1, height, 16384, privateArea);
                } else if (direction == 1) {
                    removeClipping(x, y, height, 5120, privateArea);
                    removeClipping(x, y + 1, height, 16384, privateArea);
                    removeClipping(x + 1, y, height, 65536, privateArea);
                } else if (direction == 2) {
                    removeClipping(x, y, height, 20480, privateArea);
                    removeClipping(x + 1, y, height, 65536, privateArea);
                    removeClipping(x, y - 1, height, 1024, privateArea);
                } else if (direction == 3) {
                    removeClipping(x, y, height, 81920, privateArea);
                    removeClipping(x, y - 1, height, 1024, privateArea);
                    removeClipping(x - 1, y, height, 4096, privateArea);
                }
            }
        }
    }

    /**
     * Attempts to add clipping for a solid object.
     *
     * @param x
     * @param y
     * @param height
     * @param xLength
     * @param yLength
     * @param flag
     */
    private static void addClippingForSolidObject(int x, int y, int height, int xLength, int yLength, boolean flag, PrivateArea privateArea) {
        int clipping = 256;
        if (flag) {
            clipping += 0x20000;
        }
        for (int i = x; i < x + xLength; i++) {
            for (int i2 = y; i2 < y + yLength; i2++) {
                addClipping(i, i2, height, clipping, privateArea);
            }
        }
    }

    /**
     * Attempts to remove clipping for a solid object.
     *
     * @param x
     * @param y
     * @param height
     * @param xLength
     * @param yLength
     * @param flag
     */
    private static void removeClippingForSolidObject(int x, int y, int height, int xLength, int yLength, boolean flag, PrivateArea privateArea) {
        int clipping = 256;
        if (flag) {
            clipping += 0x20000;
        }

        for (int x_ = x; x_ < x + xLength; x_++) {
            for (int y_ = y; y_ < y + yLength; y_++) {
                removeClipping(x_, y_, height, clipping, privateArea);
            }
        }
    }

    /**
     * Adds an object to a region.
     *
     * @param objectId
     * @param x
     * @param y
     * @param height
     * @param type
     * @param direction
     */
    public static void addObject(int objectId, int x, int y, int height, int type, int direction) {
        final Location position = new Location(x, y, height);

        // Edge walls removal
        if (height == 0) {
            if (x >= 3092 && x <= 3094 && (y == 3513 || y == 3514 || y == 3507 || y == 3506)) {
                objectId = -1;
            }
        }

        switch (objectId) {

        }

        if (objectId == -1) {
            MapObjects.clear(position, type);
        } else {
            MapObjects.add(new GameObject(objectId, position, type, direction, null));
        }
    }

    public static void addObjectClipping(GameObject object) {
        int id = object.getId();
        int x = object.getLocation().getX();
        int y = object.getLocation().getY();
        int height = object.getLocation().getZ();
        int type = object.getType();
        int direction = object.getFace();

        if (id == -1) {
            removeClipping(x, y, height, 0x000000, object.getPrivateArea());
            return;
        }

        ObjectDefinition def = object.getDefinition();
        if (def == null) {
            return;
        }

        int xLength;
        int yLength;
        if (direction != 1 && direction != 3) {
            xLength = def.getSizeX();
            yLength = def.getSizeY();
        } else {
            yLength = def.getSizeX();
            xLength = def.getSizeY();
        }

        if (type == 22) {
            if (def.hasActions() && def.solid) {
                addClipping(x, y, height, 0x200000, object.getPrivateArea());
            }
        } else if (type >= 9) {
            if (def.solid) {
                addClippingForSolidObject(x, y, height, xLength, yLength, def.impenetrable, object.getPrivateArea());
            }
        } else if (type >= 0 && type <= 3) {
            if (def.solid) {
                addClippingForVariableObject(x, y, height, type, direction, def.impenetrable, object.getPrivateArea());
            }
        }
    }

    public static void removeObjectClipping(GameObject object) {
        int x = object.getLocation().getX();
        int y = object.getLocation().getY();
        int height = object.getLocation().getZ();
        int type = object.getType();
        int direction = object.getFace();

        if (object.getId() == -1) {
            removeClipping(x, y, height, 0x000000, object.getPrivateArea());
            return;
        }

        ObjectDefinition def = object.getDefinition();
        if (def == null) {
            return;
        }
        int xLength;
        int yLength;
        if (direction != 1 && direction != 3) {
            xLength = def.getSizeX();
            yLength = def.getSizeY();
        } else {
            yLength = def.getSizeX();
            xLength = def.getSizeY();
        }

        if (type == 22) {
            if (def.hasActions() && def.solid) {
                removeClipping(x, y, height, 0x200000, object.getPrivateArea());
            }
        } else if (type >= 9) {
            if (def.solid) {
                removeClippingForSolidObject(x, y, height, xLength, yLength, def.solid, object.getPrivateArea());
            }
        } else if (type >= 0 && type <= 3) {
            if (def.solid) {
                removeClippingForVariableObject(x, y, height, type, direction, def.solid, object.getPrivateArea());
            }
        }
    }

    /**
     * Attempts to add clipping to a region.
     *
     * @param x
     * @param y
     * @param height
     * @param shift
     */
    public static void addClipping(int x, int y, int height, int shift, PrivateArea privateArea) {
        if (privateArea != null) {
            privateArea.setClip(new Location(x, y, height), shift);
            return;
        }
        Optional<Region> r = getRegion(x, y);
        if (r.isPresent()) {
            r.get().addClip(x, y, height, shift);
        }
    }

    /**
     * Attempts to remove clipping from a region
     *
     * @param x
     * @param y
     * @param height
     * @param shift
     */
    public static void removeClipping(int x, int y, int height, int shift, PrivateArea privateArea) {
        if (privateArea != null) {
            privateArea.removeClip(new Location(x, y, height));
            return;
        }
        Optional<Region> r = getRegion(x, y);
        if (r.isPresent()) {
            r.get().removeClip(x, y, height, shift);
        }
    }

    /**
     * Attempts to get the clipping for a region.
     *
     * @param x
     * @param y
     * @param height
     * @return
     */
    public static int getClipping(int x, int y, int height, PrivateArea privateArea) {
        if (privateArea != null) {
            int privateClip = privateArea.getClip(new Location(x, y, height));
            if (privateClip != 0) {
                return privateClip;
            }
        }

        Optional<Region> r = getRegion(x, y);
        if (r.isPresent()) {
            return r.get().getClip(x, y, height);
        }
        return 0;
    }

    public static boolean wallExists(Location location, PrivateArea area, int type) {
        GameObject object = MapObjects.get(location, type, area);
        if (object != null) {
            ObjectDefinition objectDef = object.getDefinition();
            if (objectDef.name == null || objectDef.name.equals("null")) {
                return true;
            }
        }
        return false;
    }

    public static boolean wallsExist(Location location, PrivateArea area) {
        if (wallExists(location, area, 0)) {
            return true;
        }
        if (wallExists(location, area, 1)) {
            return true;
        }
        if (wallExists(location, area, 2)) {
            return true;
        }
        if (wallExists(location, area, 3)) {
            return true;
        }
        if (wallExists(location, area, 9)) {
            return true;
        }
        return false;
    }

    /**
     * Tells you if this direction is walkable.
     *
     * @param pos The destination.
     * @param direction the direction.
     * @return if the direction is walkable.
     */
    public static boolean canMove(Location pos, int direction, PrivateArea privateArea) {
        if (direction == 0) {
            return !blockedNorthWest(pos, privateArea) && !blockedNorth(pos, privateArea) && !blockedWest(pos, privateArea);
        } else if (direction == 1) {
            return !blockedNorth(pos, privateArea);
        } else if (direction == 2) {
            return !blockedNorthEast(pos, privateArea) && !blockedNorth(pos, privateArea) && !blockedEast(pos, privateArea);
        } else if (direction == 3) {
            return !blockedWest(pos, privateArea);
        } else if (direction == 4) {
            return !blockedEast(pos, privateArea);
        } else if (direction == 5) {
            return !blockedSouthWest(pos, privateArea) && !blockedSouth(pos, privateArea) && !blockedWest(pos, privateArea);
        } else if (direction == 6) {
            return !blockedSouth(pos, privateArea);
        } else if (direction == 7) {
            return !blockedSouthEast(pos, privateArea) && !blockedSouth(pos, privateArea) && !blockedEast(pos, privateArea);
        }
        return false;
    }

    public static boolean isBlockedTileSize(Location position, int size) {
        boolean isBlocked = false;
        int x = position.getX();
        int y = position.getY();
        int z = position.getZ();
        for (int i = 0; i < size; i++) {
            if (blocked(x + i, y, z) || blocked(x - i, y, z) || blocked(x, y + i, z) || blocked(x, y, z)) {
                isBlocked = true;
                break;
            }
        }
        return isBlocked;
    }

    public static boolean blockedProjectile(Location position, PrivateArea privateArea) {
        return (getClipping(position.getX(), position.getY(), position.getZ(), privateArea) & 0x20000) == 0;
    }

    public static boolean blocked(int x, int y, int z) {
        return (getClipping(x, y, z, null) & 0x1280120) != 0;
    }
    public static boolean blocked(Location pos, PrivateArea privateArea) {
        return (getClipping(pos.getX(), pos.getY(), pos.getZ(), privateArea) & 0x1280120) != 0;
    }

    public static boolean blockedNorth(Location pos, PrivateArea privateArea) {
        return (getClipping(pos.getX(), pos.getY() + 1, pos.getZ(), privateArea) & 0x1280120) != 0;
    }

    public static boolean blockedEast(Location pos, PrivateArea privateArea) {
        return (getClipping(pos.getX() + 1, pos.getY(), pos.getZ(), privateArea) & 0x1280180) != 0;
    }

    public static boolean blockedSouth(Location pos, PrivateArea privateArea) {
        return (getClipping(pos.getX(), pos.getY() - 1, pos.getZ(), privateArea) & 0x1280102) != 0;
    }

    public static boolean blockedWest(Location pos, PrivateArea privateArea) {
        return (getClipping(pos.getX() - 1, pos.getY(), pos.getZ(), privateArea) & 0x1280108) != 0;
    }

    public static boolean blockedNorthEast(Location pos, PrivateArea privateArea) {
        return (getClipping(pos.getX() + 1, pos.getY() + 1, pos.getZ(), privateArea) & 0x12801e0) != 0;
    }

    public static boolean blockedNorthWest(Location pos, PrivateArea privateArea) {
        return (getClipping(pos.getX() - 1, pos.getY() + 1, pos.getZ(), privateArea) & 0x1280138) != 0;
    }

    public static boolean blockedSouthEast(Location pos, PrivateArea privateArea) {
        return (getClipping(pos.getX() + 1, pos.getY() - 1, pos.getZ(), privateArea) & 0x1280183) != 0;
    }

    public static boolean blockedSouthWest(Location pos, PrivateArea privateArea) {
        return (getClipping(pos.getX() - 1, pos.getY() - 1, pos.getZ(), privateArea) & 0x128010e) != 0;
    }

    public static boolean canProjectileAttack(Mobile attacker, Location from, Location to) {
        Location a = from;
        Location b = to;
        if (a.getX() > b.getX()) {
            a = to;
            b = from;
        }
        return canProjectileAttack(a, b, attacker.size(), attacker.getPrivateArea());
    }

    public static boolean canProjectileAttack(Mobile from, Mobile to) {
        Location a = from.getLocation();
        Location b = to.getLocation();

        if (from.isPlayer() && to.isPlayer()) {
            // If both participants are players, line of sight is calculated from the player on the western side.
            if (a.getX() > b.getX()) {
                a = to.getLocation();
                b = from.getLocation();
            }
        } else if (to.isPlayer()) {
            a = to.getLocation();
            b = from.getLocation();
        }
        return canProjectileAttack(a, b, from.size(), from.getPrivateArea());
    }

    public static boolean canProjectileAttack(Location a, Location b, int size, PrivateArea area) {
        return canProjectileMove(a.getX(), a.getY(), b.getX(),
                b.getY(), a.getZ(), size, size, area);
    }

    public static boolean canProjectileMove(int startX, int startY, int endX, int endY, int height, int xLength,
                                            int yLength, PrivateArea privateArea) {
        int diffX = endX - startX;
        int diffY = endY - startY;
        // height %= 4;
        int max = Math.max(Math.abs(diffX), Math.abs(diffY));
        for (int ii = 0; ii < max; ii++) {
            int currentX = endX - diffX;
            int currentY = endY - diffY;
            for (int i = 0; i < xLength; i++) {
                for (int i2 = 0; i2 < yLength; i2++) {
                    if (diffX < 0 && diffY < 0) {
                        if ((getClipping(currentX + i - 1, currentY + i2 - 1, height, privateArea) & (UNLOADED_TILE
                                | /* BLOCKED_TILE | */UNKNOWN | PROJECTILE_TILE_BLOCKED | PROJECTILE_EAST_BLOCKED
                                | PROJECTILE_NORTH_EAST_BLOCKED | PROJECTILE_NORTH_BLOCKED)) != 0
                                || (getClipping(currentX + i - 1, currentY + i2, height, privateArea)
                                & (UNLOADED_TILE | /* BLOCKED_TILE | */UNKNOWN | PROJECTILE_TILE_BLOCKED
                                | PROJECTILE_EAST_BLOCKED)) != 0
                                || (getClipping(currentX + i, currentY + i2 - 1, height, privateArea)
                                & (UNLOADED_TILE | /* BLOCKED_TILE | */UNKNOWN | PROJECTILE_TILE_BLOCKED
                                | PROJECTILE_NORTH_BLOCKED)) != 0) {
                            return false;
                        }
                    } else if (diffX > 0 && diffY > 0) {
                        if ((getClipping(currentX + i + 1, currentY + i2 + 1, height, privateArea) & (UNLOADED_TILE
                                | /* BLOCKED_TILE | */UNKNOWN | PROJECTILE_TILE_BLOCKED | PROJECTILE_WEST_BLOCKED
                                | PROJECTILE_SOUTH_WEST_BLOCKED | PROJECTILE_SOUTH_BLOCKED)) != 0
                                || (getClipping(currentX + i + 1, currentY + i2, height, privateArea)
                                & (UNLOADED_TILE | /* BLOCKED_TILE | */UNKNOWN | PROJECTILE_TILE_BLOCKED
                                | PROJECTILE_WEST_BLOCKED)) != 0
                                || (getClipping(currentX + i, currentY + i2 + 1, height, privateArea)
                                & (UNLOADED_TILE | /* BLOCKED_TILE | */UNKNOWN | PROJECTILE_TILE_BLOCKED
                                | PROJECTILE_SOUTH_BLOCKED)) != 0) {
                            return false;
                        }
                    } else if (diffX < 0 && diffY > 0) {
                        if ((getClipping(currentX + i - 1, currentY + i2 + 1, height, privateArea) & (UNLOADED_TILE
                                | /* BLOCKED_TILE | */UNKNOWN | PROJECTILE_TILE_BLOCKED | PROJECTILE_SOUTH_BLOCKED
                                | PROJECTILE_SOUTH_EAST_BLOCKED | PROJECTILE_EAST_BLOCKED)) != 0
                                || (getClipping(currentX + i - 1, currentY + i2, height, privateArea)
                                & (UNLOADED_TILE | /* BLOCKED_TILE | */UNKNOWN | PROJECTILE_TILE_BLOCKED
                                | PROJECTILE_EAST_BLOCKED)) != 0
                                || (getClipping(currentX + i, currentY + i2 + 1, height, privateArea)
                                & (UNLOADED_TILE | /* BLOCKED_TILE | */UNKNOWN | PROJECTILE_TILE_BLOCKED
                                | PROJECTILE_SOUTH_BLOCKED)) != 0) {
                            return false;
                        }
                    } else if (diffX > 0 && diffY < 0) {
                        if ((getClipping(currentX + i + 1, currentY + i2 - 1, height, privateArea) & (UNLOADED_TILE
                                | /* BLOCKED_TILE | */UNKNOWN | PROJECTILE_TILE_BLOCKED | PROJECTILE_WEST_BLOCKED
                                | PROJECTILE_NORTH_BLOCKED | PROJECTILE_NORTH_WEST_BLOCKED)) != 0
                                || (getClipping(currentX + i + 1, currentY + i2, height, privateArea)
                                & (UNLOADED_TILE | /* BLOCKED_TILE | */UNKNOWN | PROJECTILE_TILE_BLOCKED
                                | PROJECTILE_WEST_BLOCKED)) != 0
                                || (getClipping(currentX + i, currentY + i2 - 1, height, privateArea)
                                & (UNLOADED_TILE | /* BLOCKED_TILE | */UNKNOWN | PROJECTILE_TILE_BLOCKED
                                | PROJECTILE_NORTH_BLOCKED)) != 0) {
                            return false;
                        }
                    } else if (diffX > 0 && diffY == 0) {
                        if ((getClipping(currentX + i + 1, currentY + i2, height, privateArea)
                                & (UNLOADED_TILE | /* BLOCKED_TILE | */UNKNOWN | PROJECTILE_TILE_BLOCKED
                                | PROJECTILE_WEST_BLOCKED)) != 0) {
                            return false;
                        }
                    } else if (diffX < 0 && diffY == 0) {
                        if ((getClipping(currentX + i - 1, currentY + i2, height, privateArea)
                                & (UNLOADED_TILE | /* BLOCKED_TILE | */UNKNOWN | PROJECTILE_TILE_BLOCKED
                                | PROJECTILE_EAST_BLOCKED)) != 0) {
                            return false;
                        }
                    } else if (diffX == 0 && diffY > 0) {
                        if ((getClipping(currentX + i, currentY + i2 + 1, height, privateArea) & (UNLOADED_TILE
                                | /*
                         * BLOCKED_TILE |
                         */UNKNOWN | PROJECTILE_TILE_BLOCKED | PROJECTILE_SOUTH_BLOCKED)) != 0) {
                            return false;
                        }
                    } else if (diffX == 0 && diffY < 0) {
                        if ((getClipping(currentX + i, currentY + i2 - 1, height, privateArea) & (UNLOADED_TILE
                                | /*
                         * BLOCKED_TILE |
                         */UNKNOWN | PROJECTILE_TILE_BLOCKED | PROJECTILE_NORTH_BLOCKED)) != 0) {
                            return false;
                        }
                    }
                }
            }
            if (diffX < 0) {
                diffX++;
            } else if (diffX > 0) {
                diffX--;
            }
            if (diffY < 0) {
                diffY++; // change
            } else if (diffY > 0) {
                diffY--;
            }
        }
        return true;
    }

    public static boolean canMove(int startX, int startY, int endX, int endY, int height, int xLength, int yLength, PrivateArea privateArea) {
        int diffX = endX - startX;
        int diffY = endY - startY;
        int max = Math.max(Math.abs(diffX), Math.abs(diffY));
        for (int ii = 0; ii < max; ii++) {
            int currentX = endX - diffX;
            int currentY = endY - diffY;
            for (int i = 0; i < xLength; i++) {
                for (int i2 = 0; i2 < yLength; i2++)
                    if (diffX < 0 && diffY < 0) {
                        if ((getClipping((currentX + i) - 1, (currentY + i2) - 1, height, privateArea) & 0x128010e) != 0
                                || (getClipping((currentX + i) - 1, currentY + i2, height, privateArea) & 0x1280108) != 0
                                || (getClipping(currentX + i, (currentY + i2) - 1, height, privateArea) & 0x1280102) != 0)
                            return false;
                    } else if (diffX > 0 && diffY > 0) {
                        if ((getClipping(currentX + i + 1, currentY + i2 + 1, height, privateArea) & 0x12801e0) != 0
                                || (getClipping(currentX + i + 1, currentY + i2, height, privateArea) & 0x1280180) != 0
                                || (getClipping(currentX + i, currentY + i2 + 1, height, privateArea) & 0x1280120) != 0)
                            return false;
                    } else if (diffX < 0 && diffY > 0) {
                        if ((getClipping((currentX + i) - 1, currentY + i2 + 1, height, privateArea) & 0x1280138) != 0
                                || (getClipping((currentX + i) - 1, currentY + i2, height, privateArea) & 0x1280108) != 0
                                || (getClipping(currentX + i, currentY + i2 + 1, height, privateArea) & 0x1280120) != 0)
                            return false;
                    } else if (diffX > 0 && diffY < 0) {
                        if ((getClipping(currentX + i + 1, (currentY + i2) - 1, height, privateArea) & 0x1280183) != 0
                                || (getClipping(currentX + i + 1, currentY + i2, height, privateArea) & 0x1280180) != 0
                                || (getClipping(currentX + i, (currentY + i2) - 1, height, privateArea) & 0x1280102) != 0)
                            return false;
                    } else if (diffX > 0 && diffY == 0) {
                        if ((getClipping(currentX + i + 1, currentY + i2, height, privateArea) & 0x1280180) != 0)
                            return false;
                    } else if (diffX < 0 && diffY == 0) {
                        if ((getClipping((currentX + i) - 1, currentY + i2, height, privateArea) & 0x1280108) != 0)
                            return false;
                    } else if (diffX == 0 && diffY > 0) {
                        if ((getClipping(currentX + i, currentY + i2 + 1, height, privateArea) & 0x1280120) != 0)
                            return false;
                    } else if (diffX == 0 && diffY < 0
                            && (getClipping(currentX + i, (currentY + i2) - 1, height, privateArea) & 0x1280102) != 0)
                        return false;

            }

            if (diffX < 0)
                diffX++;
            else if (diffX > 0)
                diffX--;
            if (diffY < 0)
                diffY++;
            else if (diffY > 0)
                diffY--;
        }

        return true;
    }

    public static boolean canMove(Location start, Location end, int xLength, int yLength, PrivateArea privateArea) {
        return canMove(start.getX(), start.getY(), end.getX(), end.getY(), start.getZ(), xLength, yLength, privateArea);
    }

    public static boolean canMove(Location position, Direction direction, int size, PrivateArea privateArea) {
        Location end = position.transform(direction.getX(), direction.getY());
        return canMove(position.getX(), position.getY(), end.getX(), end.getY(), position.getZ(), size, size, privateArea);
    }

    /**
     * Attemps to load the map files related to this region...
     */
    public static void loadMapFiles(int x, int y) {
        try {
            int regionX = x >> 3;
            int regionY = y >> 3;
            int regionId = ((regionX / 8) << 8) + (regionY / 8);
            Optional<Region> r = getRegion(regionId);
            if (!r.isPresent()) {
                return;
            }
            if (r.get().isLoaded()) {
                return;
            }
            r.get().setLoaded(true);

            // Attempt to create streams..
            byte[] oFileData = CompressionUtil.gunzip(
                    FileUtil.readFile(GameConstants.CLIPPING_DIRECTORY + "maps/" + r.get().getObjectFile() + ".dat"));
            byte[] gFileData = CompressionUtil.gunzip(
                    FileUtil.readFile(GameConstants.CLIPPING_DIRECTORY + "maps/" + r.get().getTerrainFile() + ".dat"));

            // Don't allow ground file to be invalid..
            if (gFileData == null) {
                return;
            }

            // Read values using our streams..
            Buffer groundStream = new Buffer(gFileData);
            int absX = (r.get().getRegionId() >> 8) * 64;
            int absY = (r.get().getRegionId() & 0xff) * 64;
            byte[][][] heightMap = new byte[4][64][64];
            for (int z = 0; z < 4; z++) {
                for (int tileX = 0; tileX < 64; tileX++) {
                    for (int tileY = 0; tileY < 64; tileY++) {
                        while (true) {
                            int tileType = groundStream.readUnsignedByte();
                            if (tileType == 0) {
                                break;
                            } else if (tileType == 1) {
                                groundStream.readUnsignedByte();
                                break;
                            } else if (tileType <= 49) {
                                groundStream.readUnsignedByte();
                            } else if (tileType <= 81) {
                                heightMap[z][tileX][tileY] = (byte) (tileType - 49);
                            }
                        }
                    }
                }
            }
            for (int i = 0; i < 4; i++) {
                for (int i2 = 0; i2 < 64; i2++) {
                    for (int i3 = 0; i3 < 64; i3++) {
                        if ((heightMap[i][i2][i3] & 1) == 1) {
                            int height = i;
                            if ((heightMap[1][i2][i3] & 2) == 2) {
                                height--;
                            }
                            if (height >= 0 && height <= 3) {
                                RegionManager.addClipping(absX + i2, absY + i3, height, 0x200000, null);
                            }
                        }
                    }
                }
            }
            if (oFileData != null) {
                Buffer objectStream = new Buffer(oFileData);
                int objectId = -1;
                int incr;
                while ((incr = objectStream.getUSmart()) != 0) {
                    objectId += incr;
                    int location = 0;
                    int incr2;
                    while ((incr2 = objectStream.getUSmart()) != 0) {
                        location += incr2 - 1;
                        int localX = (location >> 6 & 0x3f);
                        int localY = (location & 0x3f);
                        int height = location >> 12;
                        int hash = objectStream.readUnsignedByte();
                        int type = hash >> 2;
                        int direction = hash & 0x3;
                        if (localX < 0 || localX >= 64 || localY < 0 || localY >= 64) {
                            continue;
                        }
                        if ((heightMap[1][localX][localY] & 2) == 2) {
                            height--;
                        }

                        // Add object..

                        if (height >= 0 && height <= 3) {
                            RegionManager.addObject(objectId, absX + localX, absY + localY, height, type, direction); // Add

                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
