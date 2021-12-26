package com.elvarg.game.definition.loader;

import com.elvarg.Server;

import java.util.logging.Level;

/**
 * An abstract class which handles the loading
 * of some sort of definition-related file.
 *
 * @author Professor Oak
 */
public abstract class DefinitionLoader implements Runnable {

    public abstract void load() throws Throwable;
    public abstract String file();

    @Override
    public void run() {
        try {
            long start = System.currentTimeMillis();
            load();
            long elapsed = System.currentTimeMillis() - start;
            Server.getLogger().log(Level.INFO, "Loaded definitions for: " + file() + ". It took " + elapsed + " milliseconds.");
        } catch (Throwable e) {
            e.printStackTrace();
            Server.getLogger().log(Level.SEVERE, "Error loading definitions for: " + file(), e);
        }
    }
}
