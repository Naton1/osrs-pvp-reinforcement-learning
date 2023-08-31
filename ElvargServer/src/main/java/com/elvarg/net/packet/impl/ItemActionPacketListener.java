package com.elvarg.net.packet.impl;

import com.elvarg.game.GameConstants;
import com.elvarg.game.content.Food;
import com.elvarg.game.content.Gambling;
import com.elvarg.game.content.PotionConsumable;
import com.elvarg.game.content.combat.CombatSpecial;
import com.elvarg.game.content.minigames.impl.Barrows;
import com.elvarg.game.content.skill.skillable.impl.Herblore;
import com.elvarg.game.content.skill.skillable.impl.Prayer;
import com.elvarg.game.content.skill.skillable.impl.Runecrafting;
import com.elvarg.game.definition.ItemDefinition;
import com.elvarg.game.entity.impl.npc.impl.Barricades;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.Animation;
import com.elvarg.game.model.BarrowsSet;
import com.elvarg.game.model.areas.impl.WildernessArea;
import com.elvarg.game.model.teleportation.TeleportHandler;
import com.elvarg.game.model.teleportation.TeleportTablets;
import com.elvarg.game.model.teleportation.TeleportType;
import com.elvarg.game.task.Task;
import com.elvarg.game.task.TaskManager;
import com.elvarg.net.packet.Packet;
import com.elvarg.net.packet.PacketConstants;
import com.elvarg.net.packet.PacketExecutor;
import com.elvarg.util.ItemIdentifiers;

import static com.elvarg.game.content.skill.skillable.impl.woodcutting.BirdNest.handleSearchNest;


public class ItemActionPacketListener implements PacketExecutor {

	private static void firstAction(final Player player, Packet packet) {
		@SuppressWarnings("unused")
		int interfaceId = packet.readUnsignedShort();
		int itemId = packet.readShort();
		int slot = packet.readShort();

		if (slot < 0 || slot > player.getInventory().capacity())
			return;
		if (player.getInventory().getItems()[slot].getId() != itemId)
			return;

		if (player.isTeleporting() || player.getHitpoints() <= 0)
			return;

		player.getPacketSender().sendInterfaceRemoval();

		// Herblore
		if (Herblore.cleanHerb(player, itemId)) {
			return;
		}

		if (itemId == Barricades.ITEM_ID && Barricades.canSetup(player)) {
			return;
		}

		if (itemId == ItemIdentifiers.CANNON_BASE) {
			player.getDwarfCannon().setup();
			return;
		}

		// Prayer
		if (Prayer.buryBone(player, itemId)) {
			return;
		}

		// Eating food..
		if (Food.consume(player, itemId, slot)) {
			return;
		}

		// Drinking potions..
		if (PotionConsumable.drink(player, itemId, slot)) {
			return;
		}

		// Runecrafting pouches..
		if (Runecrafting.handlePouch(player, itemId, 1)) {
			return;
		}

		// Teleport tablets..
		if (TeleportTablets.init(player, itemId)) {
			return;
		}

		switch (itemId) {
			case ItemIdentifiers.BIRD_NEST:
			case ItemIdentifiers.BIRD_NEST_2:
			case ItemIdentifiers.BIRD_NEST_3:
			case ItemIdentifiers.BIRD_NEST_4:
			case ItemIdentifiers.BIRD_NEST_5:
				handleSearchNest(player, itemId);
				break;
		case ItemIdentifiers.SPADE:
			player.performAnimation(new Animation(830));
			TaskManager.submit(new Task(1, player, false) {
				@Override
				protected void execute() {
					if (!player.isTeleporting()) {
						Barrows.dig(player);
					}
					stop();
				}
			});
			break;
		case Gambling.MITHRIL_SEEDS:
			Gambling.plantFlower(player);
			break;
		case 9520:
			if (!(player.getArea() instanceof WildernessArea)) {
				if (player.getSpecialPercentage() < 100) {
					player.getPacketSender().sendInterfaceRemoval();
					player.performAnimation(new Animation(829));
					player.getInventory().delete(9520, 1);
					player.setSpecialPercentage(100);
					CombatSpecial.updateBar(player);
					player.getPacketSender().sendMessage("You now have 100% special attack energy.");
				} else {
					player.getPacketSender().sendMessage("You already have full special attack energy!");
				}
			} else {
				player.getPacketSender().sendMessage("You cannot use this in the Wilderness!");
			}
			break;
		case ItemIdentifiers.TELEPORT_TO_HOUSE:
			if (TeleportHandler.checkReqs(player, GameConstants.DEFAULT_LOCATION)) {
				TeleportHandler.teleport(player, GameConstants.DEFAULT_LOCATION, TeleportType.TELE_TAB, false);
				player.getInventory().delete(ItemIdentifiers.TELEPORT_TO_HOUSE, 1);
			}
			break;
		case 2542:
		case 2543:
		case 2544:
			if (player.busy()) {
				player.getPacketSender().sendMessage("You cannot do that right now.");
				return;
			}
			if (itemId == 2542 && player.isPreserveUnlocked() || itemId == 2543 && player.isRigourUnlocked()
					|| itemId == 2544 && player.isAuguryUnlocked()) {
				player.getPacketSender().sendMessage("You have already unlocked that prayer.");
				return;
			}
			/*DialogueManager.start(player, 9);
			player.setDialogueOptions(new DialogueOptions() {
				@Override
				public void handleOption(Player player, int option) {
					if (option == 1) {
						player.getInventory().delete(itemId, 1);

						if (itemId == 2542)
							player.setPreserveUnlocked(true);
						else if (itemId == 2543)
							player.setRigourUnlocked(true);
						else if (itemId == 2544)
							player.setAuguryUnlocked(true);
						player.getPacketSender().sendConfig(709,
								PrayerHandler.canUse(player, PrayerData.PRESERVE, false) ? 1 : 0);
						player.getPacketSender().sendConfig(711,
								PrayerHandler.canUse(player, PrayerData.RIGOUR, false) ? 1 : 0);
						player.getPacketSender().sendConfig(713,
								PrayerHandler.canUse(player, PrayerData.AUGURY, false) ? 1 : 0);
						player.getPacketSender().sendMessage("You have unlocked a new prayer.");
					}
					player.getPacketSender().sendInterfaceRemoval();
				}
			});*/
			break;
		case 2545:
			if (player.busy()) {
				player.getPacketSender().sendMessage("You cannot do that right now.");
				return;
			}
			if (player.isTargetTeleportUnlocked()) {
				player.getPacketSender().sendMessage("You have already unlocked that teleport.");
				return;
			}
			/*DialogueManager.start(player, 12);
			player.setDialogueOptions(new DialogueOptions() {
				@Override
				public void handleOption(Player player, int option) {
					if (option == 1) {
						player.getInventory().delete(itemId, 1);
						player.setTargetTeleportUnlocked(true);
						player.getPacketSender().sendMessage("You have unlocked a new teleport.");
					}
					player.getPacketSender().sendInterfaceRemoval();
				}
			});*/
			break;
		case 12873:
		case 12875:
		case 12879:
		case 12881:
		case 12883:
		case 12877:
			BarrowsSet set = BarrowsSet.get(itemId);
			if (set != null) {
				if (!player.getInventory().contains(set.getSetId())) {
					return;
				}
				if ((player.getInventory().getFreeSlots() - 1) < set.getItems().length) {
					player.getPacketSender().sendMessage(
							"You need at least " + set.getItems().length + " free inventory slots to do that.");
					return;
				}
				player.getInventory().delete(set.getSetId(), 1);
				for (int item : set.getItems()) {
					player.getInventory().add(item, 1);
				}
				player.getPacketSender()
						.sendMessage("You've opened your " + ItemDefinition.forId(itemId).getName() + ".");
			}
			break;
		}
	}

	public static void secondAction(Player player, Packet packet) {
		@SuppressWarnings("unused")
		int interfaceId = packet.readLEShortA();
		int slot = packet.readLEShort();
		int itemId = packet.readShortA();
		if (slot < 0 || slot >= player.getInventory().capacity())
			return;
		if (player.getInventory().getItems()[slot].getId() != itemId)
			return;

		if (Runecrafting.handleTalisman(player, itemId)) {
			return;
		}
		if (Runecrafting.handlePouch(player, itemId, 2)) {
			return;
		}

		switch (itemId) {
		case 2550:
			/*player.setDialogueOptions(new DialogueOptions() {
				@Override
				public void handleOption(Player player, int option) {
					player.getPacketSender().sendInterfaceRemoval();
					if (option == 1) {
						if (player.getInventory().contains(2550)) {
							player.getInventory().delete(2550, 1);
							player.setRecoilDamage(0);
							player.getPacketSender().sendMessage("Your Ring of recoil has degraded.");
						}
					}
				}
			});
			player.setDialogue(DialogueManager.getDialogues().get(10)); // Yes / no option
			DialogueManager.sendStatement(player,
					"You still have " + (40 - player.getRecoilDamage()) + " damage before it breaks. Continue?");*/
			break;
		}
	}

	public void thirdClickAction(Player player, Packet packet) {
		int itemId = packet.readShortA();
		int slot = packet.readLEShortA();
		@SuppressWarnings("unused")
		int interfaceId = packet.readLEShortA();
		if (slot < 0 || slot >= player.getInventory().capacity())
			return;
		if (player.getInventory().getItems()[slot].getId() != itemId)
			return;

		if (BarrowsSet.pack(player, itemId)) {
			return;
		}
		if (Runecrafting.handlePouch(player, itemId, 3)) {
			return;
		}

		switch (itemId) {
		case 12926:
			player.getPacketSender()
					.sendMessage("Your Toxic blowpipe has " + player.getBlowpipeScales() + " Zulrah scales left.");
			break;
		}
	}

	@Override
	public void execute(Player player, Packet packet) {
		if (player == null || player.getHitpoints() <= 0)
			return;
		switch (packet.getOpcode()) {
		case PacketConstants.SECOND_ITEM_ACTION_OPCODE:
			secondAction(player, packet);
			break;
		case PacketConstants.FIRST_ITEM_ACTION_OPCODE:
			firstAction(player, packet);
			break;
		case PacketConstants.THIRD_ITEM_ACTION_OPCODE:
			thirdClickAction(player, packet);
			break;
		}
	}
}