package com.elvarg.game.entity.impl.player.persistence;

import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.entity.impl.player.persistence.dynamodb.DynamoDBPersistence;
import com.elvarg.game.entity.impl.player.persistence.jsonfile.JSONFilePersistence;
import com.elvarg.util.PasswordUtil;

import static com.elvarg.game.GameConstants.PLAYER_PERSISTENCE_METHOD;

public abstract class PersistenceMethod {

    public enum Method {
        DynamoDB, JSONFile
    };

    public abstract PlayerSave retrieve(String username);

    public abstract void save(Player player);

    public abstract boolean exists(String username);

    public String encryptPassword(String plainPassword) {
        return PasswordUtil.generatePasswordHashWithSalt(plainPassword);
    }

    public boolean checkPassword(String plainPassword, PlayerSave playerSave) {
        String passwordHashWithSalt = playerSave.getPasswordHashWithSalt();
        return PasswordUtil.passwordsMatch(plainPassword, passwordHashWithSalt);
    }

    private static PersistenceMethod singleton;

    public static PersistenceMethod getSingleton() {
        if (singleton == null) {
            switch (PLAYER_PERSISTENCE_METHOD) {
                case DynamoDB:
                    singleton = new DynamoDBPersistence();
                    break;

                case JSONFile:
                default:
                    singleton = new JSONFilePersistence();
                    break;
            }
        }

        return singleton;
    }

}
