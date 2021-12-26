package com.elvarg.game.content.skill.skillable;

import com.elvarg.game.entity.impl.player.Player;

/**
 * Acts as an interface for a skill which can be
 * trained.
 *
 * @author Professor Oak
 */
public interface Skillable {

    /**
     * Starts the skill.
     *
     * @param player
     */
    public abstract void start(Player player);

    /**
     * Cancels the skill.
     *
     * @param player
     */
    public abstract void cancel(Player player);

    /**
     * Checks if the player has the requirements to start this skill.
     *
     * @param player
     * @return
     */
    public abstract boolean hasRequirements(Player player);

    /**
     * Handles the skill's animation loops.
     *
     * @param player
     */
    public abstract void startAnimationLoop(Player player);

    /**
     * The cycles required for the skill to call {@code reward}. Used to determine
     * how long it takes for this skill to execute before the player receives
     * experience/rewards.
     */
    public abstract int cyclesRequired(Player player);

    /**
     * This method is called on every cycle.
     */
    public abstract void onCycle(Player player);

    /**
     * Once the amount of cycles has hit {@code cyclesRequired}, this method will be
     * called. It should be used for rewarding the player with items/experience
     * related to the skill
     */
    public abstract void finishedCycle(Player player);
}
