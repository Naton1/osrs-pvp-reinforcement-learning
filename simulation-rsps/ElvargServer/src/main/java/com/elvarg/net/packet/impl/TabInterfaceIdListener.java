package com.elvarg.net.packet.impl;

import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.net.packet.Packet;
import com.elvarg.net.packet.PacketExecutor;

/**
 * @author Ynneh | 09/02/2023 - 10:08
 * <https://github.com/drhenny>
 */
public class TabInterfaceIdListener implements PacketExecutor {

    @Override
    public void execute(Player player, Packet packet) {
        int tabId = packet.readByte();
        /** Sets current tab ID mainly used for music **/
        player.setCurrentInterfaceTab(tabId);


    }
}
