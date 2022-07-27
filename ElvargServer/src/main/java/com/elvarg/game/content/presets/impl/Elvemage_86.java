package com.elvarg.game.content.presets.impl;

import com.elvarg.game.content.presets.Presetable;
import com.elvarg.game.entity.impl.playerbot.fightstyle.PlayerBotFightStyle;
import com.elvarg.game.entity.impl.playerbot.fightstyle.impl.ElvemageFightStyle;
import com.elvarg.game.entity.impl.playerbot.fightstyle.impl.ObbyMaulerFightStyle;
import com.elvarg.game.model.Item;
import com.elvarg.game.model.MagicSpellbook;
import static com.elvarg.util.ItemIdentifiers.*;

public final class Elvemage_86 extends Presetable {

    /**
     * Constructs a new {@link Elvemage_86}.
     */
    public Elvemage_86() {
        super("Elvemage", 1,
                new Item[] {
                        new Item(DRAGON_DAGGER_P_PLUS_PLUS_), new Item(BLOOD_RUNE,1000), new Item(DEATH_RUNE, 1200), new Item(WATER_RUNE, 2500),
                        new Item(SHARK), new Item(SHARK), new Item(SHARK), new Item(SHARK),
                        new Item(SHARK), new Item(SHARK), new Item(SHARK), new Item(SHARK),
                        new Item(SHARK), new Item(SHARK), new Item(SHARK), new Item(SHARK),
                        new Item(SHARK), new Item(SHARK), new Item(SHARK), new Item(SHARK),
                        new Item(SHARK), new Item(SHARK), new Item(SHARK), new Item(SHARK),
                        new Item(SHARK), new Item(SHARK), new Item(SHARK), new Item(SHARK),
                },
                new Item[] {
                        new Item(GREY_HAT),
                        new Item(SARADOMIN_CAPE),
                        new Item(MAGIC_SHORTBOW),
                        new Item(AMULET_OF_GLORY),
                        new Item(MYSTIC_ROBE_TOP),
                        null,
                        new Item(BLACK_DHIDE_CHAPS),
                        new Item(MITHRIL_GLOVES),
                        new Item(CLIMBING_BOOTS),
                        new Item(RING_OF_RECOIL),
                        new Item(RUNE_ARROW, 1000),
                },
                /* atk, def, str, hp, range, pray, mage */
                new int[] { 70, 60, 61, 74, 89, 54, 94 },
                MagicSpellbook.ANCIENT,
                true
        );
    }

    @Override
    public PlayerBotFightStyle getPlayerBotFightStyle() {
        return new ElvemageFightStyle();
    }
}
