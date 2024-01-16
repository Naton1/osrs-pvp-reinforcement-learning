package com.elvarg.game.task.impl;

import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.ForceMovement;
import com.elvarg.game.model.Location;
import com.elvarg.game.task.Task;

/**
 * A {@link Task} implementation that handles forced movement.
 * An example of forced movement is the Wilderness ditch.
 *
 * @author Professor Oak
 */
public class ForceMovementTask extends Task {

    private Player player;
    private Location end;
    private Location start;

    public ForceMovementTask(Player player, int delay, ForceMovement forceM) {
        super(delay, player, (delay == 0 ? true : false));
        this.player = player;
        this.start = forceM.getStart().clone();
        this.end = forceM.getEnd().clone();

        //Reset combat
        player.getCombat().reset();

        //Reset movement queue
        player.getMovementQueue().reset();

        //Playerupdating
        player.setForceMovement(forceM);
    }

    @Override
    protected void execute() {
        int x = start.getX() + end.getX();
        int y = start.getY() + end.getY();
        player.moveTo(new Location(x, y, player.getLocation().getZ()));
        player.setForceMovement(null);
        stop();
    }
}
