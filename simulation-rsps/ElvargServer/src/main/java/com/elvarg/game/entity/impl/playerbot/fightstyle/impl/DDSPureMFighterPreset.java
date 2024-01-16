package com.elvarg.game.entity.impl.playerbot.fightstyle.impl;

import com.elvarg.game.content.combat.CombatSpecial;
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

public class DDSPureMFighterPreset implements FighterPreset {

    private static final Presetable BOT_DDS_PURE_M_73 = new Presetable("DDS Pure (M)",
            new Item[]{
                    new Item(DRAGON_DAGGER_P_PLUS_PLUS_), new Item(MANTA_RAY), new Item(MANTA_RAY), new Item(SUPER_STRENGTH_4_),
                    new Item(COOKED_KARAMBWAN), new Item(MANTA_RAY), new Item(COOKED_KARAMBWAN), new Item(SUPER_ATTACK_4_),
                    new Item(COOKED_KARAMBWAN), new Item(MANTA_RAY), new Item(MANTA_RAY), new Item(MANTA_RAY),
                    new Item(COOKED_KARAMBWAN), new Item(MANTA_RAY), new Item(MANTA_RAY), new Item(MANTA_RAY),
                    new Item(COOKED_KARAMBWAN), new Item(MANTA_RAY), new Item(MANTA_RAY), new Item(MANTA_RAY),
                    new Item(COOKED_KARAMBWAN), new Item(MANTA_RAY), new Item(MANTA_RAY), new Item(MANTA_RAY),
                    new Item(COOKED_KARAMBWAN), new Item(MANTA_RAY), new Item(MANTA_RAY), new Item(ANGLERFISH),
            },
            new Item[]{
                    new Item(IRON_FULL_HELM),
                    new Item(OBSIDIAN_CAPE),
                    new Item(DRAGON_SCIMITAR),
                    new Item(AMULET_OF_GLORY),
                    new Item(IRON_PLATEBODY),
                    new Item(BOOK_OF_DARKNESS),
                    new Item(BLACK_DHIDE_CHAPS),
                    new Item(MITHRIL_GLOVES),
                    new Item(CLIMBING_BOOTS),
                    new Item(RING_OF_RECOIL),
                    null,
            },
            /* atk, def, str, hp, range, pray, mage */
            new int[]{60, 1, 99, 85, 1, 1, 1},
            MagicSpellbook.NORMAL,
            true
    );

    public static final CombatAction[] COMBAT_ACTIONS = {
            new CombatSwitch(new int[]{DRAGON_DAGGER_P_PLUS_PLUS_}) {

                @Override
                public boolean shouldPerform(PlayerBot playerBot, Mobile enemy) {
                    boolean canAttackNextTick = playerBot.getTimers().getTicks(TimerKey.COMBAT_ATTACK) <= 1;
                    return canAttackNextTick && playerBot.getSpecialPercentage() >= 25 &&
                            // Switch if the enemy has lowish health
                            enemy.getHitpoints() < 46;
                }

                @Override
                public void performAfterSwitch(PlayerBot playerBot, Mobile enemy) {
                    if (!playerBot.isSpecialActivated()) {
                        CombatSpecial.activate(playerBot);
                    }
                    playerBot.getCombat().attack(enemy);
                }
            },
            new CombatSwitch(new int[]{DRAGON_SCIMITAR}) {

                @Override
                public boolean shouldPerform(PlayerBot playerBot, Mobile enemy) {
                    return true;
                }

                @Override
                public void performAfterSwitch(PlayerBot playerBot, Mobile enemy) {
                    playerBot.setSpecialActivated(false);
                    playerBot.getCombat().attack(enemy);
                }
            },
    };

    @Override
    public Presetable getItemPreset() {
        return BOT_DDS_PURE_M_73;
    }

    @Override
    public CombatAction[] getCombatActions() {
        return COMBAT_ACTIONS;
    }
}
