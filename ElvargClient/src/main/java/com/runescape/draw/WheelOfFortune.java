package com.runescape.draw;

import java.awt.Color;
import java.awt.Graphics2D;

public class WheelOfFortune {

    private Graphics2D g2d;
    private boolean spinning;
    private double rotation = 15;

    public void setSpinning(boolean spinning) {
        this.spinning = spinning;
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

}
