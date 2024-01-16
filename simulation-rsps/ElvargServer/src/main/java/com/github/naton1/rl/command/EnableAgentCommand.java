package com.github.naton1.rl.command;

import com.elvarg.game.content.combat.hit.HitDamage;
import com.elvarg.game.content.presets.Presetable;
import com.elvarg.game.content.presets.Presetables;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.event.EventDispatcher;
import com.elvarg.game.event.events.HitAppliedEvent;
import com.elvarg.game.event.events.HitCalculatedEvent;
import com.elvarg.game.event.events.PlayerLoggedOutEvent;
import com.elvarg.game.event.events.PlayerPacketsFlushedEvent;
import com.elvarg.game.event.events.PlayerPacketsProcessedEvent;
import com.elvarg.game.model.commands.Command;
import com.github.naton1.rl.AgentAdapter;
import com.github.naton1.rl.EnvConfig;
import com.github.naton1.rl.EnvironmentDebugger;
import com.github.naton1.rl.PvpClient;
import com.github.naton1.rl.env.EnvironmentDescriptor;
import com.github.naton1.rl.env.EnvironmentRegistry;
import com.google.gson.Gson;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class EnableAgentCommand implements Command {

    private static final Gson gson = new Gson();

    private final Map<Player, AgentAdapter<?>> adaptedPlayers = new HashMap<>();

    public EnableAgentCommand() {
        EventDispatcher.getGlobal().add(PlayerLoggedOutEvent.class, this::onPlayerLogout);
        EventDispatcher.getGlobal().add(HitAppliedEvent.class, this::onHitApplied);
        EventDispatcher.getGlobal().add(PlayerPacketsProcessedEvent.class, this::onPlayerPacketsProcessed);
        EventDispatcher.getGlobal().add(PlayerPacketsFlushedEvent.class, this::onPlayerPacketsFlushed);
        EventDispatcher.getGlobal().add(HitCalculatedEvent.class, this::onHitCalculated);
    }

    @Override
    public void execute(final Player player, final String command, final String[] parts) {
        final AgentAdapter<?> existingAdapter = adaptedPlayers.get(player);
        if (command.contains("disable")) {
            if (existingAdapter == null) {
                player.sendMessage("No agent to disable.");
                return;
            }
            existingAdapter.onLogout();
            adaptedPlayers.remove(player);
            player.sendMessage("Disabled agent.");
        } else {
            if (existingAdapter != null) {
                player.sendMessage("Agent already enabled.");
                return;
            }
            final Map<String, String> params = parseParams(parts);
            final AgentAdapter<?> adapter = createAdapter(player, params);
            if (adapter == null) {
                player.sendMessage("Adapter config invalid, not adapting agent.");
                return;
            }
            adaptedPlayers.put(player, adapter);
            player.sendMessage("Adapted agent to player. The agent will assist in your fights. Config: " + params);
        }
    }

    @Override
    public boolean canUse(final Player player) {
        return true;
    }

    private static <T> AgentAdapter<?> createAdapter(Player player, Map<String, String> params) {
        final Set<String> validParams =
                Set.of("env", "model", "stackFrames", "deterministic", "envParams", "applyLoadout");
        final Set<String> paramKeys = params.keySet();
        if (!validParams.containsAll(paramKeys)) {
            paramKeys.removeAll(validParams);
            player.sendMessage("Invalid parameters: " + paramKeys);
            return null;
        }

        final String envType = params.getOrDefault("env", EnvironmentRegistry.NH.getType());
        final EnvironmentRegistry environmentRegistry = Arrays.stream(EnvironmentRegistry.values())
                .filter(r -> r.getType().equals(envType))
                .findFirst()
                .orElse(null);
        if (environmentRegistry == null) {
            player.sendMessage("Invalid environment: " + envType);
            return null;
        }

        final int stackFrames;
        try {
            stackFrames = Integer.parseInt(params.getOrDefault("stackFrames", "1"));
        } catch (NumberFormatException e) {
            player.sendMessage("Invalid stackFrames: " + params);
            return null;
        }

        final EnvironmentDescriptor<T> environmentDescriptor =
                (EnvironmentDescriptor<T>) environmentRegistry.getEnvironmentDescriptor();

        final boolean deterministic = Boolean.parseBoolean(params.getOrDefault("deterministic", "false"));

        final String model = params.getOrDefault("model", "all");

        final String rawEnvParams = params.getOrDefault("envParams", "");
        final Map<String, String> envParamsConfig = parseParams(rawEnvParams.split(","));
        final T envParams =
                gson.fromJson(gson.toJsonTree(envParamsConfig), environmentDescriptor.getEnvironmentParamsType());

        EnvironmentDebugger.registerParams(player, envParams);

        final boolean applyLoadout = Boolean.parseBoolean(params.getOrDefault("applyLoadout", "true"));
        if (applyLoadout) {
            final Presetable presetable =
                    environmentDescriptor.getEnvironmentLoadout(envParams).asPreset();
            Presetables.load(player, presetable);
        }

        return new AgentAdapter<>(
                player,
                model,
                stackFrames,
                environmentDescriptor,
                envParams,
                deterministic,
                new PvpClient(EnvConfig.getPredictionApiHost(), EnvConfig.getPredictionApiPort()),
                () -> {});
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

    private void onPlayerPacketsFlushed(PlayerPacketsFlushedEvent event) {
        final AgentAdapter<?> adapter = adaptedPlayers.get(event.getPlayer());
        if (adapter != null) {
            adapter.onFlush();
        }
    }

    private void onHitCalculated(HitCalculatedEvent event) {
        if (event.getPendingHit().getAttacker().isPlayer()
                && event.getPendingHit().getTarget().isPlayer()) {
            final AgentAdapter<?> attackerAdapter =
                    adaptedPlayers.get(event.getPendingHit().getAttacker().getAsPlayer());
            if (attackerAdapter != null) {
                attackerAdapter.onHitCalculated(event.getPendingHit());
            }
            final AgentAdapter<?> targetAdapter =
                    adaptedPlayers.get(event.getPendingHit().getAttacker().getAsPlayer());
            if (targetAdapter != null) {
                targetAdapter.onHitCalculated(event.getPendingHit());
            }
        }
    }

    private void onHitApplied(HitAppliedEvent event) {
        final HitDamage hit = event.getHitDamage();
        if (hit.getMetadata().getAttacker().isPlayer()
                && hit.getMetadata().getTarget().isPlayer()
                && hit.getMetadata().getAssociatedPendingHit() == null) {
            final AgentAdapter<?> attackerAdapter =
                    adaptedPlayers.get(hit.getMetadata().getAttacker().getAsPlayer());
            if (attackerAdapter != null) {
                attackerAdapter.onHitApplied(hit);
            }
            final AgentAdapter<?> targetAdapter =
                    adaptedPlayers.get(hit.getMetadata().getTarget().getAsPlayer());
            if (targetAdapter != null) {
                targetAdapter.onHitApplied(hit);
            }
        }
    }

    private void onPlayerLogout(PlayerLoggedOutEvent event) {
        final AgentAdapter<?> adapter = adaptedPlayers.get(event.getPlayer());
        if (adapter != null) {
            adapter.onLogout();
        }
    }

    private void onPlayerPacketsProcessed(PlayerPacketsProcessedEvent event) {
        final AgentAdapter<?> adapter = adaptedPlayers.get(event.getPlayer());
        if (adapter != null) {
            adapter.processPlayer();
        }
    }
}
