package com.elvarg.game.content.minigames.impl;

import com.elvarg.game.World;
import com.elvarg.game.entity.impl.npc.impl.TztokJad;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.Location;
import com.elvarg.game.model.areas.impl.FightCavesArea;
import com.elvarg.game.task.Task;
import com.elvarg.game.task.TaskManager;

public class FightCaves {

    public static final Location ENTRANCE = new Location(2413, 5117);
    public static final Location EXIT = new Location(2438, 5168);
    private static final Location JAD_SPAWN_POS = new Location(2401, 5088);
    private static final int JAD_NPC_ID = 3127;

    public static void start(Player player) {
        final FightCavesArea area = new FightCavesArea();
        area.add(player);
    //    DialogueManager.start(player, 23);
        TaskManager.submit(new Task(14, player, false) {
            @Override
            protected void execute() {
                stop();
                if (area.isDestroyed()) {
                    return;
                }
                World.getAddNPCQueue().add(new TztokJad(player, area, JAD_NPC_ID, JAD_SPAWN_POS.clone()));
            }
        });
    }
}
