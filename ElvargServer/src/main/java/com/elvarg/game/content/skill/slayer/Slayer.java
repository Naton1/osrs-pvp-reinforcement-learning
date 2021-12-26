package com.elvarg.game.content.skill.slayer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.elvarg.game.entity.impl.npc.NPC;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.Skill;
import com.elvarg.util.Misc;

public class Slayer {

    /**
     * Assigns a {@link SlayerTask} to the given {@link Player} based on their
     * current combat level.
     * 
     * @param player
     */
    public static boolean assign(Player player) {
        SlayerMaster master = SlayerMaster.TURAEL;
       /* for (SlayerMaster m : SlayerMaster.MASTERS) {
            if (!m.canAssign(player)) {
                continue;
            }
            master = m;
        }*/
        return assign(player, master);
    }

    private static boolean assign(Player player, SlayerMaster master) {
        if (player.getSlayerTask() != null) {
            player.getPacketSender().sendInterfaceRemoval().sendMessage("You already have a Slayer task.");
            return false;
        }

        // Get the tasks we can assign
        List<SlayerTask> possibleTasks = new ArrayList<>();
        int totalWeight = 0;
        for (SlayerTask task : SlayerTask.VALUES) {
            // Check if player has unlocked this task
            if (!task.isUnlocked(player)) {
                continue;
            }

            // Check if player has the slayer level required for this task
            if (player.getSkillManager().getMaxLevel(Skill.SLAYER) < task.getSlayerLevel()) {
                continue;
            }

            // Check if this master is able to give out the task
            boolean correctMaster = false;
            for (SlayerMaster assignedBy : task.getMasters()) {
                if (master == assignedBy) {
                    correctMaster = true;
                    break;
                }
            }
            if (!correctMaster) {
                continue;
            }

            possibleTasks.add(task);
            totalWeight += task.getWeight();
        }
        if (possibleTasks.isEmpty()) {
            player.getPacketSender().sendInterfaceRemoval().sendMessage("Nieve was unable to give you a Slayer task. Please try again later.");
            return false;
        }

        // Shuffle them and choose a random one based on the weighting system
        Collections.shuffle(possibleTasks);
        SlayerTask toAssign = null;
        for (SlayerTask task : possibleTasks) {
            if (Misc.getRandom(totalWeight) <= task.getWeight()) {
                toAssign = task;
                break;
            }
        }
        if (toAssign == null) {
            toAssign = possibleTasks.get(0);
        }

        // Assign the new task
        player.setSlayerTask(new ActiveSlayerTask(master, toAssign, Misc.inclusive(toAssign.getMinimumAmount(), toAssign.getMaximumAmount())));
        return true;
    }

    public static void killed(Player player, NPC npc) {
        if (player.getSlayerTask() == null) {
            return;
        }
        if (npc.getDefinition() == null || npc.getDefinition().getName() == null) {
            return;
        }
        
        boolean isTask = false;
        final String killedNpcName = npc.getDefinition().getName().toLowerCase();
        for (String npcName : player.getSlayerTask().getTask().getNpcNames()) {
            if (npcName.equals(killedNpcName)) {
                isTask = true;
                break;
            }
        }
        if (!isTask) {
            return;
        }
        
        // Add experience and decrease task count
        player.getSkillManager().addExperience(Skill.SLAYER, npc.getDefinition().getHitpoints());
        player.getSlayerTask().setRemaining(player.getSlayerTask().getRemaining() - 1);

        // Handle completion of task
        if (player.getSlayerTask().getRemaining() == 0) {
            int rewardPoints = player.getSlayerTask().getMaster().getBasePoints();

            // Increase consecutive tasks
            player.setConsecutiveTasks(player.getConsecutiveTasks() + 1);

            // Check for bonus points after completing consecutive tasks
            for (int[] consecutive : player.getSlayerTask().getMaster().getConsecutiveTaskPoints()) {
                int requiredTasks = consecutive[0];
                int bonusPoints = consecutive[1];
                if (player.getConsecutiveTasks() % requiredTasks == 0) {
                    rewardPoints = bonusPoints;
                    break;
                }
            }

            // Increase points
            player.setSlayerPoints(player.getSlayerPoints() + rewardPoints);
            player.getPacketSender().sendMessage("You have succesfully completed @dre@" + player.getConsecutiveTasks() + "@bla@ slayer tasks in a row.");
            player.getPacketSender().sendMessage("You earned @dre@" + rewardPoints + "@bla@ Slayer " + (rewardPoints == 1 ? "point" : "points") + ", your new total is now @dre@" + player.getSlayerPoints() + ".");
            
            // Reset task
            player.setSlayerTask(null);
        }
    }
}
