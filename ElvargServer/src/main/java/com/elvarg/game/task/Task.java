package com.elvarg.game.task;

import com.elvarg.game.entity.impl.playerbot.PlayerBot;

import java.util.Objects;

import static com.elvarg.game.task.TaskType.DEFAULT;

/**
 * Represents a periodic task that can be scheduled with the {@link TaskManager}.
 *
 * @author Graham
 */
public abstract class Task {

    /**
     * The default key for every task.
     */
    public static final Object DEFAULT_KEY = new Object();
    /**
     * A flag which indicates if this task should be executed once immediately.
     */
    private final boolean immediate;
    /**
     * The number of cycles between consecutive executions of this task.
     */
    private int delay;
    /**
     * The current 'count down' value. When this reaches zero the task will be
     * executed.
     */
    private int countdown;

    public TaskType type;

    /**
     * A flag which indicates if this task is still running.
     */
    private boolean running = false;

    /**
     * The task's owner
     */
    private Object key;

    /**
     * Creates a new task with a delay of 1 cycle.
     */
    public Task() {
        this(1);
    }

    /**
     * Creates a new task with a delay of 1 cycle and immediate flag.
     *
     * @param immediate A flag that indicates if for the first execution there
     *                  should be no delay.
     */
    public Task(boolean immediate) {
        this(1, immediate);
    }

    /**
     * Creates a new task with the specified delay.
     *
     * @param delay The number of cycles between consecutive executions of this
     *              task.
     * @throws IllegalArgumentException if the {@code delay} is not positive.
     */
    public Task(int delay) {
        this(delay, false);
        this.bind(DEFAULT_KEY);
        this.type = DEFAULT;
    }

    public Task(int delay, TaskType type) {
        this(delay, false);
        this.bind(DEFAULT_KEY);
        this.type = type;
    }

    /**
     * Creates a new task with the specified delay and immediate flag.
     *
     * @param delay     The number of cycles between consecutive executions of this
     *                  task.
     * @param immediate A flag which indicates if for the first execution there
     *                  should be no delay.
     * @throws IllegalArgumentException if the {@code delay} is not positive.
     */
    public Task(int delay, boolean immediate) {
        this.delay = delay;
        this.countdown = delay;
        this.immediate = immediate;
        this.bind(DEFAULT_KEY);
    }

    /**
     * Creates a new task with the specified delay and immediate flag.
     *
     * @param delay     The number of cycles between consecutive executions of this
     *                  task.
     * @throws IllegalArgumentException if the {@code delay} is not positive.
     */
    public Task(int delay, Object key) {
        this.delay = delay;
        this.countdown = delay;
        this.immediate = false;
        this.bind(key);
    }

    /**
     * Creates a new task with the specified delay and immediate flag.
     *
     * @param delay     The number of cycles between consecutive executions of this
     *                  task.
     * @param immediate A flag which indicates if for the first execution there
     *                  should be no delay.
     * @throws IllegalArgumentException if the {@code delay} is not positive.
     */
    public Task(int delay, Object key, boolean immediate) {
        this.delay = delay;
        this.countdown = delay;
        this.immediate = immediate;
        this.bind(key);
    }

    public final Object getKey() {
        return Objects.requireNonNull(key);
    }

    public final Task bind(Object key) {
        this.key = Objects.requireNonNull(key);
        return this;
    }

    /**
     * Checks if this task is an immediate task.
     *
     * @return {@code true} if so, {@code false} if not.
     */
    public boolean isImmediate() {
        return immediate;
    }

    /**
     * Checks if the task is running.
     *
     * @return {@code true} if so, {@code false} if not.
     */
    public boolean isRunning() {
        return running;
    }

    /**
     * Checks if the task is stopped.
     *
     * @return {@code true} if so, {@code false} if not.
     */
    public boolean isStopped() {
        return !running;
    }

    /**
     * This method should be called by the scheduling class every cycle. It
     * updates the {@link #countdown} and calls the {@link #execute()} method
     * if necessary.
     *
     * @return A flag indicating if the task is running.
     */
    public boolean tick() {
        if (running && (countdown == 0 || --countdown == 0)) {
            // Execute task if there is no delay or the delay has elapsed on this tick
            execute();
            countdown = delay;
        }
        onTick();
        return running;
    }

    public void onTick() {
    }

    /**
     * Performs this task's action.
     */
    protected abstract void execute();

    public int getDelay() {
        return this.delay;
    }

    /**
     * Returns how many ticks are left before this Task is next executed.
     *
     * @return
     */
    public int getRemainingTicks() {
        return this.countdown;
    }

    /**
     * Changes the delay of this task.
     *
     * @param delay The number of cycles between consecutive executions of this
     *              task.
     */
    public void setDelay(int delay) {
        if (delay > 0)
            this.delay = delay;
    }

    /**
     * Sets the status of this task.
     */
    public void setRunning(boolean running) {
        this.running = running;
    }

    /**
     * Stops this task.
     */
    public void stop() {
        running = false;
    }
}