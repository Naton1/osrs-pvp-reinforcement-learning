package com.elvarg.util.timers;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by Bart on 8/12/2015.
 */
public class TimerRepository {

	private Map<TimerKey, Timer> timers = new EnumMap<>(TimerKey.class);

	public boolean has(TimerKey key) {
		Timer timer = timers.get(key);
		return timer != null && timer.ticks() > 0;
	}

	public void register(Timer timer) {
		timers.put(timer.key(), timer);
	}

	public int left(TimerKey key) {
		Timer timer = timers.get(key);
		return timer.ticks();
	}

	public boolean willEndIn(TimerKey key, int ticks) {
		Timer timer = timers.get(key);
		if(timer == null) {
			return true;
		}
		return timer.ticks() <= ticks;
	}

	public int getTicks(TimerKey key) {
		Timer timer = timers.get(key);
		if (timer == null) {
			return 0;
		}
		return timer.ticks();
	}

	public int getUncappedTicks(TimerKey key, int defaultValue) {
		Timer timer = timers.get(key);
		if (timer == null) {
			return defaultValue;
		}
		return timer.uncappedTicks();
	}

	public void register(TimerKey key, int ticks) {
		timers.put(key, new Timer(key, ticks));
	}

	/**
	 * Register a timer key which has its ticks defined on the Enum.
	 *
	 * @param key
	 */
	public void register(TimerKey key) {
		timers.put(key, new Timer(key, key.getTicks()));
	}

	/**
	 * Extend up to (if exists) the given ticks, or register new
	 */
	public void extendOrRegister(TimerKey key, int ticks) {
		timers.compute(key, (k, t) -> t == null || t.ticks() < ticks ? new Timer(key, ticks) : t);
	}

	/**
	 * Register if non-existant, or extend.
	 */
	public void addOrSet(TimerKey key, int ticks) {
		timers.compute(key, (k, t) -> t == null ? new Timer(key, ticks) : new Timer(key, t.ticks() + ticks));
	}

	public void cancel(TimerKey name) {
		timers.remove(name);
	}

	public void process() {
		if (!timers.isEmpty()) {
			Set<Map.Entry<TimerKey, Timer>> entries = timers.entrySet();
			for (Map.Entry<TimerKey, Timer> entry : entries) {
				entry.getValue().tick();
			}
		}
	}

	public Map<TimerKey, Timer> timers() {
		return timers;
	}

}