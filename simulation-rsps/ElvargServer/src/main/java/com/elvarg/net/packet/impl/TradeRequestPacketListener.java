package com.elvarg.net.packet.impl;

import com.elvarg.game.World;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.PlayerStatus;
import com.elvarg.game.task.impl.WalkToTask;
import com.elvarg.net.packet.Packet;
import com.elvarg.net.packet.PacketExecutor;


public class TradeRequestPacketListener implements PacketExecutor {

    @Override
    public void execute(Player player, Packet packet) {
        int index = packet.readLEShort();

        if (index > World.getPlayers().capacity() || index < 0) {
            return;
        }

        Player target = World.getPlayers().get(index);

        if (target == null) {
            return;
        }

        if (!target.getLocation().isWithinDistance(player.getLocation(), 20)) {
            return;
        }

        if (player.getHitpoints() <= 0 || !player.isRegistered()  || target.getHitpoints() <= 0 || !target.isRegistered()) {
            return;
        }

        WalkToTask.submit(player, target, () -> sendRequest(player, target));
    }

    public static void sendRequest(Player player, Player target) {
        if (player.busy()) {
            player.getPacketSender().sendMessage("You cannot do that right now.");
            return;
        }

        if (target.busy()) {
            String msg = "That player is currently busy.";

            if (target.getStatus() == PlayerStatus.TRADING) {
                msg = "That player is currently trading with someone else.";
            }

            player.getPacketSender().sendMessage(msg);
            return;
        }

        if (player.getArea() != null) {
            if (!player.getArea().canTrade(player, target)) {
                player.getPacketSender().sendMessage("You cannot trade here.");
                return;
            }
        }

        if (player.getLocalPlayers().contains(target)) {
            player.getTrading().requestTrade(target);
        }
    }
}
