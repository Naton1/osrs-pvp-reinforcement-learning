/*
 * Copyright (c) 2022, Mark
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.elvarg.game.definition.loader.impl;

import com.elvarg.Server;
import com.elvarg.game.GameConstants;
import com.elvarg.game.definition.GroundItemsDefinition;
import com.elvarg.game.entity.impl.grounditem.ItemOnGround;
import com.elvarg.game.entity.impl.grounditem.ItemOnGroundManager;
import com.elvarg.game.model.Item;
import com.google.gson.Gson;

import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;
import java.util.logging.Level;

public class GroundItemDefinitionLoader implements Runnable {

    public void run() {
        try {
            String fileLocation = GameConstants.DEFINITIONS_DIRECTORY + "ground_items.json";
            long start = System.currentTimeMillis();

            FileReader reader = reader = new FileReader(fileLocation);
            GroundItemsDefinition[] defs = new Gson().fromJson(reader, GroundItemsDefinition[].class);
            Arrays.stream(defs).forEach(definition -> {
                ItemOnGround groundItem = new ItemOnGround(
                        ItemOnGround.State.SEEN_BY_EVERYONE,
                        Optional.of("ground_items_spawns"),
                        definition.getLocation(), new Item(definition.getId(), definition.getAmt()), true,
                        definition.getRespawn(),
                        null
                );
                ItemOnGroundManager.register(groundItem);
            });
            reader.close();

            long elapsed = System.currentTimeMillis() - start;
            Server.getLogger().log(Level.INFO, "Loaded definitions for: " + fileLocation + " . It took " + elapsed  + " milliseconds. " + defs.length + " Ground items Loaded.");
        } catch (IOException e) {
            Server.getLogger().log(Level.SEVERE, "Unable to load world spawns.");
        }
    }

}
