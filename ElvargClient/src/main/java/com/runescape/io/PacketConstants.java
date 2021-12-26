package com.runescape.io;

/**
 * The class that contains packet-related constants.
 *
 * @author Seven
 * @author TheChosenOne
 */
public final class PacketConstants {

	public static final int SET_POISON_TYPE = 184;
	public static final int SHOW_TELEPORT_INTERFACE = 183;
	public static final int SET_SPECIAL_ENABLED = 186;
    public static final int FOCUS_CHANGE = 3;
    public static final int FLAG_ACCOUNT = 45;
    public static final int REPORT_PLAYER = 218;
    public static final int IDLE = 0;
    public static final int INTERFACE_SCROLL_RESET = 9;
    public static final int SET_SCROLLBAR_HEIGHT = 10;
    public static final int INTERFACE_TEXT_CLEAR = 105;
    public static final int INTERFACE_ITEMS_CLEAR = 112;
    public static final int SET_TOTAL_EXP = 108;
    public static final int CAMERA_MOVEMENT = 86;
    public static final int ENTER_REGION = 210;
    public static final int UPDATE_PLAYER_RIGHTS = 127;
    public static final int PLAYER_UPDATING = 81;
    public static final int DELETE_GROUND_ITEM = 64;
    public static final int SEND_REMOVE_GROUND_ITEM = 156;
    public static final int SEND_OBJECT = 151;
    public static final int TRANSFORM_PLAYER_TO_OBJECT = 147;
    public static final int SEND_REMOVE_OBJECT = 101;
    public static final int DESIGN_SCREEN = 101;
    public static final int SEND_PROJECTILE = 117;
    public static final int ANIMATE_OBJECT = 160;
    public static final int SEND_ALTER_GROUND_ITEM_COUNT = 84;
    public static final int SEND_GROUND_ITEM = 44;
    public static final int SEND_GFX = 4;
    public static final int OPEN_WELCOME_SCREEN = 176;
    public static final int SHOW_CLANCHAT_OPTIONS = 115;
    public static final int SEND_EXP_DROP = 116;
    public static final int SHOW_PLAYER_HEAD_ON_INTERFACE = 185;
    public static final int BUTTON_CLICK = 185;
    public static final int CLAN_CHAT = 217; // 317 did not have this
    public static final int RESET_CAMERA = 107;
    public static final int CLEAN_ITEMS_OF_INTERFACE = 72;
    public static final int MOVE_ITEM = 214;
    public static final int SPIN_CAMERA = 166;
    public static final int SEND_SKILL = 134;
    public static final int SEND_SIDE_TAB = 71;
    public static final int PLAY_SONG = 74;
    public static final int NEXT_OR_PREVIOUS_SONG = 121;
    public static final int LOADED_REGION = 121;
    public static final int LOGOUT = 109;
    public static final int MOVE_COMPONENT = 70;
    public static final int SEND_WALKABLE_INTERFACE = 208;
    public static final int SEND_MINIMAP_STATE = 99;
    public static final int SHOW_NPC_HEAD_ON_INTERFACE = 75;
    public static final int SEND_MULTIPLE_MAP_PACKETS = 60;
    public static final int SEND_EARTHQUAKE = 35;
    public static final int SEND_PLAYER_OPTION = 104;
    public static final int CLEAR_MINIMAP_FLAG = 78;
    public static final int SET_AUTOCAST_ID = 38;
    public static final int ENABLE_NOCLIP = 250;
    public static final int SEND_URL = 251;
    public static final int SEND_SPECIAL_MESSAGE = 252;
    public static final int SEND_MESSAGE = 253;
    public static final int STOP_ALL_ANIMATIONS = 1;
    public static final int ADD_FRIEND = 50;
    public static final int REMOVE_FRIEND = 51;
    public static final int ADD_IGNORE = 214;
    public static final int REMOVE_IGNORE = 215;
    public static final int SEND_RUN_ENERGY = 110;
    public static final int SEND_TOGGLE_QUICK_PRAYERS = 111;
    public static final int SEND_TOGGLE_RUN = 113;
    public static final int SEND_HINT_ICON = 254;
    public static final int SEND_DUO_INTERFACE = 248;
    public static final int SEND_RECEIVED_PRIVATE_MESSAGE = 196;
    public static final int SEND_REGION = 85;
    public static final int SEND_ITEM_TO_INTERFACE = 246;
    public static final int SEND_INTERFACE_VISIBILITY_STATE = 171;
    public static final int SEND_SOLO_NON_WALKABLE_SIDEBAR_INTERFACE = 142;
    public static final int SET_INTERFACE_TEXT = 126;
    public static final int SEND_CONSOLE_COMMAND = 123;
    public static final int UPDATE_CHAT_MODES = 206;
    public static final int SEND_PLAYER_WEIGHT = 240;
    public static final int SEND_MODEL_TO_INTERFACE = 8;
    public static final int SEND_CHANGE_INTERFACE_COLOUR = 122;
    public static final int SEND_UPDATE_ITEMS = 53;
    public static final int SEND_EFFECT_TIMER = 54;
    public static final int SEND_CURRENT_BANK_TAB = 55;
    public static final int SET_MODEL_INTERFACE_ZOOM = 230;
    public static final int SET_FRIENDSERVER_STATUS = 221;
    public static final int MOVE_CAMERA = 177;
    public static final int SEND_INITIALIZE_PACKET = 249;
    public static final int NPC_UPDATING = 65;
    public static final int SEND_ENTER_AMOUNT = 27;
    public static final int SEND_ENTER_NAME = 187;
    public static final int SEND_NON_WALKABLE_INTERFACE = 97;
    public static final int SEND_WALKABLE_CHATBOX_INTERFACE = 218;
    public static final int SEND_CONFIG_INT = 87;
    public static final int SEND_CONFIG_BYTE = 36;
    public static final int SEND_MULTICOMBAT_ICON = 61;
    public static final int SEND_EXIT = 62;
    public static final int SEND_ANIMATE_INTERFACE = 200;
    public static final int CLOSE_INTERFACE = 219;
    public static final int UPDATE_SPECIFIC_ITEM = 34;
    public static final int SWITCH_TAB = 106;
    public static final int SEND_NONWALKABLE_CHATBOX_INTERFACE = 164;
    public static final int SEND_MAP_REGION = 73;
    public static final int SEND_REGION_MAP_REGION = 241;
    public static final int MOUSE_CLICK = 241;
    public static final int SYSTEM_UPDATE = 114;
    public static final int PLAY_SOUND_EFFECT = 174;
    public static final int IDLE_LOGOUT = 202;
    public static final int ITEM_ON_NPC = 57;
    public static final int CREATION_MENU = 167;
    
    public static final int[] PACKET_SIZES = {
            0, 0, 0, 1, 6, 0, 0, 0, 4, 4, //0
            6, 2, -1, 1, 1, -1, 1, 0, 0, 0, // 10
            0, 0, 0, 0, 1, 0, 0, -1, 1, 1, //20
            0, 0, 0, 0, -2, 4, 3, 0, 2, 0, //30
            0, 0, 0, 0, 5, 8, 0, 6, 0, 0, //40
            9, 8, 0, -2, 0, 1, 0, 0, 0, 0, //50
            -2, 1, 0, 0, 2, -2, 0, 0, 0, 0, //60
            6, 3, 2, 4, 2, 4, 0, 0, 0, 4, //70
            0, -2, 0, 0, 7, 2, 1, 6, 6, 0, //80
            0, 0, 0, 0, 0, 0, 0, 2, 0, 1, //90
            2, 2, 0, 1, -1, 8, 1, 0, 8, 0, //100
            1, 1, 1, 1, 2, 1, 5, 15, 0, 0, //110
            0, 4, 4, -1, 9, -1, -2, 2, 0, 0, //120 // 9
            -1, 0, 0, 0, 13, 0, 0, 1, 0, 0, // 130
            3, 10, 2, 0, 0, 0, 0, 14, 0, 0, //140
            0, 4, 5, 3, 0, 0, 3, 0, 0, 0, //150
            4, 5, 0, 0, 2, 0, 6, 0, 0, 0, //160
            0, 5, -2, -2, 5, 5, 10, 6, 0, -2, // 170
            0, 0, 0, 1, 0, 2, 1, -1, 0, 0, //180
            0, 0, 0, 0, 0, 2, -1, 0, -1, 0, //190
            4, 0, 0, 0, 0, 0, 3, 0, 4, 0,  //200
            0, 0, 0, 0, -2, 7, 0, -2, 2, 0, //210
            0, 1, -2, -2, 0, 0, 0, 0, 0, 0, // 220
            8, 0, 0, 0, 0, 0, 0, 0, 0, 0,//230
            2, -2, 0, 0, -1, 0, 6, 0, 4, 3,//240
            -1, 0, 0, -1, 6, 0, 0//250
        };
}
