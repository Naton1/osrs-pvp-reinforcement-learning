package com.elvarg.game.entity.impl.npc.impl.pestcontrol;

import com.elvarg.game.content.combat.method.CombatMethod;
import com.elvarg.game.content.combat.method.impl.npcs.pestcontrol.SplatterCombatMethod;
import com.elvarg.game.content.combat.method.impl.npcs.pestcontrol.TorcherCombatMethod;
import com.elvarg.game.entity.impl.npc.NPC;
import com.elvarg.game.model.Ids;
import com.elvarg.game.model.Location;

import static com.elvarg.util.NpcIdentifiers.*;

/**
 * @author Ynneh | 01/03/2023 - 08:37
 * <https://github.com/drhenny>
 */
@Ids({TORCHER, TORCHER_3, TORCHER_5, TORCHER_7, TORCHER_9, TORCHER_10})
public class TorcherNPC extends NPC {

    private static final CombatMethod COMBAT_METHOD = new TorcherCombatMethod();
    /**
     * Constructs a new npc.
     *
     * @param id       The npc id.
     * @param position The npc spawn (default) position.
     */
    public TorcherNPC(int id, Location position) {
        super(id, position);
    }

    @Override
    public CombatMethod getCombatMethod() {
        return COMBAT_METHOD;
    }
}
