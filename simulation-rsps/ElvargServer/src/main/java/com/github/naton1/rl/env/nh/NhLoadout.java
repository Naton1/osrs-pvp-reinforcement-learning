package com.github.naton1.rl.env.nh;

import com.elvarg.game.content.PrayerHandler;
import com.elvarg.game.model.MagicSpellbook;
import com.github.naton1.rl.env.Loadout;

public interface NhLoadout extends Loadout {

    int[] getRangedGear();

    int[] getMageGear();

    int[] getMeleeGear();

    int[] getMeleeSpecGear();

    // Tank gear must be a subset of other gear
    int[] getTankGear();

    PrayerHandler.PrayerData[] getRangedPrayers();

    PrayerHandler.PrayerData[] getMagePrayers();

    PrayerHandler.PrayerData[] getMeleePrayers();

    NhLoadout randomize(long seed);

    default NhEnvironmentParams.FightType getFightType() {
        return NhEnvironmentParams.FightType.NORMAL;
    }

    @Override
    default MagicSpellbook getMagicSpellbook() {
        return MagicSpellbook.ANCIENT;
    }
}
