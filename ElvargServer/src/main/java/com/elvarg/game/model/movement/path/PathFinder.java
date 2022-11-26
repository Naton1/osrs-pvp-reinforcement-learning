package com.elvarg.game.model.movement.path;

import com.elvarg.Server;
import com.elvarg.game.collision.Region;
import com.elvarg.game.collision.RegionManager;
import com.elvarg.game.entity.impl.Mobile;
import com.elvarg.game.entity.impl.grounditem.ItemOnGroundManager;
import com.elvarg.game.model.Item;
import com.elvarg.game.model.Location;
import com.elvarg.game.model.areas.impl.PrivateArea;
import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

/**
 * @author Ynneh | 08/08/2022 - 16:36
 * <https://github.com/drhenny>
 */
public class PathFinder {

    public static final int WEST = 0x1280108, EAST = 0x1280180, SOUTH = 0x1280102,
            NORTH = 0x1280120, SOUTHEAST = 0x1280183, SOUTHWEST = 0x128010e,
            NORTHEAST = 0x12801e0, NORTHWEST = 0x1280138;

    public final static boolean isInDiagonalBlock(Location attacker, Location attacked) {
        return attacked.getX() - 1 == attacker.getX() && attacked.getY() + 1 == attacker.getY()
                || attacker.getX() - 1 == attacked.getX() && attacker.getY() + 1 == attacked.getY()
                || attacked.getX() + 1 == attacker.getX() && attacked.getY() - 1 == attacker.getY()
                || attacker.getX() + 1 == attacked.getX() && attacker.getY() - 1 == attacked.getY()
                || attacked.getX() + 1 == attacker.getX() && attacked.getY() + 1 == attacker.getY()
                || attacker.getX() + 1 == attacked.getX() && attacker.getY() + 1 == attacked.getY();
    }

    public static void calculateCombatRoute(Mobile player, Mobile target) {
        calculateRoute(player, 0, target.getLocation().getX(), target.getLocation().getY(), 1, 1, 0, 0, false);
        player.setMobileInteraction(target);
    }

    public static void calculateEntityRoute(Mobile player, int destX, int destY) {
        calculateRoute(player, 0, destX, destY, 1, 1, 0, 0, false);
    }

    public static void calculateWalkRoute(Mobile player, int destX, int destY) {
        player.getMovementQueue().reset();
        calculateRoute(player, 0, destX, destY, 0, 0, 0, 0, true);

    }

    public static void calculateObjectRoute(Mobile entity, int size, int destX, int destY, int xLength, int yLength, int direction, int blockingMask) {
        calculateRoute(entity, size, destX, destY, xLength, yLength, direction, blockingMask, false);
    }

    public static void pathClosestAttackableTile(Mobile attacker, Mobile defender, int size) {

        var tiles = getClosestTileForDistance(defender, size);

        PrivateArea area = attacker.getPrivateArea();

        tiles.stream().filter(t -> !RegionManager.blocked(t, area)).filter(t -> RegionManager.canProjectileAttack(attacker, t, defender.getLocation())).min(Comparator.comparing(attacker.getLocation()::getDistance)).ifPresent(tile -> {
            Server.logDebug("BestTile=" + tile.toString() + " pathClosestAttackableTile" + " distanceRequested=" + size + " distanceGave=" + attacker.getLocation().getDistance(defender.getLocation()));
            calculateEntityRoute(attacker, tile.getX(), tile.getY());
        });
        attacker.setMobileInteraction(defender);
        return;
    }

    private static List<Location> getClosestTileForDistance(Mobile target, int distance) {
        List<Location> perimeter = Lists.newArrayList();
        Location dest = target.getLocation();
        distance -= 1;
        for (int i = 0; i < distance; i++) {
            perimeter.add(dest.translate(i, distance)); // north
            perimeter.add(dest.translate(distance, i)); // east
            perimeter.add(dest.translate(distance, -i)); // south
            perimeter.add(dest.translate(-i, distance)); // west
            perimeter.add(dest.translate(i, -distance)); // north
            perimeter.add(dest.translate(-distance, -i)); //east-south
            perimeter.add(dest.translate(-distance, i)); //east-noth
            perimeter.add(dest.translate(-i, -distance)); //south
        }
        return perimeter;
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
                //System.err.println("already at destination.. breaking the loop.");
                break;
            }

            //System.err.println("Size="+size+" Path1="+method219(entity, destinationX, baseX, baseY, direction, size - 1, destinationY)+" path2="+method220(entity, destinationX, destinationY, baseY, size - 1, direction, baseX));

            if (size != 0) {
                /** Used for basic walking and other packet interactions also size 10 **/
                if ((size < 5 || size == 10) && defaultRoutePath(entity, destinationX, baseX, baseY, direction, size - 1, destinationY)) {
                    System.err.println("Using normal entity pathing.. :)))");
                    entity.getMovementQueue().setRoute(true);
                    break;
                }
                /** Used for larger entities e.g corp/kbd ect **/
                if (size < 10 && largeRoutePath(entity, destinationX, destinationY, baseY, size - 1, direction, baseX)) {
                    System.err.println("Using larger Size Pathing.. :)))");
                    entity.getMovementQueue().setRoute(true);
                    break;
                }
            }
            /** Used for Calculating best route to object based on sizeX/Y **/
            if (yLength != 0 && xLength != 0 && sizeRoutePath(entity, destinationY, destinationX, baseX, xLength, blockingMask, yLength, baseY)) {
                System.err.println("Using size based pathing :)))");
                entity.getMovementQueue().setRoute(true);
                break;
            }


            //System.err.println("baseX=" + baseX + " baseY=" + baseY + " destX=" + destX + " destY=" + destY + " X=" + absoluteX + " Y=" + absoluteY + " pathType=" + method219(entity, destinationX, baseX, baseY, direction, size - 1, destinationY) + "," + method220(entity, destinationX, destinationY, baseY, size - 1, direction, baseX) + "," + method221(entity, destinationY, destinationX, baseX, xLength, clipFlag, yLength, baseY, direction));

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
                System.err.println("error.. no path found... path probably not reachable.");
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

        //System.err.println("PATH :))) localX=" + localX + " localY=" + localY + " baseX=" + baseX + " baseY=" + baseY + " destinationX=" + destinationX + " destinationY=" + destinationY);

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
