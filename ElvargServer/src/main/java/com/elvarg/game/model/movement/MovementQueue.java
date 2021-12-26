package com.elvarg.game.model.movement;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import com.elvarg.game.World;
import com.elvarg.game.collision.RegionManager;
import com.elvarg.game.content.combat.CombatFactory;
import com.elvarg.game.content.combat.method.CombatMethod;
import com.elvarg.game.entity.impl.Mobile;
import com.elvarg.game.entity.impl.npc.NPC;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.Direction;
import com.elvarg.game.model.Location;
import com.elvarg.game.model.Skill;
import com.elvarg.game.model.movement.path.RS317PathFinder;
import com.elvarg.util.Misc;
import com.elvarg.util.NpcIdentifiers;
import com.elvarg.util.timers.TimerKey;

/**
 * A queue of {@link Direction}s which a {@link Mobile} will follow.
 *
 * @author Graham Edgecombe
 */
public final class MovementQueue {

	/**
	 * The maximum size of the queue. If any additional steps are added, they are
	 * discarded.
	 */
	private static final int MAXIMUM_SIZE = 100;

	/**
	 * The character whose walking queue this is.
	 */
	private final Mobile character;

	/**
	 * The queue of directions.
	 */
	private final Deque<Point> points = new ArrayDeque<Point>();

	/**
	 * The current {@link MovementStatus}.
	 */
	private boolean blockMovement = false;

	/**
	 * Are we currently moving?
	 */
	private boolean isMoving = false;

	/**
	 * Creates a walking queue for the specified character.
	 *
	 * @param character
	 *            The character.
	 */
	public MovementQueue(Mobile character) {
		this.character = character;
	}

	/**
	 * Checks if we can walk from one position to another.
	 * 
	 * @param from
	 * @param to
	 * @param size
	 * @return
	 */
	public boolean canWalk(int deltaX, int deltaY) {
        if (!canMove()) {
            return false;
        }
        if (character.getLocation().getZ() == -1) {
            return true;
        }
        return RegionManager.canMove(character.getLocation(), character.getLocation().transform(deltaX, deltaY), character.size(), character.size(), character.getPrivateArea());
    }

	/**
	 * Steps away from a Gamecharacter
	 *
	 * @param character
	 *            The gamecharacter to step away from
	 */
	public static void clippedStep(Mobile character) {
	    int size = character.size();
		if (character.getMovementQueue().canWalk(-size, 0))
			character.getMovementQueue().walkStep(-size, 0);
		else if (character.getMovementQueue().canWalk(size, 0))
			character.getMovementQueue().walkStep(size, 0);
		else if (character.getMovementQueue().canWalk(0, -size))
			character.getMovementQueue().walkStep(0, -size);
		else if (character.getMovementQueue().canWalk(0, size))
			character.getMovementQueue().walkStep(0, size);
	}

	/**
	 * Adds the first step to the queue, attempting to connect the server and client
	 * position by looking at the previous queue.
	 *
	 * @param clientConnectionPosition
	 *            The first step.
	 * @return {@code true} if the queues could be connected correctly,
	 *         {@code false} if not.
	 */
	public boolean addFirstStep(Location clientConnectionPosition) {
		reset();
		addStep(clientConnectionPosition);
		return true;
	}

	/**
	 * Adds a step to walk to the queue.
	 *
	 * @param x
	 *            X to walk to
	 * @param y
	 *            Y to walk to
	 * @param clipped
	 *            Can the step walk through objects?
	 */
	public void walkStep(int x, int y) {
		Location position = character.getLocation().clone();
		position.setX(position.getX() + x);
		position.setY(position.getY() + y);
		addStep(position);
	}

	/**
	 * Adds a step.
	 *
	 * @param x
	 *            The x coordinate of this step.
	 * @param y
	 *            The y coordinate of this step.
	 * @param heightLevel
	 * @param flag
	 */
	private void addStep(int x, int y, int heightLevel) {
		if (!canMove()) {
			return;
		}

		if (points.size() >= MAXIMUM_SIZE)
			return;

		final Point last = getLast();
		final int deltaX = x - last.position.getX();
		final int deltaY = y - last.position.getY();
		final Direction direction = Direction.fromDeltas(deltaX, deltaY);
		if (direction != Direction.NONE)
			points.add(new Point(new Location(x, y, heightLevel), direction));
	}

	/**
	 * Adds a step to the queue.
	 *
	 * @param step
	 *            The step to add.
	 * @oaram flag
	 */
	public void addStep(Location step) {
		if (!canMove()) {
			return;
		}

		final Point last = getLast();
		final int x = step.getX();
		final int y = step.getY();
		int deltaX = x - last.position.getX();
		int deltaY = y - last.position.getY();
		final int max = Math.max(Math.abs(deltaX), Math.abs(deltaY));
		for (int i = 0; i < max; i++) {
			if (deltaX < 0)
				deltaX++;
			else if (deltaX > 0)
				deltaX--;
			if (deltaY < 0)
				deltaY++;
			else if (deltaY > 0)
				deltaY--;
			addStep(x - deltaX, y - deltaY, step.getZ());
		}
	}

	public boolean canMove() {
		if (character.isNeedsPlacement()) {
			return false;
		}
		if (character.getTimers().has(TimerKey.FREEZE) || character.getTimers().has(TimerKey.STUN) || blockMovement) {
			return false;
		}
		return true;
	}

	/**
	 * Gets the last point.
	 *
	 * @return The last point.
	 */
	private Point getLast() {
		final Point last = points.peekLast();
		if (last == null)
			return new Point(character.getLocation(), Direction.NONE);
		return last;
	}

	/**
	 * @return true if the character is moving.
	 */
	public boolean isMoving() {
		return isMoving; // !points.isEmpty();
	}

	/**
	 * Processes the movement queue.
	 * 
	 * Polls through the queue of steps and handles them.
	 * 
	 */
	public void process() {
			
		// Make sure movement isnt restricted..
		if (!canMove()) {
			reset();
			return;
		}

        if (character.getFollowing() != null) {
            processFollowing();
        }
		
		// Poll through the actual movement queue and
		// begin moving.
		Point walkPoint = null;
		Point runPoint = null;

		walkPoint = points.poll();

		if (isRunToggled()) {
			runPoint = points.poll();
		}

		Location oldPosition = character.getLocation();
		boolean moved = false;

        if (walkPoint != null && walkPoint.direction != Direction.NONE) {
            Location next = walkPoint.position;
            if (canWalkTo(next)) {
                character.setLocation(next);
                character.setWalkingDirection(walkPoint.direction);
                moved = true;
            } else {
                reset();
                return;
            }
        }

        if (runPoint != null && runPoint.direction != Direction.NONE) {
            Location next = runPoint.position;
            if (canWalkTo(next)) {
                oldPosition = next;
                character.setLocation(next);
                character.setRunningDirection(runPoint.direction);
                moved = true;
            } else {
                reset();
                return;
            }
        }

		// Handle movement-related events such as
		// region change and energy drainage.
		if (character.isPlayer()) {
			if (moved) {
				handleRegionChange();
				drainRunEnergy();
				character.getAsPlayer().setOldPosition(oldPosition);
			}
		}

		isMoving = moved;
	}

    public boolean canWalkTo(Location next) {
        if (character.isNpc() && !((NPC)character).canWalkThroughNPCs()) {
            for (NPC npc : World.getNpcs()) {
                if (npc == null) {
                    continue;
                }
                if (npc.getLocation().equals(next)) {
                    return false;
                }
            }
        }
        return true;
    }

	public void handleRegionChange() {
		Player player = ((Player) character);
		final int diffX = character.getLocation().getX() - character.getLastKnownRegion().getRegionX() * 8;
		final int diffY = character.getLocation().getY() - character.getLastKnownRegion().getRegionY() * 8;
		boolean regionChanged = false;
		if (diffX < 16)
			regionChanged = true;
		else if (diffX >= 88)
			regionChanged = true;
		if (diffY < 16)
			regionChanged = true;
		else if (diffY >= 88)
			regionChanged = true;
		if (regionChanged || player.getRegionHeight() != player.getLocation().getZ()) {
			player.getPacketSender().sendMapRegion();
			player.setRegionHeight(player.getLocation().getZ());
		}
	}

	private void drainRunEnergy() {
		Player player = ((Player) character);
		if (player.isRunning()) {
			player.setRunEnergy(player.getRunEnergy() - 1);
			if (player.getRunEnergy() <= 0) {
				player.setRunEnergy(0);
				player.setRunning(false);
				player.getPacketSender().sendRunStatus();
			}
			player.getPacketSender().sendRunEnergy();
		}
	}

	/**
	 * The rate of which we restore one run energy point
	 * 
	 * @return
	 */
	public static int runEnergyRestoreDelay(Player p) {
		int delay = 1700 - (p.getSkillManager().getCurrentLevel(Skill.AGILITY) * 10);
		// if (isResting()) {
		// delay *= 0.45;
		// }
		return delay;
	}

	/**
	 * Stops the movement.
	 */
	public MovementQueue reset() {
		points.clear();
		isMoving = false;
		return this;
	}
	
	/**
	 * Processes following.
	 */
    public void processFollowing() {
        final Mobile following = character.getFollowing();
        final int size = character.size();
        final int followingSize = following.size();
        
        // Update interaction
        character.setMobileInteraction(following);
        character.getMovementQueue().reset();

        // Block if our movement is locked.
        if (!canMove()) {
            return;
        }

        boolean combatFollow = character.getCombat().getTarget() == following;
        final CombatMethod method = CombatFactory.getMethod(character);
        
        if (combatFollow && CombatFactory.canReach(character, method, following)) {
            reset();
            return;
        }

        // If we're way too far away from eachother, simply reset following completely.
        if (!character.getLocation().isViewableFrom(following.getLocation()) || !following.isRegistered() || following.getPrivateArea() != character.getPrivateArea()) {

            boolean reset = true;

            // Handle pets, they should teleport to their owner
            // when they're too far away.
            if (character.isNpc()) {
                NPC npc = character.getAsNpc();
                if (npc.isPet()) {
                    npc.setVisible(false);                    
                    List<Location> tiles = new ArrayList<>();
                    for (Location tile : following.outterTiles()) {
                        if (RegionManager.blocked(tile, following.getPrivateArea())) {
                            continue;
                        }
                        tiles.add(tile);
                    }
                    if (!tiles.isEmpty()) {
                        npc.moveTo(tiles.get(Misc.getRandom(tiles.size() - 1)));
                        npc.setVisible(true);
                        npc.setArea(following.getArea());
                    }
                    return;
                }

                switch (npc.getId()) {
                case NpcIdentifiers.TZTOK_JAD:
                    reset = false;
                    break;
                }
            }

            if (reset) {
                if (character.isPlayer() && following.isPlayer()) {
                    character.getAsPlayer().getPacketSender().sendMessage("Unable to find " + following.getAsPlayer().getUsername() + ".");
                }
                if (combatFollow) {
                    character.getCombat().reset();
                }
                character.getMovementQueue().reset();
                character.setFollowing(null);
                character.setMobileInteraction(null);
                return;
            }
        }

        final boolean dancing = (!combatFollow && character.isPlayer() && following.isPlayer() && following.getFollowing() == character);
        final boolean basicPathing = (combatFollow && character.isNpc() && !((NPC)character).canUsePathFinding());
        final Location current = character.getLocation();
        Location destination = following.getLocation();        
        if (dancing) {
            destination = following.getAsPlayer().getOldPosition();
        }
        
        if (!dancing) {
            if (!combatFollow && character.calculateDistance(following) == 1 && !RS317PathFinder.isInDiagonalBlock(current, destination)) {
                return;
            }
            
            // Handle simple walking to the destination for NPCs which don't use pathfinding.
            if (basicPathing) {
                
                // Same spot, step away.
                if (destination.equals(current) && !following.getMovementQueue().isMoving()
                        && character.size() == 1 && following.size() == 1) {
                    List<Location> tiles = new ArrayList<>();
                    for (Location tile : following.outterTiles()) {
                        if (!RegionManager.canMove(character.getLocation(), tile, size, size, character.getPrivateArea())
                                || RegionManager.blocked(tile, character.getPrivateArea())) {
                            continue;
                        }
                        // Projectile attack
                        if (character.useProjectileClipping() && !RegionManager.canProjectileAttack(tile, following.getLocation(), character.size(), character.getPrivateArea())) {
                            continue;
                        }
                        tiles.add(tile);
                    }
                    if (!tiles.isEmpty()) {
                        addFirstStep(tiles.get(Misc.getRandom(tiles.size() - 1)));
                    }
                    return;
                }
                
                int deltaX = destination.getX() - current.getX();
                int deltaY = destination.getY() - current.getY();
                if (deltaX < -1) {
                    deltaX = -1;
                } else if (deltaX > 1) {
                    deltaX = 1;
                }
                if (deltaY < -1) {
                    deltaY = -1;
                } else if (deltaY > 1) {
                    deltaY = 1;
                }
                Direction direction = Direction.fromDeltas(deltaX, deltaY);
                
                switch (direction) {
                case NORTH_WEST:
                    if (RegionManager.canMove(current, Direction.WEST, size, character.getPrivateArea())) {
                        direction = Direction.WEST;
                    } else if (RegionManager.canMove(current, Direction.NORTH, size, character.getPrivateArea())) {
                        direction = Direction.NORTH;
                    } else {
                        direction = Direction.NONE;
                    }
                    break;
                case NORTH_EAST:
                    if (RegionManager.canMove(current, Direction.NORTH, size, character.getPrivateArea())) {
                        direction = Direction.NORTH;
                    } else if (RegionManager.canMove(current, Direction.EAST, size, character.getPrivateArea())) {
                        direction = Direction.EAST;
                    } else {
                        direction = Direction.NONE;
                    }
                    break;
                case SOUTH_WEST:
                    if (RegionManager.canMove(current, Direction.WEST, size, character.getPrivateArea())) {
                        direction = Direction.WEST;
                    } else if (RegionManager.canMove(current, Direction.SOUTH, size, character.getPrivateArea())) {
                        direction = Direction.SOUTH;
                    } else {
                        direction = Direction.NONE;
                    }
                    break;
                case SOUTH_EAST:
                    if (RegionManager.canMove(current, Direction.EAST, size, character.getPrivateArea())) {
                        direction = Direction.EAST;
                    } else if (RegionManager.canMove(current, Direction.SOUTH, size, character.getPrivateArea())) {
                        direction = Direction.SOUTH;
                    } else {
                        direction = Direction.NONE;
                    }
                    break;
                default:
                    break;                    
                }
                
                if (direction == Direction.NONE) {
                    return;
                }
                
                Location next = current.transform(direction.getX(), direction.getY());
                if (RegionManager.canMove(current, next, size, size, character.getPrivateArea())) {
                    addStep(next);
                }
                return;
            }
                        
            // Find the nearest tile surrounding the target..
            List<Location> tiles = new ArrayList<>();
            for (Location tile : following.outterTiles()) {
                if (!RegionManager.canMove(character.getLocation(), tile, size, size, character.getPrivateArea())
                        || RegionManager.blocked(tile, character.getPrivateArea())) {
                    continue;
                }
                // Projectile attack
                if (character.useProjectileClipping() && !RegionManager.canProjectileAttack(tile, following.getLocation(), size, character.getPrivateArea())) {
                    continue;
                }
                tiles.add(tile);
            }
            if (!tiles.isEmpty()) {
                tiles.sort((l1, l2) -> {
                    int distance1 = (int) l1.getDistance(current);
                    int distance2 = (int) l2.getDistance(current);
                    int delta = (distance1 - distance2);

                    // Make sure we don't pick a diagonal tile if we're a small entity and have to
                    // attack closely (melee).
                    if (distance1 == distance2 && size == 1 && followingSize == 1) {
                        if (l1.isPerpendicularTo(current)) {
                            return -1;
                        } else if (l2.isPerpendicularTo(current)) {
                            return 1;
                        }
                    }

                    return delta;
                });
                destination = tiles.get(0);
            }
        }
        
        RS317PathFinder.findPath(character, destination.getX(), destination.getY(), character.isPlayer(), 1, 1);
    }
    
	/**
	 * Gets the size of the queue.
	 *
	 * @return The size of the queue.
	 */
	public int size() {
		return points.size();
	}

	public boolean isRunToggled() {
		return character.isPlayer() && ((Player) character).isRunning();
	}

	public MovementQueue setBlockMovement(boolean blockMovement) {
		this.blockMovement = blockMovement;
		return this;
	}

	public boolean isMovementBlocked() {
		return blockMovement;
	}

	/**
	 * Represents a single point in the queue.
	 *
	 * @author Graham Edgecombe
	 */
	public static final class Point {

		private final Location position;
		private final Direction direction;

		public Point(Location position, Direction direction) {
			this.position = position;
			this.direction = direction;
		}

		@Override
		public String toString() {
			return Point.class.getName() + " [direction=" + direction + ", position=" + position + "]";
		}

	}
}