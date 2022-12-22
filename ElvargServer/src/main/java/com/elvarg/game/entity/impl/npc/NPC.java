package com.elvarg.game.entity.impl.npc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.elvarg.game.Sound;
import com.elvarg.game.World;
import com.elvarg.game.content.combat.CombatFactory;
import com.elvarg.game.content.combat.CombatType;
import com.elvarg.game.content.combat.hit.PendingHit;
import com.elvarg.game.content.combat.method.CombatMethod;
import com.elvarg.game.definition.NpcDefinition;
import com.elvarg.game.entity.impl.Mobile;
import com.elvarg.game.entity.impl.npc.NPCMovementCoordinator.CoordinateState;
import com.elvarg.game.entity.impl.npc.impl.Barricades;
import com.elvarg.game.entity.impl.npc.impl.GodwarsFollower;
import com.elvarg.game.entity.impl.npc.impl.Vetion;
import com.elvarg.game.entity.impl.npc.impl.VetionHellhound;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.FacingDirection;
import com.elvarg.game.model.God;
import com.elvarg.game.model.Location;
import com.elvarg.game.model.areas.AreaManager;
import com.elvarg.game.task.TaskManager;
import com.elvarg.game.task.impl.NPCDeathTask;
import com.elvarg.util.NpcIdentifiers;

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
	 * The npc's combat method, used for attacking.
	 */
	private CombatMethod combatMethod;
	/**
	 * Is this {@link NPC} a pet?
	 */
	private boolean pet;

	public int barricadeFireTicks = 8;

	public boolean barricadeOnFire;

	public void handleBarricadeTicks() {
		/** Handles barricades once on fire **/
		if (barricadeOnFire && barricadeFireTicks > 0) {
			barricadeFireTicks--;
			if (barricadeFireTicks == 0) {
				if (this.isBarricade()) {
					Barricades.checkTile(this.getLocation());
				}
				barricadeOnFire = false;
				World.getRemoveNPCQueue().add(this);
			}
		}
	}
	
	/**
	 * Creates a new {@link NPC}.
	 * @param id
	 * @param location
	 * @return
	 */
	public static NPC create(int id, Location location) {
		switch (id) {
		case NpcIdentifiers.VETION:
		case NpcIdentifiers.VETION_REBORN:
			return new Vetion(id, location);
		case NpcIdentifiers.VETION_HELLHOUND:
		case NpcIdentifiers.GREATER_VETION_HELLHOUND:
			return new VetionHellhound(id, location);
		case NpcIdentifiers.KNIGHT_OF_SARADOMIN:
		case NpcIdentifiers.KNIGHT_OF_SARADOMIN_2:
		case NpcIdentifiers.SARADOMIN_PRIEST:
		case NpcIdentifiers.SPIRITUAL_WARRIOR:
		case NpcIdentifiers.SPIRITUAL_RANGER:
		case NpcIdentifiers.SPIRITUAL_MAGE:
			return new GodwarsFollower(id, location, God.SARADOMIN);
		case NpcIdentifiers.AVIANSIE_3:
		case NpcIdentifiers.AVIANSIE_4:
		case NpcIdentifiers.AVIANSIE_6:
		case NpcIdentifiers.AVIANSIE_7:
		case NpcIdentifiers.AVIANSIE_8:
		case NpcIdentifiers.AVIANSIE_9:
		case NpcIdentifiers.AVIANSIE_12:
		case NpcIdentifiers.AVIANSIE_13:
		case NpcIdentifiers.AVIANSIE_14:
		case NpcIdentifiers.SPIRITUAL_WARRIOR_4:
		case NpcIdentifiers.SPIRITUAL_RANGER_4:
		case NpcIdentifiers.SPIRITUAL_MAGE_4:
			return new GodwarsFollower(id, location, God.ARMADYL);
		case NpcIdentifiers.IMP:
		case NpcIdentifiers.ICEFIEND:
		case NpcIdentifiers.PYREFIEND:
		case NpcIdentifiers.GORAK_2:
		case NpcIdentifiers.VAMPIRE:
		case NpcIdentifiers.BLOODVELD_5:
		case NpcIdentifiers.WEREWOLF_21:
		case NpcIdentifiers.WEREWOLF_22:
		case NpcIdentifiers.HELLHOUND_4:
		case NpcIdentifiers.SPIRITUAL_WARRIOR_3:
		case NpcIdentifiers.SPIRITUAL_MAGE_3:
		case NpcIdentifiers.SPIRITUAL_RANGER_3:
			return new GodwarsFollower(id, location, God.ZAMORAK);
		case NpcIdentifiers.GOBLIN_18:
		case NpcIdentifiers.GOBLIN_19:
		case NpcIdentifiers.GOBLIN_20:
		case NpcIdentifiers.GOBLIN_21:			
		case NpcIdentifiers.GOBLIN_22:
		case NpcIdentifiers.HOBGOBLIN_2:
		case NpcIdentifiers.OGRE_5:
		case NpcIdentifiers.JOGRE_2:
		case NpcIdentifiers.CYCLOPS_8:
		case NpcIdentifiers.CYCLOPS_9:
		case NpcIdentifiers.ORK_5:
		case NpcIdentifiers.ORK_6:
		case NpcIdentifiers.ORK_7:
		case NpcIdentifiers.ORK_8:
		case NpcIdentifiers.SPIRITUAL_WARRIOR_2:
		case NpcIdentifiers.SPIRITUAL_RANGER_2:
		case NpcIdentifiers.SPIRITUAL_MAGE_2:
			return new GodwarsFollower(id, location, God.BANDOS);
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

		CombatFactory.assignCombatMethod(this);
	}

	@Override
	public void onAdd() {
	}

	@Override
	public void onRemove() {
	}
	
	public int aggressionDistance() {
	    return CombatFactory.getMethod(this).attackDistance(this);
	}

	/**
	 * Processes this npc.
	 */
	public void process() {
		// Only process the npc if they have properly been added
		// to the game with a definition.
		if (getDefinition() != null) {
			// Timers
			getTimers().process();

			// Handles random walk and retreating from fights
			getMovementQueue().process();
			movementCoordinator.process();

			// Handle combat
			getCombat().process();

			handleBarricadeTicks();

			// Process areas..
			AreaManager.process(this);

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
	}
	
	public List<Player> getNearbyPlayers(int distance) {
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
		return getDefinition() == null ? 1 : getDefinition().getSize();
	}

	@Override
	public int getBaseAttack(CombatType type) {

		if (type == CombatType.RANGED) {
			return getDefinition().getStats()[3];
		} else if (type == CombatType.MAGIC) {
			return getDefinition().getStats()[4];
		}

		return getDefinition().getStats()[1];
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
			base = getDefinition().getStats()[13];
			break;
		case MELEE:
			base = getDefinition().getStats()[10];
			break;
		case RANGED:
			base = getDefinition().getStats()[14];
			break;
		}
		// 10,11,12 = melee
		// 13 = magic
		// 14 = range
		return base;
	}

	@Override
	public int getBaseAttackSpeed() {
		return getDefinition().getAttackSpeed();
	}

	@Override
	public int getAttackAnim() {
		return getDefinition().getAttackAnim();
	}

	@Override
	public Sound getAttackSound() {
		// TODO: need to put proper sounds
		return Sound.IMP_ATTACKING;
	}

	@Override
	public int getBlockAnim() {
		return getDefinition().getDefenceAnim();
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
		return combatMethod;
	}

	public void setCombatMethod(CombatMethod combatMethod) {
		this.combatMethod = combatMethod;
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
}
