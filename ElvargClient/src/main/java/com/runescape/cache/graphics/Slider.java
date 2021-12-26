package com.runescape.cache.graphics;

import com.runescape.Client;
import com.runescape.cache.graphics.sprite.Sprite;
import com.runescape.cache.graphics.widget.Widget;
import com.runescape.draw.Rasterizer3D;

public class Slider {

    public final static int ZOOM = 1;
    public final static int BRIGHTNESS = 2;
    public final static int MUSIC = 3;
    public final static int SOUND = 4;
    private final double minValue, maxValue, length;
    private final Sprite[] images = new Sprite[2];
    private int position = 86;
    private double value;
    private int x, y;

    public Slider(Sprite icon, Sprite background, double minimumValue, double maximumValue) {
        this.images[0] = icon;
        this.images[1] = background;
        this.minValue = this.value = minimumValue;
        this.maxValue = maximumValue;
        this.length = this.images[1].myWidth;
    }

    public static void handleSlider(int mX, int mY) {

        int tabInterfaceId = Client.tabInterfaceIDs[Client.tabId];

        if (tabInterfaceId != -1) {

            if (tabInterfaceId == 42500) {
                tabInterfaceId = Widget.interfaceCache[42500].children[9];
            } // Settings tab adjustment
            Widget widget = Widget.interfaceCache[tabInterfaceId];

            if (widget == null ||
                    widget.children == null) {
                return;
            }

            for (int childId : widget.children) {
                Widget child = Widget.interfaceCache[childId];
                if (child == null || child.slider == null)
                    continue;
                child.slider.handleClick(mX, mY, Client.frameMode == Client.ScreenMode.FIXED ? 519 : 0, Client.frameMode == Client.ScreenMode.FIXED ? 168 : 0, child.contentType);
            }
            Client.tabAreaAltered = true;
        }

        int interfaceId = Client.openInterfaceId;
        if (interfaceId != -1) {
            Widget widget = Widget.interfaceCache[interfaceId];
            if (widget == null ||
                    widget.children == null) {
                return;
            }
            for (int childId : widget.children) {
                Widget child = Widget.interfaceCache[childId];
                if (child == null || child.slider == null)
                    continue;
                child.slider.handleClick(mX, mY, 4, 4, child.contentType);
            }
        }
    }

    public void draw(int x, int y) {
        this.x = x;
        this.y = y;
        images[1].drawSprite(x, y);
        images[0].drawSprite(x + position - (int) (position / length * images[0].myWidth), y - images[0].myHeight / 2 + images[1].myHeight / 2);
    }

    public void handleClick(int mouseX, int mouseY, int offsetX, int offsetY, int contentType) {
        int mX = Client.instance.mouseX;
        int mY = Client.instance.mouseY;
        if (mX - offsetX >= x && mX - offsetX <= x + length
                && mY - offsetY >= y + images[1].myHeight / 2 - images[0].myHeight / 2
                && mY - offsetY <= y + images[1].myHeight / 2 + images[0].myHeight / 2) {
            position = mouseX - x - offsetX;
            if (position >= length) {
                position = (int) length;
            }
            if (position <= 0) {
                position = 0;
            }
            value = minValue + (position / length) * (maxValue - minValue);
            if (value < minValue) {
                value = minValue;
            }
            if (value > maxValue) {
                value = maxValue;
            }
            handleContent(contentType);
        }
    }

    private void handleContent(int contentType) {
        switch (contentType) {
            case ZOOM:
                Client.cameraZoom = (int) (minValue + maxValue - value);
                break;
            case BRIGHTNESS:
                Client.brightnessState = minValue + maxValue - value;
                Rasterizer3D.setBrightness(minValue + maxValue - value);
                break;
            case MUSIC:
                Client.instance.changeMusicVolume((int) (minValue + maxValue - value));
                break;
            case SOUND:
                break;
        }
        Client.instance.savePlayerData();
    }

    public double getPercentage() {
        return ((position / length) * 100);
    }

    public void setValue(double value) {

        if (value < minValue) {
            value = minValue;
        } else if (value > maxValue) {
            value = maxValue;
        }

        this.value = value;
        double shift = 1 - ((value - minValue) / (maxValue - minValue));

        position = (int) (length * shift);
    }
}