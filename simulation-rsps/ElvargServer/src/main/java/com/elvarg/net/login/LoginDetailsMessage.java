package com.elvarg.net.login;

import com.elvarg.net.packet.Packet;
import com.elvarg.net.security.IsaacRandom;
import io.netty.channel.ChannelHandlerContext;

import java.nio.channels.Channel;

/**
 * The {@link Packet} implementation that contains data used for the final
 * portion of the login protocol.
 *
 * @author lare96 <http://github.org/lare96>
 */
public final class LoginDetailsMessage {

    /**
     * The context to which this player is going through.
     */
    private final ChannelHandlerContext context;

    /**
     * The username of the player.
     */
    private final String username;

    /**
     * The password of the player.
     */
    private final String password;

    /**
     * The player's host address
     */
    private final String host;

    /**
     * The encrypting isaac
     */
    private final IsaacRandom encryptor;

    /**
     * The decrypting isaac
     */
    private final IsaacRandom decryptor;

    private boolean isDiscord = false;

    /**
     * Creates a new {@link LoginDetailsMessage}.
     *
     * @param ctx       the {@link ChannelHandlerContext} that holds our
     *                  {@link Channel} instance.
     * @param username  the username of the player.
     * @param password  the password of the player.
     * @param encryptor the encryptor for encrypting messages.
     * @param decryptor the decryptor for decrypting messages.
     */
    public LoginDetailsMessage(ChannelHandlerContext context, String username, String password, String host,
                               IsaacRandom encryptor, IsaacRandom decryptor) {
        this.context = context;
        this.username = username;
        this.password = password;
        this.host = host;
        this.encryptor = encryptor;
        this.decryptor = decryptor;
    }
    
    public ChannelHandlerContext getContext() {
        return context;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getHost() {
        return host;
    }

    public IsaacRandom getEncryptor() {
        return encryptor;
    }

    public IsaacRandom getDecryptor() {
        return decryptor;
    }

    public boolean isDiscord() {
        return isDiscord;
    }

    public void setDiscord(boolean discord) {
        isDiscord = discord;
    }
}
