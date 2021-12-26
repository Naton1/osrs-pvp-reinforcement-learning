package com.runescape.util;

public final class NativeMemory {

    public static Object wrap(byte[] data) {
        return wrap(data, 0, data.length);
    }

    public static Object wrap(byte[] data, int offset, int length) {
        java.nio.ByteBuffer memory = java.nio.ByteBuffer.allocateDirect(length);
        memory.put(data, offset, length);
        return memory;
    }

    public static byte[] unwrap(Object object) {
        java.nio.ByteBuffer memory = (java.nio.ByteBuffer) object;
        memory.rewind();
        int length = memory.remaining();
        byte[] data = new byte[length];
        memory.get(data, 0, length);
        return data;
    }

    public static int size(Object object) {
        java.nio.ByteBuffer memory = (java.nio.ByteBuffer) object;
        memory.rewind();
        return memory.remaining();
    }
}
