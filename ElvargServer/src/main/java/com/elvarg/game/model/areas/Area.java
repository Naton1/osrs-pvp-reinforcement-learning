package com.elvarg.game.model.areas;

import com.elvarg.game.entity.impl.Mobile;
import com.elvarg.game.entity.impl.npc.NPC;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.entity.impl.playerbot.PlayerBot;
import com.elvarg.game.model.Boundary;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public abstract class Area {

    private final List<Boundary> boundaries;

    private final List<NPC> npcs;
    private final List<Player> players;
    private final List<PlayerBot> playerBots;

    public Area(List<Boundary> boundaries) {
        this.boundaries = boundaries;
        this.npcs = new ArrayList<NPC>();
        this.players = new ArrayList<Player>();
        this.playerBots = new ArrayList<PlayerBot>();
    }

    public void enter(Mobile character) {
        if (character.isPlayerBot()) {
            this.playerBots.add(character.getAsPlayerBot());
            return;
        }

        if (character.isPlayer()) {
            this.players.add(character.getAsPlayer());
            return;
        }

        if (character.isNpc()) {
            this.npcs.add(character.getAsNpc());
        }
    }

    public void leave(Mobile character, boolean logout) {
        if (character.isPlayerBot()) {
            this.playerBots.remove(character.getAsPlayerBot());
            return;
        }

        if (character.isPlayer()) {
            this.players.remove(character.getAsPlayer());
            return;
        }

        if (character.isNpc()) {
            this.npcs.remove(character.getAsNpc());
        }
    }

    public abstract void process(Mobile character);

    public abstract boolean canTeleport(Player player);

    public abstract boolean canAttack(Mobile attacker, Mobile target);

    public abstract void defeated(Player player, Mobile character);

    public abstract boolean canTrade(Player player, Player target);

    public abstract boolean isMulti(Mobile character);

    public abstract boolean canEat(Player player, int itemId);

    public abstract boolean canDrink(Player player, int itemId);

    public abstract boolean dropItemsOnDeath(Player player, Optional<Player> killer);

    public abstract boolean handleDeath(Player player, Optional<Player> killer);

    public abstract void onPlayerRightClick(Player player, Player rightClicked, int option);

    public abstract boolean handleObjectClick(Player player, int objectId, int type);
    
    public abstract boolean overridesNpcAggressionTolerance(Player player, int npcId);

    public List<Boundary> getBoundaries() {
        return boundaries;
    }

    public String getName() {
        return this.getClass().getSimpleName();
    }

    public List<NPC> getNpcs() {
        return this.npcs;
    }

    public List<Player> getPlayers() {
        return this.players;
    }

    public List<PlayerBot> getPlayerBots() {
        return this.playerBots;
    }
}
