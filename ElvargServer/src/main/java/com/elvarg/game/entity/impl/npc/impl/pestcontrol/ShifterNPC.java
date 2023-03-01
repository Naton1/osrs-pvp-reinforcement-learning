package com.elvarg.game.entity.impl.npc.impl.pestcontrol;

import com.elvarg.game.content.combat.method.CombatMethod;
import com.elvarg.game.content.combat.method.impl.npcs.CrazyArchaeologistCombatMethod;
import com.elvarg.game.content.combat.method.impl.npcs.pestcontrol.PestControlPortalCombatMethod;
import com.elvarg.game.content.combat.method.impl.npcs.pestcontrol.ShifterCombatMethod;
import com.elvarg.game.content.combat.method.impl.npcs.pestcontrol.SplatterCombatMethod;
import com.elvarg.game.entity.impl.npc.NPC;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.Ids;
import com.elvarg.game.model.Location;

import static com.elvarg.util.NpcIdentifiers.*;
import static com.elvarg.util.NpcIdentifiers.PORTAL_12;

/**
 * @author Ynneh | 27/02/2023 - 07:52
 * <https://github.com/drhenny>
 */
@Ids({1694, 1696, 1698, 1700, 1702})
public class ShifterNPC extends NPC {

    private static final CombatMethod COMBAT_METHOD = new ShifterCombatMethod();

    /**
     * Constructs a new npc.
     *
     * @param id       The npc id.
     * @param position The npc spawn (default) position.
     */
    public ShifterNPC(int id, Location position) {
        super(id, position);
    }

    @Override
    public boolean isAggressiveTo(Player player) {
        return true;
    }

    @Override
    public CombatMethod getCombatMethod() {
        return COMBAT_METHOD;
    }


}
