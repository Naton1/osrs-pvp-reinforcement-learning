package com.elvarg.game.content.skill.skillable.impl;

import com.elvarg.game.content.PetHandler;
import com.elvarg.game.entity.impl.grounditem.ItemOnGround;
import com.elvarg.game.entity.impl.grounditem.ItemOnGroundManager;
import com.elvarg.game.entity.impl.object.GameObject;
import com.elvarg.game.entity.impl.object.ObjectManager;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.*;
import com.elvarg.game.model.movement.MovementQueue;
import com.elvarg.game.task.Task;
import com.elvarg.game.task.TaskManager;
import com.elvarg.game.task.impl.TimedObjectSpawnTask;
import com.elvarg.util.ItemIdentifiers;
import com.elvarg.util.Misc;
import com.elvarg.util.ObjectIdentifiers;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Represents the Firemaking skill.
 * <p>
 * Has support for lighting logs that are on the ground
 * and for adding logs to fires (bonfire).
 *
 * @author Professor Oak
 */
public class Firemaking extends DefaultSkillable {

    /**
     * The {@link Animation} for lighting a fire.
     */
    public static final Animation LIGHT_FIRE = new Animation(733);
    /**
     * The {@link LightableLog} which we will be attempting
     * to light.
     */
    private final LightableLog log;
    /**
     * A log on the ground.
     * <p>
     * If present - we will focus on lighting this instead of
     * a log from the inventory.
     */
    private Optional<ItemOnGround> groundLog = Optional.empty();
    /**
     * Represents a bonfire, which we will be adding logs to
     * if present.
     */
    private Optional<GameObject> bonfire = Optional.empty();
    /**
     * Represents the amount of logs to add to a bonfire.
     */
    private int bonfireAmount;

    /**
     * Creates a Firemaking instance where we will be
     * lighting a {@link LightableLog} from our inventory.
     *
     * @param log
     */
    public Firemaking(LightableLog log) {
        this.log = log;
    }

    /**
     * Creates a Firemaking instance where
     * we'll be lighting a log which is
     * already on the ground.
     *
     * @param log
     * @param groundLog
     */
    public Firemaking(LightableLog log, ItemOnGround groundLog) {
        this.log = log;
        this.groundLog = Optional.of(groundLog);
    }

    /**
     * Creates a Firemaking instance where we'll
     * be adding logs to a bonfire.
     *
     * @param log
     * @param bonfire
     * @param bonfireAmount
     */
    public Firemaking(LightableLog log, GameObject bonfire, int bonfireAmount) {
        this.log = log;
        this.bonfire = Optional.of(bonfire);
        this.bonfireAmount = bonfireAmount;
    }

    /**
     * Checks if we should light a log.
     *
     * @param player
     * @param itemUsed
     * @param itemUsedWith
     * @return
     */
    public static boolean init(Player player, int itemUsed, int itemUsedWith) {
        if (itemUsed == ItemIdentifiers.TINDERBOX || itemUsedWith == ItemIdentifiers.TINDERBOX) {
            int logId = itemUsed == ItemIdentifiers.TINDERBOX ? itemUsedWith : itemUsed;
            Optional<LightableLog> log = LightableLog.getForItem(logId);
            if (log.isPresent()) {
                player.getSkillManager().startSkillable(new Firemaking(log.get()));
            }
            return true;
        }
        return false;
    }

    @Override
    public void start(Player player) {
        //Reset movement queue..
        player.getMovementQueue().reset();

        //Send message..
        player.getPacketSender().sendMessage("You attempt to light the logs..");

        //If we're lighting a log from our inventory..
        if (!groundLog.isPresent() && !bonfire.isPresent()) {
            //Delete logs from inventory..
            player.getInventory().delete(log.getLogId(), 1);

            //Place logs on ground..
            groundLog = Optional.of(ItemOnGroundManager.register(player, new Item(log.getLogId(), 1)));
        }

        //Face logs if present.
        if (groundLog.isPresent()) {
            player.setPositionToFace(groundLog.get().getLocation());
        }

        //Start parent execution task..
        super.start(player);
    }

    @Override
    public void startAnimationLoop(Player player) {
        //If we're not adding to a bonfire
        //Simply do the regular animation.
        if (!bonfire.isPresent()) {
            player.performAnimation(LIGHT_FIRE);
            return;
        }
        Task animLoop = new Task(3, player, true) {
            @Override
            protected void execute() {
                player.performAnimation(Cooking.ANIMATION); //Cooking anim looks fine for bonfires
            }
        };
        TaskManager.submit(animLoop);
        getTasks().add(animLoop);
    }

    @Override
    public void onCycle(Player player) {
        PetHandler.onSkill(player, Skill.FIREMAKING);
    }

    @Override
    public void finishedCycle(Player player) {
        //Handle reset of skill..
        if (bonfire.isPresent()) {
            if (bonfireAmount-- <= 0) {
                cancel(player);
            }
        } else {
            cancel(player);
        }

        //If we're adding to a bonfire or the log on ground still exists... Reward player.
        if (bonfire.isPresent() || groundLog.isPresent() && ItemOnGroundManager.exists(groundLog.get())) {

            //If we aren't adding to a bonfire..
            if (!bonfire.isPresent()) {
                //The position to create the fire at..
                final Location pos = groundLog.get().getLocation().clone();

                //Delete logs from ground ..
                ItemOnGroundManager.deregister(groundLog.get());

                //Create fire..
                TaskManager.submit(new TimedObjectSpawnTask(new GameObject(ObjectIdentifiers.FIRE_5, pos, 10, 0, player.getPrivateArea()), log.getRespawnTimer(), Optional.of(new Action() {
                    @Override
                    public void execute() {
                        if (!ItemOnGroundManager.getGroundItem(Optional.of(player.getUsername()), ItemIdentifiers.ASHES, pos).isPresent()) {
                            ItemOnGroundManager.register(player, new Item(ItemIdentifiers.ASHES), pos);
                        }
                    }
                })));

                //Step away from the fire..
                if (player.getLocation().equals(pos)) {
                    MovementQueue.clippedStep(player);
                }
            } else {
                //Delete logs from inventory when using a bonfire..
                player.getInventory().delete(log.getLogId(), 1);
            }

            //Add experience..
            player.getSkillManager().addExperience(Skill.FIREMAKING, log.getExperience());

            //Send message..
            player.getPacketSender().sendMessage("The logs catch fire and begin to burn.");
        }
    }

    @Override
    public int cyclesRequired(Player player) {
        if (bonfire.isPresent()) { //Cycle rate for adding to bonfire is constant.
            return 2;
        }
        int cycles = log.getCycles() + Misc.getRandom(2);
        cycles -= (int) player.getSkillManager().getMaxLevel(Skill.FIREMAKING) * 0.1;
        if (cycles < 3) {
            cycles = 3;
        }
        return cycles;
    }

    @Override
    public boolean hasRequirements(Player player) {
        //If we aren't adding logs to a fire - make sure player has a tinderbox..
        if (!bonfire.isPresent()) {
            if (!player.getInventory().contains(ItemIdentifiers.TINDERBOX)) {
                player.getPacketSender().sendMessage("You need a tinderbox to light fires.");
                return false;
            }
        }

        //Check if we've burnt the amount of logs on the bonfire.
        if (bonfire.isPresent() && bonfireAmount <= 0) {
            return false;
        }

        //If we aren't lighting a log on the ground, make sure we have at least one in our inventory.
        if (!groundLog.isPresent()) {
            if (!player.getInventory().contains(log.getLogId())) {
                player.getPacketSender().sendMessage("You've run out of logs.");
                return false;
            }
        }

        //If we're adding to a bonfire - make sure it still exists.
        //If we're not adding to a fire, make sure no object exists in our position.
        if (bonfire.isPresent()) {
            if (!ObjectManager.exists(ObjectIdentifiers.FIRE_5, bonfire.get().getLocation())) {
                return false;
            }
        } else {
            //Check if there's already an object where the player wants to light a fire..
            if (/*ClippedRegionManager.getObject(player.getPosition()).isPresent()
                    ||*/ ObjectManager.exists(player.getLocation())) {
                player.getPacketSender().sendMessage("You cannot light a fire here. Try moving around a bit.");
                return false;
            }
        }

        return super.hasRequirements(player);
    }

    @Override
    public boolean loopRequirements() {
        //We may have run out of logs
        //when using bonfire.
        if (bonfire.isPresent()) {
            return true;
        }

        return false;
    }

    @Override
    public boolean allowFullInventory() {
        return true;
    }

    /**
     * Represents a log which can be lit using
     * the Firemaking skill.
     *
     * @author Professor Oak
     */
    public enum LightableLog {
        NORMAL(1511, 1, 40, 7, 60),
        ACHEY(2862, 1, 40, 7, 65),
        OAK(1521, 15, 60, 8, 70),
        WILLOW(1519, 30, 90, 9, 80),
        TEAK(6333, 35, 105, 9, 80),
        ARTIC_PINE(10810, 42, 125, 10, 80),
        MAPLE(1517, 45, 135, 10, 85),
        MAHOGANY(6332, 50, 157, 11, 85),
        EUCALYPTUS(12581, 58, 193, 12, 85),
        YEW(1515, 60, 202, 13, 90),
        MAGIC(1513, 75, 303, 15, 100),
        REDWOOD(19669, 90, 350, 18, 120);

        public static Map<Integer, LightableLog> lightableLogs = new HashMap<>();

        static {
            for (LightableLog log : LightableLog.values()) {
                lightableLogs.put(log.logId, log);
            }
        }

        private int logId;
        private int level;
        private int experience;
        private int cycles;
        private int respawnTimer;

        LightableLog(int logId, int level, int experience, int cycles, int respawnTimer) {
            this.logId = logId;
            this.level = level;
            this.experience = experience;
            this.cycles = cycles;
            this.respawnTimer = respawnTimer;
        }

        public static Optional<LightableLog> getForItem(int item) {
            return Optional.ofNullable(lightableLogs.get(item));
        }

        public int getExperience() {
            return experience;
        }

        public int getLogId() {
            return logId;
        }

        public int getLevel() {
            return level;
        }

        public int getCycles() {
            return cycles;
        }

        public int getRespawnTimer() {
            return respawnTimer;
        }
    }
}
