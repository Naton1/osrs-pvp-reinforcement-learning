package com.elvarg.game.content.presets.impl;

import com.elvarg.game.content.presets.Presetable;
import com.elvarg.game.entity.impl.playerbot.fightstyle.PlayerBotFightStyle;
import com.elvarg.game.entity.impl.playerbot.fightstyle.impl.ObbyMaulerFightStyle;
import com.elvarg.game.model.Item;
import com.elvarg.game.model.MagicSpellbook;
import static com.elvarg.util.ItemIdentifiers.*;

public final class ObbyMauler_57 extends Presetable {

    /**
     * Constructs a new {@link ObbyMauler_57}.
     */
    public ObbyMauler_57() {
        super("Obby Mauler", 0,
            new Item[] {
                new Item(SUPER_STRENGTH_4_), new Item(RANGING_POTION_4_), new Item(SUPER_RESTORE_4_), new Item(RING_OF_RECOIL),
                new Item(COOKED_KARAMBWAN), new Item(COOKED_KARAMBWAN), new Item(COOKED_KARAMBWAN), new Item(COOKED_KARAMBWAN),
                new Item(COOKED_KARAMBWAN), new Item(COOKED_KARAMBWAN), new Item(COOKED_KARAMBWAN), new Item(COOKED_KARAMBWAN),
                new Item(TZHAAR_KET_OM), new Item(SHARK), new Item(SHARK), new Item(SHARK),
                new Item(SHARK), new Item(SHARK), new Item(SHARK), new Item(SHARK),
                new Item(SHARK), new Item(SHARK), new Item(SHARK), new Item(SHARK),
                new Item(SHARK), new Item(SHARK), new Item(SHARK), new Item(SHARK),
            },
            new Item[] {
                new Item(IRON_FULL_HELM),
                new Item(OBSIDIAN_CAPE),
                new Item(RUNE_KNIFE, 250),
                new Item(AMULET_OF_GLORY),
                new Item(IRON_PLATEBODY),
                new Item(UNHOLY_BOOK),
                new Item(IRON_PLATELEGS),
                new Item(MITHRIL_GLOVES),
                new Item(CLIMBING_BOOTS),
                new Item(RING_OF_RECOIL)
            },
            /* atk, def, str, hp, range, pray, mage */
            new int[] { 1, 1, 99, 80, 60, 31, 1 },
            MagicSpellbook.NORMAL,
    true
        );
    }

    @Override
    public PlayerBotFightStyle getPlayerBotFightStyle() {
        return new ObbyMaulerFightStyle();
    }
}
