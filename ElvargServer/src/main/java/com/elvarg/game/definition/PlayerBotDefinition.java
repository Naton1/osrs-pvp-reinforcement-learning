package com.elvarg.game.definition;

import com.elvarg.game.entity.impl.playerbot.PlayerBot;
import com.elvarg.game.model.Location;

public class PlayerBotDefinition {

    private String username;

    private Location spawnLocation;

    private int presetIndex;

    public PlayerBotDefinition(String _username, Location _spawnLocation, int _presetIndex) {
        this.username = _username;
        this.spawnLocation = _spawnLocation;
        this.presetIndex = _presetIndex;
    }

    public String getUsername() {
        return this.username;
    }

    public Location getSpawnLocation() {
        return this.spawnLocation;
    }

    public int getPresetIndex() {
        return this.presetIndex;
    }
}
