package com.elvarg.game.entity.impl.npc.impl;

import com.elvarg.game.content.combat.method.CombatMethod;
import com.elvarg.game.content.combat.ranged.RangedData;
import com.elvarg.game.entity.impl.npc.NPC;
import com.elvarg.game.model.Ids;
import com.elvarg.game.model.Location;

import static com.elvarg.game.content.combat.CombatFactory.RANGED_COMBAT;
import static com.elvarg.util.NpcIdentifiers.KARIL_THE_TAINTED;

@Ids({KARIL_THE_TAINTED})
public class KarilTheTainted extends NPC {

    /**
     * Constructs a new npc.
     *
     * @param id       The npc id.
     * @param position
     */
    public KarilTheTainted(int id, Location position) {
        super(id, position);

        this.getCombat().setRangedWeapon(RangedData.RangedWeapon.KARILS_CROSSBOW);
        this.getCombat().setAmmunition(RangedData.Ammunition.BOLT_RACK);
    }

    @Override
    public CombatMethod getCombatMethod() {
        return RANGED_COMBAT;
    }
}