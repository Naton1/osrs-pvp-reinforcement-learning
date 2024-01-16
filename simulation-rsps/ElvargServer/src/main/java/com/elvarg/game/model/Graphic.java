package com.elvarg.game.model;

/**
 * Represents a graphic an entity might perform.
 *
 * @author relex lawl
 */

public class Graphic {

    /**
     * The graphic's id.
     */
    private final int id;
    /**
     * The delay which the graphic must wait before being performed.
     */
    private final int delay;
    /**
     * The graphic's height level to display in.
     */
    private final GraphicHeight height;
    /**
     * The priority of the graphic.
     */
    private final Priority priority;

    /**
     * The graphic constructor for a character to perform.
     *
     * @param id The graphic's id.
     */
    public Graphic(int id) {
        this.id = id;
        this.delay = 0;
        this.height = GraphicHeight.LOW;
        this.priority = Priority.LOW;
    }

    /**
     * The graphic constructor for a character to perform.
     *
     * @param id    The graphic's id.
     * @param delay The delay to wait until performing the graphic.
     */
    public Graphic(int id, int delay) {
        this.id = id;
        this.delay = delay;
        this.height = GraphicHeight.LOW;
        this.priority = Priority.LOW;
    }

    /**
     * The graphic constructor for a character to perform.
     *
     * @param id     The graphic's id.
     * @param height The graphic's height.
     */
    public Graphic(int id, GraphicHeight height) {
        this.id = id;
        this.delay = 0;
        this.height = height;
        this.priority = Priority.LOW;
    }

    /**
     * The graphic constructor for a character to perform.
     *
     * @param id     The graphic's id.
     * @param delay  The delay to wait until performing the graphic.
     * @param height The graphic's height.
     */
    public Graphic(int id, int delay, GraphicHeight height) {
        this.id = id;
        this.delay = delay;
        this.height = height;
        this.priority = Priority.LOW;
    }

    /**
     * The graphic constructor for a character to perform.
     *
     * @param id       The graphic's id.
     * @param priority The graphic's priority.
     */
    public Graphic(int id, Priority priority) {
        this.id = id;
        this.delay = 0;
        this.priority = priority;
        this.height = GraphicHeight.LOW;
    }

    /**
     * The graphic constructor for a character to perform.
     *
     * @param id       The graphic's id.
     * @param height   The graphic's height.
     * @param priority The graphic's priority.
     */
    public Graphic(int id, GraphicHeight height, Priority priority) {
        this.id = id;
        this.delay = 0;
        this.priority = priority;
        this.height = height;
    }

    /**
     * Gets the graphic's id.
     *
     * @return id.
     */
    public int getId() {
        return id;
    }

    /**
     * Gets the graphic's wait delay.
     *
     * @return delay.
     */
    public int getDelay() {
        return delay;
    }

    /**
     * Gets the graphic's height level to be displayed in.
     *
     * @return The height level.
     */
    public GraphicHeight getHeight() {
        return height;
    }

    /**
     * Gets the priority of this graphic.
     *
     * @return the priority.
     */
    public Priority getPriority() {
        return priority;
    }
}
