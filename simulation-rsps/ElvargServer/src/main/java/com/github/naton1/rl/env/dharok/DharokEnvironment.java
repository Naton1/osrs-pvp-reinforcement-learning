package com.github.naton1.rl.env.dharok;

import static com.elvarg.game.content.combat.magic.EffectSpells.EffectSpell.VENGEANCE;
import static com.elvarg.util.ItemIdentifiers.ABYSSAL_TENTACLE;
import static com.elvarg.util.ItemIdentifiers.DHAROKS_GREATAXE;
import static com.elvarg.util.ItemIdentifiers.DHAROKS_PLATEBODY;
import static com.elvarg.util.ItemIdentifiers.DHAROKS_PLATELEGS;
import static com.elvarg.util.ItemIdentifiers.DRAGON_DEFENDER;
import static com.elvarg.util.ItemIdentifiers.GRANITE_MAUL;

import com.elvarg.game.content.Food;
import com.elvarg.game.content.PotionConsumable;
import com.elvarg.game.content.PrayerHandler;
import com.elvarg.game.content.combat.CombatSpecial;
import com.elvarg.game.content.combat.hit.HitDamage;
import com.elvarg.game.content.combat.hit.PendingHit;
import com.elvarg.game.content.combat.magic.EffectSpells;
import com.elvarg.game.content.skill.skillable.impl.Herblore;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.Item;
import com.elvarg.game.model.ItemInSlot;
import com.elvarg.game.model.Skill;
import com.elvarg.game.model.container.impl.Equipment;
import com.elvarg.net.packet.impl.EquipPacketListener;
import com.elvarg.util.timers.TimerKey;
import com.github.naton1.rl.env.AgentEnvironment;
import com.github.naton1.rl.env.EnvironmentCallback;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class DharokEnvironment implements AgentEnvironment {

    @Getter
    private final Player agent;

    @Getter
    private final Player target;

    @Nullable
    private final EnvironmentCallback environmentCallback;

    private final DharokLoadout loadout;

    private final DharokEnvironmentParams environmentParams;

    private boolean targetAttackedAgent;
    private boolean agentAttackedTarget;

    private double tickDamageScale;

    private HitDamage lastTargetPrimaryHit;
    private HitDamage lastTargetSecondaryHit;
    private HitDamage lastSelfPrimaryHit;
    private HitDamage lastSelfSecondaryHit;

    private double damageDealtScale;
    private double damageReceivedScale;

    private boolean updatedHitsplats;

    @Override
    public void onHitCalculated(PendingHit pendingHit) {
        if (pendingHit.getAttacker() == getTarget() && pendingHit.getTarget() == getAgent()) {
            this.targetAttackedAgent = true;
        } else if (pendingHit.getAttacker() == getAgent() && pendingHit.getTarget() == getTarget()) {
            this.agentAttackedTarget = true;
            this.tickDamageScale = pendingHit.getTotalDamage()
                    / (double) getTarget().getSkillManager().getMaxLevel(Skill.HITPOINTS);
        }
    }

    @Override
    public void processAction(final List<Integer> action) {
        if (action.size() != 6) {
            throw new IllegalArgumentException(action.toString());
        }
        handlePrimaryFood(action.get(2));
        handlePotion(action.get(1));
        handleKarambwan(action.get(3));
        handleGear(action.get(5));
        handleVeng(action.get(4));
        handleAttack(action.get(0));
    }

    @Override
    public List<Number> getObs() {
        return List.of(
                isPlayerUsingWhip() ? 1 : 0,
                isPlayerUsingMaul() ? 1 : 0,
                isPlayerUsingAxe() ? 1 : 0,
                getSpecialPercentage() / 100D,
                getHealthPercent(),
                getTargetHealthPercent(),
                isTargetUsingWhip() ? 1 : 0,
                isTargetUsingMaul() ? 1 : 0,
                isTargetUsingAxe() ? 1 : 0,
                getTargetSpecialPercentage() / 100D,
                getPotionDoseScale(Herblore.PotionDose.SUPER_COMBAT),
                getPotionDoseScale(Herblore.PotionDose.SUPER_RESTORE),
                getPotionDoseScale(Herblore.PotionDose.SARADOMIN_BREW),
                getFoodCountScale(),
                getKaramCountScale(),
                getPrayerPointScale(),
                getRelativeLevel(Skill.STRENGTH),
                getRelativeLevel(Skill.ATTACK),
                getRelativeLevel(Skill.DEFENCE),
                getTicksUntilNextAttackScale(),
                getTicksUntilNextFoodScale(),
                getTicksUntilNextPotionCycle(),
                getTicksUntilNextKaramCycle(),
                getFoodAttackDelayScale(),
                getTicksUntilNextTargetAttackScale(),
                getTicksUntilNextTargetPotionScale(),
                getPendingDamageOnTargetScale(),
                isDamagePendingOnPlayer() ? 1 : 0,
                didPlayerJustAttack() ? 1 : 0,
                didTargetJustAttack() ? 1 : 0,
                getAttackCalculatedDamageScale(),
                getHitsplatsLandedOnAgentScale(),
                getHitsplatsLandedOnTargetScale(),
                isHavePidOverTarget() ? 1 : 0,
                playerHasVengAvailable() ? 1 : 0,
                playerHasVengCast() ? 1 : 0,
                targetHasVengAvailable() ? 1 : 0,
                targetHasVengCast() ? 1 : 0,
                isPlayerArmorUnequipped() ? 1 : 0,
                isTargetArmorUnequipped() ? 1 : 0);
    }

    @Override
    public List<List<Boolean>> getActionMasks() {
        return List.of(
                List.of(
                        !isAttackAvailable(),
                        isAttackAvailable(),
                        isAttackAvailable() && canEquipTwoHandedWeapon(),
                        canEquipTwoHandedWeapon() && canSpec()),
                List.of(
                        true,
                        canUsePotion(Herblore.PotionDose.SARADOMIN_BREW) && canUseBrewBoost(),
                        canUsePotion(Herblore.PotionDose.SUPER_RESTORE) && canRestoreStats(),
                        canUsePotion(Herblore.PotionDose.SUPER_COMBAT) && canBoostCombatSkills()),
                List.of(true, canEatFood()),
                List.of(true, canEatKarambwan()),
                List.of(true, canUseVeng()),
                List.of(true, true, canUnequipArmor()));
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
    }

    private boolean isPlayerUsingWhip() {
        return getAgent().getEquipment().contains(ABYSSAL_TENTACLE);
    }

    private boolean isPlayerUsingMaul() {
        return getAgent().getEquipment().contains(GRANITE_MAUL);
    }

    private boolean isPlayerUsingAxe() {
        return getAgent().getEquipment().contains(DHAROKS_GREATAXE);
    }

    private boolean isTargetUsingWhip() {
        return getTarget().getEquipment().contains(ABYSSAL_TENTACLE);
    }

    private boolean isTargetUsingMaul() {
        return getTarget().getEquipment().contains(GRANITE_MAUL);
    }

    private boolean isTargetUsingAxe() {
        return getTarget().getEquipment().contains(DHAROKS_GREATAXE);
    }

    private double getSpecialPercentage() {
        return getAgent().getSpecialPercentage();
    }

    private double getTargetSpecialPercentage() {
        return getTarget().getSpecialPercentage();
    }

    private double getHealthPercent() {
        return getAgent().getSkillManager().getCurrentLevel(Skill.HITPOINTS)
                / (double) getAgent().getSkillManager().getMaxLevel(Skill.HITPOINTS);
    }

    private double getTargetHealthPercent() {
        return getTarget().getSkillManager().getCurrentLevel(Skill.HITPOINTS)
                / (double) getTarget().getSkillManager().getMaxLevel(Skill.HITPOINTS);
    }

    private double getPotionDoseScale(Herblore.PotionDose potionDose) {
        int remainingDoses = getAgent().getInventory().getAmount(potionDose.getDoseID1());
        remainingDoses += getAgent().getInventory().getAmount(potionDose.getDoseID2()) * 2;
        remainingDoses += getAgent().getInventory().getAmount(potionDose.getDoseID3()) * 3;
        remainingDoses += getAgent().getInventory().getAmount(potionDose.getFourDosePotionID()) * 4;
        return remainingDoses;
    }

    private double getFoodCountScale() {
        return getAgent()
                .getInventory()
                .getAmount(Food.Edible.ANGLERFISH.getItem().getId());
    }

    private double getKaramCountScale() {
        return getAgent()
                .getInventory()
                .getAmount(Food.Edible.KARAMBWAN.getItem().getId());
    }

    private double getPrayerPointScale() {
        return getAgent().getSkillManager().getCurrentLevel(Skill.PRAYER)
                / (double) getAgent().getSkillManager().getMaxLevel(Skill.PRAYER);
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

    private double getFoodAttackDelayScale() {
        // Cap at -2 because anything lower is useless, eating won't prevent an attack
        // And cap at 1 because anything higher won't provide more info, can't currently attack,
        // and another observation will give the actual attack delay scale
        final int attackDelay = getAgent().getTimers().getUncappedTicks(TimerKey.COMBAT_ATTACK, -100);
        final int cappedAttackDelay = Math.min(Math.max(attackDelay, -2), 1) + 2;
        return cappedAttackDelay / 3D;
    }

    private double getTicksUntilNextAttackScale() {
        return getRemainingTicks(true, TimerKey.COMBAT_ATTACK) / 6D;
    }

    private double getTicksUntilNextFoodScale() {
        return getRemainingTicks(true, TimerKey.FOOD) / 2D;
    }

    private double getTicksUntilNextPotionCycle() {
        return getRemainingTicks(true, TimerKey.POTION) / 2D;
    }

    private double getTicksUntilNextKaramCycle() {
        return getRemainingTicks(true, TimerKey.KARAMBWAN) / 2D;
    }

    private double getTicksUntilNextTargetAttackScale() {
        return getRemainingTicks(false, TimerKey.COMBAT_ATTACK) / 6D;
    }

    private double getTicksUntilNextTargetPotionScale() {
        return getRemainingTicks(false, TimerKey.FOOD) / 2D;
    }

    private double getPendingDamageOnTargetScale() {
        return getTarget().getCombat().getHitQueue().getAllAccumulatedDamage()
                / (double) getTarget().getSkillManager().getMaxLevel(Skill.HITPOINTS);
    }

    private boolean isDamagePendingOnPlayer() {
        return getAgent().getCombat().getHitQueue().getAllAccumulatedDamage() > 0;
    }

    private boolean isAttackingTarget() {
        return getAgent().getCombat().getTarget() == getTarget();
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

    private void handleVeng(final int vengAction) {
        if (vengAction == 0) {
            // no-op
            return;
        } else if (vengAction == 1) {
            EffectSpells.handleSpell(getAgent(), VENGEANCE.getSpell().spellId());
        } else {
            throw new IllegalArgumentException(String.valueOf(vengAction));
        }
    }

    private void handleAttack(final int attackTypeAction) {
        if (attackTypeAction == 0) {
            // no-op
            return;
        } else if (attackTypeAction == 1) {
            equip(ABYSSAL_TENTACLE, DRAGON_DEFENDER);
        } else if (attackTypeAction == 2) {
            equip(DHAROKS_GREATAXE);
        } else if (attackTypeAction == 3) {
            equip(GRANITE_MAUL);
            CombatSpecial.activate(getAgent());
            CombatSpecial.activate(getAgent());
        } else {
            throw new IllegalArgumentException(String.valueOf(attackTypeAction));
        }
        if (getAgent().getSkillManager().getCurrentLevel(Skill.PRAYER) > 0) {
            PrayerHandler.activatePrayer(getAgent(), PrayerHandler.PIETY);
        }
        getAgent().getCombat().attack(getTarget());
    }

    private void handleGear(int gearTypeAction) {
        if (gearTypeAction == 0) {
            return;
        } else if (gearTypeAction == 1) {
            equip(Arrays.stream(loadout.getEquipment()).mapToInt(Item::getId).toArray());
        } else if (gearTypeAction == 2) {
            unequip(DHAROKS_PLATEBODY, DHAROKS_PLATELEGS);
        } else {
            throw new IllegalArgumentException(String.valueOf(gearTypeAction));
        }
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

    private boolean canUseVeng() {
        return getRemainingTicks(true, TimerKey.VENGEANCE_COOLDOWN) <= 0;
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
    }

    private void unequip(int... itemIds) {
        for (int itemId : getAgent().getEquipment().getItemIdsArray()) {
            if (itemId == -1) {
                continue;
            }
            if (Arrays.stream(itemIds).noneMatch(i -> i == itemId)) {
                continue;
            }
            final int itemSlot = getAgent().getEquipment().getSlotForItemId(itemId);
            EquipPacketListener.unequip(getAgent(), itemSlot, itemId);
        }
    }

    private boolean isAttackAvailable() {
        if (getRemainingTicks(true, TimerKey.COMBAT_ATTACK) > 0) {
            return false;
        }
        return true;
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
        return Stream.of(Skill.ATTACK, Skill.DEFENCE, Skill.STRENGTH, Skill.PRAYER)
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

    private boolean canSpec() {
        final CombatSpecial specialWeapon = CombatSpecial.GRANITE_MAUL;
        return getAgent().getSpecialPercentage() >= specialWeapon.getDrainAmount();
    }

    private boolean canEquipTwoHandedWeapon() {
        return !getAgent().getInventory().isFull()
                || getAgent().getEquipment().get(Equipment.SHIELD_SLOT).getId() == -1;
    }

    private boolean canUnequipArmor() {
        if (getAgent().getInventory().contains(DHAROKS_PLATEBODY)
                && getAgent().getInventory().contains(DHAROKS_PLATELEGS)) {
            // already unequipped
            return true;
        }
        return getAgent().getInventory().getFreeSlots() >= 2;
    }

    private boolean playerHasVengAvailable() {
        return getRemainingTicks(true, TimerKey.VENGEANCE_COOLDOWN) <= 0;
    }

    private boolean playerHasVengCast() {
        return getAgent().hasVengeance();
    }

    private boolean targetHasVengAvailable() {
        return getRemainingTicks(false, TimerKey.VENGEANCE_COOLDOWN) <= 0;
    }

    private boolean targetHasVengCast() {
        return getTarget().hasVengeance();
    }

    private boolean isPlayerArmorUnequipped() {
        return !getAgent().getEquipment().contains(DHAROKS_PLATEBODY);
    }

    private boolean isTargetArmorUnequipped() {
        return !getTarget().getEquipment().contains(DHAROKS_PLATEBODY);
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
        this.damageReceivedScale =
                damageReceived / (double) getAgent().getSkillManager().getMaxLevel(Skill.HITPOINTS);

        final HitDamage currentTargetPrimaryHit = getTarget().getPrimaryHit();
        final HitDamage currentTargetSecondaryHit = getTarget().getSecondaryHit();
        final int newTargetPrimaryDamage =
                currentTargetPrimaryHit != this.lastTargetPrimaryHit && currentTargetPrimaryHit != null
                        ? currentTargetPrimaryHit.getDamage()
                        : 0;
        final int newTargetSecondaryDamage =
                currentTargetSecondaryHit != this.lastTargetSecondaryHit ? currentTargetSecondaryHit.getDamage() : 0;
        final int damageDealt = newTargetPrimaryDamage + newTargetSecondaryDamage;
        this.lastTargetPrimaryHit = currentTargetPrimaryHit;
        this.lastTargetSecondaryHit = currentTargetSecondaryHit;
        this.damageDealtScale =
                damageDealt / (double) getTarget().getSkillManager().getMaxLevel(Skill.HITPOINTS);

        this.updatedHitsplats = true;
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
}
