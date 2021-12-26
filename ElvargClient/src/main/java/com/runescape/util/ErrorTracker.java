package com.runescape.util;

/**
 * @author Thelife/Alex
 * @version 1.0 Created: 20:50:49 7.8.2014 ErrorTracker.java
 */

public class ErrorTracker {

    public static void track(String s, int i, boolean b) {
        System.out.println((b ? "[ERROR]" : "[RAN]") + " " + s + ": " + i);
    }
}
