package com.elvarg.game.definition.loader.impl;

import com.elvarg.game.GameConstants;
import com.elvarg.game.definition.NpcDefinition;
import com.elvarg.game.definition.loader.DefinitionLoader;
import com.google.gson.Gson;

import java.io.FileReader;

public class NpcDefinitionLoader extends DefinitionLoader {

    @Override
    public void load() throws Throwable {
    	NpcDefinition.definitions.clear();
        FileReader reader = new FileReader(file());
        NpcDefinition[] defs = new Gson().fromJson(reader, NpcDefinition[].class);
        for (NpcDefinition def : defs) {
            NpcDefinition.definitions.put(def.getId(), def);
        }
        reader.close();
    }

    @Override
    public String file() {
        return GameConstants.DEFINITIONS_DIRECTORY + "npc_defs.json";
    }

}
