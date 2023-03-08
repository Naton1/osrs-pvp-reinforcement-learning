package com.elvarg.game.definition.loader.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.elvarg.game.GameConstants;
import com.elvarg.game.definition.NpcSpawnDefinition;
import com.elvarg.game.definition.loader.DefinitionLoader;
import com.elvarg.game.model.Direction;
import com.elvarg.game.model.Location;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class NPCSpawnDumper extends DefinitionLoader {

	@Override
	public void load() throws Throwable {
		BufferedReader r = new BufferedReader(new FileReader(new File(file())));
		String s;
		
		Path path = Paths.get(GameConstants.DEFINITIONS_DIRECTORY, "gay.json");
		File file = path.toFile();
		file.getParentFile().setWritable(true);
		FileWriter w = new FileWriter(file, true);
		Gson builder = new GsonBuilder().setPrettyPrinting().create();
		
		while ((s = r.readLine()) != null) {
			if (s.startsWith("/"))
				continue;
			String[] data = s.split(" ");
			int id = Integer.parseInt(data[0]);
			int x = Integer.parseInt(data[2]);
			int y = Integer.parseInt(data[3]);
			int z = Integer.parseInt(data[4]);
			
			w.write(builder.toJson(builder.toJsonTree(new NpcSpawnDefinition(id, new Location(x, y, z), Direction.SOUTH, 2))));
			w.write(",");
			w.write("\n");
		}
		r.close();
		w.close();
	}

	@Override
	public String file() {
		return GameConstants.DEFINITIONS_DIRECTORY + "dump.txt";
	}
}
