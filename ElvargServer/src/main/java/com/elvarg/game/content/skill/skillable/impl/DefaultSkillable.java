package com.elvarg.game.content.skill.skillable.impl;

import com.elvarg.game.content.skill.skillable.Skillable;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.Animation;
import com.elvarg.game.task.Task;
import com.elvarg.game.task.TaskManager;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Represents a default skillable.
 * A "default" skill is where the player simply animates
 * until a set amount of cycles have passed, and then
 * is rewarded with items.
 *
 * @author Professor Oak
 */
public abstract class DefaultSkillable implements Skillable {

    /**
     * The {@link Task}s which is used to process
     * the skill.
     */
    private List<Task> tasks = new ArrayList<Task>();

    @Override
    public void start(Player player) {
        // Start animation loop..
        startAnimationLoop(player);

        // Start main process task..
        Task task = new Task(1, player, true) {
            int cycle = 0;

            @Override
            protected void execute() {
                // Make sure we still have the requirements to keep skilling..
                if (loopRequirements()) {
                    if (!hasRequirements(player)) {
                        cancel(player);
                        return;
                    }
                }

                // Every cycle, call the abstract method..
                onCycle(player);

                // Sequence the skill, reward players
                // with items once the right amount
                // of cycles have passed.
                if (cycle++ >= cyclesRequired(player)) {
                    finishedCycle(player);
                    cycle = 0;
                }
            }
        };

        // Submit it..
        TaskManager.submit(task);

        // Add to our list of tasks..
        tasks.add(task);
    }

    @Override
    public void cancel(Player player) {
        // Stop all tasks..
        Iterator<Task> i = tasks.iterator();
        while (i.hasNext()) {
            Task task = i.next();
            task.stop();
            i.remove();
        }

        // Reset animation..
        player.performAnimation(Animation.DEFAULT_RESET_ANIMATION);
    }

    @Override
    public boolean hasRequirements(Player player) {
        // Check inventory slots..
        if (!allowFullInventory()) {
            if (player.getInventory().getFreeSlots() == 0) {
                player.getInventory().full();
                return false;
            }
        }

        // Check if busy..
        if (player.busy()) {
            return false;
        }

        return true;
    }

    @Override
    public void onCycle(Player player) {
    }

    public List<Task> getTasks() {
        return tasks;
    }

    public abstract boolean loopRequirements();

    public abstract boolean allowFullInventory();
}
