package com.elvarg.game.model.commands.impl;

import com.elvarg.game.GameConstants;
import com.elvarg.game.World;
import com.elvarg.game.definition.NpcDefinition;
import com.elvarg.game.definition.NpcSpawnDefinition;
import com.elvarg.game.entity.impl.npc.NPC;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.Direction;
import com.elvarg.game.model.Location;
import com.elvarg.game.model.commands.Command;
import com.elvarg.game.model.rights.PlayerRights;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;

public class SpawnPermanentNPCCommand implements Command {

    @Override
    public void execute(Player player, String command, String[] parts) {

        try {
            int npcId = Integer.parseInt(parts[1]);
            int radius = parts.length > 2 ? Integer.parseInt(parts[1]) : 2;
            NpcDefinition npcDef = NpcDefinition.forId(npcId);
            String locationName = player.getArea() == null ? "Unknown area" : player.getArea().getName();
            String description = locationName + " " + npcDef.getName();
            this.write(npcId, player.getLocation().clone(), radius, description);
            player.getPacketSender().sendMessage("Permanently spawned " + description);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        NPC npc = NPC.create(Integer.parseInt(parts[1]), player.getLocation().clone());
        World.getAddNPCQueue().add(npc);
        if (player.getPrivateArea() != null) {
            player.getPrivateArea().add(npc);
        }
    }

    @Override
    public boolean canUse(Player player) {
        PlayerRights rights = player.getRights();
        return (rights == PlayerRights.OWNER || rights == PlayerRights.DEVELOPER);
    }

    public void write(int npcId, Location npcLocation, int npcRadius, String description) throws Throwable {
        Gson gson = new Gson();
        File file = new File(GameConstants.DEFINITIONS_DIRECTORY + "npc_spawns.json");
        FileReader reader = new FileReader(file);

        Gson builder = new GsonBuilder().setPrettyPrinting().create();

        NpcSpawnDefinition[] definitionArray = gson.fromJson(reader, NpcSpawnDefinition[].class);

        if (definitionArray == null) {
            return;
        }

        FileWriter writer = new FileWriter(file, false);

        ArrayList<NpcSpawnDefinition> list = new ArrayList(Arrays.asList(definitionArray));

        list.add(new NpcSpawnDefinition(npcId, npcLocation, Direction.SOUTH, 2, description));

        NpcSpawnDefinition[] finalArray = new NpcSpawnDefinition[list.size()];
        finalArray = list.toArray(finalArray);

        builder.toJson(finalArray, writer);
        writer.flush();
        writer.close();
    }


}

