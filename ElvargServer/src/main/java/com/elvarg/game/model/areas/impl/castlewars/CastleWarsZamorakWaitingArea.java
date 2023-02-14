package com.elvarg.game.model.areas.impl.castlewars;

import com.elvarg.game.content.minigames.impl.CastleWars;
import com.elvarg.game.entity.impl.Mobile;
import com.elvarg.game.entity.impl.object.GameObject;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.entity.impl.playerbot.PlayerBot;
import com.elvarg.game.model.Boundary;
import com.elvarg.game.model.Flag;
import com.elvarg.game.model.Item;
import com.elvarg.game.model.Location;
import com.elvarg.game.model.areas.Area;
import com.elvarg.game.model.container.impl.Equipment;
import com.elvarg.game.task.TaskManager;
import com.elvarg.util.Misc;

import java.util.Arrays;

import static com.elvarg.game.content.minigames.impl.CastleWars.START_GAME_TASK;
import static com.elvarg.util.ObjectIdentifiers.PORTAL_9;

public class CastleWarsZamorakWaitingArea extends Area {

    public CastleWarsZamorakWaitingArea() {
        super(Arrays.asList(new Boundary(2408, 2432, 9512, 9535)));
    }

    @Override
    public String getName() {
        return "the Zamorak waiting room in Castle Wars";
    }

    @Override
    public void postEnter(Mobile character) {
        Player player = character.getAsPlayer();
        if (player == null) {
            return;
        }

        if (!START_GAME_TASK.isRunning() && CastleWars.SARADOMIN_WAITING_AREA.getPlayers().size() > 0) {
            // Ensure the game start timer is active
            TaskManager.submit(START_GAME_TASK);
        }

        String announcement = "Next Game Begins In: " + Misc.getSeconds(START_GAME_TASK.getRemainingTicks()) + " seconds.";
        player.getPacketSender().sendMessage(announcement);

        // Announce the next game in the lobby via Lanthus
        CastleWars.LOBBY_AREA.getLanthus().forceChat(announcement);

        // Equip the cape
        player.getEquipment().setItem(Equipment.CAPE_SLOT, CastleWars.ZAMORAK_CAPE);
        player.getEquipment().refreshItems();
        player.getUpdateFlag().flag(Flag.APPEARANCE);

        // TODO: If player is wearing saradomin items, transform them
    }

    @Override
    public void postLeave(Mobile character, boolean logout) {
        Player player = character.getAsPlayer();
        if (player == null) {
            return;
        }

        if (START_GAME_TASK.isRunning() && getPlayers().size() == 0
                && CastleWars.SARADOMIN_WAITING_AREA.getPlayers().size() == 0) {
            // Ensure the game start timer is cancelled
            TaskManager.cancelTasks(START_GAME_TASK);
        }

        if (logout) {
            // Player has logged out, teleport them to the lobby
            player.moveTo(new Location(2439 + Misc.random(4), 3085 + Misc.random(5), 0));
        }

        if (player.getArea() != CastleWars.GAME_AREA) {
            // Player has left and not went into the game area, remove cape & items
            CastleWars.deleteGameItems(player);
            player.resetAttributes();
        }

        // Remove the interface
        player.getPacketSender().sendWalkableInterface(-1);

        // TODO: Un-transform player if they were transformed
    }

    @Override
    public boolean handleObjectClick(Player player, GameObject object, int type) {
        switch (object.getId()) {
            case PORTAL_9:
                player.moveTo(new Location(2439 + Misc.random(4),
                        3085 + Misc.random(5), 0));
                return true;
        }

        return false;
    }

    @Override
    public void process(Mobile character) {
        Player player = character.getAsPlayer();
        if (player == null) {
            return;
        }

        // Update the interface
        player.getPacketSender().sendString(CastleWars.START_GAME_TASK.isRunning() ?
                "Time until next game starts: " + Math.floor(START_GAME_TASK.getRemainingTicks())
                : "Waiting for players to join the other team.", 11480);

        // Send the interface
        player.getPacketSender().sendWalkableInterface(11479);
    }

    @Override
    public boolean canEquipItem(Player player, int slot, Item item) {
        if (slot == Equipment.CAPE_SLOT || slot == Equipment.HEAD_SLOT) {
            player.getPacketSender().sendMessage("You can't remove your team's colours.");
            return false;
        }

        return true;
    }

    @Override
    public boolean canUnequipItem(Player player, int slot, Item item) {
        if (slot == Equipment.CAPE_SLOT || slot == Equipment.HEAD_SLOT) {
            player.getPacketSender().sendMessage("You can't remove your team's colours.");
            return false;
        }

        return true;
    }

    @Override
    public boolean canPlayerBotIdle(PlayerBot playerBot) {
        // Allow the player bot to wait here if there are players in the other team
        return CastleWars.SARADOMIN_WAITING_AREA.getPlayers().size() > 0;
    }
}
