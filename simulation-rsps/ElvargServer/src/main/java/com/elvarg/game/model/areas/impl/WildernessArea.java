package com.elvarg.game.model.areas.impl;

import com.elvarg.game.content.Obelisks;
import com.elvarg.game.content.combat.CombatFactory;
import com.elvarg.game.content.combat.CombatFactory.CanAttackResponse;
import com.elvarg.game.content.combat.bountyhunter.BountyHunter;
import com.elvarg.game.entity.impl.Mobile;
import com.elvarg.game.entity.impl.object.GameObject;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.entity.impl.playerbot.PlayerBot;
import com.elvarg.game.model.Boundary;
import com.elvarg.game.model.Location;
import com.elvarg.game.model.areas.Area;
import com.elvarg.game.model.rights.PlayerRights;

import java.util.Arrays;
import java.util.Optional;

public class WildernessArea extends Area {

	@Override
	public String getName() {
		return "the Wilderness";
	}

	public static int getLevel(int y) {
		return ((((y > 6400 ? y - 6400 : y) - 3520) / 8) + 1);
	}

	public static boolean multi(int x, int y) {
		if (x >= 3155 && y >= 3798 || x >= 3020 && x <= 3055 && y >= 3684 && y <= 3711
				|| x >= 3150 && x <= 3195 && y >= 2958 && y <= 3003 || x >= 3645 && x <= 3715 && y >= 3454 && y <= 3550
				|| x >= 3150 && x <= 3199 && y >= 3796 && y <= 3869 || x >= 2994 && x <= 3041 && y >= 3733 && y <= 3790
				|| x >= 3136 && x <= 3327 && y >= 3527 && y <= 3650) {
			return true;
		}
		return false;
	}

	public WildernessArea() {
		super(Arrays.asList(new Boundary(2940, 3392, 3525, 3968), new Boundary(2986, 3012, 10338, 10366),
				new Boundary(3653, 3720, 3441, 3538), new Boundary(3650, 3653, 3457, 3472),
				new Boundary(3150, 3199, 3796, 3869), new Boundary(2994, 3041, 3733, 3790),
				new Boundary(3061, 3074, 10253, 10262)));
	}

	@Override
	public void postEnter(Mobile character) {
		if (character.isPlayer()) {
			Player player = character.getAsPlayer();
			player.getPacketSender().sendInteractionOption("Attack", 2, true);
			player.getPacketSender().sendWalkableInterface(197);
			BountyHunter.updateInterface(player);
			if (!BountyHunter.PLAYERS_IN_WILD.contains(player)) {
				BountyHunter.PLAYERS_IN_WILD.add(player);
			}
		}
	}

	@Override
	public void postLeave(Mobile character, boolean logout) {
		if (character.isPlayer()) {
			Player player = character.getAsPlayer();
			player.getPacketSender().sendWalkableInterface(-1);
			player.getPacketSender().sendInteractionOption("null", 2, true);
			player.getPacketSender().sendWalkableInterface(-1);
			player.setWildernessLevel(0);
			BountyHunter.PLAYERS_IN_WILD.remove(player);
		}
	}

	@Override
	public void process(Mobile character) {
		if (character.isPlayer()) {
			Player player = character.getAsPlayer();
			player.setWildernessLevel(getLevel(player.getLocation().getY()));
			player.getPacketSender().sendString(199, "Level: " + player.getWildernessLevel());
		}
	}

	@Override
	public boolean canTeleport(Player player) {
		if (player.getWildernessLevel() > 20 && player.getRights() != PlayerRights.DEVELOPER) {
			player.getPacketSender().sendMessage("Teleport spells are blocked in this level of Wilderness.");
			player.getPacketSender()
					.sendMessage("You must be below level 20 of Wilderness to use teleportation spells.");
			return false;
		}
		return true;
	}

	@Override
	public CanAttackResponse canAttack(Mobile attacker, Mobile target) {
		if (attacker.isPlayer() && target.isPlayer()) {

			Player a = attacker.getAsPlayer();
			Player t = target.getAsPlayer();

			int combatDifference = CombatFactory.combatLevelDifference(a.getSkillManager().getCombatLevel(),
					t.getSkillManager().getCombatLevel());
			if (combatDifference > a.getWildernessLevel() + 5 || combatDifference > t.getWildernessLevel() + 5) {
				return CanAttackResponse.LEVEL_DIFFERENCE_TOO_GREAT;
			}
			if (!(t.getArea() instanceof WildernessArea)) {
				return CanAttackResponse.CANT_ATTACK_IN_AREA;
			}
		}

		return CanAttackResponse.CAN_ATTACK;
	}

	@Override
	public boolean canTrade(Player player, Player target) {
		return true;
	}

	@Override
	public boolean isMulti(Mobile character) {
		int x = character.getLocation().getX();
		int y = character.getLocation().getY();
		return multi(x, y);
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
		if (character.isPlayer()) {
			BountyHunter.onDeath(player, character.getAsPlayer(), true, 50);
		}
	}
	
    @Override
    public boolean overridesNpcAggressionTolerance(Player player, int npcId) {
        return true;
    }

	@Override
	public boolean handleObjectClick(Player player, GameObject object, int type) {
		if (Obelisks.activate(object.getId())) {
			return true;
		}
		return false;
	}

	@Override
	public boolean canPlayerBotIdle(PlayerBot playerBot) {
		// Player Bots can always idle in the Wilderness
		return true;
	}
}
