package com.elvarg.game.definition.loader.impl;

import com.elvarg.game.GameConstants;
import com.elvarg.game.definition.ShopDefinition;
import com.elvarg.game.definition.loader.DefinitionLoader;
import com.elvarg.game.model.container.shop.Shop;
import com.elvarg.game.model.container.shop.ShopManager;
import com.google.gson.Gson;

import java.io.FileReader;

public class ShopDefinitionLoader extends DefinitionLoader {

    @Override
    public void load() throws Throwable {
    	ShopManager.shops.clear();
        FileReader reader = new FileReader(file());
        ShopDefinition[] defs = new Gson().fromJson(reader, ShopDefinition[].class);
        for (ShopDefinition def : defs) {
            ShopManager.shops.put(def.getId(), new Shop(def.getId(), def.getName(), def.getOriginalStock()));
        }
        reader.close();
    }

    @Override
    public String file() {
        return GameConstants.DEFINITIONS_DIRECTORY + "shops.json";
    }
}
