package com.elvarg.game.content.combat.method.impl.npcs.pestcontrol;

import com.elvarg.game.content.combat.method.impl.RangedCombatMethod;
import com.elvarg.game.content.minigames.impl.pestcontrol.PestControl;
import com.elvarg.game.entity.impl.Mobile;
import com.elvarg.game.entity.impl.npc.NPC;
import com.elvarg.game.model.Projectile;
import com.elvarg.game.model.movement.path.PathFinder;

/**
 * @author Ynneh | 08/03/2023 - 12:53
 * <https://github.com/drhenny>
 */
public class DefilerCombatMethod extends RangedCombatMethod {

    public void onTick(NPC npc, Mobile target) {

        NPC knight = PestControl.knight;

        if (knight == null || npc.isDying())
            return;

        int knightDistance = npc.getLocation().getDistance(knight.getLocation());

        if (knightDistance <= 20) {
            if (target == null || Math.random() <= 0.05) {
                setKnightTarget(npc, knight);
            }
        }
    }

    @Override
    public void start(Mobile character, Mobile target) {
        Projectile.sendProjectile(character, target, new Projectile(656, 62, 80, 35, 43));

    }

    private void setKnightTarget(NPC npc, NPC knight) {
        npc.getCombat().setTarget(knight);
        PathFinder.calculateEntityRoute(npc, knight.getLocation().getX(), knight.getLocation().getY());
    }
}
