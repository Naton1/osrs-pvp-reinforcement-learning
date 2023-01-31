package com.elvarg.game.entity.impl.npc.impl;

import com.elvarg.game.content.combat.method.CombatMethod;
import com.elvarg.game.content.combat.method.impl.npcs.CrazyArchaeologistCombatMethod;
import com.elvarg.game.entity.impl.npc.NPC;
import com.elvarg.game.model.Ids;
import com.elvarg.game.model.Location;

import static com.elvarg.util.NpcIdentifiers.*;

@Ids({CRAZY_ARCHAEOLOGIST})
public class CrazyArchaeologist extends NPC {

    private static final CombatMethod COMBAT_METHOD = new CrazyArchaeologistCombatMethod();

    /**
     * Constructs a new npc.
     *
     * @param id       The npc id.
     * @param position
     */
    public CrazyArchaeologist(int id, Location position) {
        super(id, position);
    }

    @Override
    public CombatMethod getCombatMethod() {
        return COMBAT_METHOD;
    }
}