package com.elvarg.game.entity.impl.npc.impl.pestcontrol;

import com.elvarg.game.content.combat.method.CombatMethod;
import com.elvarg.game.content.combat.method.impl.npcs.pestcontrol.SpinnerCombatMethod;
import com.elvarg.game.entity.impl.npc.NPC;
import com.elvarg.game.model.Ids;
import com.elvarg.game.model.Location;

import static com.elvarg.util.NpcIdentifiers.*;

/**
 * @author Ynneh | 01/03/2023 - 04:20
 * <https://github.com/drhenny>
 */
@Ids({SPINNER, SPINNER_2, SPINNER_3, SPINNER_4, SPINNER_5})
public class SpinnerNPC extends NPC {

    private static final CombatMethod COMBAT_METHOD = new SpinnerCombatMethod();
    /**
     * Constructs a new npc.
     *
     * @param id       The npc id.
     * @param position The npc spawn (default) position.
     */
    public SpinnerNPC(int id, Location position) {
        super(id, position);
    }

    @Override
    public CombatMethod getCombatMethod() {
        return COMBAT_METHOD;
    }
}
