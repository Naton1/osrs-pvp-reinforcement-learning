package com.runescape.sign;

import com.runescape.Configuration;

import java.applet.Applet;
import java.awt.EventQueue;
import java.awt.Toolkit;
import java.io.File;
import java.io.RandomAccessFile;

public final class SignLink {

    public static final RandomAccessFile[] indices = new RandomAccessFile[5];
    public static RandomAccessFile cache_dat = null;
    public static Applet mainapp = null;
    public static String os;
    public static String arch;
    public static EventQueue eventQueue;
    
    private SignLink() {
    }

    public static void init(Applet px) {

        System.setProperty("java.net.preferIPv4Stack", "true");

        mainapp = px;
       
        String directory = findcachedir();
        try {

            cache_dat = new RandomAccessFile(directory + "main_file_cache.dat", "rw");
            for (int index = 0; index < 5; index++) {
                indices[index] = new RandomAccessFile(directory + "main_file_cache.idx"
                        + index, "rw");
            }


        } catch (Exception exception) {
            exception.printStackTrace();
        }
        
        try {
            eventQueue = Toolkit.getDefaultToolkit().getSystemEventQueue();
        } catch (Throwable t) {
        }
        try {
            ThreadGroup t = Thread.currentThread().getThreadGroup();
            do {
                ThreadGroup t1 = t.getParent();
                if (t1 == null)
                    break;

                t = t1;
            } while (true);
            int n = t.activeCount();
            if (n > 0) {
                Thread[] h = new Thread[n];
                n = t.enumerate(h);
                if (n > 0)
                    for (int n1 = 0; n1 != n; ++n1) {
                        Thread r = h[n1];
                        if (r == null)
                            continue;

                        try {
                            String s = r.getName();
                            if (s != null && s.startsWith("AWT"))
                                r.setPriority(1);
                        } catch (Throwable w) {
                        }
                    }
            }
        } catch (Throwable t) {
        }
        os = null;
        try {
            os = System.getProperty("os.name").toLowerCase();
        } catch (Throwable ex) {
        }
        arch = null;
        try {
            arch = System.getProperty("os.arch").toLowerCase();
        } catch (Throwable ex) {
        }
    }

    public static String findcachedir() {
        final File cacheDirectory = new File(Configuration.CACHE_DIRECTORY);
        if (!cacheDirectory.exists())
            cacheDirectory.mkdir();
        return Configuration.CACHE_DIRECTORY;
    }
}
