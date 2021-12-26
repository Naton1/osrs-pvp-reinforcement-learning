package com.elvarg.game.task.impl;

import com.elvarg.game.entity.impl.grounditem.ItemOnGround;
import com.elvarg.game.entity.impl.grounditem.ItemOnGroundManager;
import com.elvarg.game.task.Task;

/**
 * A {@link Task} implementation which handles the respawn of an
 * {@link ItemOnGround}.
 *
 * @author Professor Oak
 */
public class GroundItemRespawnTask extends Task {

    /**
     * The {@link ItemOnGround} which is going to respawn.
     */
    private final ItemOnGround item;

    public GroundItemRespawnTask(ItemOnGround item, int ticks) {
        super(ticks);
        this.item = item;
    }

    @Override
    public void execute() {
        // Register the new entity..
        ItemOnGroundManager.register(item.clone());

        // Stop the task
        stop();
    }
}
