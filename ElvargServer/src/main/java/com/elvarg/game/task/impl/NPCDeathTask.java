package com.elvarg.game.task.impl;

import java.util.Optional;

import com.elvarg.game.World;
import com.elvarg.game.content.combat.method.CombatMethod;
import com.elvarg.game.content.skill.slayer.Slayer;
import com.elvarg.game.entity.impl.npc.NPC;
import com.elvarg.game.entity.impl.npc.NPCDropGenerator;
import com.elvarg.game.entity.impl.npc.impl.Barricades;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.Animation;
import com.elvarg.game.model.Priority;
import com.elvarg.game.task.Task;
import com.elvarg.game.task.TaskManager;

/**
 * Represents an npc's death task, which handles everything an npc does before
 * and after their death animation (including it), such as dropping their drop
 * table items.
 *
 * @author relex lawl
 */

public class NPCDeathTask extends Task {

    /**
     * The npc setting off the death task.
     */
    private final NPC npc;
    /**
     * The amount of ticks on the task.
     */
    private int ticks;
    /**
     * The player who killed the NPC
     */
    private Optional<Player> killer = Optional.empty();

    /**
     * The NPCDeathTask constructor.
     *
     * @param npc The npc being killed.
     */
    public NPCDeathTask(NPC npc) {
        super(2);
        this.npc = npc;
        this.ticks = 1;
    }

    @Override
    public void execute() {
        try {
            final CombatMethod script = npc.getCombatMethod();
            switch (ticks) {
                case 1:
                    // Reset and disable movement queue..
                    npc.getMovementQueue().setBlockMovement(true).reset();

                    // Get the {@link Player} who killed us..
                    killer = npc.getCombat().getKiller(true);

                    // Start death animation..
                    npc.performAnimation(new Animation(npc.getCurrentDefinition().getDeathAnim(), Priority.HIGH));

                    if (script != null) {
                        script.onDeath(npc, killer);
                    }
                    // Reset combat..
                    npc.getCombat().reset();

                    // Reset interacting entity..
                    npc.setMobileInteraction(null);
                    break;
                case 0:
                    if (killer.isPresent()) {
                        Player player = killer.get();

                        if (player.getArea() != null) {
                            player.getArea().defeated(player, npc);
                        }
                        
                        // Slayer
                        Slayer.killed(player, npc);

                        // Drop loot for the killer..
                        NPCDropGenerator.start(player, npc);
                    }
                    stop();
                    break;
            }
            ticks--;
        } catch (Exception e) {
            e.printStackTrace();
            stop();
        }
    }

    @Override
    public void stop() {
        // Stop event.
    	super.stop();

        // Remove from area..
        if (npc.getArea() != null) {
            npc.getArea().leave(npc, false);
            npc.getArea().postLeave(npc, false);
            npc.setArea(null);
        }

        // Flag that we are no longer dying.
        npc.setDying(false);

        // Reset NPC transformation
        npc.setNpcTransformationId(-1);

        // Handle respawn..
        if (npc.getDefinition().getRespawn() > 0) {
            TaskManager.submit(new NPCRespawnTask(npc, npc.getDefinition().getRespawn()));
        }

        if (npc.isBarricade()) {
            Barricades.checkTile(npc.getLocation());
        }

        // Add us to the global remove list.
        World.getRemoveNPCQueue().add(npc);
    }
}