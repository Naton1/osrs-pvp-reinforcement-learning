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

import static com.elvarg.util.ItemIdentifiers.*;
import static com.elvarg.util.ItemIdentifiers.RUNE_ARROW;

public class DDSPureRFighterPreset implements FighterPreset {

    public static final Presetable BOT_DDS_PURE_R_73 = new Presetable("DDS Pure (R)",
            new Item[]{
                    new Item(RUNE_CROSSBOW), new Item(DRAGON_BOLTS_E_, 75), new Item(RANGING_POTION_4_), new Item(SUPER_STRENGTH_4_),
                    new Item(COOKED_KARAMBWAN), new Item(DRAGON_DAGGER_P_PLUS_PLUS_), new Item(COOKED_KARAMBWAN), new Item(COOKED_KARAMBWAN),
                    new Item(COOKED_KARAMBWAN), new Item(MANTA_RAY), new Item(MANTA_RAY), new Item(MANTA_RAY),
                    new Item(COOKED_KARAMBWAN), new Item(MANTA_RAY), new Item(MANTA_RAY), new Item(MANTA_RAY),
                    new Item(COOKED_KARAMBWAN), new Item(MANTA_RAY), new Item(MANTA_RAY), new Item(MANTA_RAY),
                    new Item(COOKED_KARAMBWAN), new Item(MANTA_RAY), new Item(MANTA_RAY), new Item(MANTA_RAY),
                    new Item(COOKED_KARAMBWAN), new Item(MANTA_RAY), new Item(RING_OF_RECOIL), new Item(ANGLERFISH),
            },
            new Item[]{
                    new Item(COIF),
                    new Item(AVAS_ACCUMULATOR),
                    new Item(MAGIC_SHORTBOW),
                    new Item(AMULET_OF_GLORY),
                    new Item(LEATHER_BODY),
                    null,
                    new Item(BLACK_DHIDE_CHAPS),
                    new Item(MITHRIL_GLOVES),
                    new Item(CLIMBING_BOOTS),
                    new Item(RING_OF_RECOIL),
                    new Item(RUNE_ARROW, 75),
            },
            /* atk, def, str, hp, range, pray, mage */
            new int[]{60, 1, 99, 85, 99, 1, 1},
            MagicSpellbook.NORMAL,
            true
    );

    public static final CombatAction[] COMBAT_ACTIONS = {
            new CombatSwitch(new int[]{DRAGON_DAGGER_P_PLUS_PLUS_}) {

                @Override
                public boolean shouldPerform(PlayerBot playerBot, Mobile enemy) {
                    return playerBot.getSpecialPercentage() >= 25 &&
                            // Switch if the enemy has lowish health
                            enemy.getHitpoints() < 45;
                }

                @Override
                public void performAfterSwitch(PlayerBot playerBot, Mobile enemy) {
                    if (!playerBot.isSpecialActivated()) {
                        CombatSpecial.activate(playerBot);
                    }
                    playerBot.getCombat().attack(enemy);
                }
            },
            new CombatSwitch(new int[]{RUNE_CROSSBOW, DRAGON_BOLTS_E_}) {

                @Override
                public boolean shouldPerform(PlayerBot playerBot, Mobile enemy) {
                    return enemy.getHitpoints() < 40;
                }

                @Override
                public void performAfterSwitch(PlayerBot playerBot, Mobile enemy) {
                    playerBot.getCombat().attack(enemy);
                }
            },
            new CombatSwitch(new int[]{MAGIC_SHORTBOW, RUNE_ARROW}) {

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
        return BOT_DDS_PURE_R_73;
    }

    @Override
    public CombatAction[] getCombatActions() {
        return COMBAT_ACTIONS;
    }
}
