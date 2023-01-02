package com.elvarg.game.content.skill.skillable.impl;

import com.elvarg.game.content.PetHandler;
import com.elvarg.game.entity.impl.object.GameObject;
import com.elvarg.game.entity.impl.object.MapObjects;
import com.elvarg.game.entity.impl.object.ObjectManager;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.Animation;
import com.elvarg.game.model.Location;
import com.elvarg.game.model.Skill;
import com.elvarg.game.model.container.impl.Equipment;
import com.elvarg.game.task.Task;
import com.elvarg.game.task.TaskManager;
import com.elvarg.game.task.impl.TimedObjectReplacementTask;
import com.elvarg.util.Misc;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Represents the Mining skill.
 *
 * @author Professor Oak
 */
public class Mining extends DefaultSkillable {

    /**
     * The {@link GameObject} to mine.
     */
    private final GameObject rockObject;
    /**
     * The {@code rock} as an enumerated type
     * which contains information about it, such as
     * required level.
     */
    private final Rock rock;
    /**
     * The pickaxe we're using to mine.
     */
    private Optional<Pickaxe> pickaxe = Optional.empty();

    /**
     * Constructs a new {@link Mining}.
     *
     * @param rockObject The rock to mine.
     * @param rock       The rock's data
     */
    public Mining(GameObject rockObject, Rock rock) {
        this.rockObject = rockObject;
        this.rock = rock;
    }

    @Override
    public void start(Player player) {
        player.getPacketSender().sendMessage("You swing your pickaxe at the rock..");
        super.start(player);
    }

    @Override
    public void startAnimationLoop(Player player) {
        Task animLoop = new Task(6, player, true) {
            @Override
            protected void execute() {
                player.performAnimation(pickaxe.get().getAnimation());
            }
        };
        TaskManager.submit(animLoop);
        getTasks().add(animLoop);
    }

    @Override
    public void onCycle(Player player) {
        PetHandler.onSkill(player, Skill.MINING);
    }

    @Override
    public void finishedCycle(Player player) {
        // Add ores..
        if (rock.getOreId() > 0) {
            player.getInventory().add(rock.getOreId(), 1);
            player.getPacketSender().sendMessage("You get some ores.");
        }
        // Add exp..
        if (rock.xpReward > 0) {
            player.getSkillManager().addExperience(Skill.MINING, rock.getXpReward());
        }
        // Stop skilling..
        cancel(player);

        if (rock == Rock.CASTLE_WARS_ROCKS) {
            int id = rockObject.getId() + 1;
            Location loc = rockObject.getLocation();
            int face = rockObject.getFace();
            // Despawn original object..
            ObjectManager.deregister(rockObject, false);
            // Spawn temp object..
            if (id == 4439) {
                ObjectManager.deregister(new GameObject(-1, loc, 10, face, null), true);
                return;
            }
            ObjectManager.register(new GameObject(id, loc, 10, face, null), true);
            return;
        }
        // Despawn object and respawn it after a short period of time..
        TaskManager.submit(new TimedObjectReplacementTask(rockObject,
                new GameObject(2704, rockObject.getLocation(), 10, 0, player.getPrivateArea()),
                rock.getRespawnTimer()));
    }

    @Override
    public int cyclesRequired(Player player) {
        float cycles = rock.getCycles() + Misc.getRandom(4);
        cycles -= player.getSkillManager().getCurrentLevel(Skill.MINING) * 0.1;
        cycles -= cycles * pickaxe.get().getSpeed();

        return Math.max(3, (int) cycles);
    }

    @Override
    public boolean hasRequirements(Player player) {
        //Attempt to find a pickaxe..
        pickaxe = Optional.empty();
        for (Pickaxe a : Pickaxe.values()) {
            if (player.getEquipment().getItems()[Equipment.WEAPON_SLOT].getId() == a.getId()
                    || player.getInventory().contains(a.getId())) {

                //If we have already found a pickaxe,
                //don't select others that are worse or can't be used
                if (pickaxe.isPresent()) {
                    if (player.getSkillManager().getMaxLevel(Skill.MINING) < a.getRequiredLevel()) {
                        continue;
                    }
                    if (a.getRequiredLevel() < pickaxe.get().getRequiredLevel()) {
                        continue;
                    }
                }

                pickaxe = Optional.of(a);
            }
        }

        //Check if we found one..
        if (!pickaxe.isPresent()) {
            player.getPacketSender().sendMessage("You don't have a pickaxe which you can use.");
            return false;
        }

        //Check if we have the required level to mine this {@code rock} using the {@link Pickaxe} we found..
        if (player.getSkillManager().getCurrentLevel(Skill.MINING) < pickaxe.get().getRequiredLevel()) {
            player.getPacketSender().sendMessage("You don't have a pickaxe which you have the required Mining level to use.");
            return false;
        }

        //Check if we have the required level to mine this {@code rock}..
        if (player.getSkillManager().getCurrentLevel(Skill.MINING) < rock.getRequiredLevel()) {
            player.getPacketSender().sendMessage("You need a Mining level of at least " + rock.getRequiredLevel() + " to mine this rock.");
            return false;
        }

        //Finally, check if the rock object remains there.
        //Another player may have mined it already.
        if (!MapObjects.exists(rockObject)) {
            return false;
        }

        return super.hasRequirements(player);
    }

    @Override
    public boolean loopRequirements() {
        return true;
    }

    @Override
    public boolean allowFullInventory() {
        return false;
    }

    public GameObject getTreeObject() {
        return rockObject;
    }

    /**
     * Holds data related to the pickaxes
     * that can be used for this skill.
     */
    public enum Pickaxe {
        BRONZE(1265, 1, new Animation(625), 0.03),
        IRON(1267, 1, new Animation(626), 0.05),
        STEEL(1269, 6, new Animation(627), 0.09),
        MITHRIL(1273, 21, new Animation(628), 0.13),
        ADAMANT(1271, 31, new Animation(629), 0.16),
        RUNE(1275, 41, new Animation(624), 0.20),
        DRAGON(15259, 61, new Animation(624), 0.25);

        private final int id, requiredLevel;
        private final Animation animation;
        private final double speed;

        private Pickaxe(int id, int req, Animation animaion, double speed) {
            this.id = id;
            this.requiredLevel = req;
            this.animation = animaion;
            this.speed = speed;
        }

        public int getId() {
            return id;
        }

        public int getRequiredLevel() {
            return requiredLevel;
        }

        public Animation getAnimation() {
            return animation;
        }

        public double getSpeed() {
            return this.speed;
        }
    }

    /**
     * Holds data related to the rocks
     * which can be used to train this skill.
     */
    public static enum Rock {
        CLAY(new int[]{9711, 9712, 9713, 15503, 15504, 15505}, 1, 5, 434, 11, 2),
        COPPER(new int[]{7453}, 1, 18, 436, 12, 4),
        TIN(new int[]{7486}, 1, 18, 438, 12, 4),
        IRON(new int[]{7455, 7488}, 15, 35, 440, 13, 5),
        SILVER(new int[]{7457}, 20, 40, 442, 14, 7),
        COAL(new int[]{7456}, 30, 50, 453, 15, 7),
        GOLD(new int[]{9720, 9721, 9722, 11951, 11183, 11184, 11185, 2099}, 40, 65, 444, 15, 10),
        MITHRIL(new int[]{7492, 7459}, 50, 80, 447, 17, 11),
        ADAMANTITE(new int[]{7460}, 70, 95, 449, 18, 14),
        RUNITE(new int[]{14859, 4860, 2106, 2107, 7461}, 85, 125, 451, 23, 45),
        CASTLE_WARS_ROCKS(new int[]{4437, 4438}, 1, 0, -1, 12, -1),
        ;

        private static final Map<Integer, Rock> rocks = new HashMap<Integer, Rock>();

        static {
            for (Rock t : Rock.values()) {
                for (int obj : t.objects) {
                    rocks.put(obj, t);
                }
                rocks.put(t.getOreId(), t);
            }
        }

        private int objects[];
        private int oreId, requiredLevel, xpReward, cycles, respawnTimer;

        private Rock(int[] objects, int requiredLevel, int xpReward, int oreId, int cycles, int respawnTimer) {
            this.objects = objects;
            this.requiredLevel = requiredLevel;
            this.xpReward = xpReward;
            this.oreId = oreId;
            this.cycles = cycles;
            this.respawnTimer = respawnTimer;
        }

        public static Optional<Rock> forObjectId(int objectId) {
            return Optional.ofNullable(rocks.get(objectId));
        }

        public int getRespawnTimer() {
            return respawnTimer;
        }

        public int getRequiredLevel() {
            return requiredLevel;
        }

        public int getXpReward() {
            return xpReward;
        }

        public int getOreId() {
            return oreId;
        }

        public int getCycles() {
            return cycles;
        }
    }
}
