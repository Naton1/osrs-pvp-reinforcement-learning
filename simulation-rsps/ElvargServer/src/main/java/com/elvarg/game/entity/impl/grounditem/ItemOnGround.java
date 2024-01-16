package com.elvarg.game.entity.impl.grounditem;

import java.util.Optional;

import com.elvarg.game.World;
import com.elvarg.game.entity.Entity;
import com.elvarg.game.entity.impl.grounditem.ItemOnGroundManager.OperationType;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.Animation;
import com.elvarg.game.model.Graphic;
import com.elvarg.game.model.Item;
import com.elvarg.game.model.Location;
import com.elvarg.game.model.areas.impl.PrivateArea;

/**
 * Represents an item on the ground.
 *
 * @author Professor Oak
 */
public class ItemOnGround extends Entity {
    
    /**
     * The current {@link State} of this {@link ItemOnGround}.
     */
    private State state = State.SEEN_BY_PLAYER;
    
    /**
     * The owner of this {@link ItemOnGround}.
     */
    private Optional<String> owner = Optional.empty();
    
    /**
     * The {@Item} of this {@link ItemOnGround}.
     */
    private Item item;
    
    /**
     * Does this {@link ItemOnGround} go global?
     */
    private boolean goesGlobal;
    
    /**
     * A tick counter, used for processing.
     */
    private int tick;
    
    /**
     * Is this item in the process of being deleted?
     */
    private boolean pendingRemoval;
    
    /**
     * The amount of cycles it takes for this
     * {@link ItemOnGround} to respawn.
     */
    private int respawnTimer = -1;
    
    /**
     * The old amount of the item, used when
     * modifying its amounts.
     */
    private int oldAmount;
    
    /**
     * Represents the {@link PrivateArea} this {@link ItemOnGround} is in.
     */
    private final PrivateArea privateArea;

    /**
     * Constructs a new {@link ItemOnGround}.
     *
     * @param state
     * @param owner
     * @param location
     * @param item
     * @param goesGlobal
     * @param respawnTimer
     */
    public ItemOnGround(State state, Optional<String> owner, Location location, Item item, boolean goesGlobal, int respawnTimer, PrivateArea area) {
        super(location);
        this.state = state;
        this.owner = owner;
        this.item = item;
        this.goesGlobal = goesGlobal;
        this.respawnTimer = respawnTimer;
        this.privateArea = area;
    }

    /**
     * Processes the ground item
     * depending on its current state.
     */
    public void process() {
        incrementTick();
        switch (state) {
            case SEEN_BY_EVERYONE:
            case SEEN_BY_PLAYER:
                //If an update is required..
                if (getTick() >= ItemOnGroundManager.STATE_UPDATE_DELAY) {
                    setTick(0);

                    //Check if item is currently private and needs to go global..
                    if (state == State.SEEN_BY_PLAYER && goesGlobal()) {

                        //We make the item despawn for the owner..
                        if (getOwner().isPresent()) {
                            Optional<Player> o = World.getPlayerByName(getOwner().get());
                            if (o.isPresent()) {
                                ItemOnGroundManager.perform(o.get(), this, OperationType.DELETE);
                            }
                        }

                        //Check if we need to merge this ground item..
                        //This basically puts together two stackables
                        //that are on the same tile.
                        if (getItem().getDefinition().isStackable()) {
                            if (ItemOnGroundManager.merge(this)) {
                                setPendingRemoval(true);
                                return;
                            }
                        }

                        //Spawn the item globally..
                        setState(State.SEEN_BY_EVERYONE);
                        ItemOnGroundManager.perform(this, OperationType.CREATE);
                        return;
                    }

                    //Item needs to be deleted.
                    //However, there's no point in deleting items that will just respawn..
                    if (!respawns()) {
                        ItemOnGroundManager.deregister(this);
                    }
                }
                break;
            default:
                break;
        }
    }

    public Optional<String> getOwner() {
        return owner;
    }

    public Item getItem() {
        return item;
    }

    public int getTick() {
        return tick;
    }

    public ItemOnGround setTick(int tick) {
        this.tick = tick;
        return this;
    }

    public void incrementTick() {
        this.tick++;
    }

    public boolean goesGlobal() {
        return goesGlobal;
    }

    public State getState() {
        return state;
    }

    public ItemOnGround setState(State state) {
        this.state = state;
        return this;
    }

    public int getRespawnTimer() {
        return respawnTimer;
    }

    public boolean respawns() {
        return respawnTimer > 0;
    }

    @Override
    public void performAnimation(Animation animation) {
    }

    @Override
    public void performGraphic(Graphic graphic) {
    }

    @Override
    public int size() {
        return 1;
    }

    public PrivateArea getPrivateArea() {
        return privateArea;
    }

    public int getOldAmount() {
        return oldAmount;
    }

    public void setOldAmount(int oldAmount) {
        this.oldAmount = oldAmount;
    }

    public boolean isPendingRemoval() {
        return pendingRemoval;
    }

    public void setPendingRemoval(boolean pendingRemoval) {
        this.pendingRemoval = pendingRemoval;
    }

    @Override
    public ItemOnGround clone() {
        return new ItemOnGround(state, owner, getLocation(), item, goesGlobal, respawnTimer, privateArea);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ItemOnGround))
            return false;
        ItemOnGround item = (ItemOnGround) o;
        if (item.getOwner().isPresent()
                && this.getOwner().isPresent()) {
            if (!item.getOwner().get().equals(this.getOwner().get())) {
                return false;
            }
        }
        return item.getItem().equals(this.getItem())
                && item.getLocation().equals(this.getLocation())
                && item.getState() == this.getState()
                && item.getTick() == this.getTick()
                && item.getPrivateArea() == this.getPrivateArea();
    }

    @Override
    public String toString() {
        return "GroundItem, id: " + item.getId() + ", amount: " + item.getAmount() + ", current state: " + state.toString() + ", goesGlobal: " + goesGlobal + ", tick: " + tick + ", respawns: " + respawns();
    }

    /**
     * All the possible states a {@link ItemOnGround} can have.
     */
    public enum State {
        SEEN_BY_PLAYER, SEEN_BY_EVERYONE;
    }
}
