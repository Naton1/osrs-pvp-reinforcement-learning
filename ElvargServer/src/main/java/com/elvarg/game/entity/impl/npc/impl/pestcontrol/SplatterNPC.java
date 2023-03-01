package com.elvarg.game.entity.impl.npc.impl.pestcontrol;

import com.elvarg.game.content.combat.method.CombatMethod;
import com.elvarg.game.content.combat.method.impl.npcs.pestcontrol.PestControlPortalCombatMethod;
import com.elvarg.game.content.combat.method.impl.npcs.pestcontrol.SplatterCombatMethod;
import com.elvarg.game.entity.impl.npc.NPC;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.Ids;
import com.elvarg.game.model.Location;

/**
 * @author Ynneh | 27/02/2023 - 07:51
 * <https://github.com/drhenny>
 */
@Ids({1689, 1690, 1691, 1692, 1693})
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
