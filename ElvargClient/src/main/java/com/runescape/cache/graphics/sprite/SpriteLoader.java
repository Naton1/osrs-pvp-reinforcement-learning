package com.runescape.cache.graphics.sprite;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public final class SpriteLoader {

    public static Sprite[] sprites;
    public static int totalSprites;

    public static void load() {

        File spriteFolder = new File("./Cache/sprites/");

        if (!spriteFolder.exists()) {
            return;
        }

        File[] spriteFiles = spriteFolder.listFiles();
        if (spriteFiles == null) {
            return;
        }

        totalSprites = spriteFiles.length;
        sprites = new Sprite[totalSprites];
        for (File spriteFile : spriteFiles) {
            int id = Integer.parseInt(spriteFile.getName().replaceAll(".png", ""));
            try {
                sprites[id] = new Sprite(Files.readAllBytes(spriteFile.toPath()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        System.out.println("Loaded: " + totalSprites + " sprites");
    }

}