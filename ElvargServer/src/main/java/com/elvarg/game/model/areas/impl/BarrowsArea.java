package com.elvarg.game.model.areas.impl;

import com.elvarg.game.content.minigames.impl.Barrows;
import com.elvarg.game.entity.impl.Mobile;
import com.elvarg.game.entity.impl.object.GameObject;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.Boundary;
import com.elvarg.game.model.Location;
import com.elvarg.game.model.areas.Area;

import java.util.Arrays;
import java.util.Optional;

public class BarrowsArea extends Area {

    public BarrowsArea() {
        super(Arrays.asList(new Boundary(3521, 3582, 9662, 9724), new Boundary(3545, 3583, 3265, 3306)));
    }

    @Override
    public void postEnter(Mobile character) {
        if (character.isPlayer()) {
            Player player = character.getAsPlayer();
            player.getPacketSender().sendWalkableInterface(Barrows.KILLCOUNTER_INTERFACE_ID);
            Barrows.updateInterface(player);
        }
    }

    @Override
    public void postLeave(Mobile character, boolean logout) {
        if (character.isPlayer()) {
            character.getAsPlayer().getPacketSender().sendWalkableInterface(-1);
        }
    }

    @Override
    public void process(Mobile character) {
    }

    @Override
    public boolean canTeleport(Player player) {
        return true;
    }

    @Override
    public boolean canTrade(Player player, Player target) {
        return true;
    }

    @Override
    public boolean isMulti(Mobile character) {
        return false;
    }

    @Override
    public boolean canEat(Player player, int itemId) {
        return true;
    }

    @Override
    public boolean canDrink(Player player, int itemId) {
        return true;
    }

    @Override
    public boolean dropItemsOnDeath(Player player, Optional<Player> killer) {
        return true;
    }

    @Override
    public boolean handleDeath(Player player, Optional<Player> killer) {
        return false;
    }

    @Override
    public void onPlayerRightClick(Player player, Player rightClicked, int option) {
    }

    @Override
    public void defeated(Player player, Mobile character) {
        if (character.isNpc()) {
            Barrows.brotherDeath(player, character.getAsNpc());
        }
    }

    @Override
    public boolean handleObjectClick(Player player, GameObject object, int type) {
        return Barrows.handleObject(player, object.getId());
    }
}
