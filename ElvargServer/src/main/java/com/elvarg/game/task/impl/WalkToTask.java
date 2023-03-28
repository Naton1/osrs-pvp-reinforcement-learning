package com.elvarg.game.task.impl;

import com.elvarg.game.definition.ObjectDefinition;
import com.elvarg.game.entity.Entity;
import com.elvarg.game.entity.impl.Mobile;
import com.elvarg.game.entity.impl.grounditem.ItemOnGround;
import com.elvarg.game.entity.impl.npc.NPC;
import com.elvarg.game.entity.impl.object.GameObject;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.entity.impl.playerbot.interaction.MovementInteraction;
import com.elvarg.game.model.Action;
import com.elvarg.game.model.movement.MovementQueue;
import com.elvarg.game.model.movement.path.PathFinder;
import com.elvarg.game.task.Task;
import com.elvarg.game.task.TaskManager;

public class WalkToTask extends Task {

    public static final int NPC_INTERACT_RADIUS = 2;

    // The MovementQueue this task is related to
    MovementQueue movement;

    // The player which this task belongs to
    Player player;

    // The entity we are walking to
    Entity entity;

    // The runnable which will execute after pathing completes
    Runnable action;

    // The initial location of the entity
    int destX, destY;

    // The destination calculated by the PathFinder
    int finalDestinationX, finalDestinationY;

    // Whether the player has reached their destination
    boolean reachedDestination = false;

    public static void submit(Player player, Entity entity, Runnable action) {
        TaskManager.submit(new WalkToTask(player, entity, action));
    }

    public WalkToTask(Player player, Entity entity, Runnable action) {
        super(0, player.getIndex(), true);

        this.player = player;
        this.movement = player.getMovementQueue();
        this.entity = entity;
        this.action = action;
        this.destX = entity.getLocation().getX();
        this.destY = entity.getLocation().getY();

        // Always reset the movement queue when the player intents to walk towards an entity
        movement.reset();

        if (player.getLocation().getDistance(entity.getLocation()) == 0 && action != null) {
            // If player is already standing on top of the entity, run the action now
            action.run();
            return;
        }

        MovementQueue.Mobility mobility = movement.getMobility();
        if (!mobility.canMove()) {
            // Player can not currently move
            mobility.sendMessage(movement.getPlayer());
            return;
        }

        if (!movement.checkDestination(entity.getLocation())) {
            // Destination is not valid
            return;
        }

        movement.walkToReset();
        calculateWalkRoute(entity);

        this.finalDestinationX = movement.pathX;
        this.finalDestinationY = movement.pathY;
    }

    @Override
    protected void execute() {
        player.setPositionToFace(entity.getLocation());
        if (entity instanceof Mobile) {
            player.setMobileInteraction((Mobile) entity);
        }

        if (reachedDestination || withinInteractionDistance()) {
            // Executes the runnable and stops the task. Player will still path to the destination.
            if (action != null) {
                action.run();
            }
            stop();
            return;
        }

        // If the target has moved, update the movement queue
        checkForMovement();

        if (!movement.points().isEmpty()) {
            // Movement hasn't finished yet, skip for this cycle
            return;
        }

        if (!player.getMovementQueue().hasRoute() || player.getLocation().getX() != finalDestinationX || player.getLocation().getY() != finalDestinationY) {
            // No route is possible or queue is empty and not at destination
            player.getPacketSender().sendMessage("I can't reach that!");
            movement.reset();
            stop();
            return;
        }

        // Execute the runnable on the next game tick as per OSRS
        reachedDestination = true;
    }

    /**
     * Invokes the PathFinder to calculate the best route to the entity.
     *
     * @param entity The entity to walk to
     */
    private void calculateWalkRoute(Entity entity) {
        if (entity instanceof Mobile /* Players and NPCs */) {
            PathFinder.calculateEntityRoute(player, destX, destY);
        } else if (entity instanceof GameObject) {
            PathFinder.calculateObjectRoute(player, (GameObject) entity);
        } else if (entity instanceof ItemOnGround) {
            PathFinder.calculateWalkRoute(player, destX, destY);
        }
    }

    /**
     * Determines whether the player is within interaction distance of the entity.
     *
     * @return
     */
    private boolean withinInteractionDistance() {
        if (entity instanceof NPC && movement.points().size() <= NPC_INTERACT_RADIUS
                && player.getLocation().getDistance(entity.getLocation()) <= NPC_INTERACT_RADIUS) {
            // NPC interactions start as soon as Player is within 2 tiles
            return true;
        }

        return false;
    }

    private void checkForMovement() {
        if (!(entity instanceof Mobile)) {
            // Only Mobile's can move
            return;
        }

        if (destX != entity.getLocation().getX() || destY != entity.getLocation().getY()) {
            // Mobile has moved, update the entity path to the updated location
            movement.reset();
            destX = entity.getLocation().getX();
            destY = entity.getLocation().getY();
            PathFinder.calculateEntityRoute(player, destX, destX);
        }
    }
}
