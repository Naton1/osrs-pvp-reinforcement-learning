package com.elvarg.net.packet.impl;

import com.elvarg.game.content.skill.skillable.impl.Firemaking;
import com.elvarg.game.content.skill.skillable.impl.Firemaking.LightableLog;
import com.elvarg.game.entity.impl.grounditem.ItemOnGround;
import com.elvarg.game.entity.impl.grounditem.ItemOnGroundManager;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.Location;
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

        if (player == null || player.getHitpoints() <= 0) {
            return;
        }

        //Stop skilling..
        player.getSkillManager().stopSkillable();

        if (!player.getLastItemPickup().elapsed(300))
            return;
        if (player.busy())
            return;

        if (Math.abs(player.getLocation().getX() - x) > 25 || Math.abs(player.getLocation().getY() - y) > 25) {
            player.getMovementQueue().reset();
            return;
        }
        player.getMovementQueue().walkToGroundItem(position, () -> {
                //Get ground item..
                Optional<ItemOnGround> item = ItemOnGroundManager.getGroundItem(Optional.of(player.getUsername()), itemId, position);
                if (item.isPresent()) {
                    //Handle it..
                    /** FIREMAKING **/
                    Optional<LightableLog> log = LightableLog.getForItem(item.get().getItem().getId());
                    if (log.isPresent()) {
                        player.getSkillManager().startSkillable(new Firemaking(log.get(), item.get()));
                    }
                }
        });
    }
}
