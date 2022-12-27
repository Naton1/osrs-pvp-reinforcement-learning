package com.elvarg.game.content.minigames.impl;


import com.elvarg.game.content.Food;
import com.elvarg.game.content.combat.hit.HitDamage;
import com.elvarg.game.content.combat.hit.HitMask;
import com.elvarg.game.content.minigames.Minigame;
import com.elvarg.game.definition.ItemDefinition;
import com.elvarg.game.entity.impl.npc.impl.Barricades;
import com.elvarg.game.entity.impl.object.GameObject;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.*;
import com.elvarg.game.model.container.impl.Equipment;
import com.elvarg.util.ItemIdentifiers;
import com.elvarg.util.Misc;
import com.elvarg.util.timers.TimerKey;

import java.util.HashMap;
import java.util.Iterator;

import static com.elvarg.game.model.container.impl.Equipment.CAPE_SLOT;
import static com.elvarg.util.ItemIdentifiers.*;
import static com.elvarg.util.ObjectIdentifiers.*;


public class CastleWars implements Minigame {

    /*
     * Game timers.
     */
    private static final int GAME_TIMER = 200; // 1500 * 600 = 900000ms = 15
    // minutes
    private static final int GAME_START_TIMER = 30;
    /*
     * Hashmap for the waitingroom players
     */
    private static HashMap<Player, Integer> waitingRoom = new HashMap<Player, Integer>();
    /*
     * hashmap for the gameRoom players
     */
    private static HashMap<Player, Integer> gameRoom = new HashMap<Player, Integer>();
    /*
     * The coordinates for the waitingRoom both sara/zammy
     */
    private static final int[][] WAIT_ROOM = { { 2377, 9485 }, // sara
            { 2421, 9524 } // zammy
    };
    /*
     * The coordinates for the gameRoom both sara/zammy
     */
    private static final int[][] GAME_ROOM = { { 2426, 3076 }, // sara
            { 2372, 3131 } // zammy
    };
    private static final int[][] FLAG_STANDS = { { 2429, 3074 }, // sara
            // {X-Coord,
            // Y-Coord)
            { 2370, 3133 } // zammy
    };
    /*
     * Scores for saradomin and zamorak!
     */
    private static int[] scores = { 0, 0 };
    /*
     * Booleans to check if a team's flag is safe
     */
    private static int zammyFlag = 0;
    private static int saraFlag = 0;
    /*
     * Zamorak and saradomin banner/capes item ID's
     */
    public static final int SARA_BANNER = 4037;
    public static final Item SARA_BANNER_ITEM = new Item(SARA_BANNER);

    public static final int ZAMMY_BANNER = 4039;
    public static final Item ZAMMY_BANNER_ITEM = new Item(ZAMMY_BANNER);

    public static final int SARA_CAPE = 4041;
    public static final int ZAMMY_CAPE = 4042;
    public static final int SARA_HOOD = 4513;
    public static final int ZAMMY_HOOD = 4515;

    public static final Animation TAKE_BANDAGES_ANIM = new Animation(881);

    public static final Item EMPTY_SLOT = new Item(-1);

    private static final int[] ITEMS = { BANDAGES, ItemIdentifiers.BRONZE_PICKAXE, EXPLOSIVE_POTION, Barricades.ITEM_ID, ZAMMY_CAPE, SARA_CAPE, SARA_BANNER, ZAMMY_BANNER, ItemIdentifiers.ROCK_5 };

    public static boolean deleteCastleWarsItems(Player player, int itemId) {
        for (int item : ITEMS) {
            if (item == ItemIdentifiers.BRONZE_PICKAXE) {
                // Don't need to remove bronze pickaxe as it's not a CW item
                continue;
            }

            int amount = player.getInventory().getAmount(item);
            if (itemId == item && !isInCw(player)) {
                player.getInventory().delete(item, amount);
                ItemDefinition itemDef = ItemDefinition.forId(item);
                player.getPacketSender().sendMessage("You shouldn't have " + itemDef.getName() + " outside of Castlewars!");
                return false;
            }
        }
        return true;
    }

    private static final int[][] COLLAPSE_ROCKS = { // collapsing rocks coords
            { 2399, 2402, 9511, 9514 }, // north X Y coords sara 0
            { 2390, 2393, 9500, 9503 }, // east X Y coords sara 1
            { 2400, 2403, 9493, 9496 }, // south X Y coords zammy 2
            { 2408, 2411, 9502, 9505 } // west X Y coords zammy 3
    };

    public static final Location LOBBY_TELEPORT = new Location(2440, 3089, 0);

    /*
     *
     */
    private static int properTimer = 0;
    private static int timeRemaining = -1;
    private static int gameStartTimer = GAME_START_TIMER;
    private static boolean gameStarted = false;

    public static void resetPlayer(Player player) {
        player.moveTo(new Location(2440 + Misc.random(3), 3089 - Misc.random(3), 0));
        deleteGameItems(player);
        player.getPacketSender().sendMessage("Cheating will not be tolerated.");
    }

    /**
     * Kills any players standing under the cave collapse area.
     *
     * @param cave
     */
    public static void collapseCave(int cave) {
        Iterator<Player> iterator = gameRoom.keySet().iterator();
        while (iterator.hasNext()) {
            Player teamPlayer = iterator.next();
            if (teamPlayer.getLocation().getX() > COLLAPSE_ROCKS[cave][0]
                    && teamPlayer.getLocation().getX() < COLLAPSE_ROCKS[cave][1]
                    && teamPlayer.getLocation().getY() > COLLAPSE_ROCKS[cave][2]
                    && teamPlayer.getLocation().getY() < COLLAPSE_ROCKS[cave][3]) {
                int damage = teamPlayer.getSkillManager().getCurrentLevel(Skill.HITPOINTS);
                teamPlayer.getCombat().getHitQueue().addPendingDamage(new HitDamage(damage, HitMask.RED));
            }
        }
    }

    /**
     * Method we use to add someone to the waitinroom in a different method,
     * this will filter out some error messages
     *
     * @param player
     *            the player that wants to join
     * @param team
     *            the team!
     */
    public static void addToWaitRoom(Player player, int team) {
        if (player == null) {
            return;
        } else if (gameStarted == true) {
            player.getPacketSender().sendMessage(
                            "There's already a Castle Wars going. Please wait a few minutes before trying again.");
            return;
        } else if (player.getEquipment().getSlot(Equipment.HEAD_SLOT) > 0
                || player.getEquipment().getSlot(Equipment.CAPE_SLOT) > 0) {
            player.getPacketSender().sendMessage("You may not bring capes or helmets in castle wars.");
            return;
        }
        toWaitingRoom(player, team);
    }

    /**
     * Method we use to transfer to player from the outside to the waitingroom
     * (:
     *
     * @param player
     *            the player that wants to join
     * @param team
     *            team he wants to be in - team = 1 (saradomin), team = 2
     *            (zamorak), team = 3 (random)
     */
    public static void toWaitingRoom(Player player, int team) {
        Integer[] foodIds = Food.Edible.getTypes();
        if (player.getEquipment().containsAny(foodIds)) {
            player.getPacketSender().sendMessage("You may not bring your own consumables inside of Castle Wars.");
            return;
        }

        if (team == 1) {
            if (getSaraPlayers() > getZammyPlayers() && getSaraPlayers() > 0) {
                player.getPacketSender().sendMessage(
                        "The saradomin team is full, try again later!");
                return;
            }
            if (getZammyPlayers() >= getSaraPlayers() || getSaraPlayers() == 0) {
                player.getPacketSender().sendMessage(
                        "You have been added to the Saradomin team.");
                player.getPacketSender().sendMessage(
                        "Next Game Begins In: "
                                + (gameStartTimer * 3 + timeRemaining * 3)
                                + " seconds.");
                addCapes(player, SARA_CAPE);
                waitingRoom.put(player, team);
                player.moveTo(new Location(
                        WAIT_ROOM[team - 1][0] + Misc.random(5),
                        WAIT_ROOM[team - 1][1] + Misc.random(5), 0)
                );
            }
        } else if (team == 2) {
            if (getZammyPlayers() > getSaraPlayers() && getZammyPlayers() > 0) {
                player.getPacketSender().sendMessage(
                        "The zamorak team is full, try again later!");
                return;
            }
            if (getZammyPlayers() <= getSaraPlayers() || getZammyPlayers() == 0) {
                player.getPacketSender()
                        .sendMessage(
                                "Random team: You have been added to the Zamorak team.");
                player.getPacketSender().sendMessage(
                        "Next Game Begins In: "
                                + (gameStartTimer * 3 + timeRemaining * 3)
                                + " seconds.");
                addCapes(player, ZAMMY_CAPE);
                waitingRoom.put(player, team);
                player.moveTo(new Location(
                        WAIT_ROOM[team - 1][0] + Misc.random(5),
                        WAIT_ROOM[team - 1][1] + Misc.random(5), 0)
                );
            }
        } else if (team == 3) {
            toWaitingRoom(player, getZammyPlayers() > getSaraPlayers() ? 1 : 2);
            return;
        }
    }

    /**
     * Method to add score to scoring team
     *
     * @param player
     *            the player who scored
     * @param wearItem
     *            banner id!
     */
    public static void returnFlag(Player player, int wearItem) {
        if (player == null) {
            return;
        }
        if (wearItem != SARA_BANNER && wearItem != ZAMMY_BANNER) {
            return;
        }
        int team = gameRoom.get(player);
        int objectId = -1;
        int objectTeam = -1;
        switch (team) {
            case 1:
                if (wearItem == SARA_BANNER) {
                    setSaraFlag(0);
                    objectId = 4902;
                    objectTeam = 0;
                    player.getPacketSender().sendMessage(
                            "Returned the sara flag!");
                } else {
                    objectId = 4903;
                    objectTeam = 1;
                    setZammyFlag(0);
                    scores[0]++; // upping the score of a team; team 0 = sara,
                    // team 1 = zammy
                    player.getPacketSender().sendMessage(
                            "The team of Saradomin scores 1 point!");
                }
                break;
            case 2:
                if (wearItem == ZAMMY_BANNER) {
                    setZammyFlag(0);
                    objectId = 4903;
                    objectTeam = 1;
                    player.getPacketSender().sendMessage(
                            "Returned the zammy flag!");
                } else {
                    objectId = 4902;
                    objectTeam = 0;
                    setSaraFlag(0);
                    scores[1]++; // upping the score of a team; team 0 = sara,
                    // team 1 = zammy
                    player.getPacketSender().sendMessage(
                            "The team of Zamorak scores 1 point!");
                    zammyFlag = 0;
                }
                break;
        }
        changeFlagObject(objectId, objectTeam);
        player.getPacketSender().sendEntityHintRemoval(true);
        player.getEquipment().setItem(Equipment.WEAPON_SLOT, EMPTY_SLOT);
        player.getEquipment().refreshItems();
        player.getInventory().resetItems();
        player.getUpdateFlag().flag(Flag.APPEARANCE);
    }

    /**
     * Method that will capture a flag when being taken by the enemy team!
     *
     * @param player
     *            the player who returned the flag
     */
    public static void captureFlag(Player player) {
        if (player.getEquipment().getSlot(Equipment.WEAPON_SLOT) > 0) {
            player.getPacketSender()
                    .sendMessage(
                            "Please remove your weapon before attempting to capture the flag!");
            return;
        }

        int team = gameRoom.get(player);
        if (team == 2 && saraFlag == 0) { // sara flag
            setSaraFlag(1);
            addFlag(player, SARA_BANNER_ITEM);
            createHintIcon(player, 1);
            changeFlagObject(STANDARD_STAND, 0);
        }
        if (team == 1 && zammyFlag == 0) {
            setZammyFlag(1);
            addFlag(player, ZAMMY_BANNER_ITEM);
            createHintIcon(player, 2);
            changeFlagObject(STANDARD_STAND_2, 1);
        }
    }

    /**
     * Method that will add the flag to a player's weapon slot
     *
     * @param player
     *            the player who's getting the flag
     * @param banner
     *            the banner Item.
     */
    public static void addFlag(Player player, Item banner) {
        player.getEquipment().set(Equipment.WEAPON_SLOT, banner);
        player.getEquipment().refreshItems();
        player.getUpdateFlag().flag(Flag.APPEARANCE);
    }

    /**
     * Method we use to handle the flag dropping
     *
     * @param player
     *            the player who dropped the flag/died
     * @param flagId
     *            the flag item ID
     */
    public static void dropFlag(Player player, int flagId) {
        int object = -1;
        switch (flagId) {
            case SARA_BANNER: // sara
                setSaraFlag(2);
                object = 4900;
                createFlagHintIcon(player.getLocation());
                break;
            case ZAMMY_BANNER: // zammy
                setZammyFlag(2);
                object = 4901;
                createFlagHintIcon(player.getLocation());
                break;
        }

        player.getEquipment().setItem(Equipment.WEAPON_SLOT, EMPTY_SLOT);
        player.getEquipment().refreshItems();
        player.getUpdateFlag().flag(Flag.APPEARANCE);
        for (Player teamPlayer : gameRoom.keySet()) {
            teamPlayer.getPacketSender().sendObject(new GameObject(object, player.getLocation(), 10, 0, null));
        }
    }

    /**
     * Method we use to pickup the flag when it was dropped/lost
     *
     * @param player
     *            the player who's picking it up
     */
    public static void pickupFlag(Player player, GameObject object) {
        switch (object.getId()) {
            case SARADOMIN_STANDARD:
                if (player.getEquipment().getSlot(Equipment.WEAPON_SLOT) > 0) {
                    player.getPacketSender().sendMessage("Please remove your weapon before attempting to get the flag again!");
                    return;
                }
                if (saraFlag != 2) {
                    return;
                }
                setSaraFlag(1);
                addFlag(player, SARA_BANNER_ITEM);
                break;
            case ZAMORAK_STANDARD:
                if (player.getEquipment().getSlot(Equipment.WEAPON_SLOT) > 0) {
                    player.getPacketSender().sendMessage("Please remove your weapon before attempting to get the flag again!");
                    return;
                }
                if (zammyFlag != 2) {
                    return;
                }
                setZammyFlag(1);
                addFlag(player, ZAMMY_BANNER_ITEM);
                break;
        }
        createHintIcon(player, gameRoom.get(player) == 1 ? 2 : 1);
        Iterator<Player> iterator = gameRoom.keySet().iterator();
        while (iterator.hasNext()) {
            Player teamPlayer = iterator.next();
            teamPlayer.getPacketSender().sendPositionalHint(object.getLocation(), -1);
            teamPlayer.getPacketSender().sendObjectRemoval(new GameObject(-1, object.getLocation(),  10, 0, teamPlayer.getPrivateArea()));
        }
        return;
    }

    /**
     * Hint icons appear to your team when a enemy steals flag
     *
     * @param player
     *            the player who took the flag
     * @param t
     *            team of the opponent team. (:
     */
    public static void createHintIcon(Player player, int t) {
        Iterator<Player> iterator = gameRoom.keySet().iterator();
        while (iterator.hasNext()) {
            Player teamPlayer = iterator.next();
            teamPlayer.getPacketSender().sendEntityHintRemoval(true);
            if (gameRoom.get(teamPlayer) == t) {
                teamPlayer.getPacketSender().sendEntityHint(player);
                player.getUpdateFlag().flag(Flag.APPEARANCE);
            }
        }
    }

    /**
     * Hint icons appear to your team when a enemy steals flag
     *
     * @param location
     *            the location of the flag hint
     */
    public static void createFlagHintIcon(Location location) {
        Iterator<Player> iterator = gameRoom.keySet().iterator();
        while (iterator.hasNext()) {
            Player teamPlayer = iterator.next();
            teamPlayer.getPacketSender().sendPositionalHint(location, 2);
        }
    }

    /**
     * This method is used to get the teamNumber of a certain player
     *
     * @param player
     * @return
     */
    public static int getTeamNumber(Player player) {
        if (player == null) {
            return -1;
        }
        if (gameRoom.containsKey(player)) {
            return gameRoom.get(player);
        }
        return -1;
    }

    /**
     * The leaving method will be used on click object or log out
     *
     * @param player
     *            player who wants to leave
     */
    public static void leaveWaitingRoom(Player player) {
        if (player == null) {
            return;
        }
        if (waitingRoom.containsKey(player)) {
            waitingRoom.remove(player);
            player.getPacketSender().sendEntityHintRemoval(true);
            player.getPacketSender().sendMessage("You left your team!");
            deleteGameItems(player);
            player.moveTo(new Location(2439 + Misc.random(4),
                    3085 + Misc.random(5), 0));
            return;
        }
        player.moveTo(new Location(2439 + Misc.random(4),
                3085 + Misc.random(5), 0));
        // System.out.println("Waiting room map does not contain " +
        // player.playerName);
    }

    /**
     * Method we use to update the player's interface in the waiting room
     */
    public static void updatePlayers() {
        Iterator<Player> iterator = waitingRoom.keySet().iterator();
        while (iterator.hasNext()) {
            Player player = iterator.next();
            if (player != null) {
                player.getPacketSender().sendString(
                        "Next Game Begins In: "
                                + (gameStartTimer * 3 + timeRemaining * 3)
                                + " seconds.", 6570);
                player.getPacketSender().sendString(
                        "Zamorak Players: " + getZammyPlayers() + ".", 6572);
                player.getPacketSender().sendString(
                        "Saradomin Players: " + getSaraPlayers() + ".", 6664);
                player.getPacketSender().sendWalkableInterface(6673);
            }
        }
    }

    /**
     * Method we use the update the player's interface in the game room
     */
    public static void updateInGamePlayers() {
        if (getSaraPlayers() > 0 && getZammyPlayers() > 0) {
            Iterator<Player> iterator = gameRoom.keySet().iterator();
            while (iterator.hasNext()) {
                Player player = iterator.next();
                int config;
                if (player == null) {
                    continue;
                }
                player.getPacketSender().sendWalkableInterface(11146);
                player.getPacketSender().sendString(
                        "Zamorak = " + scores[1], 11147);
                player.getPacketSender().sendString(
                        scores[0] + " = Saradomin", 11148);
                player.getPacketSender().sendString(
                        timeRemaining * 3 + " secs", 11155);
                config = 2097152 * saraFlag;
                player.getPacketSender().sendToggle(378, config);
                config = 2097152 * zammyFlag; // flags 0 = safe 1 = taken 2 = dropped
                player.getPacketSender().sendToggle(377, config);
            }
        }
    }

    /*
     * Method that will start the game when there's enough players.
     */
    public static void startGame() {
        if (getSaraPlayers() < 1 || getZammyPlayers() < 1) {
            gameStartTimer = GAME_START_TIMER;

            return;
        }
        gameStartTimer = -1;
        System.out.println("Starting Castle Wars game.");
        gameStarted = true;
        timeRemaining = GAME_TIMER / 2;
        Iterator<Player> iterator = waitingRoom.keySet().iterator();
        while (iterator.hasNext()) {
            Player player = iterator.next();
            int team = waitingRoom.get(player);
            if (player == null) {
                continue;
            }
            player.getPacketSender().sendWalkableInterface(-1);
            player.moveTo(new Location(
                    GAME_ROOM[team - 1][0] + Misc.random(3),
                    GAME_ROOM[team - 1][1] - Misc.random(3), 1));
            gameRoom.put(player, team);
        }
        waitingRoom.clear();
    }

    /*
     * Method we use to end an ongoing cw game.
     */
    public static void endGame() {
        Iterator<Player> iterator = gameRoom.keySet().iterator();
        while (iterator.hasNext()) {
            Player player = iterator.next();
            int team = gameRoom.get(player);
            if (player == null) {
                continue;
            }
            player.moveTo(new Location(2440 + Misc.random(3),
                    3089 - Misc.random(3), 0));
            player.getPacketSender().sendMessage(
                    "Castle Wars: The Castle Wars game has ended!");
            /*
            TODO: Player counters
            player.cwGames++;
            player.getPacketSender().sendMessage(
                    "Castle Wars: Kills: " + player.cwKills + " Deaths: "
                            + player.cwDeaths + " Games Played: "
                            + player.cwGames + ".");
             */
            player.getPacketSender().sendEntityHintRemoval(true);
            deleteGameItems(player);
            player.resetAttributes();

            if (scores[0] == scores[1]) {
                player.getInventory().add(CASTLE_WARS_TICKET, 1);
                player.getPacketSender().sendMessage("Tie game! You earn 1 ticket!");
            } else if (team == 1) {
                if (scores[0] > scores[1]) {
                    player.getInventory().add(CASTLE_WARS_TICKET, 2);
                    player.getPacketSender().sendMessage("You won the game. You received 2 Castle Wars Tickets!");
                } else {
                    player.getPacketSender().sendMessage("You lost the game. You received no tickets!");
                }
            } else if (team == 2) {
                if (scores[1] > scores[0]) {
                    player.getInventory().add(CASTLE_WARS_TICKET, 2);
                    player.getPacketSender().sendMessage("You won the game. You received 2 Castle Wars Tickets!");
                } else {
                    player.getPacketSender().sendMessage("You lost the game. You received no tickets!");
                }
            }
        }
        resetGame();
    }

    /**
     * reset the game variables
     */
    public static void resetGame() {
        changeFlagObject(4902, 0);
        changeFlagObject(4903, 1);
        setSaraFlag(0);
        setZammyFlag(0);
        timeRemaining = -1;
        gameStartTimer = GAME_START_TIMER;
        gameStarted = false;
        gameRoom.clear();
    }

    /**
     * Method we use to remove a player from the game
     *
     * @param player
     *            the player we want to be removed
     */
    public static void removePlayerFromCw(Player player) {
        if (player == null) {
            System.out.println("Error removing player from castle wars [REASON = null].");
            return;
        }
        if (gameRoom.containsKey(player)) {
            /*
             * Logging/leaving with flag
             */
            if (player.getEquipment().getSlot(Equipment.WEAPON_SLOT) == SARA_BANNER) {
                player.getEquipment().delete(SARA_BANNER_ITEM, Equipment.WEAPON_SLOT);
                setSaraFlag(0); // safe flag
            } else if (player.getEquipment().getSlot(Equipment.WEAPON_SLOT) == ZAMMY_BANNER) {
                player.getEquipment().delete(ZAMMY_BANNER_ITEM, Equipment.WEAPON_SLOT);
                setZammyFlag(0); // safe flag
            }
            deleteGameItems(player);
            player.moveTo(new Location(2440, 3089, 0));
            player.getPacketSender().sendMessage("The Casle Wars game has ended for you!");
            //player.getPacketSender().sendMessage("Kills: " + player.cwKills + " Deaths: " + player.cwDeaths + ".");
            player.getPacketSender().sendEntityHintRemoval(true);
            gameRoom.remove(player);
        }

        if (getZammyPlayers() <= 0 || getSaraPlayers() <= 0) {
            endGame();
        }
    }

    /**
     * Will add a cape to a player's equip
     *
     * @param player
     *            the player
     * @param capeId
     *            the capeId
     */
    public static void addCapes(Player player, int capeId) {
        player.getEquipment().setItem(Equipment.CAPE_SLOT, new Item(capeId));
        player.getEquipment().refreshItems();
        player.getUpdateFlag().flag(Flag.APPEARANCE);
    }

    /**
     * This method will delete all items received in game. Easy to add items to
     * the array. (:
     *
     * @param player
     *            the player who want the game items deleted from.
     */

    public static void deleteGameItems(Player player) {
        switch (player.getEquipment().getSlot(Equipment.WEAPON_SLOT)) {
            case SARA_BANNER:
            case ZAMMY_BANNER:
                player.getEquipment().setItem(Equipment.WEAPON_SLOT, EMPTY_SLOT);
                player.getEquipment().refreshItems();
                player.getUpdateFlag().flag(Flag.APPEARANCE);
                break;
        }
        switch (player.getEquipment().getSlot(CAPE_SLOT)) {
            case ZAMMY_CAPE:
            case SARA_CAPE:
                player.getEquipment().setItem(Equipment.CAPE_SLOT, EMPTY_SLOT);
                player.getEquipment().refreshItems();
                player.getUpdateFlag().flag(Flag.APPEARANCE);
                break;
        }
        for (int item : ITEMS) {
            if (player.getInventory().contains(item)) {
                player.getInventory().delete(new Item(item, player.getInventory().getAmount(item)));
            }
        }
    }

    /**
     * Methode we use to get the zamorak players
     *
     * @return the amount of players in the zamorakian team!
     */
    public static int getZammyPlayers() {
        int players = 0;
        Iterator<Integer> iterator = !waitingRoom.isEmpty() ? waitingRoom
                .values().iterator() : gameRoom.values().iterator();
        while (iterator.hasNext()) {
            if (iterator.next() == 2) {
                players++;
            }
        }
        return players;
    }

    /**
     * Method we use to get the saradomin players!
     *
     * @return the amount of players in the saradomin team!
     */
    public static int getSaraPlayers() {
        int players = 0;
        Iterator<Integer> iterator = !waitingRoom.isEmpty() ? waitingRoom
                .values().iterator() : gameRoom.values().iterator();
        while (iterator.hasNext()) {
            if (iterator.next() == 1) {
                players++;
            }
        }
        return players;
    }

    /**
     * Method we use for checking if the player is in the gameRoom
     *
     * @param player
     *            player who will be checking
     * @return
     */
    public static boolean isInCw(Player player) {
        return gameRoom.containsKey(player);
    }

    /**
     * Method we use for checking if the player is in the waitingRoom
     *
     * @param player
     *            player who will be checking
     * @return
     */
    public static boolean isInCwWait(Player player) {
        return waitingRoom.containsKey(player);
    }

    /**
     * Method to make sara flag change status 0 = safe, 1 = taken, 2 = dropped
     *
     * @param status
     */
    public static void setSaraFlag(int status) {
        saraFlag = status;
    }

    /**
     * Method to make zammy flag change status 0 = safe, 1 = taken, 2 = dropped
     *
     * @param status
     */
    public static void setZammyFlag(int status) {
        zammyFlag = status;
    }

    /**
     * Method we use for the changing the object of the flag stands when
     * capturing/returning flag
     *
     * @param objectId
     *            the object
     * @param team
     *            the team of the player
     */
    public static void changeFlagObject(int objectId, int team) {
        Iterator<Player> iterator = gameRoom.keySet().iterator();
        while (iterator.hasNext()) {
            Player teamPlayer = iterator.next();
            teamPlayer.getPacketSender().sendObject(new GameObject(objectId, new Location(FLAG_STANDS[team][0], FLAG_STANDS[team][1], 2), 10, 0, teamPlayer.getPrivateArea()));
        }
    }

    @Override
    public boolean firstClickObject(Player player, GameObject object) {
        int x = object.getLocation().getX(),
                y = object.getLocation().getY();

        int playerX = player.getLocation().getX(),
                playerY = player.getLocation().getY(),
                playerZ = player.getLocation().getZ();
        
        switch (object.getId()) {
            case 4469:
                if (CastleWars.getTeamNumber(player) == 2) {
                    player.getPacketSender().sendMessage("You are not allowed in the other teams spawn point.");
                    return true;
                }
                if (x == 2426) {
                    if (playerY == 3080) {
                        player.moveTo(new Location(2426, 3081, playerZ));
                    } else if (playerY == 3081) {
                        player.moveTo(new Location(2426, 3080, playerZ));
                    }
                } else if (x == 2422) {
                    if (playerX == 2422) {
                        player.moveTo(new Location(2423, 3076, playerZ));
                    } else if (playerX == 2423) {
                        player.moveTo(new Location(2422, 3076, playerZ));
                    }
                }
                return true;
            case 4470:
                if (CastleWars.getTeamNumber(player) == 1) {
                    player.getPacketSender().sendMessage("You are not allowed in the other teams spawn point.");
                    return true;
                }
                if (x == 2373 && y == 3126) {
                    if (playerY == 3126) {
                        player.moveTo(new Location(2373, 3127, 1));
                    } else if (playerY == 3127) {
                        player.moveTo(new Location(2373, 3126, 1));
                    }
                } else if (x == 2377 && y == 3131) {
                    if (playerX == 2376) {
                        player.moveTo(new Location(2377, 3131, 1));
                    } else if (playerX == 2377) {
                        player.moveTo(new Location(2376, 3131, 1));
                    }
                }
                return true;
            case 4417:
                if (x == 2428 && y == 3081 && playerZ == 1) {
                    player.moveTo(new Location(2430, 3080, 2));
                }
                if (x == 2425 && y == 3074 && playerZ == 2) {
                    player.moveTo(new Location(2426, 3074, 3));
                }
                if (x == 2419 && y == 3078 && playerZ == 0) {
                    player.moveTo(new Location(2420, 3080, 1));
                }
                return true;
            case 4415:
                if (x == 2419 && y == 3080 && playerZ == 1) {
                    player.moveTo(new Location(2419, 3077, 0));
                }
                if (x == 2430 && y == 3081 && playerZ == 2) {
                    player.moveTo(new Location(2427, 3081, 1));
                }
                if (x == 2425 && y == 3074 && playerZ == 3) {
                    player.moveTo(new Location(2425, 3077, 2));
                }
                if (x == 2374 && y == 3133 && playerZ == 3) {
                    player.moveTo(new Location(2374, 3130, 2));
                }
                if (x == 2369 && y == 3126 && playerZ == 2) {
                    player.moveTo(new Location(2372, 3126, 1));
                }
                if (x == 2380 && y == 3127 && playerZ == 1) {
                    player.moveTo(new Location(2380, 3130, 0));
                }
                return true;
            case 4411: // castle wars jumping stones
                if (x == playerX && y == playerY) {
                    player.getPacketSender().sendMessage("You are standing on the rock you clicked.");
                } else if (x > playerX && y == playerY) {
                    player.getMovementQueue().walkStep(1,0);
                } else if (x < playerX && y == playerY) {
                    player.getMovementQueue().walkStep(-1,0);
                } else if (y > playerY && x == playerX) {
                    player.getMovementQueue().walkStep(0, 1);
                } else if (y < playerY && x == playerX) {
                    player.getMovementQueue().walkStep(0, -1);
                } else {
                    player.getPacketSender().sendMessage("Can't reach that.");
                }
                return true;
            case 4419:
                if (x == 2417 && y == 3074 && playerZ == 0) {
                    if (playerX == 2416) {
                        player.moveTo(new Location(2417, 3077, 0));
                    } else {
                        player.moveTo(new Location(2416, 3074, 0));
                    }
                }
                return true;

            case 4911:
                if (x == 2421 && y == 3073 && playerZ == 1) {
                    player.moveTo(new Location(2421, 3074, 0));
                }
                if (x == 2378 && y == 3134 && playerZ == 1) {
                    player.moveTo(new Location(2378, 3133, 0));
                }
                return true;
            case 1747:
                if (x == 2421 && y == 3073 && playerZ == 0) {
                    player.moveTo(new Location(2421, 3074, 1));
                }
                if (x == 2378 && y == 3134 && playerZ == 0) {
                    player.moveTo(new Location(2378, 3133, 1));
                }
                return true;
            case 4912:
                if (x == 2430 && y == 3082 && playerZ == 0) {
                    player.moveTo(new Location(playerX, playerY + 6400, 0));
                }
                if (x == 2369 && y == 3125 && playerZ == 0) {
                    player.moveTo(new Location(playerX, playerY + 6400, 0));
                }
                return true;
            case 1757:
                if (x == 2430 && y == 9482) {
                    player.moveTo(new Location(2430, 3081, 0));
                } else if (playerX == 2533) {
                    player.moveTo(new Location(2532, 3155, 0));
                } else {
                    player.moveTo(new Location(2369, 3126, 0));
                }
                return true;

            case 4418:
                if (x == 2380 && y == 3127 && playerZ == 0) {
                    player.moveTo(new Location(2379, 3127, 1));
                }
                if (x == 2369 && y == 3126 && playerZ == 1) {
                    player.moveTo(new Location(2369, 3127, 2));
                }
                if (x == 2374 && y == 3131 && playerZ == 2) {
                    player.moveTo(new Location(2373, 3133, 3));
                }
                return true;
            case 4420:
                if (x == 2382 && y == 3131 && playerZ == 0) {
                    if (playerX >= 2383 && playerX <= 2385) {
                        player.moveTo(new Location(2382, 3130, 0));
                    } else {
                        player.moveTo(new Location(2383, 3133, 0));
                    }
                }
                return true;
            case 4437:
                if (x == 2400 && y == 9512) {
                    player.moveTo(new Location(2400, 9514, 0));
                } else if (x == 2391 && y == 9501) {
                    player.moveTo(new Location(2393, 9502, 0));
                } else if (x == 2409 && y == 9503) {
                    player.moveTo(new Location(2411, 9503, 0));
                } else if (x == 2401 && y == 9494) {
                    player.moveTo(new Location(2401, 9493, 0));
                }
                return true;
            case 1568:
                if (x == 2399 && y == 3099) {
                    player.moveTo(new Location(2399, 9500, 0));
                } else {
                    player.moveTo(new Location(2400, 9507, 0));
                }
            case 6281:
                player.moveTo(new Location(2370, 3132, 2));
                return true;
            case 4472:
                player.moveTo(new Location(2370, 3132, 1));
                return true;
            case 6280:
                player.moveTo(new Location(2429, 3075, 2));
                return true;
            case 4471:
                player.moveTo(new Location(2429, 3075, 1));
                return true;
            case 4406:
            case 4407:
                CastleWars.removePlayerFromCw(player);
                return true;
            case 4458:
                if (!player.getTimers().has(TimerKey.CASTLEWARS_TAKE_ITEM)) {
                    player.performAnimation(TAKE_BANDAGES_ANIM);
                    player.getInventory().add(BANDAGES, 1);
                    player.getPacketSender().sendMessage("You get some bandages.");
                    player.getTimers().extendOrRegister(TimerKey.CASTLEWARS_TAKE_ITEM, 2);
                }
                return true;
            case 4902: // sara flag
            case 4377:
                switch (CastleWars.getTeamNumber(player)) {
                    case 1:
                        CastleWars.returnFlag(player, player.getEquipment().getSlot(Equipment.WEAPON_SLOT));
                        return true;
                    case 2:
                        CastleWars.captureFlag(player);
                        return true;
                }
                return true;
            case 4903: // zammy flag
            case 4378:
                switch (CastleWars.getTeamNumber(player)) {
                    case 1:
                        CastleWars.captureFlag(player);
                        return true;
                    case 2:
                        CastleWars.returnFlag(player, player.getEquipment().getSlot(Equipment.WEAPON_SLOT));
                        return true;
                }
                return true;
            case 4461: // barricades
                if (!player.getTimers().has(TimerKey.CASTLEWARS_TAKE_ITEM)) {
                    player.performAnimation(TAKE_BANDAGES_ANIM);
                    player.getInventory().add(Barricades.ITEM_ID, 1);
                    player.getPacketSender().sendMessage("You get a barricade.");
                    player.getTimers().extendOrRegister(TimerKey.CASTLEWARS_TAKE_ITEM, 2);
                }
                return true;
            case 4463: // explosive potion!
                if (!player.getTimers().has(TimerKey.CASTLEWARS_TAKE_ITEM)) {
                    player.performAnimation(TAKE_BANDAGES_ANIM);
                    player.getInventory().add(EXPLOSIVE_POTION, 1);
                    player.getPacketSender().sendMessage("You get an explosive potion!");
                    player.getTimers().extendOrRegister(TimerKey.CASTLEWARS_TAKE_ITEM, 2);
                }
                return true;
            case 4464: // pickaxe table
                if (!player.getTimers().has(TimerKey.CASTLEWARS_TAKE_ITEM)) {
                    player.performAnimation(TAKE_BANDAGES_ANIM);
                    player.getInventory().add(ItemIdentifiers.BRONZE_PICKAXE, 1);
                    player.getPacketSender().sendMessage("You get a bronze pickaxe for mining.");
                    player.getTimers().extendOrRegister(TimerKey.CASTLEWARS_TAKE_ITEM, 2);
                }
                return true;
            case 4459: // tinderbox table
                if (!player.getTimers().has(TimerKey.CASTLEWARS_TAKE_ITEM)) {
                    player.performAnimation(TAKE_BANDAGES_ANIM);
                    player.getInventory().add(TINDERBOX, 1);
                    player.getPacketSender().sendMessage("You get a Tinderbox.");
                    player.getTimers().extendOrRegister(TimerKey.CASTLEWARS_TAKE_ITEM, 2);
                }
                return true;
            case 4462:
                if (!player.getTimers().has(TimerKey.CASTLEWARS_TAKE_ITEM)) {
                    player.performAnimation(TAKE_BANDAGES_ANIM);
                    player.getInventory().add(ItemIdentifiers.ROPE, 1);
                    player.getPacketSender().sendMessage("You get some rope.");
                    player.getTimers().extendOrRegister(TimerKey.CASTLEWARS_TAKE_ITEM, 2);
                }
                return true;
            case 4460:
                if (!player.getTimers().has(TimerKey.CASTLEWARS_TAKE_ITEM)) {
                    player.performAnimation(TAKE_BANDAGES_ANIM);
                    player.getInventory().add(ItemIdentifiers.ROCK_5, 1);
                    player.getPacketSender().sendMessage("You get a rock.");
                    player.getTimers().extendOrRegister(TimerKey.CASTLEWARS_TAKE_ITEM, 2);
                }
                return true;
            case 4900:
            case 4901:
                CastleWars.pickupFlag(player, object);
                return true;
            case 4387:
                CastleWars.addToWaitRoom(player, 1); // saradomin
                return true;

            case 4388:
                CastleWars.addToWaitRoom(player, 2); // zamorak
                return true;

            case 4408:
                CastleWars.addToWaitRoom(player, 3); // guthix
                return true;

            case 4389: // sara
            case 4390: // zammy waiting room portal
                CastleWars.leaveWaitingRoom(player);
                return true;
            default:
                break;
        }
        
        return false;
    }

    /**
     * Processes all actions to keep the minigame running smoothly.
     */
    public void process() {
        if (properTimer > 0) {
            properTimer--;
            return;
        } else {
            properTimer = 4;
        }
        if (gameStartTimer > 0) {
            gameStartTimer--;
            updatePlayers();
        } else if (gameStartTimer == 0) {
            startGame();
        }
        if (timeRemaining > 0) {
            timeRemaining--;
            updateInGamePlayers();
        } else if (timeRemaining == 0) {
            endGame();
        }
    }
}
