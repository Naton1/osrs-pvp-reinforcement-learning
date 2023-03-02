package com.elvarg.game.entity.impl.npc.impl.pestcontrol;

import com.elvarg.game.content.combat.method.CombatMethod;
import com.elvarg.game.content.combat.method.impl.npcs.pestcontrol.RavagerCombatMethod;
import com.elvarg.game.entity.impl.npc.NPC;
import com.elvarg.game.model.Ids;
import com.elvarg.game.model.Location;

import static com.elvarg.util.NpcIdentifiers.*;

/**
 * @author Ynneh | 01/03/2023 - 08:27
 * <https://github.com/drhenny>
 */
@Ids({RAVAGER, RAVAGER_2, RAVAGER_3, RAVAGER_4, RAVAGER_5})
public class RavagerNPC extends NPC {

    private static final CombatMethod COMBAT_METHOD = new RavagerCombatMethod();

    /**
     * Constructs a new npc.
     *
     * @param id       The npc id.
     * @param position The npc spawn (default) position.
     */
    public RavagerNPC(int id, Location position) {
        super(id, position);
    }

    @Override
    public CombatMethod getCombatMethod() {
        return COMBAT_METHOD;
    }
}
