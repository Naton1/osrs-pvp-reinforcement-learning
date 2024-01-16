package com.elvarg.game.entity.impl.npc.impl.pestcontrol;

import com.elvarg.game.content.combat.method.CombatMethod;
import com.elvarg.game.content.combat.method.impl.npcs.pestcontrol.PestControlPortalCombatMethod;
import com.elvarg.game.entity.impl.Mobile;
import com.elvarg.game.entity.impl.npc.NPC;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.Ids;
import com.elvarg.game.model.Location;

import static com.elvarg.util.NpcIdentifiers.*;

/**
 * @author Ynneh | 24/02/2023 - 11:00
 * <https://github.com/drhenny>
 */
@Ids({PORTAL_13, PORTAL_14, PORTAL_15, PORTAL_16, PORTAL_9, PORTAL_10, PORTAL_11, PORTAL_12})
public class PestControlPortalNPC extends NPC {

    private static final CombatMethod COMBAT_METHOD = new PestControlPortalCombatMethod();

    public PestControlPortalNPC(int id, Location position) {
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
