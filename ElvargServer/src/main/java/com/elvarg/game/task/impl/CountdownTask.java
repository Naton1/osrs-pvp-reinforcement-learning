package com.elvarg.game.task.impl;

import com.elvarg.game.task.Task;

public class CountdownTask extends Task {

    Runnable onTick;
    Runnable onComplete;

    public CountdownTask(Object key, int ticks, Runnable onComplete) {
        super(ticks, key);
        this.onComplete = onComplete;
    }

    public CountdownTask(Object key, int ticks, Runnable onTick, Runnable onComplete) {
        super(ticks, key);
        this.onTick = onTick;
        this.onComplete = onComplete;
    }

    @Override
    public void onTick() {
        if (this.onTick != null) {
            this.onTick.run();
        }
    }

    @Override
    public void execute() {
        if (this.onComplete != null) {
            this.onComplete.run();
        }

        // Countdown task only runs once
        this.stop();
    }
}