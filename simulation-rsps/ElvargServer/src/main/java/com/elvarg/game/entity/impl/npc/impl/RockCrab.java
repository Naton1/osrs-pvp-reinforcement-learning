package com.elvarg.game.entity.impl.npc.impl;

import com.elvarg.game.content.combat.method.CombatMethod;
import com.elvarg.game.content.combat.method.impl.npcs.RockCrabCombatMethod;
import com.elvarg.game.entity.impl.npc.NPC;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.Animation;
import com.elvarg.game.model.Ids;
import com.elvarg.game.model.Location;
import org.checkerframework.checker.units.qual.A;

import static com.elvarg.util.NpcIdentifiers.*;

@Ids({ROCKS, ROCKS_2})
public class RockCrab extends NPC {

    private static final CombatMethod COMBAT_METHOD = new RockCrabCombatMethod();

    public static final int[] ROCK_IDS = new int[] { ROCKS, ROCKS_2 };

    /**
     * Constructs a new npc.
     *
     * @param id       The npc id.
     * @param position
     */
    public RockCrab(int id, Location position) {
        super(id, position);
    }

    @Override
    public boolean isAggressiveTo(Player player) {
        // Rock crabs always attack players, regardless of combat level
        // Otherwise, there would be no way for Players over combat level 26 to attack them
        return true;
    }

    @Override
    public int aggressionDistance() {
        // Rock crabs only attack when Player is right beside them
        return 1;
    }

    @Override
    public CombatMethod getCombatMethod() {
        return COMBAT_METHOD;
    }

    /**
     * Gets the Rock Crab NPC ID from a given Rock npc id.
     *
     * @param rockNpcId
     * @return {int} transformId
     */
    public static int getTransformationId(int rockNpcId) {

        switch (rockNpcId) {
            // Rock is transforming into a Rock Crab
            case ROCKS -> {
                return ROCK_CRAB;
            }
            case ROCKS_2 -> {
                return ROCK_CRAB_2;
            }

            // Rock Crab is transforming back into a Rock
            case ROCK_CRAB -> {
                return ROCKS;
            }
            case ROCK_CRAB_2 -> {
                return ROCKS_2;
            }
        }

        return ROCK_CRAB;
    }
}
