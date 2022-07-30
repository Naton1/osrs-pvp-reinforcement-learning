package com.elvarg.game.content.presets;

import com.elvarg.game.model.Item;
import com.elvarg.game.model.MagicSpellbook;

import static com.elvarg.util.ItemIdentifiers.*;

public class PredefinedPresets {

    public static final Presetable ATT_60_ZERKER_94 = new Presetable("60Att. Zerker",
            new Item[]{
                    new Item(DRAGON_DAGGER_P_PLUS_PLUS_), new Item(SHARK), new Item(SUPER_ATTACK_4_), new Item(SUPER_STRENGTH_4_),
                    new Item(COOKED_KARAMBWAN), new Item(SHARK), new Item(SUPER_RESTORE_4_), new Item(SARADOMIN_BREW_4_),
                    new Item(COOKED_KARAMBWAN), new Item(SHARK), new Item(SUPER_RESTORE_4_), new Item(SARADOMIN_BREW_4_),
                    new Item(COOKED_KARAMBWAN), new Item(SHARK), new Item(SHARK), new Item(SHARK),
                    new Item(COOKED_KARAMBWAN), new Item(SHARK), new Item(SHARK), new Item(SHARK),
                    new Item(COOKED_KARAMBWAN), new Item(SHARK), new Item(SHARK), new Item(RING_OF_RECOIL),
                    new Item(DEATH_RUNE, 1000), new Item(EARTH_RUNE, 1000), new Item(ASTRAL_RUNE, 1000), new Item(SHARK),
            },
            new Item[]{
                    new Item(WARRIOR_HELM),
                    new Item(STRENGTH_CAPE_T_),
                    new Item(DRAGON_SCIMITAR),
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
            new int[]{60, 45, 99, 95, 99, 52, 94},
            MagicSpellbook.LUNAR,
            true
    );

    public static final Presetable ATT_70_ZERKER_97 = new Presetable("70Att. Zerker",
            new Item[]{
                    new Item(DRAGON_DAGGER_P_PLUS_PLUS_), new Item(SHARK), new Item(SUPER_ATTACK_4_), new Item(SUPER_STRENGTH_4_),
                    new Item(COOKED_KARAMBWAN), new Item(SHARK), new Item(SUPER_RESTORE_4_), new Item(SARADOMIN_BREW_4_),
                    new Item(COOKED_KARAMBWAN), new Item(SHARK), new Item(SUPER_RESTORE_4_), new Item(SARADOMIN_BREW_4_),
                    new Item(COOKED_KARAMBWAN), new Item(SHARK), new Item(SHARK), new Item(SHARK),
                    new Item(COOKED_KARAMBWAN), new Item(SHARK), new Item(SHARK), new Item(SHARK),
                    new Item(COOKED_KARAMBWAN), new Item(SHARK), new Item(SHARK), new Item(RING_OF_RECOIL),
                    new Item(DEATH_RUNE, 1000), new Item(EARTH_RUNE, 1000), new Item(ASTRAL_RUNE, 1000), new Item(SHARK),
            },
            new Item[]{
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
            new int[]{70, 45, 99, 95, 99, 52, 94},
            MagicSpellbook.LUNAR,
            true
    );

    public static final Presetable DDS_PURE_M_73 = new Presetable("DDS Pure (M)",
            new Item[]{
                    new Item(DRAGON_DAGGER_P_PLUS_PLUS_), new Item(SHARK), new Item(SHARK), new Item(SUPER_STRENGTH_4_),
                    new Item(COOKED_KARAMBWAN), new Item(SHARK), new Item(SUPER_RESTORE_4_), new Item(SUPER_ATTACK_4_),
                    new Item(COOKED_KARAMBWAN), new Item(SHARK), new Item(SARADOMIN_BREW_4_), new Item(SARADOMIN_BREW_4_),
                    new Item(COOKED_KARAMBWAN), new Item(SHARK), new Item(SHARK), new Item(SHARK),
                    new Item(COOKED_KARAMBWAN), new Item(SHARK), new Item(SHARK), new Item(SHARK),
                    new Item(COOKED_KARAMBWAN), new Item(SHARK), new Item(SHARK), new Item(SHARK),
                    new Item(COOKED_KARAMBWAN), new Item(SHARK), new Item(RING_OF_RECOIL), new Item(SHARK),
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

    public static final Presetable DDS_PURE_R_73 = new Presetable("DDS Pure (R)",
            new Item[]{
                    new Item(RUNE_CROSSBOW), new Item(DRAGON_BOLTS_E_, 75), new Item(RANGING_POTION_4_), new Item(SUPER_STRENGTH_4_),
                    new Item(COOKED_KARAMBWAN), new Item(DRAGON_DAGGER_P_PLUS_PLUS_), new Item(SUPER_RESTORE_4_), new Item(SUPER_ATTACK_4_),
                    new Item(COOKED_KARAMBWAN), new Item(SHARK), new Item(SARADOMIN_BREW_4_), new Item(SARADOMIN_BREW_4_),
                    new Item(COOKED_KARAMBWAN), new Item(SHARK), new Item(SHARK), new Item(SHARK),
                    new Item(COOKED_KARAMBWAN), new Item(SHARK), new Item(SHARK), new Item(SHARK),
                    new Item(COOKED_KARAMBWAN), new Item(SHARK), new Item(SHARK), new Item(SHARK),
                    new Item(COOKED_KARAMBWAN), new Item(SHARK), new Item(RING_OF_RECOIL), new Item(SHARK),
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

    public static final Presetable G_MAULER_70 = new Presetable("G Mauler (R)",
            new Item[]{
                    new Item(RUNE_CROSSBOW), new Item(DRAGON_BOLTS_E_, 75), new Item(RANGING_POTION_4_), new Item(SUPER_STRENGTH_4_),
                    new Item(COOKED_KARAMBWAN), new Item(GRANITE_MAUL), new Item(SUPER_RESTORE_4_), new Item(SUPER_ATTACK_4_),
                    new Item(COOKED_KARAMBWAN), new Item(SHARK), new Item(SARADOMIN_BREW_4_), new Item(SARADOMIN_BREW_4_),
                    new Item(COOKED_KARAMBWAN), new Item(SHARK), new Item(SHARK), new Item(SHARK),
                    new Item(COOKED_KARAMBWAN), new Item(SHARK), new Item(SHARK), new Item(SHARK),
                    new Item(COOKED_KARAMBWAN), new Item(SHARK), new Item(SHARK), new Item(SHARK),
                    new Item(COOKED_KARAMBWAN), new Item(SHARK), new Item(RING_OF_RECOIL), new Item(SHARK),
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
            new int[]{50, 1, 99, 85, 99, 1, 1},
            MagicSpellbook.NORMAL,
            true
    );

    public static final Presetable MAIN_HYBRID_126 = new Presetable("Main Hybrid",
            new Item[]{
                    new Item(RUNE_PLATEBODY), new Item(ABYSSAL_WHIP), new Item(BLACK_DHIDE_BODY), new Item(SARADOMIN_BREW_4_),
                    new Item(RUNE_PLATELEGS), new Item(RUNE_DEFENDER), new Item(DRAGON_DAGGER_P_PLUS_PLUS_), new Item(SARADOMIN_BREW_4_),
                    new Item(COOKED_KARAMBWAN), new Item(COOKED_KARAMBWAN), new Item(COOKED_KARAMBWAN), new Item(SUPER_RESTORE_4_),
                    new Item(SHARK), new Item(SHARK), new Item(SHARK), new Item(SUPER_RESTORE_4_),
                    new Item(SHARK), new Item(SHARK), new Item(SHARK), new Item(SUPER_ATTACK_4_),
                    new Item(SHARK), new Item(SHARK), new Item(SHARK), new Item(SUPER_STRENGTH_4_),
                    new Item(SHARK), new Item(DEATH_RUNE, 4000), new Item(WATER_RUNE, 6000), new Item(BLOOD_RUNE, 2000),
            },
            new Item[]{
                    new Item(HELM_OF_NEITIZNOT),
                    new Item(SARADOMIN_CAPE),
                    new Item(ANCIENT_STAFF),
                    new Item(AMULET_OF_GLORY),
                    new Item(MYSTIC_ROBE_TOP),
                    new Item(UNHOLY_BOOK),
                    new Item(MYSTIC_ROBE_BOTTOM),
                    new Item(BARROWS_GLOVES),
                    new Item(CLIMBING_BOOTS),
                    new Item(RING_OF_RECOIL),
                    null,
            },
            /* atk, def, str, hp, range, pray, mage */
            new int[]{99, 99, 99, 99, 99, 99, 99},
            MagicSpellbook.ANCIENT,
            true
    );

    public static final Presetable MAIN_RUNE_126 = new Presetable("Main Rune",
            new Item[]{
                    new Item(SUPER_STRENGTH_4_), new Item(SARADOMIN_BREW_4_), new Item(SUPER_RESTORE_4_), new Item(SUPER_RESTORE_4_),
                    new Item(SUPER_ATTACK_4_), new Item(ASTRAL_RUNE, 1000), new Item(EARTH_RUNE, 1000), new Item(DEATH_RUNE, 1000),
                    new Item(DRAGON_DAGGER_P_PLUS_PLUS_), new Item(COOKED_KARAMBWAN), new Item(COOKED_KARAMBWAN), new Item(COOKED_KARAMBWAN),
                    new Item(COOKED_KARAMBWAN), new Item(COOKED_KARAMBWAN), new Item(COOKED_KARAMBWAN), new Item(COOKED_KARAMBWAN),
                    new Item(SHARK), new Item(SHARK), new Item(SHARK), new Item(SHARK),
                    new Item(SHARK), new Item(SHARK), new Item(SHARK), new Item(SHARK),
                    new Item(SHARK), new Item(SHARK), new Item(RING_OF_RECOIL), new Item(SHARK),
            },
            new Item[]{
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
            new int[]{99, 99, 99, 99, 99, 99, 99},
            MagicSpellbook.LUNAR,
            true
    );

    public static final Presetable MAIN_TRIBRID_126 = new Presetable("Main Tribrid",
            new Item[]{
                    new Item(AVAS_ACCUMULATOR), new Item(BLACK_DHIDE_BODY), new Item(ABYSSAL_WHIP), new Item(SARADOMIN_BREW_4_),
                    new Item(RUNE_CROSSBOW), new Item(RUNE_PLATELEGS), new Item(RUNE_DEFENDER), new Item(SARADOMIN_BREW_4_),
                    new Item(COOKED_KARAMBWAN), new Item(COOKED_KARAMBWAN), new Item(DRAGON_DAGGER_P_PLUS_PLUS_), new Item(SUPER_RESTORE_4_),
                    new Item(SHARK), new Item(SHARK), new Item(SHARK), new Item(SUPER_RESTORE_4_),
                    new Item(SHARK), new Item(SHARK), new Item(SHARK), new Item(SUPER_STRENGTH_4_),
                    new Item(SHARK), new Item(SHARK), new Item(SHARK), new Item(SUPER_ATTACK_4_),
                    new Item(WATER_RUNE, 6000), new Item(BLOOD_RUNE, 2000), new Item(DEATH_RUNE, 4000), new Item(RANGING_POTION_4_),
            },
            new Item[]{
                    new Item(HELM_OF_NEITIZNOT),
                    new Item(SARADOMIN_CAPE),
                    new Item(ANCIENT_STAFF),
                    new Item(AMULET_OF_GLORY),
                    new Item(MYSTIC_ROBE_TOP),
                    new Item(SPIRIT_SHIELD),
                    new Item(MYSTIC_ROBE_BOTTOM),
                    new Item(BARROWS_GLOVES),
                    new Item(CLIMBING_BOOTS),
                    new Item(RING_OF_RECOIL),
                    new Item(DRAGON_BOLTS_E_, 500),
            },
            /* atk, def, str, hp, range, pray, mage */
            new int[]{99, 99, 99, 99, 99, 99, 99},
            MagicSpellbook.ANCIENT,
            true
    );

    public static final Presetable NH_PURE_83 = new Presetable("NH Pure",
            new Item[]{
                    new Item(RUNE_CROSSBOW), new Item(BLACK_DHIDE_CHAPS), new Item(RANGING_POTION_4_), new Item(SUPER_STRENGTH_4_),
                    new Item(AVAS_ACCUMULATOR), new Item(DRAGON_DAGGER_P_PLUS_PLUS_), new Item(SUPER_RESTORE_4_), new Item(SUPER_ATTACK_4_),
                    new Item(DRAGON_BOLTS_E_, 75), new Item(BOOK_OF_WAR), new Item(SUPER_RESTORE_4_), new Item(SARADOMIN_BREW_4_),
                    new Item(COOKED_KARAMBWAN), new Item(SHARK), new Item(SHARK), new Item(SARADOMIN_BREW_4_),
                    new Item(COOKED_KARAMBWAN), new Item(SHARK), new Item(SHARK), new Item(SHARK),
                    new Item(COOKED_KARAMBWAN), new Item(SHARK), new Item(SHARK), new Item(SHARK),
                    new Item(WATER_RUNE, 1000), new Item(BLOOD_RUNE, 1000), new Item(DEATH_RUNE, 1000), new Item(SHARK),
            },
            new Item[]{
                    new Item(GREY_HAT),
                    new Item(ZAMORAK_CAPE),
                    new Item(MAGIC_SHORTBOW),
                    new Item(AMULET_OF_GLORY),
                    new Item(ZAMORAK_ROBE),
                    null,
                    new Item(ZAMORAK_ROBE_3),
                    new Item(MITHRIL_GLOVES),
                    new Item(CLIMBING_BOOTS),
                    new Item(RING_OF_RECOIL),
                    new Item(RUNE_ARROW, 175),
            },
            /* atk, def, str, hp, range, pray, mage */
            new int[]{60, 1, 85, 99, 99, 1, 99},
            MagicSpellbook.ANCIENT,
            true
    );

    public static final Presetable OBBY_MAULER_57 = new Presetable("Obby Mauler",
            new Item[]{
                    new Item(SUPER_STRENGTH_4_), new Item(RANGING_POTION_4_), new Item(SUPER_RESTORE_4_), new Item(RING_OF_RECOIL),
                    new Item(COOKED_KARAMBWAN), new Item(COOKED_KARAMBWAN), new Item(COOKED_KARAMBWAN), new Item(COOKED_KARAMBWAN),
                    new Item(COOKED_KARAMBWAN), new Item(COOKED_KARAMBWAN), new Item(COOKED_KARAMBWAN), new Item(COOKED_KARAMBWAN),
                    new Item(TZHAAR_KET_OM), new Item(SHARK), new Item(SHARK), new Item(SHARK),
                    new Item(SHARK), new Item(SHARK), new Item(SHARK), new Item(SHARK),
                    new Item(SHARK), new Item(SHARK), new Item(SHARK), new Item(SHARK),
                    new Item(SHARK), new Item(SHARK), new Item(SHARK), new Item(SHARK),
            },
            new Item[]{
                    new Item(IRON_FULL_HELM),
                    new Item(OBSIDIAN_CAPE),
                    new Item(RUNE_KNIFE, 250),
                    new Item(AMULET_OF_GLORY),
                    new Item(IRON_PLATEBODY),
                    new Item(UNHOLY_BOOK),
                    new Item(BLACK_DHIDE_CHAPS),
                    new Item(MITHRIL_GLOVES),
                    new Item(CLIMBING_BOOTS),
                    new Item(RING_OF_RECOIL)
            },
            /* atk, def, str, hp, range, pray, mage */
            new int[]{1, 1, 99, 80, 60, 31, 1},
            MagicSpellbook.NORMAL,
            true
    );

}
