package com.elvarg.game.collision;

import com.elvarg.game.model.Location;

/**
 * Represents a region.
 *
 * @author Professor Oak
 */
public class Region {

    /**
     * This region's id.
     */
    private final int regionId;

    /**
     * This region's terrain file id.
     */
    private final int terrainFile;

    /**
     * This region's object file id.
     */
    private final int objectFile;

    /**
     * The clipping in this region.
     */
    public int[][][] clips = new int[4][][];

    /**
     * Has this region been loaded?
     */
    private boolean loaded;

    /**
     * Creates a new region.
     *
     * @param regionId
     * @param terrainFile
     * @param objectFile
     */
    public Region(int regionId, int terrainFile, int objectFile) {
        this.regionId = regionId;
        this.terrainFile = terrainFile;
        this.objectFile = objectFile;
    }

    public int getRegionId() {
        return regionId;
    }

    public int getTerrainFile() {
        return terrainFile;
    }

    public int getObjectFile() {
        return objectFile;
    }

    /**
     * Gets clipping
     *
     * @param x
     * @param y
     * @param height
     * @return
     */
    public int getClip(int x, int y, int height) {
        int regionAbsX = (regionId >> 8) * 64;
        int regionAbsY = (regionId & 0xff) * 64;
        if (height < 0 || height >= 4)
            height = 0;
        if (clips[height] == null) {
            clips[height] = new int[64][64];
        }
        return clips[height][x - regionAbsX][y - regionAbsY];
    }

    /**
     * Adds clipping
     *
     * @param x
     * @param y
     * @param height
     * @param shift
     */
    public void addClip(int x, int y, int height, int shift) {
        int regionAbsX = (regionId >> 8) * 64;
        int regionAbsY = (regionId & 0xff) * 64;
        if (height < 0 || height >= 4)
            height = 0;
        if (clips[height] == null) {
            clips[height] = new int[64][64];
        }
        clips[height][x - regionAbsX][y - regionAbsY] |= shift;
    }

    /**
     * Removes clipping.
     *
     * @param x
     * @param y
     * @param height
     * @param shift
     */
    public void removeClip(int x, int y, int height, int shift) {
        int regionAbsX = (regionId >> 8) * 64;
        int regionAbsY = (regionId & 0xff) * 64;
        if (height < 0 || height >= 4)
            height = 0;
        if (clips[height] == null) {
            clips[height] = new int[64][64];
        }
        clips[height][x - regionAbsX][y - regionAbsY] &= ~shift;
    }

    /**
     * Gets the local region position.
     *
     * @param position
     * @return
     */
    public int[] getLocalPosition(Location position) {
        int absX = position.getX();
        int absY = position.getY();
        int regionAbsX = (regionId >> 8) * 64;
        int regionAbsY = (regionId & 0xff) * 64;
        int localX = absX - regionAbsX;
        int localY = absY - regionAbsY;
        return new int[]{localX, localY};
    }

    public boolean isLoaded() {
        return loaded;
    }

    public void setLoaded(boolean loaded) {
        this.loaded = loaded;
    }
}