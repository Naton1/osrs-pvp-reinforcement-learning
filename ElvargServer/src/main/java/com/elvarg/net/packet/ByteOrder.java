package com.elvarg.net.packet;

/**
 * The enumerated type whose elements represent the possible order in which
 * bytes are written in a multiple-byte value. Also known as "endianness".
 *
 * @author blakeman8192
 */
public enum ByteOrder {
    LITTLE,
    BIG,
    MIDDLE,
    INVERSE_MIDDLE,
    TRIPLE_INT;
}
