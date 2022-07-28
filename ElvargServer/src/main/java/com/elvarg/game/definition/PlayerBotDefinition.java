package com.elvarg.game.definition;

import com.elvarg.game.content.presets.Presetable;
import com.elvarg.game.entity.impl.playerbot.fightstyle.PlayerBotFightLogic;
import com.elvarg.game.model.Location;

public class PlayerBotDefinition {

    private final String username;

    private final Location spawnLocation;

    private final Presetable preset;

    private final PlayerBotFightLogic fightLogic;

    public PlayerBotDefinition(String _username, Location _spawnLocation, Presetable preset, PlayerBotFightLogic fightLogic) {
        this.username = _username;
        this.spawnLocation = _spawnLocation;
        this.preset = preset;
        this.fightLogic = fightLogic;
    }

    public String getUsername() {
        return this.username;
    }

    public Location getSpawnLocation() {
        return this.spawnLocation;
    }

    public Presetable getPreset() {
        return this.preset;
    }

    public PlayerBotFightLogic getFightLogic() {
        return fightLogic;
    }
}
