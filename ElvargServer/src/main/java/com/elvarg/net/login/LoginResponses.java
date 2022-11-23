package com.elvarg.net.login;

import com.elvarg.Server;
import com.elvarg.game.World;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.util.DiscordUtil;
import com.elvarg.util.Misc;
import com.elvarg.util.PlayerPunishment;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.Date;

import static com.elvarg.game.GameConstants.PLAYER_PERSISTENCE;

public final class LoginResponses {

    /**
     * This login opcode signifies a successful login.
     */
    public static final int LOGIN_SUCCESSFUL = 2;
    /**
     * This login opcode is used when the player has entered an invalid username
     * and/or password.
     */
    public static final int LOGIN_INVALID_CREDENTIALS = 3;
    /**
     * This login opcode is used when the account has been disabled.
     */
    public static final int LOGIN_DISABLED_ACCOUNT = 4;
    /**
     * This login opcode is used when the player's IP has been disabled.
     */
    public static final int LOGIN_DISABLED_COMPUTER = 22;
    /**
     * This login opcode is used when the player's IP has been disabled.
     */
    public static final int LOGIN_DISABLED_IP = 27;
    /**
     * This login opcode is used when the account attempting to connect is already
     * online in the server.
     */
    public static final int LOGIN_ACCOUNT_ONLINE = 5;
    /**
     * This login opcode is used when the game has been or is being updated.
     */
    public static final int LOGIN_GAME_UPDATE = 6;
    /**
     * This login opcode is used when the world being connected to is full.
     */
    public static final int LOGIN_WORLD_FULL = 7;
    /**
     * This login opcode is used when the connections from an ip address has
     * exceeded
     * {@link org.FileServerConstants.net.NetworkConstants.MAXIMUM_CONNECTIONS}.
     */
    public static final int LOGIN_CONNECTION_LIMIT = 9;
    /**
     * This login opcode is used when a connection has received a bad session id.
     */
    public static final int LOGIN_BAD_SESSION_ID = 10;
    /**
     * This login opcode is used when the login procedure has rejected the session.
     */
    public static final int LOGIN_REJECT_SESSION = 11;
    /**
     * This login opcode is used when a player has entered invalid credentials.
     */
    public static final int INVALID_CREDENTIALS_COMBINATION = 28;
    /**
     * This login opcode is used when a player has attempted to login with a old
     * client.
     */
    public static final int OLD_CLIENT_VERSION = 30;
    /**
     * New account
     */
    public static final int NEW_ACCOUNT = -1;

    public static final int evaluate(Player player, LoginDetailsMessage msg) {
        if (World.getPlayers().isFull()) {
            return LOGIN_WORLD_FULL;
        }

        if (Server.isUpdating()) {
            return LOGIN_GAME_UPDATE;
        }

        if (player.getUsername().startsWith(" ") || player.getUsername().endsWith(" ")
                || !Misc.isValidName(player.getUsername())) {
            return INVALID_CREDENTIALS_COMBINATION;
        }

        if (World.getPlayerByName(player.getUsername()).isPresent()) {
            return LOGIN_ACCOUNT_ONLINE;
        }

        if (PlayerPunishment.banned(player.getUsername())) {
            return LoginResponses.LOGIN_DISABLED_ACCOUNT;
        }

        if (PlayerPunishment.IPBanned(msg.getHost())) {
            return LoginResponses.LOGIN_DISABLED_IP;
        }

        // Attempt to load the character file..
        int playerLoadingResponse = getPlayerResult(player, msg);

        // New player?
        if (playerLoadingResponse == NEW_ACCOUNT) {
            player.setNewPlayer(true);
            player.setCreationDate(new Timestamp(new Date().getTime()));
            playerLoadingResponse = LOGIN_SUCCESSFUL;
        }

        return playerLoadingResponse;
    }

    private static int getDiscordResult(Player player, LoginDetailsMessage msg) {
        try {
            DiscordUtil.DiscordInfo discordInfo;
            if (msg.getUsername().equals(DiscordUtil.DiscordConstants.USERNAME_AUTHZ_CODE)) {
                discordInfo = DiscordUtil.getDiscordInfoWithCode(msg.getPassword());
            } else if (msg.getUsername().equals(DiscordUtil.DiscordConstants.USERNAME_CACHED_TOKEN)) {
                if (!DiscordUtil.isTokenValid(msg.getPassword())) return LoginResponses.LOGIN_INVALID_CREDENTIALS;
                discordInfo = DiscordUtil.getDiscordInfoWithToken(msg.getPassword());
            } else {
                return LoginResponses.LOGIN_INVALID_CREDENTIALS;
            }

            player.setUsername(discordInfo.username);

            var playerSave = PLAYER_PERSISTENCE.load(player.getUsername());
            if (playerSave == null) {
                player.setDiscordLogin(true);
                player.setCachedDiscordAccessToken(discordInfo.token);
                player.setPasswordHashWithSalt(discordInfo.password);
                return LoginResponses.NEW_ACCOUNT;
            }

            playerSave.applyToPlayer(player);
            return LoginResponses.LOGIN_SUCCESSFUL;

        } catch (IOException ex) {
        }

        return LoginResponses.LOGIN_INVALID_CREDENTIALS;
    }

    private static int getPlayerResult(Player player, LoginDetailsMessage msg) {
        String plainPassword = msg.getPassword();
        if (msg.isDiscord()) {
            return getDiscordResult(player, msg);
        }

        var playerSave = PLAYER_PERSISTENCE.load(player.getUsername());
        if (playerSave == null) {
            player.setPasswordHashWithSalt(PLAYER_PERSISTENCE.encryptPassword(plainPassword));
            return LoginResponses.NEW_ACCOUNT;
        }

        if (msg.isDiscord() != playerSave.isDiscordLogin()) {
            // User attempting Discord login on a non-Discord account
            return LoginResponses.LOGIN_BAD_SESSION_ID;
        }

        if (!PLAYER_PERSISTENCE.checkPassword(plainPassword, playerSave)) {
            return LoginResponses.LOGIN_INVALID_CREDENTIALS;
        }

        playerSave.applyToPlayer(player);

        return LoginResponses.LOGIN_SUCCESSFUL;
    }
}
