package com.runescape.util;

public final class SystemUtils {

    private static final String OS_NAME = System.getProperty("os.name");
    private static final int CORES = Runtime.getRuntime().availableProcessors();

    public static int getCores() {
        return CORES;
    }

    public static boolean isMac() {
        return OS_NAME.toLowerCase().contains("mac") || OS_NAME.startsWith("darwin");
    }

}