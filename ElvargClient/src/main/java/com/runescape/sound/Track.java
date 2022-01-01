package com.runescape.sound;

import com.runescape.io.Buffer;

/**
 * Refactored reference:
 * http://www.rune-server.org/runescape-development/rs2-client/downloads/575183-almost-fully-refactored-317-client.html
 */
public final class Track {

    public static final int[] delays = new int[5000];
    private static final Track[] tracks = new Track[5000];
    private static byte[] output;
    private static Buffer riff;
    private final Synthesizer[] synthesizers;
    private int loopStart;
    private int loopEnd;

    private Track() {
        synthesizers = new Synthesizer[10];
    }

    public static Buffer data(int loops, int id) {
        if (tracks[id] != null) {
            Track soundTrack = tracks[id];
            return soundTrack.pack(loops);
        } else {
            return null;
        }
    }

    private Buffer pack(int loops) {
        int size = mix(loops);
        riff.currentPosition = 0;
        riff.writeInt(0x52494646);
        riff.writeLEInt(36 + size);
        riff.writeInt(0x57415645);
        riff.writeInt(0x666d7420);
        riff.writeLEInt(16);
        riff.writeLEShort(1);
        riff.writeLEShort(1);
        riff.writeLEInt(22050);
        riff.writeLEInt(22050);
        riff.writeLEShort(1);
        riff.writeLEShort(8);
        riff.writeInt(0x64617461);
        riff.writeLEInt(size);
        riff.currentPosition += size;
        return riff;
    }

    private int mix(int loops) {
        return 0;
    }

}
