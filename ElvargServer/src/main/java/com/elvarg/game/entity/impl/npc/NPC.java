package com.elvarg.game.entity.impl.npc;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.elvarg.game.content.sound.Sound;
import com.elvarg.game.World;
import com.elvarg.game.content.combat.CombatFactory;
import com.elvarg.game.content.combat.CombatType;
import com.elvarg.game.content.combat.hit.PendingHit;
import com.elvarg.game.content.combat.method.CombatMethod;
import com.elvarg.game.definition.NpcDefinition;
import com.elvarg.game.entity.impl.Mobile;
import com.elvarg.game.entity.impl.npc.NPCMovementCoordinator.CoordinateState;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.FacingDirection;
import com.elvarg.game.model.Ids;
import com.elvarg.game.model.Location;
import com.elvarg.game.model.areas.AreaManager;
import com.elvarg.game.model.areas.impl.WildernessArea;
import com.elvarg.game.task.TaskManager;
import com.elvarg.game.task.impl.NPCDeathTask;
import org.apache.commons.lang3.tuple.ImmutablePair;

import static com.elvarg.game.content.combat.CombatFactory.MELEE_COMBAT;
import static com.elvarg.game.model.movement.MovementQueue.NPC_INTERACT_RADIUS;

/**
 * Represents a non-playable character, which players can interact with.
 *
 * @author Professor Oak
 */

public class NPC extends Mobile {

	/**
	 * The npc's id.
	 */
	private final int id;
	/**
	 * The npc's movement coordinator. Handles random walking.
	 */
	private NPCMovementCoordinator movementCoordinator = new NPCMovementCoordinator(this);
	/**
	 * The npc's current hitpoints.
	 */
	private int hitpoints;
	/**
	 * The npc's spawn position (default).
	 */
	private Location spawnPosition;
	/**
	 * The npc's head icon.
	 */
	private int headIcon = -1;
	/**
	 * The npc's current state. Is it dying?
	 */
	private boolean isDying;
	/**
	 * For PestControl
	 */
	public boolean needRespawn;
	public static int maxNPCs = 5000;
	public static int maxListedNPCs = 5000;
	public static NPC npcs[] = new NPC[maxNPCs];
	/**
	 * The npc's owner.
	 * <p>
	 * The only player who can see right-click actions on the npc.
	 */
	private Player owner;
	/**
	 * The npc's current state. Is it visible?
	 */
	private boolean visible = true;
	/**
	 * The npc's facing.
	 */
	private FacingDirection face = FacingDirection.NORTH;
	/**
	 * Is this {@link NPC} a pet?
	 */
	private boolean pet;

	/**
	 * A map of npc IDs <-> NPC implementations
	 */
	private static Map<Integer, Class<?>> NPC_IMPLEMENTATION_MAP;
	
	/**
	 * Creates a new {@link NPC}.
	 * @param id
	 * @param location
	 * @return
	 */
	public static NPC create(int id, Location location) {
		Class<?> implementationClass = NPC_IMPLEMENTATION_MAP.get(id);
		if (implementationClass != null) {
			// If this NPC has been implemented by its own class, instantiate that first
			try {
				return (NPC) implementationClass.getDeclaredConstructor(int.class, Location.class).newInstance(id, location);
			} catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
				e.printStackTrace();
			}
		}

		return new NPC(id, location);
	}
	
	/**
	 * Can this npc walk through other NPCs?
	 * @return
	 */
    public boolean canWalkThroughNPCs() {
        if (pet) {
            return true;
        }
	    return false;
	}
	
    /**
     * Can this npc use pathfinding when following its target?
     * 
     * @return
     */
    public boolean canUsePathFinding() {
	    return false;
	}

	/**
	 * Constructs a new npc.
	 *
	 * @param id
	 *            The npc id.
	 * @param position
	 *            The npc spawn (default) position.
	 */
	public NPC(int id, Location position) {
		super(position);
		this.id = id;
		this.spawnPosition = position;

		if (getDefinition() == null) {
			setHitpoints(10);
		} else {
			setHitpoints(getDefinition().getHitpoints());
		}
	}

	@Override
	public void onAdd() {
	}

	@Override
	public void onRemove() {
	}

	/**
	 * Can this NPC be aggressive towards a given player?
	 *
	 * @param player
	 * @return
	 */
	public boolean isAggressiveTo(Player player) {
		// NPCs are generally aggressive towards players under twice their combat level.
		return player.getSkillManager().getCombatLevel() <= (this.getCurrentDefinition().getCombatLevel() * 2)
				// Or players in the wilderness.
				|| player.getArea() instanceof WildernessArea;
	}

	public int aggressionDistance() {
		int attackDistance = CombatFactory.getMethod(this).attackDistance(this);

		// Ensure NPCs are aggressive from at least 3 tiles away by default
	    return Math.max(attackDistance, 3);
	}

	/**
	 * Processes this npc.
	 */
	public void process() {
		if (getDefinition() == null) {
			// Only process the npc if they have properly been added with a definition.
			return;
		}

		// Timers
		getTimers().process();

		// Handles random walk and retreating from fights
		getMovementQueue().process();
		movementCoordinator.process();

		if (getInteractingMobile() != null && this.getLocation().getDistance(getInteractingMobile().getLocation()) > NPC_INTERACT_RADIUS) {
			// Reset interacting entity if more than radius away
			setMobileInteraction(null);
			setPositionToFace(null);
		}

		// Handle combat
		getCombat().process();

		// Process areas..
		AreaManager.process(this);

		if (getCombatMethod() != null) {
			getCombatMethod().onTick(this, this.getCombat().getTarget());
		}

		// Regenerating health if needed, but only after 20 seconds of last attack.
		if (getCombat().getLastAttack().elapsed(20000)
				|| movementCoordinator.getCoordinateState() == CoordinateState.RETREATING) {

			// We've been damaged.
			// Regenerate health.
			if (getDefinition().getHitpoints() > hitpoints) {
				setHitpoints(hitpoints + (int) (getDefinition().getHitpoints() * 0.1));
				if (hitpoints > getDefinition().getHitpoints()) {
					setHitpoints(getDefinition().getHitpoints());
				}
			}
		}
	}
	
	public List<Player> getPlayersWithinDistance(int distance) {
		List<Player> list = new ArrayList<>();
		for (Player player : World.getPlayers()) {
			if (player == null) {
				continue;
			}
			if (player.getPrivateArea() != getPrivateArea()) {
			    continue;
			}
			if (player.getLocation().getDistance(this.getLocation()) <= distance) {
				list.add(player);
			}
		}
		return list;
	}

	@Override
	public void appendDeath() {
		if (!isDying) {
			TaskManager.submit(new NPCDeathTask(this));
			isDying = true;
		}
	}

	@Override
	public int getHitpoints() {
		return hitpoints;
	}

	@Override
	public NPC setHitpoints(int hitpoints) {
		this.hitpoints = hitpoints;
		if (this.hitpoints <= 0)
			appendDeath();
		return this;
	}

	@Override
	public void heal(int heal) {
		if ((this.hitpoints + heal) > getDefinition().getHitpoints()) {
			setHitpoints(getDefinition().getHitpoints());
			return;
		}
		setHitpoints(this.hitpoints + heal);
	}

	@Override
	public boolean isNpc() {
		return true;
	}

	@Override
	public boolean equals(Object other) {
		return other instanceof NPC && ((NPC) other).getIndex() == getIndex() && ((NPC) other).getId() == getId();
	}

	@Override
	public int size() {
		return getCurrentDefinition() == null ? 1 : getCurrentDefinition().getSize();
	}

	@Override
	public int getBaseAttack(CombatType type) {

		if (type == CombatType.RANGED) {
			return getCurrentDefinition().getStats()[3];
		} else if (type == CombatType.MAGIC) {
			return getCurrentDefinition().getStats()[4];
		}

		return getCurrentDefinition().getStats()[1];
		// 0 = attack
		// 1 = strength
		// 2 = defence
		// 3 = range
		// 4 = magic
	}

	@Override
	public int getBaseDefence(CombatType type) {
		int base = 0;
		switch (type) {
		case MAGIC:
			base = getCurrentDefinition().getStats()[13];
			break;
		case MELEE:
			base = getCurrentDefinition().getStats()[10];
			break;
		case RANGED:
			base = getCurrentDefinition().getStats()[14];
			break;
		}
		// 10,11,12 = melee
		// 13 = magic
		// 14 = range
		return base;
	}

	@Override
	public int getBaseAttackSpeed() {
		return getCurrentDefinition().getAttackSpeed();
	}

	@Override
	public int getAttackAnim() {
		return getCurrentDefinition().getAttackAnim();
	}

	@Override
	public Sound getAttackSound() {
		// TODO: need to put proper sounds
		return Sound.IMP_ATTACKING;
	}

	@Override
	public int getBlockAnim() {
		return getCurrentDefinition().getDefenceAnim();
	}

	/*
	 * Getters and setters
	 */

	public int getId() {
		if (getNpcTransformationId() != -1) {
			return getNpcTransformationId();
		}
		return id;
	}

	public int getRealId() {
		return id;
	}

	public boolean isVisible() {
		return visible;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	public boolean isDying() {
		return isDying;
	}

	public void setDying(boolean isDying) {
		this.isDying = isDying;
	}

	public Player getOwner() {
		return owner;
	}

	public NPC setOwner(Player owner) {
		this.owner = owner;
		return this;
	}

	public NPCMovementCoordinator getMovementCoordinator() {
		return movementCoordinator;
	}

	/**
	 * Gets the current Definition, subject to current NPC transformation.
	 *
	 * @return
	 */
	public NpcDefinition getCurrentDefinition() {
		if (getNpcTransformationId() != -1) {
			return NpcDefinition.forId(getNpcTransformationId());
		}

		return getDefinition();
	}

	/**
	 * Gets the base definition for this NPC, regardless of NPC transformation etc.
	 *
	 * @return
	 */
	public NpcDefinition getDefinition() {
		return NpcDefinition.forId(id);
	}

	public boolean isBarricade() {
		return Arrays.asList(5722, 5723, 5724, 5725).stream().anyMatch(n -> this.getId() == n.intValue());
	}

	public Location getSpawnPosition() {
		return spawnPosition;
	}

	public int getHeadIcon() {
		return headIcon;
	}

	public void setHeadIcon(int headIcon) {
		this.headIcon = headIcon;
		// getUpdateFlag().flag(Flag.NPC_APPEARANCE);
	}

	public CombatMethod getCombatMethod() {
		// By default, NPCs use Melee combat.
		// This can be overridden by creating a class in entity.impl.npc.impl
		return MELEE_COMBAT;
	}

	@Override
	public NPC clone() {
		return create(getId(), getSpawnPosition());
	}

	public FacingDirection getFace() {
		return face;
	}

	public void setFace(FacingDirection face) {
		this.face = face;
	}

	public boolean isPet() {
		return pet;
	}

	public void setPet(boolean pet) {
		this.pet = pet;
	}

	@Override
	public PendingHit manipulateHit(PendingHit hit) {
		return hit;
	}

	/**
	 * Initialises all the NPC implementation classes.
	 *
	 * @param implementationClasses
	 */
	public static void initImplementations(List<? extends Class<?>> implementationClasses) {
		// Add all the implemented NPCs to NPC_IMPLEMENTATION_MAP
		NPC_IMPLEMENTATION_MAP = implementationClasses.stream()
				.flatMap(clazz -> {
					return Arrays.stream(clazz.getAnnotation(Ids.class).value())
							.mapToObj(id -> new ImmutablePair<>(id, clazz));

				}).collect(Collectors.toMap(ImmutablePair::getLeft, ImmutablePair::getRight));
	}
}
