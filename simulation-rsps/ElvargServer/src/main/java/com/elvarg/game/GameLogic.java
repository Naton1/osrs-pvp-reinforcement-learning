package com.elvarg.game;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy;
import java.util.concurrent.TimeUnit;

/**
 * A class used for handling operations that can be done
 * asynchronously.
 *
 * @author Professor Oak
 * @author Lare96
 */
public class GameLogic {

    /**
     * A logic service, used for carrying out
     * asynchronous tasks such as file-writing.
     */
    private static final ScheduledExecutorService logicService = createLogicService();

    /**
     * Submits a task to the logic service.
     *
     * @param t
     */
    public static void submit(Runnable t) {
        try {
            logicService.execute(t);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates the logic service.
     *
     * @return
     */
    public static ScheduledExecutorService createLogicService() {
        ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);
        executor.setRejectedExecutionHandler(new CallerRunsPolicy());
        executor.setThreadFactory(new ThreadFactoryBuilder().setNameFormat("LogicServiceThread").build());
        executor.setKeepAliveTime(45, TimeUnit.SECONDS);
        executor.allowCoreThreadTimeOut(true);
        return Executors.unconfigurableScheduledExecutorService(executor);
    }
}
