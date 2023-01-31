package com.runescape.draw.teleports;

import com.runescape.Client;
import com.runescape.Client.ScreenMode;
import com.runescape.graphics.RSFont;
import com.runescape.draw.Rasterizer2D;

import java.awt.Dimension;
import java.awt.event.KeyEvent;

public class TeleportChatBox {

	public static final int PVP_TELEPORT_TYPE = 0;
	public static final int MINIGAMES_TELEPORT_TYPE = 1;
	public static final int BOSSES_TELEPORT_TYPE = 2;
	public static final int SKILLS_TELEPORT_TYPE = 3;


	private static final int SUPER_WIDTH = 98;
	private static final int SUPER_HEIGHT = 13;

	private static HierarchyOption selectedHierarchy;

	public static void draw(int offsetX, int offsetY) {
		int mouseX = Client.instance.mouseX;
		int mouseY = Client.instance.mouseY - (Client.frameHeight - 165) + offsetY;
		int chatboxWidth = Client.frameMode == ScreenMode.FIXED ? 501 : 498;
		int chatboxHeight = 166;
		boolean click = Client.instance.clickMode3 == 1;
		Rasterizer2D.drawHorizontalLine(10, 24 + offsetY, chatboxWidth, 0x847963,
				255);
		Rasterizer2D.drawHorizontalLine(10, 23 + offsetY, chatboxWidth, 0x847963,
				255);

		// Draw and handle close button..
		int spriteDrawX = (offsetX + 7);
		int spriteDrawY = (offsetY + 7);
		boolean closeHover = mouseX >= spriteDrawX && mouseX <= (spriteDrawX + 15) && mouseY >= spriteDrawY && mouseY <= (spriteDrawY + 15);
		Client.spriteCache.draw(closeHover ? 138 : 137, spriteDrawX, spriteDrawY);
		if (click && closeHover) {
			close();
			return;
		}

		final RSFont font = Client.instance.newRegularFont;
		int optionX = 13 + offsetX;
		int optionY = 36 + offsetY;
		int index = 0;
		int hoveredIndex = -1;
		int hoveredOptionX = -1;
		int hoveredOptionY = -1;
		HierarchyOption[] options = HIERARCHY_OPTIONS;
		int shiftedX = optionX / 2;
		for (HierarchyOption hierarchyOption : options) {
			if (shiftedX == optionX / 2)
				shiftedX += hierarchyOption.getDimension().width;
			if (optionY >= chatboxHeight - 23 + offsetY) {
				optionY = 36;
				optionX += hierarchyOption.getDimension().width;
				shiftedX += hierarchyOption.getDimension().width;
			}
			String shortcutKey = null;
			if (hierarchyOption.getShortcutKey() != -1) {
				shortcutKey = KeyEvent.getKeyText(hierarchyOption.getShortcutKey()) + ".";
				font.drawBasicString(shortcutKey, optionX, optionY, 0x696969, -1);
			}
			font.drawBasicString(hierarchyOption.getName(), 15 + optionX, optionY, 0x000000, -1);
			if (hierarchyOption.getIndex() == null) {
				int textWidth = font.getTextWidth(hierarchyOption.getName());
				Client.spriteCache.draw(615, 20 + textWidth + optionX, optionY - 9, true);
				/*int color = 0x847963;

				Rasterizer2D.drawHorizontalLine(20 + textWidth + optionX, optionY - 4,
						10, color);

				Rasterizer2D.drawHorizontalLine(25 + textWidth + optionX, optionY - 5,
						4, color);
				Rasterizer2D.drawHorizontalLine(25 + textWidth + optionX, optionY - 6,
						3, color);
				Rasterizer2D.drawHorizontalLine(25 + textWidth + optionX, optionY - 7,
						2, color);
				Rasterizer2D.drawHorizontalLine(25 + textWidth + optionX, optionY - 8,
						1, color);

				Rasterizer2D.drawHorizontalLine(25 + textWidth + optionX, optionY - 3,
						4, color);
				Rasterizer2D.drawHorizontalLine(25 + textWidth + optionX, optionY - 2,
						3, color);
				Rasterizer2D.drawHorizontalLine(25 + textWidth + optionX, optionY - 1,
						2, color);
				Rasterizer2D.drawHorizontalLine(25 + textWidth + optionX, optionY,
						1, color);*/
			}

			if (mouseX >= optionX
					&& mouseX <= optionX + hierarchyOption.getDimension().width
					&& mouseY >= optionY - 10
					&& mouseY <= optionY - 10 + hierarchyOption.getDimension().height) {
				hoveredIndex = index;
				hoveredOptionX = optionX;
				hoveredOptionY = optionY - hierarchyOption.getDimension().height;
			}
			optionY += font.baseCharacterHeight;
			index++;
		}

		if (hoveredIndex >= 0 && hoveredIndex < options.length) {
			HierarchyOption hierarchyOption = options[hoveredIndex];
			Rasterizer2D.fillRectangle(5 + (hoveredOptionX > 10 ? hoveredOptionX - 5 : 0), hoveredOptionY + 1,
					hierarchyOption.getDimension().width - 6, 13, 0, 50);

			if (click) {
				if (hierarchyOption.getIndex() == null)
					selectedHierarchy = hierarchyOption;
				else
					sendTeleportPacket(hierarchyOption);
			}
		}

		Rasterizer2D.drawVerticalLine(shiftedX + 3, 25 + offsetY, chatboxHeight - 55, 0x847963);
		Rasterizer2D.drawVerticalLine(shiftedX + 4, 25 + offsetY, chatboxHeight - 55, 0x847963);

		if (selectedHierarchy != null) {
			optionX = 2 + optionX + 100;
			optionY = 36 + offsetY;
			index = 0;
			hoveredIndex = -1;
			hoveredOptionX = -1;
			hoveredOptionY = -1;
			options = selectedHierarchy.getOptions();
			if (options == null)
				return;
			for (HierarchyOption hierarchyOption : options) {
				if (optionY >= chatboxHeight - 23 + offsetY) {
					optionY = 36 + offsetY;
					optionX += hierarchyOption.getDimension().width;
				}
				String shortcutKey = null;
				if (hierarchyOption.getShortcutKey() != -1) {
					shortcutKey = KeyEvent.getKeyText(hierarchyOption.getShortcutKey()) + ".";
					font.drawBasicString(shortcutKey, optionX, optionY, 0x696969, -1);
				}
				font.drawBasicString(hierarchyOption.getName(), 15 + optionX, optionY, 0x000000, -1);

				if (hierarchyOption.getIndex() == null) {
					int textWidth = font.getTextWidth(hierarchyOption.getName());
					Client.spriteCache.draw(615, 20 + textWidth + optionX, optionY - 10, true);
					/*Rasterizer2D.drawHorizontalLine(20 + textWidth + optionX, optionY - 4,
							10, 0x847963);

					Rasterizer2D.drawHorizontalLine(25 + textWidth + optionX, optionY - 5,
							4, 0x847963);
					Rasterizer2D.drawHorizontalLine(25 + textWidth + optionX, optionY - 6,
							3, 0x847963);
					Rasterizer2D.drawHorizontalLine(25 + textWidth + optionX, optionY - 7,
							2, 0x847963);
					Rasterizer2D.drawHorizontalLine(25 + textWidth + optionX, optionY - 8,
							1, 0x847963);

					Rasterizer2D.drawHorizontalLine(25 + textWidth + optionX, optionY - 3,
							4, 0x847963);
					Rasterizer2D.drawHorizontalLine(25 + textWidth + optionX, optionY - 2,
							3, 0x847963);
					Rasterizer2D.drawHorizontalLine(25 + textWidth + optionX, optionY - 1,
							2, 0x847963);
					Rasterizer2D.drawHorizontalLine(25 + textWidth + optionX, optionY,
							1, 0x847963);*/
				}

				if (mouseX >= optionX
						&& mouseX <= optionX + hierarchyOption.getDimension().width
						&& mouseY >= optionY - 10
						&& mouseY <= optionY - 10 + hierarchyOption.getDimension().height) {
					hoveredIndex = index;
					hoveredOptionX = optionX;
					hoveredOptionY = optionY - hierarchyOption.getDimension().height;
				}

				optionY += font.baseCharacterHeight;
				index++;
			}

			if (hoveredIndex >= 0 && hoveredIndex < options.length) {
				HierarchyOption hierarchyOption = options[hoveredIndex];
				int pixelsLength = hierarchyOption.getDimension().width;
				if ((hoveredOptionX + pixelsLength) > 509) {
					pixelsLength = (509 - hoveredOptionX);
				}
				Rasterizer2D.fillRectangle(hoveredOptionX,
						hoveredOptionY + 1,
						pixelsLength, 13, 0, 50);

				if (click) {
					if (hierarchyOption.getIndex() == null)
						selectedHierarchy = hierarchyOption;
					else
						sendTeleportPacket(hierarchyOption);
				}

				/*if (INSTANCE.hoverTimer++ >= 30
						&& hierarchyOption.getDescription() != null) {
					final RSFont descriptionFont = Client.instance.newSmallFont;
					int width = 380 - hierarchyOption.getDimension().width;
					final String[] descriptionSplit = TextUtils.split(descriptionFont, hierarchyOption.getDescription(), width, "<n>");
					final int height = (descriptionFont.baseCharacterHeight + 2) * descriptionSplit.length + 2;

					int tooltipX = hoveredIndex >= 9 ?  
							width - hierarchyOption.getDimension().width
							: 5 + (hoveredOptionX > 10 ? hoveredOptionX - 5 : 0) + hierarchyOption.getDimension().width;
							int tooltipY = offsetY + 24;

							Rasterizer2D.drawRoundedRectangle(tooltipX, tooltipY, width + 6, height, 
									0x000000, 180, true, false);
							Rasterizer2D.drawRoundedRectangle(tooltipX -1, tooltipY - 1, width + 8, height + 2, 
									0xFFFFFF, 180, false, false);

							tooltipY += 7;
							for (String description : descriptionSplit) {
								if (description == null)
									continue;
								descriptionFont.drawRAString(description, tooltipX + 3, tooltipY + 5, 0xFFFFFF, 0);
								tooltipY += descriptionFont.baseCharacterHeight + 2;
							}
				}
				 */
			} else {
				// hoverTimer = 0;
			}
		}

		boolean mainTitleHover = mouseX >= 28 && mouseX <= 136 && mouseY >= offsetY + 4 && mouseY <= offsetY + 21;
		if (mainTitleHover) {

			// Clicking main tab title should close current selection
			if (click) {
				selectedHierarchy = null;
			}

			Rasterizer2D.fillRectangle(29, 10 + offsetY, 108, 11, 0, 50);
		}
		font.drawBasicString("Teleportation menu"
				+ (selectedHierarchy != null ? " -> " + selectedHierarchy.getName() : ""),
				28, 20 + offsetY, 0x000080, -1);
	}

	public static void pressKey(int key) {
		if (key == KeyEvent.VK_DELETE || key == KeyEvent.VK_BACK_SPACE) {
			selectedHierarchy = null;
			return;
		}

		boolean found = false;
		if (selectedHierarchy != null
				&& selectedHierarchy.getOptions() != null) {

			HierarchyOption[] options = selectedHierarchy.getOptions();
			for (HierarchyOption hierarchyOption : options) {

				if (hierarchyOption.getShortcutKey() == key) {

					if (hierarchyOption.getIndex() != null) {
						sendTeleportPacket(hierarchyOption);
					} else {
						selectedHierarchy = hierarchyOption;
					}
					return;
				}
			}
		}

		if (!found) {
			HierarchyOption[] options = HIERARCHY_OPTIONS;
			for (HierarchyOption hierarchyOption : options) {
				if (hierarchyOption.getShortcutKey() == key) {
					if (hierarchyOption.getIndex() != null) {
						sendTeleportPacket(hierarchyOption);
					} else {
						selectedHierarchy = hierarchyOption;
					}
					return;
				}
			}
		}
	}

	public static void sendTeleportPacket(HierarchyOption teleport) {
		if (isOpen()) {

			// Send packet
			final int[] index = teleport.getIndex();
			if (index.length == 2) {
				Client.instance.packetSender.sendTeleportSelection(index[0], index[1]);
			}

			// Reset selected teleport
			selectedHierarchy = null;

			// Close interface
			close();
		}
	}

	public static void open(int index) {
		if (index < 0 || index >= HIERARCHY_OPTIONS.length) {
			selectedHierarchy = null;
		} else {
			selectedHierarchy = HIERARCHY_OPTIONS[index];
		}
		open();
	}

	public static boolean isOpen() {
		return Client.instance.inputDialogState == 3;
	}
	
	public static void open() {
		Client.instance.inputDialogState = 3;
		Client.updateChatbox = true;
	}

	public static void close() {
		Client.instance.inputDialogState = 0;
		Client.updateChatbox = true;
	}

	private static final HierarchyOption[] HIERARCHY_OPTIONS = {
			new WildernessTeleport(),
			new BossTeleport(),
			new MinigameTeleport(),
			new SkillingTeleport(),
	};

	private static final class BossTeleport extends ParentHierarchyOption {

		@Override
		public Dimension getDimension() {
			return new Dimension(SUPER_WIDTH, SUPER_HEIGHT);
		}

		@Override
		public String getName() {
			return "Bosses";
		}

		@Override
		public int getShortcutKey() {
			return KeyEvent.VK_B;
		}

		@Override
		public HierarchyOption[] getOptions() {
			return Option.values();
		}

		@Override
		public String getDescription() {
			return null;
		}

		private enum Option implements HierarchyOption {
			GODWARS_DUNGEON(KeyEvent.VK_G, "Godwars dungeon"),
			KING_BLACK_DRAGON(KeyEvent.VK_K, "King black dragon"),
			CHAOS_ELEMENTAL(KeyEvent.VK_C, "Chaos elemental"),
			ELDER_CHAOS_DRUID(KeyEvent.VK_E, "Elder chaos druid"),
			CRAZY_ARCHAEOLOGIST(KeyEvent.VK_R, "Crazy archaeologist"),
			CHAOS_FANATIC(KeyEvent.VK_H, "Chaos fanatic"),
			VENENATIS(KeyEvent.VK_V, "Venenatis"),
			VETION(KeyEvent.VK_T, "Vet'ion"),
			CALLISTO(KeyEvent.VK_A, "Callisto"),
			ZULRAH(KeyEvent.VK_Z, "Zulrah"),
			;

			Option(int shortcutKey, String name) {
				this(shortcutKey, name, null);
			}

			Option(int shortcutKey, String name, HierarchyOption[] options) {
				this.shortcutKey = shortcutKey;
				this.name = name;
				this.options = options;
			}

			private final int shortcutKey;

			private final String name;

			private String description;

			private final HierarchyOption[] options;

			@Override
			public Dimension getDimension() {
				return new Dimension(135, SUPER_HEIGHT);
			}

			@Override
			public String getName() {
				return name;
			}

			@Override
			public int getShortcutKey() {
				return shortcutKey;
			}

			@Override
			public String getDescription() {
				return description;
			}

			@Override
			public int[] getIndex() {
				return options != null ? null : new int[]{BOSSES_TELEPORT_TYPE, ordinal()};
			}

			@Override
			public HierarchyOption[] getOptions() {
				return options;
			}
		}
	}

	private static final class WildernessTeleport extends ParentHierarchyOption {

		@Override
		public Dimension getDimension() {
			return new Dimension(SUPER_WIDTH, SUPER_HEIGHT);
		}

		@Override
		public String getName() {
			return "Wilderness";
		}

		@Override
		public int getShortcutKey() {
			return KeyEvent.VK_W;
		}

		@Override
		public HierarchyOption[] getOptions() {
			return Option.values();
		}

		@Override
		public String getDescription() {
			return null;
		}

		private enum Option implements HierarchyOption {
			EDGE(KeyEvent.VK_E, "Edgeville ditch", null),
			WESTS(KeyEvent.VK_W, "West dragons", null),
			EASTS(KeyEvent.VK_A, "East dragons", null),
			GRAVEYARD(KeyEvent.VK_G, "Graveyard", null),
			BANDIT_CAMP(KeyEvent.VK_B, "Bandit camp", null),
			DEOMNIC_RUINS(KeyEvent.VK_D, "Demonic ruins", null),
			MAGE_BANK(KeyEvent.VK_M, "Mage bank", null),
			;

			Option(int shortcutKey, String name,
				   HierarchyOption[] options) {
				this.shortcutKey = shortcutKey;
				this.name = name;
				this.options = options;
			}

			private final int shortcutKey;

			private final String name;

			private final HierarchyOption[] options;

			@Override
			public Dimension getDimension() {
				return new Dimension(SUPER_WIDTH + 100, SUPER_HEIGHT);
			}

			@Override
			public String getName() {
				return name;
			}

			@Override
			public int getShortcutKey() {
				return shortcutKey;
			}

			@Override
			public String getDescription() {
				return null;
			}

			@Override
			public int[] getIndex() {
				return options != null ? null : new int[]{PVP_TELEPORT_TYPE, ordinal()};
			}

			@Override
			public HierarchyOption[] getOptions() {
				return options;
			}
		}
	}

	private static final class MinigameTeleport extends ParentHierarchyOption {

		@Override
		public Dimension getDimension() {
			return new Dimension(SUPER_WIDTH, SUPER_HEIGHT);
		}

		@Override
		public String getName() {
			return "Minigames";
		}

		@Override
		public int getShortcutKey() {
			return KeyEvent.VK_M;
		}

		@Override
		public HierarchyOption[] getOptions() {
			return Option.values();
		}

		@Override
		public String getDescription() {
			return null;
		}

		private enum Option implements HierarchyOption {
			DUEL_ARENA(KeyEvent.VK_D, "Duel arena", ""),
			BARROWS(KeyEvent.VK_B, "Barrows", ""),
			FIGHT_CAVES(KeyEvent.VK_F, "Fight caves", ""),
			CASTLE_WARS(KeyEvent.VK_C, "Castle wars", ""),
			PEST_CONTROL(KeyEvent.VK_P, "Pest control", ""),
;

			Option(int shortcutKey, String name,
				   String description) {
				this.shortcutKey = shortcutKey;
				this.name = name;
				this.description = description;
			}

			private final int shortcutKey;

			private final String name;

			private final String description;

			@Override
			public Dimension getDimension() {
				return new Dimension(160, SUPER_HEIGHT);
			}

			@Override
			public String getName() {
				return name;
			}

			@Override
			public int getShortcutKey() {
				return shortcutKey;
			}

			@Override
			public String getDescription() {
				return description;
			}

			@Override
			public int[] getIndex() {
				return new int[]{MINIGAMES_TELEPORT_TYPE, ordinal()};
			}

			@Override
			public HierarchyOption[] getOptions() {
				return null;
			}
		}
	}

	private static final class SkillingTeleport extends ParentHierarchyOption {

		@Override
		public Dimension getDimension() {
			return new Dimension(SUPER_WIDTH, SUPER_HEIGHT);
		}

		@Override
		public String getName() {
			return "Skilling";
		}

		@Override
		public int getShortcutKey() {
			return KeyEvent.VK_S;
		}

		@Override
		public HierarchyOption[] getOptions() {
			return new HierarchyOption[0];
		}

		@Override
		public String getDescription() {
			return null;
		}

	}

}
