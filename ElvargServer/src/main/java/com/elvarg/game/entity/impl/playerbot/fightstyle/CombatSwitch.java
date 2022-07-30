package com.elvarg.game.entity.impl.playerbot.fightstyle;

import com.elvarg.game.content.PrayerHandler;
import com.elvarg.game.entity.impl.Mobile;
import com.elvarg.game.entity.impl.playerbot.PlayerBot;
import com.elvarg.game.model.ItemInSlot;
import com.elvarg.net.packet.impl.EquipPacketListener;
import com.elvarg.util.timers.TimerKey;

public abstract class CombatSwitch implements CombatAction {

    private final int[] switchItemIds;
    private final PrayerHandler.PrayerData[] prayers;
    private final boolean instant;

    protected CombatSwitch(int[] switchItemIds) {
        this.switchItemIds = switchItemIds;
        this.prayers = new PrayerHandler.PrayerData[] {};
        this.instant = false;
    }

    protected CombatSwitch(int[] switchItemIds, PrayerHandler.PrayerData[] prayerData) {
        this.switchItemIds = switchItemIds;
        this.prayers = prayerData;
        this.instant = false;
    }

    private void doSwitch(PlayerBot playerBot) {
        for (PrayerHandler.PrayerData prayer: prayers) {
            PrayerHandler.activatePrayer(playerBot, prayer);
        }
        for (int itemId: switchItemIds) {
            var item = ItemInSlot.getFromInventory(itemId, playerBot.getInventory());

            if (item == null) {
                continue;
            }
            EquipPacketListener.equipFromInventory(playerBot, item);
        }
    }

    @Override
    public final void perform(PlayerBot playerBot, Mobile enemy) {
        this.doSwitch(playerBot);
        this.performAfterSwitch(playerBot,enemy);
    }

    public abstract void performAfterSwitch(PlayerBot playerBot, Mobile enemy);
}
