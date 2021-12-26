package com.elvarg.game.entity.updating.sync;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Phaser;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy;

import com.elvarg.game.GameConstants;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

/**
 * A synchronization executor that executes {@link GameSyncTask}s. These have
 * support for both concurrent and sequential synchronization tasks, and are
 * smart enough to determine when each should be used on a task-to-task basis.
 *
 * @author lare96 <http://github.org/lare96>
 */
public final class GameSyncExecutor {

    /**
     * The executor that will execute the synchronization tasks. This value may
     * or may not be {@code null}.
     */
    private final ExecutorService service;

    /**
     * The synchronizer that ensures that the thread waits until tasks are
     * completed before proceeding. This value may or may not be {@code null}.
     */
    private final Phaser phaser;

    /**
     * Creates a new {@link GameSyncExecutor}. It automatically determines how
     * many threads; if any, are needed for game synchronization.
     */
    public GameSyncExecutor() {
        this.service = GameConstants.CONCURRENCY ? create(Runtime.getRuntime().availableProcessors()) : null;
        this.phaser = GameConstants.CONCURRENCY ? new Phaser(1) : null;
    }

    /**
     * Submits {@code syncTask} to be executed as a synchronization task under
     * this executor. This method can and probably will block the calling thread
     * until it completes.
     *
     * @param syncTask the synchronization task to execute.
     */
    public void sync(GameSyncTask syncTask) {
        if (service == null || phaser == null || !syncTask.isConcurrent()) {
            for (int index = 1; index < syncTask.getCapacity(); index++) {
                if (!syncTask.checkIndex(index)) {
                    continue;
                }
                syncTask.execute(index);
            }
            return;
        }

        phaser.bulkRegister(syncTask.getAmount());
        for (int index = 1; index < syncTask.getCapacity(); index++) {
            if (!syncTask.checkIndex(index)) {
                continue;
            }
            final int finalIndex = index;
            service.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        syncTask.execute(finalIndex);
                    } finally {
                        phaser.arriveAndDeregister();
                    }
                }
            });
        }
        phaser.arriveAndAwaitAdvance();
    }

    /**
     * Creates and configures the update service for this game sync executor.
     * The returned executor is <b>unconfigurable</b> meaning it's configuration
     * can no longer be modified.
     *
     * @param nThreads the amount of threads to create this service.
     * @return the newly created and configured service.
     */
    private ExecutorService create(int nThreads) {
        if (nThreads <= 1)
            return null;
        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(nThreads);
        executor.setRejectedExecutionHandler(new CallerRunsPolicy());
        executor.setThreadFactory(new ThreadFactoryBuilder().setNameFormat("GameSyncThread").build());
        return Executors.unconfigurableExecutorService(executor);
    }
}
