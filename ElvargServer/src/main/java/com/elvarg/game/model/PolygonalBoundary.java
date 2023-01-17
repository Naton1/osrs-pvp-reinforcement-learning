package com.elvarg.game.model;

import java.awt.*;

public class PolygonalBoundary extends Boundary {

    private Polygon polygon;

    /**
     * This class allows you to define a polygonal boundary with five or more points.
     * @param points
     */
    public PolygonalBoundary(int[][] points) {
        super(0, 0, 0, 0);

        int[] xCoords = new int[points.length];
        int[] yCoords = new int[points.length];

        for (int i = 0; i < points.length; i++) {
            xCoords[i] = points[i][0];
            yCoords[i] = points[i][1];
        }

        this.polygon = new Polygon(xCoords, yCoords, points.length);
    }

    @Override
    public boolean inside(Location p) {
        return this.polygon.contains(p.getX(), p.getY());
    }
}
