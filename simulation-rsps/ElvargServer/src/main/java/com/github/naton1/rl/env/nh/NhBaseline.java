package com.github.naton1.rl.env.nh;

import static com.elvarg.game.content.PotionConsumable.SUPER_RESTORE_POTIONS;
import static com.elvarg.util.ItemIdentifiers.ANGLERFISH;
import static com.elvarg.util.ItemIdentifiers.SARADOMIN_BREW_4_;
import static com.elvarg.util.ItemIdentifiers.SUPER_RESTORE_4_;

import com.elvarg.game.content.PotionConsumable;
import com.elvarg.game.content.PrayerHandler;
import com.elvarg.game.content.combat.CombatFactory;
import com.elvarg.game.content.combat.CombatSpecial;
import com.elvarg.game.content.combat.CombatType;
import com.elvarg.game.content.combat.magic.CombatSpells;
import com.elvarg.game.content.combat.method.CombatMethod;
import com.elvarg.game.content.presets.Presetable;
import com.elvarg.game.entity.impl.Mobile;
import com.elvarg.game.entity.impl.playerbot.PlayerBot;
import com.elvarg.game.entity.impl.playerbot.fightstyle.AttackStyleSwitch;
import com.elvarg.game.entity.impl.playerbot.fightstyle.CombatAction;
import com.elvarg.game.entity.impl.playerbot.fightstyle.CombatSwitch;
import com.elvarg.game.entity.impl.playerbot.fightstyle.EnemyDefenseAwareCombatSwitch;
import com.elvarg.game.entity.impl.playerbot.fightstyle.FighterPreset;
import com.elvarg.game.model.Item;
import com.elvarg.game.model.ItemInSlot;
import com.elvarg.game.model.Skill;
import com.elvarg.game.model.movement.MovementQueue;
import com.elvarg.util.RandomGen;
import com.elvarg.util.timers.TimerKey;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class NhBaseline implements FighterPreset {

    private static final RandomGen RANDOM = new RandomGen();

    private final NhLoadout loadout;

    @Override
    public Presetable getItemPreset() {
        // Use same preset, but map brews to anglerfish since this doesn't support brews right now
        // Also, replace all but 1 super restore since there's no brews
        final AtomicInteger restoreCount = new AtomicInteger();
        return new Presetable(
                "Baseline",
                Arrays.stream(loadout.getInventory())
                        .map(i -> {
                            if (i.getId() == SARADOMIN_BREW_4_) {
                                return new Item(ANGLERFISH);
                            }
                            if (i.getId() == SUPER_RESTORE_4_ && restoreCount.incrementAndGet() > 2) {
                                return new Item(ANGLERFISH);
                            }
                            return i;
                        })
                        .toArray(Item[]::new),
                loadout.getEquipment(),
                loadout.getCombatStats().toArray(),
                loadout.getMagicSpellbook(),
                true);
    }

    @Override
    public CombatAction[] getCombatActions() {
        return new CombatAction[] {
            new OverheadPrayers(),
            new Spec(),
            new MoveAway(),
            new RestorePrayer(),
            new UseMagic(),
            new AttackStyleSwap()
        };
    }

    @Override
    public int eatAtPercent() {
        return 65;
    }

    private class OverheadPrayers implements CombatAction {
        @Override
        public boolean shouldPerform(PlayerBot playerBot, Mobile enemy) {
            return RANDOM.inclusive(0, 4) == 2;
        }

        @Override
        public void perform(PlayerBot playerBot, Mobile enemy) {

            final CombatMethod combatMethod = CombatFactory.getMethod(enemy);
            final CombatType combatType = combatMethod.type();

            if (combatType == CombatType.MELEE && CombatFactory.canReach(enemy, combatMethod, playerBot)) {
                PrayerHandler.activatePrayer(playerBot, PrayerHandler.PrayerData.PROTECT_FROM_MELEE);
                return;
            }

            if (combatType == CombatType.RANGED) {
                PrayerHandler.activatePrayer(playerBot, PrayerHandler.PrayerData.PROTECT_FROM_MISSILES);
            } else {
                PrayerHandler.activatePrayer(playerBot, PrayerHandler.PrayerData.PROTECT_FROM_MAGIC);
            }
        }

        @Override
        public boolean stopAfter() {
            return false;
        }
    }

    private class Spec extends CombatSwitch {

        public Spec() {
            super(loadout.getMeleeSpecGear(), loadout.getMeleePrayers());
        }

        @Override
        public boolean shouldPerform(PlayerBot playerBot, Mobile enemy) {
            boolean canAttackNextTick = playerBot.getTimers().willEndIn(TimerKey.COMBAT_ATTACK, 1);
            return canAttackNextTick
                    && playerBot.getMovementQueue().getMobility().canMove()
                    && enemy.getHitpointsAfterPendingDamage() <= 59
                    && playerBot.getSpecialPercentage() >= 50
                    && !enemy.getPrayerActive()[PrayerHandler.PROTECT_FROM_MELEE];
        }

        @Override
        public void performAfterSwitch(PlayerBot playerBot, Mobile enemy) {
            playerBot.getCombat().setCastSpell(null);
            if (!playerBot.isSpecialActivated()) {
                CombatSpecial.activate(playerBot);
            }
            playerBot.getCombat().attack(enemy);
        }
    }

    private class MoveAway implements CombatAction {
        @Override
        public boolean shouldPerform(PlayerBot playerBot, Mobile enemy) {
            var combatMethod = CombatFactory.getMethod(enemy);
            int distance = playerBot.calculateDistance(enemy);

            boolean cantAttack = playerBot.getTimers().has(TimerKey.COMBAT_ATTACK)
                    && playerBot.getTimers().left(TimerKey.COMBAT_ATTACK) > 2;

            return cantAttack
                    && playerBot.getMovementQueue().size() == 0
                    && !enemy.getMovementQueue().getMobility().canMove()
                    && distance == 1
                    && CombatFactory.canReach(enemy, combatMethod, playerBot);
        }

        @Override
        public void perform(PlayerBot playerBot, Mobile enemy) {
            playerBot.setFollowing(null);
            MovementQueue.randomClippedStepNotSouth(playerBot, 3);
        }
    }

    private class RestorePrayer implements CombatAction {
        @Override
        public boolean shouldPerform(PlayerBot playerBot, Mobile enemy) {
            var pot = Arrays.stream(SUPER_RESTORE_POTIONS.getIds())
                    .mapToObj(id -> ItemInSlot.getFromInventory(id, playerBot.getInventory()))
                    .filter(Objects::nonNull)
                    .findFirst();
            return pot.isPresent() && playerBot.getSkillManager().getCurrentLevel(Skill.PRAYER) < 50;
        }

        @Override
        public void perform(PlayerBot playerBot, Mobile enemy) {

            var pot = Arrays.stream(SUPER_RESTORE_POTIONS.getIds())
                    .mapToObj(id -> ItemInSlot.getFromInventory(id, playerBot.getInventory()))
                    .filter(Objects::nonNull)
                    .findFirst();

            PotionConsumable.drink(playerBot, pot.get().getId(), pot.get().getSlot());
        }
    }

    private class UseMagic extends CombatSwitch {
        public UseMagic() {
            super(loadout.getMageGear(), loadout.getMagePrayers());
        }

        @Override
        public boolean shouldPerform(PlayerBot playerBot, Mobile enemy) {
            boolean canAttackNextTick = playerBot.getTimers().willEndIn(TimerKey.COMBAT_ATTACK, 1);

            return canAttackNextTick
                    && enemy.getMovementQueue().getMobility().canMove()
                    && !enemy.getTimers().has(TimerKey.FREEZE_IMMUNITY)
                    && CombatSpells.ICE_BARRAGE.getSpell().canCast(playerBot, false);
        }

        @Override
        public void performAfterSwitch(PlayerBot playerBot, Mobile enemy) {
            playerBot.getCombat().setCastSpell(CombatSpells.ICE_BARRAGE.getSpell());
            playerBot.getCombat().attack(enemy);
        }
    }

    private class MagicSwitch extends CombatSwitch {
        public MagicSwitch() {
            super(loadout.getMageGear(), loadout.getMagePrayers());
        }

        @Override
        public boolean shouldPerform(PlayerBot playerBot, Mobile enemy) {
            return CombatSpells.ICE_BARRAGE.getSpell().canCast(playerBot, false);
        }

        @Override
        public void performAfterSwitch(PlayerBot playerBot, Mobile enemy) {
            playerBot.getCombat().setCastSpell(CombatSpells.ICE_BARRAGE.getSpell());
            playerBot.getCombat().attack(enemy);
        }
    }

    private class RangedSwitch extends CombatSwitch {
        public RangedSwitch() {
            super(loadout.getRangedGear(), loadout.getRangedPrayers());
        }

        @Override
        public boolean shouldPerform(PlayerBot playerBot, Mobile enemy) {
            return true;
        }

        @Override
        public void performAfterSwitch(PlayerBot playerBot, Mobile enemy) {
            playerBot.getCombat().setCastSpell(null);
            playerBot.setSpecialActivated(false);
            playerBot.getCombat().attack(enemy);
        }
    }

    private class MeleeSwitch extends CombatSwitch {
        public MeleeSwitch() {
            super(loadout.getMeleeGear(), loadout.getMeleePrayers());
        }

        @Override
        public boolean shouldPerform(PlayerBot playerBot, Mobile enemy) {
            return playerBot.getMovementQueue().getMobility().canMove() && enemy.getHitpointsAfterPendingDamage() <= 45;
        }

        @Override
        public void performAfterSwitch(PlayerBot playerBot, Mobile enemy) {
            playerBot.getCombat().setCastSpell(null);
            playerBot.getCombat().attack(enemy);
        }
    }

    private class AttackStyleSwap extends EnemyDefenseAwareCombatSwitch {
        public AttackStyleSwap() {
            super(new AttackStyleSwitch[] {
                new AttackStyleSwitch(CombatType.MAGIC, new MagicSwitch()),
                new AttackStyleSwitch(CombatType.RANGED, new RangedSwitch()),
                new AttackStyleSwitch(CombatType.MELEE, new MeleeSwitch())
            });
        }

        @Override
        public boolean shouldPerform(PlayerBot playerBot, Mobile enemy) {
            return playerBot.getTimers().willEndIn(TimerKey.COMBAT_ATTACK, 1);
        }
    }
}
