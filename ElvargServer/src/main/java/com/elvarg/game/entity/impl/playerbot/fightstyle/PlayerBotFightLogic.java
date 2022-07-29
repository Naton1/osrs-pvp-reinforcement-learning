package com.elvarg.game.entity.impl.playerbot.fightstyle;

public abstract class PlayerBotFightLogic {

    // The weapon the PlayerBot switches back to once none of the weapon switches apply
    public abstract int getMainWeaponId();

    public abstract WeaponSwitch[] getWeaponSwitches();

}
