package com.elvarg.game.entity.impl.npc.impl;

import com.elvarg.game.content.combat.method.CombatMethod;
import com.elvarg.game.content.combat.method.impl.npcs.JadCombatMethod;
import com.elvarg.game.entity.impl.npc.NPC;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.Ids;
import com.elvarg.game.model.Location;
import com.elvarg.game.model.areas.impl.FightCavesArea;

import static com.elvarg.util.NpcIdentifiers.TZTOK_JAD;

@Ids(TZTOK_JAD)
public class TztokJad extends NPC {

    private static final CombatMethod COMBAT_METHOD = new JadCombatMethod();

    public TztokJad(Player player, FightCavesArea area, int id, Location position) {
        super(id, position);
        setOwner(player);
        area.add(this);
    }
    
    @Override
    public int aggressionDistance() {
        return 64;
    }

    @Override
    public CombatMethod getCombatMethod() {
        return TztokJad.COMBAT_METHOD;
    }
}
