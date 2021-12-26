package com.runescape.cache;

import java.io.IOException;

public final class Cache {
    public final int maxId;
    public final JFS32 data;
    public final BufferedFile index;
    private final byte[] buffer = new byte[8];

    public Cache(JFS32 data, BufferedFile index) {
        this(data, index, -1);
    }

    public Cache(JFS32 data, BufferedFile index, int maxId) {
        this.data = data;
        this.index = index;
        if (maxId < 0) {
            maxId = 0x7fffffff;
        }
        this.maxId = maxId;
    }

    private static void putInt(byte[] array, int offset, int v) {
        array[offset] = (byte) (v >>> 24);
        array[offset + 1] = (byte) (v >>> 16);
        array[offset + 2] = (byte) (v >>> 8);
        array[offset + 3] = (byte) v;
    }

    private static int getInt(byte[] array, int offset) {
        return (array[offset] << 24) | ((array[offset + 1] & 0xff) << 16) | ((array[offset + 2] & 0xff) << 8) | (array[offset + 3] & 0xff);
    }

    public byte[] get(int id) {
        return get(id, -1);
    }

    public byte[] get(int id, int requiredLength) {
        if (id < 0 || id > maxId || (long) id * 8L + 8L > index.size()) {
            return null;
        }
        try {
            index.position(id * 8L);
            index.read(buffer, 0, 4);
            int ptr = getInt(buffer, 0);
            if (!data.validPointer(ptr)) {
                return null;
            }
            index.read(buffer, 0, 4);
            int length = getInt(buffer, 0) - 1;
            if (length < 0 || (requiredLength >= 0 && length != requiredLength)) {
                return null;
            }
            byte[] array = new byte[length];
            if (data.read(ptr, 0, array, 0, length) == length) {
                return array;
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public boolean put(int id, byte[] data) {
        return put(id, data, 0, data.length);
    }

    public boolean put(int id, byte[] array, int offset, int length) {
        if (id < 0 || id > maxId) {
            return false;
        }
        if (length != 0 && (array.length < offset + length || offset < 0 || length < 0 || offset + length < 0)) {
            throw new IndexOutOfBoundsException();
        }
        int ptr = 0;
        long pos = (long) id * 8L;
        int oldLength = -1;
        if (pos + 8L <= index.size()) {
            try {
                index.position(pos);
                index.read(buffer, 0, 8);
                ptr = getInt(buffer, 0);
                oldLength = getInt(buffer, 4) - 1;
            } catch (IOException ex) {
            }
        }
        int newPtr = data.realloc(ptr, data.getRequiredBlocks(length));
        if (newPtr != 0) {
            if (data.write(newPtr, 0, array, offset, length) == length) {
                try {
                    if (ptr != newPtr) {
                        index.position(pos);
                        if (length != oldLength) {
                            putInt(buffer, 0, newPtr);
                            putInt(buffer, 4, length + 1);
                            index.write(buffer, 0, 8);
                        } else {
                            putInt(buffer, 0, newPtr);
                            index.write(buffer, 0, 4);
                        }
                    } else if (length != oldLength) {
                        index.position(pos + 4L);
                        putInt(buffer, 0, length + 1);
                        index.write(buffer, 0, 4);
                    }
                    return true;
                } catch (IOException ex) {
                }
            }
            data.free(newPtr);
            ptr = 1;
        }
        if (ptr != 0) {
            try {
                index.position(pos);
                putInt(buffer, 0, 0);
                index.write(buffer, 0, 4);
            } catch (IOException ex) {
            }
        }
        return false;
    }

    public boolean remove(int id) {
        if (id < 0 || id > maxId) {
            return false;
        }
        long pos = (long) id * 8L;
        if (pos + 8L > index.size()) {
            return false;
        }
        try {
            index.position(pos);
            index.read(buffer, 0, 4);
            int ptr = getInt(buffer, 0);
            if (ptr != 0) {
                data.free(ptr);
                index.position(pos);
                putInt(buffer, 0, 0);
                index.write(buffer, 0, 4);
                return true;
            }
        } catch (IOException ex) {
        }
        return false;
    }
}
