package com.elvarg.game.model.movement;

import com.elvarg.game.entity.impl.player.Player;

/**
 * This class handles the execution of an action once we're close enough to the
 * given position. Used for things such as clicking on entities.
 */

public abstract class WalkToAction {

	private final Player player;

	public WalkToAction(Player player) {
	    this.player = player;
	}
	
    public abstract boolean inDistance();
    public abstract void execute();
    
    public void process() {
        if (!player.isRegistered()) {
            reset();
            return;
        }
        if (player.busy() || player.getMovementQueue().isMovementBlocked()) {
            reset();
            return;
        }
        
        if (inDistance()) {
            execute();
            reset();
        }
    }
    
    private void reset() {
        player.setWalkToTask(null);
        player.setFollowing(null);
        player.setMobileInteraction(null);
    }
}
