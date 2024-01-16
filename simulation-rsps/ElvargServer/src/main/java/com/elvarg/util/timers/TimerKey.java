package com.elvarg.util.timers;

import com.elvarg.util.Misc;

public enum TimerKey {
	FOOD,
	KARAMBWAN,
	POTION,
	COMBAT_ATTACK,
	FREEZE,
	FREEZE_IMMUNITY,
	STUN,
	STUN_IMMUNITY,
	ATTACK_IMMUNITY,
	CASTLEWARS_TAKE_ITEM,
	STEPPING_OUT,
	BOT_WAIT_FOR_PLAYERS(Misc.getTicks(180 /* 3 minutes */)),
	STAT_CHANGE;

	private int ticks;

	TimerKey() {
	}

	TimerKey(int ticks) {
		this.ticks = ticks;
	}

	public int getTicks() {
		return this.ticks;
	}
}
