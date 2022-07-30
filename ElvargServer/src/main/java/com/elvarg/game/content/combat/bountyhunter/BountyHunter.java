package com.elvarg.game.content.combat.bountyhunter;

import com.elvarg.game.content.combat.CombatFactory;
import com.elvarg.game.entity.impl.grounditem.ItemOnGroundManager;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.entity.impl.playerbot.PlayerBot;
import com.elvarg.game.model.BrokenItem;
import com.elvarg.game.model.Item;
import com.elvarg.game.model.areas.impl.WildernessArea;
import com.elvarg.util.ItemIdentifiers;
import com.elvarg.util.Misc;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Handles the "Bounty Hunter" minigame. Includes Emblems, Wealthtypes, etc.
 *
 * @author Professor Oak
 */
public class BountyHunter {

	/**
	 * All players currently in the wilderness.
	 */
	public static final List<Player> PLAYERS_IN_WILD = new CopyOnWriteArrayList<>();

	/**
	 * Target pairs.
	 */
	public static final List<TargetPair> TARGET_PAIRS = new CopyOnWriteArrayList<>();
	private static final int TARGET_WEALTH_STRING = 23305;
	private static final int TARGET_NAME_STRING = 23307;
	private static final int TARGET_LEVEL_STRING = 23308;
	/**
	 * The delay between each search for a new target.
	 */
	private static final int TARGET_SEARCH_DELAY_SECONDS = 80;
	/**
	 * The delay for abandoning a target
	 */
	private static final int TARGET_ABANDON_DELAY_SECONDS = 120;

	/**
	 * Processes the bounty hunter system for the specified player.
	 *
	 * @param player
	 */
	public static void process(Player player) {

		// Get our target..
		Optional<Player> target = getTargetFor(player);

		// Is player in the wilderness?
		if (player.getArea() instanceof WildernessArea) {

			// Check if the player has a target.
			// If not, search for a new one.
			if (!target.isPresent()) {

				// Only search for a target every {@code TARGET_DELAY_SECONDS}.
				if (player.getTargetSearchTimer().finished()) {

					// Make sure we're a valid target..
					if (!validTargetContester(player)) {
						return;
					}

					// Search for a new target for the player..
					for (final Player player2 : PLAYERS_IN_WILD) {

						// Check if player2 is a valid target..
						if (validTargetContester(player2)) {

							// Check other stuff...

							// Check if we aren't looping ourselves..
							if (player.equals(player2)) {
								continue;
							}

							// Check that we aren't both bots
							if (player instanceof PlayerBot && player2 instanceof PlayerBot) {
								continue;
							}

							// Check that we haven't killed this player before..
							if (player.getRecentKills().contains(player2.getHostAddress())) {
								continue;
							}

							// Check combat difference!
							int combatDifference = CombatFactory.combatLevelDifference(
									player.getSkillManager().getCombatLevel(),
									player2.getSkillManager().getCombatLevel());
							if (combatDifference < (player.getWildernessLevel() + 5)
									&& combatDifference < (player2.getWildernessLevel() + 5)) {
								assign(player, player2);
								break;
							}
						}
					}
					player.getTargetSearchTimer().start(TARGET_SEARCH_DELAY_SECONDS);
				}
			}
		} else {

			// Player isn't in the Wilderness.
			// Make sure we handle safe timers!
			// If player stays out of wild too long - reset their target
			// in case they have one.
			if (target.isPresent()) {
				final int safeTimer = player.decrementAndGetSafeTimer();

				// Let the player know how much time they have left before losing their target.
				if (safeTimer == 180 || safeTimer == 120 || safeTimer == 60) {
					player.getPacketSender().sendMessage("You have " + safeTimer
							+ " seconds to get back to the wilderness before you lose your target.");
					target.get().getPacketSender()
							.sendMessage("Your target has " + safeTimer
									+ " seconds to get back to the wilderness before they lose you as")
							.sendMessage("target.");
				}

				// Unassign the player if they've been out of wilderness for too long.
				if (safeTimer == 0) {
					unassign(player);

					player.getTargetSearchTimer().start(TARGET_ABANDON_DELAY_SECONDS);
					player.getPacketSender().sendMessage("You have lost your target.");

					target.get().getPacketSender()
							.sendMessage("You have lost your target and will be given a new one shortly.");
					target.get().getTargetSearchTimer().start((TARGET_SEARCH_DELAY_SECONDS / 2));
				}
			}
		}
	}

	/**
	 * Assign a new {@link TargetPair} of the two specified players.
	 *
	 * @param player1
	 * @param player2
	 */
	public static void assign(Player player1, Player player2) {
		if (!getPairFor(player1).isPresent() && !getPairFor(player2).isPresent()) {

			// Create a new pair..
			final TargetPair pair = new TargetPair(player1, player2);

			// Add the pair to our list..
			TARGET_PAIRS.add(pair);

			// Send messages..
			player1.getPacketSender().sendMessage("You've been assigned " + player2.getUsername() + " as your target!");
			player2.getPacketSender().sendMessage("You've been assigned " + player1.getUsername() + " as your target!");

			// Send hints..
			player1.getPacketSender().sendEntityHint(player2);
			player2.getPacketSender().sendEntityHint(player1);

			// Reset safing timers..
			player1.resetSafingTimer();
			player2.resetSafingTimer();

			// Update interfaces..
			updateInterface(player1);
			updateInterface(player2);

			// Handle Player Bot behaviour..
			if (player1 instanceof PlayerBot) {
				((PlayerBot)player1).getCombatInteraction().targetAssigned(player2);
			} else if (player2 instanceof PlayerBot) {
				((PlayerBot)player2).getCombatInteraction().targetAssigned(player1);
			}
		}
	}

	/**
	 * Unassign an existing {@link TargetPair}.
	 *
	 * @param player
	 */
	public static void unassign(Player player) {
		final Optional<TargetPair> pair = getPairFor(player);
		if (pair.isPresent()) {

			TARGET_PAIRS.remove(pair.get());

			final Player p1 = pair.get().getPlayer1();
			final Player p2 = pair.get().getPlayer2();

			// Reset hints..
			p1.getPacketSender().sendEntityHintRemoval(true);
			p2.getPacketSender().sendEntityHintRemoval(true);

			// Update interfaces..
			updateInterface(p1);
			updateInterface(p2);

			// Set timers
			p1.getTargetSearchTimer().start(TARGET_SEARCH_DELAY_SECONDS);
			p2.getTargetSearchTimer().start(TARGET_SEARCH_DELAY_SECONDS);
		}
	}

	/**
	 * Gets the {@link Player} target for the specified player.
	 *
	 * @param player
	 * @return
	 */
	public static Optional<Player> getTargetFor(Player player) {
		Optional<TargetPair> pair = getPairFor(player);
		if (pair.isPresent()) {

			// Check if player 1 in the pair is us.
			// If so, return the other player.
			if (pair.get().getPlayer1().equals(player)) {
				return Optional.of(pair.get().getPlayer2());
			}

			// Check if player 2 in the pair is us.
			// If so, return the other player.
			if (pair.get().getPlayer2().equals(player)) {
				return Optional.of(pair.get().getPlayer1());
			}
		}
		return Optional.empty();
	}

	/**
	 * Gets the {@link TargetPair} for the specfied player.
	 *
	 * @param p
	 * @return
	 */
	public static Optional<TargetPair> getPairFor(final Player p) {
		for (TargetPair pair : TARGET_PAIRS) {
			if (p.equals(pair.getPlayer1()) || p.equals(pair.getPlayer2())) {
				return Optional.of(pair);
			}
		}
		return Optional.empty();
	}

	/**
	 * Handles death for a player. Rewards the killer.
	 *
	 * @param killer
	 * @param killed
	 */
	public static void onDeath(Player killer, Player killed, boolean canGetFullReward, int minBloodMoneyReward) {
		// Cache the killed player's killstreak
		final int enemyKillstreak = killed.getKillstreak();

		// Reset killed player's killstreak
		if (killed.getKillstreak() > 0) {
			killed.getPacketSender().sendMessage("You have lost your " + killed.getKillstreak() + " killstreak.");
		}
		killed.setKillstreak(0);

		// Increment killed player's deaths
		killed.incrementDeaths();

		// Update interfaces for killed player containing the new deaths etc
		killed.getPacketSender().sendString(52031, "@or1@Deaths: " + killed.getDeaths()).sendString(52033,
				"@or1@K/D Ratio: " + killed.getKillDeathRatio());

		// Remove first index if we've killed 1
		if (killer.getRecentKills().size() >= 1) {
			killer.getRecentKills().remove(0);
		}

		// Should the player be rewarded for this kill?
		boolean fullRewardPlayer = canGetFullReward;

		// Check if we recently killed this player
		if (killer.getRecentKills().contains(killed.getHostAddress())
				|| killer.getHostAddress().equals(killed.getHostAddress())) {
			 fullRewardPlayer = false;
		} else {
			killer.getRecentKills().add(killed.getHostAddress());
		}

		Optional<Player> target = getTargetFor(killer);

		// Check if the player killed was our target..
		if (target.isPresent() && target.get().equals(killed)) {

			// Send messages
			killed.getPacketSender().sendMessage("You were defeated by your target!");
			killer.getPacketSender().sendMessage("Congratulations, you managed to defeat your target!");

			// Increment killer's target kills
			killer.incrementTargetKills();

			// Reset targets
			unassign(killer);

			// If player isnt farming kills..
			if (fullRewardPlayer && !(killed instanceof PlayerBot)) {

				// Search for emblem in the player's inventory
				Emblem inventoryEmblem = null;
				for (Emblem e : Emblem.values()) {
					if (killer.getInventory().contains(e.id)) {
						inventoryEmblem = e;
						// Keep looping, find best emblem.
					}
				}

				// This emblem can't be upgraded more..
				if (inventoryEmblem != null) {
					if (inventoryEmblem != Emblem.MYSTERIOUS_EMBLEM_10) {

						// We found an emblem. Upgrade it!
						// Double check that we have it inventory one more time
						if (killer.getInventory().contains(inventoryEmblem.id)) {
							killer.getInventory().delete(inventoryEmblem.id, 1);

							int nextEmblemId = 1;

							// Mysterious emblem tier 1 has a noted version too...
							// So add 2 instead of 1 to skip it.
							if (inventoryEmblem == Emblem.MYSTERIOUS_EMBLEM_1) {
								nextEmblemId = 2;
							}

							// Add the next emblem and notify the player
							killer.getInventory().add(inventoryEmblem.id + nextEmblemId, 1);
							killer.getPacketSender().sendMessage("@red@Your mysterious emblem has been upgraded!");
						}
					} else {
						// This emblem can't be upgraded more..
						killer.getPacketSender().sendMessage(
								"@red@Your mysterious emblem is already tier 10 and cannot be upgraded further.");
					}
				}

				// Randomly drop an emblem (50% chance) when killing a target.
				if (Misc.getRandom(10) <= 5) {
					ItemOnGroundManager.registerNonGlobal(killer, new Item(Emblem.MYSTERIOUS_EMBLEM_1.id),
							killed.getLocation());
					killer.getPacketSender().sendMessage(
							"@red@You have been awarded with a mysterious emblem for successfully killing your target.");
				} else {
					killer.getPacketSender().sendMessage(
							"@red@You did not receive an emblem for this target kill. Better luck next time!");
				}
			}
		} else {
			if (fullRewardPlayer) {
				// Increment regular kills since we didn't kill a target.
				killer.incrementKills();
			}
		}

		var additionalBloodMoneyFromBrokenItems = (BrokenItem.getValueLoseOnDeath(killed)* 3) / 4; // only 75%



		if (fullRewardPlayer) {

			// Increment total kills..
			killer.incrementTotalKills();

			// Increment killstreak..
			killer.incrementKillstreak();

			// Update interfaces
			killer.getPacketSender().sendString(52029, "@or1@Killstreak: " + killer.getKillstreak())
					.sendString(52030, "@or1@Kills: " + killer.getTotalKills())
					.sendString(52033, "@or1@K/D Ratio: " + killer.getKillDeathRatio());

			if (!(killer instanceof PlayerBot)) {
				// Reward player for the kill..
				int rewardAmount = 130 + (100 * enemyKillstreak) + (150 * killer.getKillstreak())
						+ (10 * killer.getWildernessLevel()) + additionalBloodMoneyFromBrokenItems;

				if (killer.getInventory().contains(ItemIdentifiers.BLOOD_MONEY)
						|| killer.getInventory().getFreeSlots() > 0) {
					killer.getInventory().add(ItemIdentifiers.BLOOD_MONEY, rewardAmount);
				} else {
					ItemOnGroundManager.registerNonGlobal(killer, new Item(ItemIdentifiers.BLOOD_MONEY, rewardAmount),
							killed.getLocation());
				}
				killer.getPacketSender().sendMessage("You've received " + rewardAmount + " blood money for that kill!");
			}
			// Check if the killstreak is their highest yet..
			if (killer.getKillstreak() > killer.getHighestKillstreak()) {
				killer.setHighestKillstreak(killer.getKillstreak());
				killer.getPacketSender().sendMessage(
						"Congratulations! Your highest killstreak is now " + killer.getHighestKillstreak() + "");
			} else {
				killer.getPacketSender().sendMessage("Your killstreak is now " + killer.getKillstreak() + ".");
			}

		} else {
			// Reward player for the kill..
			int rewardAmount = minBloodMoneyReward + Misc.getRandom(minBloodMoneyReward) + additionalBloodMoneyFromBrokenItems;

			if (killer.getInventory().contains(ItemIdentifiers.BLOOD_MONEY)
					|| killer.getInventory().getFreeSlots() > 0) {
				killer.getInventory().add(ItemIdentifiers.BLOOD_MONEY, rewardAmount);
			} else {
				ItemOnGroundManager.registerNonGlobal(killer, new Item(ItemIdentifiers.BLOOD_MONEY, rewardAmount),
						killed.getLocation());
			}

			killer.getPacketSender().sendMessage("You've received " + rewardAmount + " blood money for that kill!");
		}

		// Update interfaces
		updateInterface(killer);
		updateInterface(killed);
	}

	/**
	 * Updates the interface for this instance's {@link Player}.
	 */
	public static void updateInterface(Player player) {
		/*
		 * Optional<Player> target = getTargetFor(player);
		 * 
		 * // Check if we have a target... if (target.isPresent()) {
		 * 
		 * // We have a target - send info on interface.. final WealthType type =
		 * WealthType.getWealth(target.get());
		 * 
		 * // Send strings player.getPacketSender().sendString(TARGET_WEALTH_STRING,
		 * "Wealth: " + type.tooltip) .sendString(TARGET_NAME_STRING,
		 * target.get().getUsername()) .sendString(TARGET_LEVEL_STRING, "Combat: " +
		 * target.get().getSkillManager().getCombatLevel());
		 * 
		 * // Send wealth type showWealthType(player, type); } else {
		 * 
		 * // No target - reset target info on interface..
		 * 
		 * // Send strings.. player.getPacketSender().sendString(TARGET_WEALTH_STRING,
		 * "---").sendString(TARGET_NAME_STRING, "None")
		 * .sendString(TARGET_LEVEL_STRING, "Combat: ------");
		 * 
		 * // Send wealth type.. showWealthType(player, WealthType.NO_TARGET); }
		 * 
		 * // Update kda information.. player.getPacketSender().sendString(23323,
		 * "Targets killed: " + player.getTargetKills()) .sendString(23324,
		 * "Players killed: " + player.getNormalKills()) .sendString(23325, "Deaths: " +
		 * player.getDeaths());
		 * 
		 */
	}

	/**
	 * Shows the specified wealth type on the interface whilst removing all others.
	 *
	 * @param type
	 */
	public static void showWealthType(Player player, WealthType type) {
		for (WealthType types : WealthType.values()) {
			int state = 0;
			if (types == type) {
				state = 1;
			}
			player.getPacketSender().sendConfig(types.configId, state);
		}
	}

	/**
	 * Gets the amount of value for a player's emblems.
	 *
	 * @param player
	 * @param performSale
	 * @return
	 */
	public static int getValueForEmblems(Player player, boolean performSale) {
		ArrayList<Emblem> list = new ArrayList<Emblem>();
		for (Emblem emblem : Emblem.values()) {
			if (player.getInventory().contains(emblem.id)) {
				list.add(emblem);
			}
		}

		if (list.isEmpty()) {
			return 0;
		}

		int value = 0;

		for (Emblem emblem : list) {
			int amount = player.getInventory().getAmount(emblem.id);
			if (amount > 0) {

				if (performSale) {
					player.getInventory().delete(emblem.id, amount);
					player.getInventory().add(ItemIdentifiers.BLOOD_MONEY, (emblem.value * amount));
				}

				value += (emblem.value * amount);
			}
		}

		return value;
	}

	/***
	 * Checks if the specified player is in a state of being able to receive/be set
	 * a target.
	 *
	 * @param p
	 * @return
	 */
	private static boolean validTargetContester(Player p) {
		return !(p == null || !p.isRegistered() || !(p.getArea() instanceof WildernessArea)
				|| p.getWildernessLevel() <= 0 || p.isUntargetable() || p.getHitpoints() <= 0 || p.isNeedsPlacement()
				|| getPairFor(p).isPresent());
	}
}
