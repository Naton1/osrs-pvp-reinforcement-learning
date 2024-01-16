package com.elvarg.net.codec;

import java.util.List;

import com.elvarg.net.NetworkConstants;
import com.elvarg.net.PlayerSession;
import com.elvarg.net.packet.Packet;
import com.elvarg.net.security.IsaacRandom;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

/**
 * Decodes packets that are received from the player's channel.
 *
 */
public final class PacketDecoder extends ByteToMessageDecoder {

    private final IsaacRandom random;
    private int opcode;
    private int size;
    
    public PacketDecoder(IsaacRandom random) {
        this.random = random;
        this.opcode = -1;
        this.size = -1;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf buffer, List<Object> out) throws Exception {
        PlayerSession session = ctx.channel().attr(NetworkConstants.SESSION_KEY).get();
        if (session == null || session.getPlayer() == null) {
            return;
        }
        
        int opcode = this.opcode;
        int size = this.size;

        if (opcode == -1) {
            if (buffer.isReadable(1)) {
                opcode = buffer.readUnsignedByte();
                opcode = opcode - random.nextInt() & 0xFF;
                size = PACKET_SIZES[opcode];
                this.opcode = opcode;
                this.size = size;
            } else {
                buffer.discardReadBytes();
                return;
            }
        }

        if (size == -1) {
            if (buffer.isReadable()) {
                size = buffer.readUnsignedByte() & 0xFF;
                this.size = size;
            } else {
                buffer.discardReadBytes();
                return;
            }
        }

        if (buffer.isReadable(size)) {
            byte[] data = new byte[size];
            buffer.readBytes(data);
            this.opcode = -1;
            this.size = -1;
            out.add(new Packet(opcode, Unpooled.copiedBuffer(data)));
        }
    }

    private final static int[] PACKET_SIZES = {
            0, 0, 6, 1, -1, -1, 2, 4, 4, 4, // 0
            4, 13, -1, -1, 8, 0, 6, 2, 2, 0, // 10
            0, 2, 0, 6, 0, 12, 0, 0, 0, 0, // 20
            9, 0, 0, 0, 0, 8, 4, 0, 0, 2, // 30
            2, 6, 0, 8, 0, -1, 0, 0, 0, 1, // 40
            0, 0, 0, 12, 0, 0, 0, 8, 0, 0, // 50
            -1, 8, 0, 0, 0, 0, 0, 0, 0, 0, // 60
            6, 0, 2, 2, 8, 6, 0, -1, 0, 6, // 70
            -1, 0, 0, 0, 0, 1, 4, 6, 0, 0, // 80
            0, 0, 0, 0, 0, 3, 0, 0, -1, 0, // 90
            0, 13, 0, -1, -1, 0, 0, 0, 0, 0, // 100
            0, 0, 0, 0, 0, 0, 0, 8, 0, 0, // 110
            1, 0, 6, 0, 0, 0, -1, 0, 2, 8, // 120
            0, 4, 6, 8, 0, 8, 0, 0, 6, 2, // 130
            0, 0, 0, 0, 0, 8, 0, 0, 0, 0, // 140
            0, 0, 1, 2, 0, 2, 6, 0, 0, 0, // 150
            0, 0, 0, 0, 5, -1, 5, 0, 0, 0, // 160
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, // 170
            0, 8, 0, 2, 4, 4, 5, 6, 8, 1, // 180
            0, 0, 12, 0, 0, 0, 0, 0, 0, 0, // 190
            2, 0, 0, 0, 2, 0, 0, 0, 4, 0, // 200
            4, 0, 0, 0, 9, 8, 8, 0, 10, 0, // 210
            0, 0, 3, 2, 0, 0, -1, 0, 6, 1, // 220
            1, 0, 0, 0, 6, 6, 6, 8, 1, 1, // 230
            0, 4, 0, 0, 0, 0, -1, 0, -1, 4, // 240
            0, 0, 6, 6, 0, 0 // 250
    };
}
