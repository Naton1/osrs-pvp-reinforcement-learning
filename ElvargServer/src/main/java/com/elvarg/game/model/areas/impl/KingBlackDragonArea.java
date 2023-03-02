package com.elvarg.game.model.areas.impl;

import java.util.Arrays;
import java.util.Optional;

import com.elvarg.game.entity.impl.Mobile;
import com.elvarg.game.entity.impl.object.GameObject;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.Boundary;
import com.elvarg.game.model.Location;
import com.elvarg.game.model.areas.Area;

public class KingBlackDragonArea extends Area {

	public static final Boundary BOUNDARY = new Boundary(2249, 2292, 4672, 4720);

	public KingBlackDragonArea() {
		super(Arrays.asList(BOUNDARY));
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
	}

	@Override
	public boolean handleObjectClick(Player player, GameObject object, int type) {
		return false;
	}
}
