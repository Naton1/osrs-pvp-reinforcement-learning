package com.elvarg.game.content.combat;

import java.util.Arrays;

import com.elvarg.game.content.Dueling.DuelRule;
import com.elvarg.game.content.combat.WeaponInterfaces.WeaponInterface;
import com.elvarg.game.content.combat.method.CombatMethod;
import com.elvarg.game.content.combat.method.impl.specials.AbyssalBludgeonCombatMethod;
import com.elvarg.game.content.combat.method.impl.specials.AbyssalDaggerCombatMethod;
import com.elvarg.game.content.combat.method.impl.specials.AbyssalTentacleCombatMethod;
import com.elvarg.game.content.combat.method.impl.specials.AbyssalWhipCombatMethod;
import com.elvarg.game.content.combat.method.impl.specials.ArmadylCrossbowCombatMethod;
import com.elvarg.game.content.combat.method.impl.specials.ArmadylGodswordCombatMethod;
import com.elvarg.game.content.combat.method.impl.specials.BallistaCombatMethod;
import com.elvarg.game.content.combat.method.impl.specials.BandosGodswordCombatMethod;
import com.elvarg.game.content.combat.method.impl.specials.BarrelchestAnchorCombatMethod;
import com.elvarg.game.content.combat.method.impl.specials.DarkBowCombatMethod;
import com.elvarg.game.content.combat.method.impl.specials.DragonClawCombatMethod;
import com.elvarg.game.content.combat.method.impl.specials.DragonDaggerCombatMethod;
import com.elvarg.game.content.combat.method.impl.specials.DragonHalberdCombatMethod;
import com.elvarg.game.content.combat.method.impl.specials.DragonLongswordCombatMethod;
import com.elvarg.game.content.combat.method.impl.specials.DragonMaceCombatMethod;
import com.elvarg.game.content.combat.method.impl.specials.DragonScimitarCombatMethod;
import com.elvarg.game.content.combat.method.impl.specials.DragonWarhammerCombatMethod;
import com.elvarg.game.content.combat.method.impl.specials.GraniteMaulCombatMethod;
import com.elvarg.game.content.combat.method.impl.specials.MagicShortbowCombatMethod;
import com.elvarg.game.content.combat.method.impl.specials.SaradominGodswordCombatMethod;
import com.elvarg.game.content.combat.method.impl.specials.SaradominSwordCombatMethod;
import com.elvarg.game.content.combat.method.impl.specials.ZamorakGodswordCombatMethod;
import com.elvarg.game.entity.impl.Mobile;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.container.impl.Equipment;
import com.elvarg.game.model.equipment.BonusManager;
import com.elvarg.game.task.TaskManager;
import com.elvarg.game.task.impl.RestoreSpecialAttackTask;

/**
 * Holds constants that hold data for all of the special attacks that can be
 * used.
 *
 * @author lare96
 */
public enum CombatSpecial {

	// Melee
	ABYSSAL_WHIP(new int[] { 4151, 21371, 15441, 15442, 15443, 15444 }, 50, 1.04, 1, new AbyssalWhipCombatMethod(),
			WeaponInterface.WHIP), ABYSSAL_TENTACLE(new int[] { 12006 }, 50, 1.07, 1, new AbyssalTentacleCombatMethod(),
					WeaponInterface.WHIP),

	BARRELSCHEST_ANCHOR(new int[] { 10887 }, 50, 1.22, 1.35, new BarrelchestAnchorCombatMethod(),
			WeaponInterface.WARHAMMER), DRAGON_SCIMITAR(new int[] { 4587 }, 55, 1.08, 1.1,
					new DragonScimitarCombatMethod(), WeaponInterface.SCIMITAR), DRAGON_LONGSWORD(new int[] { 1305 },
							25, 1.31, 1.33, new DragonLongswordCombatMethod(), WeaponInterface.LONGSWORD), DRAGON_MACE(
									new int[] { 1434 }, 25, 1.61, 1.25, new DragonMaceCombatMethod(),
									WeaponInterface.MACE), DRAGON_WARHAMMER(new int[] { 13576 }, 50, 1.67, 1.39,
											new DragonWarhammerCombatMethod(), WeaponInterface.WARHAMMER),

	SARADOMIN_SWORD(new int[] { 11838 }, 100, 1.1, 1.3, new SaradominSwordCombatMethod(),
			WeaponInterface.SARADOMIN_SWORD),

	ARMADYL_GODSWORD(new int[] { 11802 }, 50, 1.50, 1.6, new ArmadylGodswordCombatMethod(),
			WeaponInterface.GODSWORD), SARADOMIN_GODSWORD(new int[] { 11806 }, 50, 1.27, 1.5,
					new SaradominGodswordCombatMethod(), WeaponInterface.GODSWORD), BANDOS_GODSWORD(new int[] { 11804 },
							100, 1.22, 1.4, new BandosGodswordCombatMethod(),
							WeaponInterface.GODSWORD), ZAMORAK_GODSWORD(new int[] { 11808 }, 50, 1.15, 1.4,
									new ZamorakGodswordCombatMethod(), WeaponInterface.GODSWORD),

	ABYSSAL_BLUDGEON(new int[] { 13263 }, 50, 1.20, 1.37, new AbyssalBludgeonCombatMethod(),
			WeaponInterface.ABYSSAL_BLUDGEON),

	// Multiple hits
	DRAGON_HALBERD(new int[] { 3204 }, 30, 1, 1.19, new DragonHalberdCombatMethod(),
			WeaponInterface.HALBERD), DRAGON_DAGGER(new int[] { 1215, 1231, 5680, 5698 }, 25, 1.27, 1.3,
					new DragonDaggerCombatMethod(), WeaponInterface.DRAGON_DAGGER), ABYSSAL_DAGGER(new int[] { 13271 },
							50, 1.10, 1.37, new AbyssalDaggerCombatMethod(),
							WeaponInterface.ABYSSAL_DAGGER), GRANITE_MAUL(new int[] { 4153, 12848 }, 50, 1.12, 1.18,
									new GraniteMaulCombatMethod(),
									WeaponInterface.GRANITE_MAUL), DRAGON_CLAWS(new int[] { 13652 }, 50, 1.3, 1.53,
											new DragonClawCombatMethod(), WeaponInterface.CLAWS),

	// Ranged
	MAGIC_SHORTBOW(new int[] { 861 }, 55, 1.03, 1.2, new MagicShortbowCombatMethod(),
			WeaponInterface.SHORTBOW), DARK_BOW(new int[] { 11235 }, 55, 1.29, 1.24, new DarkBowCombatMethod(),
					WeaponInterface.DARK_BOW), ARMADYL_CROSSBOW(new int[] { 11785 }, 40, 1.30, 2.0,
							new ArmadylCrossbowCombatMethod(), WeaponInterface.CROSSBOW), BALLISTA(new int[] { 19481 },
									65, 1.30, 1.0, new BallistaCombatMethod(), WeaponInterface.BALLISTA),;

	/**
	 * The weapon ID's that perform this special when activated.
	 */
	private int[] identifiers;

	/**
	 * The amount of special energy this attack will drain.
	 */
	private int drainAmount;

	/**
	 * The strength bonus when performing this special attack.
	 */
	private double strengthBonus;

	/**
	 * The accuracy bonus when performing this special attack.
	 */
	private double accuracyBonus;

	/**
	 * The combat type used when performing this special attack.
	 */
	private CombatMethod combatMethod;

	/**
	 * The weapon interface used by the identifiers.
	 */
	private WeaponInterface weaponType;

	/**
	 * Create a new {@link CombatSpecial}.
	 *
	 * @param identifers
	 *            the weapon ID's that perform this special when activated.
	 * @param drainAmount
	 *            the amount of special energy this attack will drain.
	 * @param strengthBonus
	 *            the strength bonus when performing this special attack.
	 * @param accuracyBonus
	 *            the accuracy bonus when performing this special attack.
	 * @param combatMethod
	 *            the combat type used when performing this special attack.
	 * @param weaponType
	 *            the weapon interface used by the identifiers.
	 */
	private CombatSpecial(int[] identifiers, int drainAmount, double strengthBonus, double accuracyBonus,
			CombatMethod combatMethod, WeaponInterface weaponType) {
		this.identifiers = identifiers;
		this.drainAmount = drainAmount;
		this.strengthBonus = strengthBonus;
		this.accuracyBonus = accuracyBonus;
		this.combatMethod = combatMethod;
		this.weaponType = weaponType;
	}

	/**
	 * Checks if a player has the reqs to perform the special attack
	 *
	 * @param player
	 * @param special
	 * @return
	 */
	public static boolean checkSpecial(Player player, CombatSpecial special) {
		return (player.getCombatSpecial() != null && player.getCombatSpecial() == special && player.isSpecialActivated()
				&& player.getSpecialPercentage() >= special.getDrainAmount());
	}

	/**
	 * Drains the special bar for the argued {@link Mobile}.
	 *
	 * @param character
	 *            the character who's special bar will be drained.
	 * @param amount
	 *            the amount of energy to drain from the special bar.
	 */
	public static void drain(Mobile character, int amount) {
		character.decrementSpecialPercentage(amount);
		character.setSpecialActivated(false);

		if (!character.isRecoveringSpecialAttack()) {
			TaskManager.submit(new RestoreSpecialAttackTask(character));
		}

		if (character.isPlayer()) {
			Player p = character.getAsPlayer();
			CombatSpecial.updateBar(p);
		}
	}

	/**
	 * Updates the special bar with the amount of special energy the argued
	 * {@link Player} has.
	 *
	 * @param player
	 *            the player who's special bar will be updated.
	 */
	public static void updateBar(Player player) {
		if (player.getWeapon().getSpecialBar() == -1
				|| player.getWeapon().getSpecialMeter() == -1) {
			return;
		}
		int specialCheck = 10;
		int specialBar = player.getWeapon().getSpecialMeter();
		int specialAmount = player.getSpecialPercentage() / 10;

		for (int i = 0; i < 10; i++) {
			player.getPacketSender().sendInterfaceComponentMoval(specialAmount >= specialCheck ? 500 : 0, 0,
					--specialBar);
			specialCheck--;
		}
		player.getPacketSender().updateSpecialAttackOrb().sendString(player.getWeapon().getSpecialMeter(),
				player.isSpecialActivated() ? ("@yel@ Special Attack (" + player.getSpecialPercentage() + "%)")
						: ("@bla@ Special Attack (" + player.getSpecialPercentage() + "%)"));
		player.getPacketSender().sendSpecialAttackState(player.isSpecialActivated());
	}

	/**
	 * Assigns special bars to the attack style interface if needed.
	 *
	 * @param player
	 *            the player to assign the special bar for.
	 */
	public static void assign(Player player) {
		if (player.getWeapon().getSpecialBar() == -1) {
			player.setSpecialActivated(false);
			player.setCombatSpecial(null);
			CombatSpecial.updateBar(player);
			return;
		}

		for (CombatSpecial c : CombatSpecial.values()) {
			if (player.getWeapon() == c.getWeaponType()) {
				if (Arrays.stream(c.getIdentifiers())
						.anyMatch(id -> player.getEquipment().get(Equipment.WEAPON_SLOT).getId() == id)) {
					player.getPacketSender().sendInterfaceDisplayState(player.getWeapon().getSpecialBar(),
							false);
					player.setCombatSpecial(c);
					return;
				}
			}
		}

		player.getPacketSender().sendInterfaceDisplayState(player.getWeapon().getSpecialBar(), true);
		player.setCombatSpecial(null);
		player.setSpecialActivated(false);
		player.getPacketSender().sendSpecialAttackState(false);
	}

	/**
	 * Activates a player's special attack.
	 *
	 * @param player
	 */
	public static void activate(Player player) {

		// Make sure the player has a valid special attack
		if (player.getCombatSpecial() == null) {
			return;
		}

		// Duel, disabled special attacks?
		if (player.getDueling().inDuel() && player.getDueling().getRules()[DuelRule.NO_SPECIAL_ATTACKS.ordinal()]) {
		//	DialogueManager.sendStatement(player, "Special attacks have been disabled in this duel!");
			return;
		}

		// Check if player has already activated special attack,
		// If that's the case - turn if off.
		if (player.isSpecialActivated()) {
			player.setSpecialActivated(false);
			CombatSpecial.updateBar(player);
		} else {

			// Get the special attack..
			final CombatSpecial spec = player.getCombatSpecial();

			// Set special attack activated
			player.setSpecialActivated(true);

			// Update special bar
			CombatSpecial.updateBar(player);

			// Handle instant special attacks here.
			// Example: Granite Maul, Dragon battleaxe...
			if (spec == CombatSpecial.GRANITE_MAUL) {

				// Make sure the player has enough special attack
				if (player.getSpecialPercentage() < player.getCombatSpecial().getDrainAmount()) {
					player.getPacketSender().sendMessage("You do not have enough special attack energy left!");
					player.setSpecialActivated(false);
					CombatSpecial.updateBar(player);
					return;
				}

				// Check if the player is attacking and using Melee..
				Mobile target = player.getCombat().getTarget();
				if (target != null && CombatFactory.getMethod(player).type() == CombatType.MELEE) {
					// Perform an immediate attack
					player.getCombat().performNewAttack(true);
					return;
				} else {

					// Uninformed player using gmaul without being in combat..
					// Teach them a lesson!
					player.getPacketSender()
							.sendMessage("Although not required, the Granite maul special attack should be used during")
							.sendMessage("combat for maximum effect.");
				}
			} /*
				 * else if(spec == CombatSpecial.DRAGON_BATTLEAXE) {
				 * 
				 * }
				 */

		}

		if (player.getInterfaceId() == BonusManager.INTERFACE_ID) {
			BonusManager.update(player);
		}
	}

	/**
	 * Gets the weapon ID's that perform this special when activated.
	 *
	 * @return the weapon ID's that perform this special when activated.
	 */
	public int[] getIdentifiers() {
		return identifiers;
	}

	/**
	 * Gets the amount of special energy this attack will drain.
	 *
	 * @return the amount of special energy this attack will drain.
	 */
	public int getDrainAmount() {
		return drainAmount;
	}

	/**
	 * Gets the strength bonus when performing this special attack.
	 *
	 * @return the strength bonus when performing this special attack.
	 */
	public double getStrengthBonus() {
		return strengthBonus;
	}

	/**
	 * Gets the accuracy bonus when performing this special attack.
	 *
	 * @return the accuracy bonus when performing this special attack.
	 */
	public double getAccuracyBonus() {
		return accuracyBonus;
	}

	/**
	 * Gets the combat type used when performing this special attack.
	 *
	 * @return the combat type used when performing this special attack.
	 */
	public CombatMethod getCombatMethod() {
		return combatMethod;
	}

	/**
	 * Gets the weapon interface used by the identifiers.
	 *
	 * @return the weapon interface used by the identifiers.
	 */
	public WeaponInterface getWeaponType() {
		return weaponType;
	}
}
