package com.elvarg.game.content.skill;

import java.util.Optional;

import com.elvarg.game.GameConstants;
import com.elvarg.game.World;
import com.elvarg.game.content.PrayerHandler;
import com.elvarg.game.content.PrayerHandler.PrayerData;
import com.elvarg.game.content.combat.WeaponInterfaces;
import com.elvarg.game.content.combat.bountyhunter.BountyHunter;
import com.elvarg.game.content.skill.skillable.Skillable;
import com.elvarg.game.content.skill.skillable.impl.Mining;
import com.elvarg.game.content.skill.skillable.impl.Mining.Rock;
import com.elvarg.game.content.skill.skillable.impl.Runecrafting;
import com.elvarg.game.content.skill.skillable.impl.woodcutting.Woodcutting;
import com.elvarg.game.content.skill.skillable.impl.woodcutting.Woodcutting.Tree;
import com.elvarg.game.entity.impl.object.GameObject;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.Flag;
import com.elvarg.game.model.Graphic;
import com.elvarg.game.model.Item;
import com.elvarg.game.model.Skill;
import com.elvarg.game.model.areas.impl.WildernessArea;
import com.elvarg.game.model.equipment.BonusManager;
import com.elvarg.game.model.rights.PlayerRights;
import com.elvarg.util.Misc;

/**
 * Acts as a manager for all of the skills ingame.
 *
 * @author relex lawl
 * @editor Professor Oak
 */

public class SkillManager {

	/**
	 * The maximum amount of skills in the game.
	 */
	public static final int AMOUNT_OF_SKILLS = Skill.values().length;
	/**
	 * The maximum amount of experience you can achieve in a skill.
	 */
	private static final int MAX_EXPERIENCE = 1000000000;
	private static final int EXPERIENCE_FOR_99 = 13034431;
	private static final int EXP_ARRAY[] = { 0, 83, 174, 276, 388, 512, 650, 801, 969, 1154, 1358, 1584, 1833, 2107,
			2411, 2746, 3115, 3523, 3973, 4470, 5018, 5624, 6291, 7028, 7842, 8740, 9730, 10824, 12031, 13363, 14833,
			16456, 18247, 20224, 22406, 24815, 27473, 30408, 33648, 37224, 41171, 45529, 50339, 55649, 61512, 67983,
			75127, 83014, 91721, 101333, 111945, 123660, 136594, 150872, 166636, 184040, 203254, 224466, 247886, 273742,
			302288, 333804, 368599, 407015, 449428, 496254, 547953, 605032, 668051, 737627, 814445, 899257, 992895,
			1096278, 1210421, 1336443, 1475581, 1629200, 1798808, 1986068, 2192818, 2421087, 2673114, 2951373, 3258594,
			3597792, 3972294, 4385776, 4842295, 5346332, 5902831, 6517253, 7195629, 7944614, 8771558, 9684577, 10692629,
			11805606, 13034431 };
	private static final Graphic LEVEL_UP_GRAPHIC = new Graphic(199);
	
	/**
	 * The player associated with this Skills instance.
	 */
	private Player player;
	private Skills skills;

	/**
	 * The skillmanager's constructor
	 *
	 * @param player
	 *            The player's who skill set is being represented.
	 */
	public SkillManager(Player player) {
		this.player = player;
		this.skills = new Skills();
		for (int i = 0; i < AMOUNT_OF_SKILLS; i++) {
			skills.level[i] = skills.maxLevel[i] = 1;
			skills.experience[i] = 0;
		}
		skills.level[Skill.HITPOINTS.ordinal()] = skills.maxLevel[Skill.HITPOINTS.ordinal()] = 10;
		skills.experience[Skill.HITPOINTS.ordinal()] = 1184;
	}

	/**
	 * Gets the minimum experience in said level.
	 *
	 * @param level
	 *            The level to get minimum experience for.
	 * @return The least amount of experience needed to achieve said level.
	 */
	public static int getExperienceForLevel(int level) {
		if (level <= 99) {
			return EXP_ARRAY[--level > 98 ? 98 : level];
		} else {
			int points = 0;
			int output = 0;
			for (int lvl = 1; lvl <= level; lvl++) {
				points += Math.floor(lvl + 300.0 * Math.pow(2.0, lvl / 7.0));
				if (lvl >= level) {
					return output;
				}
				output = (int) Math.floor(points / 4);
			}
		}
		return 0;
	}

	/**
	 * Gets the level from said experience.
	 *
	 * @param experience
	 *            The experience to get level for.
	 * @return The level you obtain when you have specified experience.
	 */
	public static int getLevelForExperience(int experience) {
		if (experience <= EXPERIENCE_FOR_99) {
			for (int j = 98; j >= 0; j--) {
				if (EXP_ARRAY[j] <= experience) {
					return j + 1;
				}
			}
		} else {
			int points = 0, output = 0;
			for (int lvl = 1; lvl <= 99; lvl++) {
				points += Math.floor(lvl + 300.0 * Math.pow(2.0, lvl / 7.0));
				output = (int) Math.floor(points / 4);
				if (output >= experience) {
					return lvl;
				}
			}
		}
		return 99;
	}

	/**
	 * Gets the max level for <code>skill</code>
	 *
	 * @param skill
	 *            The skill to get max level for.
	 * @return The max level that can be achieved in said skill.
	 */
	public static int getMaxAchievingLevel(Skill skill) {
		return 99;
	}

	/**
	 * Adds experience to {@code skill} by the {@code experience} amount.
	 * 
	 * @param skill
	 * @param experience
	 * @return
	 */
	public SkillManager addExperience(Skill skill, int experience) {
		return addExperience(skill, experience, true);
	}

	/**
	 * Adds experience to {@code skill} by the {@code experience} amount.
	 *
	 * @param skill
	 *            The skill to add experience to.
	 * @param experience
	 *            The amount of experience to add to the skill.
	 * @return The Skills instance.
	 */
	public SkillManager addExperience(Skill skill, int experience, boolean multipliers) {
		// Multipliers...
		if (multipliers) {
			if (skill == Skill.ATTACK || skill == Skill.DEFENCE || skill == Skill.STRENGTH || skill == Skill.HITPOINTS
					|| skill == Skill.RANGED || skill == Skill.MAGIC) {
				experience *= GameConstants.COMBAT_SKILLS_EXP_MULTIPLIER;
			} else {
				experience *= GameConstants.REGULAR_SKILLS_EXP_MULTIPLIER;
			}
		}

		// Send exp drop..
		player.getPacketSender().sendExpDrop(skill, experience);

		// Don't add the experience if it has been locked..
		if (player.experienceLocked())
			return this;

		// If we already have max exp, don't add any more.
		if (this.skills.experience[skill.ordinal()] >= MAX_EXPERIENCE)
			return this;

		// The skill's level before any experience is added
		final int startingLevel = skills.maxLevel[skill.ordinal()];

		// Add experience to the selected skill..
		this.skills.experience[skill.ordinal()] = this.skills.experience[skill.ordinal()] + experience > MAX_EXPERIENCE
				? MAX_EXPERIENCE
				: this.skills.experience[skill.ordinal()] + experience;

		// Get the skill's new level after experience has been added..
		int newLevel = getLevelForExperience(this.skills.experience[skill.ordinal()]);

		// Handle level up..
		if (newLevel > startingLevel) {
			int level = newLevel - startingLevel;
			String skillName = Misc.ucFirst(skill.toString().toLowerCase());
			skills.maxLevel[skill.ordinal()] += level;
			stopSkillable(); // Stop skilling on level up like osrs
			setCurrentLevel(skill, skills.maxLevel[skill.ordinal()]);
			player.getPacketSender().sendInterfaceRemoval();
			player.getPacketSender().sendString(4268, "Congratulations! You have achieved a " + skillName + " level!");
			player.getPacketSender().sendString(4269, "Well done. You are now level " + newLevel + ".");
			player.getPacketSender().sendString(358, "Click here to continue.");
			player.getPacketSender().sendChatboxInterface(skill.getChatboxInterface());
			player.performGraphic(LEVEL_UP_GRAPHIC);
			player.getPacketSender()
					.sendMessage("You've just advanced " + skillName + " level! You have reached level " + newLevel);
			if (skills.maxLevel[skill.ordinal()] == getMaxAchievingLevel(skill)) {
				player.getPacketSender()
						.sendMessage("Well done! You've achieved the highest possible level in this skill!");
				World.sendMessage("<shad=15536940>News: " + player.getUsername()
						+ " has just achieved the highest possible level in " + skillName + "!");
			}
			player.getUpdateFlag().flag(Flag.APPEARANCE);
		}
		updateSkill(skill);
		return this;
	}

	/**
	 * Checks if the button that was clicked is used for setting a skill to a
	 * desired level.
	 *
	 * @param button
	 *            The button that was clicked.
	 * @return True if a skill should be set, false otherwise.
	 */
	public boolean pressedSkill(int button) {
		Skill skill = Skill.forButton(button);
		if (skill != null) {
			if (!skill.canSetLevel()) {
				if (player.getRights() != PlayerRights.ADMINISTRATOR && player.getRights() != PlayerRights.DEVELOPER
						&& player.getRights() != PlayerRights.OWNER) {
					player.getPacketSender().sendMessage("You can currently not set that level.");
					return true;
				}
			}
			player.getPacketSender().sendInterfaceRemoval();
			player.setEnteredAmountAction((amount) -> {
		        int max = 99;
		        if (player.getRights() == PlayerRights.OWNER
		                || player.getRights() == PlayerRights.DEVELOPER) {
		            max = 9999;
		        }
		        if (amount <= 0 || amount > max) {
		            player.getPacketSender().sendMessage("Invalid syntax. Please enter a level in the range of 1-99.");
		            return;
		        }
		        player.getSkillManager().setLevel(skill, amount);
			});
			player.getPacketSender()
					.sendEnterAmountPrompt("Please enter your desired " + skill.getName() + " level below.");

			return true;
		}
		return false;
	}

	/**
	 * Sets a skill to the desired level.
	 *
	 * @param skill
	 * @param level
	 */
	public void setLevel(Skill skill, int level) {

		// Make sure they aren't in wild
		if (player.getArea() instanceof WildernessArea) {
			if (player.getRights() != PlayerRights.ADMINISTRATOR && player.getRights() != PlayerRights.DEVELOPER
					&& player.getRights() != PlayerRights.OWNER) {
				player.getPacketSender().sendMessage("You cannot do this in the Wilderness!");
				return;
			}
		}

		// make sure they aren't wearing any items which arent allowed to be worn at
		// that level.
		if (player.getRights() != PlayerRights.DEVELOPER) {
			for (Item item : player.getEquipment().getItems()) {
				if (item == null) {
					continue;
				}
				if (item.getDefinition().getRequirements() != null) {
					if (item.getDefinition().getRequirements()[skill.ordinal()] > level) {
						player.getPacketSender().sendMessage(
								"Please unequip your " + item.getDefinition().getName() + " before doing that.");
						return;
					}
				}
			}
		}

		if (skill == Skill.HITPOINTS) {
			if (level < 10) {
				player.getPacketSender().sendMessage("Hitpoints must be set to at least level 10.");
				return;
			}
		}

		// Set skill level
		player.getSkillManager().setCurrentLevel(skill, level, false).setMaxLevel(skill, level, false)
				.setExperience(skill, SkillManager.getExperienceForLevel(level));
		updateSkill(skill);

		if (skill == Skill.PRAYER) {
			player.getPacketSender().sendConfig(709, PrayerHandler.canUse(player, PrayerData.PRESERVE, false) ? 1 : 0);
			player.getPacketSender().sendConfig(711, PrayerHandler.canUse(player, PrayerData.RIGOUR, false) ? 1 : 0);
			player.getPacketSender().sendConfig(713, PrayerHandler.canUse(player, PrayerData.AUGURY, false) ? 1 : 0);
		}

		// Update weapon tab to send combat level etc.
		player.setHasVengeance(false);
		BonusManager.update(player);
		WeaponInterfaces.assign(player);
		PrayerHandler.deactivatePrayers(player);
		BountyHunter.unassign(player);
		player.getUpdateFlag().flag(Flag.APPEARANCE);
	}

	/**
	 * Updates the skill strings, for skill tab and orb updating.
	 *
	 * @param skill
	 *            The skill who's strings to update.
	 * @return The Skills instance.
	 */
	public SkillManager updateSkill(Skill skill) {
		int maxLevel = getMaxLevel(skill), currentLevel = getCurrentLevel(skill);

		// Update prayer tab if it's the prayer skill.
		if (skill == Skill.PRAYER)
			player.getPacketSender().sendString(687, currentLevel + "/" + maxLevel);

		// Send total level
		player.getPacketSender().sendString(31200, "" + getTotalLevel());

		// Send combat level
		final String combatLevel = "Combat level: " + getCombatLevel();
		player.getPacketSender().sendString(19000, combatLevel).sendString(5858, combatLevel);

		// Send the skill
		player.getPacketSender().sendSkill(skill);

		return this;
	}

	/**
	 * Calculates the player's combat level.
	 *
	 * @return The average of the player's combat skills.
	 */
	public int getCombatLevel() {
		final int attack = skills.maxLevel[Skill.ATTACK.ordinal()];
		final int defence = skills.maxLevel[Skill.DEFENCE.ordinal()];
		final int strength = skills.maxLevel[Skill.STRENGTH.ordinal()];
		final int hp = (int) (skills.maxLevel[Skill.HITPOINTS.ordinal()]);
		final int prayer = (int) (skills.maxLevel[Skill.PRAYER.ordinal()]);
		final int ranged = skills.maxLevel[Skill.RANGED.ordinal()];
		final int magic = skills.maxLevel[Skill.MAGIC.ordinal()];
		int combatLevel = 3;
		combatLevel = (int) ((defence + hp + Math.floor(prayer / 2)) * 0.2535) + 1;
		final double melee = (attack + strength) * 0.325;
		final double ranger = Math.floor(ranged * 1.5) * 0.325;
		final double mage = Math.floor(magic * 1.5) * 0.325;
		if (melee >= ranger && melee >= mage) {
			combatLevel += melee;
		} else if (ranger >= melee && ranger >= mage) {
			combatLevel += ranger;
		} else if (mage >= melee && mage >= ranger) {
			combatLevel += mage;
		}
		if (combatLevel > 126) {
			return 126;
		}
		if (combatLevel < 3) {
			return 3;
		}
		return combatLevel;
	}

	/**
	 * Gets the player's total level.
	 *
	 * @return The value of every skill summed up.
	 */
	public int getTotalLevel() {
		int total = 0;
		for (Skill skill : Skill.values()) {
			total += skills.maxLevel[skill.ordinal()];
		}
		return total;
	}

	/**
	 * Gets the player's total experience.
	 *
	 * @return The experience value from the player's every skill summed up.
	 */
	public long getTotalExp() {
		long xp = 0;
		for (Skill skill : Skill.values())
			xp += player.getSkillManager().getExperience(skill);
		return xp;
	}

	/**
	 * Gets the current level for said skill.
	 *
	 * @param skill
	 *            The skill to get current/temporary level for.
	 * @return The skill's level.
	 */
	public int getCurrentLevel(Skill skill) {
		return skills.level[skill.ordinal()];
	}

	/**
	 * Gets the max level for said skill.
	 *
	 * @param skill
	 *            The skill to get max level for.
	 * @return The skill's maximum level.
	 */
	public int getMaxLevel(Skill skill) {
		return skills.maxLevel[skill.ordinal()];
	}

	/**
	 * Gets the max level for said skill.
	 *
	 * @param skill
	 *            The skill to get max level for.
	 * @return The skill's maximum level.
	 */
	public int getMaxLevel(int skill) {
		return skills.maxLevel[skill];
	}

	/**
	 * Gets the experience for said skill.
	 *
	 * @param skill
	 *            The skill to get experience for.
	 * @return The experience in said skill.
	 */
	public int getExperience(Skill skill) {
		return skills.experience[skill.ordinal()];
	}

	/**
	 * Sets the current level of said skill.
	 *
	 * @param skill
	 *            The skill to set current/temporary level for.
	 * @param level
	 *            The level to set the skill to.
	 * @param refresh
	 *            If <code>true</code>, the skill's strings will be updated.
	 * @return The Skills instance.
	 */
	public SkillManager setCurrentLevel(Skill skill, int level, boolean refresh) {
		this.skills.level[skill.ordinal()] = level < 0 ? 0 : level;
		if (refresh)
			updateSkill(skill);
		return this;
	}

	/**
	 * Sets the maximum level of said skill.
	 *
	 * @param skill
	 *            The skill to set maximum level for.
	 * @param level
	 *            The level to set skill to.
	 * @param refresh
	 *            If <code>true</code>, the skill's strings will be updated.
	 * @return The Skills instance.
	 */
	public SkillManager setMaxLevel(Skill skill, int level, boolean refresh) {
		skills.maxLevel[skill.ordinal()] = level;
		if (refresh)
			updateSkill(skill);
		return this;
	}

	/**
	 * Sets the experience of said skill.
	 *
	 * @param skill
	 *            The skill to set experience for.
	 * @param experience
	 *            The amount of experience to set said skill to.
	 * @param refresh
	 *            If <code>true</code>, the skill's strings will be updated.
	 * @return The Skills instance.
	 */
	public SkillManager setExperience(Skill skill, int experience, boolean refresh) {
		this.skills.experience[skill.ordinal()] = experience < 0 ? 0 : experience;
		if (refresh)
			updateSkill(skill);
		return this;
	}

	/**
	 * Sets the current level of said skill.
	 *
	 * @param skill
	 *            The skill to set current/temporary level for.
	 * @param level
	 *            The level to set the skill to.
	 * @return The Skills instance.
	 */
	public SkillManager setCurrentLevel(Skill skill, int level) {
		setCurrentLevel(skill, level, true);
		return this;
	}

	/**
	 * Sets the maximum level of said skill.
	 *
	 * @param skill
	 *            The skill to set maximum level for.
	 * @param level
	 *            The level to set skill to.
	 * @return The Skills instance.
	 */
	public SkillManager setMaxLevel(Skill skill, int level) {
		setMaxLevel(skill, level, true);
		return this;
	}

	/**
	 * Sets the experience of said skill.
	 *
	 * @param skill
	 *            The skill to set experience for.
	 * @param experience
	 *            The amount of experience to set said skill to.
	 * @return The Skills instance.
	 */
	public SkillManager setExperience(Skill skill, int experience) {
		setExperience(skill, experience, true);
		return this;
	}

	/**
	 * Increments this level by {@code amount} to a maximum of
	 * {@code realLevel + amount}.
	 *
	 * @param amount
	 *            the amount to increase this level by.
	 */
	public void increaseCurrentLevelMax(Skill skill, int amount) {
		final int max = getMaxLevel(skill) + amount;
		if (getCurrentLevel(skill) > max) {
			// Skill is already boosted, nothing to increase, don't lower it
			return;
		}
		increaseCurrentLevel(skill, amount, max);
	}

	/**
	 * Increases the current level
	 *
	 * @param skill
	 * @param boostLevel
	 * @param cap
	 */
	public void increaseCurrentLevel(Skill skill, int amount, int max) {
		final int curr = getCurrentLevel(skill);
		if ((curr + amount) > max) {
			setCurrentLevel(skill, max);
			return;
		}
		setCurrentLevel(skill, curr + amount);
	}

	/**
	 * Decrements this level by {@code amount} to {@code minimum}.
	 *
	 * @param amount
	 *            the amount to decrease this level by.
	 */
	public void decreaseCurrentLevel(Skill skill, int amount, int minimum) {
		final int curr = getCurrentLevel(skill);
		if ((curr - amount) < minimum) {
			setCurrentLevel(skill, minimum);
			return;
		}
		setCurrentLevel(skill, curr - amount);
	}

	/**
	 * Decrements this level by {@code amount} to a minimum of
	 * {@code realLevel - amount}.
	 *
	 * @param amount
	 *            the amount to decrease this level by.
	 */
	public void decreaseLevelMax(Skill skill, int amount) {
		decreaseCurrentLevel(skill, amount, getMaxLevel(skill) - amount);
	}

	public boolean isBoosted(Skill skill) {
		return this.getCurrentLevel(skill) > this.getMaxLevel(skill);
	}

	/**
	 * Checks if a skill should be started based on the {@link GameObject} that was
	 * given.
	 *
	 * @param player
	 * @param object
	 * @return
	 */
	public boolean startSkillable(GameObject object) {
		// Check woodcutting..
		Optional<Tree> tree = Tree.forObjectId(object.getId());
		if (tree.isPresent()) {
			startSkillable(new Woodcutting(object, tree.get()));
			return true;
		}

		// Check mining..
		Optional<Rock> rock = Rock.forObjectId(object.getId());
		if (rock.isPresent()) {
			startSkillable(new Mining(object, rock.get()));
			return true;
		}

		// Check runecrafting
		if (Runecrafting.initialize(player, object.getId())) {
			return true;
		}

		return false;
	}

	/**
	 * Starts the {@link Skillable} skill.
	 *
	 * @param player
	 * @param skill
	 */
	public void startSkillable(Skillable skill) {
		// Stop previous skills..
		stopSkillable();

		// Close interfaces..
		player.getPacketSender().sendInterfaceRemoval();

		// Check if we have the requirements to start this skill..
		if (!skill.hasRequirements(player)) {
			return;
		}

		// Start the skill..
		player.setSkill(Optional.of(skill));
		skill.start(player);
	}

	/**
	 * Stops the player's current skill, if they have one active.
	 *
	 * @param player
	 */
	public void stopSkillable() {
		player.getSkill().ifPresent(e -> e.cancel(player));
		player.setSkill(Optional.empty());
		player.setCreationMenu(null);
	}

	public Skills getSkills() {
		return skills;
	}

	public void setSkills(Skills skills) {
		this.skills = skills;
	}

	public class Skills {

		private int[] level, maxLevel, experience;

		public Skills() {
			level = new int[AMOUNT_OF_SKILLS];
			maxLevel = new int[AMOUNT_OF_SKILLS];
			experience = new int[AMOUNT_OF_SKILLS];
		}

		public int[] getLevels() {
			return level;
		}

		public void setLevels(int[] levels) {
			level = levels;
		}

		public int[] getMaxLevels() {
			return maxLevel;
		}

		public void setMaxLevels(int[] maxLevels) {
			maxLevel = maxLevels;
		}

		public int[] getExperiences() {
			return experience;
		}

		public void setExperiences(int[] experiences) {
			experience = experiences;
		}

	}
}