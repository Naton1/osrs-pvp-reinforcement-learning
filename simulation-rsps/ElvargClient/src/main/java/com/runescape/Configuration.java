package com.runescape;

/**
 * The main configuration for the Client
 *
 * @author Seven
 */
public final class Configuration {

    public static final int UPDATE_SERVER_PORT = 43580;
    public static final int CACHE_VERSION = 2;
    public static final int UPDATE_SERVER_VERSION = 1;
    public static final int UID = 8784521;
    public static final String CACHE_DIRECTORY = "./Cache/"; //System.getProperty("user.home") + File.separator + "OSRSPKV"+CLIENT_VERSION+"/";
    /**
     * Toggles a security feature called RSA to prevent packet sniffers
     */
    public static final boolean ENABLE_RSA = true;
    /**
     * A string which indicates the Client's name.
     */
    public static final String CLIENT_NAME = "Elvarg";
    /**
     * npcBits can be changed to what your server's bits are set to.
     */
    public static final int npcBits = 14;
    /**
     * Sends client-related debug messages to the client output stream
     */
    public static boolean PRODUCTION_MODE = false;
    public static String SERVER_ADDRESS = PRODUCTION_MODE ? "" : "localhost";
    public static int SERVER_PORT = 43595;
    /**
     * Dumps map region images when new regions are loaded.
     */
    public static boolean dumpMapRegions = false;

    /**
     * Displays fps and memory
     */
    public static boolean displayFps = false;

    /**
     * Displays debug information
     */
    public static boolean clientData = false;

    /**
     * Enables the use of music played through the client
     */
    public static boolean enableMusic = true;

    /**
     * Toggles the ability for a player to see roofs in-game
     */
    public static boolean enableRoofs = false;

    /**
     * Displays a hover menu tooltip over mouse
     */
    public static boolean enableTooltipHovers = false;

    /**
     * Used to repack indexes Index 1 = Models Index 2 = Animations Index 3 =
     * Sounds/Music Index 4 = Maps
     */
    public static boolean repackIndexOne = false, repackIndexTwo = false, repackIndexThree = false, repackIndexFour = false;

    /**
     * Dump Indexes Index 1 = Models Index 2 = Animations Index 3 = Sounds/Music
     * Index 4 = Maps
     */
    public static boolean dumpIndexOne = false, dumpIndexTwo = false, dumpIndexThree = false, dumpIndexFour = false;

    /**
     * Enables exp counter
     */
    public static boolean expCounterOpen = true;
    public static boolean mergeExpDrops = true;

    /**
     * Enables fog effects
     */
    public static boolean enableFog = true;

    /**
     * Does the escape key close current interface?
     */
    public static boolean escapeCloseInterface = false;

    /**
     * Enables/Disables Revision 554 hitmarks
     */
    public static boolean hitmarks554 = false;
    /**
     * Enables the use of run energy
     */
    public static boolean runEnergy = false;
    /**
     * Displays health above entities heads
     */
    public static boolean hpAboveHeads = false;
    /**
     * Displays names above entities
     */
    public static boolean namesAboveHeads = false;
    /**
     * Displays orbs on HUD
     */
    public static boolean enableOrbs = true;
    /**
     * Displays spec orb on HUD
     */
    public static boolean enableSpecOrb = true;
    /**
     * Enables/Disables Revision 554 health bar
     */
    public static boolean hpBar554 = false;
    /**
     * Enables the HUD to display 10 X the amount of hitpoints
     */
    public static boolean tenXHp = false;
    /**
     * Attack option priorities
     * 0 -> Depends on combat level
     * 1 -> Always right-click
     * 2 -> Left-click where available
     * 3 -> Hidden
     */
    public static int playerAttackOptionPriority = 0;
    public static int npcAttackOptionPriority = 2;
    /**
     * Is the combat overlay box enabled?
     */
    public static boolean combatOverlayBox = true;
    /**
     * Enables bounty hunter interface
     */
    public static boolean bountyHunterInterface = true;
    /**
     * Enables names above ground items
     */
    public static boolean enableGroundItemNames = true;
    /**
     * Enables one-click dropping of items while holding shift
     */
    public static boolean enableShiftClickDrop = true;
    /**
     * Enables skill orbs
     */
    public static boolean enableSkillOrbs = false;
    /**
     * Enables buff overlay
     */
    public static boolean enableBuffOverlay = true;

    private Configuration() {

    }

    public static class DiscordConfiguration {
        public static final boolean ENABLE_DISCORD_OAUTH_LOGIN = true;
        public static final String CLIENT_ID = "1010001099815669811";
        public static final String REDIRECT_URL = "https://rsps.app";
    }
}
