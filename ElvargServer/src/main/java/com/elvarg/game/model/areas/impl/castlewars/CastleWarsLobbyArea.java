package com.elvarg.game.model.areas.impl.castlewars;

import com.elvarg.game.content.minigames.impl.CastleWars;
import com.elvarg.game.entity.impl.npc.impl.Lanthus;
import com.elvarg.game.entity.impl.object.GameObject;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.entity.impl.playerbot.PlayerBot;
import com.elvarg.game.model.Boundary;
import com.elvarg.game.model.Location;
import com.elvarg.game.model.areas.Area;

import java.util.Arrays;

import static com.elvarg.util.ObjectIdentifiers.*;

public class CastleWarsLobbyArea extends Area {

    private Lanthus lanthus;

    public CastleWarsLobbyArea() {
        super(Arrays.asList(new Boundary(2435, 2446, 3081, 3098)));
    }

    @Override
    public String getName() {
        return "the Castle Wars Lobby";
    }

    @Override
    public boolean handleObjectClick(Player player, GameObject object, int type) {
        switch (object.getId()) {
            case ZAMORAK_PORTAL:
                CastleWars.addToWaitingRoom(player, CastleWars.Team.ZAMORAK);
                return true;

            case SARADOMIN_PORTAL:
                CastleWars.addToWaitingRoom(player, CastleWars.Team.SARADOMIN);
                return true;

            case GUTHIX_PORTAL:
                CastleWars.addToWaitingRoom(player, CastleWars.Team.GUTHIX);
                return true;

            case CASTLEWARS_BANK_CHEST:
                if (type == 1) {
                    player.getBank(player.getCurrentBankTab()).open();
                } else {
                    player.getPacketSender().sendMessage("The Grand Exchange is not available yet.");
                }

                return true;
        }

        return false;
    }

    @Override
    public boolean canPlayerBotIdle(PlayerBot playerBot) {
        // Allow Player Bots to idle here
        return true;
    }

    public Lanthus getLanthus() {
        return this.lanthus;
    }

    public void setLanthus(Lanthus lanthus) {
        this.lanthus = lanthus;
    }
}
