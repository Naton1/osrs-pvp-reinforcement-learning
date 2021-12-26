package com.elvarg.game.model;

import com.elvarg.game.World;
import com.elvarg.game.entity.impl.Mobile;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.areas.impl.PrivateArea;

public final class Projectile {

    private final Location start;
    private final Location end;
    private final int speed;
    private final int projectileId;
    private final int startHeight;
    private final int endHeight;
    private final Mobile lockon;
    private final int delay;
    private final PrivateArea privateArea;

    public Projectile(Location start, Location end, Mobile lockon, int projectileId, int delay, int speed,
            int startHeight, int endHeight, PrivateArea privateArea) {
        this.start = start;
        this.lockon = lockon;
        this.end = end;
        this.projectileId = projectileId;
        this.delay = delay;
        this.speed = speed;
        this.startHeight = startHeight;
        this.endHeight = endHeight;
        this.privateArea = privateArea;
    }

    public Projectile(Mobile source, Mobile victim, int projectileId, int delay, int speed, int startHeight,
            int endHeight) {
        this(source.getLocation(), victim.getLocation(), victim, projectileId, delay, speed, startHeight, endHeight, source.getPrivateArea());
    }

    /**
     * Sends one projectiles using the values set when the {@link Projectile} was
     * constructed.
     */
    public void sendProjectile() {
        for (Player player : World.getPlayers()) {
            if (player == null) {
                continue;
            }
            if (player.getPrivateArea() != privateArea) {
                continue;
            }
            if (!start.isViewableFrom(player.getLocation())) {
                continue;
            }
            player.getPacketSender().sendProjectile(start, end, 0, speed, projectileId, startHeight, endHeight, lockon, delay);
        }
    }
}
