package com.runescape.util;

/**
 * Represents a timer in seconds.
 *
 * @author Professor Oak
 */
public class SecondsTimer extends Stopwatch {

    /**
     * The amount of seconds to count down.
     */
    private int seconds;

    /**
     * Constructs a new timer.
     */
    public SecondsTimer() {
    }

    /**
     * Constructs a new timer and
     * starts it immediately.
     *
     * @param seconds The amount of seconds to
     */
    public SecondsTimer(int seconds) {
        start(seconds);
    }

    /**
     * Starts this timer.
     *
     * @param seconds The amount of seconds.
     */
    public void start(int seconds) {
        this.seconds = seconds;
        reset();
    }

    /**
     * Stops this timer
     */
    public void stop() {
        seconds = 0;
    }

    /**
     * Checks if this timer has finished
     * counting down, basically reaching 0.
     *
     * @return true if finished, false otherwise.
     */
    public boolean finished() {
        return elapsed(seconds * 1000);
    }

    /**
     * Gets the amount of seconds remaining
     * before this timer has reached 0.
     *
     * @return The seconds remaining.
     */
    public int secondsRemaining() {
        return seconds - secondsElapsed();
    }

    /**
     * Gets the amount of seconds that have elapsed
     * since the timer was started.
     *
     * @return The seconds elapsed.
     */
    public int secondsElapsed() {
        return (int) elapsed() / 1000;
    }
}
