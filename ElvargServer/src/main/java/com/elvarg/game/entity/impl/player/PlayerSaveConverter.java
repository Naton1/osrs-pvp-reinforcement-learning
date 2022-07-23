package com.elvarg.game.entity.impl.player;

public class PlayerSaveConverter extends JacksonAttributeConverter<PlayerSave> {
    public PlayerSaveConverter() {
        super(PlayerSave.class);
    }
}
