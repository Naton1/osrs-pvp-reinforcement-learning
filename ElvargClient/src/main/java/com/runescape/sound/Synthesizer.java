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

    public static void init() {
        NOISE = new int[32768];
        for (int i = 0; i < 32768; i++)
            if (Math.random() > 0.5D)
                NOISE[i] = 1;
            else
                NOISE[i] = -1;

        SINE = new int[32768];
        for (int j = 0; j < 32768; j++)
            SINE[j] = (int) (Math.sin((double) j / 5215.1903000000002D) * 16384D);

        samples = new int[0x35d54];
    }

    public int[] synthesize(int sampleCount, int duration) {
        for (int k = 0; k < sampleCount; k++)
            samples[k] = 0;

        if (duration < 10)
            return samples;


        return samples;
    }


}
