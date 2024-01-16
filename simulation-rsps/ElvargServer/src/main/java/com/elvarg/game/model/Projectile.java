package com.elvarg.game.model;

import com.elvarg.game.World;
import com.elvarg.game.entity.impl.Mobile;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.areas.impl.PrivateArea;

/**
 * Class representing a Projectile. These can be sent between locations or from a mobile entity to another mobile entity or location.
 * <br>
 * There is an internal builder {@link ProjectileBuilder} that contains preset default variables that are most commonly used.
 * <br>
 * Otherwise the constructor {@link Projectile#Projectile(int, int, int, int, int)} can be called.
 * <br>
 * The generated projectile object can then be sent using the methods 
 * {@link Projectile#sendProjectile(Location, Location, Projectile)}, <br>
 * {@link Projectile#sendProjectile(Mobile, Location, Projectile)},<br>
 * {@link Projectile#sendProjectile(Mobile, Mobile, Projectile)}<br>
 * <br>
 * @author Advocatus | https://www.rune-server.ee/members/119929-advocatus/
 *
 */
public class Projectile {

	public Projectile(int projectileId, int startHeight, int endHeight, int delay, int speed) {
		this.projectileId = projectileId;
		this.startHeight = startHeight;
		this.endHeight = endHeight;
		this.delay = delay;
		this.speed = speed;
		this.angle = 16;
		this.distanceOffset = 64;
		this.duration = -1;
		this.span = -1;
	}
	
	public int getProjectileId() {
		return projectileId;
	}
	
	/**
	 * Sends a projectile from the given mobile entity's location to the location of the specified mobile entity with the given Projectile.
	 * 
	 * @param source the mobile entity sending the projectile
	 * @param victim the mobile entity receiving the projectile
	 * @param p the projectile being sent
	 */
	public static void sendProjectile(Mobile source, Mobile victim, Projectile p) {
		final Location start = source.getLocation();
		final Location end = victim.getLocation();
		Projectile.sendProjectile(start, end, victim, p, source.getPrivateArea());
	}

	/**
	 * Sends a projectile from the given start location to the specified end location with the given Projectile.
	 * 
	 * @param start the location where the projectile should start
	 * @param end the location where the projectile should end
	 * @param p the projectile being sent
	 */
	public static void sendProjectile(Location start, Location end, Projectile p) {
		Projectile.sendProjectile(start, end, null, p, null);
	}

	/**
	 * Sends a projectile from the given mobile entity's location to the specified end location with the given Projectile.
	 * 
	 * @param source the mobile entity sending the projectile
	 * @param end the location where the projectile should end
	 * @param p the projectile being sent
	 */
	public static void sendProjectile(Mobile source, Location end, Projectile p) {
		Projectile.sendProjectile(source.getLocation(), end, null, p, source.getPrivateArea());
	}

	/**
	 * Internal method to send a projectile to all nearby players.
	 */
	private static void sendProjectile(Location start, Location end, Mobile lockon, Projectile p,
			PrivateArea privateArea) {
		int speed = p.getSpeed(start, end);
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
			player.getPacketSender().sendProjectile(start, end, 0, speed, p.projectileId, p.startHeight, p.endHeight, lockon, p.delay, p.angle, p.distanceOffset);
		}
	}
	
	private final int projectileId;
	private final int startHeight;
	private final int endHeight;
	private final int delay;
	private final int speed;
	private final int distanceOffset;
	private final int angle;
	private final int duration;
	private final int span;
	
	/**
	 * Private constructor used by {@link ProjectileBuilder}
	 */
	private Projectile(final int projectileId, final int startHeight, final int endHeight, final int delay,
			final int angle, final int distanceOffset, final int duration, final int span) {
		this.projectileId = projectileId;
		this.startHeight = startHeight;
		this.endHeight = endHeight;
		this.delay = delay;
		this.speed = Integer.MIN_VALUE;
		this.angle = angle;
		this.distanceOffset = distanceOffset;
		this.duration = duration;
		this.span = span;
	}
	
	private int getSpeed(Location source, Location dest) {
		if(speed != Integer.MIN_VALUE)
			return speed;
		return delay + duration + (source.getDistance(dest) * span);
	}
	
	/**
	 * Builder class for projectiles. The default values are the most commonly used ones in spells.
	 * 
	 * @author Advocatus | https://www.rune-server.ee/members/119929-advocatus/
	 *
	 */
	public static class ProjectileBuilder {
		private int id = -1;
		private int start = 43;
		private int end = 31;
		private int delay = 51;
		private int angle = 16;
		private int duration = -5;
		private int distanceOffset = 64;
		private int span = 10;

		public ProjectileBuilder setId(int id) {
			this.id = id;
			return this;
		}

		public ProjectileBuilder setStart(int startHeight) {
			this.start = startHeight;
			return this;
		}

		public ProjectileBuilder setEnd(int endHeight) {
			this.end = endHeight;
			return this;
		}

		public ProjectileBuilder setDelay(int delay) {
			this.delay = delay;
			return this;
		}

		public ProjectileBuilder setAngle(int angle) {
			this.angle = angle;
			return this;
		}

		public ProjectileBuilder setDuration(int duration) {
			this.duration = duration;
			return this;
		}

		public ProjectileBuilder setDistanceOffset(int distanceOffset) {
			this.distanceOffset = distanceOffset;
			return this;
		}

		public ProjectileBuilder setSpan(int span) {
			this.span = span;
			return this;
		}

		public Projectile create() {
			return new Projectile(id, start, end, delay, angle, distanceOffset, duration, span);
		}
	}
}