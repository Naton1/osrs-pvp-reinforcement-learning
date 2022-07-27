package com.elvarg.game.content.presets.impl;

import com.elvarg.game.content.presets.Presetable;
import com.elvarg.game.entity.impl.playerbot.fightstyle.PlayerBotFightStyle;
import com.elvarg.game.entity.impl.playerbot.fightstyle.impl.DDSPureRFightStyle;
import com.elvarg.game.model.Item;
import com.elvarg.game.model.MagicSpellbook;

import static com.elvarg.util.ItemIdentifiers.*;

public class DDSPure_R_73  extends Presetable {

    /**
     * Constructs a new {@link DDSPure_R_73}.
     */
    public DDSPure_R_73() {
        super("DDS Pure (R)", 1,
                new Item[] {
                        new Item(RUNE_CROSSBOW), new Item(DRAGONSTONE_BOLTS_E_,75), new Item(RANGING_POTION_4_), new Item(SUPER_STRENGTH_4_),
                        new Item(COOKED_KARAMBWAN), new Item(DRAGON_DAGGER_P_PLUS_PLUS_), new Item(SUPER_RESTORE_4_), new Item(SUPER_ATTACK_4_),
                        new Item(COOKED_KARAMBWAN), new Item(SHARK), new Item(SARADOMIN_BREW_4_), new Item(SARADOMIN_BREW_4_),
                        new Item(COOKED_KARAMBWAN), new Item(SHARK), new Item(SHARK), new Item(SHARK),
                        new Item(COOKED_KARAMBWAN), new Item(SHARK), new Item(SHARK), new Item(SHARK),
                        new Item(COOKED_KARAMBWAN), new Item(SHARK), new Item(SHARK), new Item(SHARK),
                        new Item(COOKED_KARAMBWAN), new Item(SHARK), new Item(RING_OF_RECOIL), new Item(SHARK),
                },
                new Item[] {
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
                new int[] { 60, 1, 99, 85, 99, 1, 1 },
                MagicSpellbook.NORMAL,
                true
        );
    }

    @Override
    public PlayerBotFightStyle getPlayerBotFightStyle() {
        return new DDSPureRFightStyle();
    }
}
