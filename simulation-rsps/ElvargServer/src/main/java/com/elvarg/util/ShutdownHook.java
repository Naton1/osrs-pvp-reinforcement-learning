package com.elvarg.util;

import com.elvarg.game.World;

import java.util.logging.Logger;

public class ShutdownHook extends Thread {

    /**
     * The ShutdownHook logger to print out information.
     */
    private static final Logger logger = Logger.getLogger(ShutdownHook.class.getName());

    @Override
    public void run() {
        logger.info("The shutdown hook is processing all required actions...");
        World.savePlayers();
        logger.info("The shudown hook actions have been completed, shutting the server down...");
    }
}
