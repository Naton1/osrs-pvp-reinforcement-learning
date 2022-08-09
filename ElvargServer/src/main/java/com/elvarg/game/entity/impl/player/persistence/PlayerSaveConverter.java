package com.elvarg.game.entity.impl.player.persistence;

import com.elvarg.game.entity.impl.player.JacksonAttributeConverter;

public class PlayerSaveConverter extends JacksonAttributeConverter<PlayerSave> {
    public PlayerSaveConverter() {
        super(PlayerSave.class);
    }
}
