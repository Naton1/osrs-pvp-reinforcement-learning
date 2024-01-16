package com.github.naton1.rl.util;

import com.elvarg.game.content.combat.CombatType;
import com.elvarg.game.content.combat.WeaponInterfaces;
import com.elvarg.game.entity.impl.player.Player;
import java.util.EnumSet;
import java.util.Set;

public class CombatStyles {

    private static final Set<WeaponInterfaces.WeaponInterface> rangedWeapons = EnumSet.of(
            WeaponInterfaces.WeaponInterface.CROSSBOW,
            WeaponInterfaces.WeaponInterface.BALLISTA,
            WeaponInterfaces.WeaponInterface.JAVELIN,
            WeaponInterfaces.WeaponInterface.KNIFE,
            WeaponInterfaces.WeaponInterface.DARK_BOW,
            WeaponInterfaces.WeaponInterface.THROWNAXE,
            WeaponInterfaces.WeaponInterface.KARILS_CROSSBOW,
            WeaponInterfaces.WeaponInterface.SHORTBOW,
            WeaponInterfaces.WeaponInterface.LONGBOW,
            WeaponInterfaces.WeaponInterface.OBBY_RINGS,
            WeaponInterfaces.WeaponInterface.DART);

    private static final Set<WeaponInterfaces.WeaponInterface> mageWeapons =
            EnumSet.of(WeaponInterfaces.WeaponInterface.STAFF, WeaponInterfaces.WeaponInterface.ANCIENT_STAFF);

    public static CombatType getCombatType(Player player) {
        if (mageWeapons.contains(player.getWeapon())) {
            return CombatType.MAGIC;
        } else if (rangedWeapons.contains(player.getWeapon())) {
            return CombatType.RANGED;
        }
        return CombatType.MELEE;
    }
}
