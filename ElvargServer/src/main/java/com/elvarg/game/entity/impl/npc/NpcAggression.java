package com.elvarg.game.entity.impl.npc;

import com.elvarg.game.content.combat.CombatFactory;
import com.elvarg.game.content.combat.method.CombatMethod;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.areas.AreaManager;
import com.elvarg.game.model.areas.impl.PrivateArea;
import com.elvarg.game.model.areas.impl.WildernessArea;
import com.elvarg.game.model.container.impl.Equipment;
import com.elvarg.util.Misc;

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
    public static final int NPC_TOLERANCE_SECONDS = 300; // 5 mins

    public static void process(Player player) {
        // Make sure we can attack the player
        if (CombatFactory.inCombat(player) && !AreaManager.inMulti(player)) {
            return;
        }

        runAggression(player, player.getLocalNpcs());

        if (player.getArea() instanceof PrivateArea) {
            runAggression(player, ((PrivateArea) player.getArea()).getNpcs());
        }
    }

    private static void runAggression(Player player, List<NPC> npcs) {
        // Loop through all of the aggressive npcs.
        for (NPC npc : npcs) {

            // Make sure the npc is available to attack the player.
            if (npc == null || npc.getDefinition() == null || npc.getHitpoints() <= 0
                    || !npc.getDefinition().isAggressive()
                    || npc.getPrivateArea() != player.getPrivateArea()) {
                continue;
            }

            // Don't handle aggression if we've been in the region for quite some time.
            if (player.getAggressionTolerance().finished() && (player.getArea() == null
                    || !player.getArea().overridesNpcAggressionTolerance(player, npc.getId()))) {
                return;
            }


            // Randomly attack different players if they're a team.
            if (CombatFactory.inCombat(npc)) {
                if (AreaManager.inMulti(npc)) {
                    if (Misc.getRandom(9) <= 2) {
                        if (player.getLocalPlayers().size() > 0) {

                            // Get a random player from the player's local players list.
                            Player randomPlayer = player.getLocalPlayers().get(Misc.getRandom(player.getLocalPlayers().size() - 1));

                            // Attack the new player if they're a valid target.
                            if (CombatFactory.validTarget(npc, randomPlayer)) {
                                npc.getCombat().attack(randomPlayer);
                                break;
                            }
                        }
                    }
                }
                continue;
            }

            // Are we processing bandits?
            boolean bandits = npc.getId() == 690;
            if (!bandits) {
                // Unless in Wilderness, don't be aggressive to players with twice our combat
                // level.
                if (player.getSkillManager().getCombatLevel() > (npc.getDefinition().getCombatLevel() * 2)
                        && !(player.getArea() instanceof WildernessArea)) {
                    continue;
                }
            }

            // Make sure we have the proper distance to attack the player.
            final int distanceToPlayer = npc.getSpawnPosition().getDistance(player.getLocation());

            // Get the npc's combat method
            final CombatMethod method = CombatFactory.getMethod(npc);

            // Get the max distance this npc can attack from.
            // We should always attack if we're at least 3 tiles from the player.
            final int aggressionDistance = npc.aggressionDistance() < 3 ? 3 : npc.aggressionDistance();

            if (distanceToPlayer < npc.getDefinition().getCombatFollowDistance() && distanceToPlayer <= aggressionDistance) {

                // Make sure that we can actually attack the player.
                if (CombatFactory.canAttack(npc, method, player)) {

                    // Bandits
                    if (bandits) {
                        int zammy = Equipment.getItemCount(player, "Zamorak", true);
                        int sara = Equipment.getItemCount(player, "Saradomin", true);
                        if (!(zammy > 0 || sara > 0)) {
                            continue;
                        }
                        if (Misc.getRandom(2) == 1) {
                            String s = zammy > 0 ? "Zamorak" : "Saradomin";
                            if (Misc.getRandom(2) == 1) {
                                npc.forceChat("Filthy " + s + " follower scum!");
                            } else {
                                npc.forceChat("" + s + " scum! You will regret coming here!");
                            }
                        }
                    }

                    // Attack the player!
                    npc.getCombat().attack(player);
                    break;
                }
            }
        }
    }

}
