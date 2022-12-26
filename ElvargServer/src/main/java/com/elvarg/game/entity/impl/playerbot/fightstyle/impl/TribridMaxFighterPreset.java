package com.elvarg.game.entity.impl.playerbot.fightstyle.impl;

import com.elvarg.game.GameConstants;
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
import com.elvarg.game.model.teleportation.TeleportHandler;
import com.elvarg.game.model.teleportation.TeleportType;
import com.elvarg.util.ItemIdentifiers;
import com.elvarg.util.timers.TimerKey;

import java.util.Arrays;
import java.util.Objects;

import static com.elvarg.game.content.PotionConsumable.SUPER_RESTORE_POTIONS;
import static com.elvarg.util.ItemIdentifiers.*;

public class TribridMaxFighterPreset implements FighterPreset {

    private static final Presetable BOT_HARD_TRIBRID = new Presetable("Bot Tribrid",
            new Item[]{
                    new Item(ARMADYL_CROSSBOW), new Item(ARMADYL_GODSWORD), new Item(RANGING_POTION_4_), new Item(SUPER_COMBAT_POTION_4_),
                    new Item(AVAS_ACCUMULATOR), new Item(KARILS_LEATHERSKIRT), new Item(KARILS_LEATHERTOP), new Item(SUPER_RESTORE_4_),
                    new Item(COOKED_KARAMBWAN), new Item(MANTA_RAY), new Item(MANTA_RAY), new Item(COOKED_KARAMBWAN),
                    new Item(COOKED_KARAMBWAN), new Item(MANTA_RAY), new Item(MANTA_RAY), new Item(COOKED_KARAMBWAN),
                    new Item(COOKED_KARAMBWAN), new Item(MANTA_RAY), new Item(MANTA_RAY), new Item(COOKED_KARAMBWAN),
                    new Item(MANTA_RAY), new Item(MANTA_RAY), new Item(MANTA_RAY), new Item(ANGLERFISH),
                    new Item(WATER_RUNE, 10000), new Item(BLOOD_RUNE, 10000), new Item(DEATH_RUNE, 10000), new Item(TELEPORT_TO_HOUSE, 1),
            },
            new Item[]{
                    new Item(HELM_OF_NEITIZNOT),
                    new Item(INFERNAL_CAPE),
                    new Item(STAFF_OF_THE_DEAD),
                    new Item(AMULET_OF_FURY),
                    new Item(AHRIMS_ROBESKIRT),
                    new Item(BLESSED_SPIRIT_SHIELD),
                    new Item(AHRIMS_ROBETOP),
                    new Item(BARROWS_GLOVES),
                    new Item(CLIMBING_BOOTS),
                    new Item(RING_OF_RECOIL),
                    new Item(DRAGONSTONE_DRAGON_BOLTS_E_, 135),
            },
            /* atk, def, str, hp, range, pray, mage */
            new int[]{99, 99, 99, 99, 99, 99, 99},
            MagicSpellbook.ANCIENT,
            true
    );

    public static final CombatAction[] COMBAT_ACTIONS = {
            // Escape
            new CombatAction() {

                @Override
                public boolean shouldPerform(PlayerBot playerBot, Mobile enemy) {
                    var food = ItemInSlot.getFromInventory(MANTA_RAY, playerBot.getInventory());

                    return food == null;
                }

                @Override
                public void perform(PlayerBot playerBot, Mobile enemy) {
                    System.out.println("Escape");
                    if (enemy.isPlayer()) {
                        playerBot.sendChat("Cya " + enemy.getAsPlayer().getUsername());
                    }
                    if (TeleportHandler.checkReqs(playerBot, GameConstants.DEFAULT_LOCATION)) {
                        TeleportHandler.teleport(playerBot, GameConstants.DEFAULT_LOCATION, TeleportType.TELE_TAB, false);
                        playerBot.getInventory().delete(ItemIdentifiers.TELEPORT_TO_HOUSE, 1);
                    }
                }

            },
            //OverHead prayers
            new CombatAction() {

                @Override
                public boolean shouldPerform(PlayerBot playerBot, Mobile enemy) {
                    return true;
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
            new CombatSwitch(new int[]{ARMADYL_GODSWORD, INFERNAL_CAPE, KARILS_LEATHERSKIRT, KARILS_LEATHERTOP},
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
                    System.out.println("AGS Spec");
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
                    System.out.println("Retreat");
                    playerBot.setFollowing(null);
//                    playerBot.setAutoRetaliate(false);
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
                    System.out.println("Pot up");

                    var pot = Arrays.stream(SUPER_RESTORE_POTIONS.getIds())
                            .mapToObj(id -> ItemInSlot.getFromInventory(id, playerBot.getInventory()))
                            .filter(Objects::nonNull)
                            .findFirst();

                    PotionConsumable.drink(playerBot, pot.get().getId(), pot.get().getSlot());
                }

            },
            new CombatSwitch(new int[]{STAFF_OF_THE_DEAD, AHRIMS_ROBETOP, AHRIMS_ROBESKIRT, BLESSED_SPIRIT_SHIELD, INFERNAL_CAPE},
                    new PrayerHandler.PrayerData[]{PrayerHandler.PrayerData.PROTECT_ITEM, PrayerHandler.PrayerData.AUGURY}) {

                @Override
                public boolean shouldPerform(PlayerBot playerBot, Mobile enemy) {
                    boolean canAttackNextTick = playerBot.getTimers().willEndIn(TimerKey.COMBAT_ATTACK, 1);

                    return canAttackNextTick
                            && enemy.getMovementQueue().getMobility().canMove() && !enemy.getTimers().has(TimerKey.FREEZE_IMMUNITY)
                            && CombatSpells.ICE_BARRAGE.getSpell().canCast(playerBot, false);
                }

                @Override
                public void performAfterSwitch(PlayerBot playerBot, Mobile enemy) {
                    System.out.println("Freeze");
                    playerBot.getCombat().setCastSpell(CombatSpells.ICE_BARRAGE.getSpell());
                    playerBot.getCombat().attack(enemy);
                }
            },
            new EnemyDefenseAwareCombatSwitch(new AttackStyleSwitch[]{
                    new AttackStyleSwitch(CombatType.MAGIC,
                            new CombatSwitch(new int[]{STAFF_OF_THE_DEAD, AHRIMS_ROBETOP, AHRIMS_ROBESKIRT, BLESSED_SPIRIT_SHIELD, INFERNAL_CAPE},
                                    new PrayerHandler.PrayerData[]{PrayerHandler.PrayerData.PROTECT_ITEM, PrayerHandler.PrayerData.AUGURY}) {

                                @Override
                                public boolean shouldPerform(PlayerBot playerBot, Mobile enemy) {
                                    return CombatSpells.ICE_BARRAGE.getSpell().canCast(playerBot, false);
                                }

                                @Override
                                public void performAfterSwitch(PlayerBot playerBot, Mobile enemy) {
                                    System.out.println("Magic");
                                    playerBot.getCombat().setCastSpell(CombatSpells.ICE_BARRAGE.getSpell());
                                    playerBot.setSpecialActivated(false);
                                    playerBot.getCombat().attack(enemy);
                                }
                            }
                    ),
                    new AttackStyleSwitch(CombatType.RANGED,
                            new CombatSwitch(new int[]{ARMADYL_CROSSBOW, AVAS_ACCUMULATOR, KARILS_LEATHERSKIRT, KARILS_LEATHERTOP, BLESSED_SPIRIT_SHIELD},
                                    new PrayerHandler.PrayerData[]{PrayerHandler.PrayerData.PROTECT_ITEM, PrayerHandler.PrayerData.RIGOUR}) {

                                @Override
                                public boolean shouldPerform(PlayerBot playerBot, Mobile enemy) {
                                    return true;
                                }

                                @Override
                                public void performAfterSwitch(PlayerBot playerBot, Mobile enemy) {
                                    System.out.println("Ranged");
                                    playerBot.getCombat().setCastSpell(null);
                                    playerBot.setSpecialActivated(false);
                                    playerBot.getCombat().attack(enemy);
                                }
                            }
                    ),
                    new AttackStyleSwitch(CombatType.MELEE,
                            new CombatSwitch(new int[]{ARMADYL_GODSWORD, INFERNAL_CAPE, KARILS_LEATHERSKIRT, KARILS_LEATHERTOP},
                                    new PrayerHandler.PrayerData[]{PrayerHandler.PrayerData.PROTECT_ITEM, PrayerHandler.PrayerData.PIETY}) {

                                @Override
                                public boolean shouldPerform(PlayerBot playerBot, Mobile enemy) {
                                    return playerBot.getMovementQueue().getMobility().canMove()
                                            && enemy.getHitpointsAfterPendingDamage() <= 45;
                                }

                                @Override
                                public void performAfterSwitch(PlayerBot playerBot, Mobile enemy) {
                                    System.out.println("Melee");
                                    playerBot.getCombat().setCastSpell(null);
                                    playerBot.getCombat().attack(enemy);
                                }
                            }
                    ),
            }) {
                @Override
                public boolean shouldPerform(PlayerBot playerBot, Mobile enemy) {
                    boolean canAttackNextTick = playerBot.getTimers().willEndIn(TimerKey.COMBAT_ATTACK, 1);

                    return canAttackNextTick;
                }
            },
    };

    @Override
    public Presetable getItemPreset() {
        return BOT_HARD_TRIBRID;
    }

    @Override
    public CombatAction[] getCombatActions() {
        return COMBAT_ACTIONS;
    }

    @Override
    public int eatAtPercent() {
        return 62;
    }
}
