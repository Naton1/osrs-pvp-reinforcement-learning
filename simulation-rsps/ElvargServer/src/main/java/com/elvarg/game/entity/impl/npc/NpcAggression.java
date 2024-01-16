package com.elvarg.game.entity.impl.npc;

import com.elvarg.game.content.combat.CombatFactory;
import com.elvarg.game.content.combat.method.CombatMethod;
import com.elvarg.game.definition.NpcDefinition;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.areas.AreaManager;
import com.elvarg.game.model.areas.impl.PrivateArea;
import com.elvarg.game.model.areas.impl.WildernessArea;
import com.elvarg.game.model.container.impl.Equipment;
import com.elvarg.util.Misc;

import java.util.Collection;
import java.util.List;

/**
 * Handles the behavior of aggressive {@link Npc}s around players within the
 * <code>NPC_TARGET_DISTANCE</code> radius.
 *
 * @author lare96
 */
public final class NpcAggression {

    /**
     * Time that has to be spent in a region before npcs stop acting aggressive
     * toward a specific player.
     */
    public static final int NPC_TOLERANCE_SECONDS = 600; // 10 mins (Accurate to OSRS)

    public static void process(Player player) {
        // Make sure we can attack the player
        if (CombatFactory.inCombat(player) && !AreaManager.inMulti(player)) {
            return;
        }

        runAggression(player, player.getLocalNpcs());

        if (player.getArea() instanceof PrivateArea) {
            runAggression(player, (player.getArea()).getNpcs());
        }
    }

    private static void runAggression(Player player, Collection<NPC> npcs) {
        for (NPC npc : npcs) {

            if (npc == null) {
                continue;
            }

            // Get the NPC's current definition (taking into account possible transformation)
            NpcDefinition npcDefinition = npc.getCurrentDefinition();
            if (npcDefinition == null || npc.getHitpoints() <= 0
                    || !npcDefinition.isAggressive()
                    || npc.getPrivateArea() != player.getPrivateArea()) {
                // Make sure the npc is available to attack the player.
                continue;
            }

            if (npcDefinition.buildsAggressionTolerance() && player.getAggressionTolerance().finished()
                    && (player.getArea() == null || !player.getArea().overridesNpcAggressionTolerance(player, npc.getId()))) {
                // If Player has obtained tolerance to this NPC, don't be aggressive.
                return;
            }

            if (CombatFactory.inCombat(npc)) {
                if (AreaManager.inMulti(npc) && player.getLocalPlayers().size() > 0) {
                    // Randomly attack different players if they're a team.
                    if (Misc.getRandom(9) <= 2) {
                        // Get a random player from the player's local players list.
                        Player randomPlayer = player.getLocalPlayers().get(Misc.getRandom(player.getLocalPlayers().size() - 1));

                        // Attack the new player if they're a valid target.
                        if (CombatFactory.validTarget(npc, randomPlayer)) {
                            npc.getCombat().attack(randomPlayer);
                            break;
                        }
                    }
                }

                // Don't process tolerance if NPC is already in combat.
                continue;
            }

            if (!npc.isAggressiveTo(player)) {
                // Ensure the NPC can be aggressive to this player.
                continue;
            }

            // Make sure we have the proper distance to attack the player.
            final int distanceToPlayer = npc.getSpawnPosition().getDistance(player.getLocation());

            // Get the npc's combat method
            final CombatMethod method = CombatFactory.getMethod(npc);

            // Get the max distance this npc can attack from.
            // We should always attack if we're at least 3 tiles from the player.
            final int aggressionDistance = npc.aggressionDistance();

            if (distanceToPlayer < npcDefinition.getCombatFollowDistance() && distanceToPlayer <= aggressionDistance) {
                if (CombatFactory.canAttack(npc, method, player) == CombatFactory.CanAttackResponse.CAN_ATTACK) {
                    npc.getCombat().attack(player);
                    break;
                }
            }
        }
    }

}
