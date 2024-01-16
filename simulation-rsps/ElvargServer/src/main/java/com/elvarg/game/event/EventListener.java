package com.elvarg.game.event;

public interface EventListener<T extends Event> {

	void onEvent(T event);

}
