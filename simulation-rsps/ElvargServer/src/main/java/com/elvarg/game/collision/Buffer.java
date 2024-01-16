package com.elvarg.game.collision;

/**
 * Represents a buffer.
 * <p>
 * Taken from the 317 client.
 *
 * @author Professor Oak
 */
public class Buffer {

    public int offset;
    private byte[] buffer;

    public Buffer(byte[] buffer) {
        this.buffer = buffer;
        this.offset = 0;
    }

    public void skip(int length) {
        offset += length;
    }

    public void setOffset(int location) {
        offset = location;
    }

    public void setOffset(long location) {
        offset = (int) location;
    }

    public int length() {
        return buffer.length;
    }

    public byte readSignedByte() {
        return buffer[offset++];
    }

    public int readUnsignedByte() {
        return buffer[offset++] & 0xff;
    }

    public int getShort() {
        int val = (readSignedByte() << 8) + readSignedByte();
        if (val > 32767) {
            val -= 0x10000;
        }
        return val;
    }

    public int readUShort() {
        return (readUnsignedByte() << 8) + readUnsignedByte();
    }

    public int getInt() {
        return (readUnsignedByte() << 24) + (readUnsignedByte() << 16) + (readUnsignedByte() << 8) + readUnsignedByte();
    }

    public long getLong() {
        return (readUnsignedByte() << 56) + (readUnsignedByte() << 48) + (readUnsignedByte() << 40) + (readUnsignedByte() << 32) + (readUnsignedByte() << 24) + (readUnsignedByte() << 16) + (readUnsignedByte() << 8) + readUnsignedByte();
    }

    public int readUnsignedWord() {
        offset += 2;
        return ((buffer[offset - 2] & 0xff) << 8) + (buffer[offset - 1] & 0xff);
    }

    public int getUSmart() {
        int i = buffer[offset] & 0xff;
        if (i < 128) {
            return readUnsignedByte();
        } else {
            return readUShort() - 32768;
        }
    }

    public int readSmart() {
        try {
            int value = 0;
            int ptr;
            for (ptr = getUSmart(); 32767 == ptr; ptr = getUSmart())
                value += 32767;
            value += ptr;
            return value;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    public String readString() {
        int i = offset;
        while (buffer[offset++] != 10)
            ;
        return new String(buffer, i, offset - i - 1);
    }

    public byte[] getBytes() {
        int i = offset;
        while (buffer[offset++] != 10)
            ;
        byte abyte0[] = new byte[offset - i - 1];
        System.arraycopy(buffer, i, abyte0, i - i, offset - 1 - i);
        return abyte0;
    }

    public byte[] read(int length) {
        byte[] b = new byte[length];
        for (int i = 0; i < length; i++) {
            b[i] = buffer[offset++];
        }
        return b;
    }
}
