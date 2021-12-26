package com.runescape.util;

public final class Time {

    private static long last;
    private static long offset;

    private Time() {
    }

    public static synchronized long currentTimeMillis() {
        long l = System.currentTimeMillis();
        if (last > l)
            offset += last - l;

        last = l;
        return l + offset;
    }
}
