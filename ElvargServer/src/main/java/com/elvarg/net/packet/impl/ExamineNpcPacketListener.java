package com.elvarg.net.packet.impl;

import com.elvarg.game.definition.NpcDefinition;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.net.packet.Packet;
import com.elvarg.net.packet.PacketExecutor;

public class ExamineNpcPacketListener implements PacketExecutor {

    @Override
    public void execute(Player player, Packet packet) {
        int npcId = packet.readShort();
        
        if (npcId <= 0) {
            return;
        }

        NpcDefinition npcDef = NpcDefinition.forId(npcId);
        if (npcDef != null) {
            player.getPacketSender().sendMessage(npcDef.getExamine());
        }
    }

}
