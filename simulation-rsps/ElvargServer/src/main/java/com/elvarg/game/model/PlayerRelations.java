package com.elvarg.game.model;

import com.elvarg.game.World;
import com.elvarg.game.content.clan.ClanChatManager;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.util.Misc;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * This file represents a player's relation with other world entities, this
 * manages adding and removing friends who we can chat with and also adding and
 * removing ignored players who will not be able to message us or see us online.
 *
 * @author relex lawl Redone a bit by Gabbe
 */

public class PlayerRelations {

    /**
     * The player's current friend status, checks if others will be able to see them
     * online.
     */
    private PrivateChatStatus status = PrivateChatStatus.ON;

    /**
     * This map contains the player's friends list.
     */
    private List<Long> friendList = new ArrayList<Long>(200);

    /**
     * This map contains the player's ignore list.
     */
    private List<Long> ignoreList = new ArrayList<Long>(100);

    /**
     * The player's current private message index.
     */
    private int privateMessageId = 1;
    /**
     * The associated player.
     */
    private Player player;

    /**
     * The PlayerRelations constructor.
     *
     * @param player The associated-player.
     */
    public PlayerRelations(Player player) {
        this.player = player;
    }

    /**
     * Gets the current private message index.
     *
     * @return The current private message index + 1.
     */
    public int getPrivateMessageId() {
        return privateMessageId++;
    }

    /**
     * Sets the current private message index.
     *
     * @param privateMessageId The new private message index value.
     * @return The PlayerRelations instance.
     */
    public PlayerRelations setPrivateMessageId(int privateMessageId) {
        this.privateMessageId = privateMessageId;
        return this;
    }

    public PlayerRelations setStatus(PrivateChatStatus status, boolean update) {
        this.status = status;
        if (update) {
            updateLists(true);
        }
        return this;
    }

    public PrivateChatStatus getStatus() {
        return this.status;
    }

    /**
     * Gets the player's friend list.
     *
     * @return The player's friends.
     */
    public List<Long> getFriendList() {
        return friendList;
    }

    /**
     * Gets the player's ignore list.
     *
     * @return The player's ignore list.
     */
    public List<Long> getIgnoreList() {
        return ignoreList;
    }

    /**
     * Updates the player's friend list.
     *
     * @param online If <code>true</code>, the players who have this player added, will
     *               be sent the notification this player has logged in.
     * @return The PlayerRelations instance.
     */
    public PlayerRelations updateLists(boolean online) {
        if (status == PrivateChatStatus.OFF)
            online = false;
        player.getPacketSender().sendFriendStatus(2);
        for (Player players : World.getPlayers()) {
            if (players == null)
                continue;
            boolean temporaryOnlineStatus = online;
            if (players.getRelations().friendList.contains(player.getLongUsername())) {
                if (status.equals(PrivateChatStatus.FRIENDS_ONLY) && !friendList.contains(players.getLongUsername())
                        || status.equals(PrivateChatStatus.OFF) || ignoreList.contains(players.getLongUsername())) {
                    temporaryOnlineStatus = false;
                }
                players.getPacketSender().sendFriend(player.getLongUsername(), temporaryOnlineStatus ? 1 : 0);
            }
            boolean tempOn = true;
            if (player.getRelations().friendList.contains(players.getLongUsername())) {
                if (players.getRelations().status.equals(PrivateChatStatus.FRIENDS_ONLY)
                        && !players.getRelations().getFriendList().contains(player.getLongUsername())
                        || players.getRelations().status.equals(PrivateChatStatus.OFF)
                        || players.getRelations().getIgnoreList().contains(player.getLongUsername())) {
                    tempOn = false;
                }
                player.getPacketSender().sendFriend(players.getLongUsername(), tempOn ? 1 : 0);
            }
        }
        return this;
    }

    public void sendPrivateStatus() {
        int privateChat = status == PrivateChatStatus.OFF ? 2 : status == PrivateChatStatus.FRIENDS_ONLY ? 1 : 0;
        player.getPacketSender().sendChatOptions(0, privateChat, 0);
    }

    public void sendFriends() {
        for (long l : friendList) {
            player.getPacketSender().sendFriend(l, 0);
        }
    }

    public void sendIgnores() {
        for (long l : ignoreList) {
            player.getPacketSender().sendAddIgnore(l);
        }
    }

    public void sendAddFriend(long name) {
        player.getPacketSender().sendFriend(name, 0);
    }

    public void sendDeleteFriend(long name) {
        player.getPacketSender().sendDeleteFriend(name);
    }

    public void sendAddIgnore(long name) {
        player.getPacketSender().sendAddIgnore(name);
    }

    public void sendDeleteIgnore(long name) {
        player.getPacketSender().sendDeleteIgnore(name);
    }

    public PlayerRelations onLogin(Player player) {
        sendIgnores();
        sendFriends();
        sendPrivateStatus();
        return this;
    }

    /**
     * Adds a player to the associated-player's friend list.
     *
     * @param username The user name of the player to add to friend list.
     */
    public void addFriend(Long username) {
        String name = Misc.formatName(Misc.longToString(username));
        if (name.equals(player.getUsername())) {
            return;
        }
        if (friendList.size() >= 200) {
            player.getPacketSender().sendMessage("Your friend list is full!");
            return;
        }
        if (ignoreList.contains(username)) {
            player.getPacketSender().sendMessage("Please remove " + name + " from your ignore list first.");
            return;
        }
        if (friendList.contains(username)) {
            player.getPacketSender().sendMessage(name + " is already on your friends list!");
        } else {
            friendList.add(username);
            sendAddFriend(username);
            updateLists(true);
            Optional<Player> friend = World.getPlayerByName(name);
            if (friend.isPresent()) {
                friend.get().getRelations().updateLists(true);
                ClanChatManager.updateRank(ClanChatManager.getClanChat(player), friend.get());
                if (player.getInterfaceId() == ClanChatManager.CLAN_CHAT_SETUP_INTERFACE_ID) {
                	ClanChatManager.clanChatSetupInterface(player);
                }
            }
        }
    }

    /*
     * Checks if a player is friend with someone.
     */
    public boolean isFriendWith(String player) {
        return friendList.contains(Misc.stringToLong(player));
    }

    /**
     * Deletes a friend from the associated-player's friends list.
     *
     * @param username The user name of the friend to delete.
     */
    public void deleteFriend(Long username) {
        String name = Misc.formatName(Misc.longToString(username));
        if (name.equals(player.getUsername())) {
            return;
        }
        if (friendList.contains(username)) {
            friendList.remove(username);
            sendDeleteFriend(username);
            updateLists(false);
            Optional<Player> unfriend = World.getPlayerByName(name);
            if (unfriend.isPresent()) {
                unfriend.get().getRelations().updateLists(false);
                ClanChatManager.updateRank(ClanChatManager.getClanChat(player), unfriend.get());
                if (player.getInterfaceId() == ClanChatManager.CLAN_CHAT_SETUP_INTERFACE_ID) {
                	ClanChatManager.clanChatSetupInterface(player);
                }
            }
        } else {
            player.getPacketSender().sendMessage("This player is not on your friends list!");
        }
    }

    /**
     * Adds a player to the associated-player's ignore list.
     *
     * @param username The user name of the player to add to ignore list.
     */
    public void addIgnore(Long username) {
        String name = Misc.formatName(Misc.longToString(username));
        if (name.equals(player.getUsername())) {
            return;
        }
        if (ignoreList.size() >= 100) {
            player.getPacketSender().sendMessage("Your ignore list is full!");
            return;
        }
        if (friendList.contains(username)) {
            player.getPacketSender().sendMessage("Please remove " + name + " from your friend list first.");
            return;
        }
        if (ignoreList.contains(username)) {
            player.getPacketSender().sendMessage(name + " is already on your ignore list!");
        } else {
            ignoreList.add(username);
            sendAddIgnore(username);
            updateLists(true);
            Optional<Player> ignored = World.getPlayerByName(name);
            if (ignored.isPresent())
                ignored.get().getRelations().updateLists(false);
        }
    }

    /**
     * Deletes an ignored player from the associated-player's ignore list.
     *
     * @param username The user name of the ignored player to delete from ignore list.
     */
    public void deleteIgnore(Long username) {
        String name = Misc.formatName(Misc.longToString(username));
        if (name.equals(player.getUsername())) {
            return;
        }
        if (ignoreList.contains(username)) {
            ignoreList.remove(username);
            sendDeleteIgnore(username);
            updateLists(true);
            if (status.equals(PrivateChatStatus.ON)) {
                Optional<Player> ignored = World.getPlayerByName(name);
                if (ignored.isPresent())
                    ignored.get().getRelations().updateLists(true);
            }
        } else {
            player.getPacketSender().sendMessage("This player is not on your ignore list!");
        }
    }

    /**
     * Sends a private message to {@code friend}.
     *
     * @param friend  The player to private message.
     * @param message The message being sent in bytes.
     */
    public void message(Player friend, byte[] message, int size) {
        if (friend.getRelations().status.equals(PrivateChatStatus.FRIENDS_ONLY)
                && !friend.getRelations().friendList.contains(player.getLongUsername())
                || friend.getRelations().status.equals(PrivateChatStatus.OFF)) {
            player.getPacketSender().sendMessage("This player is currently offline.");
            return;
        }
        if (status == PrivateChatStatus.OFF) {
            setStatus(PrivateChatStatus.FRIENDS_ONLY, true);
        }
        friend.getPacketSender().sendPrivateMessage(player, message, size);
    }

    /**
     * Represents a player's friends list status, whether others will be able to see
     * them online or not.
     */
    public static enum PrivateChatStatus {
        ON, FRIENDS_ONLY, OFF;
    }
}
