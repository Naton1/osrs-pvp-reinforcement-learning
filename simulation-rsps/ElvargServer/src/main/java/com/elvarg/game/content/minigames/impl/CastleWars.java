package com.elvarg.game.content.minigames.impl;


import com.elvarg.game.World;
import com.elvarg.game.content.Food;
import com.elvarg.game.content.combat.hit.HitDamage;
import com.elvarg.game.content.combat.hit.HitMask;
import com.elvarg.game.content.minigames.Minigame;
import com.elvarg.game.entity.impl.npc.impl.Barricades;
import com.elvarg.game.entity.impl.object.GameObject;
import com.elvarg.game.entity.impl.object.ObjectManager;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.*;
import com.elvarg.game.model.areas.Area;
import com.elvarg.game.model.areas.impl.castlewars.CastleWarsGameArea;
import com.elvarg.game.model.areas.impl.castlewars.CastleWarsLobbyArea;
import com.elvarg.game.model.areas.impl.castlewars.CastleWarsSaradominWaitingArea;
import com.elvarg.game.model.areas.impl.castlewars.CastleWarsZamorakWaitingArea;
import com.elvarg.game.model.container.impl.Equipment;
import com.elvarg.game.model.dialogues.entries.impl.ItemStatementDialogue;
import com.elvarg.game.model.dialogues.entries.impl.StatementDialogue;
import com.elvarg.game.task.Task;
import com.elvarg.game.task.TaskManager;
import com.elvarg.game.task.impl.CountdownTask;
import com.elvarg.util.ItemIdentifiers;
import com.elvarg.util.Misc;
import com.elvarg.util.timers.TimerKey;
import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static com.elvarg.game.model.container.impl.Equipment.CAPE_SLOT;
import static com.elvarg.game.model.container.impl.Equipment.NO_ITEM;
import static com.elvarg.util.ItemIdentifiers.*;
import static com.elvarg.util.ObjectIdentifiers.*;


public class CastleWars implements Minigame {

    /**
     * Area instances
     * <p>
     * We instantiate these here as we need to reference them directly.
     */
    public static final CastleWarsSaradominWaitingArea SARADOMIN_WAITING_AREA = new CastleWarsSaradominWaitingArea();

    public static final CastleWarsZamorakWaitingArea ZAMORAK_WAITING_AREA = new CastleWarsZamorakWaitingArea();

    public static final CastleWarsGameArea GAME_AREA = new CastleWarsGameArea();

    public static final CastleWarsLobbyArea LOBBY_AREA = new CastleWarsLobbyArea();

    private static List<GameObject> spawned_objects = Lists.newCopyOnWriteArrayList();

    @Override
    public void init() {
        /** Saradomin Altar **/
        ObjectManager.register(new GameObject(411, new Location(2431, 3076, 1), 10, 1, null), true);
        /** Zamorak Altar **/
        ObjectManager.register(new GameObject(411, new Location(2373, 3135, 1), 10, 0, null), true);
    }

    public static boolean handleItemOnPlayer(Player player, Player target, Item item) {
        if (item.getId() != BANDAGES)
            return false;
        if (Team.getTeamForPlayer(player) != Team.getTeamForPlayer(target)) {
            player.getPacketSender().sendMessage("You don't want to be healing your enemies!");
            return true;
        }
        healWithBandage(target, false);
        return true;
    }

    private static void healWithBandage(Player player, boolean use) {
        /**
         * TODO...
         */
        boolean bracelet = player.getEquipment().hasCastleWarsBracelet();
        /** Boost increases BY 50% if wearing the bracelet **/
        int maxHP = player.getSkillManager().getMaxLevel(Skill.HITPOINTS);
        /** Wiki only says heal. Nothing about run energy for other players **/
        int hp = (int) Math.floor(maxHP * (bracelet ? 1.60 : 1.1));
        /** Heals the target **/
        player.heal(hp);
    }

    private static boolean isSteppingStones(Location loc) {
        return loc.getX() >= 2418 && loc.getX() <= 2420 && loc.getY() >= 3122 && loc.getY() <= 3125 || loc.getX() >= 2377 && loc.getX() <= 2378 && loc.getY() >= 3084 && loc.getY() <= 3088;
    }

    /**
     * The team enum
     */
    public enum Team {
        ZAMORAK(ZAMORAK_WAITING_AREA, new Location(2421, 9524), new Boundary(2368, 2376, 3127, 3135, 1)),
        SARADOMIN(SARADOMIN_WAITING_AREA, new Location(2381, 9489), new Boundary(2423, 2431, 3072, 3080, 1)),
        GUTHIX;

        private Area area;
        private Location waitingRoom;
        private int score;
        private List<Player> players;

        public Boundary respawn_area_bounds;

        Team() {
            this.players = new ArrayList<>();
        }

        Team(Area area, Location waitingRoom, Boundary respawn_area_bounds) {
            this();
            this.area = area;
            this.waitingRoom = waitingRoom;
            this.score = 0;
            this.respawn_area_bounds = respawn_area_bounds;
        }

        public void addPlayer(Player player) {
            players.add(player);
        }

        /**
         * Method to remove a player from whichever team they're on
         *
         * @param player
         */
        public static void removePlayer(Player player) {
            if (ZAMORAK.getPlayers().contains(player)) {
                ZAMORAK.players.remove(player);
            }

            if (SARADOMIN.getPlayers().contains(player)) {
                SARADOMIN.players.remove(player);
            }
        }

        public List<Player> getPlayers() {
            return players;
        }

        /**
         * Gets the number of players waiting to play for this team.
         *
         * @return
         */
        public int getWaitingPlayers() {
            return this.area.getPlayers().size();
        }

        public Location getWaitingRoom() {
            return this.waitingRoom;
        }

        public int getScore() {
            return this.score;
        }

        public void incrementScore() {
            this.score++;
        }

        public static void resetTeams() {
            ZAMORAK.score = 0;
            SARADOMIN.score = 0;

            ZAMORAK.players.clear();
            SARADOMIN.players.clear();
        }

        /**
         * This method is used to get the teamNumber of a certain player
         *
         * @param player
         * @return
         */
        public static Team getTeamForPlayer(Player player) {
            if (SARADOMIN.getPlayers().contains(player)) {
                return SARADOMIN;
            }

            if (ZAMORAK.getPlayers().contains(player)) {
                return ZAMORAK;
            }

            return null;
        }
    }

    /**
     * Possible catapult states
     */
    public enum CatapultState {
        FIXED,
        BURNING,
        REPAIR;
    }

    /**
     * The key used to schedule the start game CountdownTask.
     */
    public static final String START_GAME_TASK_KEY = "CW_START_GAME";

    /**
     * The task that gets scheduled to start the game.
     */
    public static final Task START_GAME_TASK = new CountdownTask(START_GAME_TASK_KEY, Misc.getTicks(10), CastleWars::startGame);

    /**
     * The key used to schedule the end game CountdownTask.
     */
    public static final String END_GAME_TASK_KEY = "CW_END_GAME";

    /**
     * The task that gets scheduled to end the game.
     */
    public static final Task GAME_END_TASK = new CountdownTask(END_GAME_TASK_KEY, Misc.getTicks(1200), CastleWars::endGame);

    /**
     * The coordinates for the gameRoom both sara/zammy
     */
    private static final int[][] GAME_ROOM = {
            {2426, 3076}, // sara
            {2372, 3131} // zammy
    };
    private static final int[][] FLAG_STANDS = {
            {2429, 3074}, // sara
            {2370, 3133} // zammy
    };
    /*
     * Scores for saradomin and zamorak!
     */
    private static int[] scores = {0, 0};
    /*
     * Booleans to check if a team's flag is safe
     */
    public static int zammyFlag = 0;
    public static int saraFlag = 0;

    /*
     * Zamorak and saradomin banner/capes item ID's
     */
    public static final int SARA_BANNER = 4037;
    public static final Item SARA_BANNER_ITEM = new Item(SARA_BANNER);

    public static final int ZAMMY_BANNER = 4039;
    public static final Item ZAMMY_BANNER_ITEM = new Item(ZAMMY_BANNER);

    public static final int SARA_CAPE = 4041;
    public static final Item ZAMORAK_CAPE = new Item(HOODED_CLOAK_2);
    public static final Item SARADOMIN_CAPE = new Item(HOODED_CLOAK);
    public static final int SARA_HOOD = 4513;
    public static final int ZAMMY_HOOD = 4515;

    public static final Item MANUAL = new Item(CASTLEWARS_MANUAL);

    public static final Animation TAKE_BANDAGES_ANIM = new Animation(881);

    private static final int[] ITEMS = {BANDAGES, ItemIdentifiers.BRONZE_PICKAXE, EXPLOSIVE_POTION, Barricades.ITEM_ID, HOODED_CLOAK_2, SARA_CAPE, SARA_BANNER, ZAMMY_BANNER, ItemIdentifiers.ROCK_5};

    private static final int CATAPULT_INTERFACE = 11169;

    public static void deleteItemsOnEnd(Player player) {
        /** Clears cwars items **/
        Arrays.stream(ITEMS).forEach(i -> player.getInventory().delete(i, Integer.MAX_VALUE));
        /** List for Equipment **/
        List<Integer> equip = Arrays.asList(SARA_CAPE, SARA_HOOD, HOODED_CLOAK_2, ZAMMY_HOOD, SARA_BANNER, ZAMMY_BANNER);
        /** Deletes Equipment **/
        equip.stream().filter(i -> i != null).filter(p -> player.getEquipment().contains(p)).forEach(e -> player.getEquipment().delete(new Item(e)));
    }

    private static final int[][] COLLAPSE_ROCKS = { // collapsing rocks coords
            {2399, 2402, 9511, 9514}, // north X Y coords sara 0
            {2390, 2393, 9500, 9503}, // east X Y coords sara 1
            {2400, 2403, 9493, 9496}, // south X Y coords zammy 2
            {2408, 2411, 9502, 9505} // west X Y coords zammy 3
    };

    public static final Location LOBBY_TELEPORT = new Location(2440, 3089, 0);

    public static final int TEAM_GUTHIX = 3;

    private static final Projectile CATAPULT_PROJECTILE = new Projectile(304, 75, 75, 30, 100);

    public static boolean isGameActive() {
        return GAME_END_TASK.isRunning();
    }

    /**
     * Kills any players standing under the cave collapse area.
     *
     * @param cave
     */
    public static void collapseCave(int cave) {
        GAME_AREA.getPlayers().forEach((player) -> {
            if (player.getLocation().getX() > COLLAPSE_ROCKS[cave][0]
                    && player.getLocation().getX() < COLLAPSE_ROCKS[cave][1]
                    && player.getLocation().getY() > COLLAPSE_ROCKS[cave][2]
                    && player.getLocation().getY() < COLLAPSE_ROCKS[cave][3]) {
                int damage = player.getSkillManager().getCurrentLevel(Skill.HITPOINTS);
                player.getCombat().getHitQueue().addPendingDamage(new HitDamage(damage, HitMask.RED));
            }
        });
    }

    /**
     * Method we use to add someone to the waiting room
     *
     * @param player the player that wants to join
     * @param team   the team!
     */
    public static void addToWaitingRoom(Player player, Team team) {
        if (player == null) {
            return;
        }

        if (isGameActive()) {
            player.getPacketSender().sendMessage("There's already a Castle Wars going. Please wait a few minutes before trying again.");
            return;
        }

        if (player.getEquipment().getItems()[Equipment.HEAD_SLOT].isValid()
                || player.getEquipment().getItems()[Equipment.CAPE_SLOT].isValid()) {
            StatementDialogue.send(player, "Some items are stopping you from entering the Castle Wars waiting " +
                    "area. See the chat for details.");
            player.getPacketSender().sendMessage("You can't wear hats, capes or helms in the arena.");
            return;
        }

        Integer[] foodIds = Food.Edible.getTypes();
        if (player.getEquipment().containsAny(foodIds)) {
            player.getPacketSender().sendMessage("You may not bring your own consumables inside of Castle Wars.");
            return;
        }

        int saradominPlayerCount = Team.SARADOMIN.getWaitingPlayers();
        int zamorakPlayerCount = Team.ZAMORAK.getWaitingPlayers();

        switch (team) {
            case SARADOMIN:
                if (saradominPlayerCount > zamorakPlayerCount) {
                    player.getPacketSender().sendMessage("The Saradomin team is full, try Zamorak!");
                    return;
                }

                player.getPacketSender().sendMessage("You have been added to the Saradomin team.");
                break;

            case ZAMORAK:
                if (zamorakPlayerCount > saradominPlayerCount) {
                    player.getPacketSender().sendMessage("The Zamorak team is full, try Saradomin!");
                    return;
                }

                player.getPacketSender().sendMessage("You have been added to the Zamorak team.");
                break;

            case GUTHIX:
                // Player should join whichever team has less players
                Team newTeam = zamorakPlayerCount > saradominPlayerCount ? Team.SARADOMIN : Team.ZAMORAK;
                addToWaitingRoom(player, newTeam);
                return;
        }

        /** Uses smart teleport with a radius of 8. **/
        player.smartMove(team.getWaitingRoom(), 8);
    }

    /**
     * Method to add score to scoring team
     *
     * @param player   the player who scored
     * @param wearItem banner id!
     */
    public static void returnFlag(Player player, int wearItem) {
        Team team = Team.getTeamForPlayer(player);
        if (player == null || team == null) {
            return;
        }
        if (wearItem != SARA_BANNER && wearItem != ZAMMY_BANNER) {
            return;
        }

        int objectId = -1;
        int objectTeam = -1;
        switch (team) {
            case SARADOMIN:
                if (wearItem == SARA_BANNER) {
                    setSaraFlag(0);
                    objectId = 4902;
                    objectTeam = 0;
                    player.getPacketSender().sendMessage("Returned the sara flag!");
                } else {
                    objectId = 4903;
                    objectTeam = 1;
                    setZammyFlag(0);
                    Team.SARADOMIN.incrementScore();
                    player.getPacketSender().sendMessage("The team of Saradomin scores 1 point!");
                }
                break;
            case ZAMORAK:
                if (wearItem == ZAMMY_BANNER) {
                    setZammyFlag(0);
                    objectId = 4903;
                    objectTeam = 1;
                    player.getPacketSender().sendMessage("Returned the Zamorak flag!");
                } else {
                    objectId = 4902;
                    objectTeam = 0;
                    setSaraFlag(0);
                    Team.ZAMORAK.incrementScore();
                    player.getPacketSender().sendMessage("The team of Zamorak scores 1 point!");
                    zammyFlag = 0;
                }
                break;
        }
        changeFlagObject(objectId, objectTeam);
        removeHintIcon();
        player.getEquipment().setItem(Equipment.WEAPON_SLOT, NO_ITEM);
        player.getEquipment().refreshItems();
        player.getInventory().resetItems();
        player.getUpdateFlag().flag(Flag.APPEARANCE);
    }

    /**
     * Method that will capture a flag when being taken by the enemy team!
     *
     * @param player the player who returned the flag
     */
    public static void captureFlag(Player player, Team team) {
        if (player.getEquipment().getSlot(Equipment.WEAPON_SLOT) > 0) {
            player.getPacketSender().sendMessage("Please remove your weapon before attempting to capture the flag!");
            return;
        }

        if (team == Team.ZAMORAK && saraFlag == 0) { // sara flag
            setSaraFlag(1);
            addFlag(player, SARA_BANNER_ITEM);
            createHintIcon(player, Team.SARADOMIN);
            changeFlagObject(STANDARD_STAND, 0);
        }

        if (team == Team.SARADOMIN && zammyFlag == 0) {
            setZammyFlag(1);
            addFlag(player, ZAMMY_BANNER_ITEM);
            createHintIcon(player, Team.ZAMORAK);
            changeFlagObject(STANDARD_STAND_2, 1);
        }
    }

    /**
     * Method that will add the flag to a player's weapon slot
     *
     * @param player the player who's getting the flag
     * @param banner the banner Item.
     */
    public static void addFlag(Player player, Item banner) {
        player.getEquipment().set(Equipment.WEAPON_SLOT, banner);
        player.getEquipment().refreshItems();
        player.getUpdateFlag().flag(Flag.APPEARANCE);
    }

    /**
     * Method we use to handle the flag dropping
     *
     * @param player the player who dropped the flag/died
     * @param flagId the flag item ID
     */
    public static void dropFlag(Player player, Team team) {
        int object = -1;
        switch (team) {
            case SARADOMIN:
                setSaraFlag(2);
                object = 4900;
                break;
            case ZAMORAK:
                setZammyFlag(2);
                object = 4901;
                break;
        }
        player.getEquipment().setItem(Equipment.WEAPON_SLOT, NO_ITEM);
        player.getEquipment().refreshItems();
        player.getUpdateFlag().flag(Flag.APPEARANCE);
        if (isSteppingStones(player.getLocation()) && object != -1) {
            returnFlag(player, Team.getTeamForPlayer(player) == Team.SARADOMIN ? SARA_BANNER : ZAMMY_BANNER);
            return;
        }
        createFlagHintIcon(player.getLocation());
        GameObject obj = new GameObject(object, player.getLocation(), 10, 0, null);
        // Spawn the flag object for all players
        spawned_objects.add(obj);
        GAME_AREA.getPlayers().forEach((teamPlayer) -> teamPlayer.getPacketSender().sendObject(obj));
    }

    /**
     * Method we use to pickup the flag when it was dropped/lost
     *
     * @param player the player who's picking it up
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
        createHintIcon(player, Team.getTeamForPlayer(player) == Team.SARADOMIN ? Team.SARADOMIN : Team.ZAMORAK);

        GAME_AREA.getPlayers().forEach((teamPlayer) -> {
            GameObject flag = new GameObject(object.getId(), object.getLocation(), 10, 0, teamPlayer.getPrivateArea());
            if (spawned_objects.contains(flag))
                spawned_objects.remove(flag);
            teamPlayer.getPacketSender().sendPositionalHint(object.getLocation(), -1);
            teamPlayer.getPacketSender().sendObjectRemoval(flag);
        });
    }

    /**
     * Hint icons appear to your team when a enemy steals flag
     *
     * @param player the player who took the flag
     * @param team   team of the opponent team. (:
     */
    public static void createHintIcon(Player player, Team team) {
        GAME_AREA.getPlayers().forEach((teamPlayer) -> {
            teamPlayer.getPacketSender().sendEntityHintRemoval(true);
            if (Team.getTeamForPlayer(teamPlayer) == team) {
                teamPlayer.getPacketSender().sendEntityHint(player);
                player.getUpdateFlag().flag(Flag.APPEARANCE);
            }
        });
    }

    /**
     * Hint icons appear to your team when a enemy steals flag
     *
     * @param location the location of the flag hint
     */
    public static void createFlagHintIcon(Location location) {
        GAME_AREA.getPlayers().forEach((teamPlayer) -> teamPlayer.getPacketSender().sendPositionalHint(location, 2));
    }

    public static void removeHintIcon() {
        GAME_AREA.getPlayers().forEach(p -> p.getPacketSender().sendEntityHintRemoval(true));
    }

    /**
     * The leaving method will be used on click object or log out
     *
     * @param player player who wants to leave
     */
    public static void leaveWaitingRoom(Player player) {
        if (player == null) {
            return;
        }

        player.getPacketSender().sendEntityHintRemoval(true);
        deleteGameItems(player);

    }

    /*
     * Method that will start the game when there's enough players.
     */
    public static void startGame() {
        SARADOMIN_WAITING_AREA.getPlayers().forEach((player) -> {
            player.resetCastlewarsIdleTime();
            Team.SARADOMIN.addPlayer(player);
            player.getPacketSender().sendWalkableInterface(-1);
            player.moveTo(new Location(
                    GAME_ROOM[0][0] + Misc.random(3),
                    GAME_ROOM[0][1] - Misc.random(3), 1));
        });

        ZAMORAK_WAITING_AREA.getPlayers().forEach((player) -> {
            player.resetCastlewarsIdleTime();
            Team.ZAMORAK.addPlayer(player);
            player.getPacketSender().sendWalkableInterface(-1);
            player.moveTo(new Location(
                    GAME_ROOM[1][0] + Misc.random(3),
                    GAME_ROOM[1][1] - Misc.random(3), 1));
        });

        // Schedule the game ending
        TaskManager.submit(GAME_END_TASK);
    }

    /*
     * Method we use to end an ongoing cw game.
     */
    public static void endGame() {
        GAME_AREA.getPlayers().forEach((player) -> {
            player.getPacketSender().sendEntityHintRemoval(true);

            boolean saradominWon = scores[0] > scores[1];

            if (scores[0] == scores[1]) {
                player.getInventory().add(CASTLE_WARS_TICKET, 1);
                player.getPacketSender().sendMessage("Tie game! You earn 1 ticket!");
            } else if ((saradominWon && Team.SARADOMIN.getPlayers().contains(player))
                    || (!saradominWon && Team.ZAMORAK.getPlayers().contains(player))) {
                player.getInventory().add(CASTLE_WARS_TICKET, 2);
                player.getPacketSender().sendMessage("You won the game. You received 2 Castle Wars Tickets!");
                ItemStatementDialogue.send(player, "", new String[] {"You won!", "You captured the enemy's standard "+getScore(Team.getTeamForPlayer(player))+" times.", "Enemies killed: TODO."}, CASTLE_WARS_TICKET, 200);
            } else {
                ItemStatementDialogue.send(player, "", new String[] {"You lost!", "You captured the enemy's standard "+getScore(Team.getTeamForPlayer(player))+" times.", "Enemies killed: TODO."}, CASTLE_WARS_TICKET, 200);
                player.getPacketSender().sendMessage("You lost the game. You received no tickets!");
            }
            // Teleport player after checking scores and adding tickets.
            player.moveTo(new Location(2440 + Misc.random(3), 3089 - Misc.random(3), 0));
        });
        spawned_objects.forEach(o -> { if (o != null)  ObjectManager.deregister(o, true);});
        spawned_objects.clear();
        // Reset game after processing players.
        resetGame();
    }

    public static int getScore(Team team) {
        return team.getScore();
    }

    /**
     * reset the game variables
     */
    public static void resetGame() {
        changeFlagObject(4902, 0);
        changeFlagObject(4903, 1);
        setSaraFlag(0);
        setZammyFlag(0);
        TaskManager.cancelTasks(new Object[]{START_GAME_TASK_KEY, END_GAME_TASK_KEY});
        Team.resetTeams();
    }

    /**
     * This method will delete all items received in game. Easy to add items to
     * the array. (:
     *
     * @param player the player who want the game items deleted from.
     */
    public static void deleteGameItems(Player player) {
        switch (player.getEquipment().getSlot(Equipment.WEAPON_SLOT)) {
            case SARA_BANNER:
            case ZAMMY_BANNER:
                player.getEquipment().setItem(Equipment.WEAPON_SLOT, NO_ITEM);
                player.getEquipment().refreshItems();
                player.getUpdateFlag().flag(Flag.APPEARANCE);
                break;
        }
        switch (player.getEquipment().getSlot(CAPE_SLOT)) {
            case HOODED_CLOAK_2:
            case SARA_CAPE:
                player.getEquipment().setItem(Equipment.CAPE_SLOT, NO_ITEM);
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
     * @param objectId the object
     * @param team     the team of the player
     */
    public static void changeFlagObject(int objectId, int team) {
        GameObject gameObject = new GameObject(objectId, new Location(FLAG_STANDS[team][0], FLAG_STANDS[team][1], 3), 10, 2, null);
        ObjectManager.register(gameObject, true);
        spawned_objects.add(gameObject);
    }

    @Override
    public boolean firstClickObject(Player player, GameObject object) {
        int x = object.getLocation().getX(),
                y = object.getLocation().getY();

        Location loc = object.getLocation();

        int id = object.getId();

        int type = object.getType();

        int face = object.getFace();

        int playerX = player.getLocation().getX(),
                playerY = player.getLocation().getY(),
                playerZ = player.getLocation().getZ();

        switch (object.getId()) {

            case 4386://zamorak burnt catapult
            case 4385: {//saradomin burnt catapult
                repairCatapult(player, object);
                return true;
            }
            case 4381:
            case 4382: {
                handleCatapult(player);
                return true;
            }

            case 4469:
                if (Team.getTeamForPlayer(player) == Team.ZAMORAK) {
                    player.getPacketSender().sendMessage("You are not allowed in the other teams spawn point.");
                    return true;
                }
                player.resetCastlewarsIdleTime();
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
                if (Team.getTeamForPlayer(player) == Team.SARADOMIN) {
                    player.getPacketSender().sendMessage("You are not allowed in the other teams spawn point.");
                    return true;
                }
                player.resetCastlewarsIdleTime();
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
                    player.getMovementQueue().walkStep(1, 0);
                } else if (x < playerX && y == playerY) {
                    player.getMovementQueue().walkStep(-1, 0);
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
            case 17387://under ground ladders to top.
                if (x == 2369 && y == 9525)
                    player.moveTo(new Location(2369, 3126, 0));
                else if (x == 2430 && y == 9482)
                    player.moveTo(new Location(2430, 3081, 0));
                else if (x == 2400 && y == 9508)//middle north
                    player.moveTo(new Location(2400, 3107, 0));
                else if (x == 2399 && y == 9499)//middle south
                    player.moveTo(new Location(2399, 3100, 0));
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

            case 1568:
                if (x == 2399 && y == 3099) {
                    player.moveTo(new Location(2399, 9500, 0));
                } else {
                    player.moveTo(new Location(2400, 9507, 0));
                }
            case 6281:
                player.moveTo(new Location(2370, 3132, 2));
                return true;

            case 6280:
                player.moveTo(new Location(2429, 3075, 2));
                return true;

            case 4458:
                if (!player.getTimers().has(TimerKey.CASTLEWARS_TAKE_ITEM)) {
                    player.performAnimation(TAKE_BANDAGES_ANIM);
                    player.getInventory().add(BANDAGES, 1);
                    player.getPacketSender().sendMessage("You get some bandages.");
                    player.getTimers().extendOrRegister(TimerKey.CASTLEWARS_TAKE_ITEM, 2);
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

    }

    public static void handleCatapult(Player player) {
        if (!player.getInventory().contains(4043)) {
            player.getPacketSender().sendMessage("You need a rock to launch from the catapult!");
            return;
        }
        resetCatapult(player);
        player.getPacketSender().sendInterface(CATAPULT_INTERFACE);
    }

    @Override
    public boolean handleButtonClick(Player player, int button) {
        if (player.getInterfaceId() != CATAPULT_INTERFACE) {
            // If player is not currently viewing the catapult interface, return early.
            return false;
        }
        int x = (Integer) player.getAttribute("catapultX", 0);
        int y = (Integer) player.getAttribute("catapultY", 0);
        boolean saradomin = Team.getTeamForPlayer(player) == Team.SARADOMIN;
        player.getPacketSender().sendInterfaceComponentMoval(1, 0, 11332);
        if (button == 11321) {//Up Y
            if (saradomin && y < 30) {
                player.setAttribute("catapultY", y + 1);
            } else if (y > 0) {
                player.setAttribute("catapultY", y - 1);
            }
        }
        if (button == 11322) {
            if (saradomin && y > 0) {//down Y
                player.setAttribute("catapultY", y - 1);
            } else if (y < 30) {
                player.setAttribute("catapultY", y + 1);
            }
        }
        if (button == 11323) {
            if (saradomin && x > 0) {//right X
                player.setAttribute("catapultX", x - 1);
            } else if (x < 30) {
                player.setAttribute("catapultX", x + 1);
            }
        }
        if (button == 11324) {//left X
            if (saradomin && x < 30) {
                player.setAttribute("catapultX", x + 1);
            } else if (x > 0) {
                player.setAttribute("catapultX", x - 1);
            }
        }
        x = (Integer) player.getAttribute("catapultX", 0);
        y = (Integer) player.getAttribute("catapultY", 0);
        player.getPacketSender().sendWidgetModel(11317, 4863 + (y < 10 ? 0 : y > 9 ? (y / 10) : y));
        player.getPacketSender().sendWidgetModel(11318, 4863 + (y > 29 ? y - 30 : y > 19 ? y - 20 : y > 9 ? y - 10 : y));
        player.getPacketSender().sendWidgetModel(11319, 4863 + (x < 10 ? 0 : x > 9 ? (x / 10) : x));
        player.getPacketSender().sendWidgetModel(11320, 4863 + (x > 29 ? x - 30 : x > 19 ? x - 20 : x > 9 ? x - 10 : x));
        player.getPacketSender().sendInterfaceComponentMoval(saradomin ? 90 - (x * 2) : x * 2, saradomin ? 90 - (y * 2) : y * 2, 11332);
        /** Fire button **/
        if (button == 11329) {
            if (x > 1)
                x /= 2;
            if (y > 1)
                y /= 2;
            player.getPacketSender().sendInterfaceRemoval();
            int startX = saradomin ? saradomin_catapult_start.getX() : zamorak_catapult_start.getX();
            int startY = saradomin ? saradomin_catapult_start.getY() : zamorak_catapult_start.getY();
            Location destination = new Location(saradomin ? (x >= 0 ? startX - x : startX + x) : (x >= 0 ? startX + x : startX - x), saradomin ? (y >= 0 ? startY + y : startY - y) : (y >= 0 ? startY - y : startY + y));
            GameObject catapult = World.findCacheObject(player, saradomin ? 4382 : 4381, saradomin ? saradomin_catapult_location : zamorak_catapult_location);
            if (catapult != null) {
                catapult.performAnimation(new Animation(443));
            }
            Projectile.sendProjectile(saradomin ? saradomin_catapult_location : zamorak_catapult_location, destination, CATAPULT_PROJECTILE);
            TaskManager.submit(new Task() {

                int ticks = 0;

                @Override
                public void execute() {
                    ticks++;
                    if (ticks == 4) {
                        World.sendLocalGraphics(303, destination, GraphicHeight.MIDDLE);
                    }
                    if (ticks == 6) {
                        World.getPlayers().stream().filter(Objects::nonNull).filter(p -> p.getLocation().isWithinDistance(destination, 5)).forEach(p -> p.getCombat().getHitQueue().addPendingDamage(new HitDamage(Misc.random(5, 15), HitMask.RED)));
                        World.sendLocalGraphics(305, destination, GraphicHeight.MIDDLE);
                        stop();
                    }
                }
            });
        }
        return true;
    }

    private static void resetCatapult(Player player) {
        for (int i = 11317; i < 11321; i++)
            player.getPacketSender().sendWidgetModel(i, 4863);
        player.setAttribute("catapultX", 0);
        player.setAttribute("catapultY", 0);
        Team team = Team.getTeamForPlayer(player);
        if (team == null) {
            System.err.println("error setting red cross for "+player.getUsername()+" they aren't on a team!");
            return;
        }
        boolean sara = team == Team.SARADOMIN;
        player.getPacketSender().sendInterfaceComponentMoval(sara ? 90 : 0, sara ? 90 : 0, 11332);
    }


    private static CatapultState saradominCatapult = CatapultState.FIXED, zamorakCatapult = CatapultState.FIXED;

    /**
     * Used for firing off-set for the catapult
     **/
    private static Location saradomin_catapult_start = new Location(2411, 3092, 0), zamorak_catapult_start = new Location(2388, 3115, 0);

    /**
     * Used for starting location for the catapult projectile
     **/
    private static Location saradomin_catapult_location = new Location(2413, 3088, 0), zamorak_catapult_location = new Location(2384, 3117, 0);

    /**
     * large doors - 4023-4024 -- 4025-4026
     *
     * @param player
     * @param item
     * @param object
     * @return
     */
    public static boolean handleItemOnObject(Player player, Item item, GameObject object) {
        final int objectId = object.getId();
        final int itemId = item.getId();
        boolean saradomin = Team.getTeamForPlayer(player) == Team.SARADOMIN;
        if (objectId == 4385) {
            if (item.getId() == 4051) {
                repairCatapult(player, object);
                return true;
            }
            return false;
        }
        /**
         * Saradomin's burning catapult
         */
        if (objectId == 4904 || objectId == 4905) {
            if (itemId == 1929) {
                if (saradomin) {
                    if (saradominCatapult == CatapultState.FIXED) {
                        player.getPacketSender().sendMessage("The fire has already been extinguished.");
                        return true;
                    }
                } else {
                    if (zamorakCatapult == CatapultState.FIXED) {
                        player.getPacketSender().sendMessage("The fire has already been extinguished.");
                        return true;
                    }
                }
                player.getInventory().delete(1929, 1);//bucket of water
                player.getInventory().add(new Item(1925, 1));//empty bucket
                if (saradomin) {
                    saradominCatapult = CatapultState.FIXED;
                } else {
                    zamorakCatapult = CatapultState.FIXED;
                }
                return true;
            }
            return false;
        }
        /**
         * Saradomin's default catapult
         */
        if (objectId == 4382 || objectId == 4381) {
            /**
             * Saradomin Catapult
             */
            if (itemId == 590 || itemId == 4045) {
                if (saradomin) {
                    if (saradominCatapult == CatapultState.BURNING) {
                        player.getPacketSender().sendMessage("The catapult is already burning!");
                        return true;
                    }
                } else {
                    if (zamorakCatapult == CatapultState.BURNING) {
                        player.getPacketSender().sendMessage("The catapult is already burning!");
                        return true;
                    }
                }
                if (itemId == 4045)
                    player.getInventory().delete(4045, 1);
                //4904 zamorak, 4905 saradomin
                GameObject onFire = new GameObject(saradomin ? 4904 : 4905, object.getLocation(), object.getType(), object.getFace(), object.getPrivateArea());
                GameObject burnt = new GameObject(saradomin ? 4385 : 4386, object.getLocation(), object.getType(), object.getFace(), object.getPrivateArea());

                GameObject fixed = new GameObject(object.getId(), object.getLocation(), object.getType(), object.getFace(), object.getPrivateArea());
                if (saradomin)
                    saradominCatapult = CatapultState.BURNING;
                else
                    zamorakCatapult = CatapultState.BURNING;
                ObjectManager.register(onFire, true);
                onFire.performAnimation(new Animation(1431));
                //4385 zamorak, 4386 saradomin
                Task task = new Task(0, player.getIndex(), true) {

                    int ticks = 0;

                    @Override
                    protected void execute() {
                        ticks++;

                        if (saradomin) {
                            if (saradominCatapult != CatapultState.BURNING) {
                                changeCatapultState(this, fixed, CatapultState.FIXED, true);
                                return;
                            }
                            if (ticks == 16) {//4385, 4386
                                changeCatapultState(this, burnt, CatapultState.REPAIR, true);
                            }
                        } else {
                            if (zamorakCatapult != CatapultState.BURNING) {
                                changeCatapultState(this, fixed, CatapultState.FIXED, false);
                                return;
                            }
                            if (ticks == 16) {//4385, 4386
                                changeCatapultState(this, burnt, CatapultState.REPAIR, false);
                            }
                        }

                    }
                };
                TaskManager.submit(task);
                return true;
            }
            return false;
        }
        return false;
    }

    private static void repairCatapult(Player player, GameObject object) {
        if (!player.getInventory().contains(4051)) {
            player.getPacketSender().sendMessage("You need a toolkit to repair the catapult.");
            return;
        }
        boolean saradomin = Team.getTeamForPlayer(player) == Team.SARADOMIN;
        if (saradomin) {
            if (saradominCatapult != CatapultState.REPAIR) {
                player.getPacketSender().sendMessage("The catapult has already been repaired");
                return;
            }
            saradominCatapult = CatapultState.FIXED;
        } else {
            if (zamorakCatapult != CatapultState.REPAIR) {
                player.getPacketSender().sendMessage("The catapult has already been repaired");
                return;
            }
            zamorakCatapult = CatapultState.FIXED;
        }
        player.getInventory().delete(4051, 1);//toolkit
        ObjectManager.register(new GameObject(saradomin ? 4382 : 4381, object.getLocation(), object.getType(), object.getFace(), object.getPrivateArea()), true);
        player.getPacketSender().sendMessage("You repair the catapult.");
    }

    private static void changeCatapultState(Task task, GameObject object, CatapultState state, boolean saradomin) {
        ObjectManager.register(object, true);
        if (saradomin) {
            saradominCatapult = state;
        } else {
            zamorakCatapult = state;
        }
        task.stop();

    }
}
