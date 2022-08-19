package com.elvarg.game.entity.impl.player.persistence.jsonfile;

import com.elvarg.Server;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.entity.impl.player.persistence.PlayerPersistence;
import com.elvarg.game.entity.impl.player.persistence.PlayerSave;
import com.elvarg.util.Misc;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;

public class JSONFilePlayerPersistence extends PlayerPersistence {

    private static final String PATH = "./data/saves/characters/";
    private static final Gson BUILDER = new GsonBuilder().create();

    @Override
    public PlayerSave load(String username) {
        if (!exists(username)) {
            return null;
        }

        Path path = Paths.get(PATH, username + ".json");
        File file = path.toFile();

        try (FileReader fileReader = new FileReader(file)) {
            return BUILDER.fromJson(fileReader, PlayerSave.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void save(Player player) {
        PlayerSave save = PlayerSave.fromPlayer(player);

        Path path = Paths.get(PATH, player.getUsername() + ".json");
        File file = path.toFile();
        setupDirectory(file);

        Gson builder = new GsonBuilder().setPrettyPrinting().create();

		try (FileWriter writer = new FileWriter(file)) {
			writer.write(builder.toJson(save));
		} catch (Exception e) {
			Server.getLogger().log(Level.SEVERE, "An error has occurred while saving a character file!", e);
            throw new RuntimeException(e);
		}
    }

    @Override
    public boolean exists(String username) {
        String formattedUsername = Misc.formatPlayerName(username.toLowerCase());
        return new File(PATH + formattedUsername + ".json").exists();
    }

    @Override
    public String encryptPassword(String plainPassword) {
        // TODO: Fix password encryption for JSON
        return plainPassword;
    }

    public boolean checkPassword(String plainPassword, PlayerSave playerSave) {
        // TODO: Fix password encryption for JSON
        return plainPassword.equals(playerSave.getPasswordHashWithSalt());
    }

    private void setupDirectory(File file) {
        file.getParentFile().setWritable(true);
        if (!file.getParentFile().exists()) {
            try {
                file.getParentFile().mkdirs();
            } catch (SecurityException e) {
                System.out.println("Unable to create directory for player data!");
                throw new RuntimeException(e);
            }
        }
    }
}
