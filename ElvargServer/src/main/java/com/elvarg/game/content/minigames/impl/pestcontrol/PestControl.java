package com.elvarg.game.content.minigames.impl.pestcontrol;

import com.elvarg.game.World;
import com.elvarg.game.content.minigames.Minigame;
import com.elvarg.game.entity.impl.npc.NPC;
import com.elvarg.game.entity.impl.npc.impl.pestcontrol.PestControlPortalNPC;
import com.elvarg.game.entity.impl.object.GameObject;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.Item;
import com.elvarg.game.model.Location;
import com.elvarg.game.model.areas.Area;
import com.elvarg.game.model.areas.impl.PrivateArea;
import com.elvarg.game.model.areas.impl.pestcontrol.PestControlArea;
import com.elvarg.game.model.areas.impl.pestcontrol.PestControlNoviceBoatArea;
import com.elvarg.game.model.areas.impl.pestcontrol.PestControlOutpostArea;
import com.elvarg.game.model.dialogues.DialogueExpression;
import com.elvarg.game.model.dialogues.builders.DialogueChainBuilder;
import com.elvarg.game.model.dialogues.entries.impl.*;
import com.elvarg.game.task.Task;
import com.elvarg.game.task.TaskManager;
import com.elvarg.util.Misc;
import com.elvarg.util.NpcIdentifiers;
import com.google.common.collect.Lists;

import java.util.*;

import static com.elvarg.util.NpcIdentifiers.*;

public class PestControl implements Minigame {

    /**
     * Spawn all NPCS outside arena
     * Make NPCs auto attack the gates, then void knight, defend portals
     * Waiting boat interface
     * Game area interface
     * Instanced games and support for novice/med/advanced boats
     * Gates need to open
     * Fence damaging and repairing
     * Handle gates
     * Fix NPC ids for portal spawns
     **/

    private static PrivateArea area;

    public PestControl(PestControlBoat boatType) {
        this.boatType = boatType;
        this.area = new PestControlArea();
    }
    public static final Area GAME_AREA = new PestControlArea();

    public static final Area OUTPOST_AREA = new PestControlOutpostArea();

    public static final Area NOVICE_BOAT_AREA = new PestControlNoviceBoatArea();

    /**
     * The tile which is right beside the gang plank.
     */
    public static final Location GANG_PLANK_START = new Location(2657, 2639, 0);
    /**
     * How many players we need to start a game
     */
    private final static int PLAYERS_REQUIRED = 1;//5 default

    public int ticksElapsed;
    public static final int DEFAULT_BOAT_WAITING_TICKS = 10;//50 secs default

    private List<NPC> spawned_npcs = Lists.newArrayList();

    private PestControlPortalData[] chosenPortalSpawnSequence;

    public static PestControlBoat boatType;

    private static final PestControlPortalData[][] PORTAL_SEQUENCE = {
            { PestControlPortalData.BLUE, PestControlPortalData.RED, PestControlPortalData.YELLOW, PestControlPortalData.PURPLE },
            { PestControlPortalData.BLUE, PestControlPortalData.PURPLE, PestControlPortalData.RED, PestControlPortalData.YELLOW },
            { PestControlPortalData.PURPLE, PestControlPortalData.BLUE, PestControlPortalData.YELLOW, PestControlPortalData.RED },
            { PestControlPortalData.PURPLE, PestControlPortalData.YELLOW, PestControlPortalData.BLUE, PestControlPortalData.RED },
            { PestControlPortalData.YELLOW, PestControlPortalData.RED, PestControlPortalData.PURPLE, PestControlPortalData.BLUE },
            { PestControlPortalData.YELLOW, PestControlPortalData.PURPLE, PestControlPortalData.RED, PestControlPortalData.BLUE }
    };

    private int totalPortalsUnlocked;
    private void unshieldPortal() {
        if (chosenPortalSpawnSequence == null)
            return;
        PestControlPortalData data = chosenPortalSpawnSequence[totalPortalsUnlocked];
        Optional<NPC> portal = spawned_npcs.stream().filter(n -> n != null && n.getId() == data.shieldId).findFirst();
        if (!portal.isPresent())
            return;
        GAME_AREA.getPlayers().forEach(p -> p.getPacketSender().sendMessage("The <col="+data.colourCode+">"+data.name().toLowerCase().replaceAll("_", " ")+", "+data.name+"</col> portal shield has dropped!"));
        totalPortalsUnlocked++;
        portal.get().setNpcTransformationId(data.unshieldId);
    }

    public static void healKnight() {
        Optional<NPC> knight = area.getNpcs().stream().filter(k -> k.getId() == boatType.void_knight_id).findFirst();
        if (!knight.isPresent())
            return;
        knight.get().heal(50);
    }

    public boolean timeExpired() {
        return ticksElapsed >= (100 * 20);
    }

    @Override
    public void init() {
        PestControlBoat novice_boat = PestControlBoat.NOVICE;
        PestControlBoat intermediate_boat = PestControlBoat.INTERMEDIATE;
        PestControlBoat veteran_boat = PestControlBoat.VETERAN;
        Task noviceLobbyTask = new Task(1, PestControlBoat.NOVICE.name()) {

            int noviceWaitTicks = DEFAULT_BOAT_WAITING_TICKS;
            @Override
            protected void execute() {

                int playersReady = novice_boat.getQueue().size();

                if (playersReady == 0) {
                    noviceWaitTicks = DEFAULT_BOAT_WAITING_TICKS;
                    return;
                }

                if (playersReady >= 10 && Math.random() <= .15) {
                    sendSquireMessage("We're about to lanch!", novice_boat);
                }

                noviceWaitTicks--;

                if (noviceWaitTicks == 0 || playersReady >= 25) {
                    noviceWaitTicks = DEFAULT_BOAT_WAITING_TICKS;
                    begin(novice_boat);
                }

            }
        };
        TaskManager.submit(noviceLobbyTask);

        Task intermediateLobbyTask = new Task(1, PestControlBoat.INTERMEDIATE.name()) {

            int intermediateWaitTicks = DEFAULT_BOAT_WAITING_TICKS;
            @Override
            protected void execute() {

                int playersReady = intermediate_boat.getQueue().size();

                if (playersReady == 0) {
                    intermediateWaitTicks = DEFAULT_BOAT_WAITING_TICKS;
                    return;
                }

                if (playersReady >= 10 && Math.random() <= .15) {
                    sendSquireMessage("We're about to lanch!", novice_boat);
                }

                intermediateWaitTicks--;

                if (intermediateWaitTicks == 0 || playersReady >= 25) {
                    intermediateWaitTicks = DEFAULT_BOAT_WAITING_TICKS;
                    begin(intermediate_boat);
                }

            }
        };
        TaskManager.submit(intermediateLobbyTask);

        Task veteranLobbyTask = new Task(1, PestControlBoat.VETERAN.name()) {

            int veteranWaitTicks = DEFAULT_BOAT_WAITING_TICKS;
            @Override
            protected void execute() {

                int playersReady = veteran_boat.getQueue().size();

                if (playersReady == 0) {
                    veteranWaitTicks = DEFAULT_BOAT_WAITING_TICKS;
                    return;
                }

                if (playersReady >= 10 && Math.random() <= .15) {
                    sendSquireMessage("We're about to lanch!", novice_boat);
                }

                veteranWaitTicks--;

                if (veteranWaitTicks == 0 || playersReady >= 25) {
                    veteranWaitTicks = DEFAULT_BOAT_WAITING_TICKS;
                    begin(veteran_boat);
                }

            }
        };
        TaskManager.submit(veteranLobbyTask);
    }

    private void begin(PestControlBoat boat) {
        Queue<Player> queue = boat.getQueue();
        Iterator lobbyQueue = queue.iterator();
        PestControlArea area = new PestControlArea();
        setupEntities(boat);
        int movedPlayers = 0;
        while (lobbyQueue.hasNext()) {
            if (movedPlayers >= 25) {
                break;
            }
            movedPlayers++;
            Player player = queue.poll();
            if (player != null) {
                moveToGame(boat, player, area);
            }
        }
        if (queue.size() > 0) {
            queue.forEach(p -> p.getPacketSender().sendMessage("You have been given priority level 1 over other players in joining the next game."));
        }
    }

    private void sendSquireMessage(String message, PestControlBoat boat) {
        Optional<NPC> squire = World.getNpcs().stream().filter(n -> n.getId() == boat.squireId).findFirst();
        if (!squire.isPresent() || message == null || message.isEmpty())
            return;
        squire.get().forceChat(message);
    }

    public void setupEntities(PestControlBoat boat) {
        /** Knight in the middle **/
        spawnNPC(boat.void_knight_id, new Location(2656, 2592));
        /** Squire to leave next to boat **/
        spawnNPC(SQUIRE_12, new Location(2655, 2607));
        /** Rando PestControlPortal Sequence **/
        chosenPortalSpawnSequence = PORTAL_SEQUENCE[Misc.random(PORTAL_SEQUENCE.length - 1)];
        /** Portal list **/
        portals = Lists.newCopyOnWriteArrayList();
        /** PestControlPortal spawns **/
        Arrays.stream(chosenPortalSpawnSequence).forEach(portal -> spawnPortal(portal.shieldId, new Location(portal.xPosition, portal.yPosition)));

        Task portalTask = new Task(1) {

            int ticks = 50;
            @Override
            protected void execute() {

                if (totalPortalsUnlocked == 4 || isKnightDead()) {
                    stop();
                    return;
                }

                ticks--;

                if (ticks == 0 || totalPortalsUnlocked == 0 && ticks / 2 == 15) {
                    unshieldPortal();
                    ticks = 50;
                }

            }
        };
        TaskManager.submit(portalTask);
    }

    private void moveToGame(PestControlBoat boat, Player player, PestControlArea area) {
        area.add(player);
        player.smartMove(PestControlArea.LAUNCHER_BOAT_BOUNDARY);
        NpcDialogue.sendStatement(player, NpcIdentifiers.SQUIRE_12, new String[] {"You must defend the Void Knight while the portals are", "unsummoned. The ritual takes twenty minutes though,", "so you can help out by destroying them yourselves!", "Now GO GO GO!" }, DialogueExpression.DISTRESSED);

        /**
         * gameStarted = true;
         * gameTimer = 400;
         */
    }

    /**
     * Determines whether the game is still active.
     *
     * @return
     */
    public boolean isActive() {
        return playersInGame() > 0 && boatType != null;
    }

    public void spawnNPC(int id, Location pos) {
        NPC npc = new NPC(id, pos);
        area.add(npc);
        spawned_npcs.add(npc);
        World.getAddNPCQueue().add(npc);
    }

    public static List<PestControlPortalNPC> portals;

    public void spawnPortal(int id, Location pos) {
        PestControlPortalNPC npc = new PestControlPortalNPC(id, pos);
        int hitPoints = boatType == PestControlBoat.NOVICE ? 200 : 250;
        npc.setHitpoints(hitPoints);
        npc.getDefinition().setMaxHitpoints(hitPoints);
        portals.add(npc);
        area.add(npc);
        spawned_npcs.add(npc);
        World.getAddNPCQueue().add(npc);
    }

    public static boolean isPortalsDead() {
        return portals != null && portals.size() == 0;
    }

    public static boolean isPortal(int id, boolean shielded) {
        List<Integer> portalIds = Lists.newArrayList();
        for (PestControlPortalData d : PestControlPortalData.values())
            portalIds.add(shielded ? d.shieldId : d.unshieldId);
        return portalIds.stream().anyMatch(s -> s.intValue() == id);
    }

    private static final int[][] PEST_CONTROL_MONSTERS = {
            {
                    BRAWLER,//Brawler - level 51
                    BRAWLER_2,//Brawler - level 76
                    BRAWLER_3,//Brawler - level 101
                    DEFILER,//Defiler - level 33
                    DEFILER_2,//Defiler - level 50
                    RAVAGER, //Ravager - level 36
                    RAVAGER_2, //Ravager - level 53
                    RAVAGER_3,//Ravager - level 71
                    SHIFTER,//Shifter - Level 38
                    SHIFTER_3,//Shifter - Level 57
                    SHIFTER,//Spinner - Level 36
                    SHIFTER_3,//Spinner - Level 55
                    SHIFTER_5,//Spinner - Level 74
                    SPLATTER,//Splatter - Level 22
                    SPLATTER_2,//Splatter - Level 33
                    SPLATTER_3,//Splatter - Level 44
                    TORCHER,//Torcher - Level 33
                    TORCHER_3,//Torcher - Level 49

            },
            {
                    BRAWLER_2,//Brawler - level 76
                    BRAWLER_3,//Brawler - level 101
                    BRAWLER_4,//Brawler - level 129
                    DEFILER_2,//Defiler - level 50
                    DEFILER_4,//Defiler - level 66
                    DEFILER_5,//Defiler - level 80
                    RAVAGER_2, //Ravager - level 53
                    RAVAGER_3,//Ravager - level 71
                    RAVAGER_4, //Ravager - level 89
                    SHIFTER_3,//Shifter - Level 57
                    SHIFTER_5,//Shifter - Level 76
                    SHIFTER_7,//Shifter - Level 90
                    SHIFTER_3,//Spinner - Level 55
                    SHIFTER_5,//Spinner - Level 74
                    SHIFTER_7,//Spinner - Level 88
                    SHIFTER_9,//Spinner - Level 92
                    SPLATTER_2,//Splatter - Level 33
                    SPLATTER_3,//Splatter - Level 44
                    SPLATTER_4,//Splatter - Level 54
                    TORCHER_3,//Torcher - Level 49
                    TORCHER_5,//Torcher - Level 66
                    TORCHER_7,//Torcher - Level 79
            },
            {
                    BRAWLER_3,//Brawler - level 101
                    BRAWLER_4,//Brawler - level 129
                    DEFILER_7,//Defiler - level 80
                    DEFILER_9,//Defiler - level 97
                    RAVAGER_3,//Ravager - level 71
                    RAVAGER_4,//Ravager - level 89
                    RAVAGER_5,//Ravager - level 106
                    SHIFTER_7,//Shifter - Level 90
                    SHIFTER_9,//Shifter - Level 104
                    SHIFTER_5,//Spinner - Level 74
                    SHIFTER_7,//Spinner - Level 88
                    SHIFTER_9,//Spinner - Level 92
                    SPLATTER_3,//Splatter - Level 44
                    SPLATTER_4,//Splatter - Level 54
                    SPLATTER_5,//Splatter - Level 65
                    TORCHER_7,//Torcher - Level 79
                    TORCHER_9,//Torcher - Level 91
                    TORCHER_10,//Torcher - Level 92
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

    private static final int SPAWN_TICK_RATE = 10;

    private int last_spawn = SPAWN_TICK_RATE;

    @Override
    public void process() {
        try {

            if (!isActive()) {
                return;
            }

            ticksElapsed++;

            if (isPortalsDead()) {
                endGame(true);
                return;
            }

            if (playersInGame() < 1 || isKnightDead() || timeExpired()) {
                endGame(false);
            }

            /**
             * NPC spawning..
             */
            if (--last_spawn == 0) {
                last_spawn = SPAWN_TICK_RATE;
                int index = boatType.ordinal();
                Arrays.stream(PestControlPortalData.values()).filter(p -> portalExists(p)).forEach(portal -> spawnNPC(PEST_CONTROL_MONSTERS[index][Misc.random(PEST_CONTROL_MONSTERS[index].length - 1)], new Location(portal.npcSpawnX, portal.npcSpawnY)));
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean portalExists(PestControlPortalData portal) {
        return area.getNpcs().contains(portal);
    }

    private boolean isKnightDead() {
        return getKnightsHealth() == 0;
    }

    public int getKnightsHealth() {
        /** For loop due to calling every tick **/
        for (NPC npc : spawned_npcs) {
            if (npc != null && npc.getId() == boatType.void_knight_id)
                return npc.getHitpoints();
        }
        return 0;
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
        GAME_AREA.getPlayers().forEach(player -> {

            player.moveTo(new Location(2657, 2639, 0));

            Integer damage = (Integer) player.getAttribute("pcDamage");
            int myDamage = damage == null ? 0 : damage;

            int reward_points = 2;

            if (!won) {
                NpcDialogue.send(player, boatType.squireId, "The Void Knight was killed, another of our Order has"+"fallen and that Island is lost.", DialogueExpression.DISTRESSED);
                return;
            }
            if (myDamage > 50) {
                sendWinnerDialogue(player,4, 1, boatType);
                return;
            }
            StatementDialogue.send(player, new String[] {"The void knights notice your lack of zeal in that battle and have not", "presented you with any points."});
            player.pcPoints += reward_points;
        });

        cleanUp();
    }

    public static void sendWinnerDialogue(Player p, int pointsToAdd, int coinReward, PestControlBoat boat) {
        DialogueChainBuilder dialogueBuilder = new DialogueChainBuilder();
            dialogueBuilder.add(
                    new NpcDialogue(0, boat.squireId, "Congratulations! You managed to destroy all the portals!"+" We've awarded you "+pointsToAdd+" Void Knight Commendation"+" points. Please also accept these coins as a reward.", (player) -> {
                        player.pcPoints += pointsToAdd;
                        player.getInventory().add(new Item(995, coinReward));
                        StatementDialogue.send(player, new String[] {"<col=00077a>You now have</col><col=b11717> "+player.pcPoints+"</col><col=00077a> Void Knight Commendation points!", "You can speak to a Void Knight to exchange your points for", "rewards."});
                    }),
                    new EndDialogue(1)
            );
        p.getDialogueManager().start(dialogueBuilder, 0);
    }

    /**
     * Resets the game variables and map
     */
    private void cleanUp() {
        ticksElapsed = -1;
        boatType = null;
        chosenPortalSpawnSequence = null;
        totalPortalsUnlocked = 0;
        spawned_npcs.stream().filter(n -> n != null).forEach(n -> n.setDying(true));
        spawned_npcs.clear();
    }

    private static boolean isQueued(Player player, PestControlBoat boat) {
        return boat.getQueue().contains(player);
    }



    private static void addToQueue(Player player, PestControlBoat boat) {
        if (isQueued(player, boat)) {
            System.err.println("Error.. adding " + player.getUsername() + " to " + boat.name() + " list.. already on the list.");
            return;
        }
        /**
         * TODO.. might be a good idea to get the players in the area then add all to the list.. however.. pest control uses a queue system not list!
         */
        boat.getQueue().add(player);
    }

    /**
     * Moves a player into the hash and into the lobby
     *
     * @param player The player
     */
    public static void addToWaitingRoom(Player player, PestControlBoat boat) {
        player.getPacketSender().sendMessage("You have joined the Pest Control boat.");
        player.getPacketSender().sendMessage("You currently have " + player.pcPoints + " Pest Control Points.");
        player.getPacketSender().sendMessage("Players needed: " + PLAYERS_REQUIRED + " to 25 players.");
        addToQueue(player, boat);
        player.moveTo(boat.enterBoatLocation);
    }


}
