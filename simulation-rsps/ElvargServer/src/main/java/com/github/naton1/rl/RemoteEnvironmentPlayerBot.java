package com.github.naton1.rl;

import com.elvarg.game.World;
import com.elvarg.game.content.Food;
import com.elvarg.game.content.PrayerHandler;
import com.elvarg.game.content.combat.CombatFactory;
import com.elvarg.game.content.combat.CombatType;
import com.elvarg.game.content.combat.hit.HitDamage;
import com.elvarg.game.content.combat.hit.PendingHit;
import com.elvarg.game.content.presets.Presetables;
import com.elvarg.game.content.skill.skillable.impl.Herblore;
import com.elvarg.game.definition.PlayerBotDefinition;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.entity.impl.playerbot.PlayerBot;
import com.elvarg.game.entity.impl.playerbot.fightstyle.FighterPreset;
import com.elvarg.game.entity.impl.playerbot.interaction.CombatInteraction;
import com.elvarg.game.entity.impl.playerbot.interaction.MovementInteraction;
import com.elvarg.game.event.EventDispatcher;
import com.elvarg.game.event.EventListener;
import com.elvarg.game.event.events.HitAppliedEvent;
import com.elvarg.game.event.events.HitCalculatedEvent;
import com.elvarg.game.event.events.PlayerPacketsFlushedEvent;
import com.elvarg.game.event.events.PlayerPacketsProcessedEvent;
import com.elvarg.game.model.Location;
import com.elvarg.game.model.Skill;
import com.elvarg.game.model.areas.AreaManager;
import com.elvarg.game.model.areas.impl.WildernessArea;
import com.elvarg.util.timers.TimerKey;
import com.github.naton1.rl.env.AgentEnvironment;
import com.github.naton1.rl.env.EnvFightContext;
import com.github.naton1.rl.env.EnvironmentCallback;
import com.github.naton1.rl.env.EnvironmentDescriptor;
import com.github.naton1.rl.env.Loadout;
import com.github.naton1.rl.util.CombatStyles;
import com.github.naton1.rl.util.GameDataUtil;
import com.github.naton1.rl.util.LoggingStateMachineTrace;
import com.github.naton1.rl.util.NoOpCombatInteraction;
import com.github.naton1.rl.util.NoOpEnvironmentCallback;
import com.github.naton1.rl.util.NoOpMovementInteraction;
import com.github.oxo42.stateless4j.StateMachine;
import com.github.oxo42.stateless4j.StateMachineConfig;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import java.lang.reflect.Type;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ThreadLocalRandom;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RemoteEnvironmentPlayerBot extends PlayerBot {

    private static final Gson gson = new Gson();

    private static final String BASELINE_TARGET = "baseline";
    private static final String ATTACKER_TARGET = "attacker";
    private static final int TICKS_WITHOUT_MESSAGE_DURING_STEP_TOLERANCE = 3;
    private static final int MAX_TICKS_WITHOUT_STATE_CHANGE_LENGTH = 500; // 5 minutes
    private static final int MAX_TICKS_WITHOUT_MESSAGE = 1000; // 10 minutes

    private final CombatInteraction noOpCombatInteraction;
    private final MovementInteraction noOpMovementInteraction;

    private final StateMachine<State, Trigger> stateMachine;
    private final EnvironmentDescriptor<?> environmentDescriptor;

    private final String id;

    private volatile RemoteEnvironmentServer.MessageContext messageContext;
    private volatile boolean isLogoutQueued;

    private EpisodeContext episodeContext = new EpisodeContext();

    private final Queue<Runnable> onFlushTasks = new LinkedList<>();

    private final EventListener<HitAppliedEvent> hitAppliedListener;
    private final EventListener<HitCalculatedEvent> hitCalculatedListener;
    private final EventListener<PlayerPacketsFlushedEvent> playerFlushListener;
    private final EventListener<PlayerPacketsProcessedEvent> playerProcessedListener;

    public RemoteEnvironmentPlayerBot(String id, EnvironmentDescriptor<?> environmentDescriptor) {
        super(new PlayerBotDefinition(
                id,
                new Location(3096, 3530),
                environmentDescriptor.getDefaultLoadout().asDummyFighterPreset()));
        this.environmentDescriptor = environmentDescriptor;
        this.id = id;
        this.stateMachine = createStateMachine();
        this.noOpCombatInteraction = new NoOpCombatInteraction(this);
        this.noOpMovementInteraction = new NoOpMovementInteraction(this);
        this.hitAppliedListener = this::onHitApplied;
        this.hitCalculatedListener = this::onHitCalculated;
        this.playerProcessedListener = this::onPlayerPacketsProcessed;
        this.playerFlushListener = this::onPlayerPacketsFlushed;
        EventDispatcher.getGlobal().add(HitAppliedEvent.class, this.hitAppliedListener);
        EventDispatcher.getGlobal().add(PlayerPacketsProcessedEvent.class, this.playerProcessedListener);
        EventDispatcher.getGlobal().add(PlayerPacketsFlushedEvent.class, this.playerFlushListener);
        EventDispatcher.getGlobal().add(HitCalculatedEvent.class, this.hitCalculatedListener);
    }

    synchronized void queueMessage(RemoteEnvironmentServer.MessageContext messageContext) {
        if (this.messageContext != null) {
            // Shouldn't happen - let's make sure
            throw new EnvironmentException("Message already queued");
        }
        this.messageContext = messageContext;
        this.episodeContext.setLastMessageTick(this.episodeContext.getCurrentEpisodeTick());
        notifyAll();
    }

    synchronized boolean isMessageQueued() {
        return this.messageContext != null;
    }

    synchronized void setQueuedForLogout() {
        this.isLogoutQueued = true;
        notifyAll();
    }

    synchronized boolean isLogoutQueued() {
        return this.isLogoutQueued;
    }

    synchronized RemoteEnvironmentServer.AgentDebugInfo getAgentDebugInfo() {
        return RemoteEnvironmentServer.AgentDebugInfo.builder()
                .id(this.id)
                .target(this.episodeContext.getTargetType())
                .state(this.stateMachine.getState().toString())
                .pendingMessage(isMessageQueued())
                .envType(this.environmentDescriptor.getClass().getSimpleName())
                .currentEpisodeTicks(this.episodeContext.getCurrentEpisodeTick())
                .lastMessageTick(this.episodeContext.getLastMessageTick())
                .isLoaded(World.getPlayers().contains(this))
                .isTargetLoaded(World.getPlayers().contains(this.episodeContext.getTarget()))
                .build();
    }

    private synchronized RemoteEnvironmentServer.MessageContext pollMessageContext() {
        final RemoteEnvironmentServer.MessageContext messageContext = this.messageContext;
        this.messageContext = null;
        return messageContext;
    }

    @Override
    public CombatInteraction getCombatInteraction() {
        // Override server's default bot combat interaction
        return this.noOpCombatInteraction;
    }

    @Override
    public MovementInteraction getMovementInteraction() {
        // Override server's default bot movement interaction
        return this.noOpMovementInteraction;
    }

    @Override
    public void onLogout() {
        super.onLogout();
        if (this.episodeContext != null) {
            this.episodeContext.cleanup();
        }
        final RemoteEnvironmentServer.MessageContext messageContext = pollMessageContext();
        if (messageContext != null) {
            messageContext.exception(new EnvironmentException("Agent logged out: " + this.id));
        }
        EnvFightContext.tryReset(getUsername());
        EventDispatcher.getGlobal().remove(HitAppliedEvent.class, this.hitAppliedListener);
        EventDispatcher.getGlobal().remove(PlayerPacketsProcessedEvent.class, this.playerProcessedListener);
        EventDispatcher.getGlobal().remove(PlayerPacketsFlushedEvent.class, this.playerFlushListener);
        EventDispatcher.getGlobal().remove(HitCalculatedEvent.class, this.hitCalculatedListener);
    }

    @Override
    public void heal(int amount) {
        final int startHealth = getHitpoints();
        super.heal(amount);
        final int actualHealedAmount = getHitpoints() - startHealth;
        if (actualHealedAmount > 0) {
            final double healedScale =
                    actualHealedAmount / (double) getSkillManager().getMaxLevel(Skill.HITPOINTS);
            episodeContext.setPlayerHealedScale(episodeContext.getPlayerHealedScale() + healedScale);
            if (episodeContext.getTarget() instanceof RemoteEnvironmentPlayerBot remoteEnvironmentPlayerBot) {
                remoteEnvironmentPlayerBot.episodeContext.setTargetHealedScale(
                        remoteEnvironmentPlayerBot.episodeContext.getTargetHealedScale() + healedScale);
            }
        }
    }

    public boolean isExpired() {
        // automated cleanup mechanism to ensure nothing stays around forever
        return episodeContext.getCurrentEpisodeTick() - episodeContext.getLastMessageTick() > MAX_TICKS_WITHOUT_MESSAGE;
    }

    RemoteEnvironmentServer.Meta getMeta() {
        // Generate metadata for training - should be stateless and able to be called multiple times
        final int damageReceived = GameDataUtil.getDamageReceived(this, getPrimaryHit(), getSecondaryHit());
        final double remainingFoodScale = GameDataUtil.getRemainingFoodScale(this);
        final double remainingBrewScale = GameDataUtil.getRemainingBrewScale(this);
        final int foodCount = GameDataUtil.getRemainingFoodCount(this);
        final CombatType playerPrayerType = GameDataUtil.getPrayerType(this);
        final int targetFoodCount;
        final double targetRemainingFoodScale;
        final double targetRemainingBrewScale;
        final double currentTargetHealthPercent;
        final Player target = episodeContext.getTarget();
        final int distance;
        final int targetFrozenTicks;
        final int targetMaxHealth;
        final CombatType targetAttackType;
        final CombatType targetPrayerType;
        final int damageDealt;
        if (target != null) {
            targetMaxHealth = target.getSkillManager().getMaxLevel(Skill.HITPOINTS);
            targetAttackType = CombatStyles.getCombatType(target);
            targetPrayerType = GameDataUtil.getPrayerType(target);
            targetFoodCount = GameDataUtil.getRemainingFoodCount(target);
            targetRemainingFoodScale = GameDataUtil.getRemainingFoodScale(target);
            targetRemainingBrewScale = GameDataUtil.getRemainingBrewScale(target);
            currentTargetHealthPercent = GameDataUtil.getHealthPercent(target);
            distance = target.getLocation().getDistance(getLocation());
            targetFrozenTicks = target.getTimers().getTicks(TimerKey.FREEZE);
            damageDealt = GameDataUtil.getDamageReceived(
                    target, episodeContext.getLastTargetPrimaryHit(), episodeContext.getLastTargetSecondaryHit());
        } else {
            targetMaxHealth = getSkillManager().getMaxLevel(Skill.HITPOINTS);
            targetFoodCount = 0;
            targetRemainingFoodScale = 0;
            targetRemainingBrewScale = 0;
            currentTargetHealthPercent = 0;
            distance = 0;
            targetFrozenTicks = 0;
            targetAttackType = CombatType.MELEE;
            targetPrayerType = null;
            damageDealt = 0;
        }
        return RemoteEnvironmentServer.Meta.builder()
                .currentHealthPercent(GameDataUtil.getHealthPercent(this))
                .currentTargetHealthPercent(currentTargetHealthPercent)
                .damageDealt(damageDealt / (double) targetMaxHealth)
                .damageReceived(damageReceived / (double) getSkillManager().getMaxLevel(Skill.HITPOINTS))
                .episodeTicks(episodeContext.getCurrentEpisodeTick())
                .protectedPrayer(episodeContext.getProtectedPrayerCorrectly())
                .hitOffPrayer(episodeContext.getHitOffPrayerCorrectly())
                .foodCount(foodCount)
                .targetFoodCount(targetFoodCount)
                .targetRemainingFoodScale(targetRemainingFoodScale)
                .targetRemainingBrewScale(targetRemainingBrewScale)
                .remainingBrewScale(remainingBrewScale)
                .currentPrayerPercent(getSkillManager().getCurrentLevel(Skill.PRAYER)
                        / (double) getSkillManager().getMaxLevel(Skill.PRAYER))
                .attackLevelScale(getSkillManager().getCurrentLevel(Skill.ATTACK)
                        / (double) getSkillManager().getMaxLevel(Skill.ATTACK))
                .strengthLevelScale(getSkillManager().getCurrentLevel(Skill.STRENGTH)
                        / (double) getSkillManager().getMaxLevel(Skill.STRENGTH))
                .defenceLevelScale(getSkillManager().getCurrentLevel(Skill.DEFENCE)
                        / (double) getSkillManager().getMaxLevel(Skill.DEFENCE))
                .rangedLevelScale(getSkillManager().getCurrentLevel(Skill.RANGED)
                        / (double) getSkillManager().getMaxLevel(Skill.RANGED))
                .magicLevelScale(getSkillManager().getCurrentLevel(Skill.MAGIC)
                        / (double) getSkillManager().getMaxLevel(Skill.MAGIC))
                .attackTypeHit(episodeContext.getAttackTypeHit())
                .attackTypeReceived(episodeContext.getAttackTypeReceived())
                .playerPrayerType(episodeContext.getPlayerPrayerType())
                .targetPrayerType(episodeContext.getTargetPrayerType())
                .distance(distance)
                .playerFrozenTicks(getTimers().getTicks(TimerKey.FREEZE))
                .targetFrozenTicks(targetFrozenTicks)
                .remainingFoodScale(remainingFoodScale)
                .wastedFoodScale(episodeContext.getWastedFoodScale())
                .eatenFoodScale(episodeContext.getEatenFoodScale())
                .eatAtFoodScale(episodeContext.getEatAtFoodScale())
                .eatToFoodScale(episodeContext.getEatToFoodScale())
                .wastedBrewScale(episodeContext.getWastedBrewScale())
                .eatAtBrewScale(episodeContext.getEatAtBrewScale())
                .eatToBrewScale(episodeContext.getEatToBrewScale())
                .damageGeneratedOnPlayerScale(episodeContext.getDamageGeneratedOnPlayerScale())
                .damageGeneratedOnTargetScale(episodeContext.getDamageGeneratedOnTargetScale())
                .hitWithSmite(episodeContext.getHitWithSmite())
                .targetAttackedWithSmite(episodeContext.getTargetAttackedWithSmite())
                .playerHealedScale(episodeContext.getPlayerHealedScale())
                .targetHealedScale(episodeContext.getTargetHealedScale())
                .targetObs(
                        episodeContext.getTargetEnvironment() != null
                                ? episodeContext.getTargetEnvironment().getObs()
                                : null)
                .targetActionMasks(
                        episodeContext.getTargetEnvironment() != null
                                ? episodeContext.getTargetEnvironment().getActionMasks()
                                : null)
                .extraDamageDealtOnPlayerScale(episodeContext.getExtraDamageDealtOnPlayerScale())
                .extraDamageDealtOnTargetScale(episodeContext.getExtraDamageDealtOnTargetScale())
                .playerHitAttackSpeed(episodeContext.getPlayerHitAttackSpeed())
                .targetHitAttackSpeed(episodeContext.getTargetHitAttackSpeed())
                .playerAttackStyleType(CombatStyles.getCombatType(this).toString())
                .targetAttackStyleType(targetAttackType.toString())
                .currentPlayerPrayerType(String.valueOf(playerPrayerType))
                .currentTargetPrayerType(String.valueOf(targetPrayerType))
                .build();
    }

    private void onPlayerPacketsProcessed(PlayerPacketsProcessedEvent event) {
        if (event.getPlayer() == this) {
            processAgent();
        }
    }

    private void processAgent() {
        onTickStart();
        if (EnvConfig.isSyncEnabled()) {
            synchronizeAgent();
        }
        handleMessage();
    }

    private void handleMessage() {
        final RemoteEnvironmentServer.MessageContext message = pollMessageContext();
        log.debug(
                "Processing tick in state {} with message {} for {}",
                this.stateMachine.getState(),
                message != null
                        ? (message.getResetRequest() != null ? message.getResetRequest() : message.getStepRequest())
                        : null,
                this.id);
        this.episodeContext.setCurrentEpisodeTick(this.episodeContext.getCurrentEpisodeTick() + 1);
        try {
            trackTerminalState(true);
            // maybe also force log out if no message in some period of time
            if (message == null) {
                trackTicksSinceLastStep();
                return;
            }
            trackStateChanges();
            episodeContext.setMessageContext(message);
            do {
                episodeContext.setCanReprocess(false);
                final Trigger trigger =
                        switch (message.getEpisodeState()) {
                            case RESET -> Trigger.RESET;
                            case ACTION -> Trigger.STEP;
                        };
                stateMachine.fire(trigger);
            } while (episodeContext.isCanReprocess());
            if (!message.isHandled()) {
                // Re-append to process again next tick (ex. reset can take multiple ticks)
                queueMessage(message);
            }
        } catch (Exception e) {
            log.error("Error processing agent", e);
            if (message != null) {
                message.exception(e);
            }
        }
    }

    private void synchronizeAgent() {
        long waitStart = -1;
        synchronized (this) {
            while (!isLogoutQueued() && !isMessageQueued()) {
                if (waitStart == -1) {
                    waitStart = System.currentTimeMillis();
                    log.info("Waiting for {}", this.id);
                }
                try {
                    wait();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        if (waitStart != -1) {
            log.info("Waited {} ms for {}", System.currentTimeMillis() - waitStart, this.id);
        }
    }

    private void trackTicksSinceLastStep() {
        // This should only run if we don't have a message
        if (this.stateMachine.getState() == State.STEPPING) {
            // Other state are okay if we don't have a message for a while, but stepping should be
            // every tick
            episodeContext.setTicksSinceLastStep(episodeContext.getTicksSinceLastStep() + 1);
        }
    }

    private void trackStateChanges() {
        // This should only run if we have a message
        if (episodeContext.getLastTrackedState() != this.stateMachine.getState()) {
            episodeContext.setLastStateChangeTick(episodeContext.getCurrentEpisodeTick());
        }
        final int ticksSinceLast = episodeContext.getLastStateChangeTick() - episodeContext.getCurrentEpisodeTick();
        if (ticksSinceLast > MAX_TICKS_WITHOUT_STATE_CHANGE_LENGTH) {
            throw new EnvironmentException(
                    "Agent has not changed state in over " + ticksSinceLast + " ticks: " + this.id);
        }
    }

    private void trackTerminalState(boolean checkDesync) {
        // This should run every tick so that nothing is missed
        if (this.stateMachine.getState() == State.STEPPING) {
            // The only valid time to move into a terminal state is from stepping
            if (checkDesync && !World.getPlayers().contains(episodeContext.getTarget())) {
                this.sendChat("Target lost.");
                this.episodeContext.setTerminalState(RemoteEnvironmentServer.TerminalState.TARGET_LOST);
            } else if (this.episodeContext.getTarget().isDying()) {
                this.sendChat("We won!");
                this.episodeContext.setTerminalState(RemoteEnvironmentServer.TerminalState.WON);
            } else if (this.isDying()) {
                this.sendChat("We lost :(");
                this.episodeContext.setTerminalState(RemoteEnvironmentServer.TerminalState.LOST);
            } else if (!this.episodeContext.isDeathMatch()
                    && (!GameDataUtil.hasFood(this) || !GameDataUtil.hasFood(this.episodeContext.getTarget()))) {
                this.sendChat("We tied.");
                this.episodeContext.setTerminalState(RemoteEnvironmentServer.TerminalState.TIED);
            } else if (checkDesync && isDesync()) {
                this.sendChat("Target agent desynced.");
                this.episodeContext.setTerminalState(RemoteEnvironmentServer.TerminalState.TARGET_LOST);
            }
            // Entered terminal state, save obs
            if (this.episodeContext.getTerminalState() != null) {
                this.episodeContext.setTerminalObs(
                        this.episodeContext.getEnvironment().getObs());
                this.stateMachine.fire(Trigger.TERMINAL);
            }
        }
    }

    private boolean isDesync() {
        // If the target is not stepping (or reset_complete and not the first tick of the episode)
        if (!(this.episodeContext.getTarget() instanceof RemoteEnvironmentPlayerBot remoteEnvironmentPlayerBot)) {
            return false;
        }
        if (remoteEnvironmentPlayerBot.stateMachine.getState() == State.STEPPING) {
            return false;
        }
        return remoteEnvironmentPlayerBot.stateMachine.getState() != State.RESET_COMPLETE
                || remoteEnvironmentPlayerBot.episodeContext.getCurrentEpisodeTick() > 1;
    }

    private void respondInProgress() {
        episodeContext.getMessageContext().setHandled(true);
        this.onFlushTasks.add(() -> {
            episodeContext
                    .getMessageContext()
                    .respond(RemoteEnvironmentServer.MessageResponse.builder()
                            .obs(this.episodeContext.getEnvironment().getObs())
                            .actionMasks(this.episodeContext.getEnvironment().getActionMasks())
                            .meta(getMeta())
                            .build());
        });
    }

    private void respondTerminalState() {
        episodeContext.getMessageContext().setHandled(true);
        this.onFlushTasks.add(() -> {
            episodeContext
                    .getMessageContext()
                    .respond(RemoteEnvironmentServer.MessageResponse.builder()
                            .obs(episodeContext.getTerminalObs())
                            .actionMasks(this.episodeContext.getEnvironment().getActionMasks())
                            .terminalState(episodeContext.getTerminalState())
                            .meta(getMeta())
                            .build());
        });
    }

    private void onTickStart() {
        final AgentEnvironment agentEnvironment = this.episodeContext.getEnvironment();
        if (agentEnvironment != null) {
            agentEnvironment.onTickStart();
        }
        final AgentEnvironment targetEnvironment = this.episodeContext.getTargetEnvironment();
        if (targetEnvironment != null) {
            targetEnvironment.onTickStart();
        }
    }

    private void onTickEnd() {
        // Clean up data from this tick (generally metadata tracking)
        episodeContext.setProtectedPrayerCorrectly(null);
        episodeContext.setHitOffPrayerCorrectly(null);
        episodeContext.setAttackTypeHit(null);
        episodeContext.setAttackTypeReceived(null);
        episodeContext.setPlayerPrayerType(null);
        episodeContext.setTargetPrayerType(null);
        episodeContext.setWastedFoodScale(null);
        episodeContext.setEatAtFoodScale(null);
        episodeContext.setEatToFoodScale(null);
        episodeContext.setEatenFoodScale(null);
        episodeContext.setWastedBrewScale(null);
        episodeContext.setEatAtBrewScale(null);
        episodeContext.setEatToBrewScale(null);
        episodeContext.setDamageGeneratedOnTargetScale(0D);
        episodeContext.setDamageGeneratedOnPlayerScale(0D);
        episodeContext.setHitWithSmite(null);
        episodeContext.setTargetAttackedWithSmite(null);
        episodeContext.setPlayerHealedScale(0D);
        episodeContext.setTargetHealedScale(0D);
        episodeContext.setLastSelfPrimaryHit(getPrimaryHit());
        episodeContext.setLastSelfSecondaryHit(getSecondaryHit());
        episodeContext.setLastTargetPrimaryHit(
                this.episodeContext.getTarget() != null
                        ? this.episodeContext.getTarget().getPrimaryHit()
                        : null);
        episodeContext.setLastTargetSecondaryHit(
                this.episodeContext.getTarget() != null
                        ? this.episodeContext.getTarget().getSecondaryHit()
                        : null);
        episodeContext.setExtraDamageDealtOnPlayerScale(0D);
        episodeContext.setExtraDamageDealtOnTargetScale(0D);
        episodeContext.setPlayerHitAttackSpeed(null);
        episodeContext.setTargetHitAttackSpeed(null);
    }

    private void onHitCalculated(HitCalculatedEvent event) {
        if (!event.getPendingHit().getAttacker().isPlayer()
                || !event.getPendingHit().getTarget().isPlayer()) {
            return;
        }
        if (event.getPendingHit().getAttacker() != this && event.getPendingHit().getTarget() != this) {
            return;
        }
        final PendingHit hit = event.getPendingHit();
        final AgentEnvironment agentEnvironment = episodeContext.getEnvironment();
        if (agentEnvironment != null) {
            agentEnvironment.onHitCalculated(hit);
        }
        final AgentEnvironment targetEnvironment = episodeContext.getTargetEnvironment();
        if (targetEnvironment != null) {
            targetEnvironment.onHitCalculated(hit);
        }
        final Player attacker = hit.getAttacker().getAsPlayer();
        final Player target = hit.getTarget().getAsPlayer();
        final int prayer = PrayerHandler.getProtectingPrayer(hit.getCombatType());
        final boolean isTargetPrayingCorrect = target.getPrayerActive()[prayer];
        final CombatType targetPrayerType = GameDataUtil.getPrayerType(target);
        final double damageDealtScale =
                hit.getTotalDamage() / (double) target.getSkillManager().getMaxLevel(Skill.HITPOINTS);
        final int attackSpeed = hit.getCombatMethod().attackSpeed(hit.getAttacker());
        if (attacker == this) {
            episodeContext.setHitOffPrayerCorrectly(!isTargetPrayingCorrect);
            episodeContext.setAttackTypeHit(hit.getCombatType().toString());
            episodeContext.setPlayerHitAttackSpeed(attackSpeed);
            if (targetPrayerType != null) {
                episodeContext.setTargetPrayerType(targetPrayerType.toString());
            }
            episodeContext.setDamageGeneratedOnTargetScale(
                    episodeContext.getDamageGeneratedOnTargetScale() + damageDealtScale);
            episodeContext.setHitWithSmite(attacker.getPrayerActive()[PrayerHandler.SMITE]);
        } else if (target == this) {
            episodeContext.setProtectedPrayerCorrectly(isTargetPrayingCorrect);
            episodeContext.setAttackTypeReceived(hit.getCombatType().toString());
            episodeContext.setTargetHitAttackSpeed(attackSpeed);
            if (targetPrayerType != null) {
                episodeContext.setPlayerPrayerType(targetPrayerType.toString());
            }
            episodeContext.setDamageGeneratedOnPlayerScale(
                    episodeContext.getDamageGeneratedOnPlayerScale() + damageDealtScale);
            episodeContext.setTargetAttackedWithSmite(attacker.getPrayerActive()[PrayerHandler.SMITE]);
        }
    }

    private void onHitApplied(HitAppliedEvent event) {
        final HitDamage hit = event.getHitDamage();
        if (!hit.getMetadata().getAttacker().isPlayer()
                || !hit.getMetadata().getTarget().isPlayer()) {
            return;
        }
        if (hit.getMetadata().getAttacker() != this && hit.getMetadata().getTarget() != this) {
            return;
        }
        if (hit.getMetadata().getAssociatedPendingHit() != null) {
            return;
        }
        final AgentEnvironment agentEnvironment = episodeContext.getEnvironment();
        if (agentEnvironment != null) {
            agentEnvironment.onHitApplied(hit);
        }
        final AgentEnvironment targetEnvironment = episodeContext.getTargetEnvironment();
        if (targetEnvironment != null) {
            targetEnvironment.onHitApplied(hit);
        }
        if (hit.getMetadata() != null) {
            final double damageDealtScale = hit.getDamage()
                    / (double) hit.getMetadata()
                            .getTarget()
                            .getAsPlayer()
                            .getSkillManager()
                            .getMaxLevel(Skill.HITPOINTS);
            if (hit.getMetadata().getAttacker() == this) {
                episodeContext.setExtraDamageDealtOnTargetScale(
                        episodeContext.getExtraDamageDealtOnTargetScale() + damageDealtScale);
            } else if (hit.getMetadata().getTarget() == this) {
                episodeContext.setExtraDamageDealtOnPlayerScale(
                        episodeContext.getExtraDamageDealtOnPlayerScale() + damageDealtScale);
            }
        }
    }

    private StateMachine<State, Trigger> createStateMachine() {

        final StateMachineConfig<State, Trigger> agentConfig = new StateMachineConfig<>();
        agentConfig.configure(State.PENDING).permit(Trigger.RESET, State.PROCESSING_RESET, this::setCanReprocess);

        // These all happen from the same reset trigger
        agentConfig.configure(State.PROCESSING_RESET).permitDynamic(Trigger.RESET, this::processReset);
        agentConfig.configure(State.PENDING_TARGET_MATCH).permitDynamic(Trigger.RESET, this::processTargetMatch);
        agentConfig.configure(State.RESET_COMPLETE).permit(Trigger.RESET, State.STEPPING, this::respondInProgress);

        agentConfig
                .configure(State.STEPPING)
                .permit(Trigger.RESET, State.PROCESSING_RESET, this::setCanReprocess)
                .permitReentry(Trigger.STEP, this::processStep)
                .permit(Trigger.TERMINAL, State.TERMINAL);

        agentConfig.configure(State.TERMINAL).permit(Trigger.RESET, State.PROCESSING_RESET, this::setCanReprocess);
        agentConfig.configure(State.TERMINAL).permit(Trigger.STEP, State.PENDING, this::respondTerminalState);

        final StateMachine<State, Trigger> agent = new StateMachine<>(State.PENDING, agentConfig);
        agent.setTrace(new LoggingStateMachineTrace<>(this.id));
        agent.fireInitialTransition();
        return agent;
    }

    private void setCanReprocess() {
        episodeContext.setCanReprocess(true);
    }

    private void processStep() {
        if (BASELINE_TARGET.equals(this.episodeContext.getTargetType())
                && !(this.episodeContext.getTarget().getArea() instanceof WildernessArea)) {
            // The player bots like to wander around, so move it back
            log.warn(
                    "Baseline target moved away from wilderness for {}: {}",
                    this,
                    this.episodeContext.getTarget().getLocation());
            this.episodeContext.getTarget().moveTo(this.getLocation());
            this.episodeContext.getTarget().sendMessage("Moving to assigned target...");
        }
        if (AreaManager.canAttack(this, this.episodeContext.getTarget())
                != CombatFactory.CanAttackResponse.CAN_ATTACK) {
            throw new EnvironmentException(String.format(
                    "Can't attack target '%s'. Player: %s in %s, Target: %s in %s. Debug Info: %s",
                    AreaManager.canAttack(this, this.episodeContext.getTarget()),
                    this.getLocation(),
                    this.getArea(),
                    this.episodeContext.getTarget().getLocation(),
                    this.episodeContext.getTarget().getArea(),
                    getAgentDebugInfo()));
        }
        if (episodeContext.getTicksSinceLastStep() > TICKS_WITHOUT_MESSAGE_DURING_STEP_TOLERANCE) {
            throw new EnvironmentException(
                    "Too many ticks since last step: " + episodeContext.getTicksSinceLastStep() + " for " + this.id);
        }
        episodeContext.setTicksSinceLastStep(0);
        final List<Integer> action =
                episodeContext.getMessageContext().getStepRequest().getAction();
        this.episodeContext.getEnvironment().processAction(action);
        this.respondInProgress();
    }

    private State processTargetMatch() {
        // Now find and wait for a target
        if (episodeContext.getTarget() == null) {
            final String requestedTarget =
                    episodeContext.getMessageContext().getResetRequest().getTarget();
            if (ATTACKER_TARGET.equals(requestedTarget)) {
                // If we're not training, wait to be attacked by someone, then consider them the
                // target for the
                // episode
                if (this.getCombat().getAttacker() != null && this.getCombat().getAttacker() instanceof Player p) {
                    if (p instanceof RemoteEnvironmentPlayerBot) {
                        // Not supported right now (no need) - see edge case below
                        throw new EnvironmentException("Cannot wait to be attacked by an agent");
                    }
                    episodeContext.setTarget(p);
                }
            } else if (requestedTarget.equals(BASELINE_TARGET)) {
                final FighterPreset baselinePreset = environmentDescriptor.getBaselinePreset(getEnvParams());
                episodeContext.setTarget(new PlayerBot(
                        new PlayerBotDefinition("T " + id, getLocation().clone().addX(-1), baselinePreset)));
            } else {
                episodeContext.setTarget(World.getPlayerByName(requestedTarget).orElse(null));
            }
            episodeContext.setTargetType(
                    episodeContext.getMessageContext().getResetRequest().getTarget());
            if (episodeContext.getTarget() == null) {
                return State.PENDING_TARGET_MATCH;
            }
        }
        // Wait for target to be loaded/reset target if not logged in anymore
        if (!World.getPlayers().contains(episodeContext.getTarget())) {
            if (!episodeContext.getTargetType().equals(BASELINE_TARGET)) {
                // Clear out target if they aren't loaded, skip baseline because they may be logging
                // in still
                episodeContext.setTarget(null);
            }
            return State.PENDING_TARGET_MATCH;
        }
        this.episodeContext.setEnvParams(getEnvParams());
        Object targetEnvParams;
        // Special case, make sure we are aligned with the target agent
        if (episodeContext.getTarget() instanceof RemoteEnvironmentPlayerBot remoteEnvironmentPlayerBot) {
            // Note: this does not support 'attacker' - an agent cannot wait to be attacked by
            // another agent
            if (remoteEnvironmentPlayerBot.stateMachine.getState() != State.PENDING_TARGET_MATCH
                    || remoteEnvironmentPlayerBot.episodeContext.getTarget() != this) {
                // Wait for target to be ready and queued onto us,
                // and wait outside of wilderness
                moveToResetTile();
                return State.PENDING_TARGET_MATCH;
            }
            // Now, move the target into a RESET_COMPLETE state too (and don't recurse)
            if (!remoteEnvironmentPlayerBot.episodeContext.isRunningTargetStateMachine()) {
                episodeContext.setRunningTargetStateMachine(true);
                remoteEnvironmentPlayerBot.stateMachine.fire(Trigger.RESET);
                targetEnvParams = remoteEnvironmentPlayerBot.getEnvParams();
                episodeContext.setRunningTargetStateMachine(false);
                if (remoteEnvironmentPlayerBot.stateMachine.getState() != State.RESET_COMPLETE) {
                    throw new EnvironmentException("Failed target matching against agent player bot");
                }
                // Now, let's move to a place to fight
                moveToFightTile(null);
                remoteEnvironmentPlayerBot.moveToFightTile(this);
                EnvFightContext.clear(getUsername());
            } else {
                targetEnvParams = remoteEnvironmentPlayerBot.episodeContext.getEnvParams();
            }
        } else {
            targetEnvParams = getEnvParams();
            EnvFightContext.clear(getUsername());
        }
        EnvFightContext.register(getUsername(), this.episodeContext.target.getUsername());
        final EnvironmentCallback remoteEnvironmentCallback = new RemoteEnvironmentCallback(this.episodeContext);
        final AgentEnvironment environment = this.environmentDescriptor.createEnvironment(
                this, this.episodeContext.target, remoteEnvironmentCallback, getEnvParams());
        this.episodeContext.setEnvironment(environment);
        if (episodeContext.getMessageContext().getResetRequest().isMaintainTargetEnvironment()) {
            if (targetEnvParams == null) {
                throw new IllegalStateException();
            }
            // We assume the target is of the same environment type.
            final AgentEnvironment targetEnvironment = this.environmentDescriptor.createEnvironment(
                    this.episodeContext.target, this, new NoOpEnvironmentCallback(), asDynamicType(targetEnvParams));
            this.episodeContext.setTargetEnvironment(targetEnvironment);
        }
        setCanReprocess();
        return State.RESET_COMPLETE;
    }

    private <T> T asDynamicType(Object o) {
        return (T) o;
    }

    private State processReset() {
        if (this.isDying()
                || (episodeContext.getTarget() != null
                        && episodeContext.getTarget().isDying())) {
            // We'll try again next tick
            episodeContext.setTerminalStateCounter(5);
            return State.PROCESSING_RESET;
        }
        // Wait 5 ticks after dying to reset
        if (episodeContext.getTerminalStateCounter() > 0) {
            episodeContext.setTerminalStateCounter(episodeContext.getTerminalStateCounter() - 1);
            return State.PROCESSING_RESET;
        }
        resetEpisode();
        setCanReprocess();
        return State.PENDING_TARGET_MATCH;
    }

    private void resetEpisode() {
        // Reset combat state, gear/inventory, and position
        this.getCombat().reset();
        this.getCombat().setUnderAttack(null);
        this.getTimers().timers().clear();
        this.setAutoRetaliate(false);
        final RemoteEnvironmentServer.MessageContext messageContext = this.episodeContext.getMessageContext();
        this.episodeContext.cleanup();
        this.episodeContext = new EpisodeContext();
        this.episodeContext.setMessageContext(messageContext); // re-apply message
        if (messageContext != null) {
            this.episodeContext.setIncludeTargetEnvironmentInfo(
                    messageContext.getResetRequest().isMaintainTargetEnvironment());
            this.episodeContext.setDeathMatch(messageContext.getResetRequest().isDeathMatch());
        }
        moveToFightTile(null);
        final Loadout loadout = this.environmentDescriptor.getEnvironmentLoadout(getEnvParams());
        Presetables.load(this, loadout.asPreset());
        sendChat("Reset and ready to go :)");
    }

    private void moveToFightTile(Player nearby) {
        if (this.episodeContext.getMessageContext().getResetRequest() != null
                && !this.episodeContext.getMessageContext().getResetRequest().isTraining()) {
            // Don't randomize if not training
            this.moveTo(this.getDefinition().getSpawnLocation());
            return;
        }
        // Bottom left - 3041, 3530, 0
        // Top right - 3102, 3558, 0
        Location baseTile = new Location(3041, 3530);
        Location topRight = new Location(baseTile.getX() + 61, baseTile.getY() + 28);
        if (nearby != null) {
            final Location nearbyTile = nearby.getLocation();
            if (nearbyTile.getX() < baseTile.getX()
                    || nearbyTile.getY() < baseTile.getY()
                    || nearbyTile.getX() >= topRight.getX()
                    || nearbyTile.getY() >= topRight.getY()) {
                throw new IllegalArgumentException("Cannot move near " + nearbyTile);
            }
            baseTile = new Location(
                    Math.max(baseTile.getX(), nearbyTile.getX() - 5), Math.max(baseTile.getY(), nearbyTile.getY() - 5));
            topRight = new Location(
                    Math.min(topRight.getX(), nearbyTile.getX() + 5), Math.min(topRight.getY(), nearbyTile.getY() + 5));
        }
        final int x = ThreadLocalRandom.current().nextInt(baseTile.getX(), topRight.getX());
        final int y = ThreadLocalRandom.current().nextInt(baseTile.getY(), topRight.getY());
        final Location fightTile = new Location(x, y);
        if (!(AreaManager.get(fightTile) instanceof WildernessArea)) {
            throw new IllegalStateException("Generated tile is not in the wilderness: " + fightTile);
        }
        this.moveTo(fightTile);
    }

    private void moveToResetTile() {
        this.moveTo(new Location(3087, 3520));
    }

    private <T> T getEnvParams() {
        final JsonElement envParams =
                this.episodeContext.messageContext.getResetRequest().getResetParams();
        return gson.fromJson(envParams, (Type) this.environmentDescriptor.getEnvironmentParamsType());
    }

    private enum State {
        PENDING,

        PROCESSING_RESET,
        PENDING_TARGET_MATCH,
        RESET_COMPLETE,

        STEPPING,

        TERMINAL
    }

    private enum Trigger {
        STEP,
        RESET,
        TERMINAL
    }

    @Data
    private static class EpisodeContext {
        private volatile RemoteEnvironmentServer.MessageContext messageContext;
        private boolean canReprocess;

        private boolean deathMatch = true;

        private AgentEnvironment environment;
        private AgentEnvironment targetEnvironment;
        private NoOpEnvironmentCallback environmentCallback;

        private Object envParams;

        private RemoteEnvironmentServer.TerminalState terminalState;
        private List<Number> terminalObs;

        private Player target;
        private String targetType;

        private boolean includeTargetEnvironmentInfo;

        private int terminalStateCounter;
        private boolean isRunningTargetStateMachine;
        private int ticksSinceLastStep;
        private State lastTrackedState;
        private int lastStateChangeTick;
        private int lastMessageTick;
        private int currentEpisodeTick;

        private HitDamage lastTargetPrimaryHit;
        private HitDamage lastTargetSecondaryHit;
        private HitDamage lastSelfPrimaryHit;
        private HitDamage lastSelfSecondaryHit;

        private Boolean protectedPrayerCorrectly;
        private Boolean hitOffPrayerCorrectly;

        private String attackTypeHit;
        private String attackTypeReceived;

        private String targetPrayerType;
        private String playerPrayerType;

        private Boolean hitWithSmite;
        private Boolean targetAttackedWithSmite;

        private Double wastedFoodScale;
        private Double eatAtFoodScale;
        private Double eatToFoodScale;
        private Double eatenFoodScale;

        private Double wastedBrewScale;
        private Double eatAtBrewScale;
        private Double eatToBrewScale;

        private double damageGeneratedOnPlayerScale;
        private double damageGeneratedOnTargetScale;

        private double playerHealedScale;
        private double targetHealedScale;

        private double extraDamageDealtOnTargetScale;
        private double extraDamageDealtOnPlayerScale;

        private Integer targetHitAttackSpeed;
        private Integer playerHitAttackSpeed;

        private String currentTargetPrayerType;
        private String currentPlayerPrayerType;

        private void cleanup() {
            if (this.target != null && this.targetType.equals(BASELINE_TARGET)) {
                // Clean up baseline
                World.getRemovePlayerQueue().add(this.target);
            }
        }
    }

    private void onPlayerPacketsFlushed(PlayerPacketsFlushedEvent event) {
        if (event.getPlayer() != this) {
            return;
        }
        final AgentEnvironment agentEnvironment = this.episodeContext.getEnvironment();
        if (agentEnvironment != null) {
            agentEnvironment.onTickProcessed();
        }
        final AgentEnvironment targetEnvironment = this.episodeContext.getTargetEnvironment();
        if (targetEnvironment != null) {
            targetEnvironment.onTickProcessed();
        }
        trackTerminalState(false);
        while (!onFlushTasks.isEmpty()) {
            onFlushTasks.poll().run();
        }
        if (agentEnvironment != null) {
            agentEnvironment.onTickEnd();
        }
        if (targetEnvironment != null) {
            targetEnvironment.onTickEnd();
        }
        onTickEnd();
    }

    @RequiredArgsConstructor
    private static class RemoteEnvironmentCallback implements EnvironmentCallback {

        private final EpisodeContext episodeContext;

        @Override
        public void onEat(final Player agent, final Food.Edible food) {
            final int currentHealth = agent.getSkillManager().getCurrentLevel(Skill.HITPOINTS);
            final int maxHealth = agent.getSkillManager().getMaxLevel(Skill.HITPOINTS);
            final int anglerBoost = food == Food.Edible.ANGLERFISH ? food.getHeal() : 0;
            final int maxBoostedHp = maxHealth + anglerBoost;
            final int overflow = Math.min(maxBoostedHp - currentHealth - food.getHeal(), 0);
            if (overflow < 0) {
                final Double currentWastedFoodScale = this.episodeContext.getWastedFoodScale();
                this.episodeContext.setWastedFoodScale((currentWastedFoodScale != null ? currentWastedFoodScale : 0D)
                        + (-overflow / (double) maxHealth));
            }
            final Double currentEatenFoodScale = this.episodeContext.getEatenFoodScale();
            this.episodeContext.setEatenFoodScale((currentEatenFoodScale != null ? currentEatenFoodScale : 0D)
                    + (food.getHeal() / (double) maxHealth));
            this.episodeContext.setEatAtFoodScale(currentHealth / (double) maxHealth);
            this.episodeContext.setEatToFoodScale(
                    Math.min(currentHealth + food.getHeal(), maxBoostedHp) / (double) maxHealth);
        }

        @Override
        public void onDrink(final Player agent, final Herblore.PotionDose potionDose) {
            if (potionDose == Herblore.PotionDose.SARADOMIN_BREW) {
                final int heal = GameDataUtil.getBrewHealAmount(agent);

                final int currentHealth = agent.getSkillManager().getCurrentLevel(Skill.HITPOINTS);
                final int maxHealth = agent.getSkillManager().getMaxLevel(Skill.HITPOINTS);
                final int maxBrewHealth = maxHealth + heal;
                final int overflow = Math.min(maxBrewHealth - currentHealth - heal, 0);
                if (overflow < 0) {
                    final Double currentWastedBrewScale = this.episodeContext.getWastedBrewScale();
                    this.episodeContext.setWastedBrewScale(
                            (currentWastedBrewScale != null ? currentWastedBrewScale : 0D)
                                    + (-overflow / (double) maxHealth));
                }
                this.episodeContext.setEatAtBrewScale(currentHealth / (double) maxHealth);
                this.episodeContext.setEatToBrewScale(
                        Math.min(currentHealth + heal, maxBrewHealth) / (double) maxHealth);
            }
        }
    }
}
