package com.runescape.sound;

/**
 * Refactored reference:
 * http://www.rune-server.org/runescape-development/rs2-client/downloads/575183-almost-fully-refactored-317-client.html
 */
final class Synthesizer {
    private static int[] samples;
    private static int[] NOISE;
    private static int[] SINE;
    private final int[] oscillatorVolume;
    private final int[] anIntArray107;
    private final int[] anIntArray108;
    int duration;
    int offset;
    private int delayTime;
    private int delayDecay;

    public Synthesizer() {
        oscillatorVolume = new int[5];
        anIntArray107 = new int[5];
        anIntArray108 = new int[5];
        delayDecay = 100;
        duration = 500;
    }

}
