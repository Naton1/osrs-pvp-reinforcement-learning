package com.elvarg.game.content.presets.impl;

import com.elvarg.game.content.presets.Presetable;
import com.elvarg.game.entity.impl.playerbot.fightstyle.PlayerBotFightStyle;
import com.elvarg.game.entity.impl.playerbot.fightstyle.impl.DDSPureMFightStyle;
import com.elvarg.game.model.Item;
import com.elvarg.game.model.MagicSpellbook;

import static com.elvarg.util.ItemIdentifiers.*;

public class DDSPure_M_73 extends Presetable {

    /**
     * Constructs a new {@link com.elvarg.game.content.presets.impl.DDSPure_M_73}.
     */
    public DDSPure_M_73() {
        super("DDS Pure (M)", 1,
                new Item[] {
                        new Item(DRAGON_DAGGER_P_PLUS_PLUS_), new Item(SHARK), new Item(SHARK), new Item(SUPER_STRENGTH_4_),
                        new Item(COOKED_KARAMBWAN), new Item(SHARK), new Item(SUPER_RESTORE_4_), new Item(SUPER_ATTACK_4_),
                        new Item(COOKED_KARAMBWAN), new Item(SHARK), new Item(SARADOMIN_BREW_4_), new Item(SARADOMIN_BREW_4_),
                        new Item(COOKED_KARAMBWAN), new Item(SHARK), new Item(SHARK), new Item(SHARK),
                        new Item(COOKED_KARAMBWAN), new Item(SHARK), new Item(SHARK), new Item(SHARK),
                        new Item(COOKED_KARAMBWAN), new Item(SHARK), new Item(SHARK), new Item(SHARK),
                        new Item(COOKED_KARAMBWAN), new Item(SHARK), new Item(RING_OF_RECOIL), new Item(SHARK),
                },
                new Item[] {
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
                new int[] { 60, 1, 99, 85, 1, 1, 1 },
                MagicSpellbook.NORMAL,
                true
        );
    }

    @Override
    public PlayerBotFightStyle getPlayerBotFightStyle() {
        return new DDSPureMFightStyle();
    }

}
