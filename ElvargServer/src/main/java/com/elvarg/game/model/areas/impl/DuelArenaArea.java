package com.elvarg.game.model.areas.impl;

import java.util.Arrays;
import java.util.Optional;

import com.elvarg.game.content.Dueling.DuelRule;
import com.elvarg.game.content.Dueling.DuelState;
import com.elvarg.game.entity.impl.Mobile;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.Boundary;
import com.elvarg.game.model.areas.Area;

public class DuelArenaArea extends Area {

    public DuelArenaArea() {
        super(Arrays.asList(new Boundary(3326, 3383, 3197, 3295)));
    }

    @Override
    public void enter(Mobile character) {
        if (character.isPlayer()) {
            Player player = character.getAsPlayer();
            player.getPacketSender().sendInteractionOption("Challenge", 1, false);
            player.getPacketSender().sendInteractionOption("null", 2, true);
        }
    }

    @Override
    public void leave(Mobile character, boolean logout) {
        if (character.isPlayer()) {
            Player player = character.getAsPlayer();
            if (player.getDueling().inDuel()) {
                player.getDueling().duelLost();
            }
            player.getPacketSender().sendInteractionOption("null", 2, true);
            player.getPacketSender().sendInteractionOption("null", 1, false);
        }
    }

    @Override
    public void process(Mobile character) {
    }

    @Override
    public boolean canTeleport(Player player) {
        if (player.getDueling().inDuel()) {
            //DialogueManager.sendStatement(player, "You cannot teleport out of a duel!");
            return false;
        }
        return true;
    }

    @Override
    public boolean canAttack(Mobile character, Mobile target) {
        if (character.isPlayer() && target.isPlayer()) {
            Player a = character.getAsPlayer();
            Player t = target.getAsPlayer();
            if (a.getDueling().getState() == DuelState.IN_DUEL && t.getDueling().getState() == DuelState.IN_DUEL) {
                return true;
            } else if (a.getDueling().getState() == DuelState.STARTING_DUEL
                    || t.getDueling().getState() == DuelState.STARTING_DUEL) {
                //DialogueManager.sendStatement(a, "The duel hasn't started yet!");
                return false;
            }
            return false;
        }
        return true;
    }

    @Override
    public boolean canTrade(Player player, Player target) {
        if (player.getDueling().inDuel()) {
            //DialogueManager.sendStatement(player, "You cannot trade during a duel!");
            return false;
        }
        return true;
    }

    @Override
    public boolean isMulti(Mobile character) {
        return true;
    }

    @Override
    public boolean canEat(Player player, int itemId) {
        if (player.getDueling().inDuel() && player.getDueling().getRules()[DuelRule.NO_FOOD.ordinal()]) {
            //DialogueManager.sendStatement(player, "Food has been disabled in this duel!");
            return true;
        }
        return true;
    }

    @Override
    public boolean canDrink(Player player, int itemId) {
        if (player.getDueling().inDuel() && player.getDueling().getRules()[DuelRule.NO_POTIONS.ordinal()]) {
            //DialogueManager.sendStatement(player, "Potions have been disabled in this duel!");
            return true;
        }
        return true;
    }

    @Override
    public boolean dropItemsOnDeath(Player player, Optional<Player> killer) {
        if (player.getDueling().inDuel()) {
            return false;
        }
        return true;
    }

    @Override
    public boolean handleDeath(Player player, Optional<Player> killer) {
        if (player.getDueling().inDuel()) {
            player.getDueling().duelLost();
            return true;
        }
        return false;
    }

    @Override
    public void onPlayerRightClick(Player player, Player rightClicked, int option) {
        if (option == 1) {
            if (player.busy()) {
                player.getPacketSender().sendMessage("You cannot do that right now.");
                return;
            }
            if (rightClicked.busy()) {
                player.getPacketSender().sendMessage("That player is currently busy.");
                return;
            }
            player.getDueling().requestDuel(rightClicked);
        }
    }

    @Override
    public void defeated(Player player, Mobile character) {
    }
    
    @Override
    public boolean overridesNpcAggressionTolerance(Player player, int npcId) {
        return false;
    }

    @Override
    public boolean handleObjectClick(Player player, int objectId, int type) {
        return false;
    }
}
