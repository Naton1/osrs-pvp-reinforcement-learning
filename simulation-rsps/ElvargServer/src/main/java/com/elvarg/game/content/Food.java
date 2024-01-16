package com.elvarg.game.content;

import java.util.HashMap;
import java.util.Map;

import com.elvarg.game.content.sound.Sound;
import com.elvarg.game.content.sound.SoundManager;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.Animation;
import com.elvarg.game.model.Item;
import com.elvarg.game.model.Priority;
import com.elvarg.game.model.Skill;
import com.elvarg.util.timers.TimerKey;

/**
 * Represents food which can be consumed by the player to restore hitpoints.
 *
 * @author Professor Oak
 */

public class Food {

	/**
	 * The {@link Animation} that will be played when consuming food.
	 */
	private static final Animation ANIMATION = new Animation(829, Priority.HIGH);

	/**
	 * Handles the player eating said food type.
	 *
	 * @param player
	 *            The player eating the consumable.
	 * @param item
	 *            The food item being consumed.
	 * @param slot
	 *            The slot of the food being eaten.
	 */
	public static boolean consume(Player player, int item, int slot) {
		Edible food = Edible.types.get(item);
		// Check if {@code item} is a valid food type..
		if (food == null) {
			return false;
		}

		if (player.getArea() != null) {
			if (!player.getArea().canEat(player, item)) {
				player.getPacketSender().sendMessage("You cannot eat here.");
				return true;
			}
		}

		// Check if we're currently able to eat..
		if (player.getTimers().has(TimerKey.STUN)) {
			player.getPacketSender().sendMessage("You're currently stunned!");
			return true;
		}

		if (food == Edible.KARAMBWAN) {
			if (player.getTimers().has(TimerKey.KARAMBWAN))
				return true;
		} else {
			if (player.getTimers().has(TimerKey.FOOD)) {
				return true;
			}
		}

		player.getTimers().extendOrRegister(TimerKey.FOOD, 3);

		final int combatTicks = player.getTimers().getUncappedTicks(TimerKey.COMBAT_ATTACK, Integer.MIN_VALUE);
		final int addAttackDelay;
		if (food == Edible.KARAMBWAN) {
			player.getTimers().register(TimerKey.KARAMBWAN, 3); // Register karambwan timer too
			player.getTimers().register(TimerKey.POTION, 3); // Register the potion timer (karambwan blocks pots)
			addAttackDelay = 2;
		}
		else {
			addAttackDelay = 3;
		}

		// Attack delays are special in that the 'timer' can go negative, and the delay is added to the timer
		final int combatTicksAfterEat = combatTicks + addAttackDelay;
		if (combatTicksAfterEat > 0) {
			player.getTimers().register(TimerKey.COMBAT_ATTACK, combatTicksAfterEat);
		}

		// Close interfaces..
		player.getPacketSender().sendInterfaceRemoval();

		// Stop skilling..
		player.getSkillManager().stopSkillable();

		// Send sound..
		SoundManager.sendSound(player, Sound.FOOD_EAT);

		// Start animation..
		player.performAnimation(ANIMATION);

		// Delete food from inventory..
		player.getInventory().delete(food.item, slot);

		// Heal the player..
		int currentHp = player.getSkillManager().getCurrentLevel(Skill.HITPOINTS);
		int maxHp = player.getSkillManager().getMaxLevel(Skill.HITPOINTS);
		int healAmount = food.heal;
		
		if (food == Edible.ANGLERFISH) {
			int c = 2;
			if (maxHp >= 25) {
				c = 4;
			}
			if (maxHp >= 50) {
				c = 6;
			}
			if (maxHp >= 75) {
				c = 8;
			}
			if (maxHp >= 93) {
				c = 13;
			}
			healAmount = (int) Math.floor((maxHp / 10) + c);
			if (healAmount > 22) {
				healAmount = 22;
			}
			maxHp += healAmount;
		}
		
		if (healAmount + currentHp > maxHp) {
			healAmount = maxHp - currentHp;
		}
		if (healAmount < 0) {
			healAmount = 0;
		}
		
		player.setHitpoints(player.getHitpoints() + healAmount);
		
		// Send message to player..
		String e = food == Edible.BANDAGES ? "use" : "eat";
		player.getPacketSender().sendMessage("You " + e + " the " + food.name + ".");

		// Handle cake slices..
		if (food == Edible.CAKE || food == Edible.SECOND_CAKE_SLICE) {
			player.getInventory().add(new Item(food.item.getId() + 2, 1));
		}
		return true;
	}

	/**
	 * Represents all types of food currently available.
	 *
	 * @author relex lawl
	 */
	public enum Edible {
		/*
		 * Fish food types players can get by fishing or purchasing from other entities.
		 */
		KEBAB(new Item(1971), 4), CHEESE(new Item(1985), 4), CAKE(new Item(1891), 5), SECOND_CAKE_SLICE(new Item(1893),
				5), THIRD_CAKE_SLICE(new Item(1895), 5), BANDAGES(new Item(14640), 12), JANGERBERRIES(new Item(247),
						2), WORM_CRUNCHIES(new Item(2205), 7), EDIBLE_SEAWEED(new Item(403), 4), ANCHOVIES(
								new Item(319),
								1), SHRIMPS(new Item(315), 3), SARDINE(new Item(325), 4), COD(new Item(339), 7), TROUT(
										new Item(333),
										7), PIKE(new Item(351), 8), SALMON(new Item(329), 9), TUNA(new Item(361),
												10), LOBSTER(new Item(379), 12), BASS(new Item(365), 13), SWORDFISH(
														new Item(373),
														14), MEAT_PIZZA(new Item(2293), 14), MONKFISH(new Item(7946),
																16), SHARK(new Item(385), 20), SEA_TURTLE(new Item(397),
																		21), DARK_CRAB(new Item(11936), 22), MANTA_RAY(new Item(391), 22), KARAMBWAN(
																				new Item(3144),
																				18), ANGLERFISH(new Item(13441), 22),
		/*
		 * Baked goods food types a player can make with the cooking skill.
		 */
		POTATO(new Item(1942), 1), BAKED_POTATO(new Item(6701), 4), POTATO_WITH_BUTTER(new Item(6703),
				14), CHILLI_POTATO(new Item(7054), 14), EGG_POTATO(new Item(7056), 16), POTATO_WITH_CHEESE(
						new Item(6705), 16), MUSHROOM_POTATO(new Item(7058), 20), TUNA_POTATO(new Item(7060), 22),

		/*
		 * Fruit food types which a player can get by picking from certain trees or
		 * hand-making them (such as pineapple chunks/rings).
		 */
		SPINACH_ROLL(new Item(1969), 2), BANANA(new Item(1963), 2), BANANA_(new Item(18199), 2), CABBAGE(new Item(1965),
				2), ORANGE(new Item(2108), 2), PINEAPPLE_CHUNKS(new Item(2116),
						2), PINEAPPLE_RINGS(new Item(2118), 2), PEACH(new Item(6883), 8),

		/*
		 * Other food types.
		 */
		PURPLE_SWEETS(new Item(4561), 3);

		static Map<Integer, Edible> types = new HashMap<Integer, Edible>();

		static {
			for (Edible type : Edible.values()) {
				types.put(type.item.getId(), type);
			}
		}

		private Item item;
		private int heal;
		private String name;

		private Edible(Item item, int heal) {
			this.item = item;
			this.heal = heal;
			this.name = (toString().toLowerCase().replaceAll("__", "-").replaceAll("_", " "));
		}

		public Item getItem() { return item; }

		/**
		 * Returns an array of all Edible item ids.
		 *
		 * @return {Integer[]} edibleTypes
		 */
		public static Integer[] getTypes() {
			return types.keySet().toArray(new Integer[0]);
		}

		public int getHeal() {
			return heal;
		}
	}
}
