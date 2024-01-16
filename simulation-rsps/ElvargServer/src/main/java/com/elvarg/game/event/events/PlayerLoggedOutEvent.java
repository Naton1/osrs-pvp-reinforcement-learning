package com.elvarg.game.event.events;

import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.event.Event;

public class PlayerLoggedOutEvent implements Event {

	private final Player player;

	public PlayerLoggedOutEvent(Player player) {
		this.player = player;
	}

	public Player getPlayer() {
		return player;
	}

}
