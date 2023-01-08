package com.elvarg.game.model.areas;

import com.elvarg.game.content.combat.CombatFactory.CanAttackResponse;
import com.elvarg.game.entity.impl.Mobile;
import com.elvarg.game.entity.impl.npc.NPC;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.entity.impl.playerbot.PlayerBot;
import com.elvarg.game.model.Boundary;

import java.util.*;

public abstract class Area {

    private final List<Boundary> boundaries;

    private final HashMap<Integer, NPC> npcs;
    private final HashMap<Integer, Player> players;
    private final HashMap<Integer, PlayerBot> playerBots;

    public Area(List<Boundary> boundaries) {
        this.boundaries = boundaries;
        this.npcs = new HashMap<>();
        this.players = new HashMap<>();
        this.playerBots = new HashMap<>();
    }

    public final void enter(Mobile character) {
        if (character.isPlayerBot()) {
            this.playerBots.put(character.getIndex(), character.getAsPlayerBot());
        } else if (character.isPlayer()) {
            this.players.put(character.getIndex(), character.getAsPlayer());
        } else if (character.isNpc()) {
            this.npcs.put(character.getIndex(), character.getAsNpc());
        }
        this.postEnter(character);
    }

    public void postEnter(Mobile character) {}

    public final void leave(Mobile character, boolean logout) {
        if (character.isPlayerBot()) {
            this.playerBots.remove(character.getIndex());
        } else if (character.isPlayer()) {
            this.players.remove(character.getIndex());
        } else if (character.isNpc()) {
            this.npcs.remove(character.getIndex());
        }

        this.postLeave(character, logout);
    }

    public void postLeave(Mobile character, boolean logout) {}

    public abstract void process(Mobile character);

    public abstract boolean canTeleport(Player player);

    public CanAttackResponse canAttack(Mobile attacker, Mobile target) {
        if (attacker.isPlayer() && target.isPlayer()) {
            return CanAttackResponse.CANT_ATTACK_IN_AREA;
        }

        return CanAttackResponse.CAN_ATTACK;
    }

    public boolean canPlayerBotIdle(PlayerBot playerBot) {
        return false;
    }

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

    public Collection<NPC> getNpcs() {
        return this.npcs.values();
    }

    public Collection<Player> getPlayers() {
        return this.players.values();
    }

    public Collection<PlayerBot> getPlayerBots() {
        return this.playerBots.values();
    }
}
