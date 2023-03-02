package com.elvarg.game.model.areas.impl;

import java.util.Arrays;
import java.util.Optional;

import com.elvarg.game.content.Dueling.DuelRule;
import com.elvarg.game.content.Dueling.DuelState;
import com.elvarg.game.content.combat.CombatFactory.CanAttackResponse;
import com.elvarg.game.entity.impl.Mobile;
import com.elvarg.game.entity.impl.object.GameObject;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.entity.impl.playerbot.PlayerBot;
import com.elvarg.game.model.Boundary;
import com.elvarg.game.model.Location;
import com.elvarg.game.model.areas.Area;
import com.elvarg.util.timers.TimerKey;

public class DuelArenaArea extends Area {

    public DuelArenaArea() {
        super(Arrays.asList(new Boundary(3326, 3383, 3197, 3295)));
    }

    @Override
    public void postEnter(Mobile character) {
        if (character.isPlayer()) {
            Player player = character.getAsPlayer();
            player.getPacketSender().sendInteractionOption("Challenge", 1, false);
            player.getPacketSender().sendInteractionOption("null", 2, true);
        }

        if (character.isPlayerBot() && this.getPlayers().size() == 0) {
            // Allow this PlayerBot to wait for players for 5 minutes
            character.getAsPlayerBot().getTimers().register(TimerKey.BOT_WAIT_FOR_PLAYERS);
        }
    }

    @Override
    public void postLeave(Mobile character, boolean logout) {
        if (character.isPlayer()) {
            Player player = character.getAsPlayer();
            if (player.getDueling().inDuel()) {
                player.getDueling().duelLost();
            }
            player.getPacketSender().sendInteractionOption("null", 2, true);
            player.getPacketSender().sendInteractionOption("null", 1, false);

            if (getPlayers().size() == 0 && getPlayerBots().size() > 0) {
                // Last player has left duel arena and there are bots
                getPlayerBots().stream().forEach((pb) -> pb.getTimers().register(TimerKey.BOT_WAIT_FOR_PLAYERS));
            }
        }
    }

    @Override
    public void process(Mobile character) {
    }

    @Override
    public boolean canTeleport(Player player) {
        if (player.getDueling().inDuel()) {
            return false;
        }
        return true;
    }

    @Override
    public CanAttackResponse canAttack(Mobile character, Mobile target) {
        if (character.isPlayer() && target.isPlayer()) {
            Player a = character.getAsPlayer();
            Player t = target.getAsPlayer();
            if (a.getDueling().getState() == DuelState.IN_DUEL && t.getDueling().getState() == DuelState.IN_DUEL) {
                return CanAttackResponse.CAN_ATTACK;
            } else if (a.getDueling().getState() == DuelState.STARTING_DUEL
                    || t.getDueling().getState() == DuelState.STARTING_DUEL) {
                return CanAttackResponse.DUEL_NOT_STARTED_YET;
            }

            return CanAttackResponse.DUEL_WRONG_OPPONENT;
        }

        return CanAttackResponse.CAN_ATTACK;
    }

    @Override
    public boolean canTrade(Player player, Player target) {
        if (player.getDueling().inDuel()) {
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
            return false;
        }
        return true;
    }

    @Override
    public boolean canDrink(Player player, int itemId) {
        if (player.getDueling().inDuel() && player.getDueling().getRules()[DuelRule.NO_POTIONS.ordinal()]) {
            return false;
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
    public boolean handleObjectClick(Player player, GameObject object, int type) {
        return false;
    }

    @Override
    public boolean canPlayerBotIdle(PlayerBot playerBot) {
        if (this.getPlayers().size() > 0) {
            // Player bots can idle here if there are any real players here
            return true;
        }

        if (playerBot.getTimers().has(TimerKey.BOT_WAIT_FOR_PLAYERS)) {
            // Player bot can idle here while waiting for players
            return true;
        }

        return false;
    }
}
