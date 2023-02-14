package com.elvarg.game.content.minigames.impl.pestcontrol;

import com.elvarg.game.World;
import com.elvarg.game.content.minigames.Minigame;
import com.elvarg.game.entity.impl.npc.NPC;
import com.elvarg.game.entity.impl.object.GameObject;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.Location;
import com.elvarg.game.model.areas.Area;
import com.elvarg.game.model.areas.impl.pestcontrol.PestControlArea;
import com.elvarg.game.model.areas.impl.pestcontrol.PestControlNoviceBoatArea;
import com.elvarg.game.model.areas.impl.pestcontrol.PestControlOutpostArea;
import com.elvarg.game.model.dialogues.DialogueExpression;
import com.elvarg.game.model.dialogues.entries.impl.NpcDialogue;
import com.elvarg.util.Misc;
import com.elvarg.util.NpcIdentifiers;
import com.google.common.collect.Lists;

import java.util.List;
import java.util.Optional;
import java.util.Queue;

import static com.elvarg.game.World.getNpcs;
import static com.elvarg.util.NpcIdentifiers.*;

public class PestControl implements Minigame {

    /**
     * TODO:
     * Spawn all NPCS outside arena
     * Make NPCs auto attack the gates, then void knight, defend portals
     * Change player.pcDamage to an attribute
     * Waiting boat interface
     * Game area interface
     * Instanced games and support for novice/med/advanced boats
     * Gates need to open
     * Fence damaging and repairing
     * <p>
     * Handle gates
     * Fix NPC ids for portal spawns
     */
    public static final Area GAME_AREA = new PestControlArea();

    public static final Area OUTPOST_AREA = new PestControlOutpostArea();

    public static final Area NOVICE_BOAT_AREA = new PestControlNoviceBoatArea();

    /**
     * The tile which is right beside the gang plank.
     */
    public static final Location GANG_PLANK_START = new Location(2657, 2639, 0);

    /**
     * /** how long before were put into the game from lobby
     */
    private static final int WAIT_TIMER = 60;
    /**
     * How many players we need to start a game
     */
    private final static int PLAYERS_REQUIRED = 1;

    public static int gameTimer;
    public static int lobbyTimer = 60;
    public static boolean gameStarted = false;

    private List<NPC> spawned_npcs = Lists.newArrayList();

    /**
     * Array used for storing the portals health
     */
    public static int[] portalHealth = {200, 200, 200, 200};
    public static int[] portals = {3777, 3778, 3779, 3780};

    private final int[][] pcNPCData = {{portals[0], 2628, 2591}, // portal
            {portals[1], 2680, 2588}, // portal
            {portals[2], 2669, 2570}, // portal
            {portals[3], 2645, 2569}, // portal
            {VOID_KNIGHT_GAME, 2656, 2592},
    };

    /**
     * Determines whether the game is still active.
     *
     * @return
     */
    public boolean isActive() {
        return gameTimer > 0 && gameStarted;
    }

    public void spawnNPC(int id, Location pos) {
        /**
         * TODO support for private area
         */
        NPC npc = new NPC(id, pos);
        spawned_npcs.add(npc);
    }

    /**
     * Moves the player to a random spot within the launcher boat.
     *
     * @param player
     */
    public void movePlayerToBoat(Player player) {
        player.smartMove(PestControlArea.LAUNCHER_BOAT_BOUNDARY);
    }

    private static final int[][] PEST_CONTROL_MONSTERS = {
            {
                1724,//Defiler - level 33
                    1726,//Defiler - level 50
            },
            {
                1725,//Defiler - level 50
                    1727,//Defiler - level 66
                    1728,//Defiler - level 80
            },
            {
                1730,//Defiler - level 80
                    1732,//Defiler - level 97
            }
    };

    @Override
    public boolean firstClickObject(Player player, GameObject object) {
        // All object handling should be done in Areas where possible
        return false;
    }

    @Override
    public boolean handleButtonClick(Player player, int button) {
        return false;
    }

    @Override
    public void process() {
        try {

            if (!isActive()) {
                return;
            }

            gameTimer--;

            if (playersInGame() < 1 || getKnightsHealth() == 0) {
                endGame(false);
            }
            if (allPortalsDead() || allPortalsDead3()) {
                endGame(true);
            }




        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int getKnightsHealth() {
        /** For loop due to calling every tick **/
        for (NPC npc : spawned_npcs) {
            if (npc != null && npc.getId() == 1755)
                return npc.getHitpoints();
        }
        return 0;
    }

    @Override
    public void init() {

    }

    /**
     * Method we use for removing a player from the pc game
     *
     * @param player The Player.
     */
    public static void removePlayerGame(Player player) {
        player.moveTo(new Location(2657, 2639, 0));
    }

    /***
     * Moving players to arena if there's enough players
     */
    private void startGame() {
        if (playersInBoat() < PLAYERS_REQUIRED) {
            lobbyTimer = WAIT_TIMER;
            return;
        }
        for (int i = 0; i < portalHealth.length; i++) {
            portalHealth[i] = 200;
        }
        gameStarted = true;
        gameTimer = 400;
        lobbyTimer = -1;
        spawnNPC();

        // Send all the players into the minigame
        NOVICE_BOAT_AREA.getPlayers().forEach((player) -> {
            movePlayerToBoat(player);
            NpcDialogue.send(player, NpcIdentifiers.VOID_KNIGHT, "The Pest Control game has begun!", DialogueExpression.HAPPY);
        });
    }

    /**
     * Checks how many players are in the waiting lobby
     *
     * @return players waiting
     */
    private static int playersInBoat() {
        return NOVICE_BOAT_AREA.getPlayers().size();
    }

    /**
     * Checks how many players are in the game
     *
     * @return players in the game
     */
    private int playersInGame() {
        return GAME_AREA.getPlayers().size();
    }

    private void endGame(boolean won) {
        for (Player player : GAME_AREA.getPlayers()) {
            player.moveTo(new Location(2657, 2639, 0));
            Integer damage = (Integer) player.getAttribute("pcDamage");
            int myDamage = damage == null ? 0 : damage;
            if (won && myDamage > 50) {
                NpcDialogue.send(player, NpcIdentifiers.VOID_KNIGHT, "Do not let the Void Knights health reach 0!" +
                        "You can regain health by destroying more monsters,", DialogueExpression.SLIGHTLY_SAD);
                int POINT_REWARD = 4;
                player.getPacketSender().sendMessage(
                        "You have won the pest control game and have been awarded "
                                + POINT_REWARD + " Pest Control points.");
                player.pcPoints += POINT_REWARD;
                //gp reward removed
            } else if (won) {
                int POINT_REWARD2 = 2;
                NpcDialogue.send(player, NpcIdentifiers.VOID_KNIGHT, "The void knights notice your lack of zeal. You only gain "
                        + POINT_REWARD2 + " points.", DialogueExpression.DISTRESSED);
                player.pcPoints += POINT_REWARD2;
            } else {
                NpcDialogue.send(player, NpcIdentifiers.VOID_KNIGHT,
                        "You failed to kill all the portals in 3 minutes and have not been awarded points.", DialogueExpression.CALM);
                player.getPacketSender()
                        .sendMessage(
                                "You failed to kill all the portals in 3 minutes and have not been awarded points.");
            }
        }
        cleanUp();
    }

    /**
     * Resets the game variables and map
     */
    private void cleanUp() {
        gameTimer = -1;
        lobbyTimer = WAIT_TIMER;
        gameStarted = false;

        /*
         * Removes the npcs from the game if any left over for whatever reason
         */
        for (int[] aPcNPCData : pcNPCData) {
            for (int j = 0; j < NPC.npcs.length; j++) {
                if (NPC.npcs[j] != null) {
                    if (NPC.npcs[j].getId() == aPcNPCData[0]) {
                        NPC.npcs[j] = null;
                    }
                }
            }
        }
    }

    /**
     * Checks if the portals are dead
     *
     * @return players dead
     */
    private static boolean allPortalsDead() {
        int count = 0;
        for (int aPortalHealth : portalHealth) {
            if (aPortalHealth <= 0) {
                count++;
                // System.out.println("Portal Health++" + count);
            }
        }
        return count >= 4;
    }

    public boolean allPortalsDead3() {
        int count = 0;
        for (NPC npc : getNpcs()) {
            if (npc != null) {
                if (npc.getId() > 3777 && npc.getId() < 3780) {
                    if (npc.needRespawn) {
                        count++;
                    }
                }
            }
        }
        return count >= 4;
    }

    private static Queue<Player> novice_boat = Lists.newLinkedList(), intermediate_boat = Lists.newLinkedList(), veteran_boat = Lists.newLinkedList();

    private static boolean onList(Player player) {
        return novice_boat.contains(player) || intermediate_boat.contains(player) || veteran_boat.contains(player);
    }
    private static void addToQueue(Player player, PestControlBoat boat) {
        if (onList(player)) {
            System.err.println("Error.. adding "+player.getUsername()+" to "+boat.name()+" list.. already on the list.");
            return;
        }
        /**
         * TODO.. might be a good idea to get the players in the area then add all to the list.. however.. pest control uses a queue system not list!
         */
        switch (boat) {
            case VETERAN -> veteran_boat.add(player);
            case INTERMEDIATE -> intermediate_boat.add(player);
            case NOVICE -> novice_boat.add(player);
        }
    }
    /**
     * Moves a player into the hash and into the lobby
     *
     * @param player The player
     */
    public static void addToWaitingRoom(Player player, PestControlBoat boat) {

        player.getPacketSender().sendMessage("You have joined the Pest Control boat.");
        player.getPacketSender().sendMessage("You currently have " + player.pcPoints + " Pest Control Points.");
        player.getPacketSender().sendMessage("There are currently " + playersInBoat() + " players ready in the boat.");
        player.getPacketSender().sendMessage("Players needed: " + PLAYERS_REQUIRED + " to 25 players.");
        addToQueue(player, boat);
        player.moveTo(boat.enterBoatLocation);
    }


    public static boolean npcIsPCMonster(int npcType) {
        return (npcType >= 3727 && npcType <= 3776);
    }

    public static boolean isPCPortal(int npcType) {
        return (npcType >= 3777 && npcType <= 3780);
    }

    private void spawnNPC() {
        //npcid, npcx, npcy, heightlevel, walking type, hp, att, def
        for (int[] aPcNPCData : pcNPCData) {
            World.getAddNPCQueue().add(new NPC(aPcNPCData[0], new Location(aPcNPCData[1], aPcNPCData[2])));
        }
    }
}
