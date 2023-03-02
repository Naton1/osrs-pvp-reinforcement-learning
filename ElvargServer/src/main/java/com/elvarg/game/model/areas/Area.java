package com.elvarg.game.model.areas;

import com.elvarg.game.content.combat.CombatFactory.CanAttackResponse;
import com.elvarg.game.content.combat.hit.PendingHit;
import com.elvarg.game.entity.impl.Mobile;
import com.elvarg.game.entity.impl.npc.NPC;
import com.elvarg.game.entity.impl.object.GameObject;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.entity.impl.playerbot.PlayerBot;
import com.elvarg.game.model.Boundary;
import com.elvarg.game.model.Item;
import com.elvarg.game.model.MagicSpellbook;

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

    public boolean allowSummonPet(Player player) {
        return true;
    }

    public boolean allowDwarfCannon(Player player) {
        return true;
    }

    public final void enter(Mobile character) {
        if (character.isPlayerBot()) {
            this.playerBots.put(character.getIndex(), character.getAsPlayerBot());
        }

        if (character.isPlayer()) {
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
        }

        if (character.isPlayer()) {
            this.players.remove(character.getIndex());
        } else if (character.isNpc()) {
            this.npcs.remove(character.getIndex());
        }
    }

    public void postLeave(Mobile character, boolean logout) {}

    public void process(Mobile character) {
        // By default, do nothing in process.
    }

    public boolean canTeleport(Player player) {
        // By default, Areas allow teleporting unless otherwise specified.
        return true;
    }

    public CanAttackResponse canAttack(Mobile attacker, Mobile target) {
        if (attacker.isPlayer() && target.isPlayer()) {
            return CanAttackResponse.CANT_ATTACK_IN_AREA;
        }

        return CanAttackResponse.CAN_ATTACK;
    }

    public boolean canPlayerBotIdle(PlayerBot playerBot) {
        return false;
    }

    public void defeated(Player player, Mobile character) {
        // By default, do nothing when a player is defeated.
    }

    public boolean canTrade(Player player, Player target) {
        // By default, allow Players to trade in an Area.
        return true;
    }

    public boolean isMulti(Mobile character) {
        // By default, Areas are single combat.
        return false;
    }

    public boolean canEat(Player player, int itemId) {
        // By default, players can eat in an Area.
        return true;
    }

    public boolean canDrink(Player player, int itemId) {
        // By default, players can drink in an Area.
        return true;
    }

    public boolean dropItemsOnDeath(Player player, Optional<Player> killer) {
        // By default, players will drop items in an Area.
        return true;
    }

    public boolean handleDeath(Player player, Optional<Player> killer) {
        // By default, players Death will be handled by the main death handler.
        return false;
    }

    public void onPlayerRightClick(Player player, Player rightClicked, int option) {
        // By default, players will have the default right click in Areas.
    }

    public void onPlayerDealtDamage(Player player, Mobile target, PendingHit hit) {
        // By default, do not do any extra processing for when players deal damage in an area.
    }

    public boolean handleObjectClick(Player player, GameObject object, int type) {
        // By default, Areas don't need to handle any specific object clicking.
        return false;
    }
    
    public boolean overridesNpcAggressionTolerance(Player player, int npcId) {
        // By default, NPC tolerance works normally in Areas.
        return false;
    }

    public boolean canEquipItem(Player player, int slot, Item item) {
        // By default, Players can equip items in all areas
        return true;
    }

    public boolean canUnequipItem(Player player, int slot, Item item) {
        // By default, Players can unequip items in all areas
        return true;
    }

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

    public boolean isSpellDisabled(Player player, MagicSpellbook spellbook, int spellId) {
        return false;
    }
}
