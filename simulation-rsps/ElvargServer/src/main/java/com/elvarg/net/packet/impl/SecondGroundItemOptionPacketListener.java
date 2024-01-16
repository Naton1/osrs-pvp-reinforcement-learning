package com.elvarg.net.packet.impl;

import com.elvarg.game.content.skill.skillable.impl.Firemaking;
import com.elvarg.game.content.skill.skillable.impl.Firemaking.LightableLog;
import com.elvarg.game.entity.impl.grounditem.ItemOnGround;
import com.elvarg.game.entity.impl.grounditem.ItemOnGroundManager;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.Location;
import com.elvarg.game.task.impl.WalkToTask;
import com.elvarg.net.packet.Packet;
import com.elvarg.net.packet.PacketExecutor;

import java.util.Optional;

/**
 * This packet is received when a player
 * clicks on the second option on a ground item.
 * An example being the "light" option on logs that
 * are on the ground.
 *
 * @author Professor Oak
 */

public class SecondGroundItemOptionPacketListener implements PacketExecutor {

    @Override
    public void execute(final Player player, Packet packet) {
        final int y = packet.readLEShort();
        final int itemId = packet.readShort();
        final int x = packet.readLEShort();
        final Location position = new Location(x, y, player.getLocation().getZ());

        if (player.getHitpoints() <= 0) {
            return;
        }

        Optional<ItemOnGround> groundItem = ItemOnGroundManager.getGroundItem(Optional.of(player.getUsername()), itemId, position);
        if (!groundItem.isPresent()) {
            return;
        }

        if (player.busy() || !player.getLastItemPickup().elapsed(300)) {
            // If player is busy or last item was picked up less than 0.3 seconds ago
            return;
        }

        WalkToTask.submit(player, groundItem.get(), () -> {
            Optional<ItemOnGround> item = ItemOnGroundManager.getGroundItem(Optional.of(player.getUsername()), itemId, position);
            if (item.isPresent()) {
                Optional<LightableLog> log = LightableLog.getForItem(item.get().getItem().getId());
                log.ifPresent(lightableLog -> player.getSkillManager().startSkillable(new Firemaking(lightableLog, item.get())));
            }
        });
    }
}
