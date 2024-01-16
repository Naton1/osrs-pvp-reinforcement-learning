package com.elvarg.game.content.combat.method.impl.npcs.pestcontrol;

import com.elvarg.game.World;
import com.elvarg.game.content.combat.CombatType;
import com.elvarg.game.content.combat.hit.HitDamage;
import com.elvarg.game.content.combat.hit.HitMask;
import com.elvarg.game.content.combat.hit.PendingHit;
import com.elvarg.game.content.combat.method.CombatMethod;
import com.elvarg.game.content.combat.method.impl.MeleeCombatMethod;
import com.elvarg.game.content.minigames.impl.pestcontrol.PestControl;
import com.elvarg.game.entity.impl.Mobile;
import com.elvarg.game.entity.impl.npc.NPC;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.Graphic;
import com.elvarg.util.Misc;
import com.google.common.collect.Lists;

import java.util.List;
import java.util.Optional;

/**
 * @author Ynneh | 27/02/2023 - 08:03
 * <https://github.com/drhenny>
 */
public class SplatterCombatMethod extends MeleeCombatMethod {

    @Override
    public CombatType type() {
        return CombatType.MELEE;
    }

    private boolean finish;

    @Override
    public void onTick(NPC npc, Mobile target) {

        if (finish)
            return;

        boolean suicide = npc.getAttribute("SPLATTER_SUICIDE") != null;

        if (!suicide) {
            if (Math.random() <= 0.05) {
                npc.setAttribute("SPLATTER_SUICIDE", true);
            }
            return;
        }

        if (suicide) {
            deathAction(npc);
        }

    }

    @Override
    public void onDeath(NPC npc, Optional<Player> killer) {
        deathAction(npc);
    }

    private void deathAction(NPC npc) {
        finish = true;
        npc.performGraphic(new Graphic(650));
        List<Mobile> inDistance = Lists.newArrayList();
        World.getPlayers().stream().filter(p -> p != null && !p.isDying() && p.getLocation().isWithinDistance(npc.getLocation(), 1)).forEach(e -> inDistance.add(e));
        World.getNpcs().stream().filter(n -> n != null && !n.isDying() && n.getDefinition().isAttackable() && n.getLocation().isWithinDistance(npc.getLocation(), 1)).forEach(e -> inDistance.add(e));
        for (Mobile entity : inDistance) {
            if (entity != null) {
                if (entity.getLocation().isWithinDistance(npc.getLocation(), 1)) {
                    entity.getCombat().getHitQueue()
                            .addPendingDamage(new HitDamage(Misc.random(5, 25), HitMask.RED));
                }
            }
        }
    }
}
