package com.elvarg.game.entity.impl.npc.impl;

import com.elvarg.game.content.combat.method.CombatMethod;
import com.elvarg.game.content.combat.method.impl.npcs.KingBlackDragonMethod;
import com.elvarg.game.entity.impl.npc.NPC;
import com.elvarg.game.model.Ids;
import com.elvarg.game.model.Location;

import static com.elvarg.util.NpcIdentifiers.*;

@Ids({KING_BLACK_DRAGON, KING_BLACK_DRAGON_2, KING_BLACK_DRAGON_3})
public class KingBlackDragon extends NPC {

    private static final CombatMethod COMBAT_METHOD = new KingBlackDragonMethod();

    /**
     * Constructs a new npc.
     *
     * @param id       The npc id.
     * @param position
     */
    public KingBlackDragon(int id, Location position) {
        super(id, position);
    }

    @Override
    public CombatMethod getCombatMethod() {
        return COMBAT_METHOD;
    }
}
