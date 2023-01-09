package com.elvarg.game.entity.impl.npc.impl;

import com.elvarg.game.content.combat.magic.CombatSpells;
import com.elvarg.game.content.combat.method.CombatMethod;
import com.elvarg.game.content.combat.ranged.RangedData;
import com.elvarg.game.entity.impl.npc.NPC;
import com.elvarg.game.model.Ids;
import com.elvarg.game.model.Location;

import static com.elvarg.game.content.combat.CombatFactory.MAGIC_COMBAT;
import static com.elvarg.util.NpcIdentifiers.ELDER_CHAOS_DRUID;

@Ids({ELDER_CHAOS_DRUID})
public class ElderChaosDruid extends NPC {

    /**
     * Constructs a new npc.
     *
     * @param id       The npc id.
     * @param position
     */
    public ElderChaosDruid(int id, Location position) {
        super(id, position);

        this.getCombat().setAutocastSpell(CombatSpells.WIND_WAVE.getSpell());
    }

    @Override
    public CombatMethod getCombatMethod() {
        return MAGIC_COMBAT;
    }
}