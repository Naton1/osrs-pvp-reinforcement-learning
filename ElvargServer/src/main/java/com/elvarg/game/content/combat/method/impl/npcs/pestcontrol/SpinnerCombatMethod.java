package com.elvarg.game.content.combat.method.impl.npcs.pestcontrol;

import com.elvarg.game.content.combat.hit.HitDamage;
import com.elvarg.game.content.combat.hit.HitMask;
import com.elvarg.game.content.combat.method.CombatMethod;
import com.elvarg.game.content.combat.method.impl.MeleeCombatMethod;
import com.elvarg.game.content.minigames.impl.pestcontrol.PestControl;
import com.elvarg.game.content.minigames.impl.pestcontrol.PestControlPortal;
import com.elvarg.game.content.minigames.impl.pestcontrol.PestControlPortalData;
import com.elvarg.game.entity.impl.Mobile;
import com.elvarg.game.entity.impl.npc.NPC;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.Animation;
import com.elvarg.game.model.Graphic;
import com.elvarg.game.model.GraphicHeight;
import com.elvarg.game.model.movement.path.PathFinder;
import com.elvarg.util.Misc;

import java.util.List;

/**
 * @author Ynneh | 01/03/2023 - 04:17
 * <https://github.com/drhenny>
 */
public class SpinnerCombatMethod extends MeleeCombatMethod {

    private boolean finish;

    @Override
    public void onTick(NPC spinner, Mobile target) {

        NPC portal = getClosestPortal(spinner);

        if (portal == null || finish) {
            return;
        }
        if (portal.isDying()) {
            finish = true;
            PestControlPortalData spawnedFrom = (PestControlPortalData) spinner.getAttribute("PEST_PORTAL");
            if (spawnedFrom == null)
                return;
            for (Player localPlayers : PestControl.GAME_AREA.getPlayers()) {
                if (localPlayers == null)
                    continue;
                if (localPlayers.getLocation().isWithinDistance(spinner.getLocation(), 10)) {
                    localPlayers.getCombat().getHitQueue().addPendingDamage(new HitDamage(5, HitMask.GREEN));
                }
            }
            return;
        }

        int distance = spinner.getLocation().getDistance(portal.getLocation());
        if (portal.getHitpoints() < portal.getDefinition().getHitpoints() && Math.random() <= 0.20) {
            if (distance <= 3) {
                healPortal(portal, spinner);
            } else if (distance <= 15) {
                PathFinder.calculateEntityRoute(spinner, portal.getLocation().getX(), portal.getLocation().getY());
            }
        }
    }

    private void healPortal(NPC portal, NPC spinner) {
        spinner.setMobileInteraction(portal);
        spinner.performGraphic(new Graphic(658, GraphicHeight.LOW));
        spinner.performAnimation(new Animation(3911));
        portal.heal(Misc.random(10, 20));
        int x = spinner.getLocation().getX();
        int y = spinner.getLocation().getY();
        PathFinder.calculateEntityRoute(spinner, Misc.random(x - 4, x + 4), Misc.random(y - 4, y + 4));
    }

    private NPC getClosestPortal(NPC shifter) {
        NPC closestPortal = null;
        int closestTiles = 100;
        for (NPC npc : PestControl.portals) {
            if (npc == null)
                continue;
            int distance = shifter.getLocation().getDistance(npc.getLocation());
            if (distance <= closestTiles) {
                closestPortal = npc;
                closestTiles = distance;
            }
        }
        return closestPortal;
    }

}
