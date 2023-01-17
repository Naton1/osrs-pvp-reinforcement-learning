package com.elvarg.game.content.clan;

import com.elvarg.game.model.SecondsTimer;

public class BannedMember {

    private SecondsTimer timer;
    private String name;

    public BannedMember(String name, int seconds) {
        this.setName(name);
        this.setTimer(new SecondsTimer(seconds).start());
    }

    public SecondsTimer getTimer() {
        return timer;
    }

    public void setTimer(SecondsTimer timer) {
        this.timer = timer;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
