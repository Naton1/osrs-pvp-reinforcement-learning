package com.elvarg.game.entity.updating.sync;

import com.elvarg.game.World;

/**
 * A synchronization task executed under a {@link GameSyncExecutor}. The
 * character instance associated to {@code index} must be the mutex of a
 * synchronization block wrapped around the code.
 *
 * @author lare96 <http://github.org/lare96>
 */
public abstract class GameSyncTask {
    
    private final boolean players;
    private final boolean concurrent;

    public GameSyncTask(boolean players, boolean concurrent) {
        this.players = players;
        this.concurrent = concurrent;
    }
    
    public GameSyncTask(boolean players) {
        this(players, true);
    }
    
    public abstract void execute(final int index);

    public boolean checkIndex(int index) {
        return (players ? World.getPlayers().get(index) != null : World.getNpcs().get(index) != null);
    }

    public int getAmount() {
        return (players ? World.getPlayers().size() : World.getNpcs().size());
    }
    
    public int getCapacity() {
        return (players ? World.getPlayers().capacity() : World.getNpcs().capacity());
    }

    public boolean isConcurrent() {
        return concurrent;
    }
}
