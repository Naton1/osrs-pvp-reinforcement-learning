package com.elvarg.game.model.movement.path;

import com.elvarg.Server;
import com.elvarg.game.collision.RegionManager;
import com.elvarg.game.content.combat.CombatConstants;
import com.elvarg.game.definition.ObjectDefinition;
import com.elvarg.game.entity.impl.Mobile;
import com.elvarg.game.entity.impl.object.GameObject;
import com.elvarg.game.model.Location;
import com.elvarg.game.model.areas.impl.PrivateArea;
import com.elvarg.game.model.commands.impl.AttackRange;
import com.elvarg.game.model.rights.PlayerRights;

import java.util.*;
import java.util.stream.Collectors;

import static com.elvarg.game.GameConstants.DEBUG_ATTACK_DISTANCE;

/**
 * @author Ynneh | 08/08/2022 - 16:36
 * <https://github.com/drhenny>
 */
public class PathFinder {

    public static final int WEST = 0x1280108, EAST = 0x1280180, SOUTH = 0x1280102,
            NORTH = 0x1280120, SOUTHEAST = 0x1280183, SOUTHWEST = 0x128010e,
            NORTHEAST = 0x12801e0, NORTHWEST = 0x1280138;

    /**
     * A list of all the deltas for distances 1-10 from any given tile.
     */
    private static final Map<Integer, int[][]> TILE_DISTANCE_DELTAS = new HashMap<Integer, int[][]>() {{
        // Deltas which are exactly 1 squares away
        put(1, new int[][]{{-1, -1, 0}, {-1, 0, 0}, {-1, 1, 0}, {0, -1, 0}, {0, 1, 0}, {1, -1, 0}, {1, 0, 0}, {1, 1, 0}});
        // Deltas which are exactly 2 squares away
        put(2, new int[][]{{-2, -2, 0}, {-2, -1, 0}, {-2, 0, 0}, {-2, 1, 0}, {-2, 2, 0}, {-1, -2, 0}, {-1, 2, 0},
                {0, -2, 0}, {0, 2, 0}, {1, -2, 0}, {1, 2, 0}, {2, -2, 0}, {2, -1, 0}, {2, 0, 0}, {2, 1, 0}, {2, 2, 0}});
        // Deltas which are exactly 3 squares away
        put(3, new int[][]{{-3, -3, 0}, {-3, -2, 0}, {-3, -1, 0}, {-3, 0, 0}, {-3, 1, 0}, {-3, 2, 0}, {-3, 3, 0},
                {-2, -3, 0}, {-2, 3, 0}, {-1, -3, 0}, {-1, 3, 0}, {0, -3, 0}, {0, 3, 0}, {1, -3, 0}, {1, 3, 0},
                {2, -3, 0}, {2, 3, 0}, {3, -3, 0}, {3, -2, 0}, {3, -1, 0}, {3, 0, 0}, {3, 1, 0}, {3, 2, 0}, {3, 3, 0}});
        // Deltas which are exactly 4 squares away
        put(4, new int[][]{{-4, -4, 0}, {-4, -3, 0}, {-4, -2, 0}, {-4, -1, 0}, {-4, 0, 0}, {-4, 1, 0}, {-4, 2, 0},
                {-4, 3, 0}, {-4, 4, 0}, {-3, -4, 0}, {-3, 4, 0}, {-2, -4, 0}, {-2, 4, 0}, {-1, -4, 0}, {-1, 4, 0},
                {0, -4, 0}, {0, 4, 0}, {1, -4, 0}, {1, 4, 0}, {2, -4, 0}, {2, 4, 0}, {3, -4, 0}, {3, 4, 0},
                {4, -4, 0}, {4, -3, 0}, {4, -2, 0}, {4, -1, 0}, {4, 0, 0}, {4, 1, 0}, {4, 2, 0}, {4, 3, 0}, {4, 4, 0}});
        // Deltas which are exactly 5 squares away
        put(5, new int[][]{{-5, -5, 0}, {-5, -4, 0}, {-5, -3, 0}, {-5, -2, 0}, {-5, -1, 0}, {-5, 0, 0}, {-5, 1, 0},
                {-5, 2, 0}, {-5, 3, 0}, {-5, 4, 0}, {-5, 5, 0}, {-4, -5, 0}, {-4, 5, 0}, {-3, -5, 0}, {-3, 5, 0},
                {-2, -5, 0}, {-2, 5, 0}, {-1, -5, 0}, {-1, 5, 0}, {0, -5, 0}, {0, 5, 0}, {1, -5, 0}, {1, 5, 0},
                {2, -5, 0}, {2, 5, 0}, {3, -5, 0}, {3, 5, 0}, {4, -5, 0}, {4, 5, 0}, {5, -5, 0}, {5, -4, 0}, {5, -3, 0},
                {5, -2, 0}, {5, -1, 0}, {5, 0, 0}, {5, 1, 0}, {5, 2, 0}, {5, 3, 0}, {5, 4, 0}, {5, 5, 0}});
        // Deltas which are exactly 6 squares away
        put(6, new int[][]{{-6, -6, 0}, {-6, -5, 0}, {-6, -4, 0}, {-6, -3, 0}, {-6, -2, 0}, {-6, -1, 0}, {-6, 0, 0},
                {-6, 1, 0}, {-6, 2, 0}, {-6, 3, 0}, {-6, 4, 0}, {-6, 5, 0}, {-6, 6, 0}, {-5, -6, 0}, {-5, 6, 0},
                {-4, -6, 0}, {-4, 6, 0}, {-3, -6, 0}, {-3, 6, 0}, {-2, -6, 0}, {-2, 6, 0}, {-1, -6, 0}, {-1, 6, 0},
                {0, -6, 0}, {0, 6, 0}, {1, -6, 0}, {1, 6, 0}, {2, -6, 0}, {2, 6, 0}, {3, -6, 0}, {3, 6, 0}, {4, -6, 0},
                {4, 6, 0}, {5, -6, 0}, {5, 6, 0}, {6, -6, 0}, {6, -5, 0}, {6, -4, 0}, {6, -3, 0}, {6, -2, 0},
                {6, -1, 0}, {6, 0, 0}, {6, 1, 0}, {6, 2, 0}, {6, 3, 0}, {6, 4, 0}, {6, 5, 0}, {6, 6, 0}});
        // Deltas which are exactly 7 squares away
        put(7, new int[][]{{-7, -7, 0}, {-7, -6, 0}, {-7, -5, 0}, {-7, -4, 0}, {-7, -3, 0}, {-7, -2, 0}, {-7, -1, 0},
                {-7, 0, 0}, {-7, 1, 0}, {-7, 2, 0}, {-7, 3, 0}, {-7, 4, 0}, {-7, 5, 0}, {-7, 6, 0}, {-7, 7, 0},
                {-6, -7, 0}, {-6, 7, 0}, {-5, -7, 0}, {-5, 7, 0}, {-4, -7, 0}, {-4, 7, 0}, {-3, -7, 0}, {-3, 7, 0},
                {-2, -7, 0}, {-2, 7, 0}, {-1, -7, 0}, {-1, 7, 0}, {0, -7, 0}, {0, 7, 0}, {1, -7, 0}, {1, 7, 0},
                {2, -7, 0}, {2, 7, 0}, {3, -7, 0}, {3, 7, 0}, {4, -7, 0}, {4, 7, 0}, {5, -7, 0}, {5, 7, 0},
                {6, -7, 0}, {6, 7, 0}, {7, -7, 0}, {7, -6, 0}, {7, -5, 0}, {7, -4, 0}, {7, -3, 0}, {7, -2, 0},
                {7, -1, 0}, {7, 0, 0}, {7, 1, 0}, {7, 2, 0}, {7, 3, 0}, {7, 4, 0}, {7, 5, 0}, {7, 6, 0}, {7, 7, 0}});
        // Deltas which are exactly 8 squares away
        put(8, new int[][]{{-8, -8, 0}, {-8, -7, 0}, {-8, -6, 0}, {-8, -5, 0}, {-8, -4, 0}, {-8, -3, 0}, {-8, -2, 0},
                {-8, -1, 0}, {-8, 0, 0}, {-8, 1, 0}, {-8, 2, 0}, {-8, 3, 0}, {-8, 4, 0}, {-8, 5, 0}, {-8, 6, 0},
                {-8, 7, 0}, {-8, 8, 0}, {-7, -8, 0}, {-7, 8, 0}, {-6, -8, 0}, {-6, 8, 0}, {-5, -8, 0}, {-5, 8, 0},
                {-4, -8, 0}, {-4, 8, 0}, {-3, -8, 0}, {-3, 8, 0}, {-2, -8, 0}, {-2, 8, 0}, {-1, -8, 0}, {-1, 8, 0},
                {0, -8, 0}, {0, 8, 0}, {1, -8, 0}, {1, 8, 0}, {2, -8, 0}, {2, 8, 0}, {3, -8, 0}, {3, 8, 0}, {4, -8, 0},
                {4, 8, 0}, {5, -8, 0}, {5, 8, 0}, {6, -8, 0}, {6, 8, 0}, {7, -8, 0}, {7, 8, 0}, {8, -8, 0}, {8, -7, 0},
                {8, -6, 0}, {8, -5, 0}, {8, -4, 0}, {8, -3, 0}, {8, -2, 0}, {8, -1, 0}, {8, 0, 0}, {8, 1, 0}, {8, 2, 0},
                {8, 3, 0}, {8, 4, 0}, {8, 5, 0}, {8, 6, 0}, {8, 7, 0}, {8, 8, 0}});
        // Deltas which are exactly 9 squares away
        put(9, new int[][]{{-9, -9, 0}, {-9, -8, 0}, {-9, -7, 0}, {-9, -6, 0}, {-9, -5, 0}, {-9, -4, 0}, {-9, -3, 0},
                {-9, -2, 0}, {-9, -1, 0}, {-9, 0, 0}, {-9, 1, 0}, {-9, 2, 0}, {-9, 3, 0}, {-9, 4, 0}, {-9, 5, 0},
                {-9, 6, 0}, {-9, 7, 0}, {-9, 8, 0}, {-9, 9, 0}, {-8, -9, 0}, {-8, 9, 0}, {-7, -9, 0}, {-7, 9, 0},
                {-6, -9, 0}, {-6, 9, 0}, {-5, -9, 0}, {-5, 9, 0}, {-4, -9, 0}, {-4, 9, 0}, {-3, -9, 0}, {-3, 9, 0},
                {-2, -9, 0}, {-2, 9, 0}, {-1, -9, 0}, {-1, 9, 0}, {0, -9, 0}, {0, 9, 0}, {1, -9, 0}, {1, 9, 0},
                {2, -9, 0}, {2, 9, 0}, {3, -9, 0}, {3, 9, 0}, {4, -9, 0}, {4, 9, 0}, {5, -9, 0}, {5, 9, 0}, {6, -9, 0},
                {6, 9, 0}, {7, -9, 0}, {7, 9, 0}, {8, -9, 0}, {8, 9, 0}, {9, -9, 0}, {9, -8, 0}, {9, -7, 0}, {9, -6, 0},
                {9, -5, 0}, {9, -4, 0}, {9, -3, 0}, {9, -2, 0}, {9, -1, 0}, {9, 0, 0}, {9, 1, 0}, {9, 2, 0}, {9, 3, 0},
                {9, 4, 0}, {9, 5, 0}, {9, 6, 0}, {9, 7, 0}, {9, 8, 0}, {9, 9, 0}});
        // Deltas which are exactly 10 squares away
        put(10, new int[][]{{-10, -10, 0}, {-10, -9, 0}, {-10, -8, 0}, {-10, -7, 0}, {-10, -6, 0}, {-10, -5, 0},
                {-10, -4, 0}, {-10, -3, 0}, {-10, -2, 0}, {-10, -1, 0}, {-10, 0, 0}, {-10, 1, 0}, {-10, 2, 0}, {-10, 3, 0},
                {-10, 4, 0}, {-10, 5, 0}, {-10, 6, 0}, {-10, 7, 0}, {-10, 8, 0}, {-10, 9, 0}, {-10, 10, 0}, {-9, -10, 0},
                {-9, 10, 0}, {-8, -10, 0}, {-8, 10, 0}, {-7, -10, 0}, {-7, 10, 0}, {-6, -10, 0}, {-6, 10, 0}, {-5, -10, 0},
                {-5, 10, 0}, {-4, -10, 0}, {-4, 10, 0}, {-3, -10, 0}, {-3, 10, 0}, {-2, -10, 0}, {-2, 10, 0}, {-1, -10, 0},
                {-1, 10, 0}, {0, -10, 0}, {0, 10, 0}, {1, -10, 0}, {1, 10, 0}, {2, -10, 0}, {2, 10, 0}, {3, -10, 0},
                {3, 10, 0}, {4, -10, 0}, {4, 10, 0}, {5, -10, 0}, {5, 10, 0}, {6, -10, 0}, {6, 10, 0}, {7, -10, 0},
                {7, 10, 0}, {8, -10, 0}, {8, 10, 0}, {9, -10, 0}, {9, 10, 0}, {10, -10, 0}, {10, -9, 0}, {10, -8, 0},
                {10, -7, 0}, {10, -6, 0}, {10, -5, 0}, {10, -4, 0}, {10, -3, 0}, {10, -2, 0}, {10, -1, 0}, {10, 0, 0},
                {10, 1, 0}, {10, 2, 0}, {10, 3, 0}, {10, 4, 0}, {10, 5, 0}, {10, 6, 0}, {10, 7, 0},
                {10, 8, 0}, {10, 9, 0}, {10, 10, 0}});
    }};


    public final static boolean isInDiagonalBlock(Location attacker, Location attacked) {
        return attacked.getX() - 1 == attacker.getX() && attacked.getY() + 1 == attacker.getY()
                || attacker.getX() - 1 == attacked.getX() && attacker.getY() + 1 == attacked.getY()
                || attacked.getX() + 1 == attacker.getX() && attacked.getY() - 1 == attacker.getY()
                || attacker.getX() + 1 == attacked.getX() && attacker.getY() - 1 == attacked.getY()
                || attacked.getX() + 1 == attacker.getX() && attacked.getY() + 1 == attacker.getY()
                || attacker.getX() + 1 == attacked.getX() && attacker.getY() + 1 == attacked.getY();
    }

    public final static boolean isDiagonalLocation(Mobile att, Mobile def) {
        Location attacker = att.getLocation().clone();
        Location attacked = def.getLocation().clone();
        boolean isDia = attacker.getX() - 1 == attacked.getX() && attacker.getY() + 1 == attacked.getY()//top left
                || attacker.getX() + 1 == attacked.getX() && attacker.getY() - 1 == attacked.getY()//bottom right
                || attacker.getX() + 1 == attacked.getX() && attacker.getY() + 1 == attacked.getY()//top right
                || attacker.getX() - 1 == attacked.getX() && attacker.getY() - 1 == attacked.getY()//bottom right
                ;
        return isDia;
    }

    public static void calculateCombatRoute(Mobile player, Mobile target) {
        calculateRoute(player, 0, target.getLocation().getX(), target.getLocation().getY(), 1, 1, 0, 0, false);
        player.setMobileInteraction(target);
    }

    public static void calculateEntityRoute(Mobile player, int destX, int destY) {
        calculateRoute(player, 0, destX, destY, 1, 1, 0, 0, false);
    }

    public static void calculateWalkRoute(Mobile player, int destX, int destY) {
        calculateRoute(player, 0, destX, destY, 0, 0, 0, 0, true);
    }

    public static void calculateObjectRoute(Mobile entity, GameObject object) {
        int objectX = object.getLocation().getX();

        int objectY = object.getLocation().getY();

        int type = object.getType();

        int id = object.getId();

        int direction = object.getFace();

        if (type == 10 || type == 11 || type == 22) {
            int xLength, yLength;
            ObjectDefinition def = ObjectDefinition.forId(id);
            if (direction == 0 || direction == 2) {
                yLength = def.objectSizeX;
                xLength = def.objectSizeY;
            } else {
                yLength = def.objectSizeY;
                xLength = def.objectSizeX;
            }
            int blockingMask = def.blockingMask;

            if (direction != 0) {
                blockingMask = (blockingMask << direction & 0xf) + (blockingMask >> 4 - direction);
            }

            calculateRoute(entity, 0, objectX, objectY, xLength, yLength, 0, blockingMask, false);
            return;
        }

        calculateRoute(entity, type + 1, objectX, objectY, 0, 0, direction, 0, false);
    }

    /**
     * Returns the best Location(x, y) to attack the defender from, from a preferred distance.
     *
     * @param attacker
     * @param defender
     * @param distance
     * @return
     */
    public static Location getClosestAttackableTile(Mobile attacker, Mobile defender, int distance) {
        PrivateArea privateArea = attacker.getPrivateArea();
        Location targetLocation = defender.getLocation();

        if (distance == 1) {
            final int size = attacker.size();
            final int followingSize = defender.size();
            final Location current = attacker.getLocation();

            List<Location> tiles = new ArrayList<>();
            List<Location> outerTiles = Arrays.stream(defender.outterTiles()).toList();

            if (DEBUG_ATTACK_DISTANCE && attacker.isPlayer() && attacker.getAsPlayer().getRights() == PlayerRights.DEVELOPER) {
                // If we're debugging attack range
                outerTiles.forEach(t -> attacker.getAsPlayer().getPacketSender().sendGraphic(AttackRange.PURPLE_GLOW, t));
            }

            for (Location tile : outerTiles) {
                if (!RegionManager.canMove(attacker.getLocation(), tile, size, size, privateArea) || RegionManager.blocked(tile, privateArea)) {
                    continue;
                }
                // Projectile attack
                if (attacker.useProjectileClipping() && !RegionManager.canProjectileAttack(tile, targetLocation, size, privateArea)) {
                    continue;
                }
                tiles.add(tile);
            }

            if (!tiles.isEmpty()) {
                tiles.sort((l1, l2) -> {
                    int distance1 = l1.getDistance(current);
                    int distance2 = l2.getDistance(current);
                    int delta = (distance1 - distance2);

                    // Make sure we don't pick a diagonal tile if we're a small entity and have to
                    // attack closely (melee).
                    if (distance1 == distance2 && size == 1 && followingSize == 1) {
                        if (l1.isPerpendicularTo(current)) {
                            return -1;
                        } else if (l2.isPerpendicularTo(current)) {
                            return 1;
                        }
                    }

                    return delta;
                });

                return tiles.get(0);
            }
        }

        Optional<Location> tile = Optional.empty();

        // Starting from the max distance, try to find a suitable tile to attack from
        while (tile.isEmpty()) {
            // Fetch the circumference of the closest attackable tiles to the target
            List<Location> possibleTiles = getTilesForDistance(targetLocation, distance);

            if (DEBUG_ATTACK_DISTANCE && attacker.isPlayer() && attacker.getAsPlayer().getRights() == PlayerRights.DEVELOPER) {
                // If we're debugging attack range
                possibleTiles.forEach(t -> attacker.getAsPlayer().getPacketSender().sendGraphic(AttackRange.PURPLE_GLOW, t));
            }

            tile = possibleTiles.stream()
            // Filter out any tiles which are clipped
            .filter(t -> !RegionManager.blocked(t, attacker.getPrivateArea()))
            // Filter out any tiles which projectiles are blocked from (i.e. tree is in the way)
            .filter(t -> RegionManager.canProjectileAttack(attacker, t, targetLocation))
            // Find the tile closest to the attacker
            .min(Comparator.comparing(attacker.getLocation()::getDistance));

            if (distance == 1) {
                // We've reached the closest attackable tile, break out of the loop as we can't get any closer
                break;
            } else {
                // Check 1 square closer if we don't have any valid tiles at this distance
                distance = Math.max(distance-1, 1);
            }
        }

        if (tile.isEmpty()) {
            attacker.sendMessage("I can't reach that.");
            return null;
        }

        return tile.get();
    }

    /**
     * Gets tile Locations exactly a given distance away from a center point.
     *
     * @param center The centre of the circle, or the target.
     * @param distance The radius, or the max distance.
     * @return {List<Location>}
     */
    public static List<Location> getTilesForDistance(Location center, int distance) {
        int[][] deltas = TILE_DISTANCE_DELTAS.get(Math.min(distance, CombatConstants.MAX_ATTACK_DISTANCE));

        return Arrays.stream(deltas).map((d) -> center.clone().translate(d[0], d[1])).collect(Collectors.toList());
    }

    public static int calculateRoute(Mobile entity, int size, int destX, int destY, int xLength, int yLength, int direction, int blockingMask, boolean basicPather) {

        /** RS Protocol **/
        byte byte0 = 104;
        byte byte1 = 104;

        int[][] directions = new int[104][104];

        int[][] distanceValues = new int[104][104];

        int[] routeStepsX = new int[4096];

        int[] routeStepsY = new int[4096];

        int anInt1264 = 0;

        int anInt1288 = 0;

        entity.getMovementQueue().lastDestX = destX;

        entity.getMovementQueue().lastDestY = destY;
        /** RS Protocol **/
        for (int l2 = 0; l2 < 104; l2++) {
            for (int i3 = 0; i3 < 104; i3++) {
                directions[l2][i3] = 0;
                distanceValues[l2][i3] = 0x5f5e0ff;
            }
        }

        /** Required for based on client **/
        int localX = entity.getLocation().getLocalX();
        int localY = entity.getLocation().getLocalY();
        /** Stored LocalX/Y into another temp list **/
        int baseX = localX;
        int baseY = localY;
        /** DestinationX for LocalX **/
        var destinationX = destX - (entity.getLocation().getRegionX() << 3);
        /** DestinationY for LocalY **/
        var destinationY = destY - (entity.getLocation().getRegionY() << 3);
        /** RS Protocol **/
        directions[localX][localY] = 99;
        distanceValues[localX][localY] = 0;
        /** Size of the 2nd queue **/
        int tail = 0;
        /** Size of the 1st queue **/
        int queueIndex = 0;
        /** Set in order to loop to find best path **/
        routeStepsX[tail] = localX;
        routeStepsY[tail++] = localY;
        /** Required for custom object walk-to actions. **/
        entity.getMovementQueue().setRoute(false);
        /** Size of the main queue **/
        int queueSizeX = routeStepsX.length;
        /** Entities height **/
        int height = entity.getLocation().getZ();
        /** Private Area **/
        PrivateArea area = entity.getPrivateArea();
        /** Steps taken to get to best route **/
        int steps = 0;
        /** Loops and checks flags for best route to destination. **/
        while (queueIndex != tail) {
            baseX = routeStepsX[queueIndex];
            baseY = routeStepsY[queueIndex];
            queueIndex = (queueIndex + 1) % queueSizeX;
            int absoluteX = (entity.getLocation().getRegionX() << 3) + baseX;
            int absoluteY = (entity.getLocation().getRegionY() << 3) + baseY;

            if (baseX == destinationX && baseY == destinationY) {
                entity.getMovementQueue().setRoute(true);
                entity.getMovementQueue().setPathX(baseX).setPathY(baseY);
                Server.logDebug("Already at destination, breaking loop");
                break;
            }

            if (size != 0) {
                /** Used for basic walking and other packet interactions also size 10 **/
                if ((size < 5 || size == 10) && defaultRoutePath(entity, destinationX, baseX, baseY, direction, size - 1, destinationY)) {
                    Server.logDebug("Using normal entity pathing..");
                    entity.getMovementQueue().setRoute(true);
                    break;
                }
                /** Used for larger entities e.g corp/kbd ect **/
                if (size < 10 && largeRoutePath(entity, destinationX, destinationY, baseY, size - 1, direction, baseX)) {
                    Server.logDebug("Using larger Size Pathing..");
                    entity.getMovementQueue().setRoute(true);
                    break;
                }
            }
            /** Used for Calculating best route to object based on sizeX/Y **/
            if (yLength != 0 && xLength != 0 && sizeRoutePath(entity, destinationY, destinationX, baseX, xLength, blockingMask, yLength, baseY)) {
                Server.logDebug("Using size based pathing..");
                entity.getMovementQueue().setRoute(true);
                break;
            }

            /** Cost for the distance **/
            int priceValue = distanceValues[baseX][baseY] + 1;

            if (baseX > 0 && directions[baseX - 1][baseY] == 0 && (RegionManager.getClipping(absoluteX - 1, absoluteY, height, area) & WEST) == 0) {
                routeStepsX[tail] = baseX - 1;
                routeStepsY[tail] = baseY;
                tail = (tail + 1) % queueSizeX;
                directions[baseX - 1][baseY] = 2;
                distanceValues[baseX - 1][baseY] = priceValue;
            }

            if (baseX < byte0 - 1 && directions[baseX + 1][baseY] == 0 && (RegionManager.getClipping(absoluteX + 1, absoluteY, height, area) & EAST) == 0) {
                routeStepsX[tail] = baseX + 1;
                routeStepsY[tail] = baseY;
                tail = (tail + 1) % queueSizeX;
                directions[baseX + 1][baseY] = 8;
                distanceValues[baseX + 1][baseY] = priceValue;
            }
            if (baseY > 0 && directions[baseX][baseY - 1] == 0 && (RegionManager.getClipping(absoluteX, absoluteY - 1, height, area) & SOUTH) == 0) {
                routeStepsX[tail] = baseX;
                routeStepsY[tail] = baseY - 1;
                tail = (tail + 1) % queueSizeX;
                directions[baseX][baseY - 1] = 1;
                distanceValues[baseX][baseY - 1] = priceValue;
            }
            if (baseY < byte1 - 1 && directions[baseX][baseY + 1] == 0 && (RegionManager.getClipping(absoluteX, absoluteY + 1, height, area) & NORTH) == 0) {
                routeStepsX[tail] = baseX;
                routeStepsY[tail] = baseY + 1;
                tail = (tail + 1) % queueSizeX;
                directions[baseX][baseY + 1] = 4;
                distanceValues[baseX][baseY + 1] = priceValue;
            }
            if (baseX > 0 && baseY > 0 && directions[baseX - 1][baseY - 1] == 0 && (RegionManager.getClipping(absoluteX - 1, absoluteY - 1, height, area) & SOUTHWEST) == 0
                    && (RegionManager.getClipping(absoluteX - 1, absoluteY, height, area) & WEST) == 0 && (RegionManager.getClipping(absoluteX, absoluteY - 1, height, area) & SOUTH) == 0) {
                routeStepsX[tail] = baseX - 1;
                routeStepsY[tail] = baseY - 1;
                tail = (tail + 1) % queueSizeX;
                directions[baseX - 1][baseY - 1] = 3;
                distanceValues[baseX - 1][baseY - 1] = priceValue;
            }

            if (baseX < byte0 - 1 && baseY > 0 && directions[baseX + 1][baseY - 1] == 0
                    && (RegionManager.getClipping(absoluteX + 1, absoluteY - 1, height, area) & SOUTHEAST) == 0 && (RegionManager.getClipping(absoluteX + 1, absoluteY, height, area) & EAST) == 0
                    && (RegionManager.getClipping(absoluteX, absoluteY - 1, height, area) & SOUTH) == 0) {
                routeStepsX[tail] = baseX + 1;
                routeStepsY[tail] = baseY - 1;
                tail = (tail + 1) % queueSizeX;
                directions[baseX + 1][baseY - 1] = 9;
                distanceValues[baseX + 1][baseY - 1] = priceValue;
            }

            if (baseX > 0 && baseY < byte1 - 1 && directions[baseX - 1][baseY + 1] == 0
                    && (RegionManager.getClipping(absoluteX - 1, absoluteY + 1, height, area) & NORTHWEST) == 0 && (RegionManager.getClipping(absoluteX - 1, absoluteY, height, area) & WEST) == 0
                    && (RegionManager.getClipping(absoluteX, absoluteY + 1, height, area) & NORTH) == 0) {
                routeStepsX[tail] = baseX - 1;
                routeStepsY[tail] = baseY + 1;
                tail = (tail + 1) % queueSizeX;
                directions[baseX - 1][baseY + 1] = 6;
                distanceValues[baseX - 1][baseY + 1] = priceValue;
            }
            if (baseX < byte0 - 1 && baseY < byte1 - 1 && directions[baseX + 1][baseY + 1] == 0
                    && (RegionManager.getClipping(absoluteX + 1, absoluteY + 1, height, area) & NORTHEAST) == 0 && (RegionManager.getClipping(absoluteX + 1, absoluteY, height, area) & EAST) == 0
                    && (RegionManager.getClipping(absoluteX, absoluteY + 1, height, area) & NORTH) == 0) {
                routeStepsX[tail] = baseX + 1;
                routeStepsY[tail] = baseY + 1;
                tail = (tail + 1) % queueSizeX;
                directions[baseX + 1][baseY + 1] = 12;
                distanceValues[baseX + 1][baseY + 1] = priceValue;
            }
        }
        anInt1264 = 0;

        if (!entity.getMovementQueue().hasRoute()) {
            if (basicPather) {
                int cost = 100;
                for (int range = 1; range < 5; range++) {
                    for (int xOffset = destinationX - range; xOffset <= destinationX + range; xOffset++) {
                        for (int yOffset = destinationY - range; yOffset <= destinationY + range; yOffset++) {
                            if (xOffset >= 0 && yOffset >= 0 && xOffset < 104 && yOffset < 104 && distanceValues[xOffset][yOffset] < cost) {
                                cost = distanceValues[xOffset][yOffset];
                                baseX = xOffset;
                                baseY = yOffset;
                                anInt1264 = 1;
                                entity.getMovementQueue().setRoute(true);
                            }
                        }
                    }
                    if (entity.getMovementQueue().hasRoute())
                        break;
                }
            }
            if (!entity.getMovementQueue().hasRoute()) {
                Server.logDebug("error.. no path found... path probably not reachable.");
                return -1;
            }
        }

        queueIndex = 0;
        routeStepsX[queueIndex] = baseX;
        routeStepsY[queueIndex++] = baseY;

        int l5;
        for (int dirc = l5 = directions[baseX][baseY]; baseX != localX || baseY != localY; dirc = directions[baseX][baseY]) {
            if (dirc != l5) {
                l5 = dirc;
                routeStepsX[queueIndex] = baseX;
                routeStepsY[queueIndex++] = baseY;
            }
            if ((dirc & 2) != 0)
                baseX++;
            else if ((dirc & 8) != 0)
                baseX--;
            if ((dirc & 1) != 0)
                baseY++;
            else if ((dirc & 4) != 0)
                baseY--;
        }

        if (queueIndex > 0) {

            if (queueIndex > 25)
                queueIndex = 25;

            while (queueIndex-- > 0) {
                int absX = entity.getLocation().getRegionX() * 8 + routeStepsX[queueIndex];
                int absY = entity.getLocation().getRegionY() * 8 + routeStepsY[queueIndex];
                entity.getMovementQueue().addStep(new Location(absX, absY, height));
                steps++;
            }
        }
        return steps;
    }

    private static boolean defaultRoutePath(Mobile entity, int destX, int baseX, int baseY, int direction, int size, int destY) {
        if (baseX == destX && baseY == destY) {
            entity.getMovementQueue().setPathX(baseX).setPathY(baseY);
            return true;
        }

        int absX = (entity.getLocation().getRegionX() << 3) + baseX;

        int absY = (entity.getLocation().getRegionY() << 3) + baseY;

        int height = entity.getLocation().getZ();

        PrivateArea area = entity.getPrivateArea();

        baseX -= 0;
        baseY -= 0;
        destX -= 0;
        destY -= 0;
        if (size == 0)
            if (direction == 0) {
                if (baseX == destX - 1 && baseY == destY) {
                    entity.getMovementQueue().setPathX(baseX).setPathY(baseY);
                    return true;
                }
                if (baseX == destX && baseY == destY + 1
                        && (RegionManager.getClipping(absX, absY, height, area) & 0x1280120) == 0) {
                    entity.getMovementQueue().setPathX(baseX).setPathY(baseY);
                    return true;
                }
                if (baseX == destX && baseY == destY - 1
                        && (RegionManager.getClipping(absX, absY, height, area) & 0x1280102) == 0) {
                    entity.getMovementQueue().setPathX(baseX).setPathY(baseY);
                    return true;
                }
            } else if (direction == 1) {
                if (baseX == destX && baseY == destY + 1) {
                    entity.getMovementQueue().setPathX(baseX).setPathY(baseY);
                    return true;
                }
                if (baseX == destX - 1 && baseY == destY
                        && (RegionManager.getClipping(absX, absY, height, area) & 0x1280108) == 0) {
                    entity.getMovementQueue().setPathX(baseX).setPathY(baseY);
                    return true;
                }
                if (baseX == destX + 1 && baseY == destY
                        && (RegionManager.getClipping(absX, absY, height, area) & 0x1280180) == 0) {
                    entity.getMovementQueue().setPathX(baseX).setPathY(baseY);
                    return true;
                }
            } else if (direction == 2) {
                if (baseX == destX + 1 && baseY == destY) {
                    entity.getMovementQueue().setPathX(baseX).setPathY(baseY);
                    return true;
                }
                if (baseX == destX && baseY == destY + 1
                        && (RegionManager.getClipping(absX, absY, height, area) & 0x1280120) == 0) {
                    entity.getMovementQueue().setPathX(baseX).setPathY(baseY);
                    return true;
                }
                if (baseX == destX && baseY == destY - 1
                        && (RegionManager.getClipping(absX, absY, height, area) & 0x1280102) == 0) {
                    entity.getMovementQueue().setPathX(baseX).setPathY(baseY);
                    return true;
                }
            } else if (direction == 3) {
                if (baseX == destX && baseY == destY - 1) {
                    entity.getMovementQueue().setPathX(baseX).setPathY(baseY);
                    return true;
                }
                if (baseX == destX - 1 && baseY == destY
                        && (RegionManager.getClipping(absX, absY, height, area) & 0x1280108) == 0) {
                    entity.getMovementQueue().setPathX(baseX).setPathY(baseY);
                    return true;
                }
                if (baseX == destX + 1 && baseY == destY
                        && (RegionManager.getClipping(absX, absY, height, area) & 0x1280180) == 0) {
                    entity.getMovementQueue().setPathX(baseX).setPathY(baseY);
                    return true;
                }
            }
        if (size == 2)
            if (direction == 0) {
                if (baseX == destX - 1 && baseY == destY) {
                    entity.getMovementQueue().setPathX(baseX).setPathY(baseY);
                    return true;
                }
                if (baseX == destX && baseY == destY + 1) {
                    entity.getMovementQueue().setPathX(baseX).setPathY(baseY);
                    return true;
                }
                if (baseX == destX + 1 && baseY == destY
                        && (RegionManager.getClipping(absX, absY, height, area) & 0x1280180) == 0) {
                    entity.getMovementQueue().setPathX(baseX).setPathY(baseY);
                    return true;
                }
                if (baseX == destX && baseY == destY - 1
                        && (RegionManager.getClipping(absX, absY, height, area) & 0x1280102) == 0) {
                    entity.getMovementQueue().setPathX(baseX).setPathY(baseY);
                    return true;
                }
            } else if (direction == 1) {
                if (baseX == destX - 1 && baseY == destY
                        && (RegionManager.getClipping(absX, absY, height, area) & 0x1280108) == 0) {
                    entity.getMovementQueue().setPathX(baseX).setPathY(baseY);
                    return true;
                }
                if (baseX == destX && baseY == destY + 1) {
                    entity.getMovementQueue().setPathX(baseX).setPathY(baseY);
                    return true;
                }
                if (baseX == destX + 1 && baseY == destY) {
                    entity.getMovementQueue().setPathX(baseX).setPathY(baseY);
                    return true;
                }
                if (baseX == destX && baseY == destY - 1
                        && (RegionManager.getClipping(absX, absY, height, area) & 0x1280102) == 0) {
                    entity.getMovementQueue().setPathX(baseX).setPathY(baseY);
                    return true;
                }
            } else if (direction == 2) {
                if (baseX == destX - 1 && baseY == destY
                        && (RegionManager.getClipping(absX, absY, height, area) & 0x1280108) == 0) {
                    entity.getMovementQueue().setPathX(baseX).setPathY(baseY);
                    return true;
                }
                if (baseX == destX && baseY == destY + 1
                        && (RegionManager.getClipping(absX, absY, height, area) & 0x1280120) == 0) {
                    entity.getMovementQueue().setPathX(baseX).setPathY(baseY);
                    return true;
                }
                if (baseX == destX + 1 && baseY == destY) {
                    entity.getMovementQueue().setPathX(baseX).setPathY(baseY);
                    return true;
                }
                if (baseX == destX && baseY == destY - 1) {
                    entity.getMovementQueue().setPathX(baseX).setPathY(baseY);
                    return true;
                }
            } else if (direction == 3) {
                if (baseX == destX - 1 && baseY == destY) {
                    entity.getMovementQueue().setPathX(baseX).setPathY(baseY);
                    return true;
                }
                if (baseX == destX && baseY == destY + 1
                        && (RegionManager.getClipping(absX, absY, height, area) & 0x1280120) == 0) {
                    entity.getMovementQueue().setPathX(baseX).setPathY(baseY);
                    return true;
                }
                if (baseX == destX + 1 && baseY == destY
                        && (RegionManager.getClipping(absX, absY, height, area) & 0x1280180) == 0) {
                    entity.getMovementQueue().setPathX(baseX).setPathY(baseY);
                    return true;
                }
                if (baseX == destX && baseY == destY - 1) {
                    entity.getMovementQueue().setPathX(baseX).setPathY(baseY);
                    return true;
                }
            }
        if (size == 9) {
            if (baseX == destX && baseY == destY + 1 && (RegionManager.getClipping(absX, absY, height, area) & 0x20) == 0) {
                entity.getMovementQueue().setPathX(baseX).setPathY(baseY);
                return true;
            }
            if (baseX == destX && baseY == destY - 1 && (RegionManager.getClipping(absX, absY, height, area) & 2) == 0) {
                entity.getMovementQueue().setPathX(baseX).setPathY(baseY);
                return true;
            }
            if (baseX == destX - 1 && baseY == destY && (RegionManager.getClipping(absX, absY, height, area) & 8) == 0) {
                entity.getMovementQueue().setPathX(baseX).setPathY(baseY);
                return true;
            }
            if (baseX == destX + 1 && baseY == destY && (RegionManager.getClipping(absX, absY, height, area) & 0x80) == 0) {
                entity.getMovementQueue().setPathX(baseX).setPathY(baseY);
                return true;
            }
        }
        return false;
    }

    private static boolean largeRoutePath(Mobile entity, int destX, int destY, int baseY, int size, int direction, int baseX) {

        if (baseX == destX && baseY == destY) {
            entity.getMovementQueue().setPathX(baseX).setPathY(baseY);
            return true;
        }

        int absX = (entity.getLocation().getRegionX() << 3) + baseX;

        int absY = (entity.getLocation().getRegionY() << 3) + baseY;

        int height = entity.getLocation().getZ();

        PrivateArea area = entity.getPrivateArea();

        baseX -= 0;
        baseY -= 0;
        destX -= 0;
        destY -= 0;
        if (size == 6 || size == 7) {
            if (size == 7)
                direction = direction + 2 & 3;
            if (direction == 0) {
                if (baseX == destX + 1 && baseY == destY
                        && (RegionManager.getClipping(absX, absY, height, area) & 0x80) == 0) {
                    entity.getMovementQueue().setPathX(baseX).setPathY(baseY);
                    return true;
                }
                if (baseX == destX && baseY == destY - 1
                        && (RegionManager.getClipping(absX, absY, height, area) & 2) == 0) {
                    entity.getMovementQueue().setPathX(baseX).setPathY(baseY);
                    return true;
                }
            } else if (direction == 1) {
                if (baseX == destX - 1 && baseY == destY
                        && (RegionManager.getClipping(absX, absY, height, area) & 8) == 0) {
                    entity.getMovementQueue().setPathX(baseX).setPathY(baseY);
                    return true;
                }
                if (baseX == destX && baseY == destY - 1
                        && (RegionManager.getClipping(absX, absY, height, area) & 2) == 0) {
                    entity.getMovementQueue().setPathX(baseX).setPathY(baseY);
                    return true;
                }
            } else if (direction == 2) {
                if (baseX == destX - 1 && baseY == destY
                        && (RegionManager.getClipping(absX, absY, height, area) & 8) == 0) {
                    entity.getMovementQueue().setPathX(baseX).setPathY(baseY);
                    return true;
                }
                if (baseX == destX && baseY == destY + 1
                        && (RegionManager.getClipping(absX, absY, height, area) & 0x20) == 0) {
                    entity.getMovementQueue().setPathX(baseX).setPathY(baseY);
                    return true;
                }
            } else if (direction == 3) {
                if (baseX == destX + 1 && baseY == destY
                        && (RegionManager.getClipping(absX, absY, height, area) & 0x80) == 0) {
                    entity.getMovementQueue().setPathX(baseX).setPathY(baseY);
                    return true;
                }
                if (baseX == destX && baseY == destY + 1
                        && (RegionManager.getClipping(absX, absY, height, area) & 0x20) == 0) {
                    entity.getMovementQueue().setPathX(baseX).setPathY(baseY);
                    return true;
                }
            }
        }
        if (size == 8) {
            if (baseX == destX && baseY == destY + 1
                    && (RegionManager.getClipping(absX, absY, height, area) & 0x20) == 0) {
                entity.getMovementQueue().setPathX(baseX).setPathY(baseY);
                return true;
            }
            if (baseX == destX && baseY == destY - 1 && (RegionManager.getClipping(absX, absY, height, area) & 2) == 0) {
                entity.getMovementQueue().setPathX(baseX).setPathY(baseY);
                return true;
            }
            if (baseX == destX - 1 && baseY == destY && (RegionManager.getClipping(absX, absY, height, area) & 8) == 0) {
                entity.getMovementQueue().setPathX(baseX).setPathY(baseY);
                return true;
            }
            if (baseX == destX + 1 && baseY == destY
                    && (RegionManager.getClipping(absX, absY, height, area) & 0x80) == 0) {
                entity.getMovementQueue().setPathX(baseX).setPathY(baseY);
                return true;
            }
        }
        return false;
    }

    /**
     * @param entity
     * @param destY
     * @param destX
     * @param baseX
     * @param sizeX
     * @param blockedMask
     * @param sizeY
     * @param baseY
     * @return
     */
    private static boolean sizeRoutePath(Mobile entity, int destY, int destX, int baseX, int sizeX, int blockedMask, int sizeY, int baseY) {
        int absX = (entity.getLocation().getRegionX() << 3) + baseX;
        int absY = (entity.getLocation().getRegionY() << 3) + baseY;
        int height = entity.getLocation().getZ();
        PrivateArea area = entity.getPrivateArea();
        int xOffset = 0;
        int yOffset = 0;
        int maxX = (destX + sizeY) - 1;
        int maxY = (destY + sizeX) - 1;

        if (baseX >= destX && baseX <= maxX && baseY >= destY && baseY <= maxY) {
            entity.getMovementQueue().setPathX(baseX).setPathY(baseY);
            return true;
        }
        if (baseX == destX - 1 && baseY >= destY && baseY <= maxY
                && (RegionManager.getClipping(absX - xOffset, absY - yOffset, height, area) & 8) == 0
                && (blockedMask & 8) == 0) {
            entity.getMovementQueue().setPathX(baseX).setPathY(baseY);
            return true;
        }
        if (baseX == maxX + 1 && baseY >= destY && baseY <= maxY
                && (RegionManager.getClipping(absX - xOffset, absY - yOffset, height, area) & 0x80) == 0
                && (blockedMask & 2) == 0) {
            entity.getMovementQueue().setPathX(baseX).setPathY(baseY);
            return true;
        }
        if (baseY == destY - 1 && baseX >= destX && baseX <= maxX
                && (RegionManager.getClipping(absX - xOffset, absY - yOffset, height, area) & 2) == 0
                && (blockedMask & 4) == 0 || baseY == maxY + 1 && baseX >= destX && baseX <= maxX
                && (RegionManager.getClipping(absX - xOffset, absY - yOffset, height, area) & 0x20) == 0
                && (blockedMask & 1) == 0) {
            entity.getMovementQueue().setPathX(baseX).setPathY(baseY);
            return true;
        }
        return false;
    }

    public static boolean findWalkable(Mobile entity, int x, int y, int targetSize) {
        //Step West
        if (calculateRoute(entity, entity.size(), x - targetSize, y, 0, 0, 0, 0, false) > 0)
            return true;
        //Step East
        if (calculateRoute(entity, entity.size(), x + targetSize, y, 0, 0, 0, 0, false) > 0)
            return true;
        //Step North
        if (calculateRoute(entity, entity.size(), x, y + targetSize, 0, 0, 0, 0, false) > 0)
            return true;
        //Step South
        if (calculateRoute(entity, entity.size(), x, y - targetSize, 0, 0, 0, 0, false) > 0)
            return true;
        return false;
    }
}
