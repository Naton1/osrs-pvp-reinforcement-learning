package com.elvarg.game.model;

/**
 * This file manages an entity's animation which should be performed.
 *
 * @author relex lawl
 */

public class Animation {

    /**
     * The default reset animation for an entity.
     */
    public static final Animation DEFAULT_RESET_ANIMATION = new Animation(65535);
    /**
     * The animation's id.
     */
    private final int id;
    /**
     * The delay in which to perform the animation.
     */
    private final int delay;
    /**
     * The priority of the animation.
     */
    private final Priority priority;

    /**
     * Animation constructor for entity to perform.
     *
     * @param id       The id of the animation entity should perform.
     * @param priority The priority of the animation entity should perform.
     */
    public Animation(int id, Priority priority) {
        this.id = id;
        this.delay = 0;
        this.priority = priority;
    }

    /**
     * Animation constructor for entity to perform.
     *
     * @param id The id of the animation entity should perform.
     */
    public Animation(int id) {
        this.id = id;
        this.delay = 0;
        this.priority = Priority.LOW;
    }

    /**
     * Animation constructor for entity to perform.
     *
     * @param id    The id of the animation entity should perform.
     * @param delay The delay which to wait before entity performs animation.
     */
    public Animation(int id, int delay) {
        this.id = id;
        this.delay = delay;
        this.priority = Priority.LOW;
    }

    /**
     * Gets the animation's id.
     *
     * @return id.
     */
    public int getId() {
        return id;
    }

    /**
     * Gets the animation's performance delay.
     *
     * @return delay.
     */
    public int getDelay() {
        return delay;
    }

    /**
     * Gets the animation's priority.
     *
     * @return priority.
     */
    public Priority getPriority() {
        return priority;
    }
}
