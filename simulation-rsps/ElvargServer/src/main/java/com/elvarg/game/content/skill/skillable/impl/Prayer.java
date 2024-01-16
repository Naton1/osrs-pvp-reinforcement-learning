package com.elvarg.game.content.skill.skillable.impl;

import com.elvarg.game.definition.ItemDefinition;
import com.elvarg.game.entity.impl.object.GameObject;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.Animation;
import com.elvarg.game.model.Graphic;
import com.elvarg.game.model.Skill;
import com.elvarg.game.task.Task;
import com.elvarg.game.task.TaskManager;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Handles the ingame Prayer skill.
 *
 * @author Professor Oak
 */
public class Prayer {

    /**
     * The animation for burying a bone.
     */
    private static final Animation BONE_BURY = new Animation(827);
    /**
     * The amount of milliseconds a player must wait between
     * each bone-bury.
     */
    private static final long BONE_BURY_DELAY = 1000;
    /**
     * The experience multiplier when using bones on a gilded altar.
     */
    private static double GILDED_ALTAR_EXPERIENCE_MULTIPLIER = 3.5;

    /**
     * Checks if we should bury a bone.
     *
     * @param player
     * @param itemId
     * @return
     */
    public static boolean buryBone(Player player, int itemId) {
        Optional<BuriableBone> b = BuriableBone.forId(itemId);
        if (b.isPresent()) {
            if (player.getClickDelay().elapsed(BONE_BURY_DELAY)) {
                player.getSkillManager().stopSkillable();
                player.getPacketSender().sendInterfaceRemoval();
                player.performAnimation(BONE_BURY);
                player.getPacketSender().sendMessage("You dig a hole in the ground..");
                player.getInventory().delete(itemId, 1);
                TaskManager.submit(new Task(1, player, false) {
                    @Override
                    protected void execute() {
                        player.getPacketSender().sendMessage("..and bury the " + ItemDefinition.forId(itemId).getName() + ".");
                        player.getSkillManager().addExperience(Skill.PRAYER, b.get().getXp());
                        stop();
                    }
                });
                player.getClickDelay().reset();
            }
            return true;
        }
        return false;
    }

    /**
     * Represents a bone which can be buried or used
     * on an altar ingame to train the Prayer skill.
     *
     * @author Professor Oak
     */
    public enum BuriableBone {
        BONES(526, 5),
        BAT_BONES(530, 6),
        WOLF_BONES(2859, 6),
        BIG_BONES(532, 15),
        BABYDRAGON_BONES(534, 30),
        JOGRE_BONE(3125, 15),
        ZOGRE_BONES(4812, 23),
        LONG_BONES(10976, 15),
        CURVED_BONE(10977, 15),
        SHAIKAHAN_BONES(3123, 25),
        DRAGON_BONES(536, 72),
        FAYRG_BONES(4830, 84),
        RAURG_BONES(4832, 96),
        OURG_BONES(14793, 140),
        DAGANNOTH_BONES(6729, 125),
        WYVERN_BONES(6816, 72),
        LAVA_DRAGON_BONES(11943, 85);

        static final Map<Integer, BuriableBone> bones = new HashMap<Integer, BuriableBone>();

        static {
            for (BuriableBone b : BuriableBone.values()) {
                bones.put(b.boneId, b);
            }
        }

        private int boneId;
        private int xp;

        BuriableBone(int boneId, int buryXP) {
            this.boneId = boneId;
            this.xp = buryXP;
        }

        public static Optional<BuriableBone> forId(int itemId) {
            return Optional.ofNullable(bones.get(itemId));
        }

        public int getBoneID() {
            return boneId;
        }

        public int getXp() {
            return xp;
        }
    }

    /**
     * Handles the altar offering.
     *
     * @author Professor Oak
     */
    public static final class AltarOffering extends DefaultSkillable {
        /**
         * The {@link Animation} used for offering bones on the altar.
         */
        private static final Animation ALTAR_OFFERING_ANIMATION = new Animation(713);

        /**
         * The {@link Graphic} which will be performed by the {@link GameObject}
         * altar once bones are offered on it.
         */
        private static final Graphic ALTAR_OFFERING_GRAPHIC = new Graphic(624);

        /**
         * The {@link BuriableBone} that's being offered.
         */
        private final BuriableBone bone;

        /**
         * The {@link GameObject} altar which we're using
         * to offer the bones on.
         */
        private final GameObject altar;

        /**
         * The amount of bones that are being offered.
         */
        private int amount;

        /**
         * Constructs this {@link DefaultSkillable}.
         *
         * @param bone
         */
        public AltarOffering(BuriableBone bone, GameObject altar, int amount) {
            this.bone = bone;
            this.altar = altar;
            this.amount = amount;
        }

        @Override
        public void startAnimationLoop(Player player) {
            Task task = new Task(2, player, true) {
                @Override
                protected void execute() {
                    player.performAnimation(ALTAR_OFFERING_ANIMATION);
                }
            };
            TaskManager.submit(task);
            getTasks().add(task);
        }

        @Override
        public void finishedCycle(Player player) {
            if (amount-- <= 0) {
                cancel(player);
            }
            altar.performGraphic(ALTAR_OFFERING_GRAPHIC);
            player.getInventory().delete(bone.getBoneID(), 1);
            player.getSkillManager().addExperience(Skill.PRAYER, (int) (bone.getXp() * GILDED_ALTAR_EXPERIENCE_MULTIPLIER));
            player.getPacketSender().sendMessage("The gods are pleased with your offering.");
        }

        @Override
        public int cyclesRequired(Player player) {
            return 2;
        }

        @Override
        public boolean hasRequirements(Player player) {
            //Check if player has bones..
            if (!player.getInventory().contains(bone.getBoneID())) {
                return false;
            }
            //Check if we offered all bones..
            if (amount <= 0) {
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
            return true;
        }
    }
}
