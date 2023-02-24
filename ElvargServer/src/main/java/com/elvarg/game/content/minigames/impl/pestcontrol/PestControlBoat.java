package com.elvarg.game.content.minigames.impl.pestcontrol;

import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.Location;
import com.google.common.collect.Lists;

import java.util.*;

/**
 * @author Ynneh | 08/02/2023 - 03:53
 * <https://github.com/drhenny>
 */
public enum PestControlBoat {

    NOVICE(40, 14315, new Location(2661, 2639), 1771),
    INTERMEDIATE(70, 25631, new Location(2640, 2644), 1772),
    VETERAN(100, 25632, new Location(2634, 2653), 1773);

    public int combatLevelRequirement, objectId;
    public Location enterBoatLocation;

    public int squireId;

    private Queue<Player> queue;
    public Queue<Player> getQueue() {
        if (queue == null)
            queue = new LinkedList<>();
        return queue;
    }

    PestControlBoat(int combatLevelRequirement, int ladderId, Location enterBoatLocation, int squireId) {
        this.combatLevelRequirement = combatLevelRequirement;
        this.objectId = ladderId;
        this.enterBoatLocation = enterBoatLocation;
        this.squireId = squireId;
    }

    public static Optional<PestControlBoat> getBoat(int ladderId) {
        return Arrays.stream(values()).filter(l -> l.objectId == ladderId).findFirst();
    }
}
