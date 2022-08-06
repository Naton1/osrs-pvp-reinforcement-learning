package com.elvarg.game.entity.impl.player.persistence.dynamodb;

import com.elvarg.game.entity.impl.player.persistence.PlayerSave;
import com.elvarg.game.entity.impl.player.persistence.PlayerSaveConverter;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbConvertedBy;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

import java.time.Instant;

@DynamoDbBean
public class PlayerSaveRecord {
    private String username;
    private PlayerSave playerSave;
    private Instant updatedAt;

    public PlayerSaveRecord() {
    }

    public PlayerSaveRecord(String username, PlayerSave playerSave, Instant updatedAt) {
        this.username = username;
        this.playerSave = playerSave;
        this.updatedAt = updatedAt;
    }

    @DynamoDbPartitionKey
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @DynamoDbAttribute("playerSave")
    @DynamoDbConvertedBy(PlayerSaveConverter.class)
    public PlayerSave getPlayerSave() {
        return playerSave;
    }

    public void setPlayerSave(PlayerSave playerSave) {
        this.playerSave = playerSave;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
