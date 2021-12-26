package com.elvarg.game.task.impl;

import com.elvarg.game.entity.impl.object.GameObject;
import com.elvarg.game.entity.impl.object.ObjectManager;
import com.elvarg.game.task.Task;

/**
 * A {@link Task} implementation which deregisters an original
 * {@link GameObject}, temporarily registers a replacement and then re-registers
 * the original one again.
 * <p>
 * Useful for skills such as Woodcutting and Mining where resources are
 * temporarily unavailable.
 *
 * @author Professor Oak
 */
public class TimedObjectReplacementTask extends Task {

    /**
     * The original {@link GameObject}.
     */
    private final GameObject original;

    /**
     * The temporary {@link GameObject}.
     * <p>
     * This object will be deregistered once the task has finished execution and
     * the {@code original} object will respawn.
     */
    private final GameObject temp;

    /**
     * The amount of ticks before the {@code original} {@link GameObject}
     * respawns again.
     */
    private final int ticks;

    /**
     * The current tick counter.
     */
    private int tick = 0;

    /**
     * Are the objects on the same tile (position)?
     */
    private boolean sameTile = false;

    /**
     * Constructs this task.
     *
     * @param original
     * @param ticks
     */
    public TimedObjectReplacementTask(GameObject original, GameObject temp, int ticks) {
        super(1, true);
        this.original = original;
        this.temp = temp;
        this.ticks = ticks;
        this.sameTile = original.getLocation().equals(temp.getLocation());
    }

    /**
     * Executes this task.
     */
    @Override
    public void execute() {
        if (tick == 0) {

            // Despawn original object..
            ObjectManager.deregister(original, !sameTile);

            // Spawn temp object..
            ObjectManager.register(temp, true);

        } else if (tick >= ticks) {

            // Despawn temp..
            ObjectManager.deregister(temp, !sameTile);

            // Spawn original object..
            ObjectManager.register(original, true);

            // Stop the task
            stop();
        }
        tick++;
    }
}
