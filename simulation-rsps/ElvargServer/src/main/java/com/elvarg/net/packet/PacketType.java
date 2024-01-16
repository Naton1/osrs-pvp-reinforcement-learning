package com.elvarg.net.packet;

public enum PacketType {
    /**
     * A fixed size packet where the size never changes.
     */
    FIXED,
    /**
     * A variable packet where the size is described by a byte.
     */
    VARIABLE,
    /**
     * A packet where the length is sent to its destination with it as a byte.
     */
    VARIABLE_BYTE,
    /**
     * A variable packet where the size is described by a word.
     */
    VARIABLE_SHORT;
}
