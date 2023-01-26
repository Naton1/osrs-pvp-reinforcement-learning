package com.elvarg.net.packet.impl;

import com.elvarg.Server;
import com.elvarg.game.GameConstants;
import com.elvarg.game.content.Dueling.DuelRule;
import com.elvarg.game.content.combat.CombatSpecial;
import com.elvarg.game.content.combat.WeaponInterfaces;
import com.elvarg.game.content.combat.magic.Autocasting;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.Flag;
import com.elvarg.game.model.Item;
import com.elvarg.game.model.ItemInSlot;
import com.elvarg.game.model.Skill;
import com.elvarg.game.model.container.impl.Equipment;
import com.elvarg.game.model.container.impl.Inventory;
import com.elvarg.game.model.equipment.BonusManager;
import com.elvarg.net.packet.Packet;
import com.elvarg.net.packet.PacketExecutor;
import com.elvarg.util.Misc;

/**
 * This packet listener manages the equip action a player executes when wielding
 * or equipping an item.
 *
 * @author relex lawl
 */

public class EquipPacketListener implements PacketExecutor {

	public static void resetWeapon(Player player, boolean deactivateSpecialAttack) {
		if (deactivateSpecialAttack) {
			player.setSpecialActivated(false);
		}
		player.getPacketSender().sendSpecialAttackState(false);
		WeaponInterfaces.assign(player);
	}

	@Override
	public void execute(Player player, Packet packet) {
		int id = packet.readShort();
		int slot = packet.readShortA();
		int interfaceId = packet.readShortA();

		EquipPacketListener.equip(player, id, slot, interfaceId);
	}

	public static void equipFromInventory(Player player, ItemInSlot itemInSlot) {
		EquipPacketListener.equip(player, itemInSlot.getId(), itemInSlot.getSlot(), Inventory.INTERFACE_ID);
	}

	public static void equip(Player player, int id, int slot, int interfaceId) {

		// Validate player..
		if (player == null || player.getHitpoints() <= 0) {
			return;
		}

		// Validate slot..
		if (slot < 0 || slot >= player.getInventory().capacity()) {
			return;
		}

		// Check if the item in the slot matches the one requested to be wielded..
		Item item = player.getInventory().getItems()[slot].clone();
		if (item.getId() != id) {
			return;
		}

		// Close all other interfaces except for the {@code
		// Equipment.EQUIPMENT_SCREEN_INTERFACE_ID} one..
		if (player.getInterfaceId() > 0 && player.getInterfaceId() != Equipment.EQUIPMENT_SCREEN_INTERFACE_ID) {
			player.getPacketSender().sendInterfaceRemoval();
		}

		// Stop skilling..
		player.getSkillManager().stopSkillable();

		switch (interfaceId) {
			case Inventory.INTERFACE_ID:
				// Check if player can wield the item..
				if (item.getDefinition().getRequirements() != null) {
					for (Skill skill : Skill.values()) {
						if (item.getDefinition().getRequirements()[skill.ordinal()] > player.getSkillManager()
								.getMaxLevel(skill)) {
							StringBuilder vowel = new StringBuilder();
							if (skill.getName().startsWith("a") || skill.getName().startsWith("e")
									|| skill.getName().startsWith("i") || skill.getName().startsWith("o")
									|| skill.getName().startsWith("u")) {
								vowel.append("an ");
							} else {
								vowel.append("a ");
							}
							player.getPacketSender()
									.sendMessage("You need " + vowel.toString() + Misc.formatText(skill.getName())
											+ " level of at least "
											+ item.getDefinition().getRequirements()[skill.ordinal()] + " to wear this.");
							return;
						}
					}
				}

				// Check if the item has a proper equipment slot..
				int equipmentSlot = item.getDefinition().getEquipmentType().getSlot();
				if (equipmentSlot == -1) {
					Server.getLogger()
							.info("Attempting to equip item " + item.getId() + " which has no defined equipment slot.");
					return;
				}

				// Handle area equipping behavior
				if (player.getArea() != null && !player.getArea().canEquipItem(player, equipmentSlot, item)) {
					return;
				}

				// Handle duel arena settings..
				if (player.getDueling().inDuel()) {
					for (int i = 11; i < player.getDueling().getRules().length; i++) {
						if (player.getDueling().getRules()[i]) {
							DuelRule duelRule = DuelRule.forId(i);
							if (equipmentSlot == duelRule.getEquipmentSlot()
									|| duelRule == DuelRule.NO_SHIELD && item.getDefinition().isDoubleHanded()) {
								///DialogueManager.sendStatement(player, "The rules that were set do not allow this item to be equipped.");
								return;
							}
						}
					}
					if (equipmentSlot == Equipment.WEAPON_SLOT || item.getDefinition().isDoubleHanded()) {
						if (player.getDueling().getRules()[DuelRule.LOCK_WEAPON.ordinal()]) {
							////DialogueManager.sendStatement(player, "Weapons have been locked in this duel!");
							return;
						}
					}
				}

				Item equipItem = player.getEquipment().forSlot(equipmentSlot).clone();
				if (equipItem.getDefinition().isStackable() && equipItem.getId() == item.getId()) {
					int amount = equipItem.getAmount() + item.getAmount() <= Integer.MAX_VALUE
							? equipItem.getAmount() + item.getAmount()
							: Integer.MAX_VALUE;
					player.getInventory().delete(item, false);
					player.getEquipment().getItems()[equipmentSlot].setAmount(amount);
					equipItem.setAmount(amount);
				} else {
					if (item.getDefinition().isDoubleHanded() && equipmentSlot == Equipment.WEAPON_SLOT) {

						int slotsRequired = player.getEquipment().isSlotOccupied(Equipment.SHIELD_SLOT)
								&& player.getEquipment().isSlotOccupied(Equipment.WEAPON_SLOT) ? 1 : 0;
						if (player.getInventory().getFreeSlots() < slotsRequired) {
							player.getInventory().full();
							return;
						}

						Item shield = player.getEquipment().getItems()[Equipment.SHIELD_SLOT];
						Item weapon = player.getEquipment().getItems()[Equipment.WEAPON_SLOT];
						player.getEquipment().set(Equipment.SHIELD_SLOT, new Item(-1, 0));
						// player.getInventory().delete(item);
						player.getEquipment().set(equipmentSlot, item);

						if (weapon.getId() != -1) {
							player.getInventory().setItem(slot, weapon);
						} else
							player.getInventory().delete(item);

						if (shield.getId() != -1) {
							player.getInventory().add(shield);
						}

					} else if (equipmentSlot == Equipment.SHIELD_SLOT
							&& player.getEquipment().getItems()[Equipment.WEAPON_SLOT].getDefinition().isDoubleHanded()) {
						player.getInventory().setItem(slot, player.getEquipment().getItems()[Equipment.WEAPON_SLOT]);
						player.getEquipment().setItem(Equipment.WEAPON_SLOT, new Item(-1));
						player.getEquipment().setItem(Equipment.SHIELD_SLOT, item);
						resetWeapon(player, true);
					} else {
						if (equipmentSlot == equipItem.getDefinition().getEquipmentType().getSlot()
								&& equipItem.getId() != -1) {
							if (player.getInventory().contains(equipItem.getId())) {
								player.getInventory().delete(item, false);
								player.getInventory().add(equipItem, false);
							} else {
								player.getInventory().setItem(slot, equipItem);
							}
							player.getEquipment().setItem(equipmentSlot, item);
						} else {
							player.getInventory().setItem(slot, new Item(-1, 0));
							player.getEquipment().setItem(equipmentSlot, item);
						}
					}
				}

				if (equipmentSlot == Equipment.WEAPON_SLOT) {
					resetWeapon(player, true);
				}

				if (player.getEquipment().get(Equipment.WEAPON_SLOT).getId() != 4153) {
					player.getCombat().reset();
				}

				BonusManager.update(player);
				player.getEquipment().refreshItems();

				// Refresh inventory
				if (GameConstants.QUEUE_SWITCHING_REFRESH) {
					player.setUpdateInventory(true);
				} else {
					player.getInventory().refreshItems();
				}

				player.getUpdateFlag().flag(Flag.APPEARANCE);
				break;
		}
	}
}