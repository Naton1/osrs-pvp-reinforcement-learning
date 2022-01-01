package com.runescape.graphics.widget;

import java.util.ArrayList;
import java.util.List;

import com.runescape.Client;
import com.runescape.Client.ScreenMode;
import com.runescape.cache.def.ItemDefinition;
import com.runescape.graphics.GameFont;
import com.runescape.graphics.sprite.Sprite;
import com.runescape.draw.Rasterizer2D;

public class OSRSCreationMenu {

    private static final String[] QUANTITIES;
    private static final int[] QUANTITY_TEXT_X;
    private static final int[] BOX_X;
    public static List<Integer> items;
    public static String quantity;
    public static boolean selectingAmount = false;

    static {
        items = new ArrayList<>();
        QUANTITIES = new String[]{"1", "5", "10", "X", "All"};
        quantity = "All";
        BOX_X = new int[]{205, 145, 95, 38};
        QUANTITY_TEXT_X = new int[]{327, 366, 406, 447, 486};
    }

    public static void draw(int x, int y) {
        boolean fixed = Client.frameMode == ScreenMode.FIXED;
        int mouseX = Client.instance.mouseX;
        int mouseY = Client.instance.mouseY - y - (fixed ? 338 : 0);
        boolean click = Client.instance.clickMode3 == 1;

        // Titles
        Client.instance.boldText.drawCenteredText(Widget.interfaceCache[31104].defaultText, x + 145, y + 30, 0x403020, false);
        Client.instance.smallText.drawCenteredText("Choose a quantity, then click an item to begin.", x + 143, y + 45, 0x605048, false);

        // Amount buttons
        for (int i = 0, amountButtonX = 310; i < QUANTITIES.length; i++, amountButtonX += 40) {
            boolean hover = (mouseX >= amountButtonX && mouseX <= amountButtonX + 35 && mouseY >= 15 && mouseY <= 45);
            if (hover && click) {
                // Select X
                if (i == 3) {
                    selectingAmount = true;
                    Client.instance.messagePromptRaised = false;
                    Client.instance.inputDialogState = 1;
                    Client.instance.amountOrNameInput = "";
                    Client.updateChatbox = true;
                } else {
                    quantity = QUANTITIES[i];
                }
            }
            boolean selected = (quantity.equals(QUANTITIES[i]));
            String text = QUANTITIES[i];
            if (i == 3) {
                text = "X: " + quantity;
                selected = true;
                for (String s : QUANTITIES) {
                    if (quantity.equals(s)) {
                        text = "X";
                        selected = false;
                        break;
                    }
                }
            }
            int spriteId = (hover || selected) ? 634 : 633;
            int textColour = selected ? 0xFFFFFF : 0x403020;
            Client.spriteCache.draw(spriteId, amountButtonX, y + 15);
            if (i == 3 && selected) {
                Client.instance.smallText.drawCenteredText(text, x + QUANTITY_TEXT_X[i], y + 35, textColour, false);
            } else {
                Client.instance.gameFont.drawCenteredText(text, x + QUANTITY_TEXT_X[i], y + 35, textColour, false);   
            }
        }

        // Draw the items
        int itemAmount = items.size();
        if (itemAmount > 4) {
            itemAmount = 4;
        } else if (itemAmount <= 0) {
            Client.instance.inputDialogState = 0;
            return;
        }
        int itemX = BOX_X[itemAmount - 1];
        int boxWidth = 100;
        int boxHeight = 75;
        for (int i = 0; i < itemAmount; i++) {
            boolean hover = (mouseX >= (x + itemX) && mouseX <= (x + itemX + 100) && mouseY >= 52 && mouseY <= 127);
            if (hover && click) {
                selectItem(i);
                return;
            }

            int spriteId = hover ? 632 : 631;
            Client.spriteCache.draw(spriteId, x + itemX, y + 52);

            int itemId = items.get(i);
            ItemDefinition def = ItemDefinition.lookup(itemId);
            String itemName = "";
            int zoom = 0;
            if (def != null) {
                zoom = (def.modelZoom / 3) + 40;
                itemName = "(" + (i + 1) + ") " + def.name;
                if (hover) {
                    GameFont font = Client.instance.gameFont;
                    int textWidth = font.getTextWidth(itemName);
                    int hoverX = x + itemX + (boxWidth / 2);
                    int hoverY = y + 132;          
                    Rasterizer2D.drawBox(hoverX - (textWidth / 2) - 2, hoverY - 1, textWidth + 5, 19, 0x000000);
                    Rasterizer2D.fillRectangle(17, hoverY, hoverX - (textWidth / 2) - 1, 0xFFFFA0, textWidth + 3);
                    font.drawCenteredText(itemName, hoverX, hoverY + 14, 0x605048, false);
                }
            }

            Sprite itemSprite = ItemDefinition.getSprite(itemId, 1, zoom, 0);
            if (itemSprite != null) {
                itemSprite.drawSprite(x + itemX + ((itemSprite.myWidth - boxWidth) / 2) + 10, y + ((itemSprite.myHeight - boxHeight) / 2) + 38);
            }
            itemX += 115;
        }
    }

    public static void selectItem(int index) {
        if (index >= OSRSCreationMenu.items.size()) {
            return;
        }
        int itemId = items.get(index);
        int amount = 1;
        try {
            if (quantity.equals("All")) {
                amount = 28;
            } else {
                amount = Integer.parseInt(quantity);
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
            amount = 1;
        }
        Client.instance.packetSender.sendCreationMenuAction(itemId, amount);
        Client.instance.inputDialogState = 0;
        OSRSCreationMenu.items.clear();
    }

    public static void build() {
        Widget.addText(31104, "How many would you like to cook?", Widget.fonts, 2, 0x403020, true, false);
    }
}
