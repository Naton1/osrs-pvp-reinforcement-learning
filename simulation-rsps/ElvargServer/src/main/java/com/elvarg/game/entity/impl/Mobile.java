package com.elvarg.game.entity.impl;

import com.elvarg.game.content.combat.CombatFactory;
import com.elvarg.game.content.minigames.impl.pestcontrol.PestControl;
import com.elvarg.game.content.sound.Sound;
import com.elvarg.game.collision.RegionManager;
import com.elvarg.game.content.combat.Combat;
import com.elvarg.game.content.combat.CombatType;
import com.elvarg.game.content.combat.hit.HitDamage;
import com.elvarg.game.content.combat.hit.PendingHit;
import com.elvarg.game.entity.Entity;
import com.elvarg.game.entity.impl.npc.NPC;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.entity.impl.playerbot.PlayerBot;
import com.elvarg.game.model.*;
import com.elvarg.game.model.movement.MovementQueue;
import com.elvarg.game.task.Task;
import com.elvarg.game.task.TaskManager;
import com.elvarg.util.Misc;
import com.elvarg.util.Stopwatch;
import com.elvarg.util.timers.TimerRepository;
import com.google.common.collect.Maps;

import java.util.Map;

/**
 * Represents a {@link Player} or {@link NPC}.
 */
public abstract class Mobile extends Entity {
	
    private int index;
    private Location lastKnownRegion;
    private final TimerRepository timers = new TimerRepository();
	private final Combat combat = new Combat(this);
	private final MovementQueue movementQueue = new MovementQueue(this);
	private String forcedChat;
	private Direction walkingDirection = Direction.NONE, runningDirection = Direction.NONE;
	private Stopwatch lastCombat = new Stopwatch();
	private UpdateFlag updateFlag = new UpdateFlag();
	private Location positionToFace;
	private Animation animation;
	private Graphic graphic;
	private Mobile following;

	private Map<Object, Object> attributes = Maps.newConcurrentMap();

	public Object getAttribute(Object name) {
		return attributes.get(name);
	}

	/**
	 * Returns an attribute value, or a default.
	 *
	 * @param name
	 * @param defaultValue
	 * @return
	 */
	public Object getAttribute(Object name, Object defaultValue) {
		Object result = attributes.get(name);
		return result != null ? result : defaultValue;
	}

	public void setAttribute(Object name, Object object) {
		this.attributes.put(name, object);
	}
	/*
	 * Fields
	 */
	private Mobile interactingMobile;
	private Mobile combatFollowing;
	private int npcTransformationId = -1;
	private int poisonDamage;
	private boolean[] prayerActive = new boolean[30], curseActive = new boolean[20];
	private boolean resetMovementQueue;
	private boolean needsPlacement;
	private boolean untargetable;
	private boolean hasVengeance;
	private int specialPercentage = 100;
	private boolean specialActivated;
	private boolean recoveringSpecialAttack;
	private boolean isTeleporting = false;
	private HitDamage primaryHit;
	private HitDamage secondaryHit;
	/**
	 * Is this entity registered.
	 */
	private boolean registered;

	/**
	 * Constructs this character/entity
	 *
	 * @param position
	 */
	public Mobile(Location position) {
		super(position);
	}

	/**
	 * An abstract method used for handling actions once this entity has been added
	 * to the world.
	 */
	public abstract void onAdd();

	/**
	 * An abstract method used for handling actions once this entity has been
	 * removed from the world.
	 */
	public abstract void onRemove();

	public abstract PendingHit manipulateHit(PendingHit hit);

	/**
	 * Teleports the character to a target location
	 *
	 * @param teleportTarget
	 * @return
	 */
	public Mobile moveTo(Location teleportTarget) {
		getMovementQueue().reset();
		setLocation(teleportTarget.clone());
		setNeedsPlacement(true);
		setResetMovementQueue(true);
		setMobileInteraction(null);
		if (this instanceof Player) {
			getMovementQueue().handleRegionChange();
		}
		return this;
	}

	public Mobile smartMove(Location location, int radius) {
		Location chosen = null;
		int requestedX = location.getX();
		int requestedY = location.getY();
		int height = location.getZ();
		while(true) {
			int randomX = Misc.random(requestedX - radius, requestedX + radius);
			int randomY = Misc.random(requestedY - radius, requestedY + radius);
			Location randomLocation = new Location(randomX, randomY, height);
			if (!RegionManager.blocked(randomLocation, null)) {
				chosen = randomLocation;
				break;
			}
		}
		getMovementQueue().reset();
		setLocation(chosen.clone());
		setNeedsPlacement(true);
		setResetMovementQueue(true);
		setMobileInteraction(null);
		if (this instanceof Player) {
			getMovementQueue().handleRegionChange();
		}
		return this;
	}

	public Mobile smartMove(Boundary bounds) {
		Location chosen = null;
		int height = bounds.height;
		while(true) {
			int randomX = Misc.random(bounds.getX(), bounds.getX2());
			int randomY = Misc.random(bounds.getY(), bounds.getY2());
			Location randomLocation = new Location(randomX, randomY, height);
			if (!RegionManager.blocked(randomLocation, null)) {
				chosen = randomLocation;
				break;
			}
		}
		getMovementQueue().reset();
		setLocation(chosen.clone());
		setNeedsPlacement(true);
		setResetMovementQueue(true);
		setMobileInteraction(null);
		if (this instanceof Player) {
			getMovementQueue().handleRegionChange();
		}
		return this;
	}

	/**
	 * Resets all flags related to updating.
	 */
	public void resetUpdating() {
		getUpdateFlag().reset();
		walkingDirection = Direction.NONE;
		runningDirection = Direction.NONE;
		needsPlacement = false;
		resetMovementQueue = false;
		forcedChat = null;
		animation = null;
		graphic = null;
	}

	public Mobile forceChat(String message) {
		setForcedChat(message);
		getUpdateFlag().flag(Flag.FORCED_CHAT);
		return this;
	}

	public Mobile setMobileInteraction(Mobile mobile) {
		this.interactingMobile = mobile;
		getUpdateFlag().flag(Flag.ENTITY_INTERACTION);
		return this;
	}

	@Override
	public void performAnimation(Animation animation) {
		if (this.animation != null && animation != null) {
			if (this.animation.getPriority().ordinal() > animation.getPriority().ordinal()) {
				return;
			}
		}

		this.animation = animation;
		getUpdateFlag().flag(Flag.ANIMATION);
	}

	@Override
	public void performGraphic(Graphic graphic) {
		if (this.graphic != null && graphic != null) {
			if (this.graphic.getPriority().ordinal() > graphic.getPriority().ordinal()) {
				return;
			}
		}

		this.graphic = graphic;
		getUpdateFlag().flag(Flag.GRAPHIC);
	}
	
	public void delayedAnimation(Animation animation, int ticks) {
        TaskManager.submit(new Task(ticks) {
            @Override
            protected void execute() {
                performAnimation(animation);
                stop();
            }
        });
    }
    
    public void delayedGraphic(Graphic graphic, int ticks) {
        TaskManager.submit(new Task(ticks) {
            @Override
            protected void execute() {
                performGraphic(graphic);
                stop();
            }
        });
    }
	
	public Location[] boundaryTiles() {
	    final int size = size();
        Location[] tiles = new Location[size * size];
        int index = 0;
	    for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                tiles[index++] = getLocation().transform(x, y);
            }
	    }
	    return tiles;
	}
	
	public Location[] outterTiles() {
	    final int size = size();
        Location[] tiles = new Location[size * 4];
        int index = 0;
        for (int x = 0; x < size; x++) {
            tiles[index++] = getLocation().transform(x, -1);
            tiles[index++] = getLocation().transform(x, size);
        }
        for (int y = 0; y < size; y++) {            
            tiles[index++] = getLocation().transform(-1, y);
            tiles[index++] = getLocation().transform(size, y);
        }
        return tiles;
	}
	
	public Location[] tiles() {
	    final int size = size();
	    Location[] tiles = new Location[size * size];
	    int index = 0;
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                tiles[index++] = getLocation().transform(x, y);
            }
        }
        return tiles;
	}
	
	public int calculateDistance(Mobile to) {
	    Location[] tiles = this.tiles();
	    Location[] otherTiles = to.tiles();
	    return Location.calculateDistance(tiles, otherTiles);
	}
	
	public boolean useProjectileClipping() {
	    return CombatFactory.getMethod(this) != CombatFactory.MELEE_COMBAT;
	}

	public abstract void appendDeath();

	public abstract void heal(int damage);

	public int getHitpointsAfterPendingDamage() {
		return this.getHitpoints() - getCombat().getHitQueue().getAccumulatedDamage();
	}

	public abstract int getHitpoints();

	public abstract Mobile setHitpoints(int hitpoints);

	public abstract int getBaseAttack(CombatType type);

	public abstract int getBaseDefence(CombatType type);

	public abstract int getBaseAttackSpeed();

	public abstract int getAttackAnim();

	public abstract Sound getAttackSound();

	public abstract int getBlockAnim();

	/*
	 * Getters and setters Also contains methods.
	 */

	public boolean isTeleporting() {
		return isTeleporting;
	}

	public void setTeleporting(boolean isTeleporting) {
		this.isTeleporting = isTeleporting;
	}

	public Graphic getGraphic() {
		return graphic;
	}

	public Animation getAnimation() {
		return animation;
	}

	/**
	 * @return the lastCombat
	 */
	public Stopwatch getLastCombat() {
		return lastCombat;
	}

	public int getPoisonDamage() {
		return poisonDamage;
	}

	public void setPoisonDamage(int poisonDamage) {
		this.poisonDamage = poisonDamage;
	}

	public boolean isPoisoned() {
		return poisonDamage > 0;
	}

	public Location getPositionToFace() {
		return positionToFace;
	}

	public Mobile setPositionToFace(Location positionToFace) {
		this.positionToFace = positionToFace;
		getUpdateFlag().flag(Flag.FACE_POSITION);
		return this;
	}

	public UpdateFlag getUpdateFlag() {
		return updateFlag;
	}

	public MovementQueue getMovementQueue() {
		return movementQueue;
	}

	public Combat getCombat() {
		return combat;
	}

	public Mobile getInteractingMobile() {
		return interactingMobile;
	}

	public void setDirection(Direction direction) {
		setPositionToFace(getLocation().clone().add(direction.getX(), direction.getY()));
	}

	public String getForcedChat() {
		return forcedChat;
	}

	public Mobile setForcedChat(String forcedChat) {
		this.forcedChat = forcedChat;
		return this;
	}

	public boolean[] getPrayerActive() {
		return prayerActive;
	}

	public Mobile setPrayerActive(boolean[] prayerActive) {
		this.prayerActive = prayerActive;
		return this;
	}

	public boolean[] getCurseActive() {
		return curseActive;
	}

	public Mobile setCurseActive(boolean[] curseActive) {
		this.curseActive = curseActive;
		return this;
	}

	public Mobile setPrayerActive(int id, boolean prayerActive) {
		this.prayerActive[id] = prayerActive;
		return this;
	}

	public Mobile setCurseActive(int id, boolean curseActive) {
		this.curseActive[id] = curseActive;
		return this;
	}

	public int getNpcTransformationId() {
		return npcTransformationId;
	}

	public Mobile setNpcTransformationId(int npcTransformationId) {
		this.npcTransformationId = npcTransformationId;
		getUpdateFlag().flag(Flag.APPEARANCE);
		return this;
	}

	public HitDamage decrementHealth(HitDamage hit) {
		if (getHitpoints() <= 0) {
			hit.setDamage(0);
			return hit;
		}
		if (hit.getDamage() > getHitpoints())
			hit.setDamage(getHitpoints());
		if (hit.getDamage() < 0)
			hit.setDamage(0);
		int outcome = getHitpoints() - hit.getDamage();
		if (outcome < 0)
			outcome = 0;
		setHitpoints(outcome);
		return hit;
	}

	/**
	 * Get the primary hit for this entity.
	 *
	 * @return the primaryHit.
	 */
	public HitDamage getPrimaryHit() {
		return primaryHit;
	}

	public void setPrimaryHit(HitDamage hit) {
		this.primaryHit = hit;
	}

	/**
	 * Get the secondary hit for this entity.
	 *
	 * @return the secondaryHit.
	 */
	public HitDamage getSecondaryHit() {
		return secondaryHit;
	}

	public void setSecondaryHit(HitDamage hit) {
		this.secondaryHit = hit;
	}

	/*
	 * Movement queue
	 */

	public Direction getWalkingDirection() {
		return walkingDirection;
	}

	public void setWalkingDirection(Direction walkDirection) {
		this.walkingDirection = walkDirection;
	}

	public Direction getRunningDirection() {
		return runningDirection;
	}

	public void setRunningDirection(Direction runDirection) {
		this.runningDirection = runDirection;
	}

	/**
	 * Determines if this character needs to reset their movement queue.
	 *
	 * @return {@code true} if this character needs to reset their movement queue,
	 *         {@code false} otherwise.
	 */
	public final boolean isResetMovementQueue() {
		return resetMovementQueue;
	}

	/**
	 * Sets the value for resetMovementQueue.
	 *
	 * @param resetMovementQueue
	 *            the new value to set.
	 */
	public final void setResetMovementQueue(boolean resetMovementQueue) {
		this.resetMovementQueue = resetMovementQueue;
	}

	/**
	 * Gets if this entity is registered.
	 *
	 * @return the unregistered.
	 */
	public boolean isRegistered() {
		return registered;
	}

	/**
	 * Sets if this entity is registered,
	 *
	 * @param registered
	 */
	public void setRegistered(boolean registered) {
		this.registered = registered;
	}

	public boolean isNeedsPlacement() {
		return needsPlacement;
	}

	public void setNeedsPlacement(boolean needsPlacement) {
		this.needsPlacement = needsPlacement;
	}

	public boolean hasVengeance() {
		return hasVengeance;
	}

	public void setHasVengeance(boolean hasVengeance) {
		this.hasVengeance = hasVengeance;
	}

	public boolean isSpecialActivated() {
		return specialActivated;
	}

	public void setSpecialActivated(boolean specialActivated) {
		this.specialActivated = specialActivated;
	}

	public int getSpecialPercentage() {
		return specialPercentage;
	}

	public void setSpecialPercentage(int specialPercentage) {
		this.specialPercentage = specialPercentage;
	}

	public void decrementSpecialPercentage(int drainAmount) {
		this.specialPercentage -= drainAmount;

		if (specialPercentage < 0) {
			specialPercentage = 0;
		}
	}

	public void incrementSpecialPercentage(int gainAmount) {
		this.specialPercentage += gainAmount;

		if (specialPercentage > 100) {
			specialPercentage = 100;
		}
	}

	public boolean isRecoveringSpecialAttack() {
		return recoveringSpecialAttack;
	}

	public void setRecoveringSpecialAttack(boolean recoveringSpecialAttack) {
		this.recoveringSpecialAttack = recoveringSpecialAttack;
	}

	public boolean isUntargetable() {
		return untargetable;
	}

	public void setUntargetable(boolean untargetable) {
		this.untargetable = untargetable;
	}

	public boolean inDungeon() {
		return false;
	}

    public Mobile getFollowing() {
        return following;
    }

    public void setFollowing(Mobile following) {
        this.following = following;
    }

	public Mobile getCombatFollowing() {
		return this.combatFollowing;
	}

	public void setCombatFollowing(Mobile target) {
		this.combatFollowing = target;
	}
    
    public int getIndex() {
        return index;
    }

    public Mobile setIndex(int index) {
        this.index = index;
        return this;
    }
    
    public Location getLastKnownRegion() {
        return lastKnownRegion;
    }

    public Mobile setLastKnownRegion(Location lastKnownRegion) {
        this.lastKnownRegion = lastKnownRegion;
        return this;
    }
    
    public TimerRepository getTimers() {
        return timers;
    }
    
    public boolean isPlayer() {
        return (this instanceof Player);
    }

    public boolean isPlayerBot() {
        return (this instanceof PlayerBot);
    }
    
    public boolean isNpc() {
        return (this instanceof NPC);
    }
    
    public Player getAsPlayer() {
        if (!isPlayer()) {
            return null;
        }
        return ((Player) this);
    }

    public PlayerBot getAsPlayerBot() {
        if (!isPlayerBot()) {
            return null;
        }
        return ((PlayerBot) this);
    }
    
    public NPC getAsNpc() {
        if (!isNpc()) {
            return null;
        }
        return ((NPC) this);
    }

	/**
	 * Sends a message (if this character is a human player).
	 *
	 * @param message
	 */
	public void sendMessage(String message) {
		if (!this.isPlayer() || this.isPlayerBot()) {
			return;
		}

		this.getAsPlayer().getPacketSender().sendMessage(message);
	}

	public boolean isImmuneToPoison() {
		if (this.isNpc()) {
			NPC npc = this.getAsNpc();
			if (PestControl.isPortal(npc.getId(), false))
				return true;
		}
		return false;
	}
}