package com.runescape.draw;

import com.runescape.cache.def.ItemDefinition;
import com.runescape.cache.graphics.widget.Widget;

import java.awt.*;
import java.awt.geom.Arc2D;

public class WheelOfFortune {

    public static final int MINIMUM_CHILD = 5;
    double previousSpinRotation = 15;
    double quarterRotations = 0;
    private Graphics2D g2d;
    private boolean spinning;
    private double rotation = 15;

    public void setSpinning(boolean spinning) {
        this.spinning = spinning;
    }

    public void drawWheel(int x, int y) {

        g2d = Rasterizer2D.createGraphics(true);

        int size = 200;

        for (int i = 0; i < 12; i++) {

            // Fill circle sectors
            Shape sector = new Arc2D.Double(x, y, size, size, 75 - rotation, 30, Arc2D.PIE);
            g2d.setColor(getColour(i));
            g2d.fill(sector);

            // Draw sector borders
            g2d.setStroke(new BasicStroke(1));
            g2d.setColor(new Color(48, 43, 35));
            g2d.draw(sector);

            // Rotate
            g2d.rotate(Math.toRadians(30), x + (size / 2), y + (size / 2));
        }

        // Draw wheel pinpoint
        Rasterizer2D.drawFilledCircle(x + (size / 2), y + (size / 2), 3, 0x302b23, 254);

        /// Draw wheel border
        g2d.setStroke(new BasicStroke(2));
        g2d.drawOval(x - 1, y, size, size);
    }

    public void drawTicker(int x, int y) {
        g2d.fill(new Arc2D.Double(x - 2, y - 9, 20, 22, 0, 180, Arc2D.PIE));

        g2d.setColor(new Color(252, 211, 27));
        g2d.fill(new Arc2D.Double(x, y - 8, 16, 20, 0, 180, Arc2D.PIE));

        g2d.setColor(new Color(48, 43, 35));
        g2d.fillPolygon(new int[]{x + 18, x - 2, x + 8, x + 18}, new int[]{y, y, y + 24, y}, 4);

        g2d.setColor(new Color(252, 211, 27));
        g2d.fillPolygon(new int[]{x + 16, x, x + 8, x + 16}, new int[]{y, y, y + 22, y}, 4);

        g2d.dispose();
    }

    private Color getColour(int i) {
        switch (i) {
            case 0:
            case 3:
            case 5:
            case 9:
                return new Color(240, 141, 24);
            case 2:
            case 7:
                return new Color(110, 163, 19);
            default:
                return new Color(74, 69, 47);
        }
    }

    public void spin() {
        if (!spinning) {
            return;
        }

        int angle = 0;

        Widget tab = Widget.interfaceCache[37000];

        for (int i = 0; i < 12; i++) {
            double x = 352 + 75 * (Math.cos(Math.toRadians(angle + rotation)));
            double y = 187 + 75 * (Math.sin(Math.toRadians(angle + rotation)));

            tab.childX[MINIMUM_CHILD + i] = (int) x;
            tab.childY[MINIMUM_CHILD + i] = (int) y;
            angle += 30;
        }

        if (quarterRotations < 4) rotation += 10; // TODO condense this
        else if (quarterRotations < 5) rotation += 9;
        else if (quarterRotations < 6) rotation += 8;
        else if (quarterRotations < 7) rotation += 7;
        else if (quarterRotations < 8) rotation += 6;
        else if (quarterRotations < 9) rotation += 5;
        else if (quarterRotations < 10) rotation += 3;
        else if (quarterRotations < 11) rotation += 2;
        else if (quarterRotations < 12) rotation += 1;
        else if (rotation < (previousSpinRotation + 2000)) rotation += 1; // Set for first spin only and adjust TODO

        else {
            spinning = false;
            previousSpinRotation = rotation;
            quarterRotations = 0;

            // TODO move server side
            int widget = 37025;
            int scale = 200;
            int item = 13145;
            ItemDefinition definition = ItemDefinition.lookup(item);
            Widget.interfaceCache[widget].defaultMediaType = 4;
            Widget.interfaceCache[widget].defaultMedia = item;
            Widget.interfaceCache[widget].modelZoom = (definition.modelZoom * 100) / scale;
        }
        if ((rotation - previousSpinRotation) % 90 == 0.0 && spinning) quarterRotations++;
    }
}
