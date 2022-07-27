package com.elvarg.game.content.presets.impl;

import com.elvarg.game.content.presets.Presetable;
import com.elvarg.game.entity.impl.playerbot.fightstyle.PlayerBotFightStyle;
import com.elvarg.game.model.Item;
import com.elvarg.game.model.MagicSpellbook;

import static com.elvarg.util.ItemIdentifiers.*;

public class MainRune_126 extends Presetable {

    public MainRune_126() {
        super("Main Rune", 1,
                new Item[] {
                        new Item(SUPER_STRENGTH_4_), new Item(SARADOMIN_BREW_4_), new Item(SUPER_RESTORE_4_), new Item(SUPER_RESTORE_4_),
                        new Item(SUPER_ATTACK_4_), new Item(ASTRAL_RUNE, 1000), new Item(EARTH_RUNE, 1000), new Item(DEATH_RUNE, 1000),
                        new Item(DRAGON_DAGGER_P_PLUS_PLUS_), new Item(COOKED_KARAMBWAN), new Item(COOKED_KARAMBWAN), new Item(COOKED_KARAMBWAN),
                        new Item(COOKED_KARAMBWAN), new Item(COOKED_KARAMBWAN), new Item(COOKED_KARAMBWAN), new Item(COOKED_KARAMBWAN),
                        new Item(SHARK), new Item(SHARK), new Item(SHARK), new Item(SHARK),
                        new Item(SHARK), new Item(SHARK), new Item(SHARK), new Item(SHARK),
                        new Item(SHARK), new Item(SHARK), new Item(RING_OF_RECOIL), new Item(SHARK),
                },
                new Item[] {
                        new Item(HELM_OF_NEITIZNOT),
                        new Item(OBSIDIAN_CAPE),
                        new Item(ABYSSAL_WHIP),
                        new Item(AMULET_OF_GLORY),
                        new Item(RUNE_PLATEBODY),
                        new Item(RUNE_DEFENDER),
                        new Item(RUNE_PLATELEGS),
                        new Item(BARROWS_GLOVES),
                        new Item(DRAGON_BOOTS),
                        new Item(RING_OF_RECOIL),
                        null,
                },
                /* atk, def, str, hp, range, pray, mage */
                new int[] { 99, 99, 99, 99, 99, 99, 99 },
                MagicSpellbook.LUNAR,
                true
        );
    }

    @Override
    public PlayerBotFightStyle getPlayerBotFightStyle() {
        // TODO: Implement fight style for MainRune_126
        return null;
    }

}