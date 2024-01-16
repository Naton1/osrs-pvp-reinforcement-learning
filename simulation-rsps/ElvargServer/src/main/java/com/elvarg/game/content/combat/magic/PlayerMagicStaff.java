package com.elvarg.game.content.combat.magic;

import com.elvarg.game.content.combat.WeaponInterfaces.WeaponInterface;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.Item;

/**
 * A set of constants representing the staves that can be used in place of
 * runes.
 *
 * @author lare96
 */
public enum PlayerMagicStaff {

    AIR(new int[]{1381, 1397, 1405}, new int[]{556}),
    WATER(new int[]{1383, 1395, 1403}, new int[]{555}),
    EARTH(new int[]{1385, 1399, 1407}, new int[]{557}),
    FIRE(new int[]{1387, 1393, 1401}, new int[]{554}),
    MUD(new int[]{6562, 6563}, new int[]{555, 557}),
    LAVA(new int[]{3053, 3054}, new int[]{554, 557});

    /**
     * The staves that can be used in place of runes.
     */
    private int[] staves;

    /**
     * The runes that the staves can be used for.
     */
    private int[] runes;

    /**
     * Create a new {@link PlayerMagicStaff}.
     *
     * @param itemIds the staves that can be used in place of runes.
     * @param runeIds the runes that the staves can be used for.
     */
    private PlayerMagicStaff(int[] itemIds, int[] runeIds) {
        this.staves = itemIds;
        this.runes = runeIds;
    }

    /**
     * Suppress items in the argued array if any of the items match the runes
     * that are represented by the staff the argued player is wielding.
     *
     * @param player        the player to suppress runes for.
     * @param runesRequired the runes to suppress.
     * @return the new array of items with suppressed runes removed.
     */
    public static Item[] suppressRunes(Player player, Item[] runesRequired) {
        if (player.getWeapon() == WeaponInterface.STAFF) {
            for (PlayerMagicStaff m : values()) {
                if (player.getEquipment().containsAny(m.staves)) {
                    for (int id : m.runes) {
                        for (int i = 0; i < runesRequired.length; i++) {
                            if (runesRequired[i] != null && runesRequired[i].getId() == id) {
                                runesRequired[i] = null;
                            }
                        }
                    }
                }
            }
            return runesRequired;
        }
        return runesRequired;
    }
}
