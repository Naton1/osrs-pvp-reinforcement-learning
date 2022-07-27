package com.elvarg.game.content.presets.impl;

import com.elvarg.game.content.presets.Presetable;
import com.elvarg.game.entity.impl.playerbot.fightstyle.PlayerBotFightStyle;
import com.elvarg.game.model.Item;
import com.elvarg.game.model.MagicSpellbook;

import static com.elvarg.util.ItemIdentifiers.*;

public class Att70Zerker_97 extends Presetable {

    public Att70Zerker_97() {
        super("70Att. Zerker", 1,
                new Item[] {
                        new Item(DRAGON_DAGGER_P_PLUS_PLUS_), new Item(SHARK), new Item(SUPER_ATTACK_4_), new Item(SUPER_STRENGTH_4_),
                        new Item(COOKED_KARAMBWAN), new Item(SHARK), new Item(SUPER_RESTORE_4_), new Item(SARADOMIN_BREW_4_),
                        new Item(COOKED_KARAMBWAN), new Item(SHARK), new Item(SUPER_RESTORE_4_), new Item(SARADOMIN_BREW_4_),
                        new Item(COOKED_KARAMBWAN), new Item(SHARK), new Item(SHARK), new Item(SHARK),
                        new Item(COOKED_KARAMBWAN), new Item(SHARK), new Item(SHARK), new Item(SHARK),
                        new Item(COOKED_KARAMBWAN), new Item(SHARK), new Item(SHARK), new Item(RING_OF_RECOIL),
                        new Item(DEATH_RUNE, 1000), new Item(EARTH_RUNE, 1000), new Item(ASTRAL_RUNE, 1000), new Item(SHARK),
                },
                new Item[] {
                        new Item(WARRIOR_HELM),
                        new Item(STRENGTH_CAPE_T_),
                        new Item(ABYSSAL_WHIP),
                        new Item(AMULET_OF_GLORY),
                        new Item(RUNE_PLATEBODY),
                        new Item(RUNE_DEFENDER),
                        new Item(RUNE_PLATELEGS),
                        new Item(BARROWS_GLOVES),
                        new Item(RUNE_BOOTS),
                        new Item(RING_OF_RECOIL),
                        null,
                },
                /* atk, def, str, hp, range, pray, mage */
                new int[] { 70, 45, 99, 95, 99, 52, 94 },
                MagicSpellbook.LUNAR,
                true
        );
    }

    @Override
    public PlayerBotFightStyle getPlayerBotFightStyle() {
        // TODO: Implement fight style for Att70Zerker_97
        return null;
    }

}