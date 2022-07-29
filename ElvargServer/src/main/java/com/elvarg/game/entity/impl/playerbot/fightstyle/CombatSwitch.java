package com.elvarg.game.entity.impl.playerbot.fightstyle;

import com.elvarg.game.entity.impl.Mobile;
import com.elvarg.game.entity.impl.playerbot.PlayerBot;
import com.elvarg.game.model.ItemInSlot;
import com.elvarg.net.packet.impl.EquipPacketListener;

public abstract class CombatSwitch implements CombatAction {

    private final int[] switchItemIds;

    protected CombatSwitch(int[] switchItemIds) {
        this.switchItemIds = switchItemIds;
    }

    public void switchGear(PlayerBot playerBot) {
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
        this.switchGear(playerBot);
        this.performAfterSwitch(playerBot,enemy);
    }

    public abstract void performAfterSwitch(PlayerBot playerBot, Mobile enemy);
}
