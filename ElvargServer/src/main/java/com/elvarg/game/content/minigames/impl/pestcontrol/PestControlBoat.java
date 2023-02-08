package com.elvarg.game.content.minigames.impl.pestcontrol;

import com.elvarg.game.model.Location;

import java.util.Arrays;
import java.util.Optional;

/**
 * @author Ynneh | 08/02/2023 - 03:53
 * <https://github.com/drhenny>
 */
public enum PestControlBoat {

    NOVICE(40, 14315, new Location(2661, 2639)),
    INTERMEDIATE(70, 25631, new Location(2640, 2644)),
    VETERAN(100, 25632, new Location(2634, 2653));

    public int combatLevelRequirement, objectId;
    public Location enterBoatLocation;

    PestControlBoat(int combatLevelRequirement, int ladderId, Location enterBoatLocation) {
        this.combatLevelRequirement = combatLevelRequirement;
        this.objectId = ladderId;
        this.enterBoatLocation = enterBoatLocation;
    }

    public static Optional<PestControlBoat> getBoat(int ladderId) {
        return Arrays.stream(values()).filter(l -> l.objectId == ladderId).findFirst();
    }
}
