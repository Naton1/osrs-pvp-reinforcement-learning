package com.elvarg.net;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;

import java.io.ByteArrayOutputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;


public final class ByteBufUtils {
    /**
     * The terminator used within the client, equal to <tt>10</tt> and otherwise know as the Jagex {@code String}
     * terminator.
     */
    public static final char J_STRING_TERMINATOR = '\n';

    /**
     * The default {@code String} terminator, equal to <tt>0</tt> and otherwise known as the 'null' {@code String}
     * terminator.
     */
    public static final char DEFAULT_STRING_TERMINATOR = '\0';

    /**
     * Gets a 24-bit medium integer from the specified {@link ByteBuffer}, this method does not mark the ByteBuffers
     * current position.
     *
     * @param buffer The ByteBuffer to read from.
     * @return The read 24-bit medium integer.
     */
    public static int getMedium(ByteBuf buffer) {
        return (buffer.readShort() & 0xFFFF) << 8 | buffer.readByte() & 0xFF;
    }

    public static int getMedium(ByteBuffer buffer) {
        return (buffer.getShort() & 0xFFFF) << 8 | buffer.get() & 0xFF;
    }

    /**
     * Gets a null-terminated String from the specified ByteBuffer.
     *
     * @param buffer The ByteBuffer to read from.
     * @return The null-terminated String.
     */
    public static String getString(ByteBuf buffer) {
        return getString(buffer, DEFAULT_STRING_TERMINATOR);
    }

    /**
     * Gets a newline-terminated String from the specified ByteBuffer.
     *
     * @param buffer The ByteBuffer to read from.
     * @return The newline-terminated String.
     */
    public static String getJString(ByteBuf buffer) {
        return getString(buffer, J_STRING_TERMINATOR);
    }

    /**
     * Reads {@code length} bytes from the specified {@link ByteBuffer}.
     *
     * @param buffer The ByteBuffer to read from.
     * @param length The amount of bytes to read.
     * @return The read bytes.
     */
    public static byte[] get(ByteBuf buffer, int length) {
        byte[] data = new byte[length];
        buffer.readBytes(data);
        return data;
    }

    /**
     * Gets a {@link String} from the specified {@link ByteBuffer}, the ByteBuffer will continue to get until the
     * specified {@code terminator} is reached.
     * <p>
     * We use a {@link ByteArrayOutputStream} as it is self expanding. We don't want to waste precious time determining
     * a fixed length for the {@code String}.
     * </p>
     *
     * @param buffer     The ByteBuffer to read from.
     * @param terminator The terminator which denotes when to stop reading.
     * @return The read String.
     */
    public static String getString(ByteBuf buffer, char terminator) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        for (; ; ) {
            int read = buffer.readByte() & 0xFF;
            if (read == terminator) {
                break;
            }
            os.write(read);
        }
        return new String(os.toByteArray());
    }

    /**
     * Gets the host address of the user logging in.
     *
     * @param ctx The context of this channel.
     * @return The host address of this connection.
     */
    public static String getHost(Channel channel) {
        return ((InetSocketAddress) channel.remoteAddress()).getAddress().getHostAddress();
    }


    /**
     * Reads a RuneScape {@code String} value.
     *
     * @return the value of the string.
     */
    public static String readString(ByteBuf buf) {
        byte temp;
        StringBuilder builder = new StringBuilder();
        while (buf.isReadable() && (temp = buf.readByte()) != 10) {
            builder.append((char) temp);
        }
        return builder.toString();
    }

    /**
     * Reads a 'smart' (either a {@code byte} or {@code short} depending on the value) from the
     * specified buffer.
     *
     * @param buffer The buffer.
     * @return The 'smart'.
     */
    public static int getUSmart(ByteBuf buffer) {
        // Reads a single byte from the buffer without modifying the current position.
        int peek = buffer.getByte(buffer.readerIndex()) & 0xFF;
        int value = peek > Byte.MAX_VALUE ? (buffer.readUnsignedShort()) + Short.MIN_VALUE
                : buffer.readUnsignedByte();
        return value;
    }

}
