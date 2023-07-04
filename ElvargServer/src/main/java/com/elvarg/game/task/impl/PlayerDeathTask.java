package com.elvarg.game.task.impl;

import com.elvarg.game.GameConstants;
import com.elvarg.game.content.ItemsKeptOnDeath;
import com.elvarg.game.content.PrayerHandler;
import com.elvarg.game.content.combat.CombatFactory;
import com.elvarg.game.content.combat.bountyhunter.Emblem;
import com.elvarg.game.content.presets.Presetables;
import com.elvarg.game.definition.ItemDefinition;
import com.elvarg.game.entity.impl.grounditem.ItemOnGroundManager;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.entity.impl.playerbot.PlayerBot;
import com.elvarg.game.model.*;
import com.elvarg.game.model.rights.PlayerRights;
import com.elvarg.game.task.Task;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Represents a player's death task, through which the process of dying is
 * handled, the animation, dropping items, etc.
 *
 * @author Professor Oak
 */

public class PlayerDeathTask extends Task {

	/**
	 * The {@link Player} which has died.
	 */
	private final Player player;
	/**
	 * The {@link Player} which killed us.
	 */
	private final Optional<Player> killer;
	/**
	 * Should the player drop their items?
	 */
	private boolean loseItems = true;
	/**
	 * The {@link List} which holds the items the {@link Player} will keep.
	 */
	private Optional<List<Item>> itemsToKeep = Optional.empty();
	/**
	 * The amount of ticks this task will execute.
	 */
	private int ticks = 2;

	/**
	 * The PlayerDeathTask constructor.
	 *
	 * @param player
	 *            The player setting off the task.
	 */
	public PlayerDeathTask(Player player) {
		super(1, player, false);
		this.player = player;
		this.killer = player.getCombat().getKiller(true);
	}

	@Override
	public void execute() {
		if (player == null) {
			stop();
			return;
		}
		try {
			switch (ticks) {
			case 0:
				if (player instanceof PlayerBot) {
					((PlayerBot) player).getCombatInteraction().handleDeath(killer);
				}

				if (player.getArea() != null) {
					loseItems = player.getArea().dropItemsOnDeath(player, killer);
				}

				final List<Item> droppedItems = new ArrayList<Item>();

				// Handle the loss of items..
				if (loseItems) {
					
					// Get items to keep
					itemsToKeep = Optional.of(ItemsKeptOnDeath.getItemsToKeep(player));

					// Fetch player's items
					final List<Item> playerItems = new ArrayList<Item>();
					playerItems.addAll(player.getInventory().getValidItems());
					playerItems.addAll(player.getEquipment().getValidItems());

					// The position the items will be dropped at
					final Location position = player.getLocation();

					// Go through player items, drop them to killer
					boolean dropped = false;

					for (Item item : playerItems) {

						// Keep tradeable items
						if (!item.getDefinition().isTradeable() || itemsToKeep.get().contains(item)) {
							if (!itemsToKeep.get().contains(item)) {
								itemsToKeep.get().add(item);
							}
							continue;
						}

						// Don't drop items if we're owner or dev
						if (player.getRights().equals(PlayerRights.OWNER)
								|| player.getRights().equals(PlayerRights.DEVELOPER)) {
							break;
						}

						// Drop emblems but downgrade them a tier.
						if (Arrays.stream(Emblem.values()).anyMatch(i -> i.id == item.getId())) {

							// Tier 1 shouldnt be dropped cause it cant be downgraded
							if (item.getId() == Emblem.MYSTERIOUS_EMBLEM_1.id) {
								continue;
							}

							if (killer.isPresent()) {
								final int lowerEmblem = item.getId() == Emblem.MYSTERIOUS_EMBLEM_2.id ? item.getId() - 2
										: item.getId() - 1;
								ItemOnGroundManager.registerNonGlobal(killer.get(), new Item(lowerEmblem), position);
								killer.get().getPacketSender().sendMessage("@red@" + player.getUsername()
										+ " dropped a " + ItemDefinition.forId(lowerEmblem).getName() + "!");
								dropped = true;
							}

							continue;
						}

						droppedItems.add(item);

						// Drop item
						ItemOnGroundManager.register(killer.isPresent() ? killer.get() : player, new Item(item.getIdOnDropOrDeath(), item.getAmount()), position);
						dropped = true;
					}

					// Handle defeat..
					if (killer.isPresent()) {
						Player k = killer.get();
						if (k.getArea() != null) {
							k.getArea().defeated(k, player);
						}
						if (!dropped) {
							killer.get().getPacketSender()
									.sendMessage("" + player.getUsername() + " had no valuable items to be dropped.");
						}
					}

					// Reset items
					player.getInventory().resetItems().refreshItems();
					player.getEquipment().resetItems().refreshItems();
				}

				// Restore the player's default attributes (such as stats)..
				player.resetAttributes();

				// If the player lost items..
				if (loseItems) {
					// Handle items kept on death..
					if (itemsToKeep.isPresent()) {
						for (Item it : itemsToKeep.get()) {
							int id = it.getId();

							// Handle item breaking..
							BrokenItem brokenItem = BrokenItem.get(id);
							if (brokenItem != null) {
								id = brokenItem.getBrokenItem();
								player.getPacketSender()
										.sendMessage("Your " + ItemDefinition.forId(it.getId()).getName()
												+ " has been broken. You can fix it by talking to Perdu.");
							}

							player.getInventory().add(new Item(id, it.getAmount()));
						}
						itemsToKeep.get().clear();
					}
				}

				boolean handledDeath = false;

				if (player.getArea() != null) {
					handledDeath = player.getArea().handleDeath(player, killer);
				}

				if (!handledDeath) {
					player.moveTo(GameConstants.DEFAULT_LOCATION);
					if (loseItems) {
						if (player.isOpenPresetsOnDeath()) {
							Presetables.open(player);
						}
					}
				}

				// Stop the event..
				stop();
				break;

				case 2:
					if (player instanceof PlayerBot) {
						((PlayerBot) player).getCombatInteraction().handleDying(this.killer);
					}

					// Reset combat..
					player.getCombat().reset();

					// Reset movement queue and disable it..
					player.getMovementQueue().setBlockMovement(true).reset();

					// Mark us as untargetable..
					player.setUntargetable(true);

					// Close all open interfaces..
					player.getPacketSender().sendInterfaceRemoval();

					// Send death message..
					player.getPacketSender().sendMessage("Oh dear, you are dead!");

					// Perform death animation..
					player.performAnimation(new Animation(836, Priority.HIGH));

					// Handle retribution prayer effect on our killer, if present..
					if (PrayerHandler.isActivated(player, PrayerHandler.RETRIBUTION)) {
						if (killer.isPresent()) {
							CombatFactory.handleRetribution(player, killer.get());
						}
					}
					break;
			}
			ticks--;
		} catch (Exception e) {
			super.stop();
			e.printStackTrace();
			player.resetAttributes();
			player.moveTo(GameConstants.DEFAULT_LOCATION);
		}
	}
}
