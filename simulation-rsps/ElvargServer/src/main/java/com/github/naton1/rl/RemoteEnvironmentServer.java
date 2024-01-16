package com.github.naton1.rl;

import com.elvarg.game.World;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.task.Task;
import com.elvarg.game.task.TaskManager;
import com.github.naton1.rl.env.EnvironmentDescriptor;
import com.github.naton1.rl.env.EnvironmentRegistry;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.ToNumberPolicy;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RemoteEnvironmentServer {

    private final Map<String, RemoteEnvironmentPlayerBot> agents = new ConcurrentHashMap<>();
    private final SimpleSocketServer app;

    private final Task cleanupTask;
    private final Task loggingTask;

    public RemoteEnvironmentServer() {
        log.info("Starting remote server on {}...", EnvConfig.getRemoteEnvPort());
        final Gson gson = new GsonBuilder()
                .setObjectToNumberStrategy(ToNumberPolicy.LONG_OR_DOUBLE)
                .create();
        this.app = SimpleSocketServer.builder()
                .gson(gson)
                .route("login", this::login)
                .route("logout", this::logout)
                .route("reset", this::reset)
                .route("step", this::step)
                .route("debug", this::debug)
                .port(EnvConfig.getRemoteEnvPort())
                .build();
        this.app.start();
        log.info("Started remote server on {}", EnvConfig.getRemoteEnvPort());
        this.cleanupTask = new Task(10) {
            @Override
            protected void execute() {
                agents.values().removeIf(a -> {
                    if (a.isExpired()) {
                        World.getRemovePlayerQueue().add(a);
                        a.setQueuedForLogout();
                        return true;
                    }
                    return false;
                });
            }
        };
        this.loggingTask = new Task(1) {
            private boolean isEmpty = false;

            @Override
            protected void execute() {
                if (isEmpty && agents.isEmpty()) {
                    // Skip logging if we've already logged there's no environments
                    return;
                }
                log.info("Processing tick; {} remote environments", agents.size());
                isEmpty = agents.isEmpty();
            }
        };
        TaskManager.submit(this.cleanupTask);
        TaskManager.submit(this.loggingTask);
    }

    public void close() {
        this.app.close();
        TaskManager.submit(new Task() {
            @Override
            protected void execute() {
                cleanupTask.stop();
                loggingTask.stop();
                for (RemoteEnvironmentPlayerBot remoteEnvironmentPlayerBot : agents.values()) {
                    World.getRemovePlayerQueue().add(remoteEnvironmentPlayerBot);
                    remoteEnvironmentPlayerBot.setQueuedForLogout();
                }
                agents.clear();
                stop();
            }
        });
    }

    private CompletableFuture<?> reset(SimpleSocketServer.Context ctx) {
        final ResetRequest resetRequest = ctx.bodyAsClass(ResetRequest.class);
        final String id = ctx.meta("id");
        log.debug("Remote reset request: " + resetRequest + " for " + id);
        final MessageContext messageContext = MessageContext.reset(resetRequest);
        agents.get(id).queueMessage(messageContext);
        return messageContext.getCompletableFuture();
    }

    private CompletableFuture<?> step(SimpleSocketServer.Context ctx) {
        final StepRequest stepRequest = ctx.bodyAsClass(StepRequest.class);
        final String id = ctx.meta("id");
        log.debug("Remote step request: " + stepRequest + " for " + id);
        final MessageContext messageContext = MessageContext.step(stepRequest);
        agents.get(id).queueMessage(messageContext);
        return messageContext.getCompletableFuture();
    }

    private CompletableFuture<?> login(SimpleSocketServer.Context ctx) {
        final LoginRequest loginRequest = ctx.bodyAsClass(LoginRequest.class);
        final String id = ctx.meta("id");
        log.debug("Logging in: {} - {}", id, loginRequest);
        final EnvironmentDescriptor<?> environmentDescriptor = Arrays.stream(EnvironmentRegistry.values())
                .filter(ar -> ar.getType().equals(loginRequest.agentType))
                .findFirst()
                .orElseThrow()
                .getEnvironmentDescriptor();
        final CompletableFuture<?> future = new CompletableFuture<>();
        TaskManager.submit(new Task() {
            @Override
            protected void execute() {
                try {
                    if (agents.containsKey(id)) {
                        throw new EnvironmentException("Agent already exists with ID: " + id);
                    }
                    final Optional<Player> existingPlayer = World.getPlayerByName(id);
                    if (existingPlayer.isPresent()) {
                        if (existingPlayer.get() instanceof RemoteEnvironmentPlayerBot bot && bot.isLogoutQueued()) {
                            // Try again next tick, bot is queued for logout so should be
                            // available soon
                            log.debug(
                                    "Agent for {} is already logged in but queued for logout,"
                                            + " trying again next tick",
                                    id);
                            return;
                        }
                        throw new EnvironmentException("Player already logged in with ID: " + id);
                    }
                    final RemoteEnvironmentPlayerBot agent = new RemoteEnvironmentPlayerBot(id, environmentDescriptor);
                    agents.put(id, agent);
                    future.complete(null);
                    stop();
                } catch (Exception e) {
                    log.error("Login failed for {}", id, e);
                    future.completeExceptionally(e);
                    stop();
                }
            }
        });
        return future;
    }

    private CompletableFuture<?> logout(SimpleSocketServer.Context ctx) {
        final String id = ctx.meta("id");
        log.debug("Logging out: " + id);
        final CompletableFuture<?> future = new CompletableFuture<>();
        final RemoteEnvironmentPlayerBot agent = agents.get(id);
        if (agent == null) {
            future.completeExceptionally(new IllegalStateException("No player found for: " + id));
            return future;
        }
        agent.setQueuedForLogout();
        TaskManager.submit(new Task() {
            @Override
            protected void execute() {
                try {
                    final RemoteEnvironmentPlayerBot agent = agents.remove(id);
                    World.getRemovePlayerQueue().add(agent);
                    future.complete(null);
                } catch (Exception e) {
                    log.error("Logout failed for {}", id, e);
                    future.completeExceptionally(e);
                } finally {
                    stop();
                }
            }
        });
        if (EnvConfig.isSyncEnabled()) {
            // Return early so nothing gets stuck
            // kind of a hack so that closing eval/old selfs/main bots sequentially doesn't deadlock
            future.complete(null);
        }
        return future;
    }

    private CompletableFuture<?> debug(SimpleSocketServer.Context ctx) {
        final CompletableFuture<List<AgentDebugInfo>> future = new CompletableFuture<>();
        TaskManager.submit(new Task() {
            @Override
            protected void execute() {
                try {
                    future.complete(agents.values().stream()
                            .map(RemoteEnvironmentPlayerBot::getAgentDebugInfo)
                            .sorted(Comparator.comparing(AgentDebugInfo::getId))
                            .collect(Collectors.toList()));
                } catch (Exception e) {
                    log.error("Debug generation failed", e);
                    future.completeExceptionally(e);
                } finally {
                    stop();
                }
            }
        });
        return future;
    }

    @Getter
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class MessageContext {
        private final StepRequest stepRequest;
        private final ResetRequest resetRequest;
        private final EpisodeState episodeState;

        @Getter(value = AccessLevel.PRIVATE)
        private final CompletableFuture<MessageResponse> completableFuture;

        @Setter
        private volatile boolean handled;

        public void respond(MessageResponse message) {
            this.completableFuture.complete(message);
        }

        public void exception(Exception e) {
            this.completableFuture.completeExceptionally(e);
        }

        public static MessageContext step(StepRequest stepRequest) {
            return new MessageContext(stepRequest, null, EpisodeState.ACTION, new CompletableFuture<>());
        }

        public static MessageContext reset(ResetRequest resetRequest) {
            return new MessageContext(null, resetRequest, EpisodeState.RESET, new CompletableFuture<>());
        }
    }

    @Value
    public static class LoginRequest {
        private final String agentType;
    }

    @Value
    public static class ResetRequest {
        private final String target;
        private final JsonElement resetParams;
        private final boolean maintainTargetEnvironment;
        private final boolean training;
        private final boolean deathMatch;
    }

    @Value
    public static class StepRequest {
        private final List<Integer> action;
    }

    @Value
    @Builder
    public static class MessageResponse {
        private final List<Number> obs;
        private final List<List<Boolean>> actionMasks;
        private final TerminalState terminalState;
        private final Meta meta;
    }

    @Value
    @Builder
    public static class Meta {
        private final double currentHealthPercent;
        private final double currentPrayerPercent;
        private final double currentTargetHealthPercent;
        private final double damageReceived;
        private final double damageDealt;
        private final int episodeTicks;
        private final Boolean protectedPrayer;
        private final Boolean hitOffPrayer;
        private final int foodCount;
        private final double attackLevelScale;
        private final double strengthLevelScale;
        private final double defenceLevelScale;
        private final double rangedLevelScale;
        private final double magicLevelScale;
        private final String attackTypeHit;
        private final String attackTypeReceived;
        private final String playerPrayerType;
        private final String targetPrayerType;
        private final String playerAttackStyleType;
        private final String targetAttackStyleType;
        private final String currentPlayerPrayerType;
        private final String currentTargetPrayerType;
        private final double distance;
        private final int playerFrozenTicks;
        private final int targetFrozenTicks;
        private final double remainingFoodScale;
        private final double remainingBrewScale;
        private final int targetFoodCount;
        private final double targetRemainingFoodScale;
        private final double targetRemainingBrewScale;
        private final Double wastedFoodScale;
        private final Double eatToFoodScale;
        private final Double eatAtFoodScale;
        private final Double eatenFoodScale;
        private final Double wastedBrewScale;
        private final Double eatToBrewScale;
        private final Double eatAtBrewScale;
        private final double damageGeneratedOnPlayerScale;
        private final double damageGeneratedOnTargetScale;
        private final double extraDamageDealtOnPlayerScale;
        private final double extraDamageDealtOnTargetScale;
        private final Boolean hitWithSmite;
        private final Boolean targetAttackedWithSmite;
        private final double playerHealedScale;
        private final double targetHealedScale;
        private final List<Number> targetObs;
        private final List<List<Boolean>> targetActionMasks;
        private final Integer targetHitAttackSpeed;
        private final Integer playerHitAttackSpeed;
    }

    @Value
    @Builder
    public static class AgentDebugInfo {
        private final String id;
        private final String target;
        private final String state;
        private final boolean pendingMessage;
        private final String envType;
        private final int currentEpisodeTicks;
        private final int lastMessageTick;
        private final boolean isLoaded;
        private final boolean isTargetLoaded;
    }

    public enum EpisodeState {
        RESET,
        ACTION
    }

    public enum TerminalState {
        WON,
        LOST,
        TIED,
        TARGET_LOST,
        DESYNC
    }
}
