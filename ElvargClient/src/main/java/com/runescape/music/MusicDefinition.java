package com.runescape.music;/* MusicDefinition - Decompiled by JODE
 * Visit http://jode.sourceforge.net/
 */

import com.runescape.io.Buffer;

final class MusicDefinition {
    private static byte[] aByteArray210
            = {2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
            2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
            2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1, 1,
            1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
            1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
            2, 2, 0, 1, 2, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    private Buffer buffer = new Buffer(null);
    private int[] anIntArray212;
    int anInt213;
    private int[] anIntArray214;
    private long aLong215;
    int[] anIntArray216;
    private int[] anIntArray217;
    private int anInt218;

	public void method520(int i) {
        int i_0_ = buffer.method428();
        anIntArray216[i] += i_0_;
    }

	public boolean method521() {
        if (buffer.currentPosition >= 0)
            return false;
        return true;
    }

	public void method522(int i) {
        anIntArray214[i] = buffer.currentPosition;
    }

	public void method523() {
        buffer.payload = null;
        anIntArray217 = null;
        anIntArray214 = null;
        anIntArray216 = null;
        anIntArray212 = null;
    }

    private int method524(int i) {
        int i_1_ = (buffer.payload[buffer.currentPosition]);
        if (i_1_ < 0) {
            i_1_ &= 0xff;
            anIntArray212[i] = i_1_;
            buffer.currentPosition++;
        } else
            i_1_ = anIntArray212[i];
        if (i_1_ == 240 || i_1_ == 247) {
            int i_2_ = buffer.method428();
            if (i_1_ == 247 && i_2_ > 0) {
                int i_3_ = ((buffer.payload
                        [buffer.currentPosition])
                        & 0xff);
                if (i_3_ >= 241 && i_3_ <= 243 || i_3_ == 246 || i_3_ == 248
                        || i_3_ >= 250 && i_3_ <= 252 || i_3_ == 254) {
                    buffer.currentPosition++;
                    anIntArray212[i] = i_3_;
                    return method535(i, i_3_);
                }
            }
            buffer.currentPosition += i_2_;
            return 0;
        }
        return method535(i, i_1_);
    }

    public void decode(byte[] payload) {
        buffer.payload = payload;
        buffer.currentPosition = 10;
        int indexLength = buffer.readUShort();
        anInt213 = buffer.readUShort();
        anInt218 = 500000;
        anIntArray217 = new int[indexLength];
        int id = 0;
        while (id < indexLength) {
            int i_5_ = buffer.readInt();
            int i_6_ = buffer.readInt();
            if (i_5_ == 1297379947) {
                anIntArray217[id] = buffer.currentPosition;
                id++;
            }
            buffer.currentPosition += i_6_;
        }
        anIntArray214 = anIntArray217.clone();
        anIntArray216 = new int[indexLength];
        anIntArray212 = new int[indexLength];
    }

	public void method526(int i) {
        buffer.currentPosition = anIntArray214[i];
    }

	public boolean hasPayload() {
        if (buffer.payload == null)
            return false;
        return true;
    }

	public void method528() {
        buffer.currentPosition = -1;
    }

	public int method529(int i) {
        int i_7_ = method524(i);
        return i_7_;
    }

    public static void reset() {
        aByteArray210 = null;
    }

	public boolean method531() {
        int i = anIntArray214.length;
        for (int i_8_ = 0; i_8_ < i; i_8_++) {
            if (anIntArray214[i_8_] >= 0)
                return false;
        }
        return true;
    }

	public long method532(int i) {
        return aLong215 + (long) i * (long) anInt218;
    }

	public int method533() {//offset for music ids?
        return anIntArray214.length;
    }

	public void method534(long l) {
        aLong215 = l;
        int i = anIntArray214.length;
        for (int index = 0; index < i; index++) {
            anIntArray216[index] = 0;
            anIntArray212[index] = 0;
            buffer.currentPosition = anIntArray217[index];
            method520(index);
            anIntArray214[index] = buffer.currentPosition;
        }
    }

    private int method535(int i, int i_10_) {
        if (i_10_ == 255) {
            int i_11_ = buffer.readUnsignedByte();
            int i_12_ = buffer.method428();
            if (i_11_ == 47) {
                buffer.currentPosition += i_12_;
                return 1;
            }
            if (i_11_ == 81) {
                int i_13_ = buffer.readTriByte();
                i_12_ -= 3;
                int i_14_ = anIntArray216[i];
                aLong215 += (long) i_14_ * (long) (anInt218 - i_13_);
                anInt218 = i_13_;
                buffer.currentPosition += i_12_;
                return 2;
            }
            buffer.currentPosition += i_12_;
            return 3;
        }
        byte i_15_ = aByteArray210[i_10_ - 128];
        int i_16_ = i_10_;
        if (i_15_ >= 1)
            i_16_ |= buffer.readUnsignedByte() << 8;
        if (i_15_ >= 2)
            i_16_ |= buffer.readUnsignedByte() << 16;
        return i_16_;
    }

	public int method536() {
        int i = anIntArray214.length;
        int i_17_ = -1;
        int i_18_ = 2147483647;
        for (int i_19_ = 0; i_19_ < i; i_19_++) {
            if (anIntArray214[i_19_] >= 0
                    && anIntArray216[i_19_] < i_18_) {
                i_17_ = i_19_;
                i_18_ = anIntArray216[i_19_];
            }
        }
        return i_17_;
    }

    public MusicDefinition() {
        /* empty */
    }
}
