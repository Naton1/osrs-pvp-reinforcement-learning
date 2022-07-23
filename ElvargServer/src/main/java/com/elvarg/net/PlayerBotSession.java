package com.elvarg.net;

import com.elvarg.game.World;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.net.codec.PacketDecoder;
import com.elvarg.net.codec.PacketEncoder;
import com.elvarg.net.login.LoginDetailsMessage;
import com.elvarg.net.login.LoginResponsePacket;
import com.elvarg.net.login.LoginResponses;
import com.elvarg.net.packet.Packet;
import com.elvarg.net.packet.PacketBuilder;
import com.elvarg.net.packet.PacketConstants;
import com.elvarg.util.Misc;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.SocketChannelConfig;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

public class PlayerBotSession extends PlayerSession {

    private Player player;

    /**
     * Creates a new {@link PlayerSession}.
     *
     * @param channel The SocketChannel.
     */
    public PlayerBotSession() {
        super(new SocketChannel() {
            @Override
            public ServerSocketChannel parent() {
                return null;
            }

            @Override
            public SocketChannelConfig config() {
                return null;
            }

            @Override
            public InetSocketAddress localAddress() {
                return null;
            }

            @Override
            public InetSocketAddress remoteAddress() {
                return null;
            }

            @Override
            public boolean isInputShutdown() {
                return false;
            }

            @Override
            public ChannelFuture shutdownInput() {
                return null;
            }

            @Override
            public ChannelFuture shutdownInput(ChannelPromise channelPromise) {
                return null;
            }

            @Override
            public boolean isOutputShutdown() {
                return false;
            }

            @Override
            public ChannelFuture shutdownOutput() {
                return null;
            }

            @Override
            public ChannelFuture shutdownOutput(ChannelPromise channelPromise) {
                return null;
            }

            @Override
            public boolean isShutdown() {
                return false;
            }

            @Override
            public ChannelFuture shutdown() {
                return null;
            }

            @Override
            public ChannelFuture shutdown(ChannelPromise channelPromise) {
                return null;
            }

            @Override
            public ChannelId id() {
                return null;
            }

            @Override
            public EventLoop eventLoop() {
                return null;
            }

            @Override
            public boolean isOpen() {
                return false;
            }

            @Override
            public boolean isRegistered() {
                return false;
            }

            @Override
            public boolean isActive() {
                return false;
            }

            @Override
            public ChannelMetadata metadata() {
                return null;
            }

            @Override
            public ChannelFuture closeFuture() {
                return null;
            }

            @Override
            public boolean isWritable() {
                return false;
            }

            @Override
            public long bytesBeforeUnwritable() {
                return 0;
            }

            @Override
            public long bytesBeforeWritable() {
                return 0;
            }

            @Override
            public Unsafe unsafe() {
                return null;
            }

            @Override
            public ChannelPipeline pipeline() {
                return null;
            }

            @Override
            public ByteBufAllocator alloc() {
                return null;
            }

            @Override
            public Channel read() {
                return null;
            }

            @Override
            public Channel flush() {
                return null;
            }

            @Override
            public ChannelFuture bind(SocketAddress socketAddress) {
                return null;
            }

            @Override
            public ChannelFuture connect(SocketAddress socketAddress) {
                return null;
            }

            @Override
            public ChannelFuture connect(SocketAddress socketAddress, SocketAddress socketAddress1) {
                return null;
            }

            @Override
            public ChannelFuture disconnect() {
                return null;
            }

            @Override
            public ChannelFuture close() {
                return null;
            }

            @Override
            public ChannelFuture deregister() {
                return null;
            }

            @Override
            public ChannelFuture bind(SocketAddress socketAddress, ChannelPromise channelPromise) {
                return null;
            }

            @Override
            public ChannelFuture connect(SocketAddress socketAddress, ChannelPromise channelPromise) {
                return null;
            }

            @Override
            public ChannelFuture connect(SocketAddress socketAddress, SocketAddress socketAddress1, ChannelPromise channelPromise) {
                return null;
            }

            @Override
            public ChannelFuture disconnect(ChannelPromise channelPromise) {
                return null;
            }

            @Override
            public ChannelFuture close(ChannelPromise channelPromise) {
                return null;
            }

            @Override
            public ChannelFuture deregister(ChannelPromise channelPromise) {
                return null;
            }

            @Override
            public ChannelFuture write(Object o) {
                return null;
            }

            @Override
            public ChannelFuture write(Object o, ChannelPromise channelPromise) {
                return null;
            }

            @Override
            public ChannelFuture writeAndFlush(Object o, ChannelPromise channelPromise) {
                return null;
            }

            @Override
            public ChannelFuture writeAndFlush(Object o) {
                return null;
            }

            @Override
            public ChannelPromise newPromise() {
                return null;
            }

            @Override
            public ChannelProgressivePromise newProgressivePromise() {
                return null;
            }

            @Override
            public ChannelFuture newSucceededFuture() {
                return null;
            }

            @Override
            public ChannelFuture newFailedFuture(Throwable throwable) {
                return null;
            }

            @Override
            public ChannelPromise voidPromise() {
                return null;
            }

            @Override
            public <T> Attribute<T> attr(AttributeKey<T> attributeKey) {
                return null;
            }

            @Override
            public <T> boolean hasAttr(AttributeKey<T> attributeKey) {
                return false;
            }

            @Override
            public int compareTo(Channel o) {
                return 0;
            }
        });
    }

    /**
     * Attempts to finalize a player's login.
     *
     * @param msg The player's login information.
     */
    public void finalizeLogin(LoginDetailsMessage msg) {
    }

    /**
     * Queues a recently decoded packet received from the channel.
     *
     * @param msg The packet that should be queued.
     */
    public void queuePacket(Packet msg) {
    }

    /**
     * Processes all of the queued messages from the {@link PacketDecoder} by
     * polling the internal queue, and then handling them via the
     * handleInputMessage. This method is called EACH GAME CYCLE.
     *
     */
    public void processPackets() {
    }

    /**
     * Queues the {@code msg} for this session to be encoded and sent to the client.
     *
     * @param builder the packet to queue.
     */
    public void write(PacketBuilder builder) {
    }

    /**
     * Flushes this channel.
     */
    public void flush() {
    }

    /**
     * Gets the player I/O operations will be executed for.
     *
     * @return the player I/O operations.
     */
    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public Channel getChannel() {
        return null;
    }
}
