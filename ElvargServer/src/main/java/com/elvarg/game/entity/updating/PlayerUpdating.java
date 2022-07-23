package com.elvarg.game.entity.updating;

import com.elvarg.game.World;
import com.elvarg.game.definition.ItemDefinition;
import com.elvarg.game.entity.impl.Mobile;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.entity.impl.playerbot.PlayerBot;
import com.elvarg.game.model.*;
import com.elvarg.game.model.areas.AreaManager;
import com.elvarg.game.model.container.impl.Equipment;
import com.elvarg.net.packet.ByteOrder;
import com.elvarg.net.packet.PacketBuilder;
import com.elvarg.net.packet.PacketBuilder.AccessType;
import com.elvarg.net.packet.PacketType;
import com.elvarg.net.packet.ValueType;

import java.util.Iterator;

/**
 * Represents the associated player's player updating.
 *
 * @author Professor Oak
 * @author relex lawl
 */

public class PlayerUpdating {

	/**
	 * The max amount of players that can be added per cycle.
	 */
	private static final int MAX_NEW_PLAYERS_PER_CYCLE = 25;

	/**
	 * Loops through the associated player's {@code localPlayer} list and updates
	 * them.
	 *
	 * @return The PlayerUpdating instance.
	 */

	public static void update(final Player player) {
		PacketBuilder update = new PacketBuilder();
		PacketBuilder packet = new PacketBuilder(81, PacketType.VARIABLE_SHORT);
		packet.initializeAccess(AccessType.BIT);
		updateMovement(player, packet);
		appendUpdates(player, update, player, false, true);
		packet.putBits(8, player.getLocalPlayers().size());
		for (Iterator<Player> playerIterator = player.getLocalPlayers().iterator(); playerIterator.hasNext();) {
			Player otherPlayer = playerIterator.next();
			if (World.getPlayers().get(otherPlayer.getIndex()) != null
					&& otherPlayer.getLocation().isViewableFrom(player.getLocation()) 
					&& !otherPlayer.isNeedsPlacement()
					&& otherPlayer.getPrivateArea() == player.getPrivateArea()) {
				updateOtherPlayerMovement(packet, otherPlayer);
				if (otherPlayer.getUpdateFlag().isUpdateRequired()) {
					appendUpdates(player, update, otherPlayer, false, false);
				}
			} else {
				playerIterator.remove();
				packet.putBits(1, 1);
				packet.putBits(2, 3);
			}
		}
		int playersAdded = 0;

		for (Player otherPlayer : World.getPlayers()) {
			if (player.getLocalPlayers().size() >= 79 || playersAdded > MAX_NEW_PLAYERS_PER_CYCLE)
				break;
			if (otherPlayer == null || otherPlayer == player || player.getLocalPlayers().contains(otherPlayer)
					|| !otherPlayer.getLocation().isViewableFrom(player.getLocation())
					|| otherPlayer.getPrivateArea() != player.getPrivateArea()) {
				continue;
			}
			player.getLocalPlayers().add(otherPlayer);
			addPlayer(player, otherPlayer, packet);
			appendUpdates(player, update, otherPlayer, true, false);
			playersAdded++;
		}

		if (update.buffer().writerIndex() > 0) {
			packet.putBits(11, 2047);
			packet.initializeAccess(AccessType.BYTE);
			packet.putBytes(update.buffer());
		} else {
			packet.initializeAccess(AccessType.BYTE);
		}
		player.getSession().write(packet);
	}

	/**
	 * Adds a new player to the associated player's client.
	 *
	 * @param target
	 *            The player to add to the other player's client.
	 * @param builder
	 *            The packet builder to write information on.
	 * @return The PlayerUpdating instance.
	 */
	private static void addPlayer(Player player, Player target, PacketBuilder builder) {
		builder.putBits(11, target.getIndex());
		builder.putBits(1, 1);
		builder.putBits(1, 1);
		int yDiff = target.getLocation().getY() - player.getLocation().getY();
		int xDiff = target.getLocation().getX() - player.getLocation().getX();
		builder.putBits(5, yDiff);
		builder.putBits(5, xDiff);
	}

	/**
	 * Updates the associated player's movement queue.
	 *
	 * @param builder
	 *            The packet builder to write information on.
	 * @return The PlayerUpdating instance.
	 */
	private static void updateMovement(Player player, PacketBuilder builder) {
		/*
		 * Check if the player is teleporting.
		 */
		if (player.isNeedsPlacement()) {
			/*
			 * They are, so an update is required.
			 */
			builder.putBits(1, 1);

			/*
			 * This value indicates the player teleported.
			 */
			builder.putBits(2, 3);

			/*
			 * This is the new player height.
			 */
			builder.putBits(2, player.getLocation().getZ());

			/*
			 * This indicates that the client should discard the walking queue.
			 */
			builder.putBits(1, player.isResetMovementQueue() ? 1 : 0);

			/*
			 * This flag indicates if an update block is appended.
			 */
			builder.putBits(1, player.getUpdateFlag().isUpdateRequired() ? 1 : 0);

			/*
			 * These are the positions.
			 */
			builder.putBits(7, player.getLocation().getLocalY(player.getLastKnownRegion()));
			builder.putBits(7, player.getLocation().getLocalX(player.getLastKnownRegion()));
		} else
		/*
		 * Otherwise, check if the player moved.
		 */
		if (player.getWalkingDirection().getId() == -1) {
			/*
			 * The player didn't move. Check if an update is required.
			 */
			if (player.getUpdateFlag().isUpdateRequired()) {
				/*
				 * Signifies an update is required.
				 */
				builder.putBits(1, 1);

				/*
				 * But signifies that we didn't move.
				 */
				builder.putBits(2, 0);
			} else
				/*
				 * Signifies that nothing changed.
				 */
				builder.putBits(1, 0);
		} else /*
				 * Check if the player was running.
				 */
		if (player.getRunningDirection().getId() == -1) {
			/*
			 * The player walked, an update is required.
			 */
			builder.putBits(1, 1);

			/*
			 * This indicates the player only walked.
			 */
			builder.putBits(2, 1);

			/*
			 * This is the player's walking direction.
			 */

			builder.putBits(3, player.getWalkingDirection().getId());

			/*
			 * This flag indicates an update block is appended.
			 */
			builder.putBits(1, player.getUpdateFlag().isUpdateRequired() ? 1 : 0);
		} else {

			/*
			 * The player ran, so an update is required.
			 */
			builder.putBits(1, 1);

			/*
			 * This indicates the player ran.
			 */
			builder.putBits(2, 2);

			/*
			 * This is the walking direction.
			 */
			builder.putBits(3, player.getWalkingDirection().getId());

			/*
			 * And this is the running direction.
			 */
			builder.putBits(3, player.getRunningDirection().getId());

			/*
			 * And this flag indicates an update block is appended.
			 */
			builder.putBits(1, player.getUpdateFlag().isUpdateRequired() ? 1 : 0);
		}
	}

	/**
	 * Updates another player's movement queue.
	 *
	 * @param builder
	 *            The packet builder to write information on.
	 * @param target
	 *            The player to update movement for.
	 * @return The PlayerUpdating instance.
	 */
	private static void updateOtherPlayerMovement(PacketBuilder builder, Player target) {

		// TODO: Teleport
		/*if (target.isNeedsPlacement()) {
			builder.putBits(1, target.getUpdateFlag().isUpdateRequired() ? 1 : 0);
			builder.putBits(2, 3); // Teleport
			builder.putBits(7, target.getPosition().getLocalY(target.getLastKnownRegion()));
			builder.putBits(7, target.getPosition().getLocalX(target.getLastKnownRegion()));
			return;
		}*/

		/*
		 * Check which type of movement took place.
		 */
		if (target.getWalkingDirection().getId() == -1) {
			/*
			 * If no movement did, check if an update is required.
			 */
			if (target.getUpdateFlag().isUpdateRequired()) {
				/*
				 * Signify that an update happened.
				 */
				builder.putBits(1, 1);

				/*
				 * Signify that there was no movement.
				 */
				builder.putBits(2, 0);
			} else {
				/*
				 * Signify that nothing changed.
				 */
				builder.putBits(1, 0);
			}
		} else if (target.getRunningDirection().getId() == -1) {
			/*
			 * The player moved but didn't run. Signify that an update is required.
			 */
			builder.putBits(1, 1);

			/*
			 * Signify we moved one tile.
			 */
			builder.putBits(2, 1);

			/*
			 * Write the primary sprite (i.e. walk direction).
			 */
			builder.putBits(3, target.getWalkingDirection().getId());

			/*
			 * Write a flag indicating if a block update happened.
			 */
			builder.putBits(1, target.getUpdateFlag().isUpdateRequired() ? 1 : 0);
		} else {
			/*
			 * The player ran. Signify that an update happened.
			 */
			builder.putBits(1, 1);

			/*
			 * Signify that we moved two tiles.
			 */
			builder.putBits(2, 2);

			/*
			 * Write the primary sprite (i.e. walk direction).
			 */
			builder.putBits(3, target.getWalkingDirection().getId());

			/*
			 * Write the secondary sprite (i.e. run direction).
			 */
			builder.putBits(3, target.getRunningDirection().getId());

			/*
			 * Write a flag indicating if a block update happened.
			 */
			builder.putBits(1, target.getUpdateFlag().isUpdateRequired() ? 1 : 0);
		}
	}

	/**
	 * Appends a player's update mask blocks.
	 *
	 * @param builder
	 *            The packet builder to write information on.
	 * @param target
	 *            The player to update masks for.
	 * @param updateAppearance
	 *            Update the player's appearance without the flag being set?
	 * @param noChat
	 *            Do not allow player to chat?
	 * @return The PlayerUpdating instance.
	 */
	private static void appendUpdates(Player player, PacketBuilder builder, Player target, boolean updateAppearance,
			boolean noChat) {
		if (!target.getUpdateFlag().isUpdateRequired() && !updateAppearance)
			return;

		// If we don't need to update again, simply send the cached update block
		// if it's available.
		/*
		 * if (player.getCachedUpdateBlock() != null && !player.equals(target) &&
		 * !updateAppearance && !noChat) {
		 * builder.putBytes(player.getCachedUpdateBlock()); return; }
		 */

		final UpdateFlag flag = target.getUpdateFlag();
		int mask = 0;
		if (flag.flagged(Flag.GRAPHIC) && target.getGraphic() != null) {
			mask |= 0x100;
		}
		if (flag.flagged(Flag.ANIMATION) && target.getAnimation() != null) {
			mask |= 0x8;
		}
		if (flag.flagged(Flag.FORCED_CHAT) && target.getForcedChat() != null) {
			mask |= 0x4;
		}
		if (flag.flagged(Flag.CHAT) && target.getCurrentChatMessage() != null && !noChat
				&& !player.getRelations().getIgnoreList().contains(target.getLongUsername())) {
			mask |= 0x80;
		}
		if (flag.flagged(Flag.ENTITY_INTERACTION)) {
			mask |= 0x1;
		}
		if (flag.flagged(Flag.APPEARANCE) || updateAppearance) {
			mask |= 0x10;
		}
		if (flag.flagged(Flag.FACE_POSITION) && target.getPositionToFace() != null) {
			mask |= 0x2;
		}
		if (flag.flagged(Flag.SINGLE_HIT)) {
			mask |= 0x20;
		}
		if (flag.flagged(Flag.DOUBLE_HIT)) {
			mask |= 0x200;
		}
		if (flag.flagged(Flag.FORCED_MOVEMENT) && target.getForceMovement() != null) {
			mask |= 0x400;
		}
		if (mask >= 0x100) {
			mask |= 0x40;
			builder.putShort(mask, ByteOrder.LITTLE);
		} else {
			builder.put(mask);
		}
		if (flag.flagged(Flag.FORCED_MOVEMENT) && target.getForceMovement() != null) {
			updateForcedMovement(player, builder, target);
		}
		if (flag.flagged(Flag.GRAPHIC) && target.getGraphic() != null) {
			updateGraphics(builder, target);
		}
		if (flag.flagged(Flag.ANIMATION) && target.getAnimation() != null) {
			updateAnimation(builder, target);
		}
		if (flag.flagged(Flag.FORCED_CHAT) && target.getForcedChat() != null) {
			updateForcedChat(builder, target);
		}
		if (flag.flagged(Flag.CHAT) && target.getCurrentChatMessage() != null && !noChat
				&& !player.getRelations().getIgnoreList().contains(target.getLongUsername())) {
			updateChat(builder, target, player);
		}
		if (flag.flagged(Flag.ENTITY_INTERACTION)) {
			updateEntityInteraction(builder, target);
		}
		if (flag.flagged(Flag.APPEARANCE) || updateAppearance) {
			updateAppearance(player, builder, target);
		}
		if (flag.flagged(Flag.FACE_POSITION) && target.getPositionToFace() != null) {
			updateFacingPosition(builder, target);
		}
		if (flag.flagged(Flag.SINGLE_HIT)) {
			updateSingleHit(builder, target);
		}
		if (flag.flagged(Flag.DOUBLE_HIT)) {
			updateDoubleHit(builder, target);
		}
		/*if (!player.equals(target) && !updateAppearance && !noChat) {
			player.setCachedUpdateBlock(builder.buffer());
		}*/
	}

	/**
	 * This update block is used to update player chat.
	 *
	 * @param builder
	 *            The packet builder to write information on.
	 * @param target
	 *            The player to update chat for.
	 * @return The PlayerUpdating instance.
	 */
	private static void updateChat(PacketBuilder builder, Player target, Player receiver) {
		ChatMessage message = target.getCurrentChatMessage();
		byte[] bytes = message.getText();
		builder.putShort(((message.getColour() & 0xff) << 8) | (message.getEffects() & 0xff), ByteOrder.LITTLE);
		builder.put(target.getRights().ordinal());
		builder.put(target.getDonatorRights().ordinal());
        builder.put(bytes.length, ValueType.C);
        for (int ptr = bytes.length - 1; ptr >= 0; ptr--) {
            builder.put(bytes[ptr]);
        }

		if (receiver instanceof PlayerBot && !(target instanceof PlayerBot)) {
			// Player Bots: Automatically listen to chat messages
			((PlayerBot) receiver).getChatInteraction().heard(message, target);
		}
	}

	/**
	 * This update block is used to update forced player chat.
	 *
	 * @param builder
	 *            The packet builder to write information on.
	 * @param target
	 *            The player to update forced chat for.
	 * @return The PlayerUpdating instance.
	 */
	private static void updateForcedChat(PacketBuilder builder, Player target) {
		builder.putString(target.getForcedChat());
	}

	/**
	 * This update block is used to update forced player movement.
	 *
	 * @param builder
	 *            The packet builder to write information on.
	 * @param target
	 *            The player to update forced movement for.
	 * @return The PlayerUpdating instance.
	 */
	private static void updateForcedMovement(Player player, PacketBuilder builder, Player target) {
		int startX = target.getForceMovement().getStart().getLocalX(player.getLastKnownRegion());
		int startY = target.getForceMovement().getStart().getLocalY(player.getLastKnownRegion());
		int endX = target.getForceMovement().getEnd().getX();
		int endY = target.getForceMovement().getEnd().getY();

		builder.put(startX, ValueType.S);
		builder.put(startY, ValueType.S);
		builder.put(startX + endX, ValueType.S);
		builder.put(startY + endY, ValueType.S);
		builder.putShort(target.getForceMovement().getSpeed(), ValueType.A, ByteOrder.LITTLE);
		builder.putShort(target.getForceMovement().getReverseSpeed(), ValueType.A, ByteOrder.BIG);
		builder.putShort(target.getForceMovement().getAnimation(), ValueType.A, ByteOrder.LITTLE);
		builder.put(target.getForceMovement().getDirection(), ValueType.S);
	}

	/**
	 * This update block is used to update a player's animation.
	 *
	 * @param builder
	 *            The packet builder to write information on.
	 * @param target
	 *            The player to update animations for.
	 * @return The PlayerUpdating instance.
	 */
	private static void updateAnimation(PacketBuilder builder, Player target) {
		builder.putShort(target.getAnimation().getId(), ByteOrder.LITTLE);
		builder.put(target.getAnimation().getDelay(), ValueType.C);
	}

	/**
	 * This update block is used to update a player's graphics.
	 *
	 * @param builder
	 *            The packet builder to write information on.
	 * @param target
	 *            The player to update graphics for.
	 * @return The PlayerUpdating instance.
	 */
	private static void updateGraphics(PacketBuilder builder, Player target) {
		builder.putShort(target.getGraphic().getId(), ByteOrder.LITTLE);
		builder.putInt(
				((target.getGraphic().getHeight().ordinal() * 50) << 16) + (target.getGraphic().getDelay() & 0xffff));
	}

	/**
	 * This update block is used to update a player's single hit.
	 *
	 * @param builder
	 *            The packet builder used to write information on.
	 * @param target
	 *            The player to update the single hit for.
	 * @return The PlayerUpdating instance.
	 */
	private static void updateSingleHit(PacketBuilder builder, Player target) {
		builder.putShort(target.getPrimaryHit().getDamage());
		builder.put(target.getPrimaryHit().getHitmask().ordinal());
		builder.putShort(target.getHitpoints());
		builder.putShort(target.getSkillManager().getMaxLevel(Skill.HITPOINTS));
	}

	/**
	 * This update block is used to update a player's double hit.
	 *
	 * @param builder
	 *            The packet builder used to write information on.
	 * @param target
	 *            The player to update the double hit for.
	 * @return The PlayerUpdating instance.
	 */
	private static void updateDoubleHit(PacketBuilder builder, Player target) {
		builder.putShort(target.getSecondaryHit().getDamage());
		builder.put(target.getSecondaryHit().getHitmask().ordinal());
		builder.putShort(target.getHitpoints());
		builder.putShort(target.getSkillManager().getMaxLevel(Skill.HITPOINTS));
	}

	/**
	 * This update block is used to update a player's face position.
	 *
	 * @param builder
	 *            The packet builder to write information on.
	 * @param target
	 *            The player to update face position for.
	 * @return The PlayerUpdating instance.
	 */
	private static void updateFacingPosition(PacketBuilder builder, Player target) {
		final Location position = target.getPositionToFace();
		builder.putShort(position.getX() * 2 + 1, ValueType.A, ByteOrder.LITTLE);
		builder.putShort(position.getY() * 2 + 1, ByteOrder.LITTLE);
	}

	/**
	 * This update block is used to update a player's entity interaction.
	 *
	 * @param builder
	 *            The packet builder to write information on.
	 * @param target
	 *            The player to update entity interaction for.
	 * @return The PlayerUpdating instance.
	 */
	private static void updateEntityInteraction(PacketBuilder builder, Player target) {
		Mobile entity = target.getInteractingMobile();
		if (entity != null) {
			int index = entity.getIndex();
			if (entity instanceof Player)
				index += +32768;
			builder.putShort(index, ByteOrder.LITTLE);
		} else {
			builder.putShort(-1, ByteOrder.LITTLE);
		}
	}

	/**
	 * This update block is used to update a player's appearance, this includes
	 * their equipment, clothing, combat level, gender, head icons, user name and
	 * animations.
	 *
	 * @param out
	 *            The packet builder to write information on.
	 * @param target
	 *            The player to update appearance for.
	 * @return The PlayerUpdating instance.
	 */
	private static void updateAppearance(Player player, PacketBuilder out, Player target) {
		Appearance appearance = target.getAppearance();
		Equipment equipment = target.getEquipment();
		PacketBuilder properties = new PacketBuilder();

		properties.put(appearance.isMale() ? 0 : 1);

		// Head icon, prayers
		properties.put(appearance.getHeadHint());

		// Skull icon
		properties.put(target.isSkulled() ? target.getSkullType().getIconId() : -1);

		// Some sort of headhint (arrow over head)
		properties.put(0);

		if (player.getNpcTransformationId() == -1) {
			int[] equip = new int[equipment.capacity()];
			for (int i = 0; i < equipment.capacity(); i++) {
				equip[i] = equipment.getItems()[i].getId();
			}
			if (equip[Equipment.HEAD_SLOT] > -1) {
				properties.putShort(0x200 + equip[Equipment.HEAD_SLOT]);
			} else {
				properties.put(0);
			}
			if (equip[Equipment.CAPE_SLOT] > -1) {
				properties.putShort(0x200 + equip[Equipment.CAPE_SLOT]);
			} else {
				properties.put(0);
			}
			if (equip[Equipment.AMULET_SLOT] > -1) {
				properties.putShort(0x200 + equip[Equipment.AMULET_SLOT]);
			} else {
				properties.put(0);
			}
			if (equip[Equipment.WEAPON_SLOT] > -1) {
				properties.putShort(0x200 + equip[Equipment.WEAPON_SLOT]);
			} else {
				properties.put(0);
			}
			if (equip[Equipment.BODY_SLOT] > -1) {
				properties.putShort(0x200 + equip[Equipment.BODY_SLOT]);
			} else {
				properties.putShort(0x100 + appearance.getLook()[Appearance.CHEST]);
			}
			if (equip[Equipment.SHIELD_SLOT] > -1) {
				properties.putShort(0x200 + equip[Equipment.SHIELD_SLOT]);
			} else {
				properties.put(0);
			}

			if (ItemDefinition.forId(equip[Equipment.BODY_SLOT]).getEquipmentType() == EquipmentType.PLATEBODY) {
				properties.put(0);
			} else {
				properties.putShort(0x100 + appearance.getLook()[Appearance.ARMS]);
			}

			if (equip[Equipment.LEG_SLOT] > -1) {
				properties.putShort(0x200 + equip[Equipment.LEG_SLOT]);
			} else {
				properties.putShort(0x100 + appearance.getLook()[Appearance.LEGS]);
			}

			if (ItemDefinition.forId(equip[Equipment.HEAD_SLOT]).getEquipmentType() == EquipmentType.FULL_HELMET
					|| ItemDefinition.forId(equip[Equipment.CAPE_SLOT]).getEquipmentType() == EquipmentType.HOODED_CAPE
					|| ItemDefinition.forId(equip[Equipment.HEAD_SLOT]).getEquipmentType() == EquipmentType.COIF) {
				properties.put(0);
			} else {
				properties.putShort(0x100 + appearance.getLook()[Appearance.HEAD]);
			}

			if (equip[Equipment.HANDS_SLOT] > -1) {
				properties.putShort(0x200 + equip[Equipment.HANDS_SLOT]);
			} else {
				properties.putShort(0x100 + appearance.getLook()[Appearance.HANDS]);
			}
			if (equip[Equipment.FEET_SLOT] > -1) {
				properties.putShort(0x200 + equip[Equipment.FEET_SLOT]);
			} else {
				properties.putShort(0x100 + appearance.getLook()[Appearance.FEET]);
			}
			if (appearance.getLook()[Appearance.BEARD] <= 0 || !appearance.isMale()
					|| ItemDefinition.forId(equip[Equipment.HEAD_SLOT]).getEquipmentType() == EquipmentType.FULL_HELMET

			) {// || ItemDefinition.forId(equip[Equipment.HEAD_SLOT]).isMask()) {
				properties.put(0);
			} else {
				properties.putShort(0x100 + appearance.getLook()[Appearance.BEARD]);
			}
		} else {
			properties.putShort(-1);
			properties.putShort(player.getNpcTransformationId());
		}
		properties.put(appearance.getLook()[Appearance.HAIR_COLOUR]);
		properties.put(appearance.getLook()[Appearance.TORSO_COLOUR]);
		properties.put(appearance.getLook()[Appearance.LEG_COLOUR]);
		properties.put(appearance.getLook()[Appearance.FEET_COLOUR]);
		properties.put(appearance.getLook()[Appearance.SKIN_COLOUR]);

		int skillAnim = target.getSkillAnimation();
		if (skillAnim > 0) {
			for (int i = 0; i < 7; i++)
				properties.putShort(skillAnim);
		} else {
			ItemDefinition wep = target.getEquipment().getItems()[Equipment.WEAPON_SLOT].getDefinition();
			properties.putShort(wep.getStandAnim());
			properties.putShort(0x337);
			properties.putShort(wep.getWalkAnim());
			properties.putShort(0x334);
			properties.putShort(0x335);
			properties.putShort(0x336);
			properties.putShort(wep.getRunAnim());
		}

		properties.putLong(target.getLongUsername());
		properties.put(target.getSkillManager().getCombatLevel());
		properties.put(target.getRights().ordinal());
		properties.putString(target.getLoyaltyTitle());

		out.put(properties.buffer().writerIndex(), ValueType.C);
		out.putBytes(properties.buffer());
	}
}