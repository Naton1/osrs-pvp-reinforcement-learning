package com.runescape.graphics.sprite;

import com.runescape.sign.SignLink;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import static java.nio.file.StandardOpenOption.READ;

public final class SpriteCache implements Closeable {

    private Sprite[] cache;
    private FileChannel dataChannel;
    private FileChannel metaChannel;

    public void init() throws IOException {
        File dataFile = new File(SignLink.findcachedir() + "main_file_sprites.dat");
        File metaFile = new File(SignLink.findcachedir() + "main_file_sprites.idx");
        if (!dataFile.exists()) {
            throw new IOException(String.format("Could not find data file=%s", dataFile.getName()));
        }

        if (!metaFile.exists()) {
            throw new IOException(String.format("Could not find meta file=%s", metaFile.getName()));
        }

        dataChannel = FileChannel.open(dataFile.toPath(), READ);
        metaChannel = FileChannel.open(metaFile.toPath(), READ);

        final int spriteCount = Math.toIntExact(metaChannel.size() / 10);

        cache = new Sprite[spriteCount];

        System.out.println(String.format("Sprite cache size=%d", cache.length));
    }

    public Sprite lookup(int id) {
        try {
            if (contains(id)) {
                return cache[id];
            }

            if (!dataChannel.isOpen() || !metaChannel.isOpen()) {
                System.out.println("Sprite channels are closed!");
                return Sprite.EMPTY_SPRITE;
            }

            final int entries = Math.toIntExact(metaChannel.size() / 10);

            if (id > entries) {
                System.out.println(String.format("id=%d > size=%d", id, entries));
                return Sprite.EMPTY_SPRITE;
            }

            metaChannel.position(id * 10);

            final ByteBuffer metaBuf = ByteBuffer.allocate(10);
            metaChannel.read(metaBuf);
            metaBuf.flip();

            final int pos = ((metaBuf.get() & 0xFF) << 16) | ((metaBuf.get() & 0xFF) << 8) | (metaBuf.get() & 0xFF);
            final int len = ((metaBuf.get() & 0xFF) << 16) | ((metaBuf.get() & 0xFF) << 8) | (metaBuf.get() & 0xFF);
            final int offsetX = metaBuf.getShort() & 0xFF;
            final int offsetY = metaBuf.getShort() & 0xFF;

            final ByteBuffer dataBuf = ByteBuffer.allocate(len);

            dataChannel.position(pos);
            dataChannel.read(dataBuf);
            dataBuf.flip();

            try (InputStream is = new ByteArrayInputStream(dataBuf.array())) {

                BufferedImage bimage = ImageIO.read(is);

                if (bimage == null) {
                    System.out.println(String.format("Could not read image at %d", id));
                    return Sprite.EMPTY_SPRITE;
                }

                if (bimage.getType() != BufferedImage.TYPE_INT_ARGB) {
                    bimage = convert(bimage, BufferedImage.TYPE_INT_ARGB);
                }

                final int[] pixels = ((DataBufferInt) bimage.getRaster().getDataBuffer()).getData();

                final Sprite sprite = new Sprite(bimage.getWidth(), bimage.getHeight(), offsetX, offsetY, pixels);

                // cache so we don't have to perform I/O calls again
                cache[id] = sprite;

                return sprite;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println(String.format("No sprite found for id=%d", id));
        return Sprite.EMPTY_SPRITE;
    }
    
    public void draw(int id, int x, int y) {
        draw(id, x, y, false);
    }
    
    public void draw(int id, int x, int y, int alpha, boolean advanced) {
        Sprite sprite = lookup(id);
        if (sprite != null) {
            if (advanced) {
                sprite.drawAdvancedSprite(x, y, alpha);
            } else {
                sprite.drawSprite(x, y, alpha);
            }
        }
    }
    
    public void draw(int id, int x, int y, boolean advanced) {
        Sprite sprite = lookup(id);
        if (sprite != null) {
            if (advanced) {
                sprite.drawAdvancedSprite(x, y);
            } else {
                sprite.drawSprite(x, y);
            }
        }
    }

    public boolean contains(int id) {
        return id < cache.length && cache[id] != null;
    }

    private static BufferedImage convert(BufferedImage bimage, int type) {
        BufferedImage converted = new BufferedImage(bimage.getWidth(), bimage.getHeight(), type);
        converted.getGraphics().drawImage(bimage, 0, 0, null);
        return converted;
    }

    public void close() throws IOException {
        dataChannel.close();
        metaChannel.close();
    }

}
