package com.elvarg.game.content.combat.method.impl.npcs.pestcontrol;

import com.elvarg.game.World;
import com.elvarg.game.content.combat.CombatType;
import com.elvarg.game.content.combat.method.impl.MeleeCombatMethod;
import com.elvarg.game.entity.impl.Mobile;
import com.elvarg.game.entity.impl.npc.NPC;
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
    public void start(Mobile character, Mobile target) {

        NPC npc = character.getAsNpc();

        if (npc == null || npc.isDying() || target == null)
            return;

        int distance = target.getLocation().getDistance(npc.getLocation());

        if (Math.random() <= .05) {//5% chance to tp to middle
            teleport(npc, target, true);
        }

        if (distance > 1) {
            if (Math.random() <= .2) {
                teleport(npc, target, false);
            }
        }

        Optional<NPC> knight = World.getNpcs().stream().filter(n -> n != null && n.getId() == VOID_KNIGHT_8 && n.getLocation().isWithinDistance(target.getLocation(), 10)).findFirst();

        if (!knight.isPresent())
            return;

        NPC knightNPC = knight.get();

        if (npc.getCombat().getTarget() == null || !Objects.equals(knightNPC, npc.getCombat().getTarget())) {
            PathFinder.calculateEntityRoute(npc, knightNPC.getLocation().getX(), knightNPC.getLocation().getY());
            npc.getCombat().setTarget(knightNPC);
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
