package com.runescape.cache.graphics.widget;

import com.runescape.Client;
import com.runescape.cache.def.ItemDefinition;
import com.runescape.cache.graphics.sprite.Sprite;

public class Bank {

    public static final int MAX_BANK_TABS = 11;
    public static final int[] CONTAINERS = {50300, 50301, 50302, 50303, 50304, 50305, 50306, 50307, 50308, 50309};
    public static BankTabShow bankTabShow = BankTabShow.FIRST_ITEM_IN_TAB;
    public static int currentBankTab;

    public static void draw(int _x, int currentY) {
        int id_start = 50070;
        int x = _x + 20;
        int y = currentY;
        int final_loop = 1;
        boolean drawPlus = false;

        for (int tab = 0; tab < MAX_BANK_TABS - 1; tab++) {

            Widget containers = Widget.interfaceCache[50300 + tab];

            // First we search for an item in the tab...
            int first_item = -1;
            int item_amount = 0;
            for (int index = 0; index < containers.inventoryItemId.length; index++) {
                if (containers.inventoryItemId[index] > 0) {
                    first_item = containers.inventoryItemId[index] - 1;
                    item_amount = containers.inventoryAmounts[index];
                    break;
                }
            }

            Widget button = Widget.interfaceCache[id_start + (tab * 4)];
            // Draw the tab if it isn't empty or is id 0
            if (first_item > 0 || tab == 0) {

                // If it's currently being viewed, change its sprite
                if (tab == currentBankTab || tab == 0 && searchingBank()) {
                    button.disabledSprite = Client.spriteCache.lookup(205);
                } else {
                    button.disabledSprite = Client.spriteCache.lookup(206);
                }

                // We have a tab! Draw it!
                Client.instance.settings[1000 + tab] = 0;
                button.hidden = false;

                // Draw its options
                button.actions = new String[]{"Select", tab == 0 ? null : "Collapse", null, null, null};

                // Draw item or tab number
                if (tab != 0) {

                    Sprite sprite = null;

                    switch (bankTabShow) {
                        case DIGIT:
                            sprite = Client.spriteCache.lookup(219 + tab);
                            break;
                        case FIRST_ITEM_IN_TAB:
                            sprite = ItemDefinition.getSprite(first_item, item_amount, 0);
                            break;
                        case ROMAN_NUMERAL:
                            sprite = Client.spriteCache.lookup(210 + tab);
                            break;
                    }

                    if (sprite != null) {
                        sprite.drawSprite(x + 3, y + 41);
                    }
                }

                x += 40;
            } else {

                // Empty tab
                // We don't have a tab. Remove the button
                Client.instance.settings[1000 + tab] = 1;
                button.hidden = true;

                // Add the draw plus sprite
                if (!drawPlus) {
                    final_loop = tab;
                    drawPlus = true;
                }
            }

        }

        // Draws a tab and a plus icon after our final bank tab
        if (drawPlus) {

            // Show tab
            Client.instance.settings[1000 + final_loop] = 0;

            // Show option
            Widget.interfaceCache[id_start + (final_loop * 4)].actions = new String[]{"Create Tab", null, null, null,
                    null};
            Widget.interfaceCache[id_start + (final_loop * 4)].hidden = false;

            // Draw plus icon
            Client.spriteCache.draw(210, x, y + 40);
        }

        // Now let's draw the actual bank interface.

        // First set the proper Y value depending on the scroll position
        final Widget scrollBar = Widget.interfaceCache[5385];
        if (scrollBar.scrollPosition > 0) {
            y -= scrollBar.scrollPosition;
        }

        // Now reset all children
        for (int i = 0; i < scrollBar.children.length; i++) {

            // Reset their positioning
            scrollBar.childX[i] = 40;
            scrollBar.childY[i] = 0;

            // Also make the children invisible
            Widget.interfaceCache[scrollBar.children[i]].hidden = true;
        }

        // Now reset scroll max
        scrollBar.scrollMax = 500;

        // Now draw the actual bank
        if (currentBankTab != 0) {

            // Get the container we're viewing..
            Widget container = Widget.interfaceCache[50300 + currentBankTab];

            // Make the container we're currently viewing visible
            container.hidden = false;

            // Set scroll bar size based on amount of rows..
            scrollBar.scrollMax = 35 * (getRows(container)) + 300;

        } else {
            int totalRows = 0;
            int containerOffsetY = 0;

            for (int tab = 0; tab <= MAX_BANK_TABS - 1; tab++) {

                // Show all containers
                Widget container = Widget.interfaceCache[50300 + tab];
                container.hidden = false;

                // Increment their Y offset based on amount of rows..
                scrollBar.childY[tab] = containerOffsetY;
                int rows = getRows(container);
                if (rows > 1) {
                    containerOffsetY += (rows + 1) * (32 + 10);
                } else {
                    containerOffsetY += (rows == 1 ? 70 : 40);
                }

                totalRows += rows;
            }

            // Increase scroll bar size based on total amount of rows..
            scrollBar.scrollMax += totalRows * 52;
        }
    }

    private static boolean searchingBank() {
        return currentBankTab == MAX_BANK_TABS - 1;
    }

    private static int getRows(Widget container) {
        // Calculate amount of rows that are occupied in this container
        int rows = 0;

        // 10 items per row
        label0:
        for (int j = 0; j < container.inventoryItemId.length; j += 10) {

            // Is this row empty or not?
            // Set to true default
            boolean emptyRow = true;

            // Check the next rows for items...
            label1:
            for (int k = 0; k < container.inventoryItemId.length; k++) {

                if (j + k >= container.inventoryItemId.length) {
                    break label0;
                }

                // Check for items...
                if (container.inventoryItemId[j + k] > 0) {
                    emptyRow = false;
                    break label1;
                }
            }

            // If the row wasn't empty, increment the amount of rows we have.
            if (!emptyRow) {
                rows++;
            }
        }
        return rows;
    }

    public static boolean isBankContainer(int interfaceId) {
        for (int i : CONTAINERS) {
            if (i == interfaceId) {
                return true;
            }
        }
        return false;
    }

    public enum BankTabShow {
        FIRST_ITEM_IN_TAB,
        DIGIT,
        ROMAN_NUMERAL;
    }
}
