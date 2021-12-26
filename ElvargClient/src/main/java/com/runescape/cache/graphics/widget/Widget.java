package com.runescape.cache.graphics.widget;

import com.runescape.Client;
import com.runescape.Configuration;
import com.runescape.cache.FileArchive;
import com.runescape.cache.anim.Frame;
import com.runescape.cache.def.ItemDefinition;
import com.runescape.cache.def.NpcDefinition;
import com.runescape.cache.graphics.Dropdown;
import com.runescape.cache.graphics.DropdownMenu;
import com.runescape.cache.graphics.GameFont;
import com.runescape.cache.graphics.RSFont;
import com.runescape.cache.graphics.Slider;
import com.runescape.cache.graphics.sprite.Sprite;
import com.runescape.collection.ReferenceCache;
import com.runescape.draw.teleports.TeleportButton;
import com.runescape.entity.model.Model;
import com.runescape.io.Buffer;
import com.runescape.model.content.Keybinding;
import com.runescape.util.StringUtils;

/**
 * Previously known as RSInterface, which is a class used to create and show
 * game interfaces.
 */
public class Widget {

	public static final int OPTION_OK = 1;
	public static final int OPTION_USABLE = 2;
	public static final int OPTION_CLOSE = 3;
	public static final int OPTION_TOGGLE_SETTING = 4;
	public static final int OPTION_RESET_SETTING = 5;
	public static final int OPTION_CONTINUE = 6;
	public static final int OPTION_DROPDOWN = 7;

	public static final int TYPE_CONTAINER = 0;
	public static final int TYPE_MODEL_LIST = 1;
	public static final int TYPE_INVENTORY = 2;
	public static final int TYPE_RECTANGLE = 3;
	public static final int TYPE_TEXT = 4;
	public static final int TYPE_SPRITE = 5;
	public static final int TYPE_MODEL = 6;
	public static final int TYPE_ITEM_LIST = 7;
	public static final int TYPE_OTHER = 8;
	public static final int TYPE_HOVER = 9;
	public static final int TYPE_CONFIG = 10;
	public static final int TYPE_CONFIG_HOVER = 11;
	public static final int TYPE_SLIDER = 12;
	public static final int TYPE_DROPDOWN = 13;
	public static final int TYPE_ROTATING = 14;
	public static final int TYPE_KEYBINDS_DROPDOWN = 15;
	public static final int TYPE_TICKER = 16;
	public static final int TYPE_ADJUSTABLE_CONFIG = 17;
	public static final int TYPE_BOX = 18;
	public static final int TYPE_MAP = 19;
	public static final int BEGIN_READING_PRAYER_INTERFACE = 6;// Amount of total custom prayers we've added
	public static final int CUSTOM_PRAYER_HOVERS = 3; // Amount of custom prayer hovers we've added
	public static final int PRAYER_INTERFACE_CHILDREN = 80 + BEGIN_READING_PRAYER_INTERFACE + CUSTOM_PRAYER_HOVERS;
	private static final ReferenceCache models = new ReferenceCache(30);
	private static final int LUNAR_RUNE_SPRITES_START = 232;
	private static final int LUNAR_OFF_SPRITES_START = 246;
	private static final int LUNAR_ON_SPRITES_START = 285;
	private static final int LUNAR_HOVER_BOX_SPRITES_START = 324;
	public static Widget interfaceCache[];
	public static RSFont[] newFonts;
	public static GameFont[] fonts;
	public static FileArchive graphics;
	private static ReferenceCache spriteCache;
	public int hoverXOffset = 0;
	public int hoverYOffset = 0;
	public int spriteXOffset = 0;
	public int spriteYOffset = 0;
	public boolean regularHoverBox;
	public int transparency = 255;
	public String hoverText;
	public boolean drawsTransparent;
	public Sprite disabledSprite;
	public int lastFrameTime;
	public Sprite sprites[];
	public int requiredValues[];
	public int contentType;
	public int spritesX[];
	public int defaultHoverColor;
	public int atActionType;
	public String spellName;
	public int secondaryColor;
	public int width;
	public String tooltip;
	public String selectedActionName;
	public boolean centerText;
	public boolean rightAlignedText;
	public boolean rollingText;
	public int scrollPosition;
	public String actions[];
	public int valueIndexArray[][];
	public boolean filled;
	public String secondaryText;
	public int hoverType;
	public int spritePaddingX;
	public int textColor;
	public int defaultMediaType;
	public int defaultMedia;
	public boolean replaceItems;
	public int parent;
	public int spellUsableOn;
	public int secondaryHoverColor;
	public int children[];
	public int childX[];
	public boolean usableItems;
	public GameFont textDrawingAreas;
	public boolean inventoryHover;
	public int spritePaddingY;
	public int valueCompareType[];
	public int currentFrame;
	public int spritesY[];
	public String defaultText;
	public boolean hasActions;
	public int id;
	public int inventoryAmounts[];
	public int inventoryItemId[];
	public byte opacity;
	public int defaultAnimationId;
	public int secondaryAnimationId;
	public boolean allowSwapItems;
	public Sprite enabledSprite;
	public int scrollMax;
	public int type;
	public int horizontalOffset;
	public int verticalOffset;
	public boolean invisible;
	public boolean hidden;
	public int height;
	public boolean textShadow;
	public int modelZoom;
	public int modelRotation1;
	public int modelRotation2;
	public int childY[];
	public DropdownMenu dropdown;
	public int[] dropdownColours;
	public boolean hovered = false;
	public Widget dropdownOpen;
	public int dropdownHover = -1;
	public Slider slider;
	public RSFont rsFont;
	public int msgX, msgY;
	public boolean toggled = false;
	public Sprite enabledAltSprite;
	public Sprite disabledAltSprite;
	public int[] buttonsToDisable;
	public boolean active;
	public boolean inverted;
	public int spriteOpacity;
	public String[] tooltips;
	public boolean newScroller;
	public boolean drawInfinity;
	private int anInt255;
	private int anInt256;
	
	private static final int SPRITE_CACHE_SIZE = 50_000;
	private static final int WIDGET_CACHE_SIZE = 57000;

	public Widget() {
	}

	public static void load(FileArchive interfaceArchive, GameFont fonts[], FileArchive graphics, RSFont[] newFonts) {
		spriteCache = new ReferenceCache(SPRITE_CACHE_SIZE);
		Buffer buffer = new Buffer(interfaceArchive.readFile("data"));
		Widget.newFonts = newFonts;
		Widget.fonts = fonts;
		Widget.graphics = graphics;
		int defaultParentId = -1;
		buffer.readUShort();
		interfaceCache = new Widget[WIDGET_CACHE_SIZE];

		while (buffer.currentPosition < buffer.payload.length) {
			int interfaceId = buffer.readUShort();
			if (interfaceId == 65535) {
				defaultParentId = buffer.readUShort();
				interfaceId = buffer.readUShort();
			}

			Widget widget = interfaceCache[interfaceId] = new Widget();
			widget.id = interfaceId;
			widget.parent = defaultParentId;
			widget.type = buffer.readUnsignedByte();
			widget.atActionType = buffer.readUnsignedByte();
			widget.contentType = buffer.readUShort();
			widget.width = buffer.readUShort();
			widget.height = buffer.readUShort();
			widget.opacity = (byte) buffer.readUnsignedByte();
			widget.hoverType = buffer.readUnsignedByte();
			if (widget.hoverType != 0)
				widget.hoverType = (widget.hoverType - 1 << 8) + buffer.readUnsignedByte();
			else
				widget.hoverType = -1;
			int operators = buffer.readUnsignedByte();
			if (operators > 0) {
				widget.valueCompareType = new int[operators];
				widget.requiredValues = new int[operators];
				for (int index = 0; index < operators; index++) {
					widget.valueCompareType[index] = buffer.readUnsignedByte();
					widget.requiredValues[index] = buffer.readUShort();
				}

			}
			int scripts = buffer.readUnsignedByte();
			if (scripts > 0) {
				widget.valueIndexArray = new int[scripts][];
				for (int script = 0; script < scripts; script++) {
					int instructions = buffer.readUShort();
					widget.valueIndexArray[script] = new int[instructions];
					for (int instruction = 0; instruction < instructions; instruction++)
						widget.valueIndexArray[script][instruction] = buffer.readUShort();

				}

			}
			if (widget.type == TYPE_CONTAINER) {
				widget.drawsTransparent = false;
				widget.scrollMax = buffer.readUShort();
				widget.invisible = buffer.readUnsignedByte() == 1;
				int length = buffer.readUShort();

				if (widget.id == 5608) {

					widget.children = new int[PRAYER_INTERFACE_CHILDREN];
					widget.childX = new int[PRAYER_INTERFACE_CHILDREN];
					widget.childY = new int[PRAYER_INTERFACE_CHILDREN];

					for (int index = 0; index < length; index++) {
						widget.children[BEGIN_READING_PRAYER_INTERFACE + index] = buffer.readUShort();
						widget.childX[BEGIN_READING_PRAYER_INTERFACE + index] = buffer.readShort();
						widget.childY[BEGIN_READING_PRAYER_INTERFACE + index] = buffer.readShort();
					}

				} else {
					widget.children = new int[length];
					widget.childX = new int[length];
					widget.childY = new int[length];

					for (int index = 0; index < length; index++) {
						widget.children[index] = buffer.readUShort();
						widget.childX[index] = buffer.readShort();
						widget.childY[index] = buffer.readShort();
					}
				}
			}
			if (widget.type == TYPE_MODEL_LIST) {
				buffer.readUShort();
				buffer.readUnsignedByte();
			}
			if (widget.type == TYPE_INVENTORY) {
				widget.inventoryItemId = new int[widget.width * widget.height];
				widget.inventoryAmounts = new int[widget.width * widget.height];
				widget.allowSwapItems = buffer.readUnsignedByte() == 1;
				widget.hasActions = buffer.readUnsignedByte() == 1;
				widget.usableItems = buffer.readUnsignedByte() == 1;
				widget.replaceItems = buffer.readUnsignedByte() == 1;
				widget.spritePaddingX = buffer.readUnsignedByte();
				widget.spritePaddingY = buffer.readUnsignedByte();
				widget.spritesX = new int[20];
				widget.spritesY = new int[20];
				widget.sprites = new Sprite[20];
				for (int j2 = 0; j2 < 20; j2++) {
					int k3 = buffer.readUnsignedByte();
					if (k3 == 1) {
						widget.spritesX[j2] = buffer.readShort();
						widget.spritesY[j2] = buffer.readShort();
						String s1 = buffer.readString();
						if (graphics != null && s1.length() > 0) {
							int i5 = s1.lastIndexOf(",");

							int index = Integer.parseInt(s1.substring(i5 + 1));

							String name = s1.substring(0, i5);

							widget.sprites[j2] = getSprite(index, graphics, name);
						}
					}
				}
				widget.actions = new String[5];
				for (int actionIndex = 0; actionIndex < 5; actionIndex++) {
					widget.actions[actionIndex] = buffer.readString();
					if (widget.actions[actionIndex].length() == 0)
						widget.actions[actionIndex] = null;
					if (widget.parent == 1644)
						widget.actions[2] = "Operate";
					if (widget.parent == 3824) {
						widget.actions[4] = "Buy X";
					}
					if (widget.parent == 3822) {
						widget.actions[4] = "Sell X";
					}
				}
			}
			if (widget.type == TYPE_RECTANGLE)
				widget.filled = buffer.readUnsignedByte() == 1;
			if (widget.type == TYPE_TEXT || widget.type == TYPE_MODEL_LIST) {
				widget.centerText = buffer.readUnsignedByte() == 1;
				int k2 = buffer.readUnsignedByte();
				if (fonts != null)
					widget.textDrawingAreas = fonts[k2];
				widget.textShadow = buffer.readUnsignedByte() == 1;
			}

			if (widget.type == TYPE_TEXT) {
				widget.defaultText = buffer.readString().replaceAll("RuneScape", Configuration.CLIENT_NAME);
				widget.secondaryText = buffer.readString();
			}

			if (widget.type == TYPE_MODEL_LIST || widget.type == TYPE_RECTANGLE || widget.type == TYPE_TEXT)
				widget.textColor = buffer.readInt();
			if (widget.type == TYPE_RECTANGLE || widget.type == TYPE_TEXT) {
				widget.secondaryColor = buffer.readInt();
				widget.defaultHoverColor = buffer.readInt();
				widget.secondaryHoverColor = buffer.readInt();
			}
			if (widget.type == TYPE_SPRITE) {
				widget.drawsTransparent = false;
				String name = buffer.readString();
				if (graphics != null && name.length() > 0) {
					int index = name.lastIndexOf(",");
					widget.disabledSprite = getSprite(Integer.parseInt(name.substring(index + 1)), graphics,
							name.substring(0, index));
				}
				name = buffer.readString();
				if (graphics != null && name.length() > 0) {
					int index = name.lastIndexOf(",");
					widget.enabledSprite = getSprite(Integer.parseInt(name.substring(index + 1)), graphics,
							name.substring(0, index));
				}
			}
			if (widget.type == TYPE_MODEL) {
				int content = buffer.readUnsignedByte();
				if (content != 0) {
					widget.defaultMediaType = 1;
					widget.defaultMedia = (content - 1 << 8) + buffer.readUnsignedByte();
				}
				content = buffer.readUnsignedByte();
				if (content != 0) {
					widget.anInt255 = 1;
					widget.anInt256 = (content - 1 << 8) + buffer.readUnsignedByte();
				}
				content = buffer.readUnsignedByte();
				if (content != 0)
					widget.defaultAnimationId = (content - 1 << 8) + buffer.readUnsignedByte();
				else
					widget.defaultAnimationId = -1;
				content = buffer.readUnsignedByte();
				if (content != 0)
					widget.secondaryAnimationId = (content - 1 << 8) + buffer.readUnsignedByte();
				else
					widget.secondaryAnimationId = -1;
				widget.modelZoom = buffer.readUShort();
				widget.modelRotation1 = buffer.readUShort();
				widget.modelRotation2 = buffer.readUShort();
			}
			if (widget.type == TYPE_ITEM_LIST) {
				widget.inventoryItemId = new int[widget.width * widget.height];
				widget.inventoryAmounts = new int[widget.width * widget.height];
				widget.centerText = buffer.readUnsignedByte() == 1;
				int l2 = buffer.readUnsignedByte();
				if (fonts != null)
					widget.textDrawingAreas = fonts[l2];
				widget.textShadow = buffer.readUnsignedByte() == 1;
				widget.textColor = buffer.readInt();
				widget.spritePaddingX = buffer.readShort();
				widget.spritePaddingY = buffer.readShort();
				widget.hasActions = buffer.readUnsignedByte() == 1;
				widget.actions = new String[5];
				for (int actionCount = 0; actionCount < 5; actionCount++) {
					widget.actions[actionCount] = buffer.readString();
					if (widget.actions[actionCount].length() == 0)
						widget.actions[actionCount] = null;
				}

			}
			if (widget.atActionType == OPTION_USABLE || widget.type == TYPE_INVENTORY) {
				widget.selectedActionName = buffer.readString();
				widget.spellName = buffer.readString();
				widget.spellUsableOn = buffer.readUShort();
			}

			if (widget.type == 8) {
				widget.defaultText = buffer.readString();
			}

			if (widget.atActionType == OPTION_OK || widget.atActionType == OPTION_TOGGLE_SETTING
					|| widget.atActionType == OPTION_RESET_SETTING || widget.atActionType == OPTION_CONTINUE) {
				widget.tooltip = buffer.readString();
				if (widget.tooltip.length() == 0) {
					// TODO
					if (widget.atActionType == OPTION_OK)
						widget.tooltip = "Ok";
					if (widget.atActionType == OPTION_TOGGLE_SETTING)
						widget.tooltip = "Select";
					if (widget.atActionType == OPTION_RESET_SETTING)
						widget.tooltip = "Select";
					if (widget.atActionType == OPTION_CONTINUE)
						widget.tooltip = "Continue";
				}
			}
		}

		//interfaceLoader = interfaces;

		spawnTab();
		clanChatTab();
		configureLunar();
		quickPrayers();
		equipmentScreen();
		equipmentTab();
		itemsKeptOnDeath();
		// bounty(textDrawingAreas);
		worldMap();
		shop();
		prayerBook();
		priceChecker();

		bankInterface();
		bankSettings();

		SettingsWidget.widget();
		SettingsWidget.advancedWidget();
		keybinding();

		levelUpInterfaces();
		// questTab(textDrawingAreas);
		teleportTab();
		presets();
		magicInterfaces();
		editSkillTab();
		
		clanSetup();
		barrowsRewards();
		godwarsDungeon();
		OSRSCreationMenu.build();
		spriteCache = null;
		
		/*int lastNull = -1;
		for (int a = interfaceCache.length - 1; a > 0; a--) {
			if (lastNull == -1 && interfaceCache[a] == null) {
				lastNull = a;
			}
			if (lastNull > 0 && interfaceCache[a] != null) {
				System.out.println("START " + (a + 1) + " LAST: " + lastNull + " SIZE: " + (lastNull - a));
				lastNull = -1;
			}
		}*/
	}
	
	private static void godwarsDungeon() {
		Widget tab = addInterface(42569);
		tab.totalChildren(9);
		addText(42570, "NPC killcount", fonts, 0, 0xff9040, false, true);
		addText(42571, "Armadyl kills", fonts, 0, 0xff9040, false, true);
		addText(42572, "Bandos kills", fonts, 0, 0xff9040, false, true);
		addText(42573, "Saradomin kills", fonts, 0, 0xff9040, false, true);
		addText(42574, "Zamorak kills", fonts, 0, 0xff9040, false, true);
		
		addText(42575, "0", fonts, 0, 0x66FFFF, false, true);
		addText(42576, "0", fonts, 0, 0x66FFFF, false, true);
		addText(42577, "0", fonts, 0, 0x66FFFF, false, true);
		addText(42578, "0", fonts, 0, 0x66FFFF, false, true);
		
		int childId = 0;
		tab.child(childId++, 42570, 390, 40);
		tab.child(childId++, 42571, 390, 65);
		tab.child(childId++, 42572, 390, 80);
		tab.child(childId++, 42573, 390, 95);
		tab.child(childId++, 42574, 390, 110);
		tab.child(childId++, 42575, 485, 65);
		tab.child(childId++, 42576, 485, 80);
		tab.child(childId++, 42577, 485, 95);
		tab.child(childId++, 42578, 485, 110);
	}
	
	private static void barrowsRewards() {
		Widget widget = addInterface(42560);
		widget.totalChildren(8);
		addSprite(42561, 630);
		closeButton(42562, 142, 143);
		
		int childId = 0;
		widget.child(childId++, 42561, 147, 64);
		widget.child(childId++, 42562, 350, 70);
		
		int itemFrameId = 42563;
		int x = 235;
		int y = 105;
		for (int i = 0; i < 6; i++, x += 43) {
			if (i == 3) {
				x = 235;
				y = 150;
			}
			addItemOnInterface(itemFrameId + i, 42560, new String[] {});
			widget.child(childId++, itemFrameId + i, x, y);
		}
	}
	
	public static void editSkillTab() {
		Widget widget = interfaceCache[3917];
		
		extendChildren(widget, 1);
		
		createTooltip(27656, 62, 30, fonts, "Total XP: 0");
		
		widget.child(55, 27656, 127, 220);
	}

	public static void magicInterfaces() {
		// Remove unused spells
		interfaceCache[18470].hidden = true;
		interfaceCache[19207].hidden = true;
		interfaceCache[19208].hidden = true;
		interfaceCache[24127].hidden = true;
		interfaceCache[13095].hidden = true;
		int[] removeLunarTeleports = { 30170, 30226, 30234, 30250, 30258, 30266, 30274, };
		for (int teleport : removeLunarTeleports) {
			interfaceCache[teleport].hidden = true;
		}

		// Configure teleport buttons
		for (TeleportButton button : TeleportButton.values()) {

			// Adjust tooltip
			// Add previous teleport option
			for (int buttonId : button.buttonIds) {
				try {
					interfaceCache[buttonId].tooltip = null;
					interfaceCache[buttonId].actions = new String[] {"Cast @gre@" + button.name, "Previous teleport"};

					// Edit sprites
					int spriteId = button.modernSpriteId;
					if (interfaceCache[buttonId].parent == 12855 || buttonId == 12856) {
						spriteId = button.ancientSpriteId;
					}
					interfaceCache[buttonId].enabledSprite = interfaceCache[buttonId].disabledSprite = Client.spriteCache.lookup(spriteId);
				} catch (Exception e) {
					System.out.println("Failed updating: " + buttonId);
				}

			}

			// Adjust names
			for (int nameId : button.nameFrames) {
				interfaceCache[nameId].defaultText = button.name;
			}

			// Adjust box hover tooltips
			for (int tooltipId : button.tooltipFrames) {
				interfaceCache[tooltipId].defaultText = button.tooltip;
			}

			// Remove required runes
			for (int runeFrame : button.requiredRunesFrames) {
				interfaceCache[runeFrame].defaultText = "@gre@Free";
			}
		}
		modernSpellbook();
	}

	public static void modernSpellbook() {
		Widget r = interfaceCache[12424];
		Widget modern = interfaceCache[1151];

		// Remove children from modern
		removeChild(modern, 1); // Removes 12424 child
		removeChild(modern, 1); // Removes home teleport, needs child index change

		int offsetX = 13;
		int offsetY = 9;

		// Put back all spells
		for (int i = 0; i < r.children.length; i++) {
			insertNewChild(modern, i + 1, r.children[i], r.childX[i] + offsetX, r.childY[i] + offsetY);
			interfaceCache[r.children[i]].parent = 1151;
		}

		// Put back home teleport
		// insertNewChild(modern, 0, 1195, offsetX, offsetY);
	}

	private static void clanSetup() {
		Widget widget = addTabInterface(38300);

		addSprite(38301, 600);

		closeButton(38302, 24, 25);
		
		addPixels(38305, 0x383023, 302, 256, 0, false);
		addPixels(38306, 0x5A5245, 300, 254, 0, false);
		addPixels(38307, 0x5A5245, 300, 15, 0, false);

		addPixels(38308, 0x383023, 1, 13, 0, false);// Left line
		addPixels(38309, 0x5A5245, 1, 13, 0, false); // Right line

	/*	addConfigButtonWithToolTip(38310, 38300, 38311, 1, "clansetup", 3, "clansetup", 170, 13, "Sort by name", 1, 5,
				222);
		addTooltip(38311, "Sort by name");

		addConfigButtonWithToolTip(38313, 38300, 38314, 5, "clansetup", 7, "clansetup", 110, 13, "Sort by rank", 1, 5,
				223);
		*/
		
		addTooltip(38314, "Sort by rank");
		
		addHoverButton(38319, 613, 145, 45, null, -1, 38320, 1, new String[] { "Set prefix", "Disable"});
		addHoveredButton(38320, 614, 145, 45, 38321);

		addHoverButton(38322, 613, 145, 45, null, -1, 38323, 1, new String[] { "Only me", "General+", "Captain+",
				"Lieutenant+", "Sergeant+", "Corporal+", "Recruit+", "Any friends", "Anyone"});
		addHoveredButton(38323, 614, 145, 45, 38324);
		
		addHoverButton(38325, 613, 145, 45, null, -1, 38326, 1, new String[] { "Only me", "General+", "Captain+",
				"Lieutenant+", "Sergeant+", "Corporal+", "Recruit+", "Any friends", "Anyone"});
		addHoveredButton(38326, 614, 145, 45, 38327);
		
		addHoverButton(38328, 613, 145, 45, null, -1, 38329, 1, new String[] { "Only me", "General+", "Captain+",
				"Lieutenant+", "Sergeant+", "Corporal+", "Recruit+", "Any friends", "Anyone"});
		addHoveredButton(38329, 614, 145, 45, 38330);
		
		addText(38331, "Clan name:", fonts, 0, 0xff9933, true, true);
		addText(38332, "Chat disabled", fonts, 2, 0xFFFFFF, true, true);

		addText(38333, "Who can enter chat?", fonts, 0, 0xff9933, true, true);
		addText(38334, "Anyone", fonts, 2, 0xFFFFFF, true, true);

		addText(38335, "Who can talk on chat?", fonts, 0, 0xff9933, true, true);
		addText(38336, "Anyone", fonts, 2, 0xFFFFFF, true, true);

		addText(38337, "Who can kick on chat?", fonts, 0, 0xff9933, true, true);
		addText(38338, "Only me", fonts, 2, 0xFFFFFF, true, true);

		addText(38339, "Clan Chat Setup", fonts, 2, 0xff9933, true, true);

		/**
		 * Friend list inside clan setup layer
		 */

		Widget layer = addTabInterface(38350);
		int maxFriends = 200;
		layer.width = 282;
		layer.height = 238;
		layer.scrollMax = maxFriends * 16;
		int childId = 0;
		
		setChildren(402, layer);
		
		for (int i = 0, id = 38351; i < maxFriends; i++, id++) {
			addPixels(id, i % 2 == 0 ? 0x3D3428 : 0x453C31, 0x50483D, 282, 16, 0, true);
		}

		addPixels(38750, 0x383023, 1, maxFriends * 16, 0, false);// Left line
		addPixels(38751, 0x5A5245, 1, maxFriends * 16, 0, false); // Right line
		setBounds(38750, 170, 0, childId++, layer);
		setBounds(38751, 171, 0, childId++, layer);
		
		int textId = 38752;
		for (int i = 0, y = 1; i < maxFriends; i++, y += 16) {
			addHoverText(textId, "", null, fonts, 2, 0xFFFF64, false, true, 168, 0xFFFFFF);
			interfaceCache[textId].actions = new String[] {"Recruit", "Corporal", "Sergeant", "Lieutenant", "Captain", "General", "Demote"};
			setBounds(textId, 5, y, childId++, layer);
			textId++;
		}
		for (int i = 0, y = 1; i < maxFriends; i++, y += 16) {
			addText(textId, "", fonts, 2, 0xFFFFFF, false, true);
			setBounds(textId, 177, y, childId++, layer);
			textId++;
		}

		/**
		 * Default layer children
		 */
		setChildren(25, widget);
		childId = 0;
		setBounds(38301, 16, 15, childId++, widget); // Background

		setBounds(38302, 470, 25, childId++, widget); // Close sprite
		
		setBounds(38305, 184, 54, childId++, widget); // Box one
		setBounds(38306, 185, 55, childId++, widget); // Box two
		setBounds(38307, 185, 55, childId++, widget); // Box three

		setBounds(38308, 356, 56, childId++, widget); // Line one
		setBounds(38309, 357, 56, childId++, widget); // Line two

		setBounds(38350, 186, 70, childId++, widget); // Layer

/*		setBounds(38310, 186, 56, 9, widget); // Sort by name
		setBounds(38311, 186, 75, 10, widget);

		setBounds(38313, 358, 56, 11, widget); // Sort by rank
		setBounds(38314, 358, 75, 12, widget);
*/
		setBounds(38319, 25, 53, childId++, widget); // Set prefix button
		setBounds(38320, 25, 53, childId++, widget);

		setBounds(38322, 25, 103, childId++, widget); // Enter button
		setBounds(38323, 25, 103, childId++, widget);

		setBounds(38325, 25, 153, childId++, widget); // Talk button
		setBounds(38326, 25, 153, childId++, widget);

		setBounds(38328, 25, 203, childId++, widget); // Kick button
		setBounds(38329, 25, 203, childId++, widget);

		setBounds(38331, 97, 63, childId++, widget); // Clan name text
		setBounds(38332, 97, 77, childId++, widget);

		setBounds(38333, 97, 112, childId++, widget); // Enter text
		setBounds(38334, 97, 127, childId++, widget);

		setBounds(38335, 97, 162, childId++, widget); // Talk text
		setBounds(38336, 97, 177, childId++, widget);

		setBounds(38337, 97, 212, childId++, widget); // Kick text
		setBounds(38338, 97, 227, childId++, widget);

		setBounds(38339, 256, 24, childId++, widget); // Title
	}

	public static void spawnTab() {
		Widget tab = addTabInterface(31000);

		addText(31002, "Spawn Tab", fonts, 2, 0xFFFFFF, true, true);
		addText(31003, "Item", fonts, 1, 0xff8000, false, true);

		addHoverButton(31004, 330, 172, 20, "Search", -1, 31005, 1);
		addHoveredButton(31005, 331, 172, 20, 31006);

		// Inventory spawn
		addText(31010, "Inventory:", fonts, 0, 0xFFFFFF, false, true);
		addHoverButton(31007, 332, 14, 15, "Select", -1, 31008, 1);
		addHoveredButton(31008, 333, 14, 15, 31009);

		// Bank spawn
		addText(31014, "Bank:", fonts, 0, 0xFFFFFF, false, true);
		addHoverButton(31011, 332, 14, 15, "Select", -1, 31012, 1);
		addHoveredButton(31012, 333, 14, 15, 31013);

		addHoverButton(31015, 572, 79, 30, "Presets", -1, 31016, 1);
		addHoveredButton(31016, 573, 79, 30, 31017);

		addSpriteLoader(31001, 196);
		tab.totalChildren(14);

		tab.child(0, 31001, 0, 89);
		tab.child(1, 31030, 0, 91);
		tab.child(2, 31002, 95, 1);
		tab.child(3, 31004, 10, 25);
		tab.child(4, 31005, 10, 25);
		tab.child(5, 31003, 15, 28);
		tab.child(6, 31007, 75, 50);
		tab.child(7, 31008, 75, 50);
		tab.child(8, 31010, 11, 52);
		tab.child(9, 31011, 75, 70);
		tab.child(10, 31012, 75, 70);
		tab.child(11, 31014, 11, 72);

		tab.child(12, 31015, 103, 52);
		tab.child(13, 31016, 103, 52);

		/* Text area */
		Widget list = addTabInterface(31030);
		list.totalChildren(700);

		int child = 0;
		for (int i = 31031, yPos = 0; i < 31731; i++, yPos += 22) {
			addHoverText(i, "", null, fonts, 1, 0xff8000, false, true, 240, 0xFFFFFF);
			interfaceCache[i].actions = new String[] { "Spawn", "Spawn X" };
			list.children[child] = i;
			list.childX[child] = 5;
			list.childY[child] = yPos;
			child++;
		}

		list.height = 154;
		list.width = 174;
		list.scrollMax = 2200;
	}

	public static void presets() {
		Widget w = addTabInterface(45000);
		w.totalChildren(89);

		// Add background sprite
		addSprite(45001, 574);

		// Add title
		addText(45002, "Presets", fonts, 2, 0xff981f, true, false);

		// Add categories
		addText(45003, "@or1@Spellbook", fonts, 0, 00000, false, false);
		addText(45004, "@or1@Inventory", fonts, 0, 00000, false, false);
		addText(45005, "@or1@Equipment", fonts, 0, 00000, false, false);
		addText(45006, "@or1@Stats", fonts, 0, 00000, false, false);

		// Add stats strings
		for (int i = 0; i <= 6; i++) {
			addText(45007 + i, "", fonts, 2, 0xFFD700, false, false);
		}

		// Add spellbook string
		addText(45014, "", fonts, 1, 00000, true, false);

		// Add inventory
		for (int i = 0; i < 28; i++) {
			addItemOnInterface(45015 + i, 45000, new String[] {});
		}

		// Add equipment
		for (int i = 0; i < 14; i++) {
			addItemOnInterface(45044 + i, 45000, new String[] {});
		}

		// Open presets on death text
		addText(45059, "@or1@Open on death: ", fonts, 1, 00000, false, false);

		// Open presets on death config tick
		addButton(45060, 45000, 14, 15, 1, 987, 332, 334, -1, "Toggle");

		// Set preset button
		addButton(45061, 45000, 146, 26, 576, 576, 45062, "Select");
		addHoveredButton(45062, 577, 146, 26, 45063);

		// Load preset button
		addButton(45064, 45000, 146, 26, 576, 576, 45065, "Select");
		addHoveredButton(45065, 577, 146, 26, 45066);

		// Preset buttons text
		addText(45067, "Edit this preset", fonts, 2, 0xff981f, false, false);
		addText(45068, "Load this preset", fonts, 2, 0xff981f, false, false);

		closeButton(45093, 24, 25);

		// Global Presets
		Widget list = addTabInterface(45069);
		list.totalChildren(10);
		for (int i = 45070, child = 0, yPos = 3; i < 45080; i++, yPos += 20) {
			addHoverText(i, "Empty", null, fonts, 1, 0xff8000, false, true, 240, 0xFFFFFF);
			interfaceCache[i].actions = new String[] { "Select" };
			list.children[child] = i;
			list.childX[child] = 5;
			list.childY[child] = yPos;
			child++;
		}
		list.height = 98;
		list.width = 85;
		list.scrollMax = 210;

		// Global presets title
		addText(45080, "@whi@Global Presets", fonts, 0, 00000, false, false);

		// Custom Presets
		list = addTabInterface(45081);
		list.totalChildren(10);
		for (int i = 45082, child = 0, yPos = 3; i < 45092; i++, yPos += 20) {
			addHoverText(i, "Empty", null, fonts, 1, 0xff8000, false, true, 240, 0xFFFFFF);
			interfaceCache[i].actions = new String[] { "Select" };
			list.children[child] = i;
			list.childX[child] = 5;
			list.childY[child] = yPos;
			child++;
		}
		list.height = 107;
		list.width = 85;
		list.scrollMax = 210;

		// Custom presets title
		addText(45092, "@whi@Your Presets", fonts, 0, 00000, false, false);

		// Children
		int child = 0;
		w.child(child++, 45001, 7, 2); // Background sprite
		w.child(child++, 45093, 482, 5); // Close button
		w.child(child++, 45002, 253, 5); // Title
		w.child(child++, 45003, 42, 26); // Category 1 - spellbook
		w.child(child++, 45004, 180, 26); // Category 1 - inventory
		w.child(child++, 45005, 333, 26); // Category 1 - equipment
		w.child(child++, 45006, 453, 26); // Category 1 - stats

		// Stats
		for (int i = 0, yPos = 55; i <= 6; i++, yPos += 40) {
			w.child(child++, 45007 + i, 469, yPos);
		}

		// Spellbook
		w.child(child++, 45014, 65, 46); // Spellbook

		// Inventory
		for (int i = 0, xPos = 130, yPos = 48; i < 28; i++, xPos += 39) {
			w.child(child++, 45015 + i, xPos, yPos);
			if (xPos >= 247) {
				xPos = 91;
				yPos += 39;
			}
		}

		// Equipment bg sprites
		w.child(child++, 1645, 337, 149 - 52 - 17);
		w.child(child++, 1646, 337, 163 - 17);
		w.child(child++, 1647, 337, 114);
		w.child(child++, 1648, 337, 58 + 146 - 17);
		w.child(child++, 1649, 282, 110 - 44 + 118 - 13 + 5 - 17);
		w.child(child++, 1650, 260 + 22, 58 + 154 - 17);
		w.child(child++, 1651, 260 + 134, 58 + 118 - 17);
		w.child(child++, 1652, 260 + 134, 58 + 154 - 17);
		w.child(child++, 1653, 260 + 48, 58 + 81 - 17);
		w.child(child++, 1654, 260 + 107, 58 + 81 - 17);
		w.child(child++, 1655, 260 + 58, 58 + 42 - 17);
		w.child(child++, 1656, 260 + 112, 58 + 41 - 17);
		w.child(child++, 1657, 260 + 78, 58 + 4 - 17);
		w.child(child++, 1658, 260 + 37, 58 + 43 - 17);
		w.child(child++, 1659, 260 + 78, 58 + 43 - 17);
		w.child(child++, 1660, 260 + 119, 58 + 43 - 17);
		w.child(child++, 1661, 260 + 22, 58 + 82 - 17);
		w.child(child++, 1662, 260 + 78, 58 + 82 - 17);
		w.child(child++, 1663, 260 + 134, 58 + 82 - 17);
		w.child(child++, 1664, 260 + 78, 58 + 122 - 17);
		w.child(child++, 1665, 260 + 78, 58 + 162 - 17);
		w.child(child++, 1666, 260 + 22, 58 + 162 - 17);
		w.child(child++, 1667, 260 + 134, 58 + 162 - 17);

		// Equipment
		w.child(child++, 45044, 341, 47); // Head slot
		w.child(child++, 45045, 300, 86); // Cape slot
		w.child(child++, 45046, 341, 86); // Amulet slot
		w.child(child++, 45047, 285, 125); // Weapon slot
		w.child(child++, 45048, 341, 125); // Body slot
		w.child(child++, 45049, 396, 125); // Shield slot
		w.child(child++, 45051, 341, 165); // Legs slot

		w.child(child++, 45053, 285, 205); // Hands slot
		w.child(child++, 45054, 341, 205); // Feet slot
		w.child(child++, 45056, 397, 205); // Ring slot
		w.child(child++, 45057, 381, 86); // Ammo slot

		// Open preset interface on death
		w.child(child++, 45059, 300, 243); // Open presets on death text
		w.child(child++, 45060, 400, 243); // Open presets on death tick config

		// Buttons
		w.child(child++, 45061, 285, 263); // Button 1 - Save This Preset
		w.child(child++, 45062, 285, 263); // Button 1 hover - Save This Preset

		w.child(child++, 45064, 285, 294); // Button 2 - Load This Preset
		w.child(child++, 45065, 285, 294); // Button 2 hover - Load This Preset

		// Button text
		w.child(child++, 45067, 306, 267); // Save this preset text
		w.child(child++, 45068, 306, 299); // Load this preset text

		// Preset lists
		w.child(child++, 45069, 12, 90); // Global presets list
		w.child(child++, 45080, 24, 75); // Global presets list text title

		w.child(child++, 45081, 12, 214); // Custom presets list
		w.child(child++, 45092, 28, 200); // Custom presets list text title
	}

	public static void keybinding() {
		Widget tab = addTabInterface(53000);

		addSpriteLoader(53001, 430);
		addText(53002, "Keybinding", fonts, 2, 0xff8a1f, false, true);
		closeButton(53003, 142, 143);

		hoverButton(Keybinding.RESTORE_DEFAULT, "Restore Defaults", 447, 448, "Restore Defaults", newFonts[2], 0xff8a1f,
				0xff8a1f, true);

		addText(53005, "Esc closes current interface", fonts, 1, 0xff8a1f, false, true);
		configButton(Keybinding.ESCAPE_CONFIG, "Select", 348, 347);

		tab.totalChildren(48);
		int childNum = 0;

		setBounds(53001, 5, 17, childNum++, tab);
		setBounds(53002, 221, 27, childNum++, tab);
		setBounds(53003, 479, 24, childNum++, tab);
		setBounds(Keybinding.RESTORE_DEFAULT, 343, 275, childNum++, tab);
		setBounds(53005, 60, 285, childNum++, tab);
		setBounds(Keybinding.ESCAPE_CONFIG, 35, 285, childNum++, tab);

		/* Tabs and dropdowns */

		int x = 31;
		int y = 63;
		childNum = 47;

		for (int i = 0; i < 14; i++, y += 43) {

			addSpriteLoader(53007 + 3 * i, 431 + i);
			configButton(53008 + 3 * i, "", 446, 445);

			boolean inverted = i == 3 || i == 4 || i == 8 || i == 9 || i == 13;
			keybindingDropdown(53009 + 3 * i, 86, 0, Keybinding.OPTIONS, Dropdown.KEYBIND_SELECTION, inverted);

			setBounds(Keybinding.MIN_FRAME - 2 + 3 * i, x + stoneOffset(431 + i, true), y + stoneOffset(431 + i, false),
					childNum--, tab);
			setBounds(Keybinding.MIN_FRAME - 1 + 3 * i, x, y, childNum--, tab);
			setBounds(Keybinding.MIN_FRAME + 3 * i, x + 39, y + 4, childNum--, tab);

			if (i == 4 || i == 9) {
				x += 160;
				y = 20;
			}
		}
	}

	public static int stoneOffset(int spriteId, boolean xOffset) {
		Sprite stone = Client.spriteCache.lookup(445);
		Sprite icon = Client.spriteCache.lookup(spriteId);

		if (xOffset) {
			return (stone.myWidth / 2) - icon.myWidth / 2;
		}
		return (stone.myHeight / 2) - icon.myHeight / 2;
	}

	public static void addButton(int i, int parent, int w, int h, int config, int configFrame, int sprite1, int sprite2,
			int hoverOver, String tooltip) {
		Widget p = addInterface(i);
		p.parent = parent;
		p.type = TYPE_SPRITE;
		p.atActionType = 1;
		p.width = w;
		p.height = h;
		p.requiredValues = new int[1];
		p.valueCompareType = new int[1];
		p.valueCompareType[0] = 1;
		p.requiredValues[0] = config;
		p.valueIndexArray = new int[1][3];
		p.valueIndexArray[0][0] = 5;
		p.valueIndexArray[0][1] = configFrame;
		p.valueIndexArray[0][2] = 0;
		p.tooltip = tooltip;
		p.defaultText = tooltip;
		p.hoverType = hoverOver;
		p.disabledSprite = Client.spriteCache.lookup(sprite1);
		p.enabledSprite = Client.spriteCache.lookup(sprite2);
	}

	public static void addButton(int i, int parent, int w, int h, int sprite1, int sprite2, int hoverOver,
			String tooltip) {
		Widget p = addInterface(i);
		p.parent = parent;
		p.type = TYPE_SPRITE;
		p.atActionType = 1;
		p.width = w;
		p.height = h;
		p.tooltip = tooltip;
		p.defaultText = tooltip;
		p.hoverType = hoverOver;
		p.disabledSprite = Client.spriteCache.lookup(sprite1);
		p.enabledSprite = Client.spriteCache.lookup(sprite2);
	}

	public static void addButton(int i, int parent, int w, int h, Sprite sprite1, Sprite sprite2, int hoverOver,
			String tooltip) {
		Widget p = addInterface(i);
		p.parent = parent;
		p.type = TYPE_SPRITE;
		p.atActionType = 1;
		p.width = w;
		p.height = h;
		p.tooltip = tooltip;
		p.defaultText = tooltip;
		p.hoverType = hoverOver;
		p.disabledSprite = sprite1;
		p.enabledSprite = sprite2;
	}

	public static void addHoveredButtonWTooltip(int i, int spriteId, int w, int h, int IMAGEID, int tooltipId,
			String hover, int hoverXOffset, int hoverYOffset) {// hoverable
		// button
		Widget tab = addTabInterface(i);
		tab.parent = i;
		tab.id = i;
		tab.type = 0;
		tab.atActionType = 0;
		tab.width = w;
		tab.height = h;
		tab.invisible = true;
		tab.opacity = 0;
		tab.hoverType = -1;
		tab.scrollMax = 0;
		addHoverImage_sprite_loader(IMAGEID, spriteId);
		tab.totalChildren(1);
		tab.child(0, IMAGEID, 0, 0);

		Widget p = addTabInterface(tooltipId);
		p.parent = i;
		p.type = 8;
		p.width = w;
		p.height = h;

		p.hoverText = p.defaultText = p.tooltip = hover;

		p.hoverXOffset = hoverXOffset;
		p.hoverYOffset = hoverYOffset;
		p.regularHoverBox = true;

	}

	public static void prayerBook() {

		Widget rsinterface = interfaceCache[5608];

		// Moves down chivalry
		rsinterface.childX[50 + BEGIN_READING_PRAYER_INTERFACE] = 10;
		rsinterface.childY[50 + BEGIN_READING_PRAYER_INTERFACE] = 195;
		rsinterface.childX[51 + BEGIN_READING_PRAYER_INTERFACE] = 10;
		rsinterface.childY[51 + BEGIN_READING_PRAYER_INTERFACE] = 195;
		rsinterface.childX[63 + BEGIN_READING_PRAYER_INTERFACE] = 10;
		rsinterface.childY[63 + BEGIN_READING_PRAYER_INTERFACE] = 190;
		// Adjust prayer glow sprites position - Chivalry
		interfaceCache[rsinterface.children[50 + BEGIN_READING_PRAYER_INTERFACE]].spriteXOffset = -7;
		interfaceCache[rsinterface.children[50 + BEGIN_READING_PRAYER_INTERFACE]].spriteYOffset = -2;

		// Moves piety to the right
		setBounds(19827, 43, 191, 52 + BEGIN_READING_PRAYER_INTERFACE, rsinterface);

		// Adjust prayer glow sprites position - Piety
		interfaceCache[rsinterface.children[52 + BEGIN_READING_PRAYER_INTERFACE]].spriteXOffset = -2;
		interfaceCache[rsinterface.children[52 + BEGIN_READING_PRAYER_INTERFACE]].spriteYOffset = 2;

		rsinterface.childX[53 + BEGIN_READING_PRAYER_INTERFACE] = 43;
		rsinterface.childY[53 + BEGIN_READING_PRAYER_INTERFACE] = 204;
		rsinterface.childX[64 + BEGIN_READING_PRAYER_INTERFACE] = 43;
		rsinterface.childY[64 + BEGIN_READING_PRAYER_INTERFACE] = 190;

		// Now we add new prayers..
		// AddPrayer adds a glow at the id
		// Adds the actual prayer sprite at id+1
		// Adds a hover box at id + 2
		addPrayer(28001, "Activate @or1@Preserve", 31, 32, 150, -2, -1, 151, 152, 1, 708, 28003);
		setBounds(28001, 153, 158, 0, rsinterface); // Prayer glow sprite
		setBounds(28002, 153, 158, 1, rsinterface); // Prayer sprites

		addPrayer(28004, "Activate @or1@Rigour", 31, 32, 150, -3, -5, 153, 154, 1, 710, 28006);
		setBounds(28004, 84, 198, 2, rsinterface); // Prayer glow sprite
		setBounds(28005, 84, 198, 3, rsinterface); // Prayer sprites

		addPrayer(28007, "Activate @or1@Augury", 31, 32, 150, -3, -5, 155, 156, 1, 712, 28009);
		setBounds(28007, 120, 198, 4, rsinterface); // Prayer glow sprite
		setBounds(28008, 120, 198, 5, rsinterface); // Prayer sprites

		// Now we add hovers..
		addPrayerHover(28003, "Level 55\nPreserve\nBoosted stats last 20% longer.", -135, -60);
		setBounds(28003, 153, 158, 86, rsinterface); // Hover box

		addPrayerHover(28006,
				"Level 74\nRigour\nIncreases your Ranged attack\nby 20% and damage by 23%,\nand your defence by 25%",
				-70, -100);
		setBounds(28006, 84, 200, 87, rsinterface); // Hover box

		addPrayerHover(28009, "Level 77\nAugury\nIncreases your Magic attack\nby 25% and your defence by 25%", -110,
				-100);
		setBounds(28009, 120, 198, 88, rsinterface); // Hover box

	}

	public static void addPrayer(int ID, String tooltip, int w, int h, int glowSprite, int glowX, int glowY,
			int disabledSprite, int enabledSprite, int config, int configFrame, int hover) {
		Widget p = addTabInterface(ID);

		// Adding config-toggleable glow on the prayer
		// Also clickable
		p.parent = 5608;
		p.type = TYPE_SPRITE;
		p.atActionType = 1;
		p.width = w;
		p.height = h;
		p.requiredValues = new int[1];
		p.valueCompareType = new int[1];
		p.valueCompareType[0] = 1;
		p.requiredValues[0] = config;
		p.valueIndexArray = new int[1][3];
		p.valueIndexArray[0][0] = 5;
		p.valueIndexArray[0][1] = configFrame;
		p.valueIndexArray[0][2] = 0;
		p.tooltip = tooltip;
		p.defaultText = tooltip;
		p.hoverType = 52;
		p.enabledSprite = Client.spriteCache.lookup(glowSprite);
		p.spriteXOffset = glowX;
		p.spriteYOffset = glowY;

		// Adding config-toggleable prayer sprites
		// not clickable
		p = addTabInterface(ID + 1);
		p.parent = 5608;
		p.type = TYPE_SPRITE;
		p.atActionType = 0;
		p.width = w;
		p.height = h;
		p.requiredValues = new int[1];
		p.valueCompareType = new int[1];
		p.valueCompareType[0] = 2;
		p.requiredValues[0] = 1;
		p.valueIndexArray = new int[1][3];
		p.valueIndexArray[0][0] = 5;
		p.valueIndexArray[0][1] = configFrame + 1;
		p.valueIndexArray[0][2] = 0;
		p.tooltip = tooltip;
		p.defaultText = tooltip;
		p.enabledSprite = Client.spriteCache.lookup(disabledSprite); // imageLoader(disabledSprite, "s");
		p.disabledSprite = Client.spriteCache.lookup(enabledSprite); // imageLoader(enabledSprite, "s");
		p.hoverType = hover;
	}

	public static void addPrayerHover(int ID, String hover, int xOffset, int yOffset) {
		// Adding hover box
		Widget p = addTabInterface(ID);
		p.parent = 5608;
		p.type = TYPE_OTHER;
		p.width = 40;
		p.height = 32;
		p.hoverText = p.defaultText = hover;
		p.hoverXOffset = xOffset;
		p.hoverYOffset = yOffset;
		p.regularHoverBox = true;
	}

	/*
	 * Price checker interface
	 */
	private static void priceChecker() {
		Widget rsi = addTabInterface(42000);
		final String[] options = { "Remove 1", "Remove 5", "Remove 10", "Remove All", "Remove X" };
		addAdvancedSprite(18245, 180);

		addHoverButton(18247, 137, 17, 17, "Close", -1, 18250, 1);
		addHoveredButton(18250, 138, 17, 17, 18251);

		addHoverButton(18252, 181, 35, 35, "Deposit All", -1, 18253, 1);
		addHoveredButton(18253, 182, 35, 35, 18254);

		addHoverButton(18255, 183, 35, 35, "Withdraw All", -1, 18256, 1);
		addHoveredButton(18256, 184, 35, 35, 18257);

		addText(18351, "0", fonts, 0, 0xFFFFFF, true, true);
		addText(18355, "", fonts, 0, 0xFFFFFF, true, true);

		// Actual items
		Widget container = addTabInterface(18500);
		container.actions = options;
		container.spritesX = new int[20];
		container.spritesY = new int[20];
		container.inventoryItemId = new int[24];
		container.inventoryAmounts = new int[24];
		container.centerText = true;
		container.filled = false;
		container.replaceItems = false;
		container.usableItems = false;
		// rsi.isInventoryInterface = false;
		container.allowSwapItems = false;
		container.spritePaddingX = 50;
		container.spritePaddingY = 30;
		container.height = 6;
		container.width = 6;
		container.parent = 42000;
		container.type = TYPE_INVENTORY;

		rsi.totalChildren(58);
		int child = 0;

		rsi.child(child++, 18245, 10, 20);// was 10 so + 10
		rsi.child(child++, 18247, 471, 23);
		rsi.child(child++, 18351, 251, 306);
		rsi.child(child++, 18355, 260, 155);
		rsi.child(child++, 18250, 471, 23); // Close button hover
		rsi.child(child++, 18500, 28, 50); // Container

		// Deposit hovers
		rsi.child(child++, 18252, 455, 285);
		rsi.child(child++, 18253, 455, 285);

		rsi.child(child++, 18255, 420, 285);
		rsi.child(child++, 18256, 420, 285);

		// Add text next to items, ROW 1
		int interface_ = 18300;
		int xDraw = 47;
		int yDraw = 81;
		int counter = 0;
		for (int i = 0; i < container.inventoryItemId.length; i++) {

			addText(interface_, "", fonts, 0, 0xFFFFFF, true, true);
			rsi.child(child++, interface_, xDraw, yDraw);

			interface_++;
			counter++;
			xDraw += 80;

			if (counter == container.width) {
				xDraw = 47;
				yDraw += 62;
				counter = 0;
			}
		}

		// Add text next to items, ROW 2
		interface_ = 18400;
		xDraw = 47;
		yDraw = 93;
		counter = 0;
		for (int i = 0; i < container.inventoryItemId.length; i++) {

			addText(interface_, "", fonts, 0, 0xFFFFFF, true, true);
			rsi.child(child++, interface_, xDraw, yDraw);

			interface_++;
			counter++;
			xDraw += 80;

			if (counter == container.width) {
				xDraw = 47;
				yDraw += 62;
				counter = 0;
			}
		}
	}

	public static void shop() {

		// Set up the shop inventory
		Widget shopInventory = interfaceCache[3900];
		shopInventory.inventoryItemId = new int[1000];
		shopInventory.inventoryAmounts = new int[1000];
		shopInventory.drawInfinity = true;
		shopInventory.width = 9;
		shopInventory.height = 200;
		shopInventory.spritePaddingX = 18;
		shopInventory.spritePaddingY = 25;

		// The scroll, add the shop inventory to it.
		Widget scroll = addTabInterface(29995);
		scroll.totalChildren(1);
		setBounds(3900, 0, 0, 0, scroll);
		scroll.height = 210;
		scroll.width = 445;
		scroll.scrollMax = 230;

		// Position the item container in the actual shop interface
		setBounds(29995, 26, 65, 75, interfaceCache[3824]);
	}

	public static void bounty() {
		Widget tab = addTabInterface(23300);
		addTransparentSprite(23301, 97, 150);

		addConfigSprite(23303, -1, 98, 0, 876);
		// addSprite(23304, 104);

		addText(23305, "---", fonts, 0, 0xffff00, true, true);
		addText(23306, "Target:", fonts, 0, 0xffff00, true, true);
		addText(23307, "None", fonts, 1, 0xffffff, true, true);
		addText(23308, "Level: ------", fonts, 0, 0xffff00, true, true);

		addText(23309, "Current  Record", fonts, 0, 0xffff00, true, true);
		addText(23310, "0", fonts, 0, 0xffff00, true, true);
		addText(23311, "0", fonts, 0, 0xffff00, true, true);
		addText(23312, "0", fonts, 0, 0xffff00, true, true);
		addText(23313, "0", fonts, 0, 0xffff00, true, true);
		addText(23314, "Rogue:", fonts, 0, 0xffff00, true, true);
		addText(23315, "Hunter:", fonts, 0, 0xffff00, true, true);

		addConfigSprite(23316, -1, 99, 0, 877);
		addConfigSprite(23317, -1, 100, 0, 878);
		addConfigSprite(23318, -1, 101, 0, 879);
		addConfigSprite(23319, -1, 102, 0, 880);
		addConfigSprite(23320, -1, 103, 0, 881);
		addText(23321, "Level: ", fonts, 1, 0xFFFF33, true, false);

		// Kda
		addTransparentSprite(23322, 97, 150);
		addText(23323, "Targets killed: 0", fonts, 0, 0xFFFF33, true, false);
		addText(23324, "Players killed: 0", fonts, 0, 0xFFFF33, true, false);
		addText(23325, "Deaths: 0", fonts, 0, 0xFFFF33, true, false);

		tab.totalChildren(17);
		tab.child(0, 23301, 319, 1);
		tab.child(1, 23322, 319, 47);
		// tab.child(1, 23302, 339, 56);
		tab.child(2, 23303, 345, 58);
		// tab.child(2, 23304, 348, 73);
		tab.child(3, 23305, 358, 77);
		tab.child(4, 23306, 455, 51);
		tab.child(5, 23307, 456, 64);
		tab.child(6, 23308, 457, 80);
		// tab.child(8, 23309, 460, 59);
		// tab.child(9, 23310, 438, 72);
		// tab.child(10, 23311, 481, 72);
		// tab.child(11, 23312, 438, 85);
		// tab.child(12, 23313, 481, 85);
		// tab.child(13, 23314, 393, 72);
		// tab.child(14, 23315, 394, 85);
		tab.child(7, 23316, 345, 58);
		tab.child(8, 23317, 345, 58);
		tab.child(9, 23318, 345, 58);
		tab.child(10, 23319, 345, 58);
		tab.child(11, 23320, 345, 58);

		tab.child(12, 23323, 435, 6);
		tab.child(13, 23324, 435, 19);
		tab.child(14, 23325, 435, 32);

		interfaceCache[197].childX[0] = 0;
		interfaceCache[197].childY[0] = 0;

		tab.child(15, 197, 331, 6);
		tab.child(16, 23321, 361, 31);

	}

	public static void itemsKeptOnDeath() {

		removeSomething(16999); // close button in text
		Widget rsinterface = interfaceCache[10494];
		rsinterface.spritePaddingX = 6;
		rsinterface.spritePaddingY = 5;
		rsinterface = interfaceCache[10600];
		rsinterface.spritePaddingX = 6;
		rsinterface.spritePaddingY = 5;

		rsinterface = addInterface(17100);
		addSpriteLoader(17101, 139);
		/*
		 * Widget scroll = addTabInterface(17149); scroll.width = 300; scroll.height =
		 * 183; scroll.scrollMax = 220;
		 */
		addText(17103, "Items Kept on Death", fonts, 2, 0xff981f, false, false);
		addText(17104, "Items you will keep on death:", fonts, 1, 0xff981f, false, false);
		addText(17105, "Items you will lose on death:", fonts, 1, 0xff981f, false, false);
		addText(17106, "Info", fonts, 1, 0xff981f, false, false);
		addText(17107, "3", fonts, 2, 0xffff00, false, false);
		String[] options = { null };

		/*
		 * Items on interface
		 */

		// Top Row
		for (int top = 17108; top <= 17111; top++) {
			addItemOnInterface(top, 17100, options);
		}
		// 1st row
		for (int top = 17112; top <= 17119; top++) {
			addItemOnInterface(top, 17100, options);
		}
		// 2nd row
		for (int top = 17120; top <= 17127; top++) {
			addItemOnInterface(top, 17100, options);
		}
		// 3rd row
		for (int top = 17128; top <= 17135; top++) {
			addItemOnInterface(top, 17100, options);
		}
		// 4th row
		for (int top = 17136; top <= 17142; top++) {
			addItemOnInterface(top, 17100, options);
		}
		// 5th row
		for (int top = 17143; top <= 17148; top++) {
			addItemOnInterface(top, 17100, options);
		}

		// 6th row (4 items)
		for (int top = 17149; top <= 17152; top++) {
			addItemOnInterface(top, 17100, options);
		}

		setChildren(54, rsinterface);
		// addTabInterface(5);
		setBounds(17101, 7, 8, 0, rsinterface);
		setBounds(16999, 478, 14, 1, rsinterface);
		setBounds(17103, 185, 18, 2, rsinterface);
		setBounds(17104, 22, 50, 3, rsinterface);
		setBounds(17105, 22, 110, 4, rsinterface);
		setBounds(17106, 347, 50, 5, rsinterface);

		setBounds(17107, 412, 287, 6, rsinterface);
		setBounds(17149, 23, 132, 7, rsinterface);
		// setBounds(17018, 480, 18, 8, rsinterface);
		// setBounds(17019, 480, 18, 9, rsinterface);

		// setBounds(38117, 480, 18, 8, rsinterface);
		// setBounds(38118, 480, 18, 9, rsinterface); TODO close button

		// setBounds(5, 480, 18, 10, rsinterface);

		// Positions for item on interface (items kept on death
		int child_index = 8;
		int topPos = 26;
		for (int top = 17108; top <= 17111; top++) {
			setBounds(top, topPos, 72, child_index, rsinterface);
			topPos += 44;
			child_index++;
		}
		// setBounds(17000, 478, 14, child_index++, rsinterface);
		itemsOnDeathDATA();
		setBounds(17315, 348, 64, child_index++, rsinterface);

		topPos = 26;

		// 1st row
		for (int top = 17112; top <= 17118; top++) {
			setBounds(top, topPos, 133, child_index, rsinterface);
			topPos += 44;
			child_index++;
		}
		// 2nd row
		topPos = 26;
		for (int top = 17119; top <= 17125; top++) {
			setBounds(top, topPos, 168, child_index, rsinterface);
			topPos += 44;
			child_index++;
		}
		// 3rd row
		topPos = 26;
		for (int top = 17126; top <= 17132; top++) {
			setBounds(top, topPos, 203, child_index, rsinterface);
			topPos += 44;
			child_index++;
		}
		// 4th row
		topPos = 26;
		for (int top = 17133; top <= 17139; top++) {
			setBounds(top, topPos, 238, child_index, rsinterface);
			topPos += 44;
			child_index++;
		}
		// 5th row
		topPos = 26;
		for (int top = 17140; top <= 17145; top++) {
			setBounds(top, topPos, 273, child_index, rsinterface);
			topPos += 44;
			child_index++;
		}

		// 6th row (4 items)
		topPos = 26;
		for (int top = 17146; top <= 17152; top++) {
			setBounds(top, topPos, 311, child_index, rsinterface);
			topPos += 44;
			child_index++;
		}
	}

	public static void itemsOnDeathDATA() {
		Widget RSinterface = addInterface(17315);
		addText(17309, "", 0xff981f, false, false, 0, fonts, 0);
		addText(17310, "The normal amount of", 0xff981f, false, false, 0, fonts, 0);
		addText(17311, "items kept is three.", 0xff981f, false, false, 0, fonts, 0);
		addText(17312, "", 0xff981f, false, false, 0, fonts, 0);
		addText(17313, "If you are skulled,", 0xff981f, false, false, 0, fonts, 0);
		addText(17314, "you will lose all your", 0xff981f, false, false, 0, fonts, 0);
		addText(17317, "items, unless an item", 0xff981f, false, false, 0, fonts, 0);
		addText(17318, "protecting prayer is", 0xff981f, false, false, 0, fonts, 0);
		addText(17319, "used.", 0xff981f, false, false, 0, fonts, 0);
		addText(17320, "", 0xff981f, false, false, 0, fonts, 0);
		addText(17321, "Item protecting prayers", 0xff981f, false, false, 0, fonts, 0);
		addText(17322, "will allow you to keep", 0xff981f, false, false, 0, fonts, 0);
		addText(17323, "one extra item.", 0xff981f, false, false, 0, fonts, 0);
		addText(17324, "", 0xff981f, false, false, 0, fonts, 0);
		addText(17325, "The items kept are", 0xff981f, false, false, 0, fonts, 0);
		addText(17326, "selected by the server", 0xff981f, false, false, 0, fonts, 0);
		addText(17327, "and include the most", 0xff981f, false, false, 0, fonts, 0);
		addText(17328, "expensive items you're", 0xff981f, false, false, 0, fonts, 0);
		addText(17329, "carrying.", 0xff981f, false, false, 0, fonts, 0);
		addText(17330, "", 0xff981f, false, false, 0, fonts, 0);
		RSinterface.parent = 17315;
		RSinterface.id = 17315;
		RSinterface.type = 0;
		RSinterface.atActionType = 0;
		RSinterface.contentType = 0;
		RSinterface.width = 130;
		RSinterface.height = 197;
		RSinterface.opacity = 0;
		RSinterface.hoverType = -1;
		RSinterface.scrollMax = 280;
		RSinterface.children = new int[20];
		RSinterface.childX = new int[20];
		RSinterface.childY = new int[20];
		RSinterface.children[0] = 17309;
		RSinterface.childX[0] = 0;
		RSinterface.childY[0] = 0;
		RSinterface.children[1] = 17310;
		RSinterface.childX[1] = 0;
		RSinterface.childY[1] = 12;
		RSinterface.children[2] = 17311;
		RSinterface.childX[2] = 0;
		RSinterface.childY[2] = 24;
		RSinterface.children[3] = 17312;
		RSinterface.childX[3] = 0;
		RSinterface.childY[3] = 36;
		RSinterface.children[4] = 17313;
		RSinterface.childX[4] = 0;
		RSinterface.childY[4] = 48;
		RSinterface.children[5] = 17314;
		RSinterface.childX[5] = 0;
		RSinterface.childY[5] = 60;
		RSinterface.children[6] = 17317;
		RSinterface.childX[6] = 0;
		RSinterface.childY[6] = 72;
		RSinterface.children[7] = 17318;
		RSinterface.childX[7] = 0;
		RSinterface.childY[7] = 84;
		RSinterface.children[8] = 17319;
		RSinterface.childX[8] = 0;
		RSinterface.childY[8] = 96;
		RSinterface.children[9] = 17320;
		RSinterface.childX[9] = 0;
		RSinterface.childY[9] = 108;
		RSinterface.children[10] = 17321;
		RSinterface.childX[10] = 0;
		RSinterface.childY[10] = 120;
		RSinterface.children[11] = 17322;
		RSinterface.childX[11] = 0;
		RSinterface.childY[11] = 132;
		RSinterface.children[12] = 17323;
		RSinterface.childX[12] = 0;
		RSinterface.childY[12] = 144;
		RSinterface.children[13] = 17324;
		RSinterface.childX[13] = 0;
		RSinterface.childY[13] = 156;
		RSinterface.children[14] = 17325;
		RSinterface.childX[14] = 0;
		RSinterface.childY[14] = 168;
		RSinterface.children[15] = 17326;
		RSinterface.childX[15] = 0;
		RSinterface.childY[15] = 180;
		RSinterface.children[16] = 17327;
		RSinterface.childX[16] = 0;
		RSinterface.childY[16] = 192;
		RSinterface.children[17] = 17328;
		RSinterface.childX[17] = 0;
		RSinterface.childY[17] = 204;
		RSinterface.children[18] = 17329;
		RSinterface.childX[18] = 0;
		RSinterface.childY[18] = 216;
		RSinterface.children[19] = 17330;
		RSinterface.childX[19] = 0;
		RSinterface.childY[19] = 228;
	}

	public static void clanChatTab() {
		Widget tab = addTabInterface(37128);

		addButton(37129, 37128, 72, 32, 194, 195, 37130, "Select");
		addHoveredButton(37130, 195, 72, 32, 37131);

		addButton(37132, 37128, 72, 32, 194, 195, 37133, "Select");
		addHoveredButton(37133, 195, 72, 32, 37134);

		// addButton(37250, 0, "/Clan Chat/Lootshare", "Toggle lootshare");
		addText(37135, "Join Chat", fonts, 0, 0xff9b00, true, true);
		addText(37136, "Clan Setup", fonts, 0, 0xff9b00, true, true);

		addSpriteLoader(37137, 196);

		addText(37138, "Clan Chat", fonts, 2, 0xff9b00, true, true);
		addText(37139, "Talking in: Not in chat", fonts, 0, 0xff9b00, false, true);
		addText(37140, "Owner: None", fonts, 0, 0xff9b00, false, true);
		tab.totalChildren(11);
		// tab.child(0, 16126, 0, 221);
		// tab.child(1, 16126, 0, 59);

		tab.child(0, 37137, 0, 62);
		tab.child(1, 37143, 0, 64);
		tab.child(2, 37129, 15, 226);
		tab.child(3, 37130, 15, 226);
		tab.child(4, 37132, 103, 226);
		tab.child(5, 37133, 103, 226);
		tab.child(6, 37135, 51, 237);
		tab.child(7, 37136, 139, 237);
		tab.child(8, 37138, 95, 1);
		tab.child(9, 37139, 10, 23);
		tab.child(10, 37140, 25, 38);
		/* Text area */
		Widget list = addTabInterface(37143);
		list.totalChildren(100);
		for (int i = 37144; i <= 37244; i++) {
			addText(i, "", fonts, 0, 0xffffff, false, true);
		}
		for (int id = 37144, i = 0; id <= 37243 && i <= 99; id++, i++) {
			interfaceCache[id].actions = new String[] { "Promote to Recruit", "Promote to Corporal",
					"Promote to Sergeant", "Promote to Lieutenant", "Promote to Captain", "Promote to General",
					"Demote", "Kick" };
			interfaceCache[id].width = 200;
			interfaceCache[id].parent = 37128;
			list.children[i] = id;
			list.childX[i] = 5;
			for (int id2 = 37144, i2 = 1; id2 <= 37243 && i2 <= 99; id2++, i2++) {
				list.childY[0] = 2;
				list.childY[i2] = list.childY[i2 - 1] + 14;
			}
		}
		list.height = 154;
		list.width = 174;
		list.scrollMax = 1405;
	}

	public static void addHoverText2(int id, String text, String[] tooltips, GameFont tda[], int idx, int color,
			boolean center, boolean textShadowed, int width) {
		Widget rsinterface = addInterface(id);
		rsinterface.id = id;
		rsinterface.parent = id;
		rsinterface.type = 4;
		rsinterface.atActionType = 1;
		rsinterface.width = width;
		rsinterface.height = 11;
		rsinterface.contentType = 0;
		rsinterface.opacity = 0;
		rsinterface.hoverType = -1;
		rsinterface.centerText = center;
		rsinterface.textShadow = textShadowed;
		rsinterface.textDrawingAreas = tda[idx];
		rsinterface.defaultText = text;
		rsinterface.secondaryText = "";
		rsinterface.textColor = color;
		rsinterface.secondaryColor = 0;
		rsinterface.defaultHoverColor = 0xffffff;
		rsinterface.secondaryHoverColor = 0;
		rsinterface.tooltips = tooltips;
	}

	public static void addText2(int id, String text, GameFont tda[], int idx, int color, boolean center,
			boolean shadow) {
		Widget tab = addTabInterface(id);
		tab.parent = id;
		tab.id = id;
		tab.type = 4;
		tab.atActionType = 0;
		tab.width = 0;
		tab.height = 11;
		tab.contentType = 0;
		tab.opacity = 0;
		tab.hoverType = -1;
		tab.centerText = center;
		tab.textShadow = shadow;
		tab.textDrawingAreas = tda[idx];
		tab.defaultText = text;
		tab.secondaryText = "";
		tab.textColor = color;
		tab.secondaryColor = 0;
		tab.defaultHoverColor = 0;
		tab.secondaryHoverColor = 0;
	}

	public static void addAdvancedSprite(int id, int spriteId) {
		Widget widget = addInterface(id);
		widget.id = id;
		widget.parent = id;
		widget.type = 5;
		widget.atActionType = 0;
		widget.contentType = 0;
		widget.hoverType = 52;
		widget.enabledSprite = Client.spriteCache.lookup(spriteId);
		widget.disabledSprite = Client.spriteCache.lookup(spriteId);
		widget.drawsTransparent = true;
		widget.opacity = 64;
		widget.width = 512;
		widget.height = 334;
	}

	public static void addConfigSprite(int id, int spriteId, int spriteId2, int state, int config) {
		Widget widget = addTabInterface(id);
		widget.id = id;
		widget.parent = id;
		widget.type = 5;
		widget.atActionType = 0;
		widget.contentType = 0;
		widget.width = 512;
		widget.height = 334;
		widget.opacity = 0;
		widget.hoverType = -1;
		widget.valueCompareType = new int[1];
		widget.requiredValues = new int[1];
		widget.valueCompareType[0] = 1;
		widget.requiredValues[0] = state;
		widget.valueIndexArray = new int[1][3];
		widget.valueIndexArray[0][0] = 5;
		widget.valueIndexArray[0][1] = config;
		widget.valueIndexArray[0][2] = 0;
		widget.enabledSprite = spriteId < 0 ? null : Client.spriteCache.lookup(spriteId);
		widget.disabledSprite = spriteId2 < 0 ? null : Client.spriteCache.lookup(spriteId2);
	}

	public static void addSprite(int id, int spriteId) {
		Widget rsint = interfaceCache[id] = new Widget();
		rsint.id = id;
		rsint.parent = id;
		rsint.type = 5;
		rsint.atActionType = 0;
		rsint.contentType = 0;
		rsint.opacity = 0;
		rsint.hoverType = 0;

		if (spriteId != -1) {
			rsint.disabledSprite = Client.spriteCache.lookup(spriteId);
			rsint.enabledSprite = Client.spriteCache.lookup(spriteId);
		}

		rsint.width = 0;
		rsint.height = 0;
	}

	public static void addText(int id, String text, GameFont wid[], int idx, int color) {
		Widget rsinterface = addTabInterface(id);
		rsinterface.id = id;
		rsinterface.parent = id;
		rsinterface.type = 4;
		rsinterface.atActionType = 0;
		rsinterface.width = 174;
		rsinterface.height = 11;
		rsinterface.contentType = 0;
		rsinterface.opacity = 0;
		rsinterface.centerText = false;
		rsinterface.textShadow = true;
		rsinterface.textDrawingAreas = wid[idx];
		rsinterface.defaultText = text;
		rsinterface.secondaryText = "";
		rsinterface.textColor = color;
		rsinterface.secondaryColor = 0;
		rsinterface.defaultHoverColor = 0;
		rsinterface.secondaryHoverColor = 0;
	}

	public static void equipmentTab() {
		Widget Interface = interfaceCache[1644];
		addSprite(15101, 0, "Interfaces/Equipment/bl");// cheap hax
		addSprite(15102, 1, "Interfaces/Equipment/bl");// cheap hax
		addSprite(15109, 2, "Interfaces/Equipment/bl");// cheap hax
		removeConfig(21338);
		removeConfig(21344);
		removeConfig(21342);
		removeConfig(21341);
		removeConfig(21340);
		removeConfig(15103);
		removeConfig(15104);
		// Interface.children[23] = 15101;
		// Interface.childX[23] = 40;
		// Interface.childY[23] = 205;
		Interface.children[24] = 15102;
		Interface.childX[24] = 110;
		Interface.childY[24] = 205;
		Interface.children[25] = 15109;
		Interface.childX[25] = 39;
		Interface.childY[25] = 240;
		Interface.children[26] = 27650;
		Interface.childX[26] = 0;
		Interface.childY[26] = 0;
		Interface = addInterface(27650);

		addHoverButton(27651, 146, 40, 40, "Price-checker", -1, 27652, 1);
		addHoveredButton(27652, 147, 40, 40, 27658);

		addHoverButton(27653, 144, 40, 40, "Show Equipment Stats", -1, 27655, 1);
		addHoveredButton(27655, 145, 40, 40, 27665);

		addHoverButton(27654, 148, 40, 40, "Show items kept on death", -1, 27657, 1);
		addHoveredButton(27657, 149, 40, 40, 27666);

		setChildren(6, Interface);
		setBounds(27651, 75, 205, 0, Interface);
		setBounds(27652, 75, 205, 1, Interface);
		setBounds(27653, 23, 205, 2, Interface);
		setBounds(27654, 127, 205, 3, Interface);
		setBounds(27655, 23, 205, 4, Interface);
		setBounds(27657, 127, 205, 5, Interface);
	}

	public static void removeConfig(int id) {
		@SuppressWarnings("unused")
		Widget rsi = interfaceCache[id] = new Widget();
	}

	public static void equipmentScreen() {
		Widget Interface = Widget.interfaceCache[1644];
		addButton(19144, 140, "Show Equipment Stats");
		removeSomething(19145);
		removeSomething(19146);
		removeSomething(19147);
		// setBounds(19144, 21, 210, 23, Interface);
		setBounds(19145, 40, 210, 24, Interface);
		setBounds(19146, 40, 210, 25, Interface);
		setBounds(19147, 40, 210, 26, Interface);
		Widget tab = addTabInterface(15106);
		addSpriteLoader(15107, 141);

		addHoverButton(15210, 142, 21, 21, "Close", 250, 15211, 3);
		addHoveredButton(15211, 143, 21, 21, 15212);

		addText(15111, "Equip Your Character...", fonts, 2, 0xe4a146, false, true);
		addText(15112, "Attack bonus", fonts, 2, 0xe4a146, false, true);
		addText(15113, "Defence bonus", fonts, 2, 0xe4a146, false, true);
		addText(15114, "Other bonuses", fonts, 2, 0xe4a146, false, true);

		addText(15115, "Melee maxhit: 1", fonts, 1, 0xe4a146, false, true);
		addText(15116, "Ranged maxhit: 1", fonts, 1, 0xe4a146, false, true);
		addText(15117, "Magic maxhit: 1", fonts, 1, 0xe4a146, false, true);

		for (int i = 1675; i <= 1684; i++) {
			textSize(i, fonts, 1);
		}
		textSize(1686, fonts, 1);
		textSize(1687, fonts, 1);
		addChar(15125);
		tab.totalChildren(47);
		tab.child(0, 15107, 4, 20);
		tab.child(1, 15210, 476, 29);
		tab.child(2, 15211, 476, 29);
		tab.child(3, 15111, 14, 30);
		int Child = 4;
		int Y = 69;
		for (int i = 1675; i <= 1679; i++) {
			tab.child(Child, i, 20, Y);
			Child++;
			Y += 14;
		}
		tab.child(9, 1680, 20, 161);
		tab.child(10, 1681, 20, 177);
		tab.child(11, 1682, 20, 192);
		tab.child(12, 1683, 20, 207);
		tab.child(13, 1684, 20, 221);
		tab.child(14, 1686, 20, 262);
		tab.child(15, 15125, 170, 200);
		tab.child(16, 15112, 16, 55);
		tab.child(17, 1687, 20, 276);
		tab.child(18, 15113, 16, 147);
		tab.child(19, 15114, 16, 248);
		tab.child(20, 1645, 104 + 295, 149 - 52);
		tab.child(21, 1646, 399, 163);
		tab.child(22, 1647, 399, 163);
		tab.child(23, 1648, 399, 58 + 146);
		tab.child(24, 1649, 26 + 22 + 297 - 2, 110 - 44 + 118 - 13 + 5);
		tab.child(25, 1650, 321 + 22, 58 + 154);
		tab.child(26, 1651, 321 + 134, 58 + 118);
		tab.child(27, 1652, 321 + 134, 58 + 154);
		tab.child(28, 1653, 321 + 48, 58 + 81);
		tab.child(29, 1654, 321 + 107, 58 + 81);
		tab.child(30, 1655, 321 + 58, 58 + 42);
		tab.child(31, 1656, 321 + 112, 58 + 41);
		tab.child(32, 1657, 321 + 78, 58 + 4);
		tab.child(33, 1658, 321 + 37, 58 + 43);
		tab.child(34, 1659, 321 + 78, 58 + 43);
		tab.child(35, 1660, 321 + 119, 58 + 43);
		tab.child(36, 1661, 321 + 22, 58 + 82);
		tab.child(37, 1662, 321 + 78, 58 + 82);
		tab.child(38, 1663, 321 + 134, 58 + 82);
		tab.child(39, 1664, 321 + 78, 58 + 122);
		tab.child(40, 1665, 321 + 78, 58 + 162);
		tab.child(41, 1666, 321 + 22, 58 + 162);
		tab.child(42, 1667, 321 + 134, 58 + 162);
		tab.child(43, 1688, 50 + 297 - 2, 110 - 13 + 5);

		// Maxhits
		tab.child(44, 15115, 370, 260);
		tab.child(45, 15116, 370, 275);
		tab.child(46, 15117, 370, 290);

		for (int i = 1675; i <= 1684; i++) {
			Widget rsi = interfaceCache[i];
			rsi.textColor = 0xe4a146;
			rsi.centerText = false;
		}
		for (int i = 1686; i <= 1687; i++) {
			Widget rsi = interfaceCache[i];
			rsi.textColor = 0xe4a146;
			rsi.centerText = false;
		}
	}

	public static void addChar(int ID) {
		Widget t = interfaceCache[ID] = new Widget();
		t.id = ID;
		t.parent = ID;
		t.type = 6;
		t.atActionType = 0;
		t.contentType = 328;
		t.width = 136;
		t.height = 168;
		t.opacity = 0;
		t.modelZoom = 560;
		t.modelRotation1 = 150;
		t.modelRotation2 = 0;
		t.defaultAnimationId = -1;
		t.secondaryAnimationId = -1;
	}

	public static void addButton(int id, int sid, String tooltip) {
		Widget tab = interfaceCache[id] = new Widget();
		tab.id = id;
		tab.parent = id;
		tab.type = 5;
		tab.atActionType = 1;
		tab.contentType = 0;
		tab.opacity = (byte) 0;
		tab.hoverType = 52;
		tab.disabledSprite = Client.spriteCache.lookup(sid);// imageLoader(sid, spriteName);
		tab.enabledSprite = Client.spriteCache.lookup(sid);// imageLoader(sid, spriteName);
		tab.width = tab.disabledSprite.myWidth;
		tab.height = tab.enabledSprite.myHeight;
		tab.tooltip = tooltip;
	}

	public static void addTooltipBox(int id, String text) {
		Widget rsi = addInterface(id);
		rsi.id = id;
		rsi.parent = id;
		rsi.type = 8;
		rsi.defaultText = text;
	}

	public static void addTooltip(int id, String text) {
		Widget rsi = addInterface(id);
		rsi.id = id;
		rsi.type = 0;
		rsi.invisible = true;
		rsi.hoverType = -1;
		addTooltipBox(id + 1, text);
		rsi.totalChildren(1);
		rsi.child(0, id + 1, 0, 0);
	}

	public static Widget addInterface(int id) {
		Widget rsi = interfaceCache[id] = new Widget();
		rsi.id = id;
		rsi.parent = id;
		rsi.width = 512;
		rsi.height = 334;
		return rsi;
	}

	public static void addText(int id, String text, GameFont tda[], int idx, int color, boolean centered) {
		Widget rsi = interfaceCache[id] = new Widget();
		if (centered)
			rsi.centerText = true;
		rsi.textShadow = true;
		rsi.textDrawingAreas = tda[idx];
		rsi.defaultText = text;
		rsi.textColor = color;
		rsi.id = id;
		rsi.type = 4;
	}

	public static void textColor(int id, int color) {
		Widget rsi = interfaceCache[id];
		rsi.textColor = color;
	}

	public static void textSize(int id, GameFont tda[], int idx) {
		Widget rsi = interfaceCache[id];
		rsi.textDrawingAreas = tda[idx];
	}

	public static void addCacheSprite(int id, int sprite1, int sprite2, String sprites) {
		Widget rsi = interfaceCache[id] = new Widget();
		//rsi.disabledSprite = getSprite(sprite1, interfaceLoader, sprites);
		//rsi.enabledSprite = getSprite(sprite2, interfaceLoader, sprites);
		rsi.parent = id;
		rsi.id = id;
		rsi.type = 5;
	}

	public static void sprite1(int id, int sprite) {
		Widget class9 = interfaceCache[id];
		class9.disabledSprite = Client.spriteCache.lookup(sprite);
	}

	public static void addActionButton(int id, int sprite, int sprite2, int width, int height, String s) {
		Widget rsi = interfaceCache[id] = new Widget();
		rsi.disabledSprite = Client.spriteCache.lookup(sprite);
		if (sprite2 == sprite)
			rsi.enabledSprite = Client.spriteCache.lookup(sprite);
		else
			rsi.enabledSprite = Client.spriteCache.lookup(sprite2);
		rsi.tooltip = s;
		rsi.contentType = 0;
		rsi.atActionType = 1;
		rsi.width = width;
		rsi.hoverType = 52;
		rsi.parent = id;
		rsi.id = id;
		rsi.type = 5;
		rsi.height = height;
	}

	public static void addToggleButton(int id, int sprite, int setconfig, int width, int height, String s) {
		Widget rsi = addInterface(id);
		rsi.disabledSprite = Client.spriteCache.lookup(sprite);
		rsi.enabledSprite = Client.spriteCache.lookup(sprite);
		rsi.requiredValues = new int[1];
		rsi.requiredValues[0] = 1;
		rsi.valueCompareType = new int[1];
		rsi.valueCompareType[0] = 1;
		rsi.valueIndexArray = new int[1][3];
		rsi.valueIndexArray[0][0] = 5;
		rsi.valueIndexArray[0][1] = setconfig;
		rsi.valueIndexArray[0][2] = 0;
		rsi.atActionType = 4;
		rsi.width = width;
		rsi.hoverType = -1;
		rsi.parent = id;
		rsi.id = id;
		rsi.type = 5;
		rsi.height = height;
		rsi.tooltip = s;
	}

	public static void removeSomething(int id) {
		@SuppressWarnings("unused")
		Widget rsi = interfaceCache[id] = new Widget();
	}

	public static void quickPrayers() {
		int frame = 0;
		Widget tab = addTabInterface(17200);

		addTransparentSprite(17235, 131, 50);
		addSpriteLoader(17201, 132);
		addText(17231, "Select your quick prayers below.", fonts, 0, 0xFF981F, false, true);

		int child = 17202;
		int config = 620;
		for (int i = 0; i < 29; i++) {
			addConfigButton(child++, 17200, 134, 133, 14, 15, "Select", 0, 1, config++);
		}

		addHoverButton(17232, 135, 190, 24, "Confirm Selection", -1, 17233, 1);
		addHoveredButton(17233, 136, 190, 24, 17234);

		setChildren(64, tab);//
		setBounds(5632, 5, 8 + 20, frame++, tab);
		setBounds(5633, 44, 8 + 20, frame++, tab);
		setBounds(5634, 79, 11 + 20, frame++, tab);
		setBounds(19813, 116, 10 + 20, frame++, tab);
		setBounds(19815, 153, 9 + 20, frame++, tab);
		setBounds(5635, 5, 48 + 20, frame++, tab);
		setBounds(5636, 44, 47 + 20, frame++, tab);
		setBounds(5637, 79, 49 + 20, frame++, tab);
		setBounds(5638, 116, 50 + 20, frame++, tab);
		setBounds(5639, 154, 50 + 20, frame++, tab);
		setBounds(5640, 4, 84 + 20, frame++, tab);
		setBounds(19817, 44, 87 + 20, frame++, tab);
		setBounds(19820, 81, 85 + 20, frame++, tab);
		setBounds(5641, 117, 85 + 20, frame++, tab);
		setBounds(5642, 156, 87 + 20, frame++, tab);
		setBounds(5643, 5, 125 + 20, frame++, tab);
		setBounds(5644, 43, 124 + 20, frame++, tab);
		setBounds(13984, 83, 124 + 20, frame++, tab);
		setBounds(5645, 115, 121 + 20, frame++, tab);
		setBounds(19822, 154, 124 + 20, frame++, tab);
		setBounds(19824, 5, 160 + 20, frame++, tab);
		setBounds(5649, 41, 158 + 20, frame++, tab);
		setBounds(5647, 79, 163 + 20, frame++, tab);
		setBounds(5648, 116, 158 + 20, frame++, tab);

		// Preserve
		setBounds(28002, 157, 160 + 20, frame++, tab);

		// Chivarly
		setBounds(19826, 10, 208, frame++, tab);

		// Piety
		setBounds(19828, 45, 207 + 13, frame++, tab);

		// Rigour
		setBounds(28005, 85, 210, frame++, tab);

		// Augury
		setBounds(28008, 124, 210, frame++, tab);

		setBounds(17235, 0, 25, frame++, tab);// Faded backing
		setBounds(17201, 0, 22, frame++, tab);// Split
		setBounds(17201, 0, 237, frame++, tab);// Split

		setBounds(17202, 5 - 3, 8 + 17, frame++, tab);
		setBounds(17203, 44 - 3, 8 + 17, frame++, tab);
		setBounds(17204, 79 - 3, 8 + 17, frame++, tab);
		setBounds(17205, 116 - 3, 8 + 17, frame++, tab);
		setBounds(17206, 153 - 3, 8 + 17, frame++, tab);
		setBounds(17207, 5 - 3, 48 + 17, frame++, tab);
		setBounds(17208, 44 - 3, 48 + 17, frame++, tab);
		setBounds(17209, 79 - 3, 48 + 17, frame++, tab);
		setBounds(17210, 116 - 3, 48 + 17, frame++, tab);
		setBounds(17211, 153 - 3, 48 + 17, frame++, tab);
		setBounds(17212, 5 - 3, 85 + 17, frame++, tab);
		setBounds(17213, 44 - 3, 85 + 17, frame++, tab);
		setBounds(17214, 79 - 3, 85 + 17, frame++, tab);
		setBounds(17215, 116 - 3, 85 + 17, frame++, tab);
		setBounds(17216, 153 - 3, 85 + 17, frame++, tab);
		setBounds(17217, 5 - 3, 124 + 17, frame++, tab);
		setBounds(17218, 44 - 3, 124 + 17, frame++, tab);
		setBounds(17219, 79 - 3, 124 + 17, frame++, tab);
		setBounds(17220, 116 - 3, 124 + 17, frame++, tab);
		setBounds(17221, 153 - 3, 124 + 17, frame++, tab);
		setBounds(17222, 5 - 3, 160 + 17, frame++, tab);
		setBounds(17223, 44 - 3, 160 + 17, frame++, tab);
		setBounds(17224, 79 - 3, 160 + 17, frame++, tab);
		setBounds(17225, 116 - 3, 160 + 17, frame++, tab);
		setBounds(17226, 153 - 3, 160 + 17, frame++, tab);

		setBounds(17227, 1, 207 + 4, frame++, tab); // Chivalry toggle button
		setBounds(17228, 41, 207 + 4, frame++, tab); // Piety toggle button
		setBounds(17229, 77, 207 + 4, frame++, tab); // Rigour toggle button
		setBounds(17230, 116, 207 + 4, frame++, tab); // Augury toggle button

		setBounds(17231, 5, 5, frame++, tab);// text
		setBounds(17232, 0, 237, frame++, tab);// confirm
		setBounds(17233, 0, 237, frame++, tab);// Confirm hover
	}

	private static void addTransparentSprite(int id, int spriteId, int transparency) {
		Widget tab = interfaceCache[id] = new Widget();
		tab.id = id;
		tab.parent = id;
		tab.type = 5;
		tab.atActionType = 0;
		tab.contentType = 0;
		tab.transparency = transparency;
		tab.hoverType = 52;
		tab.disabledSprite = Client.spriteCache.lookup(spriteId);
		tab.enabledSprite = Client.spriteCache.lookup(spriteId);
		tab.width = 512;
		tab.height = 334;
		tab.drawsTransparent = true;
	}

	public static void Pestpanel() {
		Widget RSinterface = addInterface(21119);
		addText(21120, "What", 0x999999, false, true, 52, fonts, 1);
		addText(21121, "What", 0x33cc00, false, true, 52, fonts, 1);
		addText(21122, "(Need 5 to 25 players)", 0xFFcc33, false, true, 52, fonts, 1);
		addText(21123, "Points", 0x33ccff, false, true, 52, fonts, 1);
		int last = 4;
		RSinterface.children = new int[last];
		RSinterface.childX = new int[last];
		RSinterface.childY = new int[last];
		setBounds(21120, 15, 12, 0, RSinterface);
		setBounds(21121, 15, 30, 1, RSinterface);
		setBounds(21122, 15, 48, 2, RSinterface);
		setBounds(21123, 15, 66, 3, RSinterface);
	}

	public static void Pestpanel2() {
		Widget RSinterface = addInterface(21100);
		addSprite(21101, 0, "Pest Control/PEST1");
		addSprite(21102, 1, "Pest Control/PEST1");
		addSprite(21103, 2, "Pest Control/PEST1");
		addSprite(21104, 3, "Pest Control/PEST1");
		addSprite(21105, 4, "Pest Control/PEST1");
		addSprite(21106, 5, "Pest Control/PEST1");
		addText(21107, "", 0xCC00CC, false, true, 52, fonts, 1);
		addText(21108, "", 0x0000FF, false, true, 52, fonts, 1);
		addText(21109, "", 0xFFFF44, false, true, 52, fonts, 1);
		addText(21110, "", 0xCC0000, false, true, 52, fonts, 1);
		addText(21111, "250", 0x99FF33, false, true, 52, fonts, 1);// w purp
		addText(21112, "250", 0x99FF33, false, true, 52, fonts, 1);// e blue
		addText(21113, "250", 0x99FF33, false, true, 52, fonts, 1);// se yel
		addText(21114, "250", 0x99FF33, false, true, 52, fonts, 1);// sw red
		addText(21115, "200", 0x99FF33, false, true, 52, fonts, 1);// attacks
		addText(21116, "0", 0x99FF33, false, true, 52, fonts, 1);// knights hp
		addText(21117, "Time Remaining:", 0xFFFFFF, false, true, 52, fonts, 0);
		addText(21118, "", 0xFFFFFF, false, true, 52, fonts, 0);
		int last = 18;
		RSinterface.children = new int[last];
		RSinterface.childX = new int[last];
		RSinterface.childY = new int[last];
		setBounds(21101, 361, 26, 0, RSinterface);
		setBounds(21102, 396, 26, 1, RSinterface);
		setBounds(21103, 436, 26, 2, RSinterface);
		setBounds(21104, 474, 26, 3, RSinterface);
		setBounds(21105, 3, 21, 4, RSinterface);
		setBounds(21106, 3, 50, 5, RSinterface);
		setBounds(21107, 371, 60, 6, RSinterface);
		setBounds(21108, 409, 60, 7, RSinterface);
		setBounds(21109, 443, 60, 8, RSinterface);
		setBounds(21110, 479, 60, 9, RSinterface);
		setBounds(21111, 362, 10, 10, RSinterface);
		setBounds(21112, 398, 10, 11, RSinterface);
		setBounds(21113, 436, 10, 12, RSinterface);
		setBounds(21114, 475, 10, 13, RSinterface);
		setBounds(21115, 32, 32, 14, RSinterface);
		setBounds(21116, 32, 62, 15, RSinterface);
		setBounds(21117, 8, 88, 16, RSinterface);
		setBounds(21118, 87, 88, 17, RSinterface);
	}

	public static void addHoverBox(int id, int ParentID, String text, String text2, int configId, int configFrame) {
		Widget rsi = addTabInterface(id);
		rsi.id = id;
		rsi.parent = ParentID;
		rsi.type = 8;
		rsi.secondaryText = text;
		rsi.defaultText = text2;
		rsi.valueCompareType = new int[1];
		rsi.requiredValues = new int[1];
		rsi.valueCompareType[0] = 1;
		rsi.requiredValues[0] = configId;
		rsi.valueIndexArray = new int[1][3];
		rsi.valueIndexArray[0][0] = 5;
		rsi.valueIndexArray[0][1] = configFrame;
		rsi.valueIndexArray[0][2] = 0;
	}

	public static void addItemOnInterface(int childId, int interfaceId, String[] options) {
		Widget rsi = interfaceCache[childId] = new Widget();
		rsi.actions = new String[5];
		rsi.spritesX = new int[20];
		rsi.inventoryItemId = new int[30];
		rsi.inventoryAmounts = new int[30];
		rsi.spritesY = new int[20];
		rsi.children = new int[0];
		rsi.childX = new int[0];
		rsi.childY = new int[0];
		for (int i = 0; i < rsi.actions.length; i++) {
			if (i < options.length) {
				if (options[i] != null) {
					rsi.actions[i] = options[i];
				}
			}
		}
		rsi.centerText = true;
		rsi.filled = false;
		rsi.replaceItems = false;
		rsi.usableItems = false;
		// rsi.isInventoryInterface = false;
		rsi.allowSwapItems = false;
		rsi.spritePaddingX = 4;
		rsi.spritePaddingY = 5;
		rsi.height = 1;
		rsi.width = 1;
		rsi.parent = interfaceId;
		rsi.id = childId;
		rsi.type = TYPE_INVENTORY;
	}

	public static void addToItemGroup(int id, int w, int h, int x, int y, boolean actions, String action1,
			String action2, String action3) {
		Widget rsi = addInterface(id);
		rsi.width = w;
		rsi.height = h;
		rsi.inventoryItemId = new int[w * h];
		rsi.inventoryAmounts = new int[w * h];
		rsi.usableItems = false;
		// rsi.isInventoryInterface = false;
		// rsi.isMouseoverTriggered = false;
		rsi.spritePaddingX = x;
		rsi.spritePaddingY = y;
		rsi.spritesX = new int[20];
		rsi.spritesY = new int[20];
		rsi.sprites = new Sprite[20];
		rsi.actions = new String[5];
		if (actions) {
			rsi.actions[0] = action1;
			rsi.actions[1] = action2;
			rsi.actions[2] = action3;
		}
		rsi.type = TYPE_INVENTORY;
	}

	public static void addItem(int id, int itemId, int amount) {
		addItem(id, itemId, amount, false, null, null, null, null);
	}

	public static void addItem(int id, int itemId, int amount, boolean actions, String action1, String action2,
			String action3, String action4) {
		Widget rsi = addInterface(id);
		rsi.width = 1;
		rsi.height = 1;
		rsi.inventoryItemId = new int[] { itemId + 1 };
		rsi.inventoryAmounts = new int[] { amount };
		rsi.usableItems = false;
		rsi.spritePaddingX = 16;
		rsi.spritePaddingY = 16;
		rsi.spritesX = new int[20];
		rsi.spritesY = new int[20];
		rsi.sprites = new Sprite[20];
		rsi.type = TYPE_INVENTORY;
		rsi.actions = new String[5];
		if (actions) {
			rsi.actions[0] = action1;
			rsi.actions[1] = action2;
			rsi.actions[2] = action3;
			rsi.actions[3] = action4;
		}
	}

	public static void addText(int id, String text, GameFont tda[], int idx, int color, boolean center,
			boolean shadow) {
		addText(id, text, tda, idx, color, center, false, false, shadow);
	}

	public static void addRightAlignedText(int id, String text, GameFont tda[], int idx, int color, boolean shadow) {
		addText(id, text, tda, idx, color, false, true, false, shadow);
	}

	public static void addRollingText(int id, String text, GameFont tda[], int idx, int color, boolean shadow) {
		addText(id, text, tda, idx, color, false, false, true, shadow);
	}

	public static void addText(int id, String text, GameFont[] tda, int idx, int color, boolean center,
			boolean rightAligned, boolean rollingText, boolean shadow) {
		Widget tab = addTabInterface(id);
		tab.parent = id;
		tab.id = id;
		tab.type = 4;
		tab.atActionType = 0;
		tab.width = 0;
		tab.height = 11;
		tab.contentType = 0;
		tab.opacity = 0;
		tab.hoverType = -1;
		tab.centerText = center;
		tab.rightAlignedText = rightAligned;
		tab.rollingText = rollingText;
		tab.textShadow = shadow;
		tab.textDrawingAreas = tda[idx];
		tab.defaultText = text;
		tab.secondaryText = "";
		tab.textColor = color;
		tab.secondaryColor = 0;
		tab.defaultHoverColor = 0;
		tab.secondaryHoverColor = 0;
	}

	public static void addText(int i, String s, int k, boolean l, boolean m, int a, GameFont[] TDA, int j) {
		Widget RSInterface = addInterface(i);
		RSInterface.parent = i;
		RSInterface.id = i;
		RSInterface.type = 4;
		RSInterface.atActionType = 0;
		RSInterface.width = 0;
		RSInterface.height = 0;
		RSInterface.contentType = 0;
		RSInterface.opacity = 0;
		RSInterface.hoverType = a;
		RSInterface.centerText = l;
		RSInterface.textShadow = m;
		RSInterface.textDrawingAreas = TDA[j];
		RSInterface.defaultText = s;
		RSInterface.secondaryText = "";
		RSInterface.textColor = k;
	}

	public static void addConfigButton(int ID, int pID, int bID, int bID2, int width, int height, String tT,
			int configID, int aT, int configFrame) {
		Widget Tab = addTabInterface(ID);
		Tab.parent = pID;
		Tab.id = ID;
		Tab.type = 5;
		Tab.atActionType = aT;
		Tab.contentType = 0;
		Tab.width = width;
		Tab.height = height;
		Tab.opacity = 0;
		Tab.hoverType = -1;
		Tab.valueCompareType = new int[1];
		Tab.requiredValues = new int[1];
		Tab.valueCompareType[0] = 1;
		Tab.requiredValues[0] = configID;
		Tab.valueIndexArray = new int[1][3];
		Tab.valueIndexArray[0][0] = 5;
		Tab.valueIndexArray[0][1] = configFrame;
		Tab.valueIndexArray[0][2] = 0;
		Tab.disabledSprite = Client.spriteCache.lookup(bID);// imageLoader(bID, bName);
		Tab.enabledSprite = Client.spriteCache.lookup(bID2);
		Tab.tooltip = tT;
	}

	public static void addSprite(int id, int spriteId, String spriteName) {
		Widget tab = interfaceCache[id] = new Widget();
		tab.id = id;
		tab.parent = id;
		tab.type = 5;
		tab.atActionType = 0;
		tab.contentType = 0;
		tab.opacity = (byte) 0;
		tab.hoverType = 52;
		tab.disabledSprite = imageLoader(spriteId, spriteName);
		tab.enabledSprite = imageLoader(spriteId, spriteName);
		tab.width = 512;
		tab.height = 334;
	}

	public static void bankInterface() {
		Widget bank = addInterface(5292);

		setChildren(50, bank);

		int id = 50000;
		int child = 0;

		Sprite disabled = Client.spriteCache.lookup(129);
		Sprite enabled = Client.spriteCache.lookup(130);
		/// Sprite button1 = getSprite(0, interfaceLoader, "miscgraphics");
		// Sprite button2 = getSprite(9, interfaceLoader, "miscgraphics");

		addSprite(id, 106);
		addHoverButton(id + 1, 107, 17, 17, "Close", -1, id + 2, 1);
		addHoveredButton(id + 2, 108, 17, 17, id + 3);

		bank.child(child++, id, 12, 2);
		bank.child(child++, id + 1, 472, 9);
		bank.child(child++, id + 2, 472, 9);

        addHoverButton(id + 4, 115, 36, 36, "Deposit Inventory", -1, id + 5, 1);
        addHoveredButton(id + 5, 116, 36, 36, id + 6);

        addHoverButton(id + 7, 115, 36, 36, "Deposit Equipment", -1, id + 8, 1);
        addHoveredButton(id + 8, 116, 36, 36, id + 9);

        addHoverButtonWConfig(id + 10, 115, 116, 36, 36, "Search", -1, id + 11, 1, 1, 117);
        addHoveredButton(id + 11, 116, 36, 36, id + 12);

        bank.child(child++, id + 4, 415, 292);
        bank.child(child++, id + 5, 415, 292);
        bank.child(child++, id + 7, 455, 292);
        bank.child(child++, id + 8, 455, 292);
        bank.child(child++, id + 10, 375, 292);
        bank.child(child++, id + 11, 375, 292);

		addButton(id + 13, getSprite(0, graphics, "miscgraphics3"), getSprite(0, graphics, "miscgraphics3"), "Configure", 25, 25);
		addSprite(id + 14, 209);
		bank.child(child++, id + 13, 463, 43);
		bank.child(child++, id + 14, 463, 44);
		
		addSprite(id + 15, 117);
		bank.child(child++, id + 15, 384, 300);
		addSprite(id + 16, 118);
        bank.child(child++, id + 16, 418, 300);
        addSprite(id + 17, 119);
        bank.child(child++, id + 17, 458, 299);
        
		// Text
		addText(id + 53, "%1", fonts, 0, 0xFE9624, true);
		Widget line = addInterface(id + 54);
		line.type = TYPE_RECTANGLE;
		line.allowSwapItems = true;
		line.width = 14;
		line.height = 1;
		line.textColor = 0xFE9624;
		addText(id + 55, "352", fonts, 0, 0xFE9624, true);
		bank.child(child++, id + 53, 30, 8);
		bank.child(child++, id + 54, 24, 19);
		bank.child(child++, id + 55, 30, 20);

		bank.child(child++, 5383, 190, 12);
		bank.child(child++, 5385, 0, 79);
		bank.child(child++, 8131, 102, 306);
		bank.child(child++, 8130, 17, 306);
		bank.child(child++, 5386, 282, 306);
		bank.child(child++, 5387, 197, 306);
		bank.child(child++, 8132, 127, 309);
		bank.child(child++, 8133, 45, 309);
		bank.child(child++, 5390, 54, 291);
		bank.child(child++, 5389, 227, 309);
		bank.child(child++, 5391, 311, 309);
		bank.child(child++, 5388, 248, 291);

		id = 50070;
		for (int tab = 0, counter = 0; tab <= 36; tab += 4, counter++) {

			// addHoverButton_sprite_loader(id + 1 + tab, 206, 39, 40, null, -1, id + 2 +
			// tab, 1);
			// addHoveredButton_sprite_loader(id + 2 + tab, 207, 39, 40, id + 3 + tab);

			int[] requiredValues = new int[] { 1 };
			int[] valueCompareType = new int[] { 1 };
			int[][] valueIndexArray = new int[1][3];
			valueIndexArray[0][0] = 5;
			valueIndexArray[0][1] = 1000 + counter; // Config
			valueIndexArray[0][2] = 0;

			addHoverConfigButton(id + tab, id + 1 + tab, 206, -1, 39, 40, null, valueCompareType, requiredValues,
					valueIndexArray);
			addHoveredConfigButton(interfaceCache[id + tab], id + 1 + tab, id + 2 + tab, 207, -1);

			// addHoverButtonWConfig(id + 1 + tab, 206, -1, 39, 40, null, -1, id + 2 + tab,
			// 1, 1, 1000+counter);
			// addHoveredButton_sprite_loader(id + 2 + tab, 207, 39, 40, id + 3 + tab);

			interfaceCache[id + tab].actions = new String[] { "Select", tab == 0 ? null : "Collapse", null, null,
					null };
			interfaceCache[id + tab].parent = id;
			interfaceCache[id + tab].hidden = true;
			interfaceCache[id + 1 + tab].parent = id;
			bank.child(child++, id + tab, 19 + 40 * (tab / 4), 37);
			bank.child(child++, id + 1 + tab, 19 + 40 * (tab / 4), 37);
		}

		interfaceCache[5385].height = 206;
		interfaceCache[5385].width = 474;

		int[] interfaces = new int[] { 5386, 5387, 8130, 8131 };

		for (int rsint : interfaces) {
			interfaceCache[rsint].disabledSprite = disabled;
			interfaceCache[rsint].enabledSprite = enabled;
			interfaceCache[rsint].width = enabled.myWidth;
			interfaceCache[rsint].height = enabled.myHeight;
		}

		addSprite(50040, 208);
		bank.child(child++, 50040, 20, 41);

		final Widget scrollBar = Widget.interfaceCache[5385];
		scrollBar.totalChildren(Bank.MAX_BANK_TABS);
		for (int i = 0; i < Bank.MAX_BANK_TABS; i++) {
			addBankTabContainer(50300 + i, 109, 10, 35, 352, new String[] { "Withdraw-1", "Withdraw-5", "Withdraw-10",
					"Withdraw-All", "Withdraw-X", null, "Withdraw-All but one" });
			scrollBar.child(i, 50300 + i, 40, 0);
		}
	}

	public static void teleportTab() {
		Widget tab = addInterface(28100);
		addText(28101, "Teleports", fonts, 2, 0xFFA500, true, true);
		String[] teleports = { "Home", "Training", "Skilling", "PVM", "Wildy PVM", "PVP", "Minigames" };
		dropdownMenu(28102, 90, 0, teleports, Dropdown.TELEPORT_SELECTION,
				new int[] { 0x0d0d0b, 0xd0914d, 0x342821, 0x342821, 0x787169 }, true);
		addSpriteLoader(28104, 114);
		Widget main = addInterface(28103); // Changeable widget

		tab.totalChildren(4);
		tab.child(0, 28101, 99, 10);
		tab.child(1, 28104, 0, 67);
		tab.child(2, 28103, 0, 70);
		tab.child(3, 28102, 55, 34);

		main.height = 175;
		main.totalChildren(13);
		String[] options = new String[] { "Home", "Last", "Favourite", "Favourite", "Favourite" };

		int y = 1;
		for (int i = 0; i < options.length; i++, y += 35) {
			hoverButton(28110 + i, "Teleport", 113, 112, 80);
			addText(28115 + i, options[i], fonts, 1, 0xFFA500, true, true);

			if (i >= 2) {
				addSpriteLoader(28120 + i, 110);
				main.child(8 + i, 28120 + i, 63, y + 11);
			}

			main.child(i, 28110 + i, 0, y);
			main.child(5 + i, 28115 + i, i >= 2 ? 102 : 99, y + 9);
		}

		trainingTeleports();
		skillingTeleports();
		pvpTeleports();
		minigameTeleports();
		pvmTeleports();
		wildyPvmTeleports();
	}

	public static void trainingTeleports() {
		Widget scroll = addInterface(28200);
		scroll.width = 174;
		scroll.height = 175;
		scroll.scrollMax = 281;

		String[] options = { "Cows", "Rock Crabs", "Yaks", "Slayer Tower", "Edgeville Dungeon", "Taverley Dungeon",
				"Brimhaven Dungeon", "Rellekka Dungeon" };
		scroll.totalChildren(16);

		teleportOptions(scroll, fonts, options, 28201, 28230);
	}

	public static void skillingTeleports() {
		Widget scroll = addInterface(28250);
		scroll.width = 174;
		scroll.height = 175;
		scroll.scrollMax = 246;

		String[] options = { "Miskillania", "Sailor's Hut", "Agility Courses", "Woodcutting Guild", "East Ardougne",
				"Resource Area", "Resource Area" };
		scroll.totalChildren(14);

		teleportOptions(scroll, fonts, options, 28251, 28280);
	}

	public static void pvpTeleports() {
		Widget scroll = addInterface(28300);
		scroll.width = 174;
		scroll.height = 175;
		scroll.scrollMax = 246;

		String[] options = { "Edgeville", "Safe PVP", "Mage Bank", "Easts", "Wests", "Lava Dragons", "Level 44" };
		scroll.totalChildren(14);

		teleportOptions(scroll, fonts, options, 28301, 28330);
	}

	public static void minigameTeleports() {
		Widget scroll = addInterface(28350);
		String dir = "Teleports/hover";
		scroll.width = 174;
		scroll.height = 175;
		scroll.scrollMax = 421;

		String[] options = { "Raids", "Penance Queen", "Enraged Void Knight", "Duel Arena", "Barrows", "2 Jads 1 Wave",
				"RfAD", "Warriors Guild", "League of Legends Guild", "Gunnar's Game", "Hunger Games", "Mystery Bow" };
		scroll.totalChildren(24);

		teleportOptions(scroll, fonts, options, 28351, 28380);
	}

	public static void pvmTeleports() {
		Widget scroll = addInterface(28400);
		scroll.width = 174;
		scroll.height = 175;
		scroll.scrollMax = 491;

		String[] options = { "Abyssal Sire", "Alter Ego", "Barrelchest", "Cave Kraken", "Cerberus", "Corporeal Beast",
				"Dagannoth Kings", "Demonic Gorillas", "Godwars", "Giant Mole", "Hydra", "Kalphite Queen",
				"Lizardman Shaman", "Zulrah" };
		scroll.totalChildren(28);

		teleportOptions(scroll, fonts, options, 28401, 28450);
	}

	public static void wildyPvmTeleports() {
		Widget scroll = addInterface(28150);
		scroll.width = 174;
		scroll.height = 175;
		scroll.scrollMax = 386;

		String[] options = { "Callisto", "Chaos Elemental", "Chaos Fanatic", "Crazy Archaeologist", "KBD",
				"Magma Zulrah", "Skotizo", "Scorpia", "Smoke Devil", "Venenatis", "Vet'ion" };
		scroll.totalChildren(22);

		teleportOptions(scroll, fonts, options, 28151, 29190);
	}

	public static void teleportOptions(Widget scroll, GameFont[] tda, String[] options, int hoverFrameStart,
			int textFrameStart) {
		int y = 1;
		for (int frame = 0; frame < options.length; frame++, y += 35) {
			hoverButton(hoverFrameStart + frame, "Teleport", 113, 112, 80);
			addText(textFrameStart + frame, options[frame], tda, 1, 0xFFA500, true, true);

			scroll.child(frame, hoverFrameStart + frame, 0, y);
			scroll.child(frame + options.length, textFrameStart + frame, 99, y + 9);
		}
	}

	public static void questTab() {
		Widget tab = addInterface(638);

		addText(640, "Dragonfire", fonts, 1, 0xffb000, true, true);

		addText(663, "Server:", fonts, 1, 0xffb000, true, true);
		addText(663, "Server Time:", fonts, 0, 0xffb000, true, true);
		addText(663, "Players Online:", fonts, 0, 0xffb000, true, true);
		addText(663, "Skill of the Day:", fonts, 0, 0xffb000, true, true);

		// addHoverText(iD, "", "View", TDA, 0, 0xff0000, false, true, 150);
	}

	public static void addHoverText(int id, String text, String tooltip, GameFont tda[], int idx, int color,
			boolean center, boolean textShadow, int width, int hoveredColor) {
		Widget rsinterface = addInterface(id);
		rsinterface.id = id;
		rsinterface.parent = id;
		rsinterface.type = 4;
		rsinterface.atActionType = 1;
		rsinterface.width = width;
		rsinterface.height = 13;
		rsinterface.contentType = 0;
		rsinterface.opacity = 0;
		rsinterface.hoverType = -1;
		rsinterface.centerText = center;
		rsinterface.textShadow = textShadow;
		rsinterface.textDrawingAreas = tda[idx];
		rsinterface.defaultText = text;
		rsinterface.secondaryText = "";
		rsinterface.textColor = color;
		rsinterface.secondaryColor = 0;
		rsinterface.defaultHoverColor = hoveredColor;
		rsinterface.secondaryHoverColor = 0;
		rsinterface.tooltip = tooltip;
	}

	/**
	 * Bank settings
	 *
	 * @param t
	 */
	public static void bankSettings() {
		Widget tab = addInterface(32500);
		addSprite(32501, 229);
		addText(32502, "" + Configuration.CLIENT_NAME + " Bank Settings", 0xff9933, true, true, -1, fonts, 2);

		addHoverButton(32503, 107, 21, 21, "Close", -1, 32504, 1);
		addHoveredButton(32504, 108, 21, 21, 32505);

		addConfigButton(32506, 32500, 230, 231, 14, 15, "Select", 0, 5, 1111);
		addConfigButton(32507, 32500, 230, 231, 14, 15, "Select", 1, 5, 1111);
		addConfigButton(32508, 32500, 230, 231, 14, 15, "Select", 2, 5, 1111);

		addText(32509, "First item in tab", 0xff9933, true, true, -1, fonts, 1);
		addText(32510, "Digit (1, 2, 3)", 0xff9933, true, true, -1, fonts, 1);
		addText(32511, "Roman numeral (I, II, III)", 0xff9933, true, true, -1, fonts, 1);
		addHoverText(32512, "Back to bank", "View", fonts, 1, 0xcc8000, true, true, 100, 0xFFFFFF);

		addConfigButton(32513, 32500, 230, 231, 14, 15, "Select", 1, 1, 118);
        addText(32514, "Toggle placeholders", 0xff9933, true, true, -1, fonts, 1);
        addHoverText(32515, "Release placeholders", "Select", fonts, 0, 0xcc8000, true, true, 100, 0xFFFFFF);
        
		tab.totalChildren(14);
		tab.child(0, 32501, 115, 35);
		tab.child(1, 32502, 263, 44);
		tab.child(2, 32503, 373, 42);
		tab.child(3, 32504, 373, 42);
		tab.child(4, 32506, 150, 65 + 30);
		tab.child(5, 32507, 150, 65 + 60);
		tab.child(6, 32508, 150, 65 + 90);
		tab.child(7, 32509, 218, 65 + 30);
		tab.child(8, 32510, 210, 65 + 60);
		tab.child(9, 32511, 239, 65 + 90);
		tab.child(10, 32512, 275, 265);
		tab.child(11, 32513, 150, 65 + 120);
		tab.child(12, 32514, 231, 65 + 120);
		tab.child(13, 32515, 182, 65 + 138);
	}

	public static void addHoveredConfigButton(Widget original, int ID, int IMAGEID, int disabledID, int enabledID) {
		Widget rsint = addTabInterface(ID);
		rsint.parent = original.id;
		rsint.id = ID;
		rsint.type = 0;
		rsint.atActionType = 0;
		rsint.contentType = 0;
		rsint.width = original.width;
		rsint.height = original.height;
		rsint.opacity = 0;
		rsint.hoverType = -1;
		Widget hover = addInterface(IMAGEID);
		hover.type = 5;
		hover.width = original.width;
		hover.height = original.height;
		hover.valueCompareType = original.valueCompareType;
		hover.requiredValues = original.requiredValues;
		hover.valueIndexArray = original.valueIndexArray;
		if (disabledID != -1)
			hover.disabledSprite = Client.spriteCache.lookup(disabledID);
		if (enabledID != -1)
			hover.enabledSprite = Client.spriteCache.lookup(enabledID);
		rsint.totalChildren(1);
		setBounds(IMAGEID, 0, 0, 0, rsint);
		rsint.tooltip = original.tooltip;
		rsint.invisible = true;
	}

	public static void addHoverConfigButton(int id, int hoverOver, int disabledID, int enabledID, int width, int height,
			String tooltip, int[] valueCompareType, int[] requiredValues, int[][] valueIndexArray) {
		Widget rsint = addTabInterface(id);
		rsint.parent = id;
		rsint.id = id;
		rsint.type = 5;
		rsint.atActionType = 5;
		rsint.contentType = 206;
		rsint.width = width;
		rsint.height = height;
		rsint.opacity = 0;
		rsint.hoverType = hoverOver;
		rsint.valueCompareType = valueCompareType;
		rsint.requiredValues = requiredValues;
		rsint.valueIndexArray = valueIndexArray;
		if (disabledID != -1)
			rsint.disabledSprite = Client.spriteCache.lookup(disabledID);
		if (enabledID != -1)
			rsint.enabledSprite = Client.spriteCache.lookup(enabledID);
		rsint.tooltip = tooltip;
	}

	public static void addButton(int id, Sprite enabled, Sprite disabled, String tooltip, int w, int h) {
		Widget tab = interfaceCache[id] = new Widget();
		tab.id = id;
		tab.parent = id;
		tab.type = 5;
		tab.atActionType = 1;
		tab.contentType = 0;
		tab.opacity = (byte) 0;
		tab.hoverType = 52;
		tab.disabledSprite = disabled;
		tab.enabledSprite = enabled;
		tab.width = w;
		tab.height = h;
		tab.tooltip = tooltip;
	}

	public static void addConfigButton(int ID, int pID, Sprite disabled, Sprite enabled, int width, int height,
			String tT, int configID, int aT, int configFrame) {
		Widget Tab = addTabInterface(ID);
		Tab.parent = pID;
		Tab.id = ID;
		Tab.type = 5;
		Tab.atActionType = aT;
		Tab.contentType = 0;
		Tab.width = width;
		Tab.height = height;
		Tab.opacity = 0;
		Tab.hoverType = -1;
		Tab.valueCompareType = new int[1];
		Tab.requiredValues = new int[1];
		Tab.valueCompareType[0] = 1;
		Tab.requiredValues[0] = configID;
		Tab.valueIndexArray = new int[1][3];
		Tab.valueIndexArray[0][0] = 5;
		Tab.valueIndexArray[0][1] = configFrame;
		Tab.valueIndexArray[0][2] = 0;
		Tab.disabledSprite = disabled;
		Tab.enabledSprite = enabled;
		Tab.tooltip = tT;
	}

	public static Widget addBankTabContainer(int id, int contentType, int width, int height, int size,
			String... actions) {
		Widget container = addInterface(id);
		container.parent = 5382;
		container.type = 2;
		container.contentType = contentType;
		container.width = width;
		container.height = height;
		container.sprites = new Sprite[20];
		container.spritesX = new int[20];
		container.spritesY = new int[20];
		container.spritePaddingX = 12;
		container.spritePaddingY = 8;
		container.inventoryItemId = new int[size]; // 10 bank tabs
		container.inventoryAmounts = new int[size]; // 10 bank tabs
		container.allowSwapItems = true;
		container.actions = actions;
		return container;
	}

	public static void addSpriteLoader(int childId, int spriteId) {
		Widget rsi = interfaceCache[childId] = new Widget();
		rsi.id = childId;
		rsi.parent = childId;
		rsi.type = 5;
		rsi.atActionType = 0;
		rsi.contentType = 0;
		rsi.disabledSprite = Client.spriteCache.lookup(spriteId);
		rsi.enabledSprite = Client.spriteCache.lookup(spriteId);

		// rsi.sprite1.spriteLoader = rsi.sprite2.spriteLoader = true;
		// rsi.hoverSprite1 = Client.spriteCache.lookup(hoverSpriteId];
		// rsi.hoverSprite2 = Client.spriteCache.lookup(hoverSpriteId];
		// rsi.hoverSprite1.spriteLoader = rsi.hoverSprite2.spriteLoader = true;
		// rsi.sprite1 = rsi.sprite2 = spriteId;
		// rsi.hoverSprite1Id = rsi.hoverSprite2Id = hoverSpriteId;
		rsi.width = rsi.disabledSprite.myWidth;
		rsi.height = rsi.enabledSprite.myHeight - 2;
		// rsi.isFalseTooltip = true;
	}

	public static void addSprite(int childId, Sprite sprite1, Sprite sprite2) {
		Widget rsi = interfaceCache[childId] = new Widget();
		rsi.id = childId;
		rsi.parent = childId;
		rsi.type = 5;
		rsi.atActionType = 0;
		rsi.contentType = 0;
		rsi.disabledSprite = sprite1;
		rsi.enabledSprite = sprite2;
		rsi.width = rsi.disabledSprite.myWidth;
		rsi.height = rsi.enabledSprite.myHeight - 2;
	}

	public static void addButtonWSpriteLoader(int id, int spriteId) {
		Widget tab = interfaceCache[id] = new Widget();
		tab.id = id;
		tab.parent = id;
		tab.type = 5;
		tab.atActionType = 1;
		tab.contentType = 0;
		tab.opacity = (byte) 0;
		tab.hoverType = 52;
		tab.disabledSprite = Client.spriteCache.lookup(spriteId);
		tab.enabledSprite = Client.spriteCache.lookup(spriteId);
		tab.width = tab.disabledSprite.myWidth;
		tab.height = tab.enabledSprite.myHeight - 2;
	}

	public static void addButtonWithConfig(int id, int disabledSpriteId, int enabledSpriteId, int width, int height,
			String text, int configId, int configFrame) {
		Widget tab = addTabInterface(id);
		tab.id = id;
		tab.parent = id;
		tab.type = 5;
		tab.atActionType = 1;
		tab.contentType = -1;
		tab.opacity = 0;
		tab.width = width;
		tab.height = height;
		tab.tooltip = text;
		tab.valueCompareType = new int[1];
		tab.requiredValues = new int[1];
		tab.valueCompareType[0] = 1;
		tab.requiredValues[0] = configId;
		tab.valueIndexArray = new int[1][3];
		tab.valueIndexArray[0][0] = 5;
		tab.valueIndexArray[0][1] = configFrame;
		tab.valueIndexArray[0][2] = 0;
		if (disabledSpriteId != -1)
			tab.disabledSprite = Client.spriteCache.lookup(disabledSpriteId);
		if (enabledSpriteId != -1)
			tab.enabledSprite = Client.spriteCache.lookup(enabledSpriteId);
	}

	public static void addHoverButtonWConfig(int i, int spriteId, int spriteId2, int width, int height, String text,
			int contentType, int hoverOver, int aT, int configId, int configFrame) {// hoverable
		// button
		Widget tab = addTabInterface(i);
		tab.id = i;
		tab.parent = i;
		tab.type = 5;
		tab.atActionType = aT;
		tab.contentType = contentType;
		tab.opacity = 0;
		tab.hoverType = hoverOver;
		tab.width = width;
		tab.height = height;
		tab.tooltip = text;
		tab.valueCompareType = new int[1];
		tab.requiredValues = new int[1];
		tab.valueCompareType[0] = 1;
		tab.requiredValues[0] = configId;
		tab.valueIndexArray = new int[1][3];
		tab.valueIndexArray[0][0] = 5;
		tab.valueIndexArray[0][1] = configFrame;
		tab.valueIndexArray[0][2] = 0;
		if (spriteId != -1)
			tab.disabledSprite = Client.spriteCache.lookup(spriteId);
		if (spriteId2 != -1)
			tab.enabledSprite = Client.spriteCache.lookup(spriteId2);
	}

	public static void addHoverButton(int i, int spriteId, int width, int height, String text,
			int contentType, int hoverOver, int aT, String[] actions) {
		addHoverButton(i, spriteId, width, height, text, contentType, hoverOver, aT);
		interfaceCache[i].actions = actions;
	}
	
	public static void addHoverButton(int i, int spriteId, int width, int height, String text,
			int contentType, int hoverOver, int aT) {// hoverable
		// button
		Widget tab = addTabInterface(i);
		tab.id = i;
		tab.parent = i;
		tab.type = 5;
		tab.atActionType = aT;
		tab.contentType = contentType;
		tab.opacity = 0;
		tab.hoverType = hoverOver;
		tab.disabledSprite = Client.spriteCache.lookup(spriteId);
		tab.enabledSprite = Client.spriteCache.lookup(spriteId);
		tab.width = width;
		tab.height = height;
		tab.tooltip = text;
	}

	public static void addHoveredButton(int i, int spriteId, int w, int h, int IMAGEID) {// hoverable
		// button
		Widget tab = addTabInterface(i);
		tab.parent = i;
		tab.id = i;
		tab.type = 0;
		tab.atActionType = 0;
		tab.width = w;
		tab.height = h;
		tab.invisible = true;
		tab.opacity = 0;
		tab.hoverType = -1;
		tab.scrollMax = 0;
		addHoverImage_sprite_loader(IMAGEID, spriteId);
		tab.totalChildren(1);
		tab.child(0, IMAGEID, 0, 0);
	}

	public static void addHoverImage_sprite_loader(int i, int spriteId) {
		Widget tab = addTabInterface(i);
		tab.id = i;
		tab.parent = i;
		tab.type = 5;
		tab.atActionType = 0;
		tab.contentType = 0;
		tab.width = 512;
		tab.height = 334;
		tab.opacity = 0;
		tab.hoverType = 52;
		tab.disabledSprite = Client.spriteCache.lookup(spriteId);
		tab.enabledSprite = Client.spriteCache.lookup(spriteId);
	}

	public static void addBankItem(int index, Boolean hasOption) {
		Widget rsi = interfaceCache[index] = new Widget();
		rsi.actions = new String[5];
		rsi.spritesX = new int[20];
		rsi.inventoryAmounts = new int[30];
		rsi.inventoryItemId = new int[30];
		rsi.spritesY = new int[20];

		rsi.children = new int[0];
		rsi.childX = new int[0];
		rsi.childY = new int[0];

		// rsi.hasExamine = false;

		rsi.spritePaddingX = 24;
		rsi.spritePaddingY = 24;
		rsi.height = 5;
		rsi.width = 6;
		rsi.parent = 5292;
		rsi.id = index;
		rsi.type = 2;
	}

	public static void addHoveredButton(int i, String imageName, int j, int w, int h, int IMAGEID) {// hoverable
		// button
		Widget tab = addTabInterface(i);
		tab.parent = i;
		tab.id = i;
		tab.type = 0;
		tab.atActionType = 0;
		tab.width = w;
		tab.height = h;
		tab.invisible = true;
		tab.opacity = 0;
		tab.hoverType = -1;
		tab.scrollMax = 0;
		addHoverImage(IMAGEID, j, j, imageName);
		tab.totalChildren(1);
		tab.child(0, IMAGEID, 0, 0);
	}

	public static void addHoverImage(int i, int j, int k, String name) {
		Widget tab = addTabInterface(i);
		tab.id = i;
		tab.parent = i;
		tab.type = 5;
		tab.atActionType = 0;
		tab.contentType = 0;
		tab.width = 512;
		tab.height = 334;
		tab.opacity = 0;
		tab.hoverType = 52;
		tab.disabledSprite = imageLoader(j, name);
		tab.enabledSprite = imageLoader(k, name);
	}

	public static Widget addTabInterface(int id) {
		Widget tab = interfaceCache[id] = new Widget();
		tab.id = id;// 250
		tab.parent = id;// 236
		tab.type = 0;// 262
		tab.atActionType = 0;// 217
		tab.contentType = 0;
		tab.width = 512;// 220
		tab.height = 700;// 267
		tab.opacity = (byte) 0;
		tab.hoverType = -1;// Int 230
		return tab;
	}

	public static Widget addTabInterface(int id, Widget toClone) {

		Widget tab = interfaceCache[id] = new Widget();
		tab.id = id;
		tab.parent = toClone.parent;
		tab.type = toClone.type;
		tab.atActionType = toClone.atActionType;
		tab.contentType = toClone.contentType;
		tab.width = toClone.width;
		tab.height = toClone.height;
		tab.opacity = toClone.opacity;
		tab.hoverType = toClone.hoverType;

		return tab;
	}

	private static Sprite imageLoader(int i, String s) {
		long l = (StringUtils.hashSpriteName(s) << 8) + (long) i;
		Sprite sprite = (Sprite) spriteCache.get(l);
		if (sprite != null)
			return sprite;
		try {
			sprite = new Sprite(s + "" + i);
			spriteCache.put(sprite, l);
		} catch (Exception exception) {
			return null;
		}
		return sprite;
	}

	private static Sprite getSprite(int i, FileArchive streamLoader, String s) {
	    if (spriteCache == null) {
	        spriteCache = new ReferenceCache(SPRITE_CACHE_SIZE);
	    }
		long l = (StringUtils.hashSpriteName(s) << 8) + (long) i;
		Sprite sprite = (Sprite) spriteCache.get(l);
		if (sprite != null)
			return sprite;
		try {
			sprite = new Sprite(streamLoader, s, i);
			spriteCache.put(sprite, l);
		} catch (Exception _ex) {
			return null;
		}
		return sprite;
	}

	public static void method208(boolean flag, Model model) {
		int i = 0;// was parameter
		int j = 5;// was parameter
		if (flag)
			return;
		models.clear();
		if (model != null && j != 4)
			models.put(model, (j << 16) + i);
	}

	public static void addLunarHoverBox(int interface_id, int spriteOffset) {
		Widget RSInterface = addInterface(interface_id);
		RSInterface.id = interface_id;
		RSInterface.parent = interface_id;
		RSInterface.type = 5;
		RSInterface.atActionType = 0;
		RSInterface.contentType = 0;
		RSInterface.opacity = 0;
		RSInterface.hoverType = 52;
		RSInterface.disabledSprite = Client.spriteCache.lookup(LUNAR_HOVER_BOX_SPRITES_START + spriteOffset);
		RSInterface.width = 500;
		RSInterface.height = 500;
		RSInterface.tooltip = "";
	}

	public static void addLunarRune(int i, int spriteOffset, String runeName) {
		Widget RSInterface = addInterface(i);
		RSInterface.type = 5;
		RSInterface.atActionType = 0;
		RSInterface.contentType = 0;
		RSInterface.opacity = 0;
		RSInterface.hoverType = 52;
		RSInterface.disabledSprite = Client.spriteCache.lookup(LUNAR_RUNE_SPRITES_START + spriteOffset);
		RSInterface.width = 500;
		RSInterface.height = 500;
	}

	public static void addLunarText(int ID, int runeAmount, int RuneID, GameFont[] font) {
		Widget rsInterface = addInterface(ID);
		rsInterface.id = ID;
		rsInterface.parent = 1151;
		rsInterface.type = 4;
		rsInterface.atActionType = 0;
		rsInterface.contentType = 0;
		rsInterface.width = 0;
		rsInterface.height = 14;
		rsInterface.opacity = 0;
		rsInterface.hoverType = -1;
		rsInterface.valueCompareType = new int[1];
		rsInterface.requiredValues = new int[1];
		rsInterface.valueCompareType[0] = 3;
		rsInterface.requiredValues[0] = runeAmount;
		rsInterface.valueIndexArray = new int[1][4];
		rsInterface.valueIndexArray[0][0] = 4;
		rsInterface.valueIndexArray[0][1] = 3214;
		rsInterface.valueIndexArray[0][2] = RuneID;
		rsInterface.valueIndexArray[0][3] = 0;
		rsInterface.centerText = true;
		rsInterface.textDrawingAreas = font[0];
		rsInterface.textShadow = true;
		rsInterface.defaultText = "%1/" + runeAmount + "";
		rsInterface.secondaryText = "";
		rsInterface.textColor = 12582912;
		rsInterface.secondaryColor = 49152;
	}

	public static void addLunar2RunesSmallBox(int ID, int r1, int r2, int ra1, int ra2, int rune1, int lvl, String name,
			String descr, GameFont[] TDA, int spriteOffset, int suo, int type) {
		Widget rsInterface = addInterface(ID);
		rsInterface.id = ID;
		rsInterface.parent = 1151;
		rsInterface.type = 5;
		rsInterface.atActionType = type;
		rsInterface.contentType = 0;
		rsInterface.hoverType = ID + 1;
		rsInterface.spellUsableOn = suo;
		rsInterface.selectedActionName = "Cast On";
		rsInterface.width = 20;
		rsInterface.height = 20;
		rsInterface.tooltip = "Cast @gre@" + name;
		rsInterface.spellName = name;
		rsInterface.valueCompareType = new int[3];
		rsInterface.requiredValues = new int[3];
		rsInterface.valueCompareType[0] = 3;
		rsInterface.requiredValues[0] = ra1;
		rsInterface.valueCompareType[1] = 3;
		rsInterface.requiredValues[1] = ra2;
		rsInterface.valueCompareType[2] = 3;
		rsInterface.requiredValues[2] = lvl;
		rsInterface.valueIndexArray = new int[3][];
		rsInterface.valueIndexArray[0] = new int[4];
		rsInterface.valueIndexArray[0][0] = 4;
		rsInterface.valueIndexArray[0][1] = 3214;
		rsInterface.valueIndexArray[0][2] = r1;
		rsInterface.valueIndexArray[0][3] = 0;
		rsInterface.valueIndexArray[1] = new int[4];
		rsInterface.valueIndexArray[1][0] = 4;
		rsInterface.valueIndexArray[1][1] = 3214;
		rsInterface.valueIndexArray[1][2] = r2;
		rsInterface.valueIndexArray[1][3] = 0;
		rsInterface.valueIndexArray[2] = new int[3];
		rsInterface.valueIndexArray[2][0] = 1;
		rsInterface.valueIndexArray[2][1] = 6;
		rsInterface.valueIndexArray[2][2] = 0;
		rsInterface.enabledSprite = Client.spriteCache.lookup(LUNAR_ON_SPRITES_START + spriteOffset);
		rsInterface.disabledSprite = Client.spriteCache.lookup(LUNAR_OFF_SPRITES_START + spriteOffset);

		Widget hover = addInterface(ID + 1);
		hover.parent = ID;
		hover.hoverType = -1;
		hover.type = 0;
		hover.opacity = 0;
		hover.scrollMax = 0;
		hover.invisible = true;
		setChildren(7, hover);
		addLunarHoverBox(ID + 2, 0);
		setBounds(ID + 2, 0, 0, 0, hover);
		addText(ID + 3, "Level " + (lvl + 1) + ": " + name, 0xFF981F, true, true, 52, TDA, 1);
		setBounds(ID + 3, 90, 4, 1, hover);
		addText(ID + 4, descr, 0xAF6A1A, true, true, 52, TDA, 0);
		setBounds(ID + 4, 90, 19, 2, hover);
		setBounds(30016, 37, 35, 3, hover);// Rune
		setBounds(rune1, 112, 35, 4, hover);// Rune
		addLunarText(ID + 5, ra1 + 1, r1, TDA);
		setBounds(ID + 5, 50, 66, 5, hover);
		addLunarText(ID + 6, ra2 + 1, r2, TDA);
		setBounds(ID + 6, 123, 66, 6, hover);
	}

	public static void addLunar3RunesSmallBox(int ID, int r1, int r2, int r3, int ra1, int ra2, int ra3, int rune1,
			int rune2, int lvl, String name, String descr, GameFont[] TDA, int spriteOffset, int suo, int type) {
		Widget rsInterface = addInterface(ID);
		rsInterface.id = ID;
		rsInterface.parent = 1151;
		rsInterface.type = 5;
		rsInterface.atActionType = type;
		rsInterface.contentType = 0;
		rsInterface.hoverType = ID + 1;
		rsInterface.spellUsableOn = suo;
		rsInterface.selectedActionName = "Cast on";
		rsInterface.width = 20;
		rsInterface.height = 20;
		rsInterface.tooltip = "Cast @gre@" + name;
		rsInterface.spellName = name;
		rsInterface.valueCompareType = new int[4];
		rsInterface.requiredValues = new int[4];
		rsInterface.valueCompareType[0] = 3;
		rsInterface.requiredValues[0] = ra1;
		rsInterface.valueCompareType[1] = 3;
		rsInterface.requiredValues[1] = ra2;
		rsInterface.valueCompareType[2] = 3;
		rsInterface.requiredValues[2] = ra3;
		rsInterface.valueCompareType[3] = 3;
		rsInterface.requiredValues[3] = lvl;
		rsInterface.valueIndexArray = new int[4][];
		rsInterface.valueIndexArray[0] = new int[4];
		rsInterface.valueIndexArray[0][0] = 4;
		rsInterface.valueIndexArray[0][1] = 3214;
		rsInterface.valueIndexArray[0][2] = r1;
		rsInterface.valueIndexArray[0][3] = 0;
		rsInterface.valueIndexArray[1] = new int[4];
		rsInterface.valueIndexArray[1][0] = 4;
		rsInterface.valueIndexArray[1][1] = 3214;
		rsInterface.valueIndexArray[1][2] = r2;
		rsInterface.valueIndexArray[1][3] = 0;
		rsInterface.valueIndexArray[2] = new int[4];
		rsInterface.valueIndexArray[2][0] = 4;
		rsInterface.valueIndexArray[2][1] = 3214;
		rsInterface.valueIndexArray[2][2] = r3;
		rsInterface.valueIndexArray[2][3] = 0;
		rsInterface.valueIndexArray[3] = new int[3];
		rsInterface.valueIndexArray[3][0] = 1;
		rsInterface.valueIndexArray[3][1] = 6;
		rsInterface.valueIndexArray[3][2] = 0;
		rsInterface.enabledSprite = Client.spriteCache.lookup(LUNAR_ON_SPRITES_START + spriteOffset);
		rsInterface.disabledSprite = Client.spriteCache.lookup(LUNAR_OFF_SPRITES_START + spriteOffset);

		Widget hover = addInterface(ID + 1);
		hover.parent = ID;
		hover.hoverType = -1;
		hover.type = 0;
		hover.opacity = 0;
		hover.scrollMax = 0;
		hover.invisible = true;
		setChildren(9, hover);
		addLunarHoverBox(ID + 2, 0);
		setBounds(ID + 2, 0, 0, 0, hover);
		addText(ID + 3, "Level " + (lvl + 1) + ": " + name, 0xFF981F, true, true, 52, TDA, 1);
		setBounds(ID + 3, 90, 4, 1, hover);
		addText(ID + 4, descr, 0xAF6A1A, true, true, 52, TDA, 0);
		setBounds(ID + 4, 90, 19, 2, hover);
		setBounds(30016, 14, 35, 3, hover);
		setBounds(rune1, 74, 35, 4, hover);
		setBounds(rune2, 130, 35, 5, hover);
		addLunarText(ID + 5, ra1 + 1, r1, TDA);
		setBounds(ID + 5, 26, 66, 6, hover);
		addLunarText(ID + 6, ra2 + 1, r2, TDA);
		setBounds(ID + 6, 87, 66, 7, hover);
		addLunarText(ID + 7, ra3 + 1, r3, TDA);
		setBounds(ID + 7, 142, 66, 8, hover);
	}

	public static void addLunar3RunesBigBox(int ID, int r1, int r2, int r3, int ra1, int ra2, int ra3, int rune1,
			int rune2, int lvl, String name, String descr, GameFont[] TDA, int spriteOffset, int suo, int type) {
		Widget rsInterface = addInterface(ID);
		rsInterface.id = ID;
		rsInterface.parent = 1151;
		rsInterface.type = 5;
		rsInterface.atActionType = type;
		rsInterface.contentType = 0;
		rsInterface.hoverType = ID + 1;
		rsInterface.spellUsableOn = suo;
		rsInterface.selectedActionName = "Cast on";
		rsInterface.width = 20;
		rsInterface.height = 20;
		rsInterface.tooltip = "Cast @gre@" + name;
		rsInterface.spellName = name;
		rsInterface.valueCompareType = new int[4];
		rsInterface.requiredValues = new int[4];
		rsInterface.valueCompareType[0] = 3;
		rsInterface.requiredValues[0] = ra1;
		rsInterface.valueCompareType[1] = 3;
		rsInterface.requiredValues[1] = ra2;
		rsInterface.valueCompareType[2] = 3;
		rsInterface.requiredValues[2] = ra3;
		rsInterface.valueCompareType[3] = 3;
		rsInterface.requiredValues[3] = lvl;
		rsInterface.valueIndexArray = new int[4][];
		rsInterface.valueIndexArray[0] = new int[4];
		rsInterface.valueIndexArray[0][0] = 4;
		rsInterface.valueIndexArray[0][1] = 3214;
		rsInterface.valueIndexArray[0][2] = r1;
		rsInterface.valueIndexArray[0][3] = 0;
		rsInterface.valueIndexArray[1] = new int[4];
		rsInterface.valueIndexArray[1][0] = 4;
		rsInterface.valueIndexArray[1][1] = 3214;
		rsInterface.valueIndexArray[1][2] = r2;
		rsInterface.valueIndexArray[1][3] = 0;
		rsInterface.valueIndexArray[2] = new int[4];
		rsInterface.valueIndexArray[2][0] = 4;
		rsInterface.valueIndexArray[2][1] = 3214;
		rsInterface.valueIndexArray[2][2] = r3;
		rsInterface.valueIndexArray[2][3] = 0;
		rsInterface.valueIndexArray[3] = new int[3];
		rsInterface.valueIndexArray[3][0] = 1;
		rsInterface.valueIndexArray[3][1] = 6;
		rsInterface.valueIndexArray[3][2] = 0;
		rsInterface.enabledSprite = Client.spriteCache.lookup(LUNAR_ON_SPRITES_START + spriteOffset);
		rsInterface.disabledSprite = Client.spriteCache.lookup(LUNAR_OFF_SPRITES_START + spriteOffset);

		Widget hover = addInterface(ID + 1);
		hover.parent = ID;
		hover.hoverType = -1;
		hover.type = 0;
		hover.opacity = 0;
		hover.scrollMax = 0;
		hover.invisible = true;
		setChildren(9, hover);
		addLunarHoverBox(ID + 2, 1);
		setBounds(ID + 2, 0, 0, 0, hover);
		addText(ID + 3, "Level " + (lvl + 1) + ": " + name, 0xFF981F, true, true, 52, TDA, 1);
		setBounds(ID + 3, 90, 4, 1, hover);
		addText(ID + 4, descr, 0xAF6A1A, true, true, 52, TDA, 0);
		setBounds(ID + 4, 90, 21, 2, hover);
		setBounds(30016, 14, 48, 3, hover);
		setBounds(rune1, 74, 48, 4, hover);
		setBounds(rune2, 130, 48, 5, hover);
		addLunarText(ID + 5, ra1 + 1, r1, TDA);
		setBounds(ID + 5, 26, 79, 6, hover);
		addLunarText(ID + 6, ra2 + 1, r2, TDA);
		setBounds(ID + 6, 87, 79, 7, hover);
		addLunarText(ID + 7, ra3 + 1, r3, TDA);
		setBounds(ID + 7, 142, 79, 8, hover);
	}

	public static void addLunar3RunesLargeBox(int ID, int r1, int r2, int r3, int ra1, int ra2, int ra3, int rune1,
			int rune2, int lvl, String name, String descr, GameFont[] TDA, int spriteOffset, int suo, int type) {
		Widget rsInterface = addInterface(ID);
		rsInterface.id = ID;
		rsInterface.parent = 1151;
		rsInterface.type = 5;
		rsInterface.atActionType = type;
		rsInterface.contentType = 0;
		rsInterface.hoverType = ID + 1;
		rsInterface.spellUsableOn = suo;
		rsInterface.selectedActionName = "Cast on";
		rsInterface.width = 20;
		rsInterface.height = 20;
		rsInterface.tooltip = "Cast @gre@" + name;
		rsInterface.spellName = name;
		rsInterface.valueCompareType = new int[4];
		rsInterface.requiredValues = new int[4];
		rsInterface.valueCompareType[0] = 3;
		rsInterface.requiredValues[0] = ra1;
		rsInterface.valueCompareType[1] = 3;
		rsInterface.requiredValues[1] = ra2;
		rsInterface.valueCompareType[2] = 3;
		rsInterface.requiredValues[2] = ra3;
		rsInterface.valueCompareType[3] = 3;
		rsInterface.requiredValues[3] = lvl;
		rsInterface.valueIndexArray = new int[4][];
		rsInterface.valueIndexArray[0] = new int[4];
		rsInterface.valueIndexArray[0][0] = 4;
		rsInterface.valueIndexArray[0][1] = 3214;
		rsInterface.valueIndexArray[0][2] = r1;
		rsInterface.valueIndexArray[0][3] = 0;
		rsInterface.valueIndexArray[1] = new int[4];
		rsInterface.valueIndexArray[1][0] = 4;
		rsInterface.valueIndexArray[1][1] = 3214;
		rsInterface.valueIndexArray[1][2] = r2;
		rsInterface.valueIndexArray[1][3] = 0;
		rsInterface.valueIndexArray[2] = new int[4];
		rsInterface.valueIndexArray[2][0] = 4;
		rsInterface.valueIndexArray[2][1] = 3214;
		rsInterface.valueIndexArray[2][2] = r3;
		rsInterface.valueIndexArray[2][3] = 0;
		rsInterface.valueIndexArray[3] = new int[3];
		rsInterface.valueIndexArray[3][0] = 1;
		rsInterface.valueIndexArray[3][1] = 6;
		rsInterface.valueIndexArray[3][2] = 0;
		rsInterface.enabledSprite = Client.spriteCache.lookup(LUNAR_ON_SPRITES_START + spriteOffset);
		rsInterface.disabledSprite = Client.spriteCache.lookup(LUNAR_OFF_SPRITES_START + spriteOffset);
		Widget hover = addInterface(ID + 1);
		hover.parent = ID;
		hover.hoverType = -1;
		hover.type = 0;
		hover.opacity = 0;
		hover.scrollMax = 0;
		hover.invisible = true;
		setChildren(9, hover);
		addLunarHoverBox(ID + 2, 2);
		setBounds(ID + 2, 0, 0, 0, hover);
		addText(ID + 3, "Level " + (lvl + 1) + ": " + name, 0xFF981F, true, true, 52, TDA, 1);
		setBounds(ID + 3, 90, 4, 1, hover);
		addText(ID + 4, descr, 0xAF6A1A, true, true, 52, TDA, 0);
		setBounds(ID + 4, 90, 34, 2, hover);
		setBounds(30016, 14, 61, 3, hover);
		setBounds(rune1, 74, 61, 4, hover);
		setBounds(rune2, 130, 61, 5, hover);
		addLunarText(ID + 5, ra1 + 1, r1, TDA);
		setBounds(ID + 5, 26, 92, 6, hover);
		addLunarText(ID + 6, ra2 + 1, r2, TDA);
		setBounds(ID + 6, 87, 92, 7, hover);
		addLunarText(ID + 7, ra3 + 1, r3, TDA);
		setBounds(ID + 7, 142, 92, 8, hover);
	}

	public static void configureLunar() {
		constructLunar();
		addLunarRune(30003, 0, "Fire");
		addLunarRune(30004, 1, "Water");
		addLunarRune(30005, 2, "Air");
		addLunarRune(30006, 3, "Earth");
		addLunarRune(30007, 4, "Mind");
		addLunarRune(30008, 5, "Body");
		addLunarRune(30009, 6, "Death");
		addLunarRune(30010, 7, "Nature");
		addLunarRune(30011, 8, "Chaos");
		addLunarRune(30012, 9, "Law");
		addLunarRune(30013, 10, "Cosmic");
		addLunarRune(30014, 11, "Blood");
		addLunarRune(30015, 12, "Soul");
		addLunarRune(30016, 13, "Astral");

		addLunar3RunesSmallBox(30017, 9075, 554, 555, 0, 4, 3, 30003, 30004, 64, "Bake Pie",
				"Bake pies without a stove", fonts, 0, 16, 2);
		addLunar2RunesSmallBox(30025, 9075, 557, 0, 7, 30006, 65, "Cure Plant", "Cure disease on farming patch", fonts, 1,
				4, 2);
		addLunar3RunesBigBox(30032, 9075, 564, 558, 0, 0, 0, 30013, 30007, 65, "Monster Examine",
				"Detect the combat statistics of a\\nmonster", fonts, 2, 2, 2);
		addLunar3RunesSmallBox(30040, 9075, 564, 556, 0, 0, 1, 30013, 30005, 66, "NPC Contact",
				"Speak with varied NPCs", fonts, 3, 0, 2);
		addLunar3RunesSmallBox(30048, 9075, 563, 557, 0, 0, 9, 30012, 30006, 67, "Cure Other", "Cure poisoned players",
		        fonts, 4, 8, 2);
		addLunar3RunesSmallBox(30056, 9075, 555, 554, 0, 2, 0, 30004, 30003, 67, "Humidify",
				"Fills certain vessels with water", fonts, 5, 0, 5);
		addLunar3RunesSmallBox(30064, 9075, 563, 557, 1, 0, 1, 30012, 30006, 68, "Moonclan Teleport",
				"Teleports you to moonclan island", fonts, 6, 0, 5);
		addLunar3RunesBigBox(30075, 9075, 563, 557, 1, 0, 3, 30012, 30006, 69, "Tele Group Moonclan",
				"Teleports players to Moonclan\\nisland", fonts, 7, 0, 5);
		addLunar3RunesSmallBox(30083, 9075, 563, 557, 1, 0, 5, 30012, 30006, 70, "Ourania Teleport",
				"Teleports you to ourania rune altar", fonts, 8, 0, 5);
		addLunar3RunesSmallBox(30091, 9075, 564, 563, 1, 1, 0, 30013, 30012, 70, "Cure Me", "Cures Poison", fonts, 9, 0,
				5);
		addLunar2RunesSmallBox(30099, 9075, 557, 1, 1, 30006, 70, "Hunter Kit", "Get a kit of hunting gear", fonts, 10, 0,
				5);
		addLunar3RunesSmallBox(30106, 9075, 563, 555, 1, 0, 0, 30012, 30004, 71, "Waterbirth Teleport",
				"Teleports you to Waterbirth island", fonts, 11, 0, 5);
		addLunar3RunesBigBox(30114, 9075, 563, 555, 1, 0, 4, 30012, 30004, 72, "Tele Group Waterbirth",
				"Teleports players to Waterbirth\\nisland", fonts, 12, 0, 5);
		addLunar3RunesSmallBox(30122, 9075, 564, 563, 1, 1, 1, 30013, 30012, 73, "Cure Group",
				"Cures Poison on players", fonts, 13, 0, 5);
		addLunar3RunesBigBox(30130, 9075, 564, 559, 1, 1, 4, 30013, 30008, 74, "Stat Spy",
				"Cast on another player to see their\\nskill levels", fonts, 14, 8, 2);
		addLunar3RunesBigBox(30138, 9075, 563, 554, 1, 1, 2, 30012, 30003, 74, "Barbarian Teleport",
				"Teleports you to the Barbarian\\noutpost", fonts, 15, 0, 5);
		addLunar3RunesBigBox(30146, 9075, 563, 554, 1, 1, 5, 30012, 30003, 75, "Tele Group Barbarian",
				"Teleports players to the Barbarian\\noutpost", fonts, 16, 0, 5);
		addLunar3RunesSmallBox(30154, 9075, 554, 556, 1, 5, 9, 30003, 30005, 76, "Superglass Make",
				"Make glass without a furnace", fonts, 17, 16, 2);
		addLunar3RunesSmallBox(30162, 9075, 563, 555, 1, 1, 3, 30012, 30004, 77, "Khazard Teleport",
				"Teleports you to Port khazard", fonts, 18, 0, 5);
		addLunar3RunesSmallBox(30170, 9075, 563, 555, 1, 1, 7, 30012, 30004, 78, "Tele Group Khazard",
				"Teleports players to Port khazard", fonts, 19, 0, 5);
		addLunar3RunesBigBox(30178, 9075, 564, 559, 1, 0, 4, 30013, 30008, 78, "Dream",
				"Take a rest and restore hitpoints 3\\n times faster", fonts, 20, 0, 5);
		addLunar3RunesSmallBox(30186, 9075, 557, 555, 1, 9, 4, 30006, 30004, 79, "String Jewellery",
				"String amulets without wool", fonts, 21, 0, 5);
		addLunar3RunesLargeBox(30194, 9075, 557, 555, 1, 9, 9, 30006, 30004, 80, "Stat Restore Pot\\nShare",
				"Share a potion with up to 4 nearby\\nplayers", fonts, 22, 0, 5);
		addLunar3RunesSmallBox(30202, 9075, 554, 555, 1, 6, 6, 30003, 30004, 81, "Magic Imbue",
				"Combine runes without a talisman", fonts, 23, 0, 5);
		addLunar3RunesBigBox(30210, 9075, 561, 557, 2, 1, 14, 30010, 30006, 82, "Fertile Soil",
				"Fertilise a farming patch with super\\ncompost", fonts, 24, 4, 2);
		addLunar3RunesBigBox(30218, 9075, 557, 555, 2, 11, 9, 30006, 30004, 83, "Boost Potion Share",
				"Shares a potion with up to 4 nearby\\nplayers", fonts, 25, 0, 5);
		addLunar3RunesSmallBox(30226, 9075, 563, 555, 2, 2, 9, 30012, 30004, 84, "Fishing Guild Teleport",
				"Teleports you to the fishing guild", fonts, 26, 0, 5);
		addLunar3RunesLargeBox(30234, 9075, 563, 555, 1, 2, 13, 30012, 30004, 85, "Tele Group Fishing Guild",
				"Teleports players to the Fishing\\nGuild", fonts, 27, 0, 5);
		addLunar3RunesSmallBox(30242, 9075, 557, 561, 2, 14, 0, 30006, 30010, 85, "Plank Make", "Turn Logs into planks",
		        fonts, 28, 16, 5);
		addLunar3RunesSmallBox(30250, 9075, 563, 555, 2, 2, 9, 30012, 30004, 86, "Catherby Teleport",
				"Teleports you to Catherby", fonts, 29, 0, 5);
		addLunar3RunesSmallBox(30258, 9075, 563, 555, 2, 2, 14, 30012, 30004, 87, "Tele Group Catherby",
				"Teleports players to Catherby", fonts, 30, 0, 5);
		addLunar3RunesSmallBox(30266, 9075, 563, 555, 2, 2, 7, 30012, 30004, 88, "Ice Plateau Teleport",
				"Teleports you to Ice Plateau", fonts, 31, 0, 5);
		addLunar3RunesLargeBox(30274, 9075, 563, 555, 2, 2, 15, 30012, 30004, 89, "Tele Group Ice Plateau",
				"Teleports players to Ice Plateau", fonts, 32, 0, 5);
		addLunar3RunesBigBox(30282, 9075, 563, 561, 2, 1, 0, 30012, 30010, 90, "Energy Transfer",
				"Spend HP and SA energy to\\n give another SA and run energy", fonts, 33, 8, 2);
		addLunar3RunesBigBox(30290, 9075, 563, 565, 2, 2, 0, 30012, 30014, 91, "Heal Other",
				"Transfer up to 75% of hitpoints\\n to another player", fonts, 34, 8, 2);
		addLunar3RunesBigBox(30298, 9075, 560, 557, 2, 1, 9, 30009, 30006, 92, "Vengeance Other",
				"Allows another player to rebound\\ndamage to an opponent", fonts, 35, 8, 2);
		addLunar3RunesSmallBox(30306, 9075, 560, 557, 3, 1, 9, 30009, 30006, 93, "Vengeance",
				"Rebound damage to an opponent", fonts, 36, 0, 5);
		addLunar3RunesBigBox(30314, 9075, 565, 563, 3, 2, 5, 30014, 30012, 94, "Heal Group",
				"Transfer up to 75% of hitpoints\\n to a group", fonts, 37, 0, 5);
		addLunar3RunesBigBox(30322, 9075, 564, 563, 2, 1, 0, 30013, 30012, 95, "Spellbook Swap",
				"Change to another spellbook for 1\\nspell cast", fonts, 38, 0, 5);
	}

	public static void constructLunar() {
		Widget Interface = addTabInterface(29999);
		setChildren(81, Interface);
		int child = 0;

		setBounds(30017, 20, 60, child++, Interface);
		setBounds(30025, 61, 62, child++, Interface);
		setBounds(30032, 102, 61, child++, Interface);
		setBounds(30040, 142, 62, child++, Interface);

		setBounds(30048, 20, 93, child++, Interface);
		setBounds(30056, 60, 92, child++, Interface);
		setBounds(30091, 102, 92, child++, Interface);
		setBounds(30099, 142, 90, child++, Interface);

		setBounds(30122, 20, 123, child++, Interface);
		setBounds(30130, 62, 123, child++, Interface);
		setBounds(30154, 106, 123, child++, Interface);
		setBounds(30154, 147, 123, child++, Interface);

		setBounds(30178, 19, 154, child++, Interface);
		setBounds(30186, 63, 155, child++, Interface);
		setBounds(30194, 106, 155, child++, Interface);
		setBounds(30202, 145, 155, child++, Interface);

		setBounds(30210, 21, 184, child++, Interface);
		setBounds(30218, 66, 186, child++, Interface);
		setBounds(30282, 105, 184, child++, Interface);
		setBounds(30290, 145, 183, child++, Interface);

		setBounds(30298, 23, 214, child++, Interface);
		setBounds(30306, 66, 214, child++, Interface);
		setBounds(30314, 105, 214, child++, Interface);
		setBounds(30322, 147, 214, child++, Interface);

		setBounds(30064, 39, 39, child++, Interface);
		setBounds(30075, 71, 39, child++, Interface);
		setBounds(30083, 103, 39, child++, Interface);
		setBounds(30106, 12, 68, child++, Interface);
		setBounds(30114, 42, 68, child++, Interface);
		setBounds(30138, 135, 68, child++, Interface);
		setBounds(30146, 165, 68, child++, Interface);
		setBounds(30162, 42, 97, child++, Interface);
		setBounds(30170, 71, 97, child++, Interface);

		setBounds(30226, 103, 125, child++, Interface);
		setBounds(30234, 135, 125, child++, Interface);
		setBounds(30242, 164, 126, child++, Interface);
		setBounds(30250, 10, 155, child++, Interface);
		setBounds(30258, 42, 155, child++, Interface);
		setBounds(30266, 71, 155, child++, Interface);
		setBounds(30274, 103, 155, child++, Interface);

		setBounds(30018, 5, 176, child++, Interface);
		setBounds(30026, 5, 176, child++, Interface);
		setBounds(30033, 5, 163, child++, Interface);
		setBounds(30041, 5, 176, child++, Interface);
		setBounds(30049, 5, 176, child++, Interface);
		setBounds(30057, 5, 176, child++, Interface);
		setBounds(30065, 5, 176, child++, Interface);
		setBounds(30076, 5, 163, child++, Interface);
		setBounds(30084, 5, 176, child++, Interface);
		setBounds(30092, 5, 176, child++, Interface);
		setBounds(30100, 5, 176, child++, Interface);
		setBounds(30107, 5, 176, child++, Interface);
		setBounds(30115, 5, 163, child++, Interface);
		setBounds(30123, 5, 176, child++, Interface);
		setBounds(30131, 5, 163, child++, Interface);
		setBounds(30139, 5, 163, child++, Interface);
		setBounds(30147, 5, 163, child++, Interface);
		setBounds(30155, 5, 176, child++, Interface);
		setBounds(30163, 5, 176, child++, Interface);
		setBounds(30171, 5, 176, child++, Interface);
		setBounds(30179, 5, 40, child++, Interface);
		setBounds(30187, 5, 40, child++, Interface);
		setBounds(30195, 5, 40, child++, Interface);
		setBounds(30203, 5, 40, child++, Interface);
		setBounds(30211, 5, 40, child++, Interface);
		setBounds(30219, 5, 40, child++, Interface);

		setBounds(30227, 5, 176, child++, Interface);
		setBounds(30235, 5, 149, child++, Interface);
		setBounds(30243, 5, 176, child++, Interface);
		setBounds(30251, 5, 5, child++, Interface);
		setBounds(30259, 5, 5, child++, Interface);
		setBounds(30267, 5, 5, child++, Interface);
		setBounds(30275, 5, 5, child++, Interface);
		setBounds(30283, 5, 40, child++, Interface);
		setBounds(30291, 5, 40, child++, Interface);
		setBounds(30299, 5, 40, child++, Interface);
		setBounds(30307, 5, 40, child++, Interface);
		setBounds(30323, 5, 40, child++, Interface);
		setBounds(30315, 5, 40, child++, Interface);
		
		// Add home button
		setBounds(19210, 20, 60, child++, Interface);
		setBounds(19211, 2, 50, child++, Interface);
	}

	private static void levelUpInterfaces() {
		Widget attack = interfaceCache[6247];
		Widget defence = interfaceCache[6253];
		Widget str = interfaceCache[6206];
		Widget hits = interfaceCache[6216];
		Widget rng = interfaceCache[4443];
		Widget pray = interfaceCache[6242];
		Widget mage = interfaceCache[6211];
		Widget cook = interfaceCache[6226];
		Widget wood = interfaceCache[4272];
		Widget flet = interfaceCache[6231];
		Widget fish = interfaceCache[6258];
		Widget fire = interfaceCache[4282];
		Widget craf = interfaceCache[6263];
		Widget smit = interfaceCache[6221];
		Widget mine = interfaceCache[4416];
		Widget herb = interfaceCache[6237];
		Widget agil = interfaceCache[4277];
		Widget thie = interfaceCache[4261];
		Widget slay = interfaceCache[12122];
		Widget farm = addTabInterface(5267);
		Widget rune = interfaceCache[4267];
		Widget cons = addTabInterface(7267);
		Widget hunt = addTabInterface(8267);
		Widget summ = addTabInterface(9267);
		Widget dung = addTabInterface(10267);
		addSkillChatSprite(29578, 0);
		addSkillChatSprite(29579, 1);
		addSkillChatSprite(29580, 2);
		addSkillChatSprite(29581, 3);
		addSkillChatSprite(29582, 4);
		addSkillChatSprite(29583, 5);
		addSkillChatSprite(29584, 6);
		addSkillChatSprite(29585, 7);
		addSkillChatSprite(29586, 8);
		addSkillChatSprite(29587, 9);
		addSkillChatSprite(29588, 10);
		addSkillChatSprite(29589, 11);
		addSkillChatSprite(29590, 12);
		addSkillChatSprite(29591, 13);
		addSkillChatSprite(29592, 14);
		addSkillChatSprite(29593, 15);
		addSkillChatSprite(29594, 16);
		addSkillChatSprite(29595, 17);
		addSkillChatSprite(29596, 18);
		addSkillChatSprite(11897, 19);
		addSkillChatSprite(29598, 20);
		addSkillChatSprite(29599, 21);
		addSkillChatSprite(29600, 22);
		addSkillChatSprite(29601, 23);
		addSkillChatSprite(29602, 24);
		setChildren(4, attack);
		setBounds(29578, 20, 30, 0, attack);
		setBounds(4268, 80, 15, 1, attack);
		setBounds(4269, 80, 45, 2, attack);
		setBounds(358, 95, 75, 3, attack);
		setChildren(4, defence);
		setBounds(29579, 20, 30, 0, defence);
		setBounds(4268, 80, 15, 1, defence);
		setBounds(4269, 80, 45, 2, defence);
		setBounds(358, 95, 75, 3, defence);
		setChildren(4, str);
		setBounds(29580, 20, 30, 0, str);
		setBounds(4268, 80, 15, 1, str);
		setBounds(4269, 80, 45, 2, str);
		setBounds(358, 95, 75, 3, str);
		setChildren(4, hits);
		setBounds(29581, 20, 30, 0, hits);
		setBounds(4268, 80, 15, 1, hits);
		setBounds(4269, 80, 45, 2, hits);
		setBounds(358, 95, 75, 3, hits);
		setChildren(4, rng);
		setBounds(29582, 20, 30, 0, rng);
		setBounds(4268, 80, 15, 1, rng);
		setBounds(4269, 80, 45, 2, rng);
		setBounds(358, 95, 75, 3, rng);
		setChildren(4, pray);
		setBounds(29583, 20, 30, 0, pray);
		setBounds(4268, 80, 15, 1, pray);
		setBounds(4269, 80, 45, 2, pray);
		setBounds(358, 95, 75, 3, pray);
		setChildren(4, mage);
		setBounds(29584, 20, 30, 0, mage);
		setBounds(4268, 80, 15, 1, mage);
		setBounds(4269, 80, 45, 2, mage);
		setBounds(358, 95, 75, 3, mage);
		setChildren(4, cook);
		setBounds(29585, 20, 30, 0, cook);
		setBounds(4268, 80, 15, 1, cook);
		setBounds(4269, 80, 45, 2, cook);
		setBounds(358, 95, 75, 3, cook);
		setChildren(4, wood);
		setBounds(29586, 20, 30, 0, wood);
		setBounds(4268, 80, 15, 1, wood);
		setBounds(4269, 80, 45, 2, wood);
		setBounds(358, 95, 75, 3, wood);
		setChildren(4, flet);
		setBounds(29587, 20, 30, 0, flet);
		setBounds(4268, 80, 15, 1, flet);
		setBounds(4269, 80, 45, 2, flet);
		setBounds(358, 95, 75, 3, flet);
		setChildren(4, fish);
		setBounds(29588, 20, 30, 0, fish);
		setBounds(4268, 80, 15, 1, fish);
		setBounds(4269, 80, 45, 2, fish);
		setBounds(358, 95, 75, 3, fish);
		setChildren(4, fire);
		setBounds(29589, 20, 30, 0, fire);
		setBounds(4268, 80, 15, 1, fire);
		setBounds(4269, 80, 45, 2, fire);
		setBounds(358, 95, 75, 3, fire);
		setChildren(4, craf);
		setBounds(29590, 20, 30, 0, craf);
		setBounds(4268, 80, 15, 1, craf);
		setBounds(4269, 80, 45, 2, craf);
		setBounds(358, 95, 75, 3, craf);
		setChildren(4, smit);
		setBounds(29591, 20, 30, 0, smit);
		setBounds(4268, 80, 15, 1, smit);
		setBounds(4269, 80, 45, 2, smit);
		setBounds(358, 95, 75, 3, smit);
		setChildren(4, mine);
		setBounds(29592, 20, 30, 0, mine);
		setBounds(4268, 80, 15, 1, mine);
		setBounds(4269, 80, 45, 2, mine);
		setBounds(358, 95, 75, 3, mine);
		setChildren(4, herb);
		setBounds(29593, 20, 30, 0, herb);
		setBounds(4268, 80, 15, 1, herb);
		setBounds(4269, 80, 45, 2, herb);
		setBounds(358, 95, 75, 3, herb);
		setChildren(4, agil);
		setBounds(29594, 20, 30, 0, agil);
		setBounds(4268, 80, 15, 1, agil);
		setBounds(4269, 80, 45, 2, agil);
		setBounds(358, 95, 75, 3, agil);
		setChildren(4, thie);
		setBounds(29595, 20, 30, 0, thie);
		setBounds(4268, 80, 15, 1, thie);
		setBounds(4269, 80, 45, 2, thie);
		setBounds(358, 95, 75, 3, thie);
		setChildren(4, slay);
		setBounds(29596, 20, 30, 0, slay);
		setBounds(4268, 80, 15, 1, slay);
		setBounds(4269, 80, 45, 2, slay);
		setBounds(358, 95, 75, 3, slay);
		setChildren(4, farm);
		setBounds(11897, 20, 30, 0, farm);
		setBounds(4268, 80, 15, 1, farm);
		setBounds(4269, 80, 45, 2, farm);
		setBounds(358, 95, 75, 3, farm);
		setChildren(4, rune);
		setBounds(29598, 20, 30, 0, rune);
		setBounds(4268, 80, 15, 1, rune);
		setBounds(4269, 80, 45, 2, rune);
		setBounds(358, 95, 75, 3, rune);
		setChildren(4, cons);
		setBounds(29599, 20, 30, 0, cons);
		setBounds(4268, 80, 15, 1, cons);
		setBounds(4269, 80, 45, 2, cons);
		setBounds(358, 95, 75, 3, cons);
		setChildren(4, hunt);
		setBounds(29600, 20, 30, 0, hunt);
		setBounds(4268, 80, 15, 1, hunt);
		setBounds(4269, 80, 45, 2, hunt);
		setBounds(358, 95, 75, 3, hunt);
		setChildren(4, summ);
		setBounds(29601, 20, 30, 0, summ);
		setBounds(4268, 80, 15, 1, summ);
		setBounds(4269, 80, 45, 2, summ);
		setBounds(358, 95, 75, 3, summ);
		setChildren(4, dung);
		setBounds(29602, 20, 30, 0, dung);
		setBounds(4268, 80, 15, 1, dung);
		setBounds(4269, 80, 45, 2, dung);
		setBounds(358, 95, 75, 3, dung);
	}

	public static void addSkillChatSprite(int id, int skill) {
		addSpriteLoader(id, 456 + skill);
	}

	public static void setChildren(int total, Widget i) {
		i.children = new int[total];
		i.childX = new int[total];
		i.childY = new int[total];
	}

	public static void setBounds(int ID, int X, int Y, int frame, Widget r) {
		r.children[frame] = ID;
		r.childX[frame] = X;
		r.childY[frame] = Y;
	}

	public static void addButton(int i, int j, String name, int W, int H, String S, int AT) {
		Widget RSInterface = addInterface(i);
		RSInterface.id = i;
		RSInterface.parent = i;
		RSInterface.type = 5;
		RSInterface.atActionType = AT;
		RSInterface.contentType = 0;
		RSInterface.opacity = 0;
		RSInterface.hoverType = 52;
		RSInterface.disabledSprite = imageLoader(j, name);
		RSInterface.enabledSprite = imageLoader(j, name);
		RSInterface.width = W;
		RSInterface.height = H;
		RSInterface.tooltip = S;
	}

	public static void addSprites(int ID, int i, int i2, String name, int configId, int configFrame) {
		Widget tab = addTabInterface(ID);
		tab.id = ID;
		tab.parent = ID;
		tab.type = 5;
		tab.atActionType = 0;
		tab.contentType = 0;
		tab.width = 512;
		tab.height = 334;
		tab.opacity = 0;
		tab.hoverType = -1;
		tab.valueCompareType = new int[1];
		tab.requiredValues = new int[1];
		tab.valueCompareType[0] = 1;
		tab.requiredValues[0] = configId;
		tab.valueIndexArray = new int[1][3];
		tab.valueIndexArray[0][0] = 5;
		tab.valueIndexArray[0][1] = configFrame;
		tab.valueIndexArray[0][2] = 0;
		tab.disabledSprite = imageLoader(i, name);
		tab.enabledSprite = imageLoader(i2, name);
	}

	public static void closeButton(int id, int enabledSprite, int disabledSprite) {
		Widget tab = addInterface(id);
		tab.atActionType = OPTION_CLOSE;
		tab.type = TYPE_HOVER;
		tab.enabledSprite = Client.spriteCache.lookup(enabledSprite);
		tab.disabledSprite = Client.spriteCache.lookup(disabledSprite);
		tab.width = tab.enabledSprite.myWidth;
		tab.height = tab.disabledSprite.myHeight;
		tab.toggled = false;
		tab.spriteOpacity = 255;
	}

	public static void hoverButton(int id, String tooltip, int enabledSprite, int disabledSprite) {
		hoverButton(id, tooltip, enabledSprite, disabledSprite, 255);
	}

	public static void hoverButton(int id, String tooltip, int enabledSprite, int disabledSprite, int opacity) {
		Widget tab = addInterface(id);
		tab.tooltip = tooltip;
		tab.atActionType = 1;
		tab.type = TYPE_HOVER;
		tab.enabledSprite = Client.spriteCache.lookup(enabledSprite);
		tab.disabledSprite = Client.spriteCache.lookup(disabledSprite);
		tab.width = tab.enabledSprite.myWidth;
		tab.height = tab.disabledSprite.myHeight;
		tab.toggled = false;
		tab.spriteOpacity = opacity;
	}

	public static void hoverButton(int id, String tooltip, int enabledSprite, int disabledSprite, String buttonText,
			RSFont rsFont, int colour, int hoveredColour, boolean centerText) {
		Widget tab = addInterface(id);
		tab.tooltip = tooltip;
		tab.atActionType = 1;
		tab.type = TYPE_HOVER;
		tab.enabledSprite = Client.spriteCache.lookup(enabledSprite);
		tab.disabledSprite = Client.spriteCache.lookup(disabledSprite);
		tab.width = tab.enabledSprite.myWidth;
		tab.height = tab.disabledSprite.myHeight;
		tab.msgX = tab.width / 2;
		tab.msgY = (tab.height / 2) + 4;
		tab.defaultText = buttonText;
		tab.toggled = false;
		tab.rsFont = rsFont;
		tab.textColor = colour;
		tab.defaultHoverColor = hoveredColour;
		tab.centerText = centerText;
		tab.spriteOpacity = 255;
	}

	public static void addPixels(int id, int color, int width, int height, int alpha, boolean filled) {
		Widget rsi = addInterface(id);
		rsi.type = TYPE_RECTANGLE;
		rsi.opacity = (byte) alpha;
		rsi.textColor = color;
		rsi.defaultHoverColor = color;
		rsi.secondaryHoverColor = color;
		rsi.secondaryColor = color;
		rsi.filled = filled;
		rsi.width = width;
		rsi.height = height;
	}

	public static void addPixels(int id, int color, int hoverColor, int width, int height, int alpha, boolean filled) {
		Widget rsi = addInterface(id);
		rsi.type = TYPE_RECTANGLE;
		rsi.opacity = (byte) alpha;
		rsi.textColor = color;
		rsi.textColor = color;
		rsi.defaultHoverColor = color;
		rsi.secondaryHoverColor = color;
		rsi.secondaryColor = color;
		rsi.filled = filled;
		rsi.width = width;
		rsi.height = height;
		rsi.contentType = 0;
		rsi.atActionType = 1;
		rsi.hoverType = id;
	}

	public static void configButton(int id, String tooltip, int enabledSprite, int disabledSprite) {
		Widget tab = addInterface(id);
		tab.tooltip = tooltip;
		tab.atActionType = OPTION_OK;
		tab.type = TYPE_CONFIG;
		tab.enabledSprite = Client.spriteCache.lookup(enabledSprite);
		tab.disabledSprite = Client.spriteCache.lookup(disabledSprite);
		tab.width = tab.enabledSprite.myWidth;
		tab.height = tab.disabledSprite.myHeight;
		tab.active = false;
	}

	public static void adjustableConfig(int id, String tooltip, int sprite, int opacity, int enabledSpriteBehind,
			int disabledSpriteBehind) {
		Widget tab = addInterface(id);
		tab.tooltip = tooltip;
		tab.atActionType = OPTION_OK;
		tab.type = TYPE_ADJUSTABLE_CONFIG;
		tab.enabledSprite = Client.spriteCache.lookup(sprite);
		tab.enabledAltSprite = Client.spriteCache.lookup(enabledSpriteBehind);
		tab.disabledAltSprite = Client.spriteCache.lookup(disabledSpriteBehind);
		tab.width = tab.enabledAltSprite.myWidth;
		tab.height = tab.disabledAltSprite.myHeight;
		tab.spriteOpacity = opacity;
	}

	public static void configHoverButton(int id, String tooltip, int enabledSprite, int disabledSprite,
			int enabledAltSprite, int disabledAltSprite) {
		Widget tab = addInterface(id);
		tab.tooltip = tooltip;
		tab.atActionType = OPTION_OK;
		tab.type = TYPE_CONFIG_HOVER;
		tab.enabledSprite = Client.spriteCache.lookup(enabledSprite);
		tab.disabledSprite = Client.spriteCache.lookup(disabledSprite);
		tab.width = tab.enabledSprite.myWidth;
		tab.height = tab.disabledSprite.myHeight;
		tab.enabledAltSprite = Client.spriteCache.lookup(enabledAltSprite);
		tab.disabledAltSprite = Client.spriteCache.lookup(disabledAltSprite);
		tab.spriteOpacity = 255;
	}

	public static void configHoverButton(int id, String tooltip, int enabledSprite, int disabledSprite,
			int enabledAltSprite, int disabledAltSprite, boolean active, int... buttonsToDisable) {
		Widget tab = addInterface(id);
		tab.tooltip = tooltip;
		tab.atActionType = OPTION_OK;
		tab.type = TYPE_CONFIG_HOVER;
		tab.enabledSprite = Client.spriteCache.lookup(enabledSprite);
		tab.disabledSprite = Client.spriteCache.lookup(disabledSprite);
		tab.width = tab.enabledSprite.myWidth;
		tab.height = tab.disabledSprite.myHeight;
		tab.enabledAltSprite = Client.spriteCache.lookup(enabledAltSprite);
		tab.disabledAltSprite = Client.spriteCache.lookup(disabledAltSprite);
		tab.buttonsToDisable = buttonsToDisable;
		tab.active = active;
		tab.spriteOpacity = 255;
	}

	public static void configHoverButton(int id, String tooltip, int enabledSprite, int disabledSprite,
			int enabledAltSprite, int disabledAltSprite, boolean active, String buttonText, RSFont rsFont, int colour,
			int hoveredColour, boolean centerText, int... buttonsToDisable) {
		Widget tab = addInterface(id);
		tab.tooltip = tooltip;
		tab.atActionType = OPTION_OK;
		tab.type = TYPE_CONFIG_HOVER;
		tab.enabledSprite = Client.spriteCache.lookup(enabledSprite);
		tab.disabledSprite = Client.spriteCache.lookup(disabledSprite);
		tab.width = tab.enabledSprite.myWidth;
		tab.height = tab.disabledSprite.myHeight;
		tab.enabledAltSprite = Client.spriteCache.lookup(enabledAltSprite);
		tab.disabledAltSprite = Client.spriteCache.lookup(disabledAltSprite);
		tab.buttonsToDisable = buttonsToDisable;
		tab.active = active;
		tab.msgX = tab.width / 2;
		tab.msgY = (tab.height / 2) + 4;
		tab.defaultText = buttonText;
		tab.rsFont = rsFont;
		tab.textColor = colour;
		tab.defaultHoverColor = hoveredColour;
		tab.centerText = centerText;
		tab.spriteOpacity = 255;
	}

	public static void handleConfigHover(Widget widget) {
		if (widget.active) {
			return;
		}
		widget.active = true;

		configHoverButtonSwitch(widget);
		disableOtherButtons(widget);
	}

	public static void configHoverButtonSwitch(Widget widget) {
		Sprite[] backup = new Sprite[] { widget.enabledSprite, widget.disabledSprite };

		widget.enabledSprite = widget.enabledAltSprite;
		widget.disabledSprite = widget.disabledAltSprite;

		widget.enabledAltSprite = backup[0];
		widget.disabledAltSprite = backup[1];
	}

	public static void disableOtherButtons(Widget widget) {
		if (widget.buttonsToDisable == null) {
			return;
		}
		for (int btn : widget.buttonsToDisable) {
			Widget btnWidget = interfaceCache[btn];

			if (btnWidget.active) {

				btnWidget.active = false;
				configHoverButtonSwitch(btnWidget);
			}
		}
	}

	public static void slider(int id, double min, double max, int icon, int background, int contentType) {
		Widget widget = addInterface(id);
		widget.slider = new Slider(Client.spriteCache.lookup(icon), Client.spriteCache.lookup(background), min, max);
		widget.type = TYPE_SLIDER;
		widget.contentType = contentType;
	}

	public static void keybindingDropdown(int id, int width, int defaultOption, String[] options, Dropdown d,
			boolean inverted) {
		Widget widget = addInterface(id);
		widget.type = TYPE_KEYBINDS_DROPDOWN;
		widget.dropdown = new DropdownMenu(width, true, defaultOption, options, d);
		widget.atActionType = OPTION_DROPDOWN;
		widget.inverted = inverted;
	}

	public static void dropdownMenu(int id, int width, int defaultOption, String[] options, Dropdown d) {
		dropdownMenu(id, width, defaultOption, options, d,
				new int[] { 0x0d0d0b, 0x464644, 0x473d32, 0x51483c, 0x787169 }, false);
	}

	public static void dropdownMenu(int id, int width, int defaultOption, String[] options, Dropdown d,
			int[] dropdownColours, boolean centerText) {
		Widget menu = addInterface(id);
		menu.type = TYPE_DROPDOWN;
		menu.dropdown = new DropdownMenu(width, false, defaultOption, options, d);
		menu.atActionType = OPTION_DROPDOWN;
		menu.dropdownColours = dropdownColours;
		menu.centerText = centerText;
	}

	public static void rotatingSprite(int id, int spriteId) {
		Widget widget = interfaceCache[id] = new Widget();
		widget.id = id;
		widget.parent = id;
		widget.type = TYPE_ROTATING;
		widget.atActionType = 0;
		widget.contentType = 0;
		widget.disabledSprite = Client.spriteCache.lookup(spriteId);
		widget.enabledSprite = Client.spriteCache.lookup(spriteId);
		widget.width = widget.disabledSprite.myWidth;
		widget.height = widget.enabledSprite.myHeight - 2;
	}

	public static void addTicker(int id) {
		Widget widget = interfaceCache[id] = new Widget();
		widget.id = id;
		widget.parent = id;
		widget.type = TYPE_TICKER;
		widget.atActionType = 0;
		widget.contentType = 0;
	}

	private static void addItemModel(int id, int item, int w, int h, int zoom) {
		Widget widget = interfaceCache[id] = new Widget();
		// rsinterface.modelRotation1 = itemDef.modelRotationY;
		// rsinterface.modelRotation2 = itemDef.modelRotationX;
		widget.contentType = 329;
		widget.type = TYPE_MODEL;
		widget.defaultMediaType = 4;
		widget.defaultMedia = item;
		if (widget.defaultMedia != -1)
			widget.modelZoom = (ItemDefinition.lookup(item).modelZoom * 100) / zoom;
		widget.height = h;
		widget.width = w;
	}

	static void addBox(int id, int w, int h, int colour, int hoverColour, String tooltip) {
		Widget box = addInterface(id);
		box.type = TYPE_BOX;
		box.width = w;
		box.height = h;
		box.atActionType = 1;
		box.tooltip = tooltip;
		box.defaultHoverColor = colour;
		box.secondaryHoverColor = hoverColour;
	}

	public static void addWorldMap(int id) {
		Widget box = addInterface(id);
		box.type = TYPE_MAP;
		box.width = 400;
		box.height = 400;
		box.atActionType = 1;
	}

	private static void worldMap() {
		Widget map = addInterface(54000);
		map.totalChildren(3);

		addWorldMap(54001);
		addSpriteLoader(54002, 568);
		closeButton(54003, 569, 570);

		setBounds(54001, 0, -52, 0, map);
		setBounds(54002, 46, 0, 1, map);
		setBounds(54003, 431, 10, 2, map);
	}

	public void swapInventoryItems(int i, int j) {
		int id = inventoryItemId[i];
		inventoryItemId[i] = inventoryItemId[j];
		inventoryItemId[j] = id;
		id = inventoryAmounts[i];
		inventoryAmounts[i] = inventoryAmounts[j];
		inventoryAmounts[j] = id;
	}

	public void totalChildren(int id, int x, int y) {
		children = new int[id];
		childX = new int[x];
		childY = new int[y];
	}

	public void child(int id, int interID, int x, int y) {
		children[id] = interID;
		childX[id] = x;
		childY[id] = y;
	}

	public void totalChildren(int t) {
		children = new int[t];
		childX = new int[t];
		childY = new int[t];
	}

	private Model getModel(int type, int mobId) {
		Model model = (Model) models.get((type << 16) + mobId);

		if (model != null) {
			return model;
		}

		if (type == 1) {
			model = Model.getModel(mobId);
		}

		if (type == 2) {
			model = NpcDefinition.lookup(mobId).model();
		}

		if (type == 3) {
			model = Client.localPlayer.getHeadModel();
		}

		if (type == 4) {
			model = ItemDefinition.lookup(mobId).getUnshadedModel(50);
		}

		if (type == 5) {
			model = null;
		}

		if (model != null) {
			models.put(model, (type << 16) + mobId);
		}

		return model;
	}

	public Model method209(int j, int k, boolean flag) {
		Model model;
		if (flag)
			model = getModel(anInt255, anInt256);
		else
			model = getModel(defaultMediaType, defaultMedia);
		if (model == null)
			return null;
		if (k == -1 && j == -1 && model.triangleColours == null)
			return model;
		Model model_1 = new Model(true, Frame.noAnimationInProgress(k) & Frame.noAnimationInProgress(j), false, model);
		if (k != -1 || j != -1)
			model_1.skin();
		if (k != -1)
			model_1.applyTransform(k);
		if (j != -1)
			model_1.applyTransform(j);
		model_1.light(64, 850, -30, -50, -30, true);
		return model_1;
	}
	
	public static void createTooltip(int id, int width, int height, GameFont[] font, String tooltip) {
		Widget rsi = addTabInterface(id);
		rsi.type = 8;
		rsi.inventoryHover = true;
		rsi.defaultText = tooltip;
		rsi.textDrawingAreas = font[1];
		rsi.width = width;
		rsi.height = height;
	}

	public static void insertNewChild(Widget widget, int id, int x, int y) {
		int[] newChildren = new int[widget.children.length + 1];
		int[] newChildX = new int[widget.childX.length + 1];
		int[] newChildY = new int[widget.childY.length + 1];

		System.arraycopy(widget.children, 0, newChildren, 0, widget.children.length);
		System.arraycopy(widget.childX, 0, newChildX, 0, widget.childX.length);
		System.arraycopy(widget.childY, 0, newChildY, 0, widget.childY.length);

		widget.children = newChildren;
		widget.childX = newChildX;
		widget.childY = newChildY;
		widget.children[widget.children.length - 1] = id;
		widget.childX[widget.childX.length - 1] = x;
		widget.childY[widget.childY.length - 1] = y;
	}

	public static void insertNewChild(Widget widget, int index, int id, int x, int y) {
		int[] newChildren = new int[widget.children.length + 1];
		int[] newChildX = new int[widget.childX.length + 1];
		int[] newChildY = new int[widget.childY.length + 1];

		System.arraycopy(widget.children, 0, newChildren, 0, index);
		System.arraycopy(widget.childX, 0, newChildX, 0, index);
		System.arraycopy(widget.childY, 0, newChildY, 0, index);

		System.arraycopy(widget.children, index, newChildren, index + 1, widget.children.length - index);
		System.arraycopy(widget.childX, index, newChildX, index + 1, widget.children.length - index);
		System.arraycopy(widget.childY, index, newChildY, index + 1, widget.children.length - index);

		newChildren[index] = id;
		newChildX[index] = x;
		newChildY[index] = y;

		widget.children = newChildren;
		widget.childX = newChildX;
		widget.childY = newChildY;
	}

	public static void swapChildrenIndexes(Widget widget, int fromIndex, int toIndex) {
		int toIndexId = widget.children[toIndex];
		int toIndexX = widget.childX[toIndex];
		int toIndexY = widget.childY[toIndex];

		widget.children[toIndex] = widget.children[fromIndex];
		widget.childX[toIndex] = widget.childX[fromIndex];
		widget.childY[toIndex] = widget.childY[fromIndex];

		widget.children[fromIndex] = toIndexId;
		widget.childX[fromIndex] = toIndexX;
		widget.childY[fromIndex] = toIndexY;

	}

	private static void shiftChildrenIds(Widget widget, int direction, int startIndex, int endIndex) {
		for (int index = startIndex; index < endIndex; index++) {
			widget.children[index] = widget.children[index + direction];
		}
	}

	public static void extendChildren(Widget widget, int extendBy) {
		int[] childIds = new int[widget.children.length + extendBy];
		int[] childX = new int[widget.childX.length + extendBy];
		int[] childY = new int[widget.childY.length + extendBy];

		System.arraycopy(widget.children, 0, childIds, 0, widget.children.length);
		System.arraycopy(widget.childX, 0, childX, 0, widget.childX.length);
		System.arraycopy(widget.childY, 0, childY, 0, widget.childY.length);

		widget.totalChildren(childIds.length);

		System.arraycopy(childIds, 0, widget.children, 0, childIds.length - extendBy);
		System.arraycopy(childX, 0, widget.childX, 0, childX.length - extendBy);
		System.arraycopy(childY, 0, widget.childY, 0, childY.length - extendBy);
	}

	public static void removeChild(Widget widget, int index) {
		int[] childIds = new int[widget.children.length - 1];
		int[] childX = new int[widget.childX.length - 1];
		int[] childY = new int[widget.childY.length - 1];
		int i = 0;
		for (int c = 0; c < widget.children.length; c++) {
			if (c == index) {
				continue;
			}
			childIds[i] = widget.children[c];
			childX[i] = widget.childX[c];
			childY[i] = widget.childY[c];
			i++;
		}
		widget.totalChildren(childIds.length);
		System.arraycopy(childIds, 0, widget.children, 0, childIds.length);
		System.arraycopy(childX, 0, widget.childX, 0, childX.length);
		System.arraycopy(childY, 0, widget.childY, 0, childY.length);
	}
}
