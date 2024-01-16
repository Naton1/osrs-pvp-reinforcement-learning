package com.elvarg.game.plugin;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;

import java.util.List;
import java.util.stream.Collectors;

public class PluginLoader {

	public List<Plugin> load() {
		try (final ScanResult scanResult = new ClassGraph().enableClassInfo().scan()) {
			return scanResult.getSubclasses(Plugin.class).stream().map(c -> c.loadClass(Plugin.class)).map(c -> {
				try {
					return c.getConstructor().newInstance();
				}
				catch (Exception e) {
					throw new RuntimeException(e);
				}
			}).collect(Collectors.toList());
		}
	}

}
