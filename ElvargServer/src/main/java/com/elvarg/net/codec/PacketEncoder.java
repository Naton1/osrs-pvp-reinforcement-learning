package com.elvarg.net.codec;

import com.elvarg.net.packet.Packet;
import com.elvarg.net.packet.PacketType;
import com.elvarg.net.security.IsaacRandom;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * Encodes packets before they're sent to the channel.
 */
public class PacketEncoder extends MessageToByteEncoder<Packet> {

    /**
     * The encoder used for encryption of the packet.
     */
    private final IsaacRandom encoder;

    /**
     * The GamePacketEncoder constructor.
     *
     * @param encoder The encoder used for the packets.
     */
    public PacketEncoder(IsaacRandom encoder) {
        this.encoder = encoder;
    }


    @Override
    protected void encode(ChannelHandlerContext ctx, Packet packet, ByteBuf out) throws Exception {
        final int opcode = (packet.getOpcode() + encoder.nextInt()) & 0xFF;
        PacketType type = packet.getType();
        final int size = packet.getSize();

//        System.err.println("{PacketEncoder} Send opcode: " + packet.getOpcode() + " \t  size: " + size + " \t encOp: " + opcode);

        // Used for finding incorrect client pkt sizes
        if (type == PacketType.FIXED) {
            int currSize = CLIENT_PACKET_SIZES[packet.getOpcode()];
            if (size != currSize) {
                System.err.println("{PacketEncoder} Opcode " + packet.getOpcode() + " has defined size " + currSize + " but is actually " + size + ".");
                return;
            }
        } else if (type == PacketType.VARIABLE) {
            int currSize = CLIENT_PACKET_SIZES[packet.getOpcode()];
            if (currSize != -1) {
                System.err.println("{PacketEncoder} Opcode " + packet.getOpcode() + "'s size needs to be -1, it's currently " + currSize + ".");
                return;
            }
        } else if (type == PacketType.VARIABLE_SHORT) {
            int currSize = CLIENT_PACKET_SIZES[packet.getOpcode()];
            if (currSize != -2) {
                System.err.println("{PacketEncoder} Opcode " + packet.getOpcode() + "'s size needs to be -2, it's currently " + currSize + ".");
                return;
            }
        }
        
        int finalSize = size + 1;
        switch (type) {
        case VARIABLE:
            
            if (size > 255) { // trying to send more data then we can represent with 8 bits!
                throw new IllegalArgumentException("Tried to send packet length " + size + " in variable-byte packet");
            }
            finalSize++;
            break;
        case VARIABLE_SHORT:
            if (size > 65535) { // trying to send more data then we can represent with 8 bits!
                throw new IllegalArgumentException("Tried to send packet length " + size + " in variable-short packet");
            }
            finalSize += 2;
            break;
        default:
            break;
        }

        // Create a new buffer
        ByteBuf buffer = Unpooled.buffer(finalSize);
        
        // Write opcode
        buffer.writeByte(opcode);
        
        // Write packet size
        switch (type) {
        case VARIABLE:
            buffer.writeByte((byte) size);
            break;
        case VARIABLE_SHORT:
            buffer.writeShort((short) size);
            break;
        default:
            break;
        }
        
        // Write packet
        buffer.writeBytes(packet.getBuffer());
        
        // Write the packet to the out buffer
        out.writeBytes(buffer);
    }

    public static final int[] CLIENT_PACKET_SIZES = {
            0, 0, 0, 1, 6, 0, 0, 0, 4, 4, //0
            6, 2, -1, 1, 1, -1, 1, 0, 0, 0, // 10
            0, 0, 0, 0, 1, 0, 0, -1, 1, 1, //20
            0, 0, 0, 0, -2, 4, 3, 0, 2, 0, //30
            0, 0, 0, 0, 7, 8, 0, 6, 0, 0, //40
            9, 8, 0, -2, 4, 1, 0, 0, 0, 0, //50
            -2, 1, 0, 0, 2, -2, 0, 0, 0, 0, //60
            6, 3, 2, 4, 2, 4, 0, 0, 0, 4, //70
            0, -2, 0, 0, 11, 2, 1, 6, 6, 0, //80
            0, 0, 0, 0, 0, 0, 0, 2, 0, 1, //90
            2, 2, 0, 1, -1, 8, 1, 0, 8, 0, //100
            1, 1, 1, 1, 2, 1, 5, 15, 0, 0, //110
            0, 4, 4, -1, 9, -1, -2, 2, 0, 0, //120 // 9
            -1, 0, 0, 0, 13, 0, 0, 1, 0, 0, // 130
            3, 10, 2, 0, 0, 0, 0, 14, 0, 0, //140
            0, 4, 5, 3, 0, 0, 3, 0, 0, 0, //150
            4, 5, 0, 0, 2, 0, 6, 5, 0, 0, //160
            0, 5, -2, -2, 7, 5, 10, 6, 0, -2, // 170
            0, 0, 0, 1, 1, 2, 1, -1, 0, 0, //180
            0, 0, 0, 0, 0, 2, -1, 0, -1, 0, //190
            4, 0, 0, 0, 0, 0, 3, 0, 4, 0,  //200
            0, 0, 0, 0, -2, 7, 0, -2, 2, 0, //210
            0, 1, -2, -2, 0, 0, 0, 0, 0, 0, // 220
            8, 0, 0, 0, 0, 0, 0, 0, 0, 0,//230
            2, -2, 0, 0, -1, 0, 6, 0, 4, 3,//240
            -1, 0, -1, -1, 6, 0, 0//250
        };
}
