package com.elvarg.game.content.minigames.impl.pestcontrol;

import com.elvarg.game.model.Location;

/**
 * @author Ynneh | 14/02/2023 - 02:36
 * <https://github.com/drhenny>
 */
public class PestControlPortal {


    public int id;
    public Location location;

    PestControlPortal(int id, Location loc) {
        this.id = id;
        this.location = loc;
    }
}
