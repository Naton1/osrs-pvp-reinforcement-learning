package com.elvarg.game.entity.impl.playerbot.fightstyle.impl;

import com.elvarg.game.content.presets.Presetable;
import com.elvarg.game.entity.impl.Mobile;
import com.elvarg.game.entity.impl.playerbot.PlayerBot;
import com.elvarg.game.entity.impl.playerbot.fightstyle.CombatAction;
import com.elvarg.game.entity.impl.playerbot.fightstyle.CombatSwitch;
import com.elvarg.game.entity.impl.playerbot.fightstyle.FighterPreset;
import com.elvarg.game.model.Item;
import com.elvarg.game.model.MagicSpellbook;
import com.elvarg.util.timers.TimerKey;

import static com.elvarg.util.ItemIdentifiers.*;

public class F2PMeleeFighterPreset implements FighterPreset {

    public static final Presetable PRESETABLE = new Presetable("F2P Pure",
            new Item[]{
                    new Item(RUNE_2H_SWORD), new Item(STRENGTH_POTION_4_), new Item(SWORDFISH), new Item(SWORDFISH),
                    new Item(SWORDFISH), new Item(SWORDFISH), new Item(SWORDFISH), new Item(SWORDFISH),
                    new Item(SWORDFISH), new Item(SWORDFISH), new Item(SWORDFISH), new Item(SWORDFISH),
                    new Item(SWORDFISH), new Item(SWORDFISH), new Item(SWORDFISH), new Item(SWORDFISH),
                    new Item(SWORDFISH), new Item(SWORDFISH), new Item(SWORDFISH), new Item(SWORDFISH),
                    new Item(SWORDFISH), new Item(SWORDFISH), new Item(SWORDFISH), new Item(SWORDFISH),
                    new Item(SWORDFISH), new Item(SWORDFISH), new Item(SWORDFISH), new Item(SWORDFISH),
            },
            new Item[] {
                    new Item(IRON_FULL_HELM),
                    new Item(CAPE_OF_LEGENDS),
                    new Item(MAPLE_SHORTBOW),
                    new Item(AMULET_OF_POWER),
                    new Item(LEATHER_BODY),
                    new Item(GREEN_DHIDE_VAMB),
                    new Item(GREEN_DHIDE_CHAPS),
                    null,
                    new Item(LEATHER_BOOTS),
                    null,
                    new Item(ADAMANT_ARROW, 100),
            },
            /* atk, def, str, hp, range, pray, mage */
            new int[]{40, 1, 90, 58, 84, 1, 1},
            MagicSpellbook.NORMAL,
            true
    );

    public static final CombatAction[] COMBAT_ACTIONS = {
            new CombatSwitch(new int[]{ RUNE_2H_SWORD }) {

                /**
                 * KO Weapon - Rune 2H sword
                 */

                @Override
                public boolean shouldPerform(PlayerBot playerBot, Mobile enemy) {
                    boolean canAttackNextTick = playerBot.getTimers().getTicks(TimerKey.COMBAT_ATTACK) <= 1;
                    return canAttackNextTick &&
                            // Switch if the enemy has lowish health
                            enemy.getHitpoints() < 25;
                }

                @Override
                public void performAfterSwitch(PlayerBot playerBot, Mobile enemy) {
                    playerBot.getCombat().attack(enemy);
                }
            },

            new CombatSwitch(new int[]{ MAPLE_SHORTBOW }) {

                /**
                 * Default Weapon - Maple Shortbow (Max DPS)
                 */

                @Override
                public boolean shouldPerform(PlayerBot playerBot, Mobile enemy) {
                    return enemy.getHitpoints() >= 25;
                }

                @Override
                public void performAfterSwitch(PlayerBot playerBot, Mobile enemy) {
                    playerBot.getCombat().attack(enemy);
                }
            },
    };

    @Override
    public Presetable getItemPreset() {
        return PRESETABLE;
    }

    @Override
    public CombatAction[] getCombatActions() {
        return COMBAT_ACTIONS;
    }
}
