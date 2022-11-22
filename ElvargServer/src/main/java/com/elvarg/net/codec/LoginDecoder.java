package com.elvarg.net.codec;

import com.elvarg.Server;
import com.elvarg.game.GameConstants;
import com.elvarg.net.ByteBufUtils;
import com.elvarg.net.NetworkConstants;
import com.elvarg.net.login.LoginDetailsMessage;
import com.elvarg.net.login.LoginResponses;
import com.elvarg.net.security.IsaacRandom;
import com.elvarg.util.DiscordUtil;
import com.elvarg.util.Misc;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.List;
import java.util.Random;

/**
 * Attempts to decode a player's login request.
 *
 * @author Professor Oak
 */
public final class LoginDecoder extends ByteToMessageDecoder {

    /**
     * Generates random numbers via secure cryptography. Generates the session key
     * for packet encryption.
     */
    private static final Random random = new SecureRandom();
    private static final int SECRET_VALUE = 345749224;

    /**
     * The size of the encrypted data.
     */
    private int encryptedLoginBlockSize;

    /**
     * The current login decoder state
     */
    private LoginDecoderState state = LoginDecoderState.LOGIN_REQUEST;

    /**
     * The size of the encrypted data.
     */
    private String hostAddressOverride = null;

    /**
     * Sends a response code to the client to notify the user logging in.
     *
     * @param ctx      The context of the channel handler.
     * @param response The response code to send.
     */
    public static void sendLoginResponse(ChannelHandlerContext ctx, int response) {
        ByteBuf buffer = Unpooled.buffer(Byte.BYTES);
        buffer.writeByte(response);
        ctx.writeAndFlush(buffer).addListener(ChannelFutureListener.CLOSE);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf buffer, List<Object> out) throws Exception {
        switch (state) {

            case LOGIN_REQUEST:
                decodeRequest(ctx, buffer);
                break;

            case LOGIN_TYPE:
                decodeType(ctx, buffer);
                break;

            case LOGIN:
                decodeLogin(ctx, buffer, out);
                break;
        }
    }

    private void decodeRequest(ChannelHandlerContext ctx, ByteBuf buffer) {

        if (!buffer.isReadable()) {
            ctx.channel().close();
            return;
        }

        int request = buffer.readUnsignedByte();
        if (request != NetworkConstants.LOGIN_REQUEST_OPCODE) {
            Server.getLogger().info("Session rejected for bad login request id: " + request);
            sendLoginResponse(ctx, LoginResponses.LOGIN_BAD_SESSION_ID);
            return;
        }

        if (buffer.isReadable(8)) {
            int secret = buffer.readInt();

            if (secret != SECRET_VALUE) {
                Server.getLogger().info("Invalid secret value given: " + secret);
                sendLoginResponse(ctx, LoginResponses.LOGIN_BAD_SESSION_ID);
                return;
            }
            int ip = buffer.readInt();

            hostAddressOverride = String.format("%d.%d.%d.%d",
                    (ip & 0xff),
                    (ip >> 8 & 0xff),
                    (ip >> 16 & 0xff),
                    (ip >> 24 & 0xff));
        }

        // Send information to the client
        ByteBuf buf = Unpooled.buffer(Byte.BYTES + Long.BYTES);
        buf.writeByte(0); // 0 = continue login
        buf.writeLong(random.nextLong()); // This long will be used for
        // encryption later on
        ctx.writeAndFlush(buf);

        state = LoginDecoderState.LOGIN_TYPE;
    }

    private void decodeType(ChannelHandlerContext ctx, ByteBuf buffer) {

        if (!buffer.isReadable()) {
            ctx.channel().close();
            return;
        }

        int connectionType = buffer.readUnsignedByte();
        if (connectionType != NetworkConstants.NEW_CONNECTION_OPCODE
                && connectionType != NetworkConstants.RECONNECTION_OPCODE) {
            Server.getLogger().info("Session rejected for bad connection type id: " + connectionType);
            sendLoginResponse(ctx, LoginResponses.LOGIN_BAD_SESSION_ID);
            return;
        }

        state = LoginDecoderState.LOGIN;
    }

    private void decodeLogin(ChannelHandlerContext ctx, ByteBuf buffer, List<Object> out) {

        if (!buffer.isReadable()) {
            ctx.channel().close();
            return;
        }

        encryptedLoginBlockSize = buffer.readUnsignedByte();

        if (encryptedLoginBlockSize != buffer.readableBytes()) {
            Server.getLogger().info(String.format("[host= %s] encryptedLoginBlockSize != readable bytes", ctx.channel().remoteAddress()));
            sendLoginResponse(ctx, LoginResponses.LOGIN_REJECT_SESSION);
            return;
        }

        if (buffer.isReadable(encryptedLoginBlockSize)) {

            int magicId = buffer.readUnsignedByte();
            if (magicId != 0xFF) {
                Server.getLogger().info(String.format("[host= %s] [magic= %d] was rejected for the wrong magic value.",
                        ctx.channel().remoteAddress(), magicId));
                sendLoginResponse(ctx, LoginResponses.LOGIN_REJECT_SESSION);
                return;
            }

            int memory = buffer.readByte();
            if (memory != 0 && memory != 1) {
                Server.getLogger().info(String.format("[host= %s] was rejected for having the memory setting.",
                        ctx.channel().remoteAddress()));
                sendLoginResponse(ctx, LoginResponses.LOGIN_REJECT_SESSION);
                return;
            }

			/*
             * int[] archiveCrcs = new int[9]; for (int i = 0; i < 9; i++) { archiveCrcs[i]
			 * = buffer.readInt(); }
			 */

            /**
             * Our RSA components.
             */
            int length = buffer.readUnsignedByte();
            byte[] rsaBytes = new byte[length];
            buffer.readBytes(rsaBytes);

            ByteBuf rsaBuffer = Unpooled.wrappedBuffer(new BigInteger(rsaBytes)
                    .modPow(NetworkConstants.RSA_EXPONENT, NetworkConstants.RSA_MODULUS).toByteArray());

            int securityId = rsaBuffer.readByte();
            if (securityId != 10 && securityId != 11) {
                Server.getLogger().info(String.format("[host= %s] was rejected for having the wrong securityId.",
                        ctx.channel().remoteAddress()));
                sendLoginResponse(ctx, LoginResponses.LOGIN_REJECT_SESSION);
                return;
            }

            long clientSeed = rsaBuffer.readLong();
            long seedReceived = rsaBuffer.readLong();

            int[] seed = {(int) (clientSeed >> 32), (int) clientSeed, (int) (seedReceived >> 32), (int) seedReceived};
            IsaacRandom decodingRandom = new IsaacRandom(seed);
            for (int i = 0; i < seed.length; i++) {
                seed[i] += 50;
            }

            int uid = rsaBuffer.readInt();
            if (uid != GameConstants.CLIENT_UID) {
                Server.getLogger().info(String.format("[host= %s] was rejected for having the wrong UID.",
                        ctx.channel().remoteAddress()));
                sendLoginResponse(ctx, LoginResponses.OLD_CLIENT_VERSION);
                return;
            }

            String host = hostAddressOverride;
            if (host == null) {
                host = ByteBufUtils.getHost(ctx.channel());
            }

            String rawUsername = ByteBufUtils.readString(rsaBuffer);
            String password = ByteBufUtils.readString(rsaBuffer);

            if (securityId == 10) {
                String username = Misc.formatText(rawUsername.toLowerCase());

                if (username.length() < 3 || username.length() > 30 || password.length() < 3 || password.length() > 30) {
                    sendLoginResponse(ctx, LoginResponses.INVALID_CREDENTIALS_COMBINATION);
                    return;
                }

                out.add(new LoginDetailsMessage(ctx, username, password, host,
                        new IsaacRandom(seed), decodingRandom));
            } else if (securityId == 11) {
                if (rawUsername.equals(DiscordUtil.DiscordConstants.USERNAME_CACHED_TOKEN) || rawUsername.equals(DiscordUtil.DiscordConstants.USERNAME_AUTHZ_CODE)) {
                    var msg = new LoginDetailsMessage(ctx, rawUsername, password, host,
                            new IsaacRandom(seed), decodingRandom);
                    msg.setDiscord(true);
                    out.add(msg);
                } else {
                    sendLoginResponse(ctx, LoginResponses.INVALID_CREDENTIALS_COMBINATION);
                }
            }
        }
    }

    private enum LoginDecoderState {
        LOGIN_REQUEST, LOGIN_TYPE, LOGIN;
    }
}
