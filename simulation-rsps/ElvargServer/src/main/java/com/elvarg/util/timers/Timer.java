package com.elvarg.util.timers;

/**
 * Created by Bart on 8/12/2015.
 */
public class Timer {

	private TimerKey key;
	private int ticks;
	private int uncappedTicks;

	public Timer(TimerKey key, int ticks) {
		this.key = key;
		this.ticks = ticks;
		this.uncappedTicks = ticks;
	}

	public int ticks() {
		return ticks;
	}

	public int uncappedTicks() {
		return uncappedTicks;
	}

	public TimerKey key() {
		return key;
	}

	public void tick() {
		uncappedTicks--;
		if (ticks > 0)
			ticks--;
	}

}
