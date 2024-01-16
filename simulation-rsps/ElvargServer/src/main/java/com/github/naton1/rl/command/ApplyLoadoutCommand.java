package com.github.naton1.rl.command;

import com.elvarg.game.content.presets.Presetable;
import com.elvarg.game.content.presets.Presetables;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.commands.Command;
import com.github.naton1.rl.EnvironmentDebugger;
import com.github.naton1.rl.env.EnvironmentDescriptor;
import com.github.naton1.rl.env.EnvironmentRegistry;
import com.google.gson.Gson;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ApplyLoadoutCommand implements Command {

    private static final Gson gson = new Gson();

    @Override
    public void execute(final Player player, final String command, final String[] parts) {
        final Map<String, String> params = parseParams(parts);
        applyLoadout(player, params);
        player.sendMessage("Applied loadout. Config: " + params);
    }

    @Override
    public boolean canUse(final Player player) {
        return true;
    }

    private static <T> void applyLoadout(Player player, Map<String, String> params) {
        final Set<String> validParams = Set.of("env", "envParams");
        final Set<String> paramKeys = params.keySet();
        if (!validParams.containsAll(paramKeys)) {
            paramKeys.removeAll(validParams);
            player.sendMessage("Invalid parameters: " + paramKeys);
            return;
        }

        final String envType = params.getOrDefault("env", EnvironmentRegistry.NH.getType());
        final EnvironmentRegistry environmentRegistry = Arrays.stream(EnvironmentRegistry.values())
                .filter(r -> r.getType().equals(envType))
                .findFirst()
                .orElse(null);
        if (environmentRegistry == null) {
            player.sendMessage("Invalid environment: " + envType);
            return;
        }

        final EnvironmentDescriptor<T> environmentDescriptor =
                (EnvironmentDescriptor<T>) environmentRegistry.getEnvironmentDescriptor();

        final String rawEnvParams = params.getOrDefault("envParams", "");
        final Map<String, String> envParamsConfig = parseParams(rawEnvParams.split(","));
        final T envParams =
                gson.fromJson(gson.toJsonTree(envParamsConfig), environmentDescriptor.getEnvironmentParamsType());

        EnvironmentDebugger.registerParams(player, envParams);

        final Presetable presetable =
                environmentDescriptor.getEnvironmentLoadout(envParams).asPreset();
        Presetables.load(player, presetable);
    }

    private static Map<String, String> parseParams(String[] params) {
        // Params must be key=value pairs
        return Arrays.stream(params)
                .map(String::trim)
                .filter(s -> s.contains("="))
                .map(s -> s.split("=", 2))
                .filter(s -> s.length == 2)
                .collect(Collectors.toMap(s -> s[0], s -> s[1]));
    }
}
