package com.elvarg.game.definition.loader.impl;

import com.elvarg.game.GameConstants;
import com.elvarg.game.definition.ItemDefinition;
import com.elvarg.game.definition.loader.DefinitionLoader;
import com.google.gson.Gson;

import java.io.FileReader;

public class ItemDefinitionLoader extends DefinitionLoader {

    @Override
    public void load() throws Throwable {
    	ItemDefinition.definitions.clear();
        FileReader reader = new FileReader(file());
        ItemDefinition[] defs = new Gson().fromJson(reader, ItemDefinition[].class);
        for (ItemDefinition def : defs) {
            ItemDefinition.definitions.put(def.getId(), def);
        }
        reader.close();
    }

    @Override
    public String file() {
        return GameConstants.DEFINITIONS_DIRECTORY + "items.json";
    }
}
