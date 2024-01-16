package com.elvarg.game.model;

import com.google.common.base.Stopwatch;

import java.util.concurrent.TimeUnit;

/**
 * Represents a timer in seconds.
 *
 * @author Professor Oak
 */
public class SecondsTimer {

    /**
     * The actual timer.
     */
    private final Stopwatch stopwatch;

    /**
     * The amount of seconds to count down.
     */
    private int seconds;

    /**
     * Constructs a new timer.
     */
    public SecondsTimer() {
        this.stopwatch = Stopwatch.createUnstarted();
        this.seconds = 0;
    }

    /**
     * Constructs a new timer with a pre-defined number of seconds.
     *
     * @param seconds The amount of seconds to
     */
    public SecondsTimer(int seconds) {
        this();
        this.seconds = seconds;
    }

    /**
     * Starts this timer.
     *
     * @param seconds The amount of seconds.
     */
    public void start(int seconds) {
        this.seconds = seconds;

        //Reset and then start the stopwatch.
        stopwatch.reset();
        stopwatch.start();
    }

    /**
     * Starts this timer.
     */
    public SecondsTimer start() {
        if (this.seconds <= 0) {
            return this;
        }

        //Reset and then start the stopwatch.
        stopwatch.reset();
        stopwatch.start();

        return this;
    }

    /**
     * Stops this timer
     */
    public void stop() {
        seconds = 0;
        if (stopwatch.isRunning()) {
            stopwatch.reset();
        }
    }

    /**
     * Determines whether the stopwatch is currently running or not.
     * @return
     */
    public boolean isRunning() {
        return stopwatch.isRunning();
    }

    /**
     * Gets the amount of seconds remaining
     * before this timer has reached 0.
     *
     * @return The seconds remaining.
     */
    public int secondsRemaining() {
        if (seconds == 0) {
            return 0;
        }
        int remaining = seconds - secondsElapsed();
        if (remaining < 0) {
            remaining = 0;
        }
        return remaining;
    }

    /**
     * Checks if this timer has finished
     * counting down, basically reaching 0.
     *
     * @return true if finished, false otherwise.
     */
    public boolean finished() {
        if (secondsRemaining() == 0) {
            stop();
            return true;
        }
        return false;
    }

    /**
     * Gets the amount of seconds that have elapsed
     * since the timer was started.
     *
     * @return The seconds elapsed.
     */
    public int secondsElapsed() {
        return (int) stopwatch.elapsed(TimeUnit.MILLISECONDS) / 1000;
    }

    /**
     * Returns a formatted string containing details
     * about this timer.
     */
    public String toString() {
        StringBuilder builder = new StringBuilder();

        int secondsRemaining = secondsRemaining();
        int minutesRemaining = (int) TimeUnit.SECONDS.toMinutes(secondsRemaining);
        secondsRemaining -= (minutesRemaining * 60);

        if (minutesRemaining > 0) {
            builder.append(Integer.toString(minutesRemaining) + " " + (minutesRemaining > 1 ? "minutes" : "minute") + " and ");
        }

        builder.append(Integer.toString(secondsRemaining) + " seconds");

        return builder.toString();
    }
}
