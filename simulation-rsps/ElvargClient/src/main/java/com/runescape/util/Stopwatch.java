package com.runescape.util;

public class Stopwatch {

    private long time = System.currentTimeMillis();

    public Stopwatch() {
        time = 0;
    }

    public Stopwatch reset() {
        time = System.currentTimeMillis();
        return this;
    }

    public long elapsed() {
        return System.currentTimeMillis() - time;
    }

    public boolean elapsed(long time) {
        return elapsed() >= time;
    }

}