package com.elvarg.game.model.areas.impl;

import java.util.Arrays;
import java.util.Optional;

import com.elvarg.game.entity.impl.Mobile;
import com.elvarg.game.entity.impl.npc.impl.GodwarsFollower;
import com.elvarg.game.entity.impl.object.GameObject;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.Boundary;
import com.elvarg.game.model.Location;
import com.elvarg.game.model.areas.Area;

public class GodwarsDungeonArea extends Area {

	public static final Boundary BOUNDARY = new Boundary(2800, 2950, 5200, 5400);

	public GodwarsDungeonArea() {
		super(Arrays.asList(BOUNDARY));
	}

	@Override
	public void postEnter(Mobile character) {
		if (character.isPlayer()) {
			Player player = character.getAsPlayer();
			updateInterface(player);
			player.getPacketSender().sendWalkableInterface(42569);
		}
	}

	@Override
	public void postLeave(Mobile character, boolean logout) {
		if (character.isPlayer()) {
			Player player = character.getAsPlayer();
			player.getPacketSender().sendWalkableInterface(-1);
			
			// Reset killcount
			for (int i = 0; i < player.getGodwarsKillcount().length; i++) {
				player.setGodwarsKillcount(i, 0);
			}
			player.getPacketSender().sendMessage("Your Godwars killcount has been reset.");
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
		return true;
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
		if (character instanceof GodwarsFollower) {
			GodwarsFollower gwdFoller = (GodwarsFollower) character;
			int index = gwdFoller.getGod().ordinal();
			int current = player.getGodwarsKillcount()[index];
			player.setGodwarsKillcount(index, current + 1);
			updateInterface(player);
		}
	}

	@Override
	public boolean handleObjectClick(Player player, GameObject object, int type) {
		return false;
	}
	
	private void updateInterface(Player player) {
		for (int i = 0; i < player.getGodwarsKillcount().length; i++) {
			player.getPacketSender().sendString(42575 + i, Integer.toString(player.getGodwarsKillcount()[i]));
		}
	}
}
