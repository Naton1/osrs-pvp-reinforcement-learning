package com.elvarg.game.model.areas.impl.pestcontrol;

import com.elvarg.game.content.minigames.impl.pestcontrol.PestControl;
import com.elvarg.game.content.minigames.impl.pestcontrol.PestControlBoat;
import com.elvarg.game.entity.impl.object.GameObject;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.Boundary;
import com.elvarg.game.model.Location;
import com.elvarg.game.model.areas.Area;

import java.util.List;
import java.util.Optional;

public class PestControlOutpostArea extends Area {

    public PestControlOutpostArea() {
        super(List.of(new Boundary(2626, 2682, 2632, 2681)));
    }

    @Override
    public String getName() {
        return "the Pest Control Outpost island";
    }

    @Override
    public boolean handleObjectClick(Player player, GameObject object, int type) {
        switch (object.getId()) {

        }

        Optional<PestControlBoat> boatdata = PestControlBoat.getBoat(object.getId());

        if (boatdata == null || !boatdata.isPresent())
            return false;

        PestControlBoat boat = boatdata.get();

        if (player.getSkillManager().getCombatLevel() < boat.combatLevelRequirement) {
            player.getPacketSender().sendMessage("You need a combat level of "+boat.combatLevelRequirement+" to board this boat.");
            return false;
        }

        if (player.getCurrentPet() != null) {
            player.getPacketSender().sendMessage("You cannot bring your follower with you.");
            return false;
        }

        PestControl.addToWaitingRoom(player, boat);
        return true;
    }
}
