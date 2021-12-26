package com.runescape.util;

public class Stopwatch {

    private long time = System.currentTimeMillis();

    public Stopwatch() {
        time = 0;
    }

    public Stopwatch headStart(long startAt) {
        time = System.currentTimeMillis() - startAt;
        return this;
    }

    public Stopwatch reset(long i) {
        time = i;
        return this;
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

    public long getTime() {
        return time;
    }
}