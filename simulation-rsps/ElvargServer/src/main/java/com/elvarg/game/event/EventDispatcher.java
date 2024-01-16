package com.elvarg.game.event;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class EventDispatcher {

	private static final EventDispatcher GLOBAL = new EventDispatcher();

	public static EventDispatcher getGlobal() {
		return GLOBAL;
	}

	private final Map<Class<? extends Event>, List<EventListener<? extends Event>>> listeners =
			new ConcurrentHashMap<>();

	public <T extends Event> void add(Class<T> eventType, EventListener<T> listener) {
		getListeners(eventType).add(listener);
	}

	public <T extends Event> void remove(Class<T> eventType, EventListener<T> listener) {
		getListeners(eventType).remove(listener);
	}

	public <T extends Event> void dispatch(T event) {
		//noinspection unchecked
		for (EventListener<T> t : getListeners((Class<T>) event.getClass())) {
			try {
				t.onEvent(event);
			}
			catch (Exception e) {
				e.printStackTrace();
				throw e;
			}
		}
	}

	private <T extends Event> List<EventListener<T>> getListeners(Class<T> eventType) {
		// It will always be of the correct type, casting to make compiler happy
		//noinspection unchecked
		return (List<EventListener<T>>) (List<?>) this.listeners.computeIfAbsent(eventType,
		                                                                         v -> new CopyOnWriteArrayList<>());
	}

}
