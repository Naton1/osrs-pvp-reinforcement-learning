package com.elvarg.game.model.areas.impl.pestcontrol;

import com.elvarg.game.content.minigames.impl.PestControl;
import com.elvarg.game.entity.impl.Mobile;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.Boundary;
import com.elvarg.game.model.areas.Area;

import java.util.List;

import static com.elvarg.util.ObjectIdentifiers.*;

public class PestControlOutpostArea extends Area {

    public PestControlOutpostArea() {
        super(List.of(new Boundary(2626, 2682, 2632, 2681)));
    }

    @Override
    public String getName() {
        return "the Pest Control Outpost island";
    }

    @Override
    public boolean handleObjectClick(Player player, int objectId, int type) {
        switch (objectId) {
            case GANGPLANK_27:
                PestControl.addToWaitRoom(player);
                return true;
        }

        return false;
    }
}
