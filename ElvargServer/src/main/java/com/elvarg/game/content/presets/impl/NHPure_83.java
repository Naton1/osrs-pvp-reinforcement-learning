package com.elvarg.game.content.presets.impl;

import com.elvarg.game.content.presets.Presetable;
import com.elvarg.game.entity.impl.playerbot.fightstyle.PlayerBotFightStyle;
import com.elvarg.game.entity.impl.playerbot.fightstyle.impl.NHPureFightStyle;
import com.elvarg.game.model.Item;
import com.elvarg.game.model.MagicSpellbook;

import static com.elvarg.util.ItemIdentifiers.*;

public class NHPure_83 extends Presetable {

    /**
     * Constructs a new {@link DDSPure_R_73}.
     */
    public NHPure_83() {
        super("NH Pure", 1,
                new Item[] {
                        new Item(RUNE_CROSSBOW), new Item(BLACK_DHIDE_CHAPS), new Item(RANGING_POTION_4_), new Item(SUPER_STRENGTH_4_),
                        new Item(AVAS_ACCUMULATOR), new Item(DRAGON_DAGGER_P_PLUS_PLUS_), new Item(SUPER_RESTORE_4_), new Item(SUPER_ATTACK_4_),
                        new Item(COOKED_KARAMBWAN), new Item(BOOK_OF_WAR), new Item(SUPER_RESTORE_4_), new Item(SARADOMIN_BREW_4_),
                        new Item(COOKED_KARAMBWAN), new Item(SHARK), new Item(SHARK), new Item(SARADOMIN_BREW_4_),
                        new Item(COOKED_KARAMBWAN), new Item(SHARK), new Item(SHARK), new Item(SHARK),
                        new Item(COOKED_KARAMBWAN), new Item(SHARK), new Item(SHARK), new Item(SHARK),
                        new Item(WATER_RUNE, 1000), new Item(BLOOD_RUNE, 1000), new Item(DEATH_RUNE, 1000), new Item(SHARK),
                },
                new Item[] {
                        new Item(GREY_HAT),
                        new Item(ZAMORAK_CAPE),
                        new Item(MAGIC_SHORTBOW),
                        new Item(AMULET_OF_GLORY),
                        new Item(ZAMORAK_ROBE_TOP_ORIGINAL_),
                        null,
                        new Item(ZAMORAK_ROBE_BOTTOMS_ORIGINAL_),
                        new Item(MITHRIL_GLOVES),
                        new Item(CLIMBING_BOOTS),
                        new Item(RING_OF_RECOIL),
                        new Item(DRAGONSTONE_BOLTS_E_, 75),
                },
                /* atk, def, str, hp, range, pray, mage */
                new int[] { 60, 1, 99, 99, 99, 52, 94 },
                MagicSpellbook.ANCIENT,
                true
        );
    }

    @Override
    public PlayerBotFightStyle getPlayerBotFightStyle() {
        return new NHPureFightStyle();
    }
}
