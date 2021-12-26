package com.elvarg.game.task.impl;

import com.elvarg.game.entity.impl.object.GameObject;
import com.elvarg.game.entity.impl.object.ObjectManager;
import com.elvarg.game.model.Action;
import com.elvarg.game.task.Task;

import java.util.Optional;

/**
 * A {@link Task} implementation which spawns a {@link GameObject}
 * and then despawns it after a period of time.
 *
 * @author Professor Oak
 */
public class TimedObjectSpawnTask extends Task {

    /**
     * The temporary {@link GameObject}.
     * <p>
     * This object will be deregistered once the task has finished executing.
     */
    private final GameObject temp;

    /**
     * The amount of ticks this task has.
     */
    private final int ticks;

    /**
     * The action which should be executed
     * once this task has finished.
     */
    private final Optional<Action> action;

    /**
     * The current tick counter.
     */
    private int tick = 0;

    /**
     * Constructs this task to spawn an object and then delete it
     * after a period of time.
     *
     * @param temp
     * @param ticks
     */
    public TimedObjectSpawnTask(GameObject temp, int ticks, Optional<Action> action) {
        super(1, true);
        this.temp = temp;
        this.action = action;
        this.ticks = ticks;
    }

    /**
     * Executes this task.
     */
    @Override
    public void execute() {
        if (tick == 0) {
            ObjectManager.register(temp, true);
        } else if (tick >= ticks) {
            ObjectManager.deregister(temp, true);

            if (action.isPresent()) {
                action.get().execute();
            }

            stop();
        }
        tick++;
    }
}
