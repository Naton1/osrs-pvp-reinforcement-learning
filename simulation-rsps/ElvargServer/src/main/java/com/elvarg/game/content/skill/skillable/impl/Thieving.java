package com.elvarg.game.content.skill.skillable.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.elvarg.game.content.PetHandler;
import com.elvarg.game.content.combat.CombatFactory;
import com.elvarg.game.content.combat.hit.HitDamage;
import com.elvarg.game.content.combat.hit.HitMask;
import com.elvarg.game.entity.impl.npc.NPC;
import com.elvarg.game.entity.impl.object.GameObject;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.Animation;
import com.elvarg.game.model.Graphic;
import com.elvarg.game.model.GraphicHeight;
import com.elvarg.game.model.Item;
import com.elvarg.game.model.Skill;
import com.elvarg.game.task.Task;
import com.elvarg.game.task.TaskManager;
import com.elvarg.game.task.impl.TimedObjectReplacementTask;
import com.elvarg.util.ItemIdentifiers;
import com.elvarg.util.Misc;
import com.elvarg.util.timers.TimerKey;

/**
 * Handles actions related to the Thieving skill.
 * <p>
 * The Thieving skill allows a player to steal items in the game. Either from
 * npcs or objects.
 *
 * @author Professor Oak
 */
public class Thieving extends ItemIdentifiers {

	/**
	 * The {@link Animation} a player will perform when thieving.
	 */
	private static final Animation THIEVING_ANIMATION = new Animation(881);

	/**
	 * The {@link Graphic} a player will perform when being stunned.
	 */
	private static final Graphic STUNNED_GFX = new Graphic(254, GraphicHeight.HIGH);

	/**
	 * The {@link Animation} an npc will perform when attacking a pickpocket.
	 */
	private static final Animation NPC_ATTACK_ANIMATION = new Animation(401);

	/**
	 * The {@link Animation} the player will perform when blocking an attacking
	 * {@link NPC}.
	 */
	private static final Animation PLAYER_BLOCK_ANIMATION = new Animation(404);

	/**
	 * Handles Pickpocketing.
	 *
	 * @author Professor Oak
	 */
	public static final class Pickpocketing {

		/**
		 * Attempts to pickpocket an npc.
		 *
		 * @param player
		 * @param npc
		 * @return
		 */
		public static boolean init(Player player, NPC npc) {
			Optional<Pickpocketable> pickpocket = Pickpocketable.get(npc.getId());
			if (pickpocket.isPresent()) {
				if (hasRequirements(player, npc, pickpocket.get())) {
					// Stop movement..
					player.getMovementQueue().reset();

					// Start animation..
					player.performAnimation(THIEVING_ANIMATION);

					// Send message..
					String name = npc.getCurrentDefinition().getName().toLowerCase();
					if (!name.endsWith("s")) {
						name += "'s";
					}
					player.getPacketSender().sendMessage("You attempt to pick the " + name + " pocket..");

					// Face npc..
					player.setPositionToFace(npc.getLocation());

					// Reset click delay..
					player.getClickDelay().reset();

					// Mark npc as immune for 5 seconds..
					// This makes it so other players can't attack it.
					npc.getTimers().register(TimerKey.ATTACK_IMMUNITY, Misc.getTicks(5));

					// Submit new task..
					TaskManager.submit(new Task(2, player, false) {
						@Override
						protected void execute() {
							if (isSuccessful(player, pickpocket.get())) {
								// Get the loot..
								Item loot = pickpocket.get().getRewards()[Misc
										.getRandom(pickpocket.get().getRewards().length - 1)].clone();

								// If we're pickpocketing the Master farmer and the required chance
								// isn't hit, make sure to reward the default item.
								// This is to make sure the other seeds remain semi-rare.
								if (pickpocket.get() == Pickpocketable.MASTER_FARMER) {
									if (Misc.getRandom(100) > 18) {
										loot = pickpocket.get().getRewards()[0];
									}

									// Mix up loot amounts aswell for seeds..
									if (loot.getAmount() > 1) {
										loot.setAmount(1 + Misc.getRandom(loot.getAmount()));
									}
								}

								// Reward loot
								if (!player.getInventory().isFull()) {
									player.getInventory().add(loot);
								}

								// Send second item loot message..
								String name = loot.getDefinition().getName().toLowerCase();
								if (!name.endsWith("s") && loot.getAmount() > 1) {
									name += "s";
								}
								player.getPacketSender().sendMessage("You steal "
										+ (loot.getAmount() > 1 ? Integer.toString(loot.getAmount()) : Misc.anOrA(name))
										+ " " + name + ".");

								// Add experience..
								player.getSkillManager().addExperience(Skill.THIEVING, (int) pickpocket.get().getExp());
							} else {
								// Make npc hit the player..
								npc.setPositionToFace(player.getLocation());
								npc.forceChat((pickpocket.get() == Pickpocketable.MASTER_FARMER
										? "Cor blimey, mate! What are ye doing in me pockets?"
										: "What do you think you're doing?"));
								npc.performAnimation(NPC_ATTACK_ANIMATION);
								player.getPacketSender().sendMessage("You fail to pick the pocket.");
								CombatFactory.stun(player, pickpocket.get().getStunTime(), true);
								player.getCombat().getHitQueue()
										.addPendingDamage(new HitDamage(pickpocket.get().getStunDamage(), HitMask.RED));
								player.getMovementQueue().reset();
							}
							// Add pet..
							PetHandler.onSkill(player, Skill.THIEVING);

							// Stop task..
							stop();
						}
					});
				}
				return true;
			}
			return false;
		}

		/**
		 * Checks if a player has the requirements to thieve the given
		 * {@link Pickpocketable}.
		 *
		 * @param player
		 * @param p
		 * @return
		 */
		private static boolean hasRequirements(Player player, NPC npc, Pickpocketable p) {
			// Make sure they aren't spam clicking..
			if (!player.getClickDelay().elapsed(1500)) {
				return false;
			}

			// Check thieving level..
			if (player.getSkillManager().getCurrentLevel(Skill.THIEVING) < p.getLevel()) {
			//	DialogueManager.sendStatement(player, "You need a Thieving level of at least " + Integer.toString(p.getLevel()) + " to do this.");
				return false;
			}

			// Check stun..
			if (player.getTimers().has(TimerKey.STUN)) {
				return false;
			}

			// Make sure we aren't in combat..
			if (CombatFactory.inCombat(player)) {
				player.getPacketSender().sendMessage("You must wait a few seconds after being in combat to do this.");
				return false;
			}

			// Make sure they aren't in combat..
			if (CombatFactory.inCombat(npc)) {
				player.getPacketSender().sendMessage("That npc is currently in combat and cannot be pickpocketed.");
				return false;
			}

			// Make sure we have inventory space..
			if (player.getInventory().isFull()) {
				player.getInventory().full();
				return false;
			}

			return true;
		}

		/**
		 * Determines the chance of failure. method.
		 *
		 * @param player
		 *            The entity who is urging to reach for the pocket.
		 * @return the result of chance.
		 */
		private static boolean isSuccessful(Player player, Pickpocketable p) {
			int base = 4;
			if (p == Pickpocketable.FEMALE_HAM_MEMBER || p == Pickpocketable.MALE_HAM_MEMBER) {
				// TODO: Handle ham clothing bonus chance of success
			}
			short factor = (short) Misc.getRandom(player.getSkillManager().getCurrentLevel(Skill.THIEVING) + base);
			short fluke = (short) Misc.getRandom(p.getLevel());
			return factor > fluke;
		}

		/**
		 * Represents an npc which can be pickpocketed ingame.
		 *
		 * @author Professor Oak
		 */
		// TODO: Add the npc ids for the ones that are commented out.
		public enum Pickpocketable {
			MAN_WOMAN(1, 8, 5, 1, new Item[] { new Item(COINS, 3) }, 3014, 3015, 3078, 3079, 3080, 3081, 3082, 3083,
					3084, 3085, 3267, 3268, 3260, 3264, 3265, 3266, 3267, 3268), FARMER(10, 15, 5, 1,
							new Item[] { new Item(COINS, 9), new Item(POTATO_SEED) }, 3086, 3087, 3088, 3089, 3090,
							3091), FEMALE_HAM_MEMBER(15, 19, 4, 3, new Item[] { new Item(BUTTONS),
									new Item(RUSTY_SWORD), new Item(DAMAGED_ARMOUR), new Item(FEATHER, 5),
									new Item(BRONZE_ARROW), new Item(BRONZE_AXE), new Item(BRONZE_DAGGER),
									new Item(BRONZE_PICKAXE), new Item(COWHIDE), new Item(IRON_AXE),
									new Item(IRON_PICKAXE), new Item(LEATHER_BOOTS), new Item(LEATHER_GLOVES),
									new Item(LEATHER_BODY), new Item(LOGS), new Item(THREAD), new Item(RAW_ANCHOVIES),
									new Item(LOGS), new Item(RAW_CHICKEN), new Item(IRON_ORE), new Item(COAL),
									new Item(STEEL_ARROW, 2), new Item(STEEL_AXE), new Item(STEEL_PICKAXE),
									new Item(KNIFE), new Item(NEEDLE), new Item(STEEL_DAGGER), new Item(TINDERBOX),
									new Item(UNCUT_JADE), new Item(UNCUT_OPAL), new Item(COINS, 25),
									new Item(HAM_GLOVES), new Item(HAM_CLOAK), new Item(HAM_BOOTS), new Item(HAM_SHIRT),
									new Item(HAM_ROBE), new Item(HAM_LOGO), new Item(HAM_HOOD),
									new Item(GRIMY_GUAM_LEAF), new Item(GRIMY_MARRENTILL), new Item(GRIMY_TARROMIN),
									new Item(GRIMY_HARRALANDER) }, 2540,
									2541), MALE_HAM_MEMBER(20, 23, 4, 3, new Item[] { new Item(BUTTONS),
											new Item(RUSTY_SWORD), new Item(DAMAGED_ARMOUR), new Item(FEATHER, 5),
											new Item(BRONZE_ARROW), new Item(BRONZE_AXE), new Item(BRONZE_DAGGER),
											new Item(BRONZE_PICKAXE), new Item(COWHIDE), new Item(IRON_AXE),
											new Item(IRON_PICKAXE), new Item(LEATHER_BOOTS), new Item(LEATHER_GLOVES),
											new Item(LEATHER_BODY), new Item(LOGS), new Item(THREAD),
											new Item(RAW_ANCHOVIES), new Item(LOGS), new Item(RAW_CHICKEN),
											new Item(IRON_ORE), new Item(COAL), new Item(STEEL_ARROW, 2),
											new Item(STEEL_AXE), new Item(STEEL_PICKAXE), new Item(KNIFE),
											new Item(NEEDLE), new Item(STEEL_DAGGER), new Item(TINDERBOX),
											new Item(UNCUT_JADE), new Item(UNCUT_OPAL), new Item(COINS, 25),
											new Item(HAM_GLOVES), new Item(HAM_CLOAK), new Item(HAM_BOOTS),
											new Item(HAM_SHIRT), new Item(HAM_ROBE), new Item(HAM_LOGO),
											new Item(HAM_HOOD), new Item(GRIMY_GUAM_LEAF), new Item(GRIMY_MARRENTILL),
											new Item(GRIMY_TARROMIN), new Item(GRIMY_HARRALANDER) }), AL_KHARID_WARRIOR(
													25, 26, 5, 2, new Item[] { new Item(COINS, 18) }, 3100), ROGUE(32,
															36, 5, 2,
															new Item[] { new Item(COINS, 34), new Item(
																	LOCKPICK), new Item(IRON_DAGGER_P_),
																	new Item(JUG_OF_WINE), new Item(AIR_RUNE, 8) },
															2884), CAVE_GOBLIN(36, 40, 5, 1, new Item[] {
																	new Item(COINS, 10), new Item(IRON_ORE),
																	new Item(TINDERBOX), new Item(SWAMP_TAR),
																	new Item(OIL_LANTERN), new Item(TORCH),
																	new Item(GREEN_GLOOP_SOUP),
																	new Item(FROGSPAWN_GUMBO), new Item(FROGBURGER),
																	new Item(COATED_FROGS_LEGS), new Item(BAT_SHISH),
																	new Item(FINGERS), new Item(BULLSEYE_LANTERN),
																	new Item(CAVE_GOBLIN_WIRE) }, 2268, 2269, 2270,
																	2271, 2272, 2273, 2274, 2275, 2276, 2277, 2278,
																	2279, 2280, 2281, 2282, 2283, 2284,
																	2285), MASTER_FARMER(38, 43, 5, 3,
																			new Item[] { new Item(POTATO_SEED, 12),
																					new Item(ONION_SEED, 8),
																					new Item(CABBAGE_SEED, 5),
																					new Item(TOMATO_SEED, 4),
																					new Item(HAMMERSTONE_SEED, 4),
																					new Item(BARLEY_SEED, 4),
																					new Item(MARIGOLD_SEED, 4),
																					new Item(ASGARNIAN_SEED, 4),
																					new Item(JUTE_SEED, 4),
																					new Item(REDBERRY_SEED, 4),
																					new Item(NASTURTIUM_SEED, 4),
																					new Item(YANILLIAN_SEED, 4),
																					new Item(CADAVABERRY_SEED, 4),
																					new Item(SWEETCORN_SEED, 4),
																					new Item(ROSEMARY_SEED, 4),
																					new Item(DWELLBERRY_SEED, 3),
																					new Item(GUAM_SEED, 3),
																					new Item(WOAD_SEED, 3),
																					new Item(KRANDORIAN_SEED, 3),
																					new Item(STRAWBERRY_SEED, 3),
																					new Item(LIMPWURT_SEED, 3),
																					new Item(MARRENTILL_SEED, 3),
																					new Item(JANGERBERRY_SEED, 3),
																					new Item(TARROMIN_SEED, 2),
																					new Item(WILDBLOOD_SEED, 2),
																					new Item(WATERMELON_SEED, 2),
																					new Item(HARRALANDER_SEED, 2),
																					new Item(RANARR_SEED, 1),
																					new Item(WHITEBERRY_SEED, 2),
																					new Item(TOADFLAX_SEED, 2),
																					new Item(MUSHROOM_SPORE, 2),
																					new Item(IRIT_SEED, 2),
																					new Item(BELLADONNA_SEED, 2),
																					new Item(POISON_IVY_SEED, 2),
																					new Item(AVANTOE_SEED, 1),
																					new Item(CACTUS_SEED, 1),
																					new Item(KWUARM_SEED, 1),
																					new Item(SNAPDRAGON_SEED, 1),
																					new Item(CADANTINE_SEED, 1),
																					new Item(LANTADYME_SEED, 1),
																					new Item(DWARF_WEED_SEED, 1),
																					new Item(TORSTOL_SEED, 1), },
																			3257, 3258, 5832), GUARD(40, 47, 5, 2,
																					new Item[] { new Item(COINS, 30) },
																					1546, 1547, 1548, 1549, 1550, 3010,
																					3011, 3094, 3245, 3267, 3268, 3269,
																					3270, 3271, 3272, 3273, 3274,
																					3283), FREMENNIK_CITIZEN(45, 65, 5,
																							2,
																							new Item[] { new Item(COINS,
																									40) },
																							2462), BEARDED_POLLNIVNIAN_BANDIT(
																									45, 65, 5, 5,
																									new Item[] {
																											new Item(
																													COINS,
																													40) },
																									1880),
			// DESERT_BANDIT(53, 80, 5, 3, new Item[]{new Item(COINS, 30), new
			// Item(ANTIPOISON_4_), new Item(LOCKPICK)}),
			// KNIGHT(55, 84, 5, 3, new Item[]{new Item(COINS, 50)}),
			// POLLNIVIAN_BANDIT(55, 84, 5, 5, new Item[]{new Item(COINS, 50)}),
			YANILLE_WATCHMAN(65, 137, 5, 3, new Item[] { new Item(COINS, 60), new Item(BREAD) }, 3251), MENAPHITE_THUG(
					65, 137, 5, 5, new Item[] { new Item(COINS, 60) }, 3549, 3550), PALADIN(70, 152, 5, 3,
							new Item[] { new Item(COINS, 80), new Item(CHAOS_RUNE, 2) }, 3104, 3105), GNOME(75, 199, 5,
									1,
									new Item[] { new Item(COINS, 300), new Item(EARTH_RUNE), new Item(GOLD_ORE),
											new Item(FIRE_ORB), new Item(SWAMP_TOAD), new Item(KING_WORM) },
									6086, 6087, 6094, 6095, 6096),
			// HERO(80, 275, 6, 4, new Item[]{new Item(COINS, 280), new Item(BLOOD_RUNE),
			// new Item(DIAMOND), new Item(JUG_OF_WINE), new Item(DEATH_RUNE, 2), new
			// Item(FIRE_ORB), new Item(GOLD_ORE)}),
			// ELF(85, 353, 6, 5, new Item[]{new Item(COINS, 325), new Item(NATURE_RUNE, 3),
			// new Item(DIAMOND), new Item(JUG_OF_WINE), new Item(DEATH_RUNE, 2), new
			// Item(FIRE_ORB), new Item(GOLD_ORE)}),

			;
			static Map<Integer, Pickpocketable> pickpockets = new HashMap<Integer, Pickpocketable>();

			static {
				for (Pickpocketable p : Pickpocketable.values()) {
					for (int i : p.getNpcs()) {
						pickpockets.put(i, p);
					}
				}
			}

			private final int level;
			private final int exp;
			private final int stunTime;
			private final int stunDamage;
			private final Item[] rewards;
			private final int[] npcs;

			Pickpocketable(int level, int exp, int stunTime, int stunDamage, Item[] rewards, int... npcs) {
				this.level = level;
				this.exp = exp;
				this.stunTime = stunTime;
				this.stunDamage = stunDamage;
				this.rewards = rewards;
				this.npcs = npcs;
			}

			public static Optional<Pickpocketable> get(int npcId) {
				return Optional.ofNullable(pickpockets.get(npcId));
			}

			public int getLevel() {
				return level;
			}

			public int getExp() {
				return exp;
			}

			public int getStunTime() {
				return stunTime;
			}

			public int getStunDamage() {
				return stunDamage;
			}

			public Item[] getRewards() {
				return rewards;
			}

			public int[] getNpcs() {
				return npcs;
			}
		}
	}

	/**
	 * Handles thieving from stalls.
	 *
	 * @author Professor Oak
	 */
	public static final class StallThieving {

		/**
		 * Checks if we're attempting to steal from a stall based on the clicked object.
		 *
		 * @param player
		 * @param object
		 * @return
		 */
		public static boolean init(Player player, GameObject object) {
			Optional<Stall> stall = Stall.get(object.getId());
			if (stall.isPresent()) {

				// Make sure we have the required thieving level..
				if (player.getSkillManager().getCurrentLevel(Skill.THIEVING) >= stall.get().getReqLevel()) {

					// Make sure we aren't spam clicking..
					if (player.getClickDelay().elapsed(1000)) {

						// Reset click delay..
						player.getClickDelay().reset();

						// Face stall..
						player.setPositionToFace(object.getLocation());

						// Perform animation..
						player.performAnimation(THIEVING_ANIMATION);

						// Add items..
						Item item = stall.get().getRewards()[Misc.getRandom(stall.get().getRewards().length - 1)];
						player.getInventory().add(item.getId(),
								item.getAmount() > 1 ? Misc.getRandom(item.getAmount()) : 1);

						// Add pet..
						PetHandler.onSkill(player, Skill.THIEVING);

						// Respawn stall..
						for (StallDefinition stallDef : stall.get().getStalls()) {
							if (stallDef.getObjectId() == object.getId()) {
								if (stallDef.getReplacement().isPresent()) {
									TaskManager.submit(new TimedObjectReplacementTask(object,
											new GameObject(stallDef.getReplacement().get(), object.getLocation(),
													object.getType(), object.getFace(), player.getPrivateArea()),
											stall.get().getRespawnTicks()));
								}
								break;
							}
						}
					}
				} else {
					//DialogueManager.sendStatement(player, "You need a Thieving level of at least "
					//		+ Integer.toString(stall.get().getReqLevel()) + " to do this.");
				}
				return true;
			}
			return false;
		}

		/**
		 * Represents a stall which can be stolen from using the Thieving skill.
		 *
		 * @author Professor Oak
		 */
		public enum Stall {
			BAKERS_STALL(new StallDefinition[] { new StallDefinition(11730, Optional.of(634)), }, 5, 16, 3,
					new Item(CAKE), new Item(CHOCOLATE_SLICE), new Item(BREAD)), CRAFTING_STALL(
							new StallDefinition[] { new StallDefinition(4874, Optional.empty()),
									new StallDefinition(6166, Optional.empty()) },
							5, 16, 12, new Item(CHISEL), new Item(RING_MOULD), new Item(NECKLACE_MOULD)), MONKEY_STALL(
									new StallDefinition[] { new StallDefinition(4875, Optional.empty()) }, 5, 16, 12,
									new Item(BANANA)), MONKEY_GENERAL_STALL(
											new StallDefinition[] { new StallDefinition(4876, Optional.empty()) }, 5,
											16, 12, new Item(POT), new Item(TINDERBOX), new Item(HAMMER)), TEA_STALL(
													new StallDefinition[] { new StallDefinition(635, Optional.of(634)),
															new StallDefinition(6574, Optional.of(6573)),
															new StallDefinition(20350, Optional.of(20349)) },
													5, 16, 12, new Item(CUP_OF_TEA)), SILK_STALL(
															new StallDefinition[] {
																	new StallDefinition(11729, Optional.of(634)) },
															20, 24, 8, new Item(SILK)), WINE_STALL(
																	new StallDefinition[] { new StallDefinition(14011,
																			Optional.of(634)) },
																	22, 27, 27, new Item(JUG_OF_WATER),
																	new Item(JUG_OF_WINE), new Item(GRAPES),
																	new Item(EMPTY_JUG),
																	new Item(BOTTLE_OF_WINE)), SEED_STALL(
																			new StallDefinition[] { new StallDefinition(
																					7053, Optional.of(634)), },
																			27, 10, 30, new Item(POTATO_SEED, 12),
																			new Item(ONION_SEED, 11),
																			new Item(CABBAGE_SEED, 10),
																			new Item(TOMATO_SEED, 9),
																			new Item(SWEETCORN_SEED, 7),
																			new Item(STRAWBERRY_SEED, 5),
																			new Item(WATERMELON_SEED, 3),
																			new Item(BARLEY_SEED, 5),
																			new Item(HAMMERSTONE_SEED, 5),
																			new Item(ASGARNIAN_SEED, 5),
																			new Item(JUTE_SEED, 5),
																			new Item(YANILLIAN_SEED, 5),
																			new Item(KRANDORIAN_SEED, 5),
																			new Item(WILDBLOOD_SEED, 3),
																			new Item(MARIGOLD_SEED, 4),
																			new Item(ROSEMARY_SEED, 4),
																			new Item(NASTURTIUM_SEED, 4)), FUR_STALL(
																					new StallDefinition[] {
																							new StallDefinition(11732,
																									Optional.of(634)),
																							new StallDefinition(4278,
																									Optional.of(634)) },
																					35, 36, 17,
																					new Item(
																							GREY_WOLF_FUR)), FISH_STALL(
																									new StallDefinition[] {
																											new StallDefinition(
																													4277,
																													Optional.of(
																															4276)),
																											new StallDefinition(
																													4707,
																													Optional.of(
																															4276)),
																											new StallDefinition(
																													4705,
																													Optional.of(
																															4276)) },
																									42, 42, 17,
																									new Item(
																											RAW_SALMON),
																									new Item(
																											RAW_TUNA)), CROSSBOW_STALL(
																													new StallDefinition[] {
																															new StallDefinition(
																																	17031,
																																	Optional.of(
																																			6984)) },
																													49,
																													52,
																													15,
																													new Item(
																															BRONZE_BOLTS,
																															6),
																													new Item(
																															BRONZE_LIMBS),
																													new Item(
																															WOODEN_STOCK)), SILVER_STALL(
																																	new StallDefinition[] {
																																			new StallDefinition(
																																					11734,
																																					Optional.of(
																																							634)),
																																			new StallDefinition(
																																					6164,
																																					Optional.of(
																																							6984)), },
																																	50,
																																	54,
																																	50,
																																	new Item(
																																			SILVER_ORE)), SPICE_STALL(
																																					new StallDefinition[] {
																																							new StallDefinition(
																																									11733,
																																									Optional.of(
																																											634)),
																																							new StallDefinition(
																																									6572,
																																									Optional.of(
																																											6573)),
																																							new StallDefinition(
																																									20348,
																																									Optional.of(
																																											20349)), },
																																					65,
																																					81,
																																					133,
																																					new Item(
																																							SPICE)), MAGIC_STALL(
																																									new StallDefinition[] {
																																											new StallDefinition(
																																													4877,
																																													Optional.empty()), },
																																									65,
																																									100,
																																									133,
																																									new Item(
																																											AIR_RUNE,
																																											20),
																																									new Item(
																																											WATER_RUNE,
																																											20),
																																									new Item(
																																											EARTH_RUNE,
																																											20),
																																									new Item(
																																											FIRE_RUNE,
																																											20),
																																									new Item(
																																											LAW_RUNE,
																																											6)), SCIMITAR_STALL(
																																													new StallDefinition[] {
																																															new StallDefinition(
																																																	4878,
																																																	Optional.empty()) },
																																													65,
																																													100,
																																													133,
																																													new Item(
																																															IRON_SCIMITAR)), GEM_STALL(
																																																	new StallDefinition[] {
																																																			new StallDefinition(
																																																					11731,
																																																					Optional.of(
																																																							634)),
																																																			new StallDefinition(
																																																					6162,
																																																					Optional.of(
																																																							6984)), },
																																																	75,
																																																	160,
																																																	133,
																																																	new Item(
																																																			UNCUT_SAPPHIRE),
																																																	new Item(
																																																			UNCUT_EMERALD),
																																																	new Item(
																																																			UNCUT_RUBY),
																																																	new Item(
																																																			UNCUT_DIAMOND)),;

			private static Map<Integer, Stall> map = new HashMap<Integer, Stall>();

			static {
				for (Stall stall : Stall.values()) {
					for (StallDefinition def : stall.getStalls()) {
						map.put(def.getObjectId(), stall);
					}
				}
			}

			private final StallDefinition[] stalls;
			private final int reqLevel;
			private final int exp;
			private final int respawnTicks;
			private final Item[] rewards;

			Stall(StallDefinition[] stalls, int reqLevel, int exp, int respawnTicks, Item... rewards) {
				this.stalls = stalls;
				this.reqLevel = reqLevel;
				this.exp = exp;
				this.respawnTicks = respawnTicks;
				this.rewards = rewards;
			}

			public static Optional<Stall> get(int objectId) {
				return Optional.ofNullable(map.get(objectId));
			}

			public StallDefinition[] getStalls() {
				return stalls;
			}

			public int getReqLevel() {
				return reqLevel;
			}

			public int getExp() {
				return exp;
			}

			public int getRespawnTicks() {
				return respawnTicks;
			}

			public Item[] getRewards() {
				return rewards;
			}
		}

		/**
		 * Represents a stall's definition.
		 *
		 * @author Professor Oak
		 */
		public static final class StallDefinition {
			/**
			 * The stall's object id.
			 */
			private final int objectId;

			/**
			 * The replacement object for when this stall temporarily despawns.
			 */
			private final Optional<Integer> replacement;

			public StallDefinition(int objectId, Optional<Integer> replacement) {
				this.objectId = objectId;
				this.replacement = replacement;
			}

			public int getObjectId() {
				return objectId;
			}

			public Optional<Integer> getReplacement() {
				return replacement;
			}
		}
	}
}