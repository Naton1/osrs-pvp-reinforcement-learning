package com.runescape.util;

import java.awt.Toolkit;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;

public final class SystemUtils {

    private static final String USER_HOME = System.getProperty("user.home");
    private static final String OS_NAME = System.getProperty("os.name");
    private static final String OS_ARCH = System.getProperty("os.arch");
    private static final String OS_VERSION = System.getProperty("os.version");
    private static final int CORES = Runtime.getRuntime().availableProcessors();
    /**
     * The MAC address.
     */
    private static String macAddress;

    public static String getUserHome() {
        return USER_HOME;
    }

    public static String getOsName() {
        return OS_NAME;
    }

    public static String getOsArch() {
        return OS_ARCH;
    }

    public static String getOsVersion() {
        return OS_VERSION;
    }

    public static int getCores() {
        return CORES;
    }

    public static boolean isMac() {
        return OS_NAME.toLowerCase().contains("mac") || OS_NAME.startsWith("darwin");
    }

    public static boolean isWindows() {
        return OS_NAME.toLowerCase().contains("windows");
    }

    public static boolean isLinux() {
        return OS_NAME.toLowerCase().contains("linux");
    }

    public static boolean isUnix() {
        final String os = OS_NAME.toLowerCase();
        if ((os.indexOf("sunos") >= 0) || (os.indexOf("linux") >= 0)) {
            return true;
        }
        return isMac() && (System.getProperty("os.version", "").startsWith("10."));
    }

    public static boolean isSolaris() {
        final String os = OS_NAME.toLowerCase();
        return os.indexOf("sunos") >= 0;
    }

    public static boolean isAix() {
        return OS_NAME.toLowerCase().indexOf("aix") >= 0;
    }

    /**
     * Returns whether or not the system has a retina display.
     *
     * @return
     */
    public static boolean hasRetinaDisplay() {
        if (!isMac()) {
            return false;
        }
        Object obj = Toolkit.getDefaultToolkit().getDesktopProperty("apple.awt.contentScaleFactor");
        if (obj instanceof Float) {
            Float f = (Float) obj;
            int scale = f.intValue();
            return (scale == 2);
        }
        return false;
    }

    /**
     * @return the fetched MAC address.
     */
    public static String getMacAddress() {
        if (macAddress == null) {
            try {
                fetchMacAddress();
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (SocketException e) {
                e.printStackTrace();
            }
            if (macAddress == null) {
                macAddress = "unknown";
            }
        }
        return macAddress;
    }

    /**
     * Fetch the MAC address.
     *
     * @throws UnknownHostException
     * @throws SocketException
     */
    private static void fetchMacAddress() throws UnknownHostException, SocketException {
        InetAddress ip = InetAddress.getLocalHost();

        if (ip == null) {
            macAddress = "NO-MA-C0-00-00-00";
            return;
        }

        NetworkInterface network = NetworkInterface.getByInetAddress(ip);

        if (network == null) {
            macAddress = "NO-MA-C0-00-00-00";
            return;
        }

        byte[] mac = network.getHardwareAddress();

        if (mac == null) {
            macAddress = "NO-MA-C0-00-00-00";
            return;
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < mac.length; i++) {
            sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));
        }
        macAddress = sb.toString();
    }

    /**
     * Get Total Memory available to JVM.
     *
     * @return Numeric (long) value of count memory in Bytes.
     */
    public static long getTotalMem() {
        return (Runtime.getRuntime().totalMemory());
    }

    /**
     * Maximum memory used by JVM during runtime in Bytes.
     *
     * @return Numeric (long) value of max memory in Bytes.
     */
    public static long getMaxMem() {
        return (Runtime.getRuntime().maxMemory());
    }

    /**
     * Get free memory.
     *
     * @return Numeric (long) value of free memory in Bytes.
     */
    public static long getFreeMem() {
        return (Runtime.getRuntime().freeMemory());
    }

    /**
     * Gets a count of processors installed on the system.
     *
     * @return String value of computer processor count.
     */
    public static String getCoresToString() {
        String[] prefix = {"Single", "Dual", "Triple", "Quad", "Penta", "Hexa", "Hepta", "Octa", "Nona", "Deca"};
        return prefix[getCores() - 1] + "-Core";
    }

}