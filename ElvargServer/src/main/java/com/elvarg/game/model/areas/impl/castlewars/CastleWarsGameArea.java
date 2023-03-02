package com.elvarg.game.model.areas.impl.castlewars;

import com.elvarg.game.content.combat.CombatFactory;
import com.elvarg.game.content.minigames.impl.CastleWars;
import com.elvarg.game.entity.impl.Mobile;
import com.elvarg.game.entity.impl.object.GameObject;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.entity.impl.playerbot.PlayerBot;
import com.elvarg.game.model.*;
import com.elvarg.game.model.areas.Area;
import com.elvarg.game.model.container.impl.Equipment;
import com.elvarg.game.model.dialogues.entries.impl.StatementDialogue;
import com.elvarg.util.Misc;

import java.util.Arrays;
import java.util.Optional;

import static com.elvarg.util.ObjectIdentifiers.*;

public class CastleWarsGameArea extends Area {

    private static final Boundary[] DUNGEON_BOUNDARIES = {
            new Boundary(2365, 2404, 9500, 9530),
            new Boundary(2394, 2431, 9474, 9499),
            new Boundary(2405, 2424, 9500, 9509)
    };

    private static final Boundary GAME_SURFACE_BOUNDARY = new PolygonalBoundary(
            new int[][]{
                    {2377, 3079},
                    {2368, 3079},
                    {2368, 3136},
                    {2416, 3136},
                    {2432, 3120},
                    {2432, 3080},
                    {2432, 3072},
                    {2384, 3072}
            }
    );

    public CastleWarsGameArea() {
        // Merge the Dungeon boundaries and the game surface area polygonal boundary
        super(Arrays.asList(Misc.concatWithCollection(DUNGEON_BOUNDARIES, new Boundary[]{GAME_SURFACE_BOUNDARY})));
    }

    @Override
    public String getName() {
        return "the Castle Wars Minigame";
    }

    @Override
    public void process(Mobile character) {
        Player player = character.getAsPlayer();
        if (player == null) {
            return;
        }

        CastleWars.Team team = CastleWars.Team.getTeamForPlayer(player);

        if (team == null)
            return;

        int config;
        player.getPacketSender().sendWalkableInterface(11146);
        player.getPacketSender().sendString("Zamorak = " + CastleWars.Team.ZAMORAK.getScore(), 11147);
        player.getPacketSender().sendString(CastleWars.Team.SARADOMIN.getScore() + " = Saradomin", 11148);
        player.getPacketSender().sendString(CastleWars.START_GAME_TASK.getRemainingTicks() + " mins", 11155);
        config = 2097152 * CastleWars.saraFlag;
        player.getPacketSender().sendToggle(378, config);
        config = 2097152 * CastleWars.zammyFlag; // flags 0 = safe 1 = taken 2 = dropped
        player.getPacketSender().sendToggle(377, config);

        boolean inSpawn = team.respawn_area_bounds.inside(player.getLocation());

        player.getPacketSender().sendString(inSpawn ? "You have "+Misc.ticksToTime(player.castlewarsIdleTime)+" to leave the respawn room." : "", 12837);

        if (inSpawn && !player.isPlayerBot()) {
            if (player.castlewarsIdleTime > 0) {
                player.castlewarsIdleTime--;
                if (player.castlewarsIdleTime == 0) {
                    postLeave(player, false);
                }
            }
        }
    }

    @Override
    public CombatFactory.CanAttackResponse canAttack(Mobile attacker, Mobile target) {
        Player playerAttacker = attacker.getAsPlayer();
        Player playerTarget = target.getAsPlayer();
        if (playerAttacker == null || playerTarget == null) {
            // If either attacker or target is not a player, run default behaviour.
            return super.canAttack(attacker, target);
        }

        if (CastleWars.Team.getTeamForPlayer(playerTarget) == CastleWars.Team.getTeamForPlayer(playerAttacker)) {
            // Stop players from attacking their own team members
            return CombatFactory.CanAttackResponse.CASTLE_WARS_FRIENDLY_FIRE;
        }

        return CombatFactory.CanAttackResponse.CAN_ATTACK;
    }

    @Override
    public void postEnter(Mobile character) {
        Player player = character.getAsPlayer();
        if (player == null) {
            return;
        }

        // Add attack option
        player.getPacketSender().sendInteractionOption("Attack", 2, true);
    }

    @Override
    public void postLeave(Mobile character, boolean logout) {
        Player player = character.getAsPlayer();
        if (player == null) {
            return;
        }

        // Remove attack option
        player.getPacketSender().sendInteractionOption("null", 2, true);

        CastleWars.Team.removePlayer(player);

        if (getPlayers().size() < 2 || (CastleWars.Team.ZAMORAK.getPlayers().size() == 0 ||
                CastleWars.Team.SARADOMIN.getPlayers().size() == 0)) {
            // If either team has no players left, the game must end
            CastleWars.endGame();
        }

        if (logout) {
            // Player has logged out, teleport them to the lobby
            player.moveTo(new Location(2439 + Misc.random(4), 3085 + Misc.random(5), 0));
        }

        // Remove items
        CastleWars.deleteGameItems(player);

        // Remove the cape
        player.getEquipment().setItem(Equipment.CAPE_SLOT, Equipment.NO_ITEM);
        player.getEquipment().refreshItems();
        player.getUpdateFlag().flag(Flag.APPEARANCE);

        // Remove the interface
        player.getPacketSender().sendWalkableInterface(-1);
        player.getPacketSender().sendEntityHintRemoval(true);
    }

    @Override
    public boolean canPlayerBotIdle(PlayerBot playerBot) {
        // Allow Player Bots to idle here
        return true;
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
    public boolean handleObjectClick(Player player, GameObject object, int type) {
        switch (object.getId()) {
            case PORTAL_10:// Portals in team respawn room
            case PORTAL_11:
                player.moveTo(new Location(2440, 3089, 0));
                player.getPacketSender().sendMessage("The Castle Wars game has ended for you!");
                return true;

            case SARADOMIN_STANDARD_2:
            case 4377:
                CastleWars.Team team = CastleWars.Team.getTeamForPlayer(player);
                if (team == null) {
                    return true;
                }

                switch (team) {
                    case SARADOMIN:
                        CastleWars.returnFlag(player, player.getEquipment().getSlot(Equipment.WEAPON_SLOT));
                        return true;
                    case ZAMORAK:
                        CastleWars.captureFlag(player, team);
                        return true;
                }
                return true;

            case ZAMORAK_STANDARD_2: // zammy flag
            case 4378:
                team = CastleWars.Team.getTeamForPlayer(player);
                if (team == null) {
                    return true;
                }

                switch (team) {
                    case SARADOMIN:
                        CastleWars.captureFlag(player, team);
                        return true;
                    case ZAMORAK:
                        CastleWars.returnFlag(player, player.getEquipment().getSlot(Equipment.WEAPON_SLOT));
                        return true;
                }
                return true;

            case TRAPDOOR_16: // Trap door into saradomin spawn point
                if (CastleWars.Team.getTeamForPlayer(player) == CastleWars.Team.ZAMORAK) {
                    player.getPacketSender().sendMessage("You are not allowed in the other teams spawn point.");
                    return true;
                }

                player.moveTo(new Location(2429, 3075, 1));
                return true;
            case TRAPDOOR_17: // Trap door into saradomin spawn point
                if (CastleWars.Team.getTeamForPlayer(player) == CastleWars.Team.SARADOMIN) {
                    player.getPacketSender().sendMessage("You are not allowed in the other teams spawn point.");
                    return true;
                }

                player.moveTo(new Location(2370, 3132, 1));
                return true;
        }

        return false;
    }

    @Override
    public boolean canTeleport(Player player) {
        StatementDialogue.send(player, "You can't leave just like that!");
        return false;
    }

    @Override
    public boolean handleDeath(Player player, Optional<Player> kill) {
        CastleWars.Team team = CastleWars.Team.getTeamForPlayer(player);

        if (team == null) {
            System.err.println("no team for " + player.getUsername());
            return false;
        }
        /** Respawns them in any free tile within the starting room **/
        CastleWars.dropFlag(player, team);
        player.smartMove(team.respawn_area_bounds);
        player.castlewarsDeaths++;

        if (!kill.isPresent())
            return true;

        Player killer = kill.get();

        killer.castlewarsKills++;
        return true;
    }
}
