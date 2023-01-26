package com.elvarg.game.model.areas.impl.pestcontrol;

import com.elvarg.game.content.minigames.impl.PestControl;
import com.elvarg.game.entity.impl.Mobile;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.Boundary;
import com.elvarg.game.model.areas.Area;

import java.util.List;

import static com.elvarg.game.content.minigames.impl.PestControl.*;
import static com.elvarg.util.ObjectIdentifiers.LADDER_175;

public class PestControlNoviceBoatArea extends Area {

    public static final Boundary BOUNDARY = new Boundary(2660, 2663, 2638, 2643);

    public PestControlNoviceBoatArea() {
        super(List.of(BOUNDARY));
    }

    @Override
    public void postEnter(Mobile character) {
        if (!character.isPlayer()) {
            return;
        }

        character.getAsPlayer().setWalkableInterfaceId(21119);
    }

    @Override
    public void postLeave(Mobile character, boolean logout) {
        if (!character.isPlayer()) {
            return;
        }

        character.getAsPlayer().setWalkableInterfaceId(-1);
    }

    @Override
    public void process(Mobile character) {
        Player player = character.getAsPlayer();
        if (player == null) {
            // Don't process for any other type of Mobile, just players
            return;
        }

        if (gameStarted) {
            player.getPacketSender().sendString("Next Departure: " + (waitTimer + gameTimer) / 60 + " minutes", 21120);
        } else {
            player.getPacketSender().sendString("Next Departure: " + waitTimer + " seconds.", 21120);
        }
        player.getPacketSender().sendString("Players Ready: " + NOVICE_BOAT_AREA.getPlayers().size(), 21121);
        player.getPacketSender().sendString("(Need 3 to 25 players)", 21122);
        player.getPacketSender().sendString("Points: " + player.pcPoints, 21123);
    }

    @Override
    public boolean handleObjectClick(Player player, int objectId, int type) {
        switch (objectId) {
            case LADDER_175:
                // Move player to the pier
                player.moveTo(PestControl.GANG_PLANK_START);
                return true;
        }

        return false;
    }
}
