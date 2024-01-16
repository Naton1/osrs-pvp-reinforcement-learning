package com.elvarg.game.definition.loader.impl;

import com.elvarg.game.GameConstants;
import com.elvarg.game.definition.ObjectSpawnDefinition;
import com.elvarg.game.definition.loader.DefinitionLoader;
import com.elvarg.game.entity.impl.object.GameObject;
import com.elvarg.game.entity.impl.object.ObjectManager;
import com.google.gson.Gson;

import java.io.FileReader;

public class ObjectSpawnDefinitionLoader extends DefinitionLoader {

    @Override
    public void load() throws Throwable {
        FileReader reader = new FileReader(file());
        ObjectSpawnDefinition[] defs = new Gson().fromJson(reader, ObjectSpawnDefinition[].class);
        for (ObjectSpawnDefinition def : defs) {
            ObjectManager.register(new GameObject(def.getId(), def.getPosition(), def.getType(), def.getFace(), null), true);
        }
        reader.close();
    }

    @Override
    public String file() {
        return GameConstants.DEFINITIONS_DIRECTORY + "object_spawns.json";
    }
}