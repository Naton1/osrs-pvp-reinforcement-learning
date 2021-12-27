package com.runescape.util;

public final class NativeMemory {

    public static Object wrap(byte[] data, int offset, int length) {
        java.nio.ByteBuffer memory = java.nio.ByteBuffer.allocateDirect(length);
        memory.put(data, offset, length);
        return memory;
    }

}
