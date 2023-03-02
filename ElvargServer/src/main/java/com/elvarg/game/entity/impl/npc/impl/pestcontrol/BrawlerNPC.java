package com.elvarg.game.entity.impl.npc.impl.pestcontrol;

import com.elvarg.game.content.combat.method.CombatMethod;
import com.elvarg.game.content.combat.method.impl.npcs.pestcontrol.BrawlerCombatMethod;
import com.elvarg.game.entity.impl.npc.NPC;
import com.elvarg.game.model.Ids;
import com.elvarg.game.model.Location;

import static com.elvarg.util.NpcIdentifiers.*;

/**
 * @author Ynneh | 01/03/2023 - 07:53
 * <https://github.com/drhenny>
 */
@Ids({BRAWLER, BRAWLER_2, BRAWLER_3, BRAWLER_4, BRAWLER_5})
public class BrawlerNPC extends NPC {

    private static final CombatMethod COMBAT_METHOD = new BrawlerCombatMethod();
    /**
     * Constructs a new npc.
     *
     * @param id       The npc id.
     * @param position The npc spawn (default) position.
     */
    public BrawlerNPC(int id, Location position) {
        super(id, position);
    }

    @Override
    public CombatMethod getCombatMethod() {
        return COMBAT_METHOD;
    }
}
