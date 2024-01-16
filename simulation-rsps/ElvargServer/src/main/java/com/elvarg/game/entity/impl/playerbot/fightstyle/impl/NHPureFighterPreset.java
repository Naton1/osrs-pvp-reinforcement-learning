package com.elvarg.game.entity.impl.playerbot.fightstyle.impl;

import com.elvarg.game.content.combat.CombatFactory;
import com.elvarg.game.content.combat.CombatSpecial;
import com.elvarg.game.content.combat.CombatType;
import com.elvarg.game.content.combat.magic.CombatSpells;
import com.elvarg.game.content.presets.Presetable;
import com.elvarg.game.entity.impl.Mobile;
import com.elvarg.game.entity.impl.playerbot.PlayerBot;
import com.elvarg.game.entity.impl.playerbot.fightstyle.*;
import com.elvarg.game.model.Item;
import com.elvarg.game.model.ItemInSlot;
import com.elvarg.game.model.MagicSpellbook;
import com.elvarg.game.model.movement.MovementQueue;
import com.elvarg.util.timers.TimerKey;

import static com.elvarg.util.ItemIdentifiers.*;

public class NHPureFighterPreset implements FighterPreset {

    private static final Presetable BOT_NH_PURE_83 = new Presetable("BOT NH Pure",
            new Item[]{
                    new Item(RUNE_CROSSBOW), new Item(BLACK_DHIDE_CHAPS), new Item(RANGING_POTION_4_), new Item(SUPER_STRENGTH_4_),
                    new Item(AVAS_ACCUMULATOR), new Item(GRANITE_MAUL), new Item(MANTA_RAY), new Item(MANTA_RAY),
                    new Item(DRAGON_BOLTS_E_, 75), new Item(MANTA_RAY), new Item(MANTA_RAY), new Item(COOKED_KARAMBWAN),
                    new Item(COOKED_KARAMBWAN), new Item(MANTA_RAY), new Item(MANTA_RAY), new Item(COOKED_KARAMBWAN),
                    new Item(COOKED_KARAMBWAN), new Item(MANTA_RAY), new Item(MANTA_RAY), new Item(MANTA_RAY),
                    new Item(COOKED_KARAMBWAN), new Item(MANTA_RAY), new Item(MANTA_RAY), new Item(MANTA_RAY),
                    new Item(WATER_RUNE, 1000), new Item(BLOOD_RUNE, 1000), new Item(DEATH_RUNE, 1000), new Item(ANGLERFISH),
            },
            new Item[]{
                    new Item(GHOSTLY_HOOD),
                    new Item(ZAMORAK_CAPE),
                    new Item(MAGIC_SHORTBOW),
                    new Item(AMULET_OF_GLORY),
                    new Item(GHOSTLY_ROBE),
                    null,
                    new Item(GHOSTLY_ROBE_2),
                    new Item(MITHRIL_GLOVES),
                    new Item(CLIMBING_BOOTS),
                    new Item(RING_OF_RECOIL),
                    new Item(RUNE_ARROW, 175),
            },
            /* atk, def, str, hp, range, pray, mage */
            new int[]{60, 1, 85, 99, 99, 1, 99},
            MagicSpellbook.ANCIENT,
            true
    );

    public static final CombatAction[] COMBAT_ACTIONS = {
            new CombatSwitch(new int[]{GRANITE_MAUL, BLACK_DHIDE_CHAPS}) {

                @Override
                public boolean shouldPerform(PlayerBot playerBot, Mobile enemy) {
                    return playerBot.getSpecialPercentage() >= 50 && playerBot.getMovementQueue().getMobility().canMove() &&
                            // Switch if the enemy has lowish health
                            enemy.getHitpointsAfterPendingDamage() <= 45;
                }

                @Override
                public void performAfterSwitch(PlayerBot playerBot, Mobile enemy) {
                    playerBot.getCombat().attack(enemy);

                    CombatSpecial.activate(playerBot);
                    CombatSpecial.activate(playerBot);
                }
            },
            new CombatSwitch(new int[]{RING_OF_RECOIL}) {

                @Override
                public boolean shouldPerform(PlayerBot playerBot, Mobile enemy) {
                    var hasRing = ItemInSlot.getFromInventory(RING_OF_RECOIL, playerBot.getInventory()) != null;
                    return hasRing && playerBot.getEquipment().getById(RING_OF_RECOIL) == null;
                }

                @Override
                public void performAfterSwitch(PlayerBot playerBot, Mobile enemy) {
                    playerBot.getCombat().attack(enemy);
                }
            },
            new CombatAction() {

                @Override
                public boolean shouldPerform(PlayerBot playerBot, Mobile enemy) {
                    var combatMethod = CombatFactory.getMethod(enemy);
                    int distance = playerBot.calculateDistance(enemy);

                    boolean cantAttack = playerBot.getTimers().has(TimerKey.COMBAT_ATTACK) && playerBot.getTimers().left(TimerKey.COMBAT_ATTACK) > 1;

                    return cantAttack
                            &&!enemy.getMovementQueue().getMobility().canMove()
                            && distance == 1
                            && CombatFactory.canReach(enemy, combatMethod, playerBot);
                }

                @Override
                public void perform(PlayerBot playerBot, Mobile enemy) {
                    if (playerBot.getMovementQueue().size() > 0) {
                        return;
                    }
                    playerBot.setFollowing(null);
                    MovementQueue.randomClippedStepNotSouth(playerBot, 3);
                }

            },
            new CombatSwitch(new int[]{ZAMORAK_CAPE, GHOSTLY_ROBE_2, GHOSTLY_ROBE}) {

                @Override
                public boolean shouldPerform(PlayerBot playerBot, Mobile enemy) {
                    // Freeze the player if they can move
                    return enemy.getMovementQueue().getMobility().canMove() && !enemy.getTimers().has(TimerKey.FREEZE_IMMUNITY)
                            && CombatSpells.ICE_BARRAGE.getSpell().canCast(playerBot, false);
                }

                @Override
                public void performAfterSwitch(PlayerBot playerBot, Mobile enemy) {
                    playerBot.getCombat().setCastSpell(CombatSpells.ICE_BARRAGE.getSpell());
                    playerBot.getCombat().attack(enemy);
                }
            },
            new CombatSwitch(new int[]{RUNE_CROSSBOW, DRAGON_BOLTS_E_, AVAS_ACCUMULATOR, BLACK_DHIDE_CHAPS}) {

                @Override
                public boolean shouldPerform(PlayerBot playerBot, Mobile enemy) {
                    return enemy.getHitpoints() < 40;
                }

                @Override
                public void performAfterSwitch(PlayerBot playerBot, Mobile enemy) {
                    playerBot.getCombat().attack(enemy);
                }
            },
            new EnemyDefenseAwareCombatSwitch(new AttackStyleSwitch[]{
                    new AttackStyleSwitch(CombatType.MAGIC,
                            new CombatSwitch(new int[]{ZAMORAK_CAPE, GHOSTLY_ROBE_2, GHOSTLY_ROBE}) {

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
                            new CombatSwitch(new int[]{MAGIC_SHORTBOW, RUNE_ARROW, AVAS_ACCUMULATOR, BLACK_DHIDE_CHAPS}) {

                                @Override
                                public boolean shouldPerform(PlayerBot playerBot, Mobile enemy) {
                                    return true;
                                }

                                @Override
                                public void performAfterSwitch(PlayerBot playerBot, Mobile enemy) {
                                    playerBot.setSpecialActivated(false);
                                    playerBot.getCombat().attack(enemy);
                                }
                            }
                    ),
            }) {
                @Override
                public boolean shouldPerform(PlayerBot playerBot, Mobile enemy) {
                    return true;
                }
            },
    };

    @Override
    public Presetable getItemPreset() {
        return BOT_NH_PURE_83;
    }

    @Override
    public CombatAction[] getCombatActions() {
        return COMBAT_ACTIONS;
    }
}
