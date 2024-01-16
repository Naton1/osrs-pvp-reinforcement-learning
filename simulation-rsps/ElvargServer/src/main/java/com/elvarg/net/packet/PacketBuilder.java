package com.elvarg.net.packet;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/**
 * The {@link Message} implementation that functions as a dynamic buffer wrapper
 * backed by a {@link ByteBuf} that is used for reading and writing data.
 *
 * @author lare96 <http://github.com/lare96>
 * @author blakeman8192
 */
public final class PacketBuilder {
    
    /**
     * An array of the bit masks used for writing bits.
     */
    public static final int[] BIT_MASK = {0, 0x1, 0x3, 0x7, 0xf, 0x1f, 0x3f, 0x7f, 0xff, 0x1ff, 0x3ff, 0x7ff, 0xfff, 0x1fff, 0x3fff,
            0x7fff, 0xffff, 0x1ffff, 0x3ffff, 0x7ffff, 0xfffff, 0x1fffff, 0x3fffff, 0x7fffff, 0xffffff, 0x1ffffff, 0x3ffffff, 0x7ffffff,
            0xfffffff, 0x1fffffff, 0x3fffffff, 0x7fffffff, -1};
    /**
     * The packet id.
     */
    private final int opcode;
    
    /**
     * The packet type.
     */
    private final PacketType type;
    
    /**
     * The packet's current bit position.
     */
    private int bitPosition;
    
    /**
     * The buffer used to write the packet information.
     */
    private ByteBuf buffer = Unpooled.buffer();

    
    /**
     * The PacketBuilder constructor.
     */
    public PacketBuilder() {
        this(-1);
    }

    /**
     * The PacketBuilder constructor.
     *
     * @param opcode The packet id to write information for.
     */
    public PacketBuilder(int opcode) {
        this(opcode, PacketType.FIXED);
    }
    
    /**
     * The PacketBuilder constructor.
     *
     * @param opcode The packet id to write information for.
     */
    public PacketBuilder(int opcode, PacketType type) {
        this.opcode = opcode;
        this.type = type;
    }

    /**
     * Writes the bytes from the argued buffer into this buffer. This method
     * does not modify the argued buffer, and please do not flip the buffer
     * beforehand.
     *
     * @param from the argued buffer that bytes will be written from.
     * @return an instance of this message builder.
     */
    public PacketBuilder putBytes(ByteBuf from) {
        for (int i = 0; i < from.writerIndex(); i++) {
            put(from.getByte(i));
        }
        return this;
    }

    /**
     * Writes {@code buffer}'s bytes onto this PacketBuilder's buffer.
     *
     * @param buffer The buffer to take values from.
     * @return The PacketBuilder instance.
     */
    public PacketBuilder writeBuffer(ByteBuf buffer) {
        this.buffer.writeBytes(buffer);
        return this;
    }
    
    /**
     * Writes the bytes from the argued byte-array into this buffer.
     *
     * @param from the argued buffer that bytes will be written from.
     * @return an instance of this message builder.
     */
    public PacketBuilder putBytes(byte[] from) {
    	buffer.writeBytes(from);
    	return this;
    }

    /**
     * Writes the bytes from the argued buffer into this buffer.
     *
     * @param from the argued buffer that bytes will be written from.
     * @return an instance of this message builder.
     */
    public PacketBuilder putBytes(byte[] from, int size) {
        buffer.writeBytes(from, 0, size);
        return this;
    }

    /**
     * Writes the bytes from the argued byte array into this buffer, in reverse.
     *
     * @param data the data to write to this buffer.
     */
    public PacketBuilder putBytesReverse(byte[] data) {
        for (int i = data.length - 1; i >= 0; i--) {
            put(data[i]);
        }
        return this;
    }

    public PacketBuilder writeByteArray(byte[] bytes, int offset, int length) {
        buffer.writeBytes(bytes, offset, length);
        return this;
    }

    public PacketBuilder writeByteArray(byte[] bytes) {
        buffer.writeBytes(bytes);
        return this;
    }

    /**
     * Writes the value as a variable amount of bits.
     *
     * @param amount the amount of bits to write.
     * @param value  the value of the bits.
     * @return an instance of this message builder.
     * @throws IllegalArgumentException if the number of bits is not between {@code 1} and {@code 32}
     *                                  inclusive.
     */
    public PacketBuilder putBits(int numBits, int value) {
        if (!buffer.hasArray()) {
            throw new UnsupportedOperationException("The ByteBuf implementation must support array() for bit usage.");
        }

        int bytes = (int) Math.ceil((double) numBits / 8D) + 1;
        buffer.ensureWritable((bitPosition + 7) / 8 + bytes);

        final byte[] buffer = this.buffer.array();

        int bytePos = bitPosition >> 3;
        int bitOffset = 8 - (bitPosition & 7);
        bitPosition += numBits;

        for (; numBits > bitOffset; bitOffset = 8) {
            buffer[bytePos] &= ~BIT_MASK[bitOffset];
            buffer[bytePos++] |= (value >> (numBits - bitOffset)) & BIT_MASK[bitOffset];
            numBits -= bitOffset;
        }
        if (numBits == bitOffset) {
            buffer[bytePos] &= ~BIT_MASK[bitOffset];
            buffer[bytePos] |= value & BIT_MASK[bitOffset];
        } else {
            buffer[bytePos] &= ~(BIT_MASK[numBits] << (bitOffset - numBits));
            buffer[bytePos] |= (value & BIT_MASK[numBits]) << (bitOffset - numBits);
        }
        return this;
    }

    /**
     * Starts bit access, whether it's a Bit or Byte.
     *
     * @param packetType The packet's access packetType.
     * @return The PacketBuilder instance.
     */
    public PacketBuilder initializeAccess(AccessType type) {
        switch (type) {
            case BIT:
                bitPosition = buffer.writerIndex() * 8;
                break;
            case BYTE:
                buffer.writerIndex((bitPosition + 7) / 8);
                break;
        }
        return this;
    }

    /**
     * Writes a boolean bit flag.
     *
     * @param flag the flag to write.
     * @return an instance of this message builder.
     */
    public PacketBuilder putBit(boolean flag) {
        putBits(1, flag ? 1 : 0);
        return this;
    }

    /**
     * Writes a value as a {@code byte}.
     *
     * @param value the value to write.
     * @param type  the value type.
     * @return an instance of this message builder.
     */
    public PacketBuilder put(int value, ValueType type) {
        switch (type) {
            case A:
                value += 128;
                break;
            case C:
                value = -value;
                break;
            case S:
                value = 128 - value;
                break;
            case STANDARD:
                break;
        }
        buffer.writeByte((byte) value);
        return this;
    }

    /**
     * Writes a value as a normal {@code byte}.
     *
     * @param value the value to write.
     * @return an instance of this message builder.
     */
    public PacketBuilder put(int value) {
        put(value, ValueType.STANDARD);
        return this;
    }

    /**
     * Writes a value as a {@code short}.
     *
     * @param value the value to write.
     * @param type  the value type.
     * @param order the byte order.
     * @return an instance of this message builder.
     * @throws IllegalArgumentExcpetion if middle or inverse-middle value types are selected.
     */
    public PacketBuilder putShort(int value, ValueType type, ByteOrder order) {
        switch (order) {
            case BIG:
                put(value >> 8);
                put(value, type);
                break;
            case MIDDLE:
                throw new IllegalArgumentException("Middle-endian short is " + "impossible!");
            case INVERSE_MIDDLE:
                throw new IllegalArgumentException("Inverse-middle-endian " + "short is impossible!");
            case LITTLE:
                put(value, type);
                put(value >> 8);
                break;
            case TRIPLE_INT:
                throw new IllegalArgumentException("TRIPLE_INT " + "short not added!");
        }
        return this;
    }

    /**
     * Writes a value as a normal big-endian {@code short}.
     *
     * @param value the value to write.
     * @return an instance of this message builder.
     */
    public PacketBuilder putShort(int value) {
        putShort(value, ValueType.STANDARD, ByteOrder.BIG);
        return this;
    }

    /**
     * Writes a value as a big-endian {@code short}.
     *
     * @param value the value to write.
     * @param type  the value type.
     * @return an instance of this message builder.
     */
    public PacketBuilder putShort(int value, ValueType type) {
        putShort(value, type, ByteOrder.BIG);
        return this;
    }

    /**
     * Writes a value as a standard {@code short}.
     *
     * @param value the value to write.
     * @param order the byte order.
     * @return an instance of this message builder.
     */
    public PacketBuilder putShort(int value, ByteOrder order) {
        putShort(value, ValueType.STANDARD, order);
        return this;
    }

    /**
     * Writes a value as an {@code int}.
     *
     * @param value the value to write.
     * @param type  the value type.
     * @param order the byte order.
     * @return an instance of this message builder.
     */
    public PacketBuilder putInt(int value, ValueType type, ByteOrder order) {
        switch (order) {
            case BIG:
                put(value >> 24);
                put(value >> 16);
                put(value >> 8);
                put(value, type);
                break;
            case MIDDLE:
                put(value >> 8);
                put(value, type);
                put(value >> 24);
                put(value >> 16);
                break;
            case INVERSE_MIDDLE:
                put(value >> 16);
                put(value >> 24);
                put(value, type);
                put(value >> 8);
                break;
            case LITTLE:
                put(value, type);
                put(value >> 8);
                put(value >> 16);
                put(value >> 24);
                break;
            case TRIPLE_INT:
                put((value >> 16));
                put((value >> 8));
                put(value);
                break;
        }
        return this;
    }

    /**
     * Writes a value as a standard big-endian {@code int}.
     *
     * @param value the value to write.
     * @return an instance of this message builder.
     */
    public PacketBuilder putInt(int value) {
        putInt(value, ValueType.STANDARD, ByteOrder.BIG);
        return this;
    }

    /**
     * Writes a value as a big-endian {@code int}.
     *
     * @param value the value to write.
     * @param type  the value type.
     * @return an instance of this message builder.
     */
    public PacketBuilder putInt(int value, ValueType type) {
        putInt(value, type, ByteOrder.BIG);
        return this;
    }

    /**
     * Writes a value as a standard {@code int}.
     *
     * @param value the value to write.
     * @param order the byte order.
     * @return an instance of this message builder.
     */
    public PacketBuilder putInt(int value, ByteOrder order) {
        putInt(value, ValueType.STANDARD, order);
        return this;
    }

    /**
     * Writes a value as a {@code long}.
     *
     * @param value the value to write.
     * @param type  the value type.
     * @param order the byte order.
     * @return an instance of this message builder.
     * @throws UnsupportedOperationException if middle or inverse-middle value types are selected.
     */
    public PacketBuilder putLong(long value, ValueType type, ByteOrder order) {
        switch (order) {
            case BIG:
                put((int) (value >> 56));
                put((int) (value >> 48));
                put((int) (value >> 40));
                put((int) (value >> 32));
                put((int) (value >> 24));
                put((int) (value >> 16));
                put((int) (value >> 8));
                put((int) value, type);
                break;
            case MIDDLE:
                throw new UnsupportedOperationException("Middle-endian long " + "is not implemented!");
            case INVERSE_MIDDLE:
                throw new UnsupportedOperationException("Inverse-middle-endian long is not implemented!");
            case TRIPLE_INT:
                throw new UnsupportedOperationException("triple-int long is not implemented!");
            case LITTLE:
                put((int) value, type);
                put((int) (value >> 8));
                put((int) (value >> 16));
                put((int) (value >> 24));
                put((int) (value >> 32));
                put((int) (value >> 40));
                put((int) (value >> 48));
                put((int) (value >> 56));
                break;
        }
        return this;
    }

    /**
     * Writes a value as a standard big-endian {@code long}.
     *
     * @param value the value to write.
     * @return an instance of this message builder.
     */
    public PacketBuilder putLong(long value) {
        putLong(value, ValueType.STANDARD, ByteOrder.BIG);
        return this;
    }

    /**
     * Writes a value as a big-endian {@code long}.
     *
     * @param value the value to write.
     * @param type  the value type.
     * @return an instance of this message builder.
     */
    public PacketBuilder putLong(long value, ValueType type) {
        putLong(value, type, ByteOrder.BIG);
        return this;
    }

    /**
     * Writes a value as a standard {@code long}.
     *
     * @param value the value to write.
     * @param order the byte order to write.
     * @return an instance of this message builder.
     */
    public PacketBuilder putLong(long value, ByteOrder order) {
        putLong(value, ValueType.STANDARD, order);
        return this;
    }

    /**
     * Writes a RuneScape {@code String} value.
     *
     * @param string the string to write.
     * @return an instance of this message builder.
     */
    public PacketBuilder putString(String string) {
        if (string == null) {
            string = "unkown";
        }
        for (byte value : string.getBytes()) {
            put(value);
        }
        put(10);
        return this;
    }

    /**
     * Gets the packet's opcode.
     *
     * @return the packets opcode.
     */
    public int getOpcode() {
        return opcode;
    }

    /**
     * Gets the packet's size.
     *
     * @return the packets size.
     */
    public int getSize() {
        return buffer.readableBytes();
    }

    /**
     * Gets the backing byte buffer used to read and write data.
     *
     * @return the backing byte buffer.
     */
    public ByteBuf buffer() {
        return buffer;
    }

    /**
     * Creates the actual packet from this builder
     *
     * @return
     */
    public Packet toPacket() {
        return new Packet(opcode, type, buffer);
    }

    public PacketType getType() {
        return type;
    }

    /**
     * Represents an access packetType the packet can have.
     *
     * @author relex lawl
     */
    public enum AccessType {
        BIT,
        BYTE,
    }
}

