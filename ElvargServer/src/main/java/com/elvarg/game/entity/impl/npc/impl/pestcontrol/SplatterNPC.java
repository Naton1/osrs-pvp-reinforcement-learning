package com.elvarg.game.entity.impl.npc.impl.pestcontrol;

import com.elvarg.game.content.combat.method.CombatMethod;
import com.elvarg.game.content.combat.method.impl.npcs.pestcontrol.SplatterCombatMethod;
import com.elvarg.game.entity.impl.npc.NPC;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.Ids;
import com.elvarg.game.model.Location;

import static com.elvarg.util.NpcIdentifiers.*;

/**
 * @author Ynneh | 27/02/2023 - 07:51
 * <https://github.com/drhenny>
 */
@Ids({SPLATTER, SPLATTER_2, SPLATTER_3, SPLATTER_4, SPLATTER_5})
public class SplatterNPC extends NPC {

    private static final CombatMethod COMBAT_METHOD = new SplatterCombatMethod();

    /**
     * Constructs a new npc.
     *
     * @param id       The npc id.
     * @param position The npc spawn (default) position.
     */
    public SplatterNPC(int id, Location position) {
        super(id, position);
    }

    @Override
    public boolean isAggressiveTo(Player player) {
        return false;
    }

    @Override
    public CombatMethod getCombatMethod() {
        return COMBAT_METHOD;
    }
}
