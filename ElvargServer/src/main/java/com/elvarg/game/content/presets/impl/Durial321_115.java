package com.elvarg.game.content.presets.impl;

import com.elvarg.game.content.presets.Presetable;
import com.elvarg.game.entity.impl.playerbot.fightstyle.PlayerBotFightStyle;
import com.elvarg.game.entity.impl.playerbot.fightstyle.impl.ElvemageFightStyle;
import com.elvarg.game.model.Item;
import com.elvarg.game.model.MagicSpellbook;

import static com.elvarg.util.ItemIdentifiers.*;
import static com.elvarg.util.ItemIdentifiers.RUNE_ARROW;

public class Durial321_115 extends Presetable {

    /**
     * Constructs a new {@link Durial321_115}.
     */
    public Durial321_115() {
        super("Durial321", 1,
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
                        new Item(GREEN_PARTYHAT),
                        new Item(FIRE_CAPE),
                        new Item(ABYSSAL_WHIP),
                        new Item(AMULET_OF_FURY),
                        new Item(AHRIMS_ROBETOP),
                        new Item(TOKTZ_KET_XIL),
                        new Item(AHRIMS_ROBESKIRT),
                        new Item(MITHRIL_GLOVES),
                        new Item(CLIMBING_BOOTS),
                        null,
                        null,
                },
                /* atk, def, str, hp, range, pray, mage */
                new int[] { 95, 88, 98, 99, 80, 54, 94 },
                MagicSpellbook.ANCIENT,
                true
        );
    }

    @Override
    public PlayerBotFightStyle getPlayerBotFightStyle() {
        return null; // TODO: Build Durial321's fight style according to youtube video
    }
}
