package com.elvarg.game.entity.impl.grounditem;

import java.util.*;

import com.elvarg.game.World;
import com.elvarg.game.entity.impl.grounditem.ItemOnGround.State;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.Item;
import com.elvarg.game.model.Location;
import com.elvarg.game.task.TaskManager;
import com.elvarg.game.task.impl.GroundItemRespawnTask;

/**
 * Manages all {@link ItemOnGround}s.
 *
 * @author Professor Oak
 */
public class ItemOnGroundManager {

	/**
	 * The delay between every {@link ItemOnGround} state update.
	 */
	public static final int STATE_UPDATE_DELAY = 50; // 3 minutes = 300, 2 minutes = 200

	/**
	 * Handles what happens when a player enters a new region. We need to send all
	 * the items related to that region.
	 *
	 * @param player
	 *            The player whose changing region.
	 */
	public static void onRegionChange(Player player) {
		Iterator<ItemOnGround> iterator = World.getItems().iterator();
		for (; iterator.hasNext();) {
			ItemOnGround item = iterator.next();
			perform(player, item, OperationType.CREATE);
		}
	}

	/**
	 * Processes all active {@link ItemOnGround}.
	 */
	public static void process() {
		Iterator<ItemOnGround> iterator = World.getItems().iterator();
		while (iterator.hasNext()) {
			ItemOnGround i = iterator.next();

			// Process item..
			i.process();

			// Check if the item needs to be removed..
			if (i.isPendingRemoval()) {

				// If it respawns, make sure we fire off a respawn task before
				// we remove it..
				if (i.respawns()) {
					TaskManager.submit(new GroundItemRespawnTask(i, i.getRespawnTimer()));
				}

				// Remove!
				iterator.remove();
			}
		}
	}

	/**
	 * Perform an operation on a ground item.
	 *
	 * @param item
	 * @param type
	 */
	public static void perform(ItemOnGround item, OperationType type) {
		switch (item.getState()) {
		case SEEN_BY_PLAYER:
			if (item.getOwner().isPresent()) {
				Optional<Player> owner = World.getPlayerByName(item.getOwner().get());
				owner.ifPresent(o -> perform(o, item, type));
			}
			break;
		case SEEN_BY_EVERYONE:
			for (Player player : World.getPlayers()) {
				if (player == null)
					continue;
				perform(player, item, type);
			}
			break;
		default:
			break;
		}
	}

	/**
	 * Perform an operation on a ground item for a specific player.
	 *
	 * @param item
	 * @param type
	 */
	public static void perform(Player player, ItemOnGround item, OperationType type) {
		if (item.isPendingRemoval()) {
			type = OperationType.DELETE;
		}
		if (item.getLocation().getZ() != player.getLocation().getZ())
			return;
		if (player.getPrivateArea() != item.getPrivateArea()) {
            return;
        }
		if (item.getLocation().getDistance(player.getLocation()) > 64)
			return;
		switch (type) {
		case ALTER:
			player.getPacketSender().alterGroundItem(item);
			break;
		case DELETE:
			player.getPacketSender().deleteGroundItem(item);
			break;
		case CREATE:
			if (!isOwner(player.getUsername(), item)) {
				if (item.getState() == State.SEEN_BY_PLAYER)
					return;
				if (!item.getItem().getDefinition().isTradeable() || !item.getItem().getDefinition().isDropable())
					return;
			}
			player.getPacketSender().createGroundItem(item);
			break;
		default:
			throw new UnsupportedOperationException(
					"Unsupported operation (" + type.toString() + ")  on: " + item.toString());
		}
	}

	/**
	 * Registers the given {@link ItemOnGround} to the world.
	 *
	 * @param item
	 */
	public static void register(ItemOnGround item) {
		// Check for merge with existing stackables..
		if (item.getItem().getDefinition().isStackable()) {
			if (merge(item)) {
				return;
			}
		}

		// We didn't need to modify a previous item.
		// Simply register the given item to the world..
		World.getItems().add(item);
		ItemOnGroundManager.perform(item, OperationType.CREATE);
	}

	/**
	 * Attempts to merge an item with one that already exists in the same position.
	 * <p>
	 * This is mostly used for stackable ground items.
	 *
	 * @param item
	 * @return
	 */
	public static boolean merge(ItemOnGround item) {
		Iterator<ItemOnGround> iterator = World.getItems().iterator();
		for (; iterator.hasNext();) {
			ItemOnGround item_ = iterator.next();
			if (item_ == null || item_.isPendingRemoval() || item_.equals(item)) {
				continue;
			}
			if (!item_.getLocation().equals(item.getLocation())) {
				continue;
			}

			// Check if the ground item is private...
			// If we aren't the owner, we shouldn't modify it.
			if (item_.getState() == State.SEEN_BY_PLAYER) {
				boolean flag = true;
				if (item_.getOwner().isPresent() && item.getOwner().isPresent()) {
					if (item_.getOwner().get().equals(item.getOwner().get())) {
						flag = false;
					}
				}
				if (flag) {
					continue;
				}
			}

			// Modify the existing item.
			if (item_.getItem().getId() == item.getItem().getId()) {
				int oldAmount = item_.getItem().getAmount();
				item_.getItem().incrementAmountBy(item.getItem().getAmount());
				item_.setOldAmount(oldAmount);
				item_.setTick(0);
				ItemOnGroundManager.perform(item_, OperationType.ALTER);
				return true;
			}
		}
		return false;
	}

	/**
	 * Deregisters the given {@link ItemOnGround} from the world by flagging it as
	 * deleted. The iterator in {@link GroundItemSequenceTask} will pick this up and
	 * remove it.
	 *
	 * @param item
	 */
	public static void deregister(ItemOnGround item) {
		item.setPendingRemoval(true);
		ItemOnGroundManager.perform(item, OperationType.DELETE);
	}

	/**
	 * A utility method which quickly registers a default {@link ItemOnGround} which
	 * goes global once the item's counter hits {@code STATE_UPDATE_DELAY}.
	 *
	 * @param player
	 * @param item
	 * @return
	 */
	public static ItemOnGround register(Player player, Item item) {
		return register(player, item, player.getLocation().clone());
	}

	/**
	 * A utility method which quickly registers a default {@link ItemOnGround} which
	 * goes global once the item's counter hits {@code STATE_UPDATE_DELAY}.
	 *
	 * @param player
	 * @param item
	 * @param position
	 * @return
	 */
    public static ItemOnGround register(Player player, Item item, Location position) {
        ItemOnGround i = new ItemOnGround(State.SEEN_BY_PLAYER, Optional.of(player.getUsername()), position, item, true,
                -1, player.getPrivateArea());
		register(i);
		return i;
	}

	/**
	 * A utility method which quickly registers a default {@link ItemOnGround} which
	 * does not go global once the item's counter hits {@code STATE_UPDATE_DELAY}.
	 *
	 * @param player
	 * @param item
	 * @param position
	 * @return
	 */
	public static void registerNonGlobal(Player player, Item item) {
		registerNonGlobal(player, item, player.getLocation().clone());
	}

	/**
	 * A utility method which quickly registers a default {@link ItemOnGround} which
	 * does not go global once the item's counter hits {@code STATE_UPDATE_DELAY}.
	 *
	 * @param player
	 * @param item
	 * @param position
	 */
	public static void registerNonGlobal(Player player, Item item, Location position) {
		register(new ItemOnGround(State.SEEN_BY_PLAYER, Optional.of(player.getUsername()), position, item, false, -1, player.getPrivateArea()));
	}
	
	/**
	 * A utility method which quickly registers a default {@link ItemOnGround} which
	 * is global.
	 * 
	 * @param player
	 * @param item
	 */
	public static void registerGlobal(Player player, Item item) {
		register(new ItemOnGround(State.SEEN_BY_EVERYONE, Optional.of(player.getUsername()), player.getLocation().clone(), item, false, -1, player.getPrivateArea()));
	}

	/**
	 * A utility method used to find a {@link ItemOnGround} with the specified
	 * {@link Location}.
	 *
	 * @param player
	 * @param id
	 * @param x
	 * @param y
	 * @return
	 */
	public static Optional<ItemOnGround> getGroundItem(Optional<String> owner, int id, Location position) {
		Iterator<ItemOnGround> iterator = World.getItems().iterator();
		for (; iterator.hasNext();) {
			ItemOnGround item = iterator.next();
			if (item == null || item.isPendingRemoval()) {
				continue;
			}
			if (item.getState() == State.SEEN_BY_PLAYER) {
				if (!owner.isPresent() || !isOwner(owner.get(), item)) {
					continue;
				}
			}
			if (id != item.getItem().getId()) {
				continue;
			}
			if (!item.getLocation().equals(position)) {
				continue;
			}
			return Optional.of(item);
		}
		return Optional.empty();
	}

	/**
	 * Checks if this gound item exists.
	 *
	 * @param i
	 * @return
	 */
	public static boolean exists(ItemOnGround i) {
		return getGroundItem(i.getOwner(), i.getItem().getId(), i.getLocation()).isPresent();
	}

	/**
	 * A utitily method used to check if the given {@link Player} is the owner of
	 * the given {@link ItemOnGround}.
	 *
	 * @param player
	 * @param i
	 * @return
	 */
	public static boolean isOwner(String player, ItemOnGround i) {
		if (i.getOwner().isPresent()) {
			return i.getOwner().get().equals(player);
		}
		return false;
	}

	/**
	 * Represents the different types of packet-operations related to ground items
	 * that are currently supported.
	 *
	 * @author Professor Oak
	 */
	public enum OperationType {
		CREATE, DELETE, ALTER;
	}

}