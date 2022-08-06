package com.elvarg.game.entity.impl.player.persistence.jsonfile;

import com.elvarg.Server;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.entity.impl.player.persistence.PersistenceMethod;
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

public class JSONFilePersistence extends PersistenceMethod {

    private static final String PATH = "./data/saves/characters/";

    @Override
    public PlayerSave retrieve(String username) {
        if (!exists(username)) {
            return null;
        }

        Path path = Paths.get(PATH, username + ".json");
        File file = path.toFile();

        try (FileReader fileReader = new FileReader(file)) {
            JsonParser fileParser = new JsonParser();
            Gson builder = new GsonBuilder().create();
            JsonObject reader = (JsonObject) fileParser.parse(fileReader);

            PlayerSave result = builder.fromJson(reader, PlayerSave.class);

            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
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
		}
    }

    @Override
    public boolean exists(String username) {
        username = Misc.formatPlayerName(username.toLowerCase());
        return new File(PATH + username + ".json").exists();
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
            }
        }
    }
}
