package com.elvarg.game.content.combat.method.impl.npcs.pestcontrol;

import com.elvarg.game.World;
import com.elvarg.game.content.combat.CombatType;
import com.elvarg.game.content.combat.method.impl.MeleeCombatMethod;
import com.elvarg.game.entity.impl.Mobile;
import com.elvarg.game.entity.impl.npc.NPC;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.Graphic;
import com.elvarg.game.model.GraphicHeight;
import com.elvarg.game.model.Location;
import com.elvarg.game.model.movement.path.PathFinder;
import com.elvarg.game.task.Task;
import com.elvarg.game.task.TaskManager;

import java.util.Objects;
import java.util.Optional;

import static com.elvarg.util.NpcIdentifiers.VOID_KNIGHT_8;

/**
 * @author Ynneh | 27/02/2023 - 08:30
 * <https://github.com/drhenny>
 */
public class ShifterCombatMethod extends MeleeCombatMethod {

    @Override
    public CombatType type() {
        return CombatType.MELEE;
    }

    private static final Location CENTER = new Location(2657, 2592, 0);

    @Override
    public void onTick(NPC npc, Mobile target) {

        if (npc == null || npc.isDying())
            return;

        Optional<NPC> knight = World.getNpcs().stream().filter(n -> n != null && n.getId() == VOID_KNIGHT_8).findFirst();

        if (!knight.isPresent())
            return;

        NPC knightNPC = knight.get();

        if (target == null) {
            if (Math.random() <= .20) {//20% chance to tp to middle
                teleport(npc, null, true);
                PathFinder.calculateEntityRoute(npc, knightNPC.getLocation().getX(), knightNPC.getLocation().getY());
                npc.getCombat().setTarget(knightNPC);
            } else {
                Optional<Player> p = World.getPlayers().stream().filter(n -> n != null && n.getLocation().isWithinDistance(n.getLocation(), 10)).findFirst();
                if (!p.isPresent())
                    return;
                Player t = p.get();
                PathFinder.calculateEntityRoute(npc, t.getLocation().getX(), t.getLocation().getY());
                npc.getCombat().setTarget(t);
            }
        } else {

            int distance = target.getLocation().getDistance(npc.getLocation());

            if (distance > 1) {
                if (Math.random() <= .1) {
                    teleport(npc, target, false);
                }
            }
        }
    }

    private void teleport(NPC npc, Mobile target, boolean center) {

        World.sendLocalGraphics(654, npc.getLocation(), GraphicHeight.LOW);

        TaskManager.submit(new Task(1) {

            int ticks = 0;

            @Override
            protected void execute() {
                ticks++;

                if (ticks == 1) {
                    if (center) {
                        npc.smartMove(CENTER, 2);
                    } else {
                        npc.smartMove(target.getLocation(), 2);
                    }
                    npc.performGraphic(new Graphic(654));
                }

                if (ticks == 2) {
                    npc.performGraphic(new Graphic(654));
                    this.stop();
                }
            }
        });
    }
}
