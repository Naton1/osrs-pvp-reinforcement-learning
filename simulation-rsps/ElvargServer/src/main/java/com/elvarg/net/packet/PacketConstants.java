package com.elvarg.net.packet;

import com.elvarg.net.packet.impl.*;

/**
 * Defining all packets and other packet-related-constants that are in the 317
 * protocol.
 *
 * @author Gabriel Hannason
 */
public class PacketConstants {

	public static final PacketExecutor[] PACKETS = new PacketExecutor[257];

	public static final int TELEPORT_OPCODE = 183;
	public static final int SPECIAL_ATTACK_OPCODE = 184;
	public static final int BUTTON_CLICK_OPCODE = 185;
	public static final int INTERFACE_ACTION_CLICK_OPCODE = 186;
	public static final int SPAWN_TAB_ACTION_OPCODE = 187;
	public static final int REGULAR_CHAT_OPCODE = 4;
	public static final int CLAN_CHAT_OPCODE = 104;
	public static final int DROP_ITEM_OPCODE = 87;
	public static final int FINALIZED_MAP_REGION_OPCODE = 121;
	public static final int CHANGE_MAP_REGION_OPCODE = 210;
	public static final int CLOSE_INTERFACE_OPCODE = 130;
	public static final int EXAMINE_ITEM_OPCODE = 2;
	public static final int EXAMINE_NPC_OPCODE = 6;
	public static final int CHANGE_APPEARANCE = 11;
	public static final int DIALOGUE_OPCODE = 40;
	public static final int ENTER_AMOUNT_OPCODE = 208, ENTER_SYNTAX_OPCODE = 60;
	public static final int EQUIP_ITEM_OPCODE = 41;
	public static final int PLAYER_INACTIVE_OPCODE = 202;
	public static final int CHAT_SETTINGS_OPCODE = 95;
	public static final int COMMAND_OPCODE = 103;
	public static final int COMMAND_MOVEMENT_OPCODE = 98;
	public static final int GAME_MOVEMENT_OPCODE = 164;
	public static final int MINIMAP_MOVEMENT_OPCODE = 248;
	public static final int PICKUP_ITEM_OPCODE = 236;
	public static final int SECOND_GROUNDITEM_OPTION_OPCODE = 235;
	public static final int FIRST_ITEM_CONTAINER_ACTION_OPCODE = 145;
	public static final int SECOND_ITEM_CONTAINER_ACTION_OPCODE = 117;
	public static final int THIRD_ITEM_CONTAINER_ACTION_OPCODE = 43;
	public static final int FOURTH_ITEM_CONTAINER_ACTION_OPCODE = 129;
	public static final int FIFTH_ITEM_CONTAINER_ACTION_OPCODE = 135;
	public static final int SIXTH_ITEM_CONTAINER_ACTION_OPCODE = 138;
	public static final int ADD_FRIEND_OPCODE = 188;
	public static final int REMOVE_FRIEND_OPCODE = 215;
	public static final int ADD_IGNORE_OPCODE = 133;
	public static final int REMOVE_IGNORE_OPCODE = 74;
	public static final int SEND_PM_OPCODE = 126;
	public static final int ATTACK_PLAYER_OPCODE = 153;
	public static final int PLAYER_OPTION_1_OPCODE = 128;
	public static final int PLAYER_OPTION_2_OPCODE = 37;
	public static final int PLAYER_OPTION_3_OPCODE = 227;
	public static final int SWITCH_ITEM_SLOT_OPCODE = 214;
	public static final int FOLLOW_PLAYER_OPCODE = 73;
	public static final int MAGIC_ON_PLAYER_OPCODE = 249;
	public static final int MAGIC_ON_ITEM_OPCODE = 237;
	public static final int MAGIC_ON_GROUND_ITEM_OPCODE = 181;
	public static final int BANK_TAB_CREATION_OPCODE = 216;
	public static final int TRADE_REQUEST_OPCODE = 139;
	public static final int DUEL_REQUEST_OPCODE = 128;
	public static final int CREATION_MENU_OPCODE = 166;
	public static final int SEND_GRAND_EXCHANGE_UPDATE = 200;

	public static final int INTERFACE_TAB_ID_OPCODE = 239;

	public static final int OBJECT_FIRST_CLICK_OPCODE = 132, OBJECT_SECOND_CLICK_OPCODE = 252,
			OBJECT_THIRD_CLICK_OPCODE = 70, OBJECT_FOURTH_CLICK_OPCODE = 234, OBJECT_FIFTH_CLICK_OPCODE = 228;

	public static final int ATTACK_NPC_OPCODE = 72, FIRST_CLICK_NPC_OPCODE = 155, MAGE_NPC_OPCODE = 131,
			SECOND_CLICK_NPC_OPCODE = 17, THIRD_CLICK_NPC_OPCODE = 21, FOURTH_CLICK_NPC_OPCODE = 18;

	public static final int FIRST_ITEM_ACTION_OPCODE = 122, SECOND_ITEM_ACTION_OPCODE = 75,
			THIRD_ITEM_ACTION_OPCODE = 16;

	public static final int ITEM_ON_NPC = 57, ITEM_ON_ITEM = 53, ITEM_ON_OBJECT = 192, ITEM_ON_GROUND_ITEM = 25,
			ITEM_ON_PLAYER = 14;

	static {
		PACKETS[TELEPORT_OPCODE] = new TeleportPacketListener();
		PACKETS[SPECIAL_ATTACK_OPCODE] = new SpecialAttackPacketListener();
		PACKETS[BUTTON_CLICK_OPCODE] = new ButtonClickPacketListener();
		PACKETS[INTERFACE_ACTION_CLICK_OPCODE] = new InterfaceActionClickOpcode();
		PACKETS[REGULAR_CHAT_OPCODE] = new ChatPacketListener();
		PACKETS[CLAN_CHAT_OPCODE] = new ChatPacketListener();
		
		PACKETS[DROP_ITEM_OPCODE] = new DropItemPacketListener();
		PACKETS[FINALIZED_MAP_REGION_OPCODE] = new FinalizedMapRegionChangePacketListener();
		PACKETS[CHANGE_MAP_REGION_OPCODE] = new RegionChangePacketListener();
		PACKETS[CLOSE_INTERFACE_OPCODE] = new CloseInterfacePacketListener();
		PACKETS[EXAMINE_ITEM_OPCODE] = new ExamineItemPacketListener();
		PACKETS[EXAMINE_NPC_OPCODE] = new ExamineNpcPacketListener();
		PACKETS[CHANGE_APPEARANCE] = new ChangeAppearancePacketListener();
		PACKETS[DIALOGUE_OPCODE] = new DialoguePacketListener();
		PACKETS[ENTER_AMOUNT_OPCODE] = new EnterInputPacketListener();
		PACKETS[EQUIP_ITEM_OPCODE] = new EquipPacketListener();
		PACKETS[PLAYER_INACTIVE_OPCODE] = new PlayerInactivePacketListener();
		PACKETS[CHAT_SETTINGS_OPCODE] = new ChatSettingsPacketListener();
		PACKETS[COMMAND_OPCODE] = new CommandPacketListener();
		PACKETS[COMMAND_MOVEMENT_OPCODE] = new MovementPacketListener();
		PACKETS[GAME_MOVEMENT_OPCODE] = new MovementPacketListener();
		PACKETS[MINIMAP_MOVEMENT_OPCODE] = new MovementPacketListener();
		PACKETS[PICKUP_ITEM_OPCODE] = new PickupItemPacketListener();
		PACKETS[SECOND_GROUNDITEM_OPTION_OPCODE] = new SecondGroundItemOptionPacketListener();
		PACKETS[SWITCH_ITEM_SLOT_OPCODE] = new SwitchItemSlotPacketListener();
		PACKETS[FOLLOW_PLAYER_OPCODE] = new FollowPlayerPacketListener();
		PACKETS[MAGIC_ON_PLAYER_OPCODE] = new MagicOnPlayerPacketListener();
		PACKETS[MAGIC_ON_ITEM_OPCODE] = new MagicOnItemPacketListener();
		PACKETS[MAGIC_ON_GROUND_ITEM_OPCODE] = new MagicOnItemPacketListener();
		PACKETS[BANK_TAB_CREATION_OPCODE] = new BankTabCreationPacketListener();
		PACKETS[SPAWN_TAB_ACTION_OPCODE] = new SpawnItemPacketListener();

		PACKETS[FIRST_ITEM_CONTAINER_ACTION_OPCODE] = new ItemContainerActionPacketListener();
		PACKETS[SECOND_ITEM_CONTAINER_ACTION_OPCODE] = new ItemContainerActionPacketListener();
		PACKETS[THIRD_ITEM_CONTAINER_ACTION_OPCODE] = new ItemContainerActionPacketListener();
		PACKETS[FOURTH_ITEM_CONTAINER_ACTION_OPCODE] = new ItemContainerActionPacketListener();
		PACKETS[FIFTH_ITEM_CONTAINER_ACTION_OPCODE] = new ItemContainerActionPacketListener();
		PACKETS[SIXTH_ITEM_CONTAINER_ACTION_OPCODE] = new ItemContainerActionPacketListener();

		PACKETS[ATTACK_PLAYER_OPCODE] = new PlayerOptionPacketListener();
		PACKETS[PLAYER_OPTION_1_OPCODE] = new PlayerOptionPacketListener();
		PACKETS[PLAYER_OPTION_2_OPCODE] = new PlayerOptionPacketListener();
		PACKETS[PLAYER_OPTION_3_OPCODE] = new PlayerOptionPacketListener();

		PACKETS[OBJECT_FIRST_CLICK_OPCODE] = new ObjectActionPacketListener();
		PACKETS[OBJECT_SECOND_CLICK_OPCODE] = new ObjectActionPacketListener();
		PACKETS[OBJECT_THIRD_CLICK_OPCODE] = new ObjectActionPacketListener();
		PACKETS[OBJECT_FOURTH_CLICK_OPCODE] = new ObjectActionPacketListener();
		PACKETS[OBJECT_FIFTH_CLICK_OPCODE] = new ObjectActionPacketListener();

		PACKETS[ATTACK_NPC_OPCODE] = new NPCOptionPacketListener();
		PACKETS[FIRST_CLICK_NPC_OPCODE] = new NPCOptionPacketListener();
		PACKETS[MAGE_NPC_OPCODE] = new NPCOptionPacketListener();
		PACKETS[SECOND_CLICK_NPC_OPCODE] = new NPCOptionPacketListener();
		PACKETS[THIRD_CLICK_NPC_OPCODE] = new NPCOptionPacketListener();
		PACKETS[FOURTH_CLICK_NPC_OPCODE] = new NPCOptionPacketListener();

		PACKETS[FIRST_ITEM_ACTION_OPCODE] = new ItemActionPacketListener();
		PACKETS[SECOND_ITEM_ACTION_OPCODE] = new ItemActionPacketListener();
		PACKETS[THIRD_ITEM_ACTION_OPCODE] = new ItemActionPacketListener();

		PACKETS[ITEM_ON_NPC] = new UseItemPacketListener();
		PACKETS[ITEM_ON_ITEM] = new UseItemPacketListener();
		PACKETS[ITEM_ON_OBJECT] = new UseItemPacketListener();
		PACKETS[ITEM_ON_GROUND_ITEM] = new UseItemPacketListener();
		PACKETS[ITEM_ON_PLAYER] = new UseItemPacketListener();

		PACKETS[ADD_FRIEND_OPCODE] = new PlayerRelationPacketListener();
		PACKETS[REMOVE_FRIEND_OPCODE] = new PlayerRelationPacketListener();
		PACKETS[ADD_IGNORE_OPCODE] = new PlayerRelationPacketListener();
		PACKETS[REMOVE_IGNORE_OPCODE] = new PlayerRelationPacketListener();
		PACKETS[SEND_PM_OPCODE] = new PlayerRelationPacketListener();

		PACKETS[ENTER_AMOUNT_OPCODE] = new EnterInputPacketListener();
		PACKETS[ENTER_SYNTAX_OPCODE] = new EnterInputPacketListener();

		PACKETS[TRADE_REQUEST_OPCODE] = new TradeRequestPacketListener();
		PACKETS[CREATION_MENU_OPCODE] = new CreationMenuPacketListener();
		PACKETS[INTERFACE_TAB_ID_OPCODE] = new TabInterfaceIdListener();
	}
}
