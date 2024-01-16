package com.github.naton1.rl;

import com.elvarg.game.World;
import com.elvarg.game.content.combat.hit.HitDamage;
import com.elvarg.game.content.combat.hit.PendingHit;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.areas.impl.WildernessArea;
import com.github.naton1.rl.env.AgentEnvironment;
import com.github.naton1.rl.env.EnvironmentDescriptor;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AgentAdapter<AgentParams> {

    private final EnvironmentDescriptor<AgentParams> environmentDescriptor;
    private final AgentParams agentParams;

    private final String model;
    private final int stackFrames;
    private final boolean deterministic;
    private final Player agent;
    private final ExecutorService executorService;

    private final Queue<Runnable> onFlushTasks = new LinkedList<>();

    private final PvpClient pvpClient;

    private final Runnable onFightEnd;

    private AgentEnvironment agentEnvironment;
    private LinkedList<List<Number>> frames;

    @Getter(AccessLevel.PACKAGE)
    private CompletableFuture<PvpClient.Response> nextAction;

    public AgentAdapter(
            Player agent,
            String model,
            int stackFrames,
            EnvironmentDescriptor<AgentParams> environmentDescriptor,
            AgentParams agentParams,
            boolean deterministic,
            PvpClient pvpClient,
            Runnable onFightEnd) {
        this.environmentDescriptor = environmentDescriptor;
        this.stackFrames = stackFrames;
        this.model = model;
        this.deterministic = deterministic;
        this.agent = agent;
        this.agentParams = agentParams;
        this.pvpClient = pvpClient;
        this.executorService = Executors.newSingleThreadExecutor();
        this.onFightEnd = onFightEnd;
    }

    public void processPlayer() {
        if (this.agentEnvironment != null) {
            this.agentEnvironment.onTickStart();
        }
        checkAttacker();
        checkFightComplete();
        processAction();
    }

    public void onFlush() {
        if (this.agentEnvironment != null) {
            this.agentEnvironment.onTickProcessed();
        }
        while (!onFlushTasks.isEmpty()) {
            onFlushTasks.poll().run();
        }
        if (this.agentEnvironment != null) {
            this.agentEnvironment.onTickEnd();
        }
    }

    public void onHitCalculated(PendingHit hit) {
        final AgentEnvironment agentEnvironment = this.agentEnvironment;
        if (agentEnvironment != null) {
            agentEnvironment.onHitCalculated(hit);
        }
    }

    public void onHitApplied(HitDamage hit) {
        final AgentEnvironment agentEnvironment = this.agentEnvironment;
        if (agentEnvironment != null) {
            agentEnvironment.onHitApplied(hit);
        }
    }

    public void onLogout() {
        this.executorService.shutdown();
    }

    private void checkAttacker() {
        if (this.agentEnvironment != null) {
            return;
        }
        final Player nextTarget;
        if (this.agent.getCombat().getAttacker() instanceof Player target) {
            nextTarget = target;
        } else if (this.agent.getCombat().getTarget() instanceof Player target) {
            nextTarget = target;
        } else {
            return;
        }
        log.info("Fight started: {} vs. {}", this.agent.getUsername(), nextTarget.getUsername());
        this.agentEnvironment =
                this.environmentDescriptor.createEnvironment(this.agent, nextTarget, null, this.agentParams);
        this.frames = new LinkedList<>();
    }

    private void checkFightComplete() {
        if (this.agentEnvironment == null) {
            return;
        }
        if (isFightComplete()) {
            this.agentEnvironment = null;
            this.frames = null;
            if (this.nextAction != null) {
                this.nextAction.cancel(true);
                this.nextAction = null;
            }
            this.agent.getCombat().setUnderAttack(null);
            this.agent.getCombat().reset();
            this.onFightEnd.run();
        }
    }

    private boolean isFightComplete() {
        return !World.getPlayers().contains(this.agentEnvironment.getTarget())
                || this.agentEnvironment.getTarget().isDying()
                || this.agentEnvironment.getAgent().isDying()
                || !(this.agentEnvironment.getTarget().getArea() instanceof WildernessArea)
                || !(this.agentEnvironment.getAgent().getArea() instanceof WildernessArea);
    }

    private void processAction() {
        if (this.agentEnvironment == null) {
            return;
        }
        handleAction();
        this.onFlushTasks.add(this::requestNewAction);
    }

    private void handleAction() {
        if (this.nextAction != null) {
            if (this.nextAction.isDone()) {
                try {
                    final PvpClient.Response actionResponse = this.nextAction.get();
                    this.nextAction = null;
                    this.agentEnvironment.processAction(actionResponse.getAction());
                } catch (Exception e) {
                    log.error("Error processing action", e);
                }
            } else {
                // Somehow, we missed a tick
                this.nextAction.cancel(true);
                this.nextAction = null;
                log.warn("Missed a tick");
            }
        }
    }

    private void requestNewAction() {
        final List<Number> obs = agentEnvironment.getObs();
        this.frames.addFirst(obs);
        while (this.frames.size() > this.stackFrames) {
            this.frames.pollLast();
        }
        final PvpClient.Request actionRequest = PvpClient.Request.builder()
                .actionMasks(agentEnvironment.getActionMasks())
                .deterministic(this.deterministic)
                .model(this.model)
                .obs(this.frames)
                .build();
        final CompletableFuture<PvpClient.Response> nextAction = new CompletableFuture<>();
        this.nextAction = nextAction;
        this.executorService.submit(() -> {
            try {
                nextAction.complete(this.pvpClient.sendRequest(actionRequest));
            } catch (IOException e) {
                log.error("Failed to get action", e);
            }
        });
    }
}
