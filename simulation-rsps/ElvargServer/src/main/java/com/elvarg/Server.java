package com.elvarg;

import com.elvarg.game.GameBuilder;
import com.elvarg.game.GameConstants;
import com.elvarg.game.event.EventDispatcher;
import com.elvarg.game.event.events.ServerStartedEvent;
import com.elvarg.game.plugin.PluginLoader;
import com.elvarg.net.NetworkBuilder;
import com.elvarg.net.NetworkConstants;
import com.elvarg.util.ShutdownHook;
import com.elvarg.util.flood.Flooder;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The starting point of the application. Initializes the bootstrap.
 *
 * @author Professor Oak
 * @author Lare96
 */
public class Server {

    /**
     * The flooder used to stress-test the server.
     */
    private static final Flooder flooder = new Flooder();

    /**
     * Is the server running in production mode?
     */
    public static boolean PRODUCTION = false;

    /**
     * Enable various debugging logs?
     */
    public static boolean DEBUG_LOGGING = false;

    /**
     * The logger that will print important information.
     */
    private static Logger logger = Logger.getLogger(Server.class.getSimpleName());

    /**
     * The flag that determines if the server is currently being updated or not.
     */
    private static boolean updating = false;

    /**
     * The main method that will put the server online.
     */
    public static void main(String[] args) {
        System.setProperty("org.slf4j.simpleLogger.showDateTime", "true");
        System.setProperty("org.slf4j.simpleLogger.dateTimeFormat", "[yyyy-MM-dd'T'HH:mm:ss.SSSZ]");
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "info");
        try {
            Runtime.getRuntime().addShutdownHook(new ShutdownHook());
            new PluginLoader().load();

            if (args.length == 1) {
                PRODUCTION = Integer.parseInt(args[0]) == 1;
            }

            logger.info("Initializing " + GameConstants.NAME + " in " + (PRODUCTION ? "production" : "non-production") + " mode..");
            new GameBuilder().initialize();
            new NetworkBuilder().initialize(NetworkConstants.GAME_PORT);
            EventDispatcher.getGlobal().dispatch(new ServerStartedEvent());
            logger.info(GameConstants.NAME + " is now online!");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "An error occurred while binding the Bootstrap!", e);

            // No point in continuing server startup when the
            // bootstrap either failed to bind or was bound
            // incorrectly.
            System.exit(1);
        }
    }

    public static void logDebug(String logMessage) {
        if (!DEBUG_LOGGING) {
            return;
        }

        getLogger().info(logMessage);
    }

    public static Logger getLogger() {
        return logger;
    }

    public static boolean isUpdating() {
        return updating;
    }

    public static void setUpdating(boolean isUpdating) {
        Server.updating = isUpdating;
    }

    public static Flooder getFlooder() {
        return flooder;
    }
}