package com.github.naton1.rl;

import com.elvarg.game.content.presets.Presetables;
import com.elvarg.game.definition.PlayerBotDefinition;
import com.elvarg.game.entity.impl.playerbot.PlayerBot;
import com.elvarg.game.entity.impl.playerbot.interaction.CombatInteraction;
import com.elvarg.game.event.EventDispatcher;
import com.elvarg.game.event.EventListener;
import com.elvarg.game.event.events.HitAppliedEvent;
import com.elvarg.game.event.events.HitCalculatedEvent;
import com.elvarg.game.event.events.PlayerPacketsFlushedEvent;
import com.elvarg.game.event.events.PlayerPacketsProcessedEvent;
import com.elvarg.game.model.Location;
import com.elvarg.game.model.teleportation.TeleportHandler;
import com.elvarg.game.model.teleportation.TeleportType;
import com.elvarg.game.task.Task;
import com.elvarg.game.task.TaskManager;
import com.github.naton1.rl.env.EnvironmentDescriptor;
import com.github.naton1.rl.util.NoOpCombatInteraction;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AgentPlayerBot<T> extends PlayerBot {

    private static final Location START_TILE = new Location(3096, 3530);

    private final CombatInteraction dummyCombatInteraction;

    private final AgentAdapter<?> agentAdapter;

    private final EventListener<HitAppliedEvent> hitAppliedListener;
    private final EventListener<HitCalculatedEvent> hitCalculatedListener;
    private final EventListener<PlayerPacketsFlushedEvent> playerFlushListener;
    private final EventListener<PlayerPacketsProcessedEvent> playerProcessedListener;

    public AgentPlayerBot(
            String host,
            int port,
            String name,
            String model,
            int stackFrames,
            EnvironmentDescriptor<T> environmentDescriptor,
            T environmentParams,
            boolean deterministic) {
        super(new PlayerBotDefinition(
                name,
                START_TILE,
                environmentDescriptor.getEnvironmentLoadout(environmentParams).asDummyFighterPreset()));
        this.dummyCombatInteraction = new NoOpCombatInteraction(this);
        setAutoRetaliate(false);
        this.agentAdapter = new AgentAdapter<>(
                this,
                model,
                stackFrames,
                environmentDescriptor,
                environmentParams,
                deterministic,
                new PvpClient(host, port),
                this::reset);
        reset();
        this.hitAppliedListener = this::onHitApplied;
        this.hitCalculatedListener = this::onHitCalculated;
        this.playerProcessedListener = this::onPlayerPacketsProcessed;
        this.playerFlushListener = this::onPlayerPacketsFlushed;
        EventDispatcher.getGlobal().add(HitAppliedEvent.class, this.hitAppliedListener);
        EventDispatcher.getGlobal().add(PlayerPacketsProcessedEvent.class, this.playerProcessedListener);
        EventDispatcher.getGlobal().add(PlayerPacketsFlushedEvent.class, this.playerFlushListener);
        EventDispatcher.getGlobal().add(HitCalculatedEvent.class, this.hitCalculatedListener);
    }

    @Override
    public CombatInteraction getCombatInteraction() {
        // Override default combat interaction
        return this.dummyCombatInteraction;
    }

    private void reset() {
        TaskManager.submit(new Task(10) {
            @Override
            protected void execute() {
                Presetables.load(
                        AgentPlayerBot.this, getDefinition().getFighterPreset().getItemPreset());
                TeleportHandler.teleport(
                        AgentPlayerBot.this,
                        AgentPlayerBot.this.getDefinition().getSpawnLocation(),
                        TeleportType.NORMAL,
                        false);
                stop();
            }
        });
    }

    @Override
    public void onLogout() {
        super.onLogout();
        this.agentAdapter.onLogout();
        EventDispatcher.getGlobal().remove(HitAppliedEvent.class, this.hitAppliedListener);
        EventDispatcher.getGlobal().remove(PlayerPacketsProcessedEvent.class, this.playerProcessedListener);
        EventDispatcher.getGlobal().remove(PlayerPacketsFlushedEvent.class, this.playerFlushListener);
        EventDispatcher.getGlobal().remove(HitCalculatedEvent.class, this.hitCalculatedListener);
    }

    private void onHitApplied(HitAppliedEvent event) {
        if (event.getHitDamage().getMetadata().getAttacker() != this
                && event.getHitDamage().getMetadata().getTarget() != this) {
            return;
        }
        if (!event.getHitDamage().getMetadata().getAttacker().isPlayer()
                || !event.getHitDamage().getMetadata().getTarget().isPlayer()) {
            return;
        }
        if (event.getHitDamage().getMetadata().getAssociatedPendingHit() != null) {
            return;
        }
        agentAdapter.onHitApplied(event.getHitDamage());
    }

    private void onHitCalculated(HitCalculatedEvent event) {
        if (!event.getPendingHit().getAttacker().isPlayer()
                || !event.getPendingHit().getTarget().isPlayer()) {
            return;
        }
        if (event.getPendingHit().getAttacker() != this && event.getPendingHit().getTarget() != this) {
            return;
        }
        agentAdapter.onHitCalculated(event.getPendingHit());
    }

    private void onPlayerPacketsProcessed(PlayerPacketsProcessedEvent event) {
        if (event.getPlayer() == this) {
            agentAdapter.processPlayer();
        }
    }

    private void onPlayerPacketsFlushed(PlayerPacketsFlushedEvent event) {
        if (event.getPlayer() == this) {
            agentAdapter.onFlush();
        }
    }
}
