package com.elvarg.game.entity.impl.player;

import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;

import java.time.Instant;

public class PlayerSaveDb {

    private static DynamoDbClient dynamoDbClient = DynamoDbClient.builder().build();
    private static DynamoDbEnhancedClient enhancedClient = DynamoDbEnhancedClient.builder().dynamoDbClient(dynamoDbClient).build();
    private static String playerTableName = System.getenv("PLAYER_TABLE_NAME");

    private static final TableSchema<PlayerSaveRecord> PLAYER_SAVE_TABLE_SCHEMA = TableSchema.fromClass(PlayerSaveRecord.class);

    public static void save(Player player) {
        if (playerTableName == null) {
            return;
        }

        var playerSave = PlayerSave.fromPlayer(player);
        DynamoDbTable<PlayerSaveRecord> playerTable = enhancedClient.table(playerTableName, PLAYER_SAVE_TABLE_SCHEMA);

        playerTable.putItem(new PlayerSaveRecord(player.getUsername(), playerSave, Instant.now()));
    }

    public static PlayerSave fetch(String username) {
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

    public static boolean playerExists(String username) {
        // Have to do it properly later. Have to make sure we dont block main loop
        return true;
    }
}
