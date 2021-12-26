package com.elvarg.game.content.skill.slayer;

public class ActiveSlayerTask {

    private final SlayerMaster master;
    private final SlayerTask task;
    private int remaining;
    
    ActiveSlayerTask(SlayerMaster master, SlayerTask task, int amount) {
        this.master = master;
        this.task = task;
        this.remaining = amount;
    }
    
    public SlayerMaster getMaster() {
        return master;
    }
    
    public SlayerTask getTask() {
        return task;
    }
    
    public void setRemaining(int amount) {
        this.remaining = amount;
    }

    public int getRemaining() {
        return remaining;
    }
}
