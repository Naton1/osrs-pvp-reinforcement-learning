package com.runescape.draw;

import com.runescape.collection.Cacheable;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBufferInt;
import java.awt.image.DirectColorModel;
import java.awt.image.Raster;
import java.util.Hashtable;

public class Rasterizer2D extends Cacheable {
    private static final ColorModel COLOR_MODEL = new DirectColorModel(32, 0xff0000, 0xff00, 0xff);
    public static int[] pixels;
    public static int width;
    public static int height;
    public static int topY;
    public static int bottomY;
    public static int leftX;
    public static int bottomX;
    public static int lastX;
    public static int viewportCenterX;
    public static int viewportCenterY;

    /**
     * Sets the Rasterizer2D in the upper left corner with height, width and pixels set.
     *
     * @param height The height of the drawingArea.
     * @param width  The width of the drawingArea.
     * @param pixels The array of pixels (RGBColours) in the drawingArea.
     */
    public static void initDrawingArea(int height, int width, int[] pixels) {
        Rasterizer2D.pixels = pixels;
        Rasterizer2D.width = width;
        Rasterizer2D.height = height;
        setDrawingArea(height, 0, width, 0);
    }

    /**
     * Sets the drawingArea to the default size and position.
     * Position: Upper left corner.
     * Size: As specified before.
     */
    public static void defaultDrawingAreaSize() {
        leftX = 0;
        topY = 0;
        bottomX = width;
        bottomY = height;
        lastX = bottomX;
        viewportCenterX = bottomX / 2;
    }

    /**
     * Sets the drawingArea based on the coordinates of the edges.
     *
     * @param bottomY The bottom edge Y-Coordinate.
     * @param leftX   The left edge X-Coordinate.
     * @param rightX  The right edge X-Coordinate.
     * @param topY    The top edge Y-Coordinate.
     */
    public static void setDrawingArea(int bottomY, int leftX, int rightX, int topY) {
        if (leftX < 0) {
            leftX = 0;
        }
        if (topY < 0) {
            topY = 0;
        }
        if (rightX > width) {
            rightX = width;
        }
        if (bottomY > height) {
            bottomY = height;
        }
        Rasterizer2D.leftX = leftX;
        Rasterizer2D.topY = topY;
        bottomX = rightX;
        Rasterizer2D.bottomY = bottomY;
        lastX = bottomX;
        viewportCenterX = bottomX / 2;
        viewportCenterY = Rasterizer2D.bottomY / 2;
    }

	/* Graphics2D methods */

    /**
     * Clears the drawingArea by setting every pixel to 0 (black).
     */
    public static void clear() {
        int i = width * height;
        for (int j = 0; j < i; j++) {
            pixels[j] = 0;
        }
    }

    /**
     * Draws a box filled with a certain colour.
     *
     * @param leftX     The left edge X-Coordinate of the box.
     * @param topY      The top edge Y-Coordinate of the box.
     * @param width     The width of the box.
     * @param height    The height of the box.
     * @param rgbColour The RGBColour of the box.
     */
    public static void drawBox(int leftX, int topY, int width, int height, int rgbColour) {
        if (leftX < Rasterizer2D.leftX) {
            width -= Rasterizer2D.leftX - leftX;
            leftX = Rasterizer2D.leftX;
        }
        if (topY < Rasterizer2D.topY) {
            height -= Rasterizer2D.topY - topY;
            topY = Rasterizer2D.topY;
        }
        if (leftX + width > bottomX)
            width = bottomX - leftX;
        if (topY + height > bottomY)
            height = bottomY - topY;
        int leftOver = Rasterizer2D.width - width;
        int pixelIndex = leftX + topY * Rasterizer2D.width;
        for (int rowIndex = 0; rowIndex < height; rowIndex++) {
            for (int columnIndex = 0; columnIndex < width; columnIndex++)
                pixels[pixelIndex++] = rgbColour;
            pixelIndex += leftOver;
        }
    }

    /**
     * Draws a transparent box.
     *
     * @param leftX     The left edge X-Coordinate of the box.
     * @param topY      The top edge Y-Coordinate of the box.
     * @param width     The box width.
     * @param height    The box height.
     * @param rgbColour The box colour.
     * @param opacity   The opacity value ranging from 0 to 256.
     */
    public static void drawTransparentBox(int leftX, int topY, int width, int height, int rgbColour, int opacity) {
        if (leftX < Rasterizer2D.leftX) {
            width -= Rasterizer2D.leftX - leftX;
            leftX = Rasterizer2D.leftX;
        }
        if (topY < Rasterizer2D.topY) {
            height -= Rasterizer2D.topY - topY;
            topY = Rasterizer2D.topY;
        }
        if (leftX + width > bottomX)
            width = bottomX - leftX;
        if (topY + height > bottomY)
            height = bottomY - topY;
        int transparency = 256 - opacity;
        int red = (rgbColour >> 16 & 0xff) * opacity;
        int green = (rgbColour >> 8 & 0xff) * opacity;
        int blue = (rgbColour & 0xff) * opacity;
        int leftOver = Rasterizer2D.width - width;
        int pixelIndex = leftX + topY * Rasterizer2D.width;
        for (int rowIndex = 0; rowIndex < height; rowIndex++) {
            for (int columnIndex = 0; columnIndex < width; columnIndex++) {
                int otherRed = (pixels[pixelIndex] >> 16 & 0xff) * transparency;
                int otherGreen = (pixels[pixelIndex] >> 8 & 0xff) * transparency;
                int otherBlue = (pixels[pixelIndex] & 0xff) * transparency;
                int transparentColour = ((red + otherRed >> 8) << 16) + ((green + otherGreen >> 8) << 8) + (blue + otherBlue >> 8);
                pixels[pixelIndex++] = transparentColour;
            }
            pixelIndex += leftOver;
        }
    }

    public static void drawPixels(int height, int posY, int posX, int color, int w) {
        if (posX < leftX) {
            w -= leftX - posX;
            posX = leftX;
        }
        if (posY < topY) {
            height -= topY - posY;
            posY = topY;
        }
        if (posX + w > bottomX) {
            w = bottomX - posX;
        }
        if (posY + height > bottomY) {
            height = bottomY - posY;
        }
        int k1 = width - w;
        int l1 = posX + posY * width;
        for (int i2 = -height; i2 < 0; i2++) {
            for (int j2 = -w; j2 < 0; j2++) {
                pixels[l1++] = color;
            }

            l1 += k1;
        }
    }

    /**
     * Draws a 1 pixel thick box outline in a certain colour.
     *
     * @param leftX     The left edge X-Coordinate.
     * @param topY      The top edge Y-Coordinate.
     * @param width     The width.
     * @param height    The height.
     * @param rgbColour The RGB-Colour.
     */
    public static void drawBoxOutline(int leftX, int topY, int width, int height, int rgbColour) {
        drawHorizontalLine(leftX, topY, width, rgbColour);
        drawHorizontalLine(leftX, (topY + height) - 1, width, rgbColour);
        drawVerticalLine(leftX, topY, height, rgbColour);
        drawVerticalLine((leftX + width) - 1, topY, height, rgbColour);
    }

    /**
     * Draws a coloured horizontal line in the drawingArea.
     *
     * @param xPosition The start X-Position of the line.
     * @param yPosition The Y-Position of the line.
     * @param width     The width of the line.
     * @param rgbColour The colour of the line.
     */
    public static void drawHorizontalLine(int xPosition, int yPosition, int width, int rgbColour) {
        if (yPosition < topY || yPosition >= bottomY)
            return;
        if (xPosition < leftX) {
            width -= leftX - xPosition;
            xPosition = leftX;
        }
        if (xPosition + width > bottomX)
            width = bottomX - xPosition;
        int pixelIndex = xPosition + yPosition * Rasterizer2D.width;
        for (int i = 0; i < width; i++)
            pixels[pixelIndex + i] = rgbColour;
    }
    
    public static void drawHorizontalLine(int x, int y, int width, int color, int alpha) {
    	if (y < topY || y >= bottomY)
            return;
        if (x < leftX) {
            width -= leftX - x;
            x = leftX;
        }
        if (x + width > bottomX)
            width = bottomX - x;
        int transparency = 256 - alpha;
        int red = (color >> 16 & 0xff) * alpha;
        int green = (color >> 8 & 0xff) * alpha;
        int blue = (color & 0xff) * alpha;
        int pixelIndex = x + y * Rasterizer2D.width;
        for (int j3 = 0; j3 < width; j3++) {
            int otherRed = (pixels[pixelIndex] >> 16 & 0xff) * transparency;
            int otherGreen = (pixels[pixelIndex] >> 8 & 0xff) * transparency;
            int otherBlue = (pixels[pixelIndex] & 0xff) * transparency;
            int transparentColour = ((red + otherRed >> 8) << 16) + ((green + otherGreen >> 8) << 8) + (blue + otherBlue >> 8);
            pixels[pixelIndex++] = transparentColour;
        }
    }
    
    public static void fillRectangle(int h, int yPos, int xPos, int color, int w) {
        if (xPos < leftX) {
            w -= leftX - xPos;
            xPos = leftX;
        }
        if (yPos < topY) {
            h -= topY - yPos;
            yPos = topY;
        }
        if (xPos + w > bottomX)
            w = bottomX - xPos;
        if (yPos + h > bottomY)
            h = bottomY - yPos;
        int k1 = Rasterizer2D.width - w;
        int l1 = xPos + yPos * Rasterizer2D.width;
        for (int i2 = -h; i2 < 0; i2++) {
            for (int j2 = -w; j2 < 0; j2++)
            	pixels[l1++] = color;

            l1 += k1;
        }
    }
    
    public static void fillRectangle(int x, int y, int w, int h, int color, int alpha) {
        if (x < leftX) {
            w -= leftX - x;
            x = leftX;
        }
        if (y < topY) {
            h -= topY - y;
            y = topY;
        }
        if (x + w > bottomX)
            w = bottomX - x;
        if (y + h > bottomY)
            h = bottomY - y;
        int a2 = 256 - alpha;
        int r1 = (color >> 16 & 0xff) * alpha;
        int g1 = (color >> 8 & 0xff) * alpha;
        int b1 = (color & 0xff) * alpha;
        int k3 = Rasterizer2D.width - w;
        int pixel = x + y * Rasterizer2D.width;
        for (int i4 = 0; i4 < h; i4++) {
            for (int index = -w; index < 0; index++) {
                int r2 = (pixels[pixel] >> 16 & 0xff) * a2;
                int g2 = (pixels[pixel] >> 8 & 0xff) * a2;
                int b2 = (pixels[pixel] & 0xff) * a2;
                int rgb = ((r1 + r2 >> 8) << 16) + ((g1 + g2 >> 8) << 8) + (b1 + b2 >> 8);
                pixels[pixel++] = rgb;
            }
            pixel += k3;
        }
    }

    /**
     * Draws a coloured vertical line in the drawingArea.
     *
     * @param xPosition The X-Position of the line.
     * @param yPosition The start Y-Position of the line.
     * @param height    The height of the line.
     * @param rgbColour The colour of the line.
     */
    public static void drawVerticalLine(int xPosition, int yPosition, int height, int rgbColour) {
        if (xPosition < leftX || xPosition >= bottomX)
            return;
        if (yPosition < topY) {
            height -= topY - yPosition;
            yPosition = topY;
        }
        if (yPosition + height > bottomY)
            height = bottomY - yPosition;
        int pixelIndex = xPosition + yPosition * width;
        for (int rowIndex = 0; rowIndex < height; rowIndex++)
            pixels[pixelIndex + rowIndex * width] = rgbColour;
    }

    /**
     * Draws a 1 pixel thick transparent box outline in a certain colour.
     *
     * @param leftX     The left edge X-Coordinate
     * @param topY      The top edge Y-Coordinate.
     * @param width     The width.
     * @param height    The height.
     * @param rgbColour The RGB-Colour.
     * @param opacity   The opacity value ranging from 0 to 256.
     */
    public static void drawTransparentBoxOutline(int leftX, int topY, int width, int height, int rgbColour, int opacity) {
        drawTransparentHorizontalLine(leftX, topY, width, rgbColour, opacity);
        drawTransparentHorizontalLine(leftX, topY + height - 1, width, rgbColour, opacity);
        if (height >= 3) {
            drawTransparentVerticalLine(leftX, topY + 1, height - 2, rgbColour, opacity);
            drawTransparentVerticalLine(leftX + width - 1, topY + 1, height - 2, rgbColour, opacity);
        }
    }

    /**
     * Draws a transparent coloured horizontal line in the drawingArea.
     *
     * @param xPosition The start X-Position of the line.
     * @param yPosition The Y-Position of the line.
     * @param width     The width of the line.
     * @param rgbColour The colour of the line.
     * @param opacity   The opacity value ranging from 0 to 256.
     */
    public static void drawTransparentHorizontalLine(int xPosition, int yPosition, int width, int rgbColour, int opacity) {
        if (yPosition < topY || yPosition >= bottomY) {
            return;
        }
        if (xPosition < leftX) {
            width -= leftX - xPosition;
            xPosition = leftX;
        }
        if (xPosition + width > bottomX) {
            width = bottomX - xPosition;
        }
        final int transparency = 256 - opacity;
        final int red = (rgbColour >> 16 & 0xff) * opacity;
        final int green = (rgbColour >> 8 & 0xff) * opacity;
        final int blue = (rgbColour & 0xff) * opacity;
        int pixelIndex = xPosition + yPosition * Rasterizer2D.width;
        for (int i = 0; i < width; i++) {
            final int otherRed = (pixels[pixelIndex] >> 16 & 0xff) * transparency;
            final int otherGreen = (pixels[pixelIndex] >> 8 & 0xff) * transparency;
            final int otherBlue = (pixels[pixelIndex] & 0xff) * transparency;
            final int transparentColour = (red + otherRed >> 8 << 16) + (green + otherGreen >> 8 << 8) + (blue + otherBlue >> 8);
            pixels[pixelIndex++] = transparentColour;
        }
    }

    /**
     * Draws a transparent coloured vertical line in the drawingArea.
     *
     * @param xPosition The X-Position of the line.
     * @param yPosition The start Y-Position of the line.
     * @param height    The height of the line.
     * @param rgbColour The colour of the line.
     * @param opacity   The opacity value ranging from 0 to 256.
     */
    public static void drawTransparentVerticalLine(int xPosition, int yPosition, int height, int rgbColour, int opacity) {
        if (xPosition < leftX || xPosition >= bottomX) {
            return;
        }
        if (yPosition < topY) {
            height -= topY - yPosition;
            yPosition = topY;
        }
        if (yPosition + height > bottomY) {
            height = bottomY - yPosition;
        }
        final int transparency = 256 - opacity;
        final int red = (rgbColour >> 16 & 0xff) * opacity;
        final int green = (rgbColour >> 8 & 0xff) * opacity;
        final int blue = (rgbColour & 0xff) * opacity;
        int pixelIndex = xPosition + yPosition * width;
        for (int i = 0; i < height; i++) {
            final int otherRed = (pixels[pixelIndex] >> 16 & 0xff) * transparency;
            final int otherGreen = (pixels[pixelIndex] >> 8 & 0xff) * transparency;
            final int otherBlue = (pixels[pixelIndex] & 0xff) * transparency;
            final int transparentColour = (red + otherRed >> 8 << 16) + (green + otherGreen >> 8 << 8) + (blue + otherBlue >> 8);
            pixels[pixelIndex] = transparentColour;
            pixelIndex += width;
        }
    }

    public static void drawFilledCircle(int x, int y, int radius, int color, int alpha) {
        int y1 = y - radius;
        if (y1 < 0) {
            y1 = 0;
        }
        int y2 = y + radius;
        if (y2 >= height) {
            y2 = height - 1;
        }
        int a2 = 256 - alpha;
        int r1 = (color >> 16 & 0xff) * alpha;
        int g1 = (color >> 8 & 0xff) * alpha;
        int b1 = (color & 0xff) * alpha;
        for (int iy = y1; iy <= y2; iy++) {
            int dy = iy - y;
            int dist = (int) Math.sqrt(radius * radius - dy * dy);
            int x1 = x - dist;
            if (x1 < 0) {
                x1 = 0;
            }
            int x2 = x + dist;
            if (x2 >= width) {
                x2 = width - 1;
            }
            int pos = x1 + iy * width;
            for (int ix = x1; ix <= x2; ix++) {
                /*  Tried replacing all pixels[pos] with:
                    Client.instance.gameScreenImageProducer.canvasRaster[pos]
					AND Rasterizer3D.pixels[pos] */
                int r2 = (pixels[pos] >> 16 & 0xff) * a2;
                int g2 = (pixels[pos] >> 8 & 0xff) * a2;
                int b2 = (pixels[pos] & 0xff) * a2;
                pixels[pos++] = ((r1 + r2 >> 8) << 16) + ((g1 + g2 >> 8) << 8) + (b1 + b2 >> 8);
            }
        }
    }
    
    public static void fillGradientRectangle(int x, int y, int w, int h, int startColour, int endColour) {
		int k1 = 0;
		int l1 = 0x10000 / h;
		if (x < leftX) {
			w -= leftX - x;
			x = leftX;
		}
		if (y < topY) {
			k1 += (topY - y) * l1;
			h -= topY - y;
			y = topY;
		}
		if (x + w > bottomX)
			w = bottomX - x;
		if (y + h > bottomY)
			h = bottomY - y;
		int lineGap = width - w;
		int pixelOffset = x + y * width;
		for (int yi = -h; yi < 0; yi++) {
			int blendAmount = 0x10000 - k1 >> 8;
			int blendInverse = k1 >> 8;
			int combinedColour = ((startColour & 0xff00ff) * blendAmount + (endColour & 0xff00ff) * blendInverse & 0xff00ff00) + ((startColour & 0xff00) * blendAmount + (endColour & 0xff00) * blendInverse & 0xff0000) >>> 8;
			int alpha = ((((startColour >> 24) & 0xff) * blendAmount + ((endColour >> 24) & 0xff) * blendInverse) >>> 8) + 5;
			for (int index = -w; index < 0; index++) {
				int backingPixel = pixels[pixelOffset];
				pixels[pixelOffset++] = ((backingPixel & 0xff00ff) * (256 - alpha) + (combinedColour & 0xff00ff) * alpha & 0xff00ff00) + ((backingPixel & 0xff00) * (256 - alpha) + (combinedColour & 0xff00) * alpha & 0xff0000) >>> 8;
			}
			pixelOffset += lineGap;
			k1 += l1;
		}
	}

    public static Graphics2D createGraphics(boolean renderingHints) {
        Graphics2D g2d = createGraphics(pixels, width, height);
        if (renderingHints) {
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        }
        return g2d;
    }

    public static Graphics2D createGraphics(int[] pixels, int width, int height) {
        return new BufferedImage(COLOR_MODEL, Raster.createWritableRaster(COLOR_MODEL.createCompatibleSampleModel(width, height), new DataBufferInt(pixels, width * height), null), false, new Hashtable<Object, Object>()).createGraphics();
    }

    public static Shape createSector(int x, int y, int r, int angle) {
        return new Arc2D.Double(x, y, r, r, 90, -angle, Arc2D.PIE);
    }

    public static Shape createCircle(int x, int y, int r) {
        return new Ellipse2D.Double(x, y, r, r);
    }

    public static Shape createRing(Shape sector, Shape innerCircle) {
        Area ring = new Area(sector);
        ring.subtract(new Area(innerCircle));
        return ring;
    }
}