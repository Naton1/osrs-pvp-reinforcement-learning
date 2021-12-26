package com.elvarg.game.content;

import com.elvarg.game.entity.impl.npc.NPC;
import com.elvarg.game.entity.impl.object.GameObject;
import com.elvarg.game.entity.impl.object.ObjectManager;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.Animation;
import com.elvarg.game.model.movement.MovementQueue;
import com.elvarg.game.task.TaskManager;
import com.elvarg.game.task.impl.TimedObjectSpawnTask;
import com.elvarg.util.Misc;

import java.util.Optional;

public class Gambling {

    /**
     * The item id of mithril seeds.
     * Used for planting flowers.
     */
    public static final int MITHRIL_SEEDS = 299;

    /**
     * Plants flowers using mithril seeds.
     *
     * @param player
     */
    public static void plantFlower(Player player) {
        if (!player.getClickDelay().elapsed(3000)) {
            return;
        }
        for (NPC npc : player.getLocalNpcs()) {
            if (npc != null && npc.getLocation().equals(player.getLocation())) {
                player.getPacketSender().sendMessage("You cannot plant a seed right here.");
                return;
            }
        }
        if (ObjectManager.exists(player.getLocation())) {
            player.getPacketSender().sendMessage("You cannot plant a seed right here.");
            return;
        }
        final FlowersData flowers = FlowersData.generate();
        final GameObject flowerObject = new GameObject(flowers.objectId, player.getLocation().clone(), 10, 0, player.getPrivateArea());

        //Stop skilling..
        player.getSkillManager().stopSkillable();

        player.getMovementQueue().reset();
        player.getInventory().delete(MITHRIL_SEEDS, 1);
        player.performAnimation(new Animation(827));
        player.getPacketSender().sendMessage("You plant the seed and suddenly some flowers appear..");
        MovementQueue.clippedStep(player);
        //Start a task which will spawn and then delete them after a period of time.
        TaskManager.submit(new TimedObjectSpawnTask(flowerObject, 60, Optional.empty()));
        player.setPositionToFace(flowerObject.getLocation());
        player.getClickDelay().reset();
    }

    public enum FlowersData {
        PASTEL_FLOWERS(2980, 2460),
        RED_FLOWERS(2981, 2462),
        BLUE_FLOWERS(2982, 2464),
        YELLOW_FLOWERS(2983, 2466),
        PURPLE_FLOWERS(2984, 2468),
        ORANGE_FLOWERS(2985, 2470),
        RAINBOW_FLOWERS(2986, 2472),

        WHITE_FLOWERS(2987, 2474),
        BLACK_FLOWERS(2988, 2476);

        public int objectId;
        public int itemId;
        FlowersData(int objectId, int itemId) {
            this.objectId = objectId;
            this.itemId = itemId;
        }

        public static FlowersData forObject(int object) {
            for (FlowersData data : FlowersData.values()) {
                if (data.objectId == object)
                    return data;
            }
            return null;
        }

        public static FlowersData generate() {
            double RANDOM = (java.lang.Math.random() * 100);
            if (RANDOM >= 1) {
                return values()[Misc.getRandom(6)];
            } else {
                return Misc.getRandom(3) == 1 ? WHITE_FLOWERS : BLACK_FLOWERS;
            }
        }
    }
}
