package com.elvarg.net.packet;

import io.netty.buffer.ByteBuf;

/**
 * Manages reading packet information from the netty's channel.
 *
 * @author relex lawl
 */

public class Packet {

    /**
     * The packet id being received.
     */
    private final int opcode;
    
    /**
     * The packet type.
     */
    private final PacketType type;
    
    /**
     * The buffer being used to read the packet information.
     */
    private ByteBuf buffer;

    /**
     * The Packet constructor.
     *
     * @param opcode     The packet id.
     * @param buffer     The buffer used to receive information from the netty's channel.
     */
    public Packet(int opcode, ByteBuf buffer) {
        this(opcode, PacketType.FIXED, buffer);
    }
    
    /**
     * The Packet constructor.
     *
     * @param opcode     The packet id.
     * @param packetType The packetType of packet being read.
     * @param buffer     The buffer used to receive information from the netty's channel.
     */
    public Packet(int opcode, PacketType type, ByteBuf buffer) {
        this.opcode = opcode;
        this.type = type;
        this.buffer = buffer;
    }

    /**
     * Gets the packet id.
     *
     * @return The packet id being sent.
     */
    public int getOpcode() {
        return opcode;
    }

    /**
     * Gets the buffer used to receive the packet information.
     *
     * @return The ChannelBuffer instance.
     */
    public ByteBuf getBuffer() {
        return buffer;
    }

    /**
     * Gets the size of the packet being read.
     *
     * @return The size of the packet.
     */
    public int getSize() {
        return buffer.readableBytes();
    }

    public int getLength() {
        return buffer.capacity();
    }

    /**
     * Reads an unsigned byte from the packet.
     *
     * @return The unsigned byte.
     */
    public byte readByte() {
        byte b = 0;
        try {
            b = buffer.readByte();
        } catch (Exception e) {

        }
        return b;
    }

    /**
     * Reads a packetType-A byte from the packet.
     *
     * @return The unsigned byte - 128.
     */
    public byte readByteA() {
        return (byte) (readByte() - 128);
    }

    /**
     * Reads an inverse (negative) unsigned byte from the packet.
     *
     * @return readByte()
     */
    public byte readByteC() {
        return (byte) (-readByte());
    }

    /**
     * Reads a packetType-S byte from the packet.
     *
     * @return 128 - the unsigned byte value.
     */
    public byte readByteS() {
        return (byte) (128 - readByte());
    }

    /**
     * Reads an unsigned packetType-S byte from the packet.
     *
     * @return The unsigned readByteS value.
     */
    public int readUnsignedByteS() {
        return readByteS() & 0xff;
    }

    /**
     * Reads a byte array from the packet
     */
    public Packet readBytes(byte[] bytes) {
        buffer.readBytes(bytes);
        return this;
    }

    /**
     * Reads said amount of bytes from the packet.
     *
     * @param amount The amount of bytes to read.
     * @return The bytes array values.
     */
    public byte[] readBytes(int amount) {
        byte[] bytes = new byte[amount];
        for (int i = 0; i < amount; i++) {
            bytes[i] = readByte();
        }
        return bytes;
    }

    /**
     * Reads the amount of bytes packetType-A.
     *
     * @param amount The amount of bytes packetType-A to read.
     * @return The bytes array values.
     */
    public byte[] readBytesA(int amount) {
        if (amount < 0)
            throw new NegativeArraySizeException("The byte array amount cannot have a negative value!");
        byte[] bytes = new byte[amount];
        for (int i = 0; i < amount; i++) {
            bytes[i] = (byte) (readByte() + 128);
        }
        return bytes;
    }

    /**
     * Reads said amount of reversed-bytes from the packet.
     *
     * @param amount The amount of bytes to read.
     * @return The bytes array values.
     */
    public byte[] readReversedBytesA(int amount) {
        byte[] bytes = new byte[amount];
        int position = amount - 1;
        for (; position >= 0; position--) {
            bytes[position] = (byte) (readByte() + 128);
        }
        return bytes;
    }

    /**
     * Reads an unsigned byte.
     *
     * @return The unsigned byte value read from the packet.
     */
    public int readUnsignedByte() {
        return buffer.readUnsignedByte();
    }

    /**
     * Reads a short value.
     *
     * @return The short value read from the packet.
     */
    public short readShort() {
        return buffer.readShort();
    }

    /**
     * Reads a short packetType-A from the packet.
     *
     * @return The short packetType-A value.
     */
    public short readShortA() {
        int value = ((readByte() & 0xFF) << 8) | (readByte() - 128 & 0xFF);
        return (short) (value > 32767 ? value - 0x10000 : value);
    }

    /**
     * Reads a little-endian short from the packet.
     *
     * @return The little-endian short value.
     */
    public short readLEShort() {
        int value = (readByte() & 0xFF) | (readByte() & 0xFF) << 8;
        return (short) (value > 32767 ? value - 0x10000 : value);
    }

    /**
     * Reads a little-endian packetType-A short from the packet.
     *
     * @return The little-endian packetType-A short value.
     */
    public short readLEShortA() {
        int value = (readByte() - 128 & 0xFF) | (readByte() & 0xFF) << 8;
        return (short) (value > 32767 ? value - 0x10000 : value);
    }

    /**
     * Reads the unsigned short value from the packet.
     *
     * @return The unsigned short value.
     */
    public int readUnsignedShort() {
        return buffer.readUnsignedShort();
    }

    /**
     * Reads the unsigned short value packetType-A from the packet.
     *
     * @return The unsigned short packetType-A value.
     */
    public int readUnsignedShortA() {
        int value = 0;
        value |= readUnsignedByte() << 8;
        value |= (readByte() - 128) & 0xff;
        return value;
    }

    /**
     * Reads an int value from the packet.
     *
     * @return The int value.
     */
    public int readInt() {
        return buffer.readInt();
    }

    /**
     * Reads a single int value from the packet.
     *
     * @return The single int value.
     */
    public int readSingleInt() {
        byte firstByte = readByte(), secondByte = readByte(), thirdByte = readByte(), fourthByte = readByte();
        return ((thirdByte << 24) & 0xFF) | ((fourthByte << 16) & 0xFF) | ((firstByte << 8) & 0xFF) | (secondByte & 0xFF);
    }

    /**
     * Reads a double int value from the packet.
     *
     * @return The double int value.
     */
    public int readDoubleInt() {
        int firstByte = readByte() & 0xFF, secondByte = readByte() & 0xFF, thirdByte = readByte() & 0xFF, fourthByte = readByte() & 0xFF;
        return ((secondByte << 24) & 0xFF) | ((firstByte << 16) & 0xFF) | ((fourthByte << 8) & 0xFF) | (thirdByte & 0xFF);
    }

    /**
     * Reads a triple int value from the packet.
     *
     * @return The triple int value.
     */
    public int readTripleInt() {
        return ((readByte() << 16) & 0xFF) | ((readByte() << 8) & 0xFF) | (readByte() & 0xFF);
    }

    /**
     * Reads the long value from the packet.
     *
     * @return The long value.
     */
    public long readLong() {
        return buffer.readLong();
    }

    public byte[] getBytesReverse(int amount, ValueType type) {
        byte[] data = new byte[amount];
        int dataPosition = 0;
        for (int i = buffer.writerIndex() + amount - 1; i >= buffer.writerIndex(); i--) {
            int value = buffer.getByte(i);
            switch (type) {
                case A:
                    value -= 128;
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
            data[dataPosition++] = (byte) value;
        }
        return data;
    }

    /**
     * Reads the string value from the packet.
     *
     * @return The string value.
     */
    public String readString() {
        StringBuilder builder = new StringBuilder();
        byte value;
        while (buffer.isReadable() && (value = buffer.readByte()) != 10) {
            builder.append((char) value);
        }
        return builder.toString();
    }

    /**
     * Reads a smart value from the packet.
     *
     * @return The smart value.
     */
    public int readSmart() {
        return buffer.getByte(buffer.readerIndex()) < 128 ? readByte() & 0xFF : (readShort() & 0xFFFF) - 32768;
    }

    /**
     * Reads a signed smart value from the packet.
     *
     * @return The signed smart value.
     */
    public int readSignedSmart() {
        return buffer.getByte(buffer.readerIndex()) < 128 ? (readByte() & 0xFF) - 64 : (readShort() & 0xFFFF) - 49152;
    }

    @Override
    public String toString() {
        return "Packet - [opcode, size] : [" + getOpcode() + ", " + getSize() + "]";
    }

    public PacketType getType() {
        return type;
    }
}