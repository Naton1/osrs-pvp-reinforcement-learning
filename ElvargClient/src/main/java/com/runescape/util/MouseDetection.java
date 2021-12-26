package com.runescape.util;

import com.runescape.Client;

public final class MouseDetection implements Runnable {

    public final Object syncObject;
    public final int[] coordsY;
    public final int[] coordsX;
    public boolean running;
    public int coordsIndex;
    private Client clientInstance;

    public MouseDetection(Client client1) {
        syncObject = new Object();
        coordsY = new int[500];
        running = true;
        coordsX = new int[500];
        clientInstance = client1;
    }

    public void run() {
        while (running) {
            synchronized (syncObject) {
                if (coordsIndex < 500) {
                    coordsX[coordsIndex] = clientInstance.mouseX;
                    coordsY[coordsIndex] = clientInstance.mouseY;
                    coordsIndex++;
                }
            }
            try {
                Thread.sleep(50L);
            } catch (Exception _ex) {
            }
        }
    }
}
