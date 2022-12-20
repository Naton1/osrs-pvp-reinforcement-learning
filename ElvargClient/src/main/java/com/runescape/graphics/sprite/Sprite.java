package com.runescape.graphics.sprite;

import com.runescape.cache.FileArchive;
import com.runescape.draw.Rasterizer2D;
import com.runescape.io.Buffer;
import com.runescape.sign.SignLink;

import javax.swing.ImageIcon;
import java.awt.Color;
import java.awt.Component;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.awt.image.ImageProducer;
import java.awt.image.PixelGrabber;
import java.awt.image.RGBImageFilter;

public final class Sprite extends Rasterizer2D {

    public static int SETTINGS_FIXED_ACTIVE = 185,
            SETTINGS_FIXED_INACTIVE = 186,
            SETTINGS_FIXED_INACTIVE_HOVER = 485,
            SETTINGS_RESIZABLE_ACTIVE = 484,
            SETTINGS_RESIZABLE_INACTIVE = 188,
            SETTINGS_RESIZABLE_INACTIVE_HOVER = 187;

    public static Sprite EMPTY_SPRITE = new Sprite();

    public int[] myPixels;
    public int myWidth;
    public int myHeight;
    public int drawOffsetY;
    public int maxWidth;
    public int maxHeight;
    private int drawOffsetX;
    private int identifier;
    private String name;
    
    public Sprite(int width, int height, int offsetX, int offsetY, int[] pixels) {
        this.myWidth = width;
        this.myHeight = height;
        this.drawOffsetX = offsetX;
        this.drawOffsetY = offsetY;
        this.myPixels = pixels;

        Color color = Color.MAGENTA;
        setTransparency(color.getRed(), color.getGreen(), color.getBlue());
    }

    public Sprite(int i, int j) {
        myPixels = new int[i * j];
        myWidth = maxWidth = i;
        myHeight = maxHeight = j;
        drawOffsetX = drawOffsetY = 0;
    }

    public Sprite(byte[] data, Component component) {
        try {
            Image image = Toolkit.getDefaultToolkit().createImage(data);
            MediaTracker mediatracker = new MediaTracker(component);
            mediatracker.addImage(image, 0);
            mediatracker.waitForAll();
            myWidth = image.getWidth(component);
            myHeight = image.getHeight(component);
            maxWidth = myWidth;
            maxHeight = myHeight;
            drawOffsetX = 0;
            drawOffsetY = 0;
            myPixels = new int[myWidth * myHeight];
            PixelGrabber pixelgrabber = new PixelGrabber(image, 0, 0, myWidth, myHeight, myPixels, 0, myWidth);
            pixelgrabber.grabPixels();
        } catch (Exception _ex) {
            System.out.println("Error converting jpg");
        }
    }

    public Sprite(Sprite sprite, int width, int height) {
        this.myWidth = width;
        this.myHeight = height;
        this.maxWidth = width;
        this.maxHeight = height;
        drawOffsetX = 0;
        drawOffsetY = 0;

        myPixels = new int[width * height];

        System.arraycopy(sprite.myPixels, 0, myPixels, 0, myPixels.length);

    }

    public Sprite(String img) {
        try {
            Image image = Toolkit.getDefaultToolkit().getImage(SignLink.findcachedir() + "Sprites/" + img + ".png");
            ImageIcon sprite = new ImageIcon(image);
            myWidth = sprite.getIconWidth();
            myHeight = sprite.getIconHeight();
            maxWidth = myWidth;
            maxHeight = myHeight;
            drawOffsetX = 0;
            drawOffsetY = 0;
            myPixels = new int[myWidth * myHeight];
            PixelGrabber pixelgrabber = new PixelGrabber(image, 0, 0, myWidth, myHeight, myPixels, 0, myWidth);
            pixelgrabber.grabPixels();
            image = null;
            setTransparency(255, 0, 255);
        } catch (Exception _ex) {
            System.out.println(_ex);
        }
    }

    public Sprite(FileArchive streamLoader, String name, int i) {
        Buffer dataBuffer = new Buffer(streamLoader.readFile(name + ".dat"));
        Buffer indexBuffer = new Buffer(streamLoader.readFile("index.dat"));

        indexBuffer.currentPosition = dataBuffer.readUShort();

        maxWidth = indexBuffer.readUShort();
        maxHeight = indexBuffer.readUShort();
        int pixelCount = indexBuffer.readUnsignedByte();
        int[] raster = new int[pixelCount];

        for (int pixel = 0; pixel < pixelCount - 1; pixel++) {
            raster[pixel + 1] = indexBuffer.readTriByte();
            if (raster[pixel + 1] == 0)
                raster[pixel + 1] = 1;
        }

        for (int index = 0; index < i; index++) {
            indexBuffer.currentPosition += 2;
            dataBuffer.currentPosition += indexBuffer.readUShort() * indexBuffer.readUShort();
            indexBuffer.currentPosition++;
        }

        drawOffsetX = indexBuffer.readUnsignedByte();
        drawOffsetY = indexBuffer.readUnsignedByte();
        myWidth = indexBuffer.readUShort();
        myHeight = indexBuffer.readUShort();

        int type = indexBuffer.readUnsignedByte();

        int spriteSize = myWidth * myHeight;

        myPixels = new int[spriteSize];
        if (type == 0) {
            for (int pixel = 0; pixel < spriteSize; pixel++) {
                myPixels[pixel] = raster[dataBuffer.readUnsignedByte()];
            }
            setTransparency(255, 0, 255);
            return;
        }
        if (type == 1) {
            for (int x = 0; x < myWidth; x++) {
                for (int y = 0; y < myHeight; y++) {
                    myPixels[x + y * myWidth] = raster[dataBuffer.readUnsignedByte()];
                }
            }

        }
        setTransparency(255, 0, 255);
    }

    public Sprite() {
    }

    public void draw24BitSprite(int x, int y) {
        if (this == EMPTY_SPRITE) {
            return;
        }
        int alpha = 256;
        x += this.drawOffsetX;// offsetX
        y += this.drawOffsetY;// offsetY
        int destOffset = x + y * Rasterizer2D.width;
        int srcOffset = 0;
        int height = this.myHeight;
        int width = this.myWidth;
        int destStep = Rasterizer2D.width - width;
        int srcStep = 0;
        if (y < Rasterizer2D.topY) {
            int trimHeight = Rasterizer2D.topY - y;
            height -= trimHeight;
            y = Rasterizer2D.topY;
            srcOffset += trimHeight * width;
            destOffset += trimHeight * Rasterizer2D.width;
        }
        if (y + height > Rasterizer2D.bottomY) {
            height -= (y + height) - Rasterizer2D.bottomY;
        }
        if (x < Rasterizer2D.leftX) {
            int trimLeft = Rasterizer2D.leftX - x;
            width -= trimLeft;
            x = Rasterizer2D.leftX;
            srcOffset += trimLeft;
            destOffset += trimLeft;
            srcStep += trimLeft;
            destStep += trimLeft;
        }
        if (x + width > Rasterizer2D.bottomX) {
            int trimRight = (x + width) - Rasterizer2D.bottomX;
            width -= trimRight;
            srcStep += trimRight;
            destStep += trimRight;
        }
        if (!((width <= 0) || (height <= 0))) {
            set24BitPixels(width, height, Rasterizer2D.pixels, myPixels, alpha, destOffset, srcOffset, destStep, srcStep);
        }
    }

    public void drawTransparentSprite(int x, int y, int opacity) {
        if (this == EMPTY_SPRITE) {
            return;
        }
        int k = opacity;// was parameter
        x += drawOffsetX;
        y += drawOffsetY;
        int i1 = x + y * Rasterizer2D.width;
        int j1 = 0;
        int k1 = myHeight;
        int l1 = myWidth;
        int i2 = Rasterizer2D.width - l1;
        int j2 = 0;
        if (y < Rasterizer2D.topY) {
            int k2 = Rasterizer2D.topY - y;
            k1 -= k2;
            y = Rasterizer2D.topY;
            j1 += k2 * l1;
            i1 += k2 * Rasterizer2D.width;
        }
        if (y + k1 > Rasterizer2D.bottomY)
            k1 -= (y + k1) - Rasterizer2D.bottomY;
        if (x < Rasterizer2D.leftX) {
            int l2 = Rasterizer2D.leftX - x;
            l1 -= l2;
            x = Rasterizer2D.leftX;
            j1 += l2;
            i1 += l2;
            j2 += l2;
            i2 += l2;
        }
        if (x + l1 > Rasterizer2D.bottomX) {
            int i3 = (x + l1) - Rasterizer2D.bottomX;
            l1 -= i3;
            j2 += i3;
            i2 += i3;
        }
        if (!(l1 <= 0 || k1 <= 0)) {
            method351(j1, l1, Rasterizer2D.pixels, myPixels, j2, k1, i2, k, i1);
        }
    }

    private void set24BitPixels(int width, int height, int[] destPixels, int[] srcPixels, int srcAlpha, int destOffset, int srcOffset, int destStep, int srcStep) {
        if (this == EMPTY_SPRITE) {
            return;
        }
        int srcColor;
        int destAlpha;
        for (int loop = -height; loop < 0; loop++) {
            for (int loop2 = -width; loop2 < 0; loop2++) {
                srcAlpha = ((this.myPixels[srcOffset] >> 24) & 255);
                destAlpha = 256 - srcAlpha;
                srcColor = srcPixels[srcOffset++];
                if (srcColor != 0 && srcColor != 0xffffff) {
                    int destColor = destPixels[destOffset];
                    destPixels[destOffset++] = ((srcColor & 0xff00ff) * srcAlpha + (destColor & 0xff00ff) * destAlpha & 0xff00ff00) + ((srcColor & 0xff00) * srcAlpha + (destColor & 0xff00) * destAlpha & 0xff0000) >> 8;
                } else {
                    destOffset++;
                }
            }
            destOffset += destStep;
            srcOffset += srcStep;
        }
    }

    public void setTransparency(int transRed, int transGreen, int transBlue) {
        if (this == EMPTY_SPRITE) {
            return;
        }
        for (int index = 0; index < myPixels.length; index++)
            if (((myPixels[index] >> 16) & 255) == transRed && ((myPixels[index] >> 8) & 255) == transGreen && (myPixels[index] & 255) == transBlue)
                myPixels[index] = 0;
    }

    public void init() {
        Rasterizer2D.initDrawingArea(myHeight, myWidth, myPixels);
    }

    public void method344(int i, int j, int k) {
        if (this == EMPTY_SPRITE) {
            return;
        }
        for (int i1 = 0; i1 < myPixels.length; i1++) {
            int j1 = myPixels[i1];
            if (j1 != 0) {
                int k1 = j1 >> 16 & 0xff;
                k1 += i;
                if (k1 < 1)
                    k1 = 1;
                else if (k1 > 255)
                    k1 = 255;
                int l1 = j1 >> 8 & 0xff;
                l1 += j;
                if (l1 < 1)
                    l1 = 1;
                else if (l1 > 255)
                    l1 = 255;
                int i2 = j1 & 0xff;
                i2 += k;
                if (i2 < 1)
                    i2 = 1;
                else if (i2 > 255)
                    i2 = 255;
                myPixels[i1] = (k1 << 16) + (l1 << 8) + i2;
            }
        }

    }

    public void method346(int x, int y) {
        if (this == EMPTY_SPRITE) {
            return;
        }
        x += drawOffsetX;
        y += drawOffsetY;
        int l = x + y * Rasterizer2D.width;
        int i1 = 0;
        int height = myHeight;
        int width = myWidth;
        int l1 = Rasterizer2D.width - width;
        int i2 = 0;
        if (y < Rasterizer2D.topY) {
            int j2 = Rasterizer2D.topY - y;
            height -= j2;
            y = Rasterizer2D.topY;
            i1 += j2 * width;
            l += j2 * Rasterizer2D.width;
        }
        if (y + height > Rasterizer2D.bottomY)
            height -= (y + height) - Rasterizer2D.bottomY;
        if (x < Rasterizer2D.leftX) {
            int k2 = Rasterizer2D.leftX - x;
            width -= k2;
            x = Rasterizer2D.leftX;
            i1 += k2;
            l += k2;
            i2 += k2;
            l1 += k2;
        }
        if (x + width > Rasterizer2D.bottomX) {
            int l2 = (x + width) - Rasterizer2D.bottomX;
            width -= l2;
            i2 += l2;
            l1 += l2;
        }
        if (width <= 0 || height <= 0) {
        } else {
            method347(l, width, height, i2, i1, l1, myPixels, Rasterizer2D.pixels);
        }
    }

    private void method347(int i, int j, int k, int l, int i1, int k1, int[] ai, int[] ai1) {
        if (this == EMPTY_SPRITE) {
            return;
        }
        int l1 = -(j >> 2);
        j = -(j & 3);
        for (int i2 = -k; i2 < 0; i2++) {
            for (int j2 = l1; j2 < 0; j2++) {
                ai1[i++] = ai[i1++];
                ai1[i++] = ai[i1++];
                ai1[i++] = ai[i1++];
                ai1[i++] = ai[i1++];
            }

            for (int k2 = j; k2 < 0; k2++)
                ai1[i++] = ai[i1++];

            i += k1;
            i1 += l;
        }
    }

    public void drawSprite1(int i, int j) {
        drawSprite1(i, j, 128);
    }

    public void drawSprite1(int i, int j, int k) {
        if (this == EMPTY_SPRITE) {
            return;
        }
        i += drawOffsetX;
        j += drawOffsetY;
        int i1 = i + j * Rasterizer2D.width;
        int j1 = 0;
        int k1 = myHeight;
        int l1 = myWidth;
        int i2 = Rasterizer2D.width - l1;
        int j2 = 0;
        if (j < Rasterizer2D.topY) {
            int k2 = Rasterizer2D.topY - j;
            k1 -= k2;
            j = Rasterizer2D.topY;
            j1 += k2 * l1;
            i1 += k2 * Rasterizer2D.width;
        }
        if (j + k1 > Rasterizer2D.bottomY)
            k1 -= (j + k1) - Rasterizer2D.bottomY;
        if (i < Rasterizer2D.leftX) {
            int l2 = Rasterizer2D.leftX - i;
            l1 -= l2;
            i = Rasterizer2D.leftX;
            j1 += l2;
            i1 += l2;
            j2 += l2;
            i2 += l2;
        }
        if (i + l1 > Rasterizer2D.bottomX) {
            int i3 = (i + l1) - Rasterizer2D.bottomX;
            l1 -= i3;
            j2 += i3;
            i2 += i3;
        }
        if (!(l1 <= 0 || k1 <= 0)) {
            method351(j1, l1, Rasterizer2D.pixels, myPixels, j2, k1, i2, k, i1);
        }
    }

    public void drawSprite(int x, int y) {
        if (this == EMPTY_SPRITE) {
            return;
        }
        x += drawOffsetX;
        y += drawOffsetY;
        int rasterClip = x + y * Rasterizer2D.width;
        int imageClip = 0;
        int height = myHeight;
        int width = myWidth;
        int rasterOffset = Rasterizer2D.width - width;
        int imageOffset = 0;
        if (y < Rasterizer2D.topY) {
            int dy = Rasterizer2D.topY - y;
            height -= dy;
            y = Rasterizer2D.topY;
            imageClip += dy * width;
            rasterClip += dy * Rasterizer2D.width;
        }
        if (y + height > Rasterizer2D.bottomY)
            height -= (y + height) - Rasterizer2D.bottomY;
        if (x < Rasterizer2D.leftX) {
            int dx = Rasterizer2D.leftX - x;
            width -= dx;
            x = Rasterizer2D.leftX;
            imageClip += dx;
            rasterClip += dx;
            imageOffset += dx;
            rasterOffset += dx;
        }
        if (x + width > Rasterizer2D.bottomX) {
            int dx = (x + width) - Rasterizer2D.bottomX;
            width -= dx;
            imageOffset += dx;
            rasterOffset += dx;
        }
        if (!(width <= 0 || height <= 0)) {
            method349(Rasterizer2D.pixels, myPixels, imageClip, rasterClip, width, height, rasterOffset, imageOffset);
        }
    }

    public void drawSprite(int i, int k, int color) {
        if (this == EMPTY_SPRITE) {
            return;
        }
        int tempWidth = myWidth + 2;
        int tempHeight = myHeight + 2;
        int[] tempArray = new int[tempWidth * tempHeight];
        for (int x = 0; x < myWidth; x++) {
            for (int y = 0; y < myHeight; y++) {
                if (myPixels[x + y * myWidth] != 0)
                    tempArray[(x + 1) + (y + 1) * tempWidth] = myPixels[x + y * myWidth];
            }
        }
        for (int x = 0; x < tempWidth; x++) {
            for (int y = 0; y < tempHeight; y++) {
                if (tempArray[(x) + (y) * tempWidth] == 0) {
                    if (x < tempWidth - 1 && tempArray[(x + 1) + ((y) * tempWidth)] > 0 && tempArray[(x + 1) + ((y) * tempWidth)] != 0xffffff) {
                        tempArray[(x) + (y) * tempWidth] = color;
                    }
                    if (x > 0 && tempArray[(x - 1) + ((y) * tempWidth)] > 0 && tempArray[(x - 1) + ((y) * tempWidth)] != 0xffffff) {
                        tempArray[(x) + (y) * tempWidth] = color;
                    }
                    if (y < tempHeight - 1 && tempArray[(x) + ((y + 1) * tempWidth)] > 0 && tempArray[(x) + ((y + 1) * tempWidth)] != 0xffffff) {
                        tempArray[(x) + (y) * tempWidth] = color;
                    }
                    if (y > 0 && tempArray[(x) + ((y - 1) * tempWidth)] > 0 && tempArray[(x) + ((y - 1) * tempWidth)] != 0xffffff) {
                        tempArray[(x) + (y) * tempWidth] = color;
                    }
                }
            }
        }
        i--;
        k--;
        i += drawOffsetX;
        k += drawOffsetY;
        int l = i + k * Rasterizer2D.width;
        int i1 = 0;
        int j1 = tempHeight;
        int k1 = tempWidth;
        int l1 = Rasterizer2D.width - k1;
        int i2 = 0;
        if (k < Rasterizer2D.topY) {
            int j2 = Rasterizer2D.topY - k;
            j1 -= j2;
            k = Rasterizer2D.topY;
            i1 += j2 * k1;
            l += j2 * Rasterizer2D.width;
        }
        if (k + j1 > Rasterizer2D.bottomY) {
            j1 -= (k + j1) - Rasterizer2D.bottomY;
        }
        if (i < Rasterizer2D.leftX) {
            int k2 = Rasterizer2D.leftX - i;
            k1 -= k2;
            i = Rasterizer2D.leftX;
            i1 += k2;
            l += k2;
            i2 += k2;
            l1 += k2;
        }
        if (i + k1 > Rasterizer2D.bottomX) {
            int l2 = (i + k1) - Rasterizer2D.bottomX;
            k1 -= l2;
            i2 += l2;
            l1 += l2;
        }
        if (!(k1 <= 0 || j1 <= 0)) {
            method349(Rasterizer2D.pixels, tempArray, i1, l, k1, j1, l1, i2);
        }
    }

    private void method349(int[] ai, int[] ai1, int j, int k, int l, int i1, int j1, int k1) {
        if (this == EMPTY_SPRITE) {
            return;
        }
        int i;// was parameter
        int l1 = -(l >> 2);
        l = -(l & 3);
        for (int i2 = -i1; i2 < 0; i2++) {
            for (int j2 = l1; j2 < 0; j2++) {
                i = ai1[j++];
                if (i != 0 && i != -1) {
                    ai[k++] = i;
                } else {
                    k++;
                }
                i = ai1[j++];
                if (i != 0 && i != -1) {
                    ai[k++] = i;
                } else {
                    k++;
                }
                i = ai1[j++];
                if (i != 0 && i != -1) {
                    ai[k++] = i;
                } else {
                    k++;
                }
                i = ai1[j++];
                if (i != 0 && i != -1) {
                    ai[k++] = i;
                } else {
                    k++;
                }
            }

            for (int k2 = l; k2 < 0; k2++) {
                i = ai1[j++];
                if (i != 0 && i != -1) {
                    ai[k++] = i;
                } else {
                    k++;
                }
            }
            k += j1;
            j += k1;
        }
    }

    private void method351(int i, int j, int[] ai, int[] ai1, int l, int i1, int j1, int k1, int l1) {
        if (this == EMPTY_SPRITE) {
            return;
        }
        int k;// was parameter
        int j2 = 256 - k1;
        for (int k2 = -i1; k2 < 0; k2++) {
            for (int l2 = -j; l2 < 0; l2++) {
                k = ai1[i++];
                if (k != 0) {
                    int i3 = ai[l1];
                    ai[l1++] = ((k & 0xff00ff) * k1 + (i3 & 0xff00ff) * j2 & 0xff00ff00) + ((k & 0xff00) * k1 + (i3 & 0xff00) * j2 & 0xff0000) >> 8;
                } else {
                    l1++;
                }
            }

            l1 += j1;
            i += l;
        }
    }

    public void rotate(int i, int j, int[] ai, int k, int[] ai1, int i1, int j1, int k1, int l1, int i2) {
        if (this == EMPTY_SPRITE) {
            return;
        }
        try {
            int j2 = -l1 / 2;
            int k2 = -i / 2;
            int l2 = (int) (Math.sin((double) j / 326.11000000000001D) * 65536D);
            int i3 = (int) (Math.cos((double) j / 326.11000000000001D) * 65536D);
            l2 = l2 * k >> 8;
            i3 = i3 * k >> 8;
            int j3 = (i2 << 16) + (k2 * l2 + j2 * i3);
            int k3 = (i1 << 16) + (k2 * i3 - j2 * l2);
            int l3 = k1 + j1 * Rasterizer2D.width;
            for (j1 = 0; j1 < i; j1++) {
                int i4 = ai1[j1];
                int j4 = l3 + i4;
                int k4 = j3 + i3 * i4;
                int l4 = k3 - l2 * i4;
                for (k1 = -ai[j1]; k1 < 0; k1++) {
                    Rasterizer2D.pixels[j4++] = myPixels[(k4 >> 16) + (l4 >> 16) * myWidth];
                    k4 += i3;
                    l4 -= l2;
                }

                j3 += l2;
                k3 += i3;
                l3 += Rasterizer2D.width;
            }

        } catch (Exception _ex) {
            _ex.printStackTrace();
        }
    }

    public void drawAdvancedSprite(int xPos, int yPos) {
        drawAdvancedSprite(xPos, yPos, 256);
    }

    public void drawAdvancedSprite(int xPos, int yPos, int alpha) {
        if (this == EMPTY_SPRITE) {
            return;
        }
        int alphaValue = alpha;
        xPos += drawOffsetX;
        yPos += drawOffsetY;
        int i1 = xPos + yPos * Rasterizer2D.width;
        int j1 = 0;
        int spriteHeight = myHeight;
        int spriteWidth = myWidth;
        int i2 = Rasterizer2D.width - spriteWidth;
        int j2 = 0;
        if (yPos < Rasterizer2D.topY) {
            int k2 = Rasterizer2D.topY - yPos;
            spriteHeight -= k2;
            yPos = Rasterizer2D.topY;
            j1 += k2 * spriteWidth;
            i1 += k2 * Rasterizer2D.width;
        }
        if (yPos + spriteHeight > Rasterizer2D.bottomY)
            spriteHeight -= (yPos + spriteHeight) - Rasterizer2D.bottomY;
        if (xPos < Rasterizer2D.leftX) {
            int l2 = Rasterizer2D.leftX - xPos;
            spriteWidth -= l2;
            xPos = Rasterizer2D.leftX;
            j1 += l2;
            i1 += l2;
            j2 += l2;
            i2 += l2;
        }
        if (xPos + spriteWidth > Rasterizer2D.bottomX) {
            int i3 = (xPos + spriteWidth) - Rasterizer2D.bottomX;
            spriteWidth -= i3;
            j2 += i3;
            i2 += i3;
        }
        if (!(spriteWidth <= 0 || spriteHeight <= 0)) {
            renderARGBPixels(spriteWidth, spriteHeight, myPixels, Rasterizer2D.pixels, i1, alphaValue, j1, j2, i2);
        }
    }

    private void renderARGBPixels(int spriteWidth, int spriteHeight, int[] spritePixels, int[] renderAreaPixels, int pixel, int alphaValue, int i, int l, int j1) {
        if (this == EMPTY_SPRITE) {
            return;
        }
        int pixelColor;
        int alphaLevel;
        int alpha = alphaValue;
        for (int height = -spriteHeight; height < 0; height++) {
            for (int width = -spriteWidth; width < 0; width++) {
                alphaValue = ((myPixels[i] >> 24) & (alpha - 1));
                alphaLevel = 256 - alphaValue;
                if (alphaLevel > 256) {
                    alphaValue = 0;
                }
                if (alpha == 0) {
                    alphaLevel = 256;
                    alphaValue = 0;
                }
                pixelColor = spritePixels[i++];
                if (pixelColor != 0) {
                    int pixelValue = renderAreaPixels[pixel];
                    renderAreaPixels[pixel++] = ((pixelColor & 0xff00ff) * alphaValue + (pixelValue & 0xff00ff) * alphaLevel & 0xff00ff00) + ((pixelColor & 0xff00) * alphaValue + (pixelValue & 0xff00) * alphaLevel & 0xff0000) >> 8;
                } else {
                    pixel++;
                }
            }
            pixel += j1;
            i += l;
        }
    }

    public void outline(int color) {
        if (this == EMPTY_SPRITE) {
            return;
        }
        int[] raster = new int[myWidth * myHeight];
        int start = 0;
        for (int y = 0; y < myHeight; y++) {
            for (int x = 0; x < myWidth; x++) {
                int outline = myPixels[start];
                if (outline == 0) {
                    if (x > 0 && myPixels[start - 1] != 0) {
                        outline = color;
                    } else if (y > 0 && myPixels[start - myWidth] != 0) {
                        outline = color;
                    } else if (x < myWidth - 1 && myPixels[start + 1] != 0) {
                        outline = color;
                    } else if (y < myHeight - 1 && myPixels[start + myWidth] != 0) {
                        outline = color;
                    }
                }
                raster[start++] = outline;
            }
        }
        myPixels = raster;
    }

    public void shadow(int color) {
        if (this == EMPTY_SPRITE) {
            return;
        }
        for (int y = myHeight - 1; y > 0; y--) {
            int pos = y * myWidth;
            for (int x = myWidth - 1; x > 0; x--) {
                if (myPixels[x + pos] == 0 && myPixels[x + pos - 1 - myWidth] != 0) {
                    myPixels[x + pos] = color;
                }
            }
        }
    }

    public Image convertToImage() {
        if (this == EMPTY_SPRITE) {
            return null;
        }
        // Convert to buffered image
        BufferedImage bufferedimage = new BufferedImage(myWidth, myHeight, 1);
        bufferedimage.setRGB(0, 0, myWidth, myHeight, myPixels, 0, myWidth);

        // Filter to ensure transparency preserved
        ImageFilter filter = new RGBImageFilter() {
            public final int markerRGB = Color.BLACK.getRGB() | 0xFF000000;

            public final int filterRGB(int x, int y, int rgb) {
                if ((rgb | 0xFF000000) == markerRGB) {
                    return 0x00FFFFFF & rgb;
                } else {
                    return rgb;
                }
            }
        };

        // Create image
        ImageProducer ip = new FilteredImageSource(bufferedimage.getSource(), filter);
        return Toolkit.getDefaultToolkit().createImage(ip);
    }
}
