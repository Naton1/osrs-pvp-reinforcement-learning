package com.elvarg.game.entity.impl.playerbot.fightstyle.impl;

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
import com.elvarg.game.entity.impl.playerbot.fightstyle.*;
import com.elvarg.game.model.Item;
import com.elvarg.game.model.ItemInSlot;
import com.elvarg.game.model.MagicSpellbook;
import com.elvarg.game.model.Skill;
import com.elvarg.game.model.equipment.BonusManager;
import com.elvarg.game.model.movement.MovementQueue;
import com.elvarg.util.RandomGen;
import com.elvarg.util.timers.TimerKey;

import java.util.Arrays;
import java.util.Objects;

import static com.elvarg.game.content.PotionConsumable.SUPER_RESTORE_POTIONS;
import static com.elvarg.util.ItemIdentifiers.*;

public class MidTribridMaxFighterPreset implements FighterPreset {

    private static final RandomGen RANDOM = new RandomGen();

    public static final Presetable BOT_MID_TRIBRID = new Presetable("Mid Tribrid",
            new Item[]{
                    new Item(AVAS_ACCUMULATOR), new Item(BLACK_DHIDE_BODY), new Item(ABYSSAL_WHIP), new Item(SHARK),
                    new Item(RUNE_CROSSBOW), new Item(RUNE_PLATELEGS), new Item(DRAGON_DEFENDER), new Item(SHARK),
                    new Item(COOKED_KARAMBWAN), new Item(COOKED_KARAMBWAN), new Item(DRAGON_DAGGER_P_PLUS_PLUS_), new Item(SUPER_RESTORE_4_),
                    new Item(SHARK), new Item(SHARK), new Item(SHARK), new Item(SHARK),
                    new Item(SHARK), new Item(SHARK), new Item(SHARK), new Item(SUPER_COMBAT_POTION_4_),
                    new Item(SHARK), new Item(SHARK), new Item(SHARK), new Item(ANGLERFISH),
                    new Item(WATER_RUNE, 6000), new Item(BLOOD_RUNE, 2000), new Item(DEATH_RUNE, 4000), new Item(RANGING_POTION_4_),
            },
            //
            new Item[]{
                    new Item(HELM_OF_NEITIZNOT),
                    new Item(SARADOMIN_CAPE),
                    new Item(MASTER_WAND),
                    new Item(AMULET_OF_FURY),
                    new Item(MYSTIC_ROBE_TOP),
                    new Item(SPIRIT_SHIELD),
                    new Item(MYSTIC_ROBE_BOTTOM),
                    new Item(BARROWS_GLOVES),
                    new Item(CLIMBING_BOOTS),
                    new Item(RING_OF_RECOIL),
                    new Item(DRAGON_BOLTS_E_, 500),
            },
            /* atk, def, str, hp, range, pray, mage */
            new int[]{99, 99, 99, 99, 99, 99, 99},
            MagicSpellbook.ANCIENT,
            true
    );

    public static final CombatAction[] COMBAT_ACTIONS = {
            //Slower
            new CombatAction() {

                @Override
                public boolean shouldPerform(PlayerBot playerBot, Mobile enemy) {
                    return RANDOM.inclusive(0, 4) != 2;
                }

                @Override
                public void perform(PlayerBot playerBot, Mobile enemy) {

                }

                @Override
                public boolean stopAfter() {
                    return false;
                }
            },
            //OverHead prayers
            new CombatAction() {

                @Override
                public boolean shouldPerform(PlayerBot playerBot, Mobile enemy) {
                    return RANDOM.inclusive(0, 4) == 2;
                }

                @Override
                public void perform(PlayerBot playerBot, Mobile enemy) {


                    CombatMethod combatMethod = CombatFactory.getMethod(enemy);
                    var combatType = combatMethod.type();

                    var magicAccuracy = (enemy.isNpc() ? 0 : enemy.getAsPlayer().getBonusManager().getAttackBonus()[BonusManager.ATTACK_MAGIC]);

                    if (!CombatFactory.canReach(enemy, combatMethod, playerBot) && magicAccuracy < 35) {
                        PrayerHandler.activatePrayer(playerBot, PrayerHandler.PrayerData.SMITE);
                        return;
                    }

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
            },
            new CombatSwitch(new int[]{DRAGON_DAGGER_P_PLUS_PLUS_, DRAGON_DEFENDER},
                    new PrayerHandler.PrayerData[]{PrayerHandler.PrayerData.PROTECT_ITEM, PrayerHandler.PrayerData.PIETY}) {

                @Override
                public boolean shouldPerform(PlayerBot playerBot, Mobile enemy) {
                    boolean canAttackNextTick = playerBot.getTimers().willEndIn(TimerKey.COMBAT_ATTACK, 1);
                    return canAttackNextTick && playerBot.getMovementQueue().getMobility().canMove()
                            && enemy.getHitpointsAfterPendingDamage() <= 49
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
            },
            new CombatAction() {

                @Override
                public boolean shouldPerform(PlayerBot playerBot, Mobile enemy) {
                    var combatMethod = CombatFactory.getMethod(enemy);
                    int distance = playerBot.calculateDistance(enemy);

                    boolean cantAttack = playerBot.getTimers().has(TimerKey.COMBAT_ATTACK) && playerBot.getTimers().left(TimerKey.COMBAT_ATTACK) > 2;

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

            },
            // Restore prayer
            new CombatAction() {

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

            },
            new CombatSwitch(new int[]{MASTER_WAND, SARADOMIN_CAPE, MYSTIC_ROBE_TOP, MYSTIC_ROBE_BOTTOM, SPIRIT_SHIELD},
                    new PrayerHandler.PrayerData[]{PrayerHandler.PrayerData.PROTECT_ITEM, PrayerHandler.PrayerData.MYSTIC_MIGHT}) {

                @Override
                public boolean shouldPerform(PlayerBot playerBot, Mobile enemy) {
                    boolean canAttackNextTick = playerBot.getTimers().willEndIn(TimerKey.COMBAT_ATTACK, 1);

                    return canAttackNextTick
                            && enemy.getMovementQueue().getMobility().canMove() && !enemy.getTimers().has(TimerKey.FREEZE_IMMUNITY)
                            && CombatSpells.ICE_BARRAGE.getSpell().canCast(playerBot, false);
                }

                @Override
                public void performAfterSwitch(PlayerBot playerBot, Mobile enemy) {
                    playerBot.getCombat().setCastSpell(CombatSpells.ICE_BARRAGE.getSpell());
                    playerBot.getCombat().attack(enemy);
                }
            },
            new EnemyDefenseAwareCombatSwitch(new AttackStyleSwitch[]{
                    new AttackStyleSwitch(CombatType.MAGIC,
                            new CombatSwitch(new int[]{MASTER_WAND, SARADOMIN_CAPE, MYSTIC_ROBE_TOP, MYSTIC_ROBE_BOTTOM, SPIRIT_SHIELD},
                                    new PrayerHandler.PrayerData[]{PrayerHandler.PrayerData.PROTECT_ITEM, PrayerHandler.PrayerData.AUGURY}) {

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
                    ),
                    new AttackStyleSwitch(CombatType.RANGED,
                            new CombatSwitch(new int[]{ RUNE_CROSSBOW, AVAS_ACCUMULATOR, RUNE_PLATELEGS, BLACK_DHIDE_BODY},
                                    new PrayerHandler.PrayerData[]{PrayerHandler.PrayerData.PROTECT_ITEM, PrayerHandler.PrayerData.EAGLE_EYE}) {

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
                    ),
                    new AttackStyleSwitch(CombatType.MELEE,
                            new CombatSwitch(new int[]{ABYSSAL_WHIP, DRAGON_DEFENDER},
                                    new PrayerHandler.PrayerData[]{PrayerHandler.PrayerData.PROTECT_ITEM, PrayerHandler.PrayerData.PIETY}) {

                                @Override
                                public boolean shouldPerform(PlayerBot playerBot, Mobile enemy) {
                                    return playerBot.getMovementQueue().getMobility().canMove()
                                            && enemy.getHitpointsAfterPendingDamage() <= 45;
                                }

                                @Override
                                public void performAfterSwitch(PlayerBot playerBot, Mobile enemy) {
                                    playerBot.getCombat().setCastSpell(null);
                                    playerBot.getCombat().attack(enemy);
                                }
                            }
                    ),
            }) {
                @Override
                public boolean shouldPerform(PlayerBot playerBot, Mobile enemy) {
                    return playerBot.getTimers().willEndIn(TimerKey.COMBAT_ATTACK, 1);
                }
            },
    };

    @Override
    public Presetable getItemPreset() {
        return BOT_MID_TRIBRID;
    }

    @Override
    public CombatAction[] getCombatActions() {
        return COMBAT_ACTIONS;
    }

    @Override
    public int eatAtPercent() {
        return 45;
    }
}
