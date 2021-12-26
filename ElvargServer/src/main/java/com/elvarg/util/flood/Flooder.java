package com.elvarg.util.flood;

import com.elvarg.util.Misc;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

/**
 * An implementation of {@link Runnable} which creates
 * new clients and tries to connect them with the server.
 *
 * @author Professor Oak
 */
public class Flooder implements Runnable {

    /**
     * The clients that are currently active.
     * We can use this map to distinguish fake-clients
     * from real ones.
     */
    public final Map<String, Client> clients = new HashMap<String, Client>();

    /**
     * Is this flooder currently running?
     */
    private boolean running;

    /**
     * Starts this flooder if it hasn't
     * been started already.
     */
    public void start() {
        if (!running) {
            running = true;
            new Thread(this).start();
        }
    }

    /**
     * Stops this flooder.
     * <p>
     * Any logged in clients will eventually be disconnected
     * from the server automatically for being idle.
     */
    public void stop() {
        running = false;
    }

    /**
     * Attempts to login the amount of given clients.
     *
     * @param amount
     */
    public void login(int amount) {
        //Make sure we have started before logging in clients.
        start();

        //Attempt to login the amount of bots..
        synchronized (clients) {
            for (int i = 0; i < amount; i++) {
                try {
                    String username = "bot" + Integer.toString(clients.size());
                    String password = "bot";
                    new Client(Misc.formatText(username), password).attemptLogin();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void run() {
        while (running) {
            try {
                Iterator<Entry<String, Client>> i = clients.entrySet().iterator();
                while (i.hasNext()) {
                    Entry<String, Client> entry = i.next();
                    try {
                        entry.getValue().process();
                    } catch (Exception e) {
                        e.printStackTrace();
                        i.remove();
                    }
                }
                Thread.sleep(300);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
