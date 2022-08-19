package com.elvarg.util;

import com.elvarg.game.model.Location;

/**
 * @author Ynneh | 12/08/2022 - 17:54
 * <https://github.com/drhenny>
 */
public class TileUtils {

    public static int getDistance(Location source, Location dest) {
        return getDistance(source.getX(), source.getY(), dest.getX(), dest.getY());
    }

    public static int getDistance(Location source, int destX, int destY) {
        return getDistance(source.getX(), source.getY(), destX, destY);
    }

    public static int getDistance(int x1, int y1, int x2, int y2) {
        return (int) Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
    }
}
