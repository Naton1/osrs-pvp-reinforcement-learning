package com.elvarg.game.event.events;

import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.event.Event;

public class PlayerPacketsProcessedEvent implements Event {

	private final Player player;

	public PlayerPacketsProcessedEvent(Player player) {
		this.player = player;
	}

	public Player getPlayer() {
		return player;
	}

}
