package com.runescape.draw.skillorbs;

import com.runescape.Client;
import com.runescape.cache.graphics.sprite.Sprite;
import com.runescape.draw.Rasterizer2D;
import com.runescape.util.SecondsTimer;
import com.runescape.util.SkillConstants;

import java.awt.*;
import java.text.NumberFormat;

/**
 * Represents a skill orb.
 *
 * @author Ghost
 * @author Professor Oak
 * @author Christian_
 */
public class SkillOrb {

    /**
     * The skill this orb is intended for.
     */
    private final int skill;

    /**
     * The sprite icon for this skill orb.
     */
    private final Sprite icon;

    /**
     * The show timer. Resets when this orb
     * receives experience.
     */
    private SecondsTimer showTimer = new SecondsTimer();

    /**
     * The orb's current alpha (transparency)
     */
    private int alpha;

    /**
     * The angle representing XP progress, in degrees
     */
    private int progressAngle = 0;

    /**
     * The change in angle since last draw() call
     */
    private int progressChange = 0;

    /**
     * The angle stepper. Used to achieve a smooth increase transition on XP gain
     */
    private int angleStepper = 0;

    /**
     * Time since draw() was last called
     */
    private long lastDraw = 0;

    /**
     * Constructs this skill orb
     *
     * @param skill
     */
    public SkillOrb(int skill, Sprite icon) {
        this.skill = skill;
        this.icon = icon;
    }

    /**
     * Called upon the player receiving experience.
     * <p>
     * Resets the attributes of the orb
     * to make sure the orb is drawn
     * properly.
     */
    public void receivedExperience() {
        alpha = 255;
        showTimer.start(12);
    }

    /**
     * Draws this skill orb
     *
     * @param x
     * @param y
     */
    public void draw(int x, int y) {

        if (alpha < 0) {
            alpha = 0;
        }

        // Draw inner semi-transparent circle
        Rasterizer2D.drawFilledCircle(x + 28, y + 28, 24, 0x504a41, alpha < 180 ? alpha : 180);

        calculateAngleStep();

        Rasterizer2D.createGraphics(true);
        Shape xpRing = createRing(x, y, 360);
        Shape progressRing = createRing(x, y, angleStepper);

        lastDraw = System.currentTimeMillis();

        drawRing(xpRing, new Color(22, 21, 16, alpha));
        drawRing(progressRing, getColour());

        icon.drawAdvancedSprite(x + 29 - icon.myWidth / 2, 28 - icon.myHeight / 2 + y, alpha);
    }

    /**
     * Create a Ring Shape objet
     *
     * @param x
     * @param y
     * @param angle
     * @return
     */

    public Shape createRing(int x, int y, int angle) {
        Shape sector = Rasterizer2D.createSector(x + 2, y + 1, 55, angle);
        Shape innerCircle = Rasterizer2D.createCircle(x + 7, y + 6, 45);

        return Rasterizer2D.createRing(sector, innerCircle);
    }

    /**
     * Draw the ring of specified colour
     *
     * @param ring
     * @param colour
     */
    public void drawRing(Shape ring, Color colour) {
        SkillOrbs.g2d.setColor(colour);
        SkillOrbs.g2d.fill(ring);
    }

    /**
     * Calculate the angle step, in order to achieve a smooth increase transition on XP gain
     */
    private void calculateAngleStep() {
        progressAngle = (int) ((percentage() / 100.0) * 360);

        if (System.currentTimeMillis() - lastDraw < 600) {
            if (progressChange <= 0) {
                progressChange = progressAngle - angleStepper;
            }
        }

        boolean levelUp = false;
        if (progressAngle < angleStepper) {
            levelUp = true;
        }

        if (progressChange < 360 && ((progressChange > 0 && angleStepper <= progressAngle - 2) || levelUp)) {
            angleStepper += 2; // TODO need to altar for larger xp drops? base on progressChange size
            progressChange--;
        } else {
            progressChange = 0;
            angleStepper = progressAngle;
        }

        if (angleStepper > 360) {
            angleStepper = progressAngle;
        }
    }

    /**
     * Gets progress bar colour
     *
     * @return
     */
    private Color getColour() {
        return currentLevel() >= 99 ? new Color(255, 165, 0, alpha) : new Color(232, 232, 232, alpha);
    }

    /**
     * Draws a tooltip containing information about
     * this skill orb.
     */
    public void drawTooltip() {

        final int percentProgress = percentage();
        NumberFormat nf = NumberFormat.getInstance();
        int mouse_Y = Client.instance.mouseX;
        int mouseY = Client.instance.mouseY;

		/* Draw box */
        Rasterizer2D.drawTransparentBox(mouse_Y + 1, mouseY + 6, 122, 83, 0x504a41, 180);
        Rasterizer2D.drawBoxOutline(mouse_Y + 1, mouseY + 6, 122, 83, 0x383023);

		/* Draw stat information */
        String skillName = SkillConstants.SKILL_NAMES_ORDER[skill];
        Client.instance.newSmallFont.drawCenteredString(skillName, mouse_Y + 122 / 2, mouseY + 20, 16777215, 1);

        String[] labels = new String[]{"Level:", "Exp:", "Exp left:"};
        String[] info = new String[]{Integer.toString(Client.instance.maximumLevels[skill]), nf.format(Client.instance.currentExp[skill]), nf.format(remainderExp())};
        int y = 35;

        for (int i = 0; i < 3; i++, y += 15) {
            Client.instance.newSmallFont.drawBasicString(labels[i], mouse_Y + 5, mouseY + y, 16777215, 1);
            Client.instance.newSmallFont.drawRightAlignedString(info[i], mouse_Y + 117, mouseY + y, 0x00ff00, 1);
        }

		/* Draw progress bar */
        Rasterizer2D.drawTransparentBox(mouse_Y + 3, mouseY + 70, 118, 14, 0x2c2720, 180);
        Rasterizer2D.drawBox(mouse_Y + 3, mouseY + 70, (int) ((percentProgress / 100.0) * 118), 14, Client.getProgressColor(percentProgress));

        Client.instance.newSmallFont.drawCenteredString(percentProgress + "% ", mouse_Y + 118 / 2 + 10, mouseY + 82, 0xFFFFFF, 1);
    }

    private int currentLevel() {
        return Client.instance.maximumLevels[skill];
    }

    private int startExp() {
        return Client.getXPForLevel(currentLevel());
    }

    private int requiredExp() {
        return Client.getXPForLevel(currentLevel() + 1);
    }

    private int obtainedExp() {
        return Client.instance.currentExp[skill] - startExp();
    }

    private int remainderExp() {
        if (currentLevel() < 99) {
            return requiredExp() - (startExp() + obtainedExp());
        } else {
            return 200_000_000 - Client.instance.currentExp[skill];
        }
    }

    private int percentage() {
        // Attempt to calculate percent progress
        int percent = 0;
        try {
            if (currentLevel() < 99) {
                percent = (int) (((double) obtainedExp() / (double) (requiredExp() - startExp())) * 100);
            } else {
                percent = (int) (((double) Client.instance.currentExp[skill] / 200_000_000) * 100);
            }
            // Max percent progress is 100
            if (percent > 100) {
                percent = 100;
            }
        } catch (ArithmeticException e) {
            e.printStackTrace();
        }
        return percent;
    }

    /**
     * Gets the timer
     *
     * @return
     */
    public SecondsTimer getShowTimer() {
        return showTimer;
    }

    /**
     * Gets the skill
     *
     * @return
     */
    public int getSkill() {
        return skill;
    }

    /**
     * Gets the alpha
     *
     * @return
     */
    public int getAlpha() {
        return alpha;
    }

    /**
     * Decrements alpha
     */
    public void decrementAlpha() {
        alpha -= 10;
    }
}
