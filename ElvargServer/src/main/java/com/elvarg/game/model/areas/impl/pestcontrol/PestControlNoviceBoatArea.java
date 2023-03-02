package com.elvarg.game.model.areas.impl.pestcontrol;

import com.elvarg.game.content.minigames.impl.CastleWars;
import com.elvarg.game.content.minigames.impl.pestcontrol.PestControl;
import com.elvarg.game.entity.impl.Mobile;
import com.elvarg.game.entity.impl.object.GameObject;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.Boundary;
import com.elvarg.game.model.Location;
import com.elvarg.game.model.areas.Area;
import com.elvarg.game.task.TaskManager;

import java.util.List;

import static com.elvarg.game.content.minigames.impl.CastleWars.START_GAME_TASK;
import static com.elvarg.game.content.minigames.impl.pestcontrol.PestControl.*;
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

        if (!NOVICE_LOBBY_TASK.isRunning() && getPlayers().size() > 0) {
            TaskManager.submit(NOVICE_LOBBY_TASK);
        }

        character.getAsPlayer().setWalkableInterfaceId(21119);
    }

    @Override
    public boolean allowDwarfCannon(Player player) {
        player.sendMessage("This would be a silly.");
        return false;
    }

    @Override
    public boolean allowSummonPet(Player player) {
        player.sendMessage("The squire doesn't allow you to bring your pet with you.");
        return false;
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

        player.getPacketSender().sendString("Players Ready: " + NOVICE_BOAT_AREA.getPlayers().size(), 21121);
        player.getPacketSender().sendString("(Need 3 to 25 players)", 21122);
        player.getPacketSender().sendString("Points: " + player.pcPoints, 21123);
    }

    @Override
    public boolean handleObjectClick(Player player, GameObject object, int type) {
        switch (object.getId()) {
            case LADDER_175:
                // Move player to the pier
                player.moveTo(PestControl.GANG_PLANK_START);
                return true;
        }

        return false;
    }
}
