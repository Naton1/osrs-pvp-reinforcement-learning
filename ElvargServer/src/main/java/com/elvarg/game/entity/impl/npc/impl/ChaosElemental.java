package com.elvarg.game.entity.impl.npc.impl;

import com.elvarg.game.content.combat.method.CombatMethod;
import com.elvarg.game.content.combat.method.impl.npcs.ChaosElementalCombatMethod;
import com.elvarg.game.entity.impl.npc.NPC;
import com.elvarg.game.model.Ids;
import com.elvarg.game.model.Location;

import static com.elvarg.util.NpcIdentifiers.*;

@Ids({CHAOS_ELEMENTAL})
public class ChaosElemental extends NPC {

    private static final CombatMethod COMBAT_METHOD = new ChaosElementalCombatMethod();

    /**
     * Constructs a new npc.
     *
     * @param id       The npc id.
     * @param position
     */
    public ChaosElemental(int id, Location position) {
        super(id, position);
    }

    @Override
    public CombatMethod getCombatMethod() {
        return COMBAT_METHOD;
    }
}
