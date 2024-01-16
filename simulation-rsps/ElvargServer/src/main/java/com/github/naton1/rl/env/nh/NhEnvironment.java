package com.github.naton1.rl.env.nh;

import static com.elvarg.util.ItemIdentifiers.AMULET_OF_BLOOD_FURY;
import static com.elvarg.util.ItemIdentifiers.DARK_BOW;
import static com.elvarg.util.ItemIdentifiers.DHAROKS_GREATAXE;
import static com.elvarg.util.ItemIdentifiers.DIAMOND_BOLTS_E_;
import static com.elvarg.util.ItemIdentifiers.DIAMOND_DRAGON_BOLTS_E_;
import static com.elvarg.util.ItemIdentifiers.DRAGONSTONE_DRAGON_BOLTS_E_;
import static com.elvarg.util.ItemIdentifiers.DRAGON_BOLTS_E_;
import static com.elvarg.util.ItemIdentifiers.DRAGON_KNIFE;
import static com.elvarg.util.ItemIdentifiers.HEAVY_BALLISTA;
import static com.elvarg.util.ItemIdentifiers.LIGHT_BALLISTA;
import static com.elvarg.util.ItemIdentifiers.MORRIGANS_JAVELIN;
import static com.elvarg.util.ItemIdentifiers.OPAL_BOLTS_E_;
import static com.elvarg.util.ItemIdentifiers.OPAL_DRAGON_BOLTS_E_;
import static com.elvarg.util.ItemIdentifiers.VOLATILE_NIGHTMARE_STAFF;
import static com.elvarg.util.ItemIdentifiers.ZARYTE_CROSSBOW;
import static com.elvarg.util.ItemIdentifiers.ZURIELS_STAFF;

import com.elvarg.game.collision.RegionManager;
import com.elvarg.game.content.Food;
import com.elvarg.game.content.PotionConsumable;
import com.elvarg.game.content.PrayerHandler;
import com.elvarg.game.content.combat.CombatSpecial;
import com.elvarg.game.content.combat.CombatType;
import com.elvarg.game.content.combat.hit.HitDamage;
import com.elvarg.game.content.combat.hit.PendingHit;
import com.elvarg.game.content.combat.magic.CombatSpells;
import com.elvarg.game.content.combat.magic.EffectSpells;
import com.elvarg.game.content.combat.ranged.RangedData;
import com.elvarg.game.content.skill.skillable.impl.Herblore;
import com.elvarg.game.definition.ItemDefinition;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.EquipmentType;
import com.elvarg.game.model.ItemInSlot;
import com.elvarg.game.model.Location;
import com.elvarg.game.model.MagicSpellbook;
import com.elvarg.game.model.Skill;
import com.elvarg.game.model.areas.AreaManager;
import com.elvarg.game.model.areas.impl.WildernessArea;
import com.elvarg.game.model.container.impl.Equipment;
import com.elvarg.game.model.movement.path.PathFinder;
import com.elvarg.net.packet.impl.EquipPacketListener;
import com.elvarg.util.timers.TimerKey;
import com.github.naton1.rl.env.AgentEnvironment;
import com.github.naton1.rl.env.EnvFightContext;
import com.github.naton1.rl.env.EnvironmentCallback;
import com.github.naton1.rl.util.CircularList;
import com.github.naton1.rl.util.CombatStyles;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.ToIntFunction;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class NhEnvironment implements AgentEnvironment {

    private static final int RECENT_THRESHOLD = 5;

    private static final List<CombatSpecial> combatSpecials = Arrays.asList(
            CombatSpecial.DRAGON_DAGGER,
            CombatSpecial.ARMADYL_GODSWORD,
            CombatSpecial.DRAGON_CLAWS,
            CombatSpecial.GRANITE_MAUL,
            CombatSpecial.VESTAS_LONGSWORD,
            CombatSpecial.ANCIENT_GODSWORD,
            CombatSpecial.STATIUS_WARHAMMER);

    @Getter
    private final Player agent;

    @Getter
    private final Player target;

    @Nullable
    private final EnvironmentCallback environmentCallback;

    private final NhLoadout loadout;

    private final NhEnvironmentParams environmentParams;

    private boolean isTargetLunarSpellbook;

    private double totalDamageDealt;
    private double totalDamageReceived;

    private boolean targetAttackedAgent;
    private boolean agentAttackedTarget;

    private double tickDamageScale;

    private HitDamage lastTargetPrimaryHit;
    private HitDamage lastTargetSecondaryHit;
    private HitDamage lastSelfPrimaryHit;
    private HitDamage lastSelfSecondaryHit;
    // We can't see target health in real-time in the real game - just since the last hitsplat.
    private double lastTargetHealthPercent;

    // We need to store the location at the time of obs,
    // since movement is based on the location and the player could have moved in the same game
    // tick,
    // but somehow we move to the new location as well
    // (which is unrealistic to the actual game, where you'd send a click, and it'd be processed
    // next tick)
    private Location lastObsTargetLocation;

    private int totalTargetPrayCount;
    private int targetPrayMeleeCount;
    private int targetPrayRangeCount;
    private int targetPrayMagicCount;
    private int targetPrayCorrectCount;
    private int playerPrayMeleeCount;
    private int playerPrayRangeCount;
    private int playerPrayMagicCount;

    private int totalTargetHitCount;
    private int targetHitMeleeCount;
    private int targetHitRangeCount;
    private int targetHitMagicCount;
    private int targetHitCorrectCount;
    private int playerHitMeleeCount;
    private int playerHitRangeCount;
    private int playerHitMagicCount;

    private final Queue<CombatType> recentPlayerAttackStyles = new CircularList<>(RECENT_THRESHOLD);
    private final Queue<CombatType> recentPlayerPrayerStyles = new CircularList<>(RECENT_THRESHOLD);
    private final Queue<CombatType> recentTargetAttackStyles = new CircularList<>(RECENT_THRESHOLD);
    private final Queue<CombatType> recentTargetPrayerStyles = new CircularList<>(RECENT_THRESHOLD);
    private final Queue<Boolean> recentTargetPrayerCorrect = new CircularList<>(RECENT_THRESHOLD);
    private final Queue<Boolean> recentTargetHitCorrect = new CircularList<>(RECENT_THRESHOLD);

    private boolean targetPrayedCorrect;
    private boolean playerPrayedCorrect;

    private boolean updatedHitsplats;

    private double damageDealtScale;
    private double damageReceivedScale;

    private int targetMagicAccuracy = -1;
    private int targetMagicStrength = -1;
    private int targetRangedAccuracy = -1;
    private int targetRangedStrength = -1;
    private int targetMeleeAccuracy = -1;
    private int targetMeleeStrength = -1;
    private int targetMeleeGearMagicDefence = -1;
    private int targetMeleeGearRangedDefence = -1;
    private int targetMeleeGearMeleeDefence = -1;
    private int targetMagicGearMagicDefence = -1;
    private int targetMagicGearRangedDefence = -1;
    private int targetMagicGearMeleeDefence = -1;
    private int targetRangedGearMagicDefence = -1;
    private int targetRangedGearRangedDefence = -1;
    private int targetRangedGearMeleeDefence = -1;

    @Override
    public void onHitCalculated(PendingHit pendingHit) {
        if (pendingHit.getAttacker() == getTarget() && pendingHit.getTarget() == getAgent()) {
            this.targetAttackedAgent = true;
            this.totalTargetHitCount += 1;
            this.recentTargetAttackStyles.add(pendingHit.getCombatType());
            if (pendingHit.getCombatType() == CombatType.MAGIC) {
                this.targetHitMagicCount += 1;
                this.targetMagicAccuracy = getTargetCurrentMagicGearAccuracy();
                this.targetMagicStrength = getTargetCurrentMagicGearStrength();
                this.targetMagicGearMagicDefence = getTargetCurrentGearMageDefence();
                this.targetMagicGearMeleeDefence = getTargetCurrentGearMeleeDefence();
                this.targetMagicGearRangedDefence = getTargetCurrentGearRangedDefence();
            } else if (pendingHit.getCombatType() == CombatType.RANGED) {
                this.targetHitRangeCount += 1;
                this.targetRangedAccuracy = getTargetCurrentRangedGearAccuracy();
                this.targetRangedStrength = getTargetCurrentRangedGearStrength();
                this.targetRangedGearMagicDefence = getTargetCurrentGearMageDefence();
                this.targetRangedGearMeleeDefence = getTargetCurrentGearMeleeDefence();
                this.targetRangedGearRangedDefence = getTargetCurrentGearRangedDefence();
            } else if (pendingHit.getCombatType() == CombatType.MELEE) {
                this.targetHitMeleeCount += 1;
                // Don't set for melee spec weapon, which has lower stats
                if (getTargetCurrentMeleeGearStrength() >= this.targetMeleeStrength) {
                    this.targetMeleeAccuracy = getTargetCurrentMeleeGearAccuracy();
                    this.targetMeleeStrength = getTargetCurrentMeleeGearStrength();
                    this.targetMeleeGearMagicDefence = getTargetCurrentGearMageDefence();
                    this.targetMeleeGearMeleeDefence = getTargetCurrentGearMeleeDefence();
                    this.targetMeleeGearRangedDefence = getTargetCurrentGearRangedDefence();
                }
            }
            if (pendingHit.getTarget().getPrayerActive()[PrayerHandler.PROTECT_FROM_MAGIC]) {
                this.playerPrayMagicCount += 1;
                this.recentPlayerPrayerStyles.add(CombatType.MAGIC);
            } else if (pendingHit.getTarget().getPrayerActive()[PrayerHandler.PROTECT_FROM_MISSILES]) {
                this.playerPrayRangeCount += 1;
                this.recentPlayerPrayerStyles.add(CombatType.RANGED);
            } else if (pendingHit.getTarget().getPrayerActive()[PrayerHandler.PROTECT_FROM_MELEE]) {
                this.playerPrayMeleeCount += 1;
                this.recentPlayerPrayerStyles.add(CombatType.MELEE);
            }
            if (!pendingHit.getTarget()
                    .getPrayerActive()[PrayerHandler.getProtectingPrayer(pendingHit.getCombatType())]) {
                this.targetHitCorrectCount += 1;
                this.recentTargetHitCorrect.add(true);
            } else {
                this.playerPrayedCorrect = true;
                this.recentTargetHitCorrect.add(false);
            }
        } else if (pendingHit.getAttacker() == getAgent() && pendingHit.getTarget() == getTarget()) {
            this.recentPlayerAttackStyles.add(pendingHit.getCombatType());
            if (pendingHit.getCombatType() == CombatType.MAGIC) {
                this.playerHitMagicCount += 1;
            } else if (pendingHit.getCombatType() == CombatType.RANGED) {
                this.playerHitRangeCount += 1;
            } else if (pendingHit.getCombatType() == CombatType.MELEE) {
                this.playerHitMeleeCount += 1;
            }
            this.agentAttackedTarget = true;
            this.tickDamageScale = pendingHit.getTotalDamage()
                    / (double) getTarget().getSkillManager().getMaxLevel(Skill.HITPOINTS);
            this.totalTargetPrayCount += 1;
            if (pendingHit.getTarget().getPrayerActive()[PrayerHandler.PROTECT_FROM_MAGIC]) {
                this.targetPrayMagicCount += 1;
                this.recentTargetPrayerStyles.add(CombatType.MAGIC);
            } else if (pendingHit.getTarget().getPrayerActive()[PrayerHandler.PROTECT_FROM_MISSILES]) {
                this.targetPrayRangeCount += 1;
                this.recentTargetPrayerStyles.add(CombatType.RANGED);
            } else if (pendingHit.getTarget().getPrayerActive()[PrayerHandler.PROTECT_FROM_MELEE]) {
                this.targetPrayMeleeCount += 1;
                this.recentTargetPrayerStyles.add(CombatType.MELEE);
            }
            if (pendingHit.getTarget()
                    .getPrayerActive()[PrayerHandler.getProtectingPrayer(pendingHit.getCombatType())]) {
                this.targetPrayCorrectCount += 1;
                this.targetPrayedCorrect = true;
                this.recentTargetPrayerCorrect.add(true);
            } else {
                this.recentTargetPrayerCorrect.add(false);
            }
        }
    }

    @Override
    public void processAction(final List<Integer> action) {
        if (action.size() != 12) {
            throw new IllegalArgumentException(action.toString());
        }
        handleOverheadPrayer(action.get(11));
        handlePrimaryFood(action.get(5));
        handlePotion(action.get(4));
        handleKarambwan(action.get(6));
        handleGear(action.get(8));
        handleVeng(action.get(7));
        handleAttack(action.get(0), action.get(1), action.get(2), action.get(3));
        handleMovement(action.get(9), action.get(10));
    }

    @Override
    public List<Number> getObs() {
        this.lastObsTargetLocation = getTarget().getLocation();
        return List.of(
                isMeleeEquipped() ? 1 : 0,
                isRangedEquipped() ? 1 : 0,
                isMageEquipped() ? 1 : 0,
                isMeleeSpecialWeaponEquipped() ? 1 : 0,
                getSpecialPercentage(),
                isProtectMeleeActive() ? 1 : 0,
                isProtectRangedActive() ? 1 : 0,
                isProtectMagicActive() ? 1 : 0,
                isSmiteActive() ? 1 : 0,
                isRedemptionActive() ? 1 : 0,
                getHealthPercent(),
                getTargetHealthPercent(),
                isTargetMeleeEquipped() ? 1 : 0,
                isTargetRangedEquipped() ? 1 : 0,
                isTargetMageEquipped() ? 1 : 0,
                isTargetMeleeSpecialWeaponEquipped() ? 1 : 0,
                isTargetProtectMeleeActive() ? 1 : 0,
                isTargetProtectRangedActive() ? 1 : 0,
                isTargetProtectMagicActive() ? 1 : 0,
                isTargetSmiteActive() ? 1 : 0,
                isTargetRedemptionActive() ? 1 : 0,
                getTargetSpecialPercentage(),
                getRemainingPotionDoses(Herblore.PotionDose.RANGING_POTION),
                getRemainingPotionDoses(Herblore.PotionDose.SUPER_COMBAT),
                getRemainingPotionDoses(Herblore.PotionDose.SUPER_RESTORE),
                getRemainingPotionDoses(Herblore.PotionDose.SARADOMIN_BREW),
                getFoodCount(),
                getKaramCount(),
                getPrayerPointScale(),
                getPlayerFrozenTicks(),
                getTargetFrozenTicks(),
                getPlayerFrozenImmunityTicks(),
                getTargetFrozenImmunityTicks(),
                isInMeleeRange() ? 1 : 0,
                getRelativeLevel(Skill.STRENGTH),
                getRelativeLevel(Skill.ATTACK),
                getRelativeLevel(Skill.DEFENCE),
                getRelativeLevel(Skill.RANGED),
                getRelativeLevel(Skill.MAGIC),
                getTicksUntilNextAttack(),
                getTicksUntilNextFood(),
                getTicksUntilNextPotionCycle(),
                getTicksUntilNextKaramCycle(),
                getFoodAttackDelay(),
                getTicksUntilNextTargetAttack(),
                getTicksUntilNextTargetPotion(),
                getPendingDamageOnTargetScale(),
                getTicksUntilHitOnTarget(),
                getTicksUntilHitOnPlayer(),
                didPlayerJustAttack() ? 1 : 0,
                didTargetJustAttack() ? 1 : 0,
                getAttackCalculatedDamageScale(),
                getHitsplatsLandedOnAgentScale(),
                getHitsplatsLandedOnTargetScale(),
                isAttackingTarget() ? 1 : 0,
                isMoving() ? 1 : 0,
                isTargetMoving() ? 1 : 0,
                isHavePidOverTarget() ? 1 : 0,
                canCastSpell(CombatSpells.ICE_BARRAGE) ? 1 : 0,
                canCastSpell(CombatSpells.BLOOD_BARRAGE) ? 1 : 0,
                Math.min(getDestinationDistanceToTarget(), 7D),
                Math.min(getDistanceToDestination(), 7D),
                Math.min(getDistanceToTarget(), 7D),
                didPlayerPrayCorrectly() ? 1D : 0D,
                didTargetPrayCorrectly() ? 1D : 0D,
                getDamageDealtScale(),
                getTargetHitConfidence(),
                getTargetHitMeleeCount(),
                getTargetHitMageCount(),
                getTargetHitRangeCount(),
                getPlayerHitMeleeCount(),
                getPlayerHitMageCount(),
                getPlayerHitRangeCount(),
                getTargetHitCorrectCount(),
                getTargetPrayConfidence(),
                getTargetPrayMageCount(),
                getTargetPrayRangeCount(),
                getTargetPrayMeleeCount(),
                getPlayerPrayMageCount(),
                getPlayerPrayRangeCount(),
                getPlayerPrayMeleeCount(),
                getTargetPrayCorrectCount(),
                getRecentTargetHitMeleeCount(),
                getRecentTargetHitMageCount(),
                getRecentTargetHitRangeCount(),
                getRecentPlayerHitMeleeCount(),
                getRecentPlayerHitMageCount(),
                getRecentPlayerHitRangeCount(),
                getRecentTargetHitCorrectCount(),
                getRecentTargetPrayMageCount(),
                getRecentTargetPrayRangeCount(),
                getRecentTargetPrayMeleeCount(),
                getRecentPlayerPrayMageCount(),
                getRecentPlayerPrayRangeCount(),
                getRecentPlayerPrayMeleeCount(),
                getRecentTargetPrayCorrectCount(),
                getAbsoluteLevel(Skill.ATTACK),
                getAbsoluteLevel(Skill.STRENGTH),
                getAbsoluteLevel(Skill.DEFENCE),
                getAbsoluteLevel(Skill.RANGED),
                getAbsoluteLevel(Skill.MAGIC),
                getAbsoluteLevel(Skill.PRAYER),
                getAbsoluteLevel(Skill.HITPOINTS),
                isEnchantedDragonBolts() ? 1 : 0,
                isEnchantedOpalBolts() ? 1 : 0,
                isEnchantedDiamondBolts() ? 1 : 0,
                isMageSpecWeaponInLoadout() ? 1 : 0,
                isRangeSpecWeaponInLoadout() ? 1 : 0,
                isNightmareStaff() ? 1 : 0,
                isZaryteCrossbow() ? 1 : 0,
                isBallista() ? 1 : 0,
                isMorrigansJavelins() ? 1 : 0,
                isDragonKnives() ? 1 : 0,
                isDarkBow() ? 1 : 0,
                isMeleeSpecDclaws() ? 1 : 0,
                isMeleeSpecDds() ? 1 : 0,
                isMeleeSpecAgs() ? 1 : 0,
                isMeleeSpecVls() ? 1 : 0,
                isMeleeSpecStatHammer() ? 1 : 0,
                isMeleeSpecAncientGodsword() ? 1 : 0,
                isMeleeSpecGraniteMaul() ? 1 : 0,
                isBloodFury() ? 1 : 0,
                isDharoksSet() ? 1 : 0,
                isZurielStaff() ? 1 : 0,
                getMagicGearAccuracy(),
                getMagicGearStrength(),
                getRangedGearAccuracy(),
                getRangedGearStrength(),
                getRangedGearAttackSpeed(),
                getRangedGearAttackRange(),
                getMeleeGearAccuracy(),
                getMeleeGearStrength(),
                getMeleeGearAttackSpeed(),
                getMagicGearRangedDefence(),
                getMagicGearMageDefence(),
                getMagicGearMeleeDefence(),
                getRangedGearRangedDefence(),
                getRangedGearMageDefence(),
                getRangedGearMeleeDefence(),
                getMeleeGearRangedDefence(),
                getMeleeGearMageDefence(),
                getMeleeGearMeleeDefence(),
                getTargetCurrentGearRangedDefence(),
                getTargetCurrentGearMageDefence(),
                getTargetCurrentGearMeleeDefence(),
                getTargetLastMagicGearAccuracy(),
                getTargetLastMagicGearStrength(),
                getTargetLastRangedGearAccuracy(),
                getTargetLastRangedGearStrength(),
                getTargetLastMeleeGearAccuracy(),
                getTargetLastMeleeGearStrength(),
                getTargetLastMagicGearRangedDefence(),
                getTargetLastMagicGearMageDefence(),
                getTargetLastMagicGearMeleeDefence(),
                getTargetLastRangedGearRangedDefence(),
                getTargetLastRangedGearMageDefence(),
                getTargetLastRangedGearMeleeDefence(),
                getTargetLastMeleeGearRangedDefence(),
                getTargetLastMeleeGearMageDefence(),
                getTargetLastMeleeGearMeleeDefence(),
                isLms() ? 1 : 0,
                isPvpArena() ? 1 : 0,
                isVengActive() ? 1 : 0,
                isTargetVengActive() ? 1 : 0,
                isPlayerLunarSpellbook() ? 1 : 0,
                isTargetLunarSpellbook() ? 1 : 0,
                getPlayerVengCooldownTicks(),
                getTargetVengCooldownTicks(),
                isBloodAttackAvailable() ? 1 : 0,
                isIceAttackAvailable() ? 1 : 0,
                isMageSpecAttackAvailable() ? 1 : 0,
                isRangedAttackAvailable() ? 1 : 0,
                isRangedSpecAttackAvailable() ? 1 : 0,
                isMeleeAttackAvailable() ? 1 : 0,
                isMeleeSpecAttackAvailable() ? 1 : 0,
                isAnglerfish() ? 1 : 0);
    }

    @Override
    public List<List<Boolean>> getActionMasks() {
        return List.of(
                List.of(
                        !canAttack(),
                        isBloodAttackAvailable() || isIceAttackAvailable() || isMageSpecAttackAvailable(),
                        isRangedAttackAvailable() || isRangedSpecAttackAvailable(),
                        isMeleeAttackAvailable() || isMeleeSpecAttackAvailable()),
                List.of(true, isMeleeAttackAvailable(), isMeleeSpecAttackAvailable()),
                List.of(true, isRangedAttackAvailable(), isRangedSpecAttackAvailable()),
                List.of(true, isIceAttackAvailable(), isBloodAttackAvailable(), isMageSpecAttackAvailable()),
                List.of(
                        true,
                        canUsePotion(Herblore.PotionDose.SARADOMIN_BREW) && canUseBrewBoost(),
                        canUsePotion(Herblore.PotionDose.SUPER_RESTORE) && canRestoreStats(),
                        canUsePotion(Herblore.PotionDose.SUPER_COMBAT) && canBoostCombatSkills(),
                        canUsePotion(Herblore.PotionDose.RANGING_POTION) && canBoostRanged()),
                List.of(true, canEatFood()),
                List.of(true, canEatKarambwan()),
                List.of(true, getPlayerVengCooldownTicks() == 0 && isPlayerLunarSpellbook()),
                List.of(true, canSwitchToTankGear()),
                List.of(
                        true,
                        canMoveAction() && canMoveAdjacentToTarget(),
                        canMoveAction() && canMoveUnderTarget(),
                        canMoveAction() && canMoveToFarcastTile(),
                        canMoveAction() && canMoveDiagonalToTarget()),
                List.of(
                        true,
                        canMoveToFarcastTile(2),
                        canMoveToFarcastTile(3),
                        canMoveToFarcastTile(4),
                        canMoveToFarcastTile(5),
                        canMoveToFarcastTile(6),
                        canMoveToFarcastTile(7)),
                List.of(
                        false,
                        isProtectedPrayerActionAvailable() && canTargetCastMagicSpells(),
                        isProtectedPrayerActionAvailable(),
                        isProtectedPrayerActionAvailable() && isMeleeRangePossible(),
                        isSmiteAvailable(),
                        isRedemptionAvailable()));
    }

    @Override
    public void onTickProcessed() {
        if (isTargetVengActive()) {
            this.isTargetLunarSpellbook = true;
        }
        // Prayer doesn't drain in LMS
        if (isLms()) {
            getAgent()
                    .getSkillManager()
                    .setCurrentLevel(Skill.PRAYER, getAgent().getSkillManager().getMaxLevel(Skill.PRAYER));
        }
    }

    @Override
    public void onTickStart() {
        // PID doesn't swap in PvP Arena
        getAgent().setSwapPid(!isPvpArena());
    }

    @Override
    public void onTickEnd() {
        tryUpdateHitsplats(); // Ensure hitsplat info was updated this tick
        this.updatedHitsplats = false;
        this.damageReceivedScale = 0;
        this.damageDealtScale = 0;
        this.agentAttackedTarget = false;
        this.targetAttackedAgent = false;
        this.tickDamageScale = 0;
        this.playerPrayedCorrect = false;
        this.targetPrayedCorrect = false;
    }

    private double getMagicGearAccuracy() {
        return Arrays.stream(loadout.getMageGear())
                .mapToObj(ItemDefinition::forId)
                .mapToDouble(i -> i.getBonuses()[3])
                .sum();
    }

    private double getMagicGearStrength() {
        return Arrays.stream(loadout.getMageGear())
                .mapToObj(ItemDefinition::forId)
                .mapToDouble(i -> i.getBonuses()[12])
                .sum();
    }

    private double getRangedGearAccuracy() {
        return Arrays.stream(loadout.getRangedGear())
                .mapToObj(ItemDefinition::forId)
                .mapToDouble(i -> i.getBonuses()[4])
                .sum();
    }

    private double getRangedGearStrength() {
        return Arrays.stream(loadout.getRangedGear())
                .mapToObj(ItemDefinition::forId)
                .mapToDouble(i -> i.getBonuses()[11])
                .sum();
    }

    private double getMeleeGearAccuracy() {
        return Arrays.stream(loadout.getMeleeGear())
                .mapToObj(ItemDefinition::forId)
                .mapToDouble(i -> i.getBonuses()[1])
                .sum();
    }

    private double getMeleeGearStrength() {
        return Arrays.stream(loadout.getMeleeGear())
                .mapToObj(ItemDefinition::forId)
                .mapToDouble(i -> i.getBonuses()[10])
                .sum();
    }

    private double getMagicGearRangedDefence() {
        return Arrays.stream(loadout.getMageGear())
                .mapToObj(ItemDefinition::forId)
                .mapToDouble(i -> i.getBonuses()[9])
                .sum();
    }

    private double getMagicGearMageDefence() {
        return Arrays.stream(loadout.getMageGear())
                .mapToObj(ItemDefinition::forId)
                .mapToDouble(i -> i.getBonuses()[8])
                .sum();
    }

    private double getMagicGearMeleeDefence() {
        return Arrays.stream(loadout.getMageGear())
                .mapToObj(ItemDefinition::forId)
                .mapToDouble(i -> i.getBonuses()[6])
                .sum();
    }

    private double getRangedGearMageDefence() {
        return Arrays.stream(loadout.getRangedGear())
                .mapToObj(ItemDefinition::forId)
                .mapToDouble(i -> i.getBonuses()[8])
                .sum();
    }

    private double getRangedGearMeleeDefence() {
        return Arrays.stream(loadout.getRangedGear())
                .mapToObj(ItemDefinition::forId)
                .mapToDouble(i -> i.getBonuses()[6])
                .sum();
    }

    private double getRangedGearRangedDefence() {
        return Arrays.stream(loadout.getRangedGear())
                .mapToObj(ItemDefinition::forId)
                .mapToDouble(i -> i.getBonuses()[9])
                .sum();
    }

    private double getMeleeGearRangedDefence() {
        return Arrays.stream(loadout.getMeleeGear())
                .mapToObj(ItemDefinition::forId)
                .mapToDouble(i -> i.getBonuses()[9])
                .sum();
    }

    private double getMeleeGearMageDefence() {
        return Arrays.stream(loadout.getMeleeGear())
                .mapToObj(ItemDefinition::forId)
                .mapToDouble(i -> i.getBonuses()[8])
                .sum();
    }

    private double getMeleeGearMeleeDefence() {
        return Arrays.stream(loadout.getMeleeGear())
                .mapToObj(ItemDefinition::forId)
                .mapToDouble(i -> i.getBonuses()[6])
                .sum();
    }

    private int getTargetCurrentGearRangedDefence() {
        // Skip ring because you can't actually see that ingame
        return Arrays.stream(target.getEquipment().getItemIdsArray())
                .filter(i -> i > 0)
                .mapToObj(ItemDefinition::forId)
                .filter(i -> i.getEquipmentType() != EquipmentType.RING && i.getEquipmentType() != EquipmentType.ARROWS)
                .mapToInt(i -> i.getBonuses()[9])
                .sum();
    }

    private int getTargetCurrentGearMeleeDefence() {
        // Skip ring because you can't actually see that ingame
        return Arrays.stream(target.getEquipment().getItemIdsArray())
                .filter(i -> i > 0)
                .mapToObj(ItemDefinition::forId)
                .filter(i -> i.getEquipmentType() != EquipmentType.RING && i.getEquipmentType() != EquipmentType.ARROWS)
                .mapToInt(i -> i.getBonuses()[6])
                .sum();
    }

    private int getTargetCurrentGearMageDefence() {
        // Skip ring because you can't actually see that ingame
        return Arrays.stream(target.getEquipment().getItemIdsArray())
                .filter(i -> i > 0)
                .mapToObj(ItemDefinition::forId)
                .filter(i -> i.getEquipmentType() != EquipmentType.RING && i.getEquipmentType() != EquipmentType.ARROWS)
                .mapToInt(i -> i.getBonuses()[8])
                .sum();
    }

    private int getTargetCurrentMagicGearAccuracy() {
        return Arrays.stream(target.getEquipment().getItemIdsArray())
                .filter(i -> i > 0)
                .mapToObj(ItemDefinition::forId)
                .filter(i -> i.getEquipmentType() != EquipmentType.RING && i.getEquipmentType() != EquipmentType.ARROWS)
                .mapToInt(i -> i.getBonuses()[3])
                .sum();
    }

    private int getTargetCurrentMagicGearStrength() {
        return Arrays.stream(target.getEquipment().getItemIdsArray())
                .filter(i -> i > 0)
                .mapToObj(ItemDefinition::forId)
                .filter(i -> i.getEquipmentType() != EquipmentType.RING && i.getEquipmentType() != EquipmentType.ARROWS)
                .mapToInt(i -> i.getBonuses()[12])
                .sum();
    }

    private int getTargetCurrentRangedGearAccuracy() {
        return Arrays.stream(target.getEquipment().getItemIdsArray())
                .filter(i -> i > 0)
                .mapToObj(ItemDefinition::forId)
                .filter(i -> i.getEquipmentType() != EquipmentType.RING && i.getEquipmentType() != EquipmentType.ARROWS)
                .mapToInt(i -> i.getBonuses()[4])
                .sum();
    }

    private int getTargetCurrentRangedGearStrength() {
        return Arrays.stream(target.getEquipment().getItemIdsArray())
                .filter(i -> i > 0)
                .mapToObj(ItemDefinition::forId)
                .filter(i -> i.getEquipmentType() != EquipmentType.RING && i.getEquipmentType() != EquipmentType.ARROWS)
                .mapToInt(i -> i.getBonuses()[11])
                .sum();
    }

    private int getTargetCurrentMeleeGearAccuracy() {
        return Arrays.stream(target.getEquipment().getItemIdsArray())
                .filter(i -> i > 0)
                .mapToObj(ItemDefinition::forId)
                .filter(i -> i.getEquipmentType() != EquipmentType.RING && i.getEquipmentType() != EquipmentType.ARROWS)
                .mapToInt(i -> i.getBonuses()[1])
                .sum();
    }

    private int getTargetCurrentMeleeGearStrength() {
        return Arrays.stream(target.getEquipment().getItemIdsArray())
                .filter(i -> i > 0)
                .mapToObj(ItemDefinition::forId)
                .filter(i -> i.getEquipmentType() != EquipmentType.RING && i.getEquipmentType() != EquipmentType.ARROWS)
                .mapToInt(i -> i.getBonuses()[10])
                .sum();
    }

    private double getTargetLastMagicGearAccuracy() {
        return this.targetMagicAccuracy != -1 ? this.targetMagicAccuracy : getMagicGearAccuracy();
    }

    private double getTargetLastMagicGearStrength() {
        return this.targetMagicStrength != -1 ? this.targetMagicStrength : getMagicGearStrength();
    }

    private double getTargetLastRangedGearAccuracy() {
        return this.targetRangedAccuracy != -1 ? this.targetRangedAccuracy : getRangedGearAccuracy();
    }

    private double getTargetLastRangedGearStrength() {
        return this.targetRangedStrength != -1 ? this.targetRangedStrength : getRangedGearStrength();
    }

    private double getTargetLastMeleeGearAccuracy() {
        return this.targetMeleeAccuracy != -1 ? this.targetMeleeAccuracy : getMeleeGearAccuracy();
    }

    private double getTargetLastMeleeGearStrength() {
        return this.targetMeleeStrength != -1 ? this.targetMeleeStrength : getMeleeGearStrength();
    }

    private double getTargetLastMagicGearRangedDefence() {
        return this.targetMagicGearRangedDefence != -1
                ? this.targetMagicGearRangedDefence
                : getMagicGearRangedDefence();
    }

    private double getTargetLastMagicGearMageDefence() {
        return this.targetMagicGearMagicDefence != -1 ? this.targetMagicGearMagicDefence : getMagicGearMageDefence();
    }

    private double getTargetLastMagicGearMeleeDefence() {
        return this.targetMagicGearMeleeDefence != -1 ? this.targetMagicGearMeleeDefence : getMagicGearMeleeDefence();
    }

    private double getTargetLastRangedGearMageDefence() {
        return this.targetRangedGearMagicDefence != -1 ? this.targetRangedGearMagicDefence : getRangedGearMageDefence();
    }

    private double getTargetLastRangedGearMeleeDefence() {
        return this.targetRangedGearMeleeDefence != -1
                ? this.targetRangedGearMeleeDefence
                : getRangedGearMeleeDefence();
    }

    private double getTargetLastRangedGearRangedDefence() {
        return this.targetRangedGearRangedDefence != -1
                ? this.targetRangedGearRangedDefence
                : getRangedGearRangedDefence();
    }

    private double getTargetLastMeleeGearRangedDefence() {
        return this.targetMeleeGearRangedDefence != -1
                ? this.targetMeleeGearRangedDefence
                : getMeleeGearRangedDefence();
    }

    private double getTargetLastMeleeGearMageDefence() {
        return this.targetMeleeGearMagicDefence != -1 ? this.targetMeleeGearMagicDefence : getMeleeGearMageDefence();
    }

    private double getTargetLastMeleeGearMeleeDefence() {
        return this.targetMeleeGearMeleeDefence != -1 ? this.targetMeleeGearMeleeDefence : getMeleeGearMeleeDefence();
    }

    private boolean isMeleeSpecDds() {
        return getMeleeSpecialWeapon() == CombatSpecial.DRAGON_DAGGER;
    }

    private boolean isMeleeSpecDclaws() {
        return getMeleeSpecialWeapon() == CombatSpecial.DRAGON_CLAWS;
    }

    private boolean isEnchantedOpalBolts() {
        return Arrays.stream(loadout.getRangedGear()).anyMatch(i -> i == OPAL_DRAGON_BOLTS_E_ || i == OPAL_BOLTS_E_);
    }

    private boolean isEnchantedDragonBolts() {
        return Arrays.stream(loadout.getRangedGear())
                .anyMatch(i -> i == DRAGONSTONE_DRAGON_BOLTS_E_ || i == DRAGON_BOLTS_E_);
    }

    private boolean isEnchantedDiamondBolts() {
        return Arrays.stream(loadout.getRangedGear())
                .anyMatch(i -> i == DIAMOND_DRAGON_BOLTS_E_ || i == DIAMOND_BOLTS_E_);
    }

    private boolean isMeleeSpecAgs() {
        return getMeleeSpecialWeapon() == CombatSpecial.ARMADYL_GODSWORD;
    }

    private boolean isBloodFury() {
        return Arrays.stream(loadout.getMeleeGear()).anyMatch(i -> i == AMULET_OF_BLOOD_FURY);
    }

    private boolean isDharoksSet() {
        // Assume it's full DH if we have the axe at least
        return Arrays.stream(loadout.getMeleeGear()).anyMatch(i -> i == DHAROKS_GREATAXE);
    }

    private boolean canCastBloodSpell() {
        return canCastSpell(CombatSpells.BLOOD_RUSH)
                && getAgent().getSkillManager().getCurrentLevel(Skill.HITPOINTS)
                        < getAgent().getSkillManager().getMaxLevel(Skill.HITPOINTS);
    }

    private boolean isIceAttackAvailable() {
        if (isPlayerLunarSpellbook()) {
            return false;
        }
        if (loadout.getMageGear().length == 0) {
            return false;
        }
        return canCastIceSpell() && isAttackAvailable();
    }

    private boolean isBloodAttackAvailable() {
        if (isPlayerLunarSpellbook()) {
            return false;
        }
        if (loadout.getMageGear().length == 0) {
            return false;
        }
        return canCastBloodSpell() && isAttackAvailable();
    }

    private boolean canCastIceSpell() {
        return canCastSpell(CombatSpells.ICE_RUSH);
    }

    private boolean canSwitchToTankGear() {
        // Switch to tank is possible if target can attack next tick
        return getRemainingTicks(false, TimerKey.COMBAT_ATTACK) <= 0;
    }

    private boolean canMoveAdjacentToTarget() {
        final Location adjacent = getClosestAdjacentTile(lastObsTargetLocation);
        return adjacent != null && !getAgent().getLocation().equals(adjacent);
    }

    private boolean canMoveUnderTarget() {
        return getRemainingTicks(false, TimerKey.FREEZE) > 0
                && !getAgent().getLocation().equals(lastObsTargetLocation);
    }

    private boolean canMoveToFarcastTile() {
        return IntStream.range(2, 7).anyMatch(this::canMoveToFarcastTile);
    }

    private boolean canMoveToFarcastTile(int distance) {
        final Location farcastTile = getNearbyFarcastTile(lastObsTargetLocation, distance);
        return farcastTile != null && !farcastTile.equals(agent.getLocation());
    }

    private boolean canMoveDiagonalToTarget() {
        final Location diagonal = getClosestDiagonalTile(lastObsTargetLocation);
        return diagonal != null && !getAgent().getLocation().equals(diagonal);
    }

    private double getSpecialPercentage() {
        return getAgent().getSpecialPercentage();
    }

    private double getTargetSpecialPercentage() {
        return getTarget().getSpecialPercentage();
    }

    private boolean isMeleeEquipped() {
        return !isRangedEquipped() && !isMageEquipped();
    }

    private boolean isRangedEquipped() {
        return CombatStyles.getCombatType(getAgent()) == CombatType.RANGED;
    }

    private boolean isMageEquipped() {
        return CombatStyles.getCombatType(getAgent()) == CombatType.MAGIC;
    }

    private boolean isProtectMeleeActive() {
        return getAgent().getPrayerActive()[PrayerHandler.PROTECT_FROM_MELEE];
    }

    private boolean isProtectRangedActive() {
        return getAgent().getPrayerActive()[PrayerHandler.PROTECT_FROM_MISSILES];
    }

    private boolean isProtectMagicActive() {
        return getAgent().getPrayerActive()[PrayerHandler.PROTECT_FROM_MAGIC];
    }

    private boolean isSmiteActive() {
        return getAgent().getPrayerActive()[PrayerHandler.SMITE];
    }

    private boolean isRedemptionActive() {
        return getAgent().getPrayerActive()[PrayerHandler.REDEMPTION];
    }

    private double getHealthPercent() {
        return getAgent().getSkillManager().getCurrentLevel(Skill.HITPOINTS)
                / (double) getAgent().getSkillManager().getMaxLevel(Skill.HITPOINTS);
    }

    private double getTargetHealthPercent() {
        return this.lastTargetHealthPercent;
    }

    private boolean isTargetMeleeEquipped() {
        return !isTargetRangedEquipped() && !isTargetMageEquipped();
    }

    private boolean isTargetRangedEquipped() {
        return CombatStyles.getCombatType(getTarget()) == CombatType.RANGED;
    }

    private boolean isTargetMageEquipped() {
        return CombatStyles.getCombatType(getTarget()) == CombatType.MAGIC;
    }

    private boolean isTargetProtectMeleeActive() {
        return getTarget().getPrayerActive()[PrayerHandler.PROTECT_FROM_MELEE];
    }

    private boolean isTargetProtectRangedActive() {
        return getTarget().getPrayerActive()[PrayerHandler.PROTECT_FROM_MISSILES];
    }

    private boolean isTargetProtectMagicActive() {
        return getTarget().getPrayerActive()[PrayerHandler.PROTECT_FROM_MAGIC];
    }

    private boolean isTargetSmiteActive() {
        return getTarget().getPrayerActive()[PrayerHandler.SMITE];
    }

    private boolean isTargetRedemptionActive() {
        return getTarget().getPrayerActive()[PrayerHandler.REDEMPTION];
    }

    private double getRemainingPotionDoses(Herblore.PotionDose potionDose) {
        int remainingDoses = getAgent().getInventory().getAmount(potionDose.getDoseID1());
        remainingDoses += getAgent().getInventory().getAmount(potionDose.getDoseID2()) * 2;
        remainingDoses += getAgent().getInventory().getAmount(potionDose.getDoseID3()) * 3;
        remainingDoses += getAgent().getInventory().getAmount(potionDose.getFourDosePotionID()) * 4;
        return remainingDoses;
    }

    private double getFoodCount() {
        return getAgent()
                .getInventory()
                .getAmount(Food.Edible.ANGLERFISH.getItem().getId());
    }

    private double getKaramCount() {
        return getAgent()
                .getInventory()
                .getAmount(Food.Edible.KARAMBWAN.getItem().getId());
    }

    private double getPrayerPointScale() {
        return getAgent().getSkillManager().getCurrentLevel(Skill.PRAYER)
                / (double) getAgent().getSkillManager().getMaxLevel(Skill.PRAYER);
    }

    private double getPlayerFrozenTicks() {
        return getRemainingTicks(true, TimerKey.FREEZE);
    }

    private double getTargetFrozenTicks() {
        return getRemainingTicks(false, TimerKey.FREEZE);
    }

    private double getPlayerFrozenImmunityTicks() {
        return getRemainingTicks(true, TimerKey.FREEZE_IMMUNITY);
    }

    private double getTargetFrozenImmunityTicks() {
        return getRemainingTicks(false, TimerKey.FREEZE_IMMUNITY);
    }

    private boolean isInMeleeRange() {
        return getAgent().getLocation().getDistance(getTarget().getLocation()) == 1
                && !PathFinder.isDiagonalLocation(getAgent(), getTarget());
    }

    private double getRelativeLevel(Skill skill) {
        // Scale stats from min level (0) to max level (max level + max boost) so they will be
        // relatively similar
        // across different stat brackets
        final int max;
        switch (skill) {
            case ATTACK:
            case STRENGTH:
            case DEFENCE:
                max = getAgent().getSkillManager().getMaxLevel(skill)
                        + (int) (Math.floor(getAgent().getSkillManager().getMaxLevel(skill) * 0.15) + 5);
                break;
            case RANGED:
                max = getAgent().getSkillManager().getMaxLevel(skill)
                        + (int) (Math.floor(getAgent().getSkillManager().getMaxLevel(skill) * 0.1) + 4);
                break;
            default:
                max = getAgent().getSkillManager().getMaxLevel(skill);
                break;
        }
        return getAgent().getSkillManager().getCurrentLevel(skill) / (double) max;
    }

    private double getAbsoluteLevel(Skill skill) {
        return getAgent().getSkillManager().getMaxLevel(skill);
    }

    private double getFoodAttackDelay() {
        // Cap at -3 because anything lower is useless, eating won't prevent an attack
        // And cap at 0 because anything higher won't provide more info, can't currently attack,
        // and another observation will give the actual attack delay.
        // This obs should tell us how close we are to being able to eat without impacting attack
        // delay.
        final int attackDelay = getAgent().getTimers().getUncappedTicks(TimerKey.COMBAT_ATTACK, -100) - 1;
        return Math.min(Math.max(attackDelay, -3), 0) + 3;
    }

    private double getTicksUntilNextAttack() {
        return getRemainingTicks(true, TimerKey.COMBAT_ATTACK);
    }

    private double getTicksUntilNextFood() {
        return getRemainingTicks(true, TimerKey.FOOD);
    }

    private double getTicksUntilNextPotionCycle() {
        return getRemainingTicks(true, TimerKey.POTION);
    }

    private double getTicksUntilNextKaramCycle() {
        return getRemainingTicks(true, TimerKey.KARAMBWAN);
    }

    private double getTicksUntilNextTargetAttack() {
        return getRemainingTicks(false, TimerKey.COMBAT_ATTACK);
    }

    private double getTicksUntilNextTargetPotion() {
        return getRemainingTicks(false, TimerKey.FOOD);
    }

    private double getPendingDamageOnTargetScale() {
        return getTarget().getCombat().getHitQueue().getAllAccumulatedDamage()
                / (double) getTarget().getSkillManager().getMaxLevel(Skill.HITPOINTS);
    }

    private int getTicksUntilHitOnTarget() {
        return getTarget().getCombat().getHitQueue().getTicksUntilNextHit();
    }

    private int getTicksUntilHitOnPlayer() {
        return getAgent().getCombat().getHitQueue().getTicksUntilNextHit();
    }

    private boolean isAttackingTarget() {
        return getAgent().getCombat().getTarget() == getTarget();
    }

    private boolean isMoving() {
        return getAgent().getMovementQueue().isMoving();
    }

    private boolean isTargetMoving() {
        return getTarget().getMovementQueue().isMoving();
    }

    private double getDistanceToTarget() {
        return getAgent().getLocation().getDistance(getTarget().getLocation());
    }

    private double getDestinationDistanceToTarget() {
        final Location destination = getAgent().getMovementQueue().getDestination();
        if (destination == null) {
            return getTarget().getLocation().getDistance(getAgent().getLocation());
        }
        return getTarget().getLocation().getDistance(destination);
    }

    private double getDistanceToDestination() {
        final Location destination = getAgent().getMovementQueue().getDestination();
        if (destination == null) {
            return 0D;
        }
        return getAgent().getLocation().getDistance(destination);
    }

    private void handlePotion(final int action) {
        final Herblore.PotionDose potionDose;
        if (action == 0) {
            return;
        } else if (action == 1) {
            potionDose = Herblore.PotionDose.SARADOMIN_BREW;
        } else if (action == 2) {
            potionDose = Herblore.PotionDose.SUPER_RESTORE;
        } else if (action == 3) {
            potionDose = Herblore.PotionDose.SUPER_COMBAT;
        } else if (action == 4) {
            potionDose = Herblore.PotionDose.RANGING_POTION;
        } else {
            throw new IllegalArgumentException(String.valueOf(action));
        }
        final int[] ids = {
            potionDose.getDoseID1(), potionDose.getDoseID2(), potionDose.getDoseID3(), potionDose.getFourDosePotionID()
        };
        final ItemInSlot itemInSlot =
                ItemInSlot.getFromInventory(ids, getAgent().getInventory());
        if (itemInSlot != null) {
            if (environmentCallback != null) {
                environmentCallback.onDrink(getAgent(), potionDose);
            }
            PotionConsumable.drink(getAgent(), itemInSlot.getId(), itemInSlot.getSlot());
        }
    }

    private void handlePrimaryFood(final int action) {
        if (action == 0) {
            return;
        } else if (action == 1) {
            final ItemInSlot itemInSlot = getEdibleItemSlot();
            if (itemInSlot != null) {
                final Food.Edible food = Arrays.stream(Food.Edible.values())
                        .filter(e -> e.getItem().getId() == itemInSlot.getId())
                        .findFirst()
                        .orElseThrow();
                if (environmentCallback != null) {
                    environmentCallback.onEat(getAgent(), food);
                }
                Food.consume(getAgent(), itemInSlot.getId(), itemInSlot.getSlot());
            }
        } else {
            throw new IllegalArgumentException(String.valueOf(action));
        }
    }

    private void handleKarambwan(final int action) {
        if (action == 0) {
            return;
        } else if (action == 1) {
            final ItemInSlot itemInSlot = getKarambwanItemSlot();
            if (itemInSlot != null) {
                if (environmentCallback != null) {
                    environmentCallback.onEat(getAgent(), Food.Edible.KARAMBWAN);
                }
                Food.consume(getAgent(), itemInSlot.getId(), itemInSlot.getSlot());
            }
        } else {
            throw new IllegalArgumentException(String.valueOf(action));
        }
    }

    private void handleMovement(final int action, final int farcastAction) {
        // These actions should be no-ops if already in that position
        // (ex. move adjacent to target, if already adjacent then don't move)
        if (this.lastObsTargetLocation == null) {
            throw new IllegalStateException();
        }
        final Location destination;
        final boolean skipIfAtDestination;
        if (action == 0) {
            return;
        } else if (action == 1) {
            destination = getClosestAdjacentTile(this.lastObsTargetLocation);
            skipIfAtDestination = true;
            if (destination == null) {
                log.warn(
                        "No walkable adjacent tile for target from {} to {}",
                        getAgent().getLocation(),
                        this.lastObsTargetLocation);
                return;
            }
        } else if (action == 2) {
            destination = this.lastObsTargetLocation;
            skipIfAtDestination = false;
        } else if (action == 3) {
            final int farcastDistance = farcastAction + 1;
            destination = getNearbyFarcastTile(this.lastObsTargetLocation, farcastDistance);
            skipIfAtDestination = true;
            if (destination == null) {
                log.warn(
                        "No valid locations to walk to around {} from {} for {}",
                        getTarget().getLocation(),
                        this.lastObsTargetLocation,
                        this);
                return;
            }
        } else if (action == 4) {
            destination = getClosestDiagonalTile(this.lastObsTargetLocation);
            skipIfAtDestination = true;
            if (destination == null) {
                log.warn(
                        "No walkable diagonal tile for target from {} to {}",
                        getAgent().getLocation(),
                        this.lastObsTargetLocation);
                return;
            }
        } else {
            throw new IllegalArgumentException(String.valueOf(action));
        }
        if (!(AreaManager.get(destination) instanceof WildernessArea)) {
            // Sanity check, should always be in the wilderness
            log.warn("Moving to non-wilderness location: {}", destination);
        }
        if (skipIfAtDestination && getAgent().getLocation().equals(destination)) {
            return;
        }
        getAgent().getMovementQueue().reset();
        getAgent().getMovementQueue().walkToReset();
        PathFinder.calculateWalkRoute(getAgent(), destination.getX(), destination.getY());
    }

    private void handleOverheadPrayer(final int action) {
        if (action == 0) {
            // no-op
            return;
        }
        if (action == 1) {
            PrayerHandler.activatePrayer(getAgent(), PrayerHandler.PrayerData.PROTECT_FROM_MAGIC);
        } else if (action == 2) {
            PrayerHandler.activatePrayer(getAgent(), PrayerHandler.PrayerData.PROTECT_FROM_MISSILES);
        } else if (action == 3) {
            PrayerHandler.activatePrayer(getAgent(), PrayerHandler.PrayerData.PROTECT_FROM_MELEE);
        } else if (action == 4) {
            PrayerHandler.activatePrayer(getAgent(), PrayerHandler.PrayerData.SMITE);
        } else if (action == 5) {
            PrayerHandler.activatePrayer(getAgent(), PrayerHandler.PrayerData.REDEMPTION);
        } else {
            throw new IllegalArgumentException(String.valueOf(action));
        }
    }

    private void handleVeng(final int vengTypeAction) {
        if (vengTypeAction == 0) {
            // no-op
            return;
        } else if (vengTypeAction == 1) {
            EffectSpells.handleSpell(
                    getAgent(), EffectSpells.EffectSpell.VENGEANCE.getSpell().spellId());
        } else {
            throw new IllegalArgumentException(String.valueOf(vengTypeAction));
        }
    }

    private void handleAttack(
            final int attackTypeAction, final int meleeType, final int rangedType, final int mageType) {
        if (attackTypeAction == 0) {
            // no-op
            return;
        } else if (attackTypeAction == 1) {
            equip(loadout.getMageGear());
            if (getAgent().getSkillManager().getCurrentLevel(Skill.PRAYER) > 0) {
                for (PrayerHandler.PrayerData prayer : loadout.getMagePrayers()) {
                    PrayerHandler.activatePrayer(getAgent(), prayer);
                }
            }
            if (mageType == 0) {
                // No-op spell, invalid state
                throw new IllegalStateException(String.valueOf(mageType));
            } else if (mageType == 1) {
                final CombatSpells spell = Stream.of(
                                CombatSpells.ICE_BARRAGE,
                                CombatSpells.ICE_BLITZ,
                                CombatSpells.ICE_BURST,
                                CombatSpells.ICE_RUSH)
                        .filter(this::canCastSpell)
                        .findFirst()
                        .orElse(CombatSpells.ICE_RUSH);
                getAgent().getCombat().setCastSpell(spell.getSpell());
                getAgent().setSpecialActivated(false);
            } else if (mageType == 2) {
                final CombatSpells spell = Stream.of(
                                CombatSpells.BLOOD_BARRAGE,
                                CombatSpells.BLOOD_BLITZ,
                                CombatSpells.BLOOD_BURST,
                                CombatSpells.BLOOD_RUSH)
                        .filter(this::canCastSpell)
                        .findFirst()
                        .orElse(CombatSpells.BLOOD_RUSH);
                getAgent().getCombat().setCastSpell(spell.getSpell());
                getAgent().setSpecialActivated(false);
            } else if (mageType == 3) {
                getAgent().getCombat().setCastSpell(null);
                if (!getAgent().isSpecialActivated()) {
                    CombatSpecial.activate(getAgent());
                }
            } else {
                throw new IllegalArgumentException(String.valueOf(mageType));
            }
        } else if (attackTypeAction == 2) {
            equip(loadout.getRangedGear());
            getAgent().getCombat().setCastSpell(null);
            if (getAgent().getSkillManager().getCurrentLevel(Skill.PRAYER) > 0) {
                for (PrayerHandler.PrayerData prayer : loadout.getRangedPrayers()) {
                    PrayerHandler.activatePrayer(getAgent(), prayer);
                }
            }
            if (rangedType == 0) {
                // No-op type, invalid state
                throw new IllegalStateException(String.valueOf(rangedType));
            } else if (rangedType == 1) {
                getAgent().setSpecialActivated(false);
            } else if (rangedType == 2) {
                if (!getAgent().isSpecialActivated()) {
                    CombatSpecial.activate(getAgent());
                }
            } else {
                throw new IllegalArgumentException(String.valueOf(rangedType));
            }
        } else if (attackTypeAction == 3) {
            if (getAgent().getSkillManager().getCurrentLevel(Skill.PRAYER) > 0) {
                for (PrayerHandler.PrayerData prayer : loadout.getMeleePrayers()) {
                    PrayerHandler.activatePrayer(getAgent(), prayer);
                }
            }
            getAgent().getCombat().setCastSpell(null);
            if (meleeType == 0) {
                // No-op type, invalid state
                throw new IllegalStateException(String.valueOf(meleeType));
            } else if (meleeType == 1) {
                equip(loadout.getMeleeGear());
                getAgent().setSpecialActivated(false);
            } else if (meleeType == 2) {
                equip(loadout.getMeleeSpecGear());
                // Special case - gmaul, use all specs
                if (isMeleeSpecGraniteMaul()) {
                    final int specs = (int) (getSpecialPercentage()
                            / getAgent().getCombatSpecial().getDrainAmount());
                    for (int i = 0; i < specs; i++) {
                        CombatSpecial.activate(getAgent());
                    }
                } else if (!getAgent().isSpecialActivated()) {
                    CombatSpecial.activate(getAgent());
                }
            } else {
                throw new IllegalArgumentException(String.valueOf(meleeType));
            }
        } else {
            throw new IllegalArgumentException(String.valueOf(attackTypeAction));
        }
        getAgent().getCombat().attack(getTarget());
    }

    private void handleGear(int gearTypeAction) {
        if (gearTypeAction == 0) {
            return;
        } else if (gearTypeAction == 1) {
            // tank gear
            equip(loadout.getTankGear());
            if (getAgent().getSkillManager().getCurrentLevel(Skill.PRAYER) > 0) {
                for (PrayerHandler.PrayerData prayer : loadout.getMagePrayers()) {
                    PrayerHandler.activatePrayer(getAgent(), prayer);
                }
            }
        } else {
            throw new IllegalArgumentException(String.valueOf(gearTypeAction));
        }
    }

    private Location getNearbyFarcastTile(Location targetLocation, int distance) {
        return PathFinder.getTilesForDistance(targetLocation, distance).stream()
                .filter(t -> RegionManager.canMove(
                        getAgent().getLocation(),
                        t,
                        getAgent().size(),
                        getAgent().size(),
                        getAgent().getPrivateArea()))
                .filter(t -> AreaManager.get(t) instanceof WildernessArea)
                .min(Comparator.<Location>comparingInt(
                                l -> l.getDistance(getAgent().getLocation()))
                        .thenComparingInt(l -> l.getDistance(targetLocation))
                        .thenComparingInt(getConsistentRandomFunction()))
                .orElse(null);
    }

    private Location getClosestAdjacentTile(Location targetLocation) {
        return Stream.of(
                        targetLocation.clone().add(0, 1),
                        targetLocation.clone().add(1, 0),
                        targetLocation.clone().add(0, -1),
                        targetLocation.clone().add(-1, 0))
                .filter(l -> RegionManager.canMove(
                        getAgent().getLocation(),
                        l,
                        getAgent().size(),
                        getAgent().size(),
                        getAgent().getPrivateArea()))
                .filter(l -> AreaManager.get(l) instanceof WildernessArea)
                .min(Comparator.<Location>comparingInt(
                                l -> l.getDistance(getAgent().getLocation()))
                        .thenComparingInt(l -> l.getDistance(targetLocation))
                        .thenComparingInt(getConsistentRandomFunction()))
                .orElse(null);
    }

    private Location getClosestDiagonalTile(Location targetLocation) {
        return Stream.of(
                        targetLocation.clone().add(1, 1),
                        targetLocation.clone().add(1, -1),
                        targetLocation.clone().add(-1, -1),
                        targetLocation.clone().add(-1, 1))
                .filter(l -> RegionManager.canMove(
                        getAgent().getLocation(),
                        l,
                        getAgent().size(),
                        getAgent().size(),
                        getAgent().getPrivateArea()))
                .filter(l -> AreaManager.get(l) instanceof WildernessArea)
                .min(Comparator.<Location>comparingInt(
                                l -> l.getDistance(getAgent().getLocation()))
                        .thenComparingInt(l -> l.getDistance(targetLocation))
                        .thenComparingInt(getConsistentRandomFunction()))
                .orElse(null);
    }

    private <T> ToIntFunction<T> getConsistentRandomFunction() {
        final Map<T, Integer> cache = new HashMap<>();
        return t -> cache.computeIfAbsent(t, v -> ThreadLocalRandom.current().nextInt());
    }

    private boolean canEatFood() {
        if (getRemainingTicks(true, TimerKey.FOOD) > 0) {
            return false;
        }
        final ItemInSlot food = getEdibleItemSlot();
        if (food == null) {
            return false;
        }
        if (food.getId() == Food.Edible.ANGLERFISH.getItem().getId()) {
            // Allow anglers to heal over max hp (don't eat at 100+ though)
            return getAgent().getHitpoints() <= getAgent().getSkillManager().getMaxLevel(Skill.HITPOINTS);
        }
        return getAgent().getHitpoints() < getAgent().getSkillManager().getMaxLevel(Skill.HITPOINTS);
    }

    private boolean canEatKarambwan() {
        if (getRemainingTicks(true, TimerKey.KARAMBWAN) > 0) {
            return false;
        }
        return getKarambwanItemSlot() != null
                && getAgent().getHitpoints() < getAgent().getSkillManager().getMaxLevel(Skill.HITPOINTS);
    }

    private ItemInSlot getEdibleItemSlot() {
        return Arrays.stream(Food.Edible.values())
                .filter(f -> f != Food.Edible.KARAMBWAN)
                .map(food -> ItemInSlot.getFromInventory(
                        food.getItem().getId(), getAgent().getInventory()))
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    private ItemInSlot getKarambwanItemSlot() {
        return Arrays.stream(Food.Edible.values())
                .filter(f -> f == Food.Edible.KARAMBWAN)
                .map(food -> ItemInSlot.getFromInventory(
                        food.getItem().getId(), getAgent().getInventory()))
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    private void equip(int... itemIds) {
        for (int itemId : itemIds) {
            final ItemInSlot item =
                    ItemInSlot.getFromInventory(itemId, getAgent().getInventory());
            if (item == null) {
                continue;
            }
            EquipPacketListener.equipFromInventory(getAgent(), item);
        }
        for (int itemId : getAgent().getEquipment().getItemIdsArray()) {
            if (itemId == -1) {
                continue;
            }
            if (Arrays.stream(itemIds).anyMatch(i -> i == itemId)) {
                continue;
            }
            final int itemSlot = getAgent().getEquipment().getSlotForItemId(itemId);
            EquipPacketListener.unequip(getAgent(), itemSlot, itemId);
        }
    }

    private boolean isProtectedPrayerActionAvailable() {
        if (this.environmentParams.isOnlySwitchPrayerWhenAboutToAttack()
                && getRemainingTicks(false, TimerKey.COMBAT_ATTACK) > 0) {
            return false;
        }
        return getAgent().getSkillManager().getCurrentLevel(Skill.PRAYER) > 0;
    }

    private boolean isAttackAvailable() {
        if (this.environmentParams.isOnlySwitchGearWhenAttackSoon() && !canAttack()) {
            return false;
        }
        return true;
    }

    private boolean canAttack() {
        return getRemainingTicks(true, TimerKey.COMBAT_ATTACK) == 0;
    }

    private boolean isGraniteMaulAttackAvailable() {
        return getMeleeSpecialWeapon() == CombatSpecial.GRANITE_MAUL
                && getSpecialPercentage() >= CombatSpecial.GRANITE_MAUL.getDrainAmount();
    }

    private boolean isRangedAttackAvailable() {
        if (!isAttackAvailable()) {
            return false;
        }
        return !isRangedWeaponTwoHanded() || canEquipTwoHandedWeapon();
    }

    private boolean isMeleeAttackAvailable() {
        if (!isAttackAvailable()) {
            return false;
        }
        return canMelee() && (!isMeleeWeaponTwoHanded() || canEquipTwoHandedWeapon());
    }

    private boolean isMeleeSpecAttackAvailable() {
        if (!isGraniteMaulAttackAvailable() && !isAttackAvailable()) {
            return false;
        }
        return canMelee() && (!isSpecWeaponTwoHanded() || canEquipTwoHandedWeapon()) && canSpec();
    }

    private boolean isSmiteAvailable() {
        if (!this.environmentParams.isAllowSmite()) {
            return false;
        }
        // Not available in LMS
        if (isLms()) {
            return false;
        }
        // Smite is only useful if we can attack
        if (getRemainingTicks(true, TimerKey.COMBAT_ATTACK) > 0) {
            return false;
        }
        return getAgent().getSkillManager().getCurrentLevel(Skill.PRAYER) > 0;
    }

    private boolean isRedemptionAvailable() {
        if (!this.environmentParams.isAllowRedemption()) {
            return false;
        }
        // Not available in LMS
        if (isLms()) {
            return false;
        }
        // Don't allow redemption if we have food
        if (getFoodCount() > 0
                || getKaramCount() > 0
                || getRemainingPotionDoses(Herblore.PotionDose.SARADOMIN_BREW) > 0) {
            return false;
        }
        // Don't allow unless there is a possible hit on the player (hit pending, or target can
        // attack)
        if (getTicksUntilHitOnPlayer() < 0 && getRemainingTicks(false, TimerKey.COMBAT_ATTACK) > 0) {
            return false;
        }
        return getAgent().getSkillManager().getCurrentLevel(Skill.PRAYER) > 0;
    }

    private boolean canUsePotion(Herblore.PotionDose potionDose) {
        if (getRemainingTicks(true, TimerKey.POTION) > 0) {
            return false;
        }
        final int[] ids = {
            potionDose.getDoseID1(), potionDose.getDoseID2(), potionDose.getDoseID3(), potionDose.getFourDosePotionID()
        };
        return getAgent().getInventory().containsAny(ids);
    }

    private boolean canRestoreStats() {
        return Stream.of(Skill.ATTACK, Skill.DEFENCE, Skill.STRENGTH, Skill.RANGED, Skill.MAGIC, Skill.PRAYER)
                .anyMatch(s -> getAgent().getSkillManager().getCurrentLevel(s)
                        < getAgent().getSkillManager().getMaxLevel(s));
    }

    private boolean canBoostCombatSkills() {
        // Add +1, so it doesn't re-pot at just a single level boost
        return Stream.of(Skill.ATTACK, Skill.DEFENCE, Skill.STRENGTH)
                .anyMatch(s -> getMaxCombatPotionBoost(s)
                        > getAgent().getSkillManager().getCurrentLevel(s) + 1);
    }

    private int getMaxCombatPotionBoost(Skill skill) {
        return (int) Math.floor(getAgent().getSkillManager().getMaxLevel(skill) * 0.15)
                + 5
                + getAgent().getSkillManager().getMaxLevel(skill);
    }

    private boolean canUseBrewBoost() {
        final int currentDefLevel = getAgent().getSkillManager().getCurrentLevel(Skill.DEFENCE);
        final int maxDefLevel = getAgent().getSkillManager().getMaxLevel(Skill.DEFENCE);
        final int defBoost = (int) Math.floor(2 + (0.20 * maxDefLevel));
        // Add +1, so it doesn't re-pot at just a single level boost
        if (currentDefLevel < maxDefLevel + defBoost - 1) {
            return true;
        }
        final int currentHpLevel = getAgent().getSkillManager().getCurrentLevel(Skill.HITPOINTS);
        final int maxHpLevel = getAgent().getSkillManager().getMaxLevel(Skill.HITPOINTS);
        // No need to brew at 100+ hp
        if (currentHpLevel <= maxHpLevel) {
            return true;
        }
        return false;
    }

    private boolean canBoostRanged() {
        final int maxRangedLevel = (int) Math.floor(getAgent().getSkillManager().getMaxLevel(Skill.RANGED) * 0.10)
                + 4
                + getAgent().getSkillManager().getMaxLevel(Skill.RANGED);
        // Add +1, so it doesn't re-pot at just a single level boost
        return maxRangedLevel > getAgent().getSkillManager().getCurrentLevel(Skill.RANGED) + 1;
    }

    private boolean canCastSpell(CombatSpells combatSpell) {
        if (getAgent().getSpellbook() != combatSpell.getSpell().getSpellbook()) {
            return false;
        }
        return getAgent().getSkillManager().getCurrentLevel(Skill.MAGIC)
                >= combatSpell.getSpell().levelRequired();
    }

    private boolean isRangedSpecAttackAvailable() {
        if (!isAttackAvailable()) {
            return false;
        }
        if (!isRangedAttackAvailable()) {
            // Ranged weapon is always the same, ensure we can equip two handed etc.
            return false;
        }
        final CombatSpecial weapon = getRangedSpecialWeapon();
        return weapon != null && getSpecialPercentage() >= weapon.getDrainAmount();
    }

    private boolean isMageSpecAttackAvailable() {
        if (!isAttackAvailable()) {
            return false;
        }
        return isNightmareStaff() && getSpecialPercentage() >= 55;
    }

    private boolean isZaryteCrossbow() {
        return Arrays.stream(loadout.getRangedGear()).anyMatch(id -> id == ZARYTE_CROSSBOW);
    }

    private boolean isBallista() {
        return Arrays.stream(loadout.getRangedGear()).anyMatch(id -> id == LIGHT_BALLISTA || id == HEAVY_BALLISTA);
    }

    private boolean isNightmareStaff() {
        return Arrays.stream(loadout.getMageGear()).anyMatch(id -> id == VOLATILE_NIGHTMARE_STAFF);
    }

    private int getMeleeGearAttackSpeed() {
        return Arrays.stream(loadout.getMeleeGear())
                .mapToObj(ItemDefinition::forId)
                .filter(i -> i.getWeaponInterface() != null)
                .mapToInt(i -> i.getWeaponInterface().getSpeed())
                .findFirst()
                .orElseThrow();
    }

    private int getRangedGearAttackSpeed() {
        // Subtract 1 for rapid
        return Arrays.stream(loadout.getRangedGear())
                .mapToObj(ItemDefinition::forId)
                .filter(i -> i.getWeaponInterface() != null)
                .mapToInt(i -> i.getWeaponInterface().getSpeed() - 1)
                .findFirst()
                .orElseThrow();
    }

    private boolean isZurielStaff() {
        return Arrays.stream(loadout.getMageGear()).anyMatch(id -> id == ZURIELS_STAFF);
    }

    private boolean isMeleeSpecAncientGodsword() {
        return getMeleeSpecialWeapon() == CombatSpecial.ANCIENT_GODSWORD;
    }

    private boolean isMeleeSpecGraniteMaul() {
        return getMeleeSpecialWeapon() == CombatSpecial.GRANITE_MAUL;
    }

    private boolean isMeleeSpecStatHammer() {
        return getMeleeSpecialWeapon() == CombatSpecial.STATIUS_WARHAMMER;
    }

    private boolean isMeleeSpecVls() {
        return getMeleeSpecialWeapon() == CombatSpecial.VESTAS_LONGSWORD;
    }

    private boolean isDragonKnives() {
        return Arrays.stream(loadout.getRangedGear()).anyMatch(id -> id == DRAGON_KNIFE);
    }

    private boolean isDarkBow() {
        return Arrays.stream(loadout.getRangedGear()).anyMatch(id -> id == DARK_BOW);
    }

    private boolean isMorrigansJavelins() {
        return Arrays.stream(loadout.getRangedGear()).anyMatch(id -> id == MORRIGANS_JAVELIN);
    }

    private boolean canMelee() {
        return isInMeleeRange() || canMove();
    }

    private boolean canSpec() {
        final CombatSpecial specialWeapon = getMeleeSpecialWeapon();
        return specialWeapon != null && getAgent().getSpecialPercentage() >= specialWeapon.getDrainAmount();
    }

    private boolean canEquipTwoHandedWeapon() {
        return !getAgent().getInventory().isFull()
                || getAgent().getEquipment().get(Equipment.SHIELD_SLOT).getId() == -1;
    }

    private boolean isMeleeWeaponTwoHanded() {
        return Arrays.stream(loadout.getMeleeGear())
                .anyMatch(i -> ItemDefinition.forId(i).isDoubleHanded());
    }

    private boolean isSpecWeaponTwoHanded() {
        return Arrays.stream(loadout.getMeleeSpecGear())
                .anyMatch(i -> ItemDefinition.forId(i).isDoubleHanded());
    }

    private boolean isRangedWeaponTwoHanded() {
        return Arrays.stream(loadout.getRangedGear())
                .anyMatch(i -> ItemDefinition.forId(i).isDoubleHanded());
    }

    private boolean isHavePidOverTarget() {
        return getAgent().getPid() < getTarget().getPid();
    }

    private boolean didPlayerJustAttack() {
        return this.agentAttackedTarget;
    }

    private boolean didTargetJustAttack() {
        return this.targetAttackedAgent;
    }

    private double getAttackCalculatedDamageScale() {
        return this.tickDamageScale;
    }

    private boolean didPlayerPrayCorrectly() {
        return this.playerPrayedCorrect;
    }

    private boolean didTargetPrayCorrectly() {
        return this.targetPrayedCorrect;
    }

    private double getHitsplatsLandedOnAgentScale() {
        tryUpdateHitsplats();
        return damageReceivedScale;
    }

    private double getHitsplatsLandedOnTargetScale() {
        tryUpdateHitsplats();
        return damageDealtScale;
    }

    private void tryUpdateHitsplats() {
        if (this.updatedHitsplats) {
            return;
        }
        final HitDamage currentPrimaryHit = getAgent().getPrimaryHit();
        final HitDamage currentSecondaryHit = getAgent().getSecondaryHit();
        final int newPrimaryDamage = currentPrimaryHit != this.lastSelfPrimaryHit && currentPrimaryHit != null
                ? currentPrimaryHit.getDamage()
                : 0;
        final int newSecondaryDamage =
                currentSecondaryHit != this.lastSelfSecondaryHit ? currentSecondaryHit.getDamage() : 0;
        final int damageReceived = newPrimaryDamage + newSecondaryDamage;
        this.lastSelfPrimaryHit = currentPrimaryHit;
        this.lastSelfSecondaryHit = currentSecondaryHit;
        final double scale =
                damageReceived / (double) getAgent().getSkillManager().getMaxLevel(Skill.HITPOINTS);
        this.totalDamageReceived += scale;
        this.damageReceivedScale = scale;

        final HitDamage currentTargetPrimaryHit = getTarget().getPrimaryHit();
        final HitDamage currentTargetSecondaryHit = getTarget().getSecondaryHit();
        final boolean targetHealthUpdated = currentTargetPrimaryHit != this.lastTargetPrimaryHit
                || currentTargetSecondaryHit != this.lastTargetSecondaryHit;
        if (targetHealthUpdated) {
            // Update known target health percent when HP updates from new hitsplat
            this.lastTargetHealthPercent = getTarget().getSkillManager().getCurrentLevel(Skill.HITPOINTS)
                    / (double) getTarget().getSkillManager().getMaxLevel(Skill.HITPOINTS);
        }
        final int newTargetPrimaryDamage =
                currentTargetPrimaryHit != this.lastTargetPrimaryHit && currentTargetPrimaryHit != null
                        ? currentTargetPrimaryHit.getDamage()
                        : 0;
        final int newTargetSecondaryDamage =
                currentTargetSecondaryHit != this.lastTargetSecondaryHit ? currentTargetSecondaryHit.getDamage() : 0;
        final int damageDealt = newTargetPrimaryDamage + newTargetSecondaryDamage;
        this.lastTargetPrimaryHit = currentTargetPrimaryHit;
        this.lastTargetSecondaryHit = currentTargetSecondaryHit;
        final double targetScale =
                damageDealt / (double) getTarget().getSkillManager().getMaxLevel(Skill.HITPOINTS);
        this.totalDamageDealt += targetScale;
        this.damageDealtScale = targetScale;

        this.updatedHitsplats = true;
    }

    private boolean isMeleeSpecialWeaponEquipped() {
        final int weaponId = getAgent().getEquipment().getWeapon().getId();
        return combatSpecials.stream()
                .anyMatch(c -> Arrays.stream(c.getIdentifiers()).anyMatch(i -> i == weaponId));
    }

    private boolean isTargetMeleeSpecialWeaponEquipped() {
        final int weaponId = getTarget().getEquipment().getWeapon().getId();
        return combatSpecials.stream()
                .anyMatch(c -> Arrays.stream(c.getIdentifiers()).anyMatch(i -> i == weaponId));
    }

    private boolean canMoveAction() {
        if (!environmentParams.isAllowMovingIfCanAttack() && getRemainingTicks(true, TimerKey.COMBAT_ATTACK) == 0) {
            // Don't move if we can attack
            return false;
        }
        return canMove();
    }

    private boolean canMove() {
        return getRemainingTicks(true, TimerKey.FREEZE) == 0;
    }

    private boolean canTargetMove() {
        return getRemainingTicks(false, TimerKey.FREEZE) == 0;
    }

    private boolean isMeleeRangePossible() {
        return canMove() || canTargetMove() || isInMeleeRange();
    }

    private boolean canTargetCastMagicSpells() {
        return !isTargetLunarSpellbook;
    }

    private double getDamageDealtScale() {
        // Add 1D for stability purposes, so it doesn't shoot off to 0.5/2 (inf) with small numbers
        return Math.min(Math.max((this.totalDamageDealt + 1D) / (this.totalDamageReceived + 1D), 0.5), 2);
    }

    private double getTargetHitConfidence() {
        return Math.min((this.totalTargetHitCount / 20D), 1D);
    }

    private double getTargetHitMageCount() {
        if (this.totalTargetHitCount == 0) {
            return 0D;
        }
        return this.targetHitMagicCount / (double) this.totalTargetHitCount;
    }

    private double getTargetHitRangeCount() {
        if (this.totalTargetHitCount == 0) {
            return 0D;
        }
        return this.targetHitRangeCount / (double) this.totalTargetHitCount;
    }

    private double getTargetHitMeleeCount() {
        if (this.totalTargetHitCount == 0) {
            return 0D;
        }
        return this.targetHitMeleeCount / (double) this.totalTargetHitCount;
    }

    private double getPlayerHitMageCount() {
        if (this.totalTargetPrayCount == 0) {
            return 0D;
        }
        return this.playerHitMagicCount / (double) this.totalTargetPrayCount;
    }

    private double getPlayerHitRangeCount() {
        if (this.totalTargetPrayCount == 0) {
            return 0D;
        }
        return this.playerHitRangeCount / (double) this.totalTargetPrayCount;
    }

    private double getPlayerHitMeleeCount() {
        if (this.totalTargetPrayCount == 0) {
            return 0D;
        }
        return this.playerHitMeleeCount / (double) this.totalTargetPrayCount;
    }

    private double getTargetHitCorrectCount() {
        if (this.totalTargetHitCount == 0) {
            return 0D;
        }
        return this.targetHitCorrectCount / (double) this.totalTargetHitCount;
    }

    private double getTargetPrayConfidence() {
        return Math.min((this.totalTargetPrayCount / 20D), 1D);
    }

    private double getTargetPrayMageCount() {
        if (this.totalTargetPrayCount == 0) {
            return 0D;
        }
        return this.targetPrayMagicCount / (double) this.totalTargetPrayCount;
    }

    private double getTargetPrayRangeCount() {
        if (this.totalTargetPrayCount == 0) {
            return 0D;
        }
        return this.targetPrayRangeCount / (double) this.totalTargetPrayCount;
    }

    private double getTargetPrayMeleeCount() {
        if (this.totalTargetPrayCount == 0) {
            return 0D;
        }
        return this.targetPrayMeleeCount / (double) this.totalTargetPrayCount;
    }

    private double getPlayerPrayMageCount() {
        if (this.totalTargetHitCount == 0) {
            return 0D;
        }
        return this.playerPrayMagicCount / (double) this.totalTargetHitCount;
    }

    private double getPlayerPrayRangeCount() {
        if (this.totalTargetHitCount == 0) {
            return 0D;
        }
        return this.playerPrayRangeCount / (double) this.totalTargetHitCount;
    }

    private double getPlayerPrayMeleeCount() {
        if (this.totalTargetHitCount == 0) {
            return 0D;
        }
        return this.playerPrayMeleeCount / (double) this.totalTargetHitCount;
    }

    private double getTargetPrayCorrectCount() {
        if (this.totalTargetPrayCount == 0) {
            return 0D;
        }
        return this.targetPrayCorrectCount / (double) this.totalTargetPrayCount;
    }

    private double getRecentTargetHitMageCount() {
        return this.recentTargetAttackStyles.stream()
                        .filter(p -> p == CombatType.MAGIC)
                        .count()
                / (double) RECENT_THRESHOLD;
    }

    private double getRecentTargetHitRangeCount() {
        return this.recentTargetAttackStyles.stream()
                        .filter(p -> p == CombatType.RANGED)
                        .count()
                / (double) RECENT_THRESHOLD;
    }

    private double getRecentTargetHitMeleeCount() {
        return this.recentTargetAttackStyles.stream()
                        .filter(p -> p == CombatType.MELEE)
                        .count()
                / (double) RECENT_THRESHOLD;
    }

    private double getRecentPlayerHitMageCount() {
        return this.recentPlayerAttackStyles.stream()
                        .filter(p -> p == CombatType.MAGIC)
                        .count()
                / (double) RECENT_THRESHOLD;
    }

    private double getRecentPlayerHitRangeCount() {
        return this.recentPlayerAttackStyles.stream()
                        .filter(p -> p == CombatType.RANGED)
                        .count()
                / (double) RECENT_THRESHOLD;
    }

    private double getRecentPlayerHitMeleeCount() {
        return this.recentPlayerAttackStyles.stream()
                        .filter(p -> p == CombatType.MELEE)
                        .count()
                / (double) RECENT_THRESHOLD;
    }

    private double getRecentTargetHitCorrectCount() {
        return this.recentTargetHitCorrect.stream().filter(b -> b).count() / (double) RECENT_THRESHOLD;
    }

    private double getRecentTargetPrayMageCount() {
        return this.recentTargetPrayerStyles.stream()
                        .filter(p -> p == CombatType.MAGIC)
                        .count()
                / (double) RECENT_THRESHOLD;
    }

    private double getRecentTargetPrayRangeCount() {
        return this.recentTargetPrayerStyles.stream()
                        .filter(p -> p == CombatType.RANGED)
                        .count()
                / (double) RECENT_THRESHOLD;
    }

    private double getRecentTargetPrayMeleeCount() {
        return this.recentTargetPrayerStyles.stream()
                        .filter(p -> p == CombatType.MELEE)
                        .count()
                / (double) RECENT_THRESHOLD;
    }

    private double getRecentPlayerPrayMageCount() {
        return this.recentPlayerPrayerStyles.stream()
                        .filter(p -> p == CombatType.MAGIC)
                        .count()
                / (double) RECENT_THRESHOLD;
    }

    private double getRecentPlayerPrayRangeCount() {
        return this.recentPlayerPrayerStyles.stream()
                        .filter(p -> p == CombatType.RANGED)
                        .count()
                / (double) RECENT_THRESHOLD;
    }

    private double getRecentPlayerPrayMeleeCount() {
        return this.recentPlayerPrayerStyles.stream()
                        .filter(p -> p == CombatType.MELEE)
                        .count()
                / (double) RECENT_THRESHOLD;
    }

    private double getRecentTargetPrayCorrectCount() {
        return this.recentTargetPrayerCorrect.stream().filter(b -> b).count() / (double) RECENT_THRESHOLD;
    }

    private int getRemainingTicks(boolean player, TimerKey key) {
        // Note: whenever a timer has 1 tick left, it will be available again next turn
        // (timers tick before player actions)
        // so always subtract by 1 for next tick
        if (player) {
            return Math.max(getAgent().getTimers().getTicks(key) - 1, 0);
        } else {
            return Math.max(getTarget().getTimers().getTicks(key) - 1, 0);
        }
    }

    private CombatSpecial getMeleeSpecialWeapon() {
        return Arrays.stream(loadout.getMeleeSpecGear())
                .mapToObj(i -> combatSpecials.stream()
                        .filter(c -> Arrays.stream(c.getIdentifiers()).anyMatch(si -> si == i))
                        .findFirst()
                        .orElse(null))
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    private CombatSpecial getRangedSpecialWeapon() {
        final int weaponId = getRangedWeapon();
        return Arrays.stream(CombatSpecial.values())
                .filter(s -> Arrays.stream(s.getIdentifiers()).anyMatch(c -> c == weaponId))
                .findFirst()
                .orElse(null);
    }

    private int getRangedWeapon() {
        return Arrays.stream(loadout.getRangedGear())
                .mapToObj(ItemDefinition::forId)
                .filter(i -> i.getWeaponInterface() != null)
                .mapToInt(ItemDefinition::getId)
                .findFirst()
                .orElseThrow();
    }

    private int getRangedGearAttackRange() {
        final int weaponId = getRangedWeapon();
        return RangedData.RangedWeapon.getFor(weaponId).getType().getDefaultDistance();
    }

    private boolean isLms() {
        return getFightType() == NhEnvironmentParams.FightType.LMS;
    }

    private boolean isPvpArena() {
        return getFightType() == NhEnvironmentParams.FightType.PVP_ARENA;
    }

    private boolean isRangeSpecWeaponInLoadout() {
        return getRangedSpecialWeapon() != null;
    }

    private boolean isMageSpecWeaponInLoadout() {
        return isNightmareStaff();
    }

    private boolean isVengActive() {
        return getAgent().hasVengeance();
    }

    private boolean isTargetVengActive() {
        return getTarget().hasVengeance();
    }

    private boolean isPlayerLunarSpellbook() {
        return getAgent().getSpellbook() == MagicSpellbook.LUNAR;
    }

    private boolean isTargetLunarSpellbook() {
        return isTargetLunarSpellbook;
    }

    private int getPlayerVengCooldownTicks() {
        return getRemainingTicks(true, TimerKey.VENGEANCE_COOLDOWN);
    }

    private int getTargetVengCooldownTicks() {
        return getRemainingTicks(false, TimerKey.VENGEANCE_COOLDOWN);
    }

    private boolean isAnglerfish() {
        final ItemInSlot editableItem = getEdibleItemSlot();
        return editableItem != null
                && editableItem.getId() == Food.Edible.ANGLERFISH.getItem().getId();
    }

    private NhEnvironmentParams.FightType getFightType() {
        if (environmentParams.isRandomizeFightType()) {
            // Randomize fight type, but must be same as target too
            final int selectedIndex = EnvFightContext.getContextRandom(
                            getAgent().getUsername(), "FightType")
                    .nextInt(3);
            return NhEnvironmentParams.FightType.values()[selectedIndex];
        }
        return loadout.getFightType();
    }
}
