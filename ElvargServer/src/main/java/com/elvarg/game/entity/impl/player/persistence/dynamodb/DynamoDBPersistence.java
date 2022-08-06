package com.elvarg.game.entity.impl.player.persistence.dynamodb;

import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.entity.impl.player.persistence.PersistenceMethod;
import com.elvarg.game.entity.impl.player.persistence.PlayerSave;
import com.elvarg.game.entity.impl.playerbot.PlayerBot;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import java.time.Instant;

public class DynamoDBPersistence extends PersistenceMethod {

    private static DynamoDbClient dynamoDbClient = DynamoDbClient.builder().region(Region.EU_WEST_1).build();
    private static DynamoDbEnhancedClient enhancedClient = DynamoDbEnhancedClient.builder().dynamoDbClient(dynamoDbClient).build();
    private static String playerTableName = System.getenv("PLAYER_TABLE_NAME");

    private static final TableSchema<PlayerSaveRecord> PLAYER_SAVE_TABLE_SCHEMA = TableSchema.fromClass(PlayerSaveRecord.class);

    @Override
    public void save(Player player) {
        if (playerTableName == null || player instanceof PlayerBot) {
            return;
        }

        var playerSave = PlayerSave.fromPlayer(player);
        DynamoDbTable<PlayerSaveRecord> playerTable = enhancedClient.table(playerTableName, PLAYER_SAVE_TABLE_SCHEMA);

        playerTable.putItem(new PlayerSaveRecord(player.getUsername(), playerSave, Instant.now()));
    }

    @Override
    public PlayerSave retrieve(String username) {
        if (playerTableName == null) {
            return null;
        }

        DynamoDbTable<PlayerSaveRecord> playerTable = enhancedClient.table(playerTableName, PLAYER_SAVE_TABLE_SCHEMA);

        var playerSaveRecord = playerTable.getItem(Key.builder().partitionValue(username).build());

        if (playerSaveRecord == null) {
            return null;
        }

        return playerSaveRecord.getPlayerSave();
    }

    @Override
    public boolean exists(String username) {
        // Have to do it properly later. Have to make sure we dont block main loop
        return true;
    }
}
