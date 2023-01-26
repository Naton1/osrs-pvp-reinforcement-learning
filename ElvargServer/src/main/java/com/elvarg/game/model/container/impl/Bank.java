package com.elvarg.game.model.container.impl;

import java.util.ArrayList;
import java.util.Arrays;

import com.elvarg.game.GameConstants;
import com.elvarg.game.content.combat.WeaponInterfaces;
import com.elvarg.game.definition.ItemDefinition;
import com.elvarg.game.entity.impl.object.GameObject;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.Flag;
import com.elvarg.game.model.Item;
import com.elvarg.game.model.PlayerStatus;
import com.elvarg.game.model.container.ItemContainer;
import com.elvarg.game.model.container.StackType;
import com.elvarg.game.model.dialogues.DialogueOption;
import com.elvarg.game.model.dialogues.builders.DialogueChainBuilder;
import com.elvarg.game.model.dialogues.entries.impl.OptionDialogue;
import com.elvarg.game.model.equipment.BonusManager;

/**
 * Pretty decent bank system without flaws.
 *
 * @author Gabriel Hannason
 */
public class Bank extends ItemContainer {

    /**
     * The bank interface id.
     */
    public static final int TOTAL_BANK_TABS = 11;
    public static final int CONTAINER_START = 50300;
    /**
     * The bank reserved for bank searches.
     */
    public static final int BANK_SEARCH_TAB_INDEX = TOTAL_BANK_TABS - 1;
    /**
     * The scroll bar in the bank - interface id
     */
    public static final int BANK_SCROLL_BAR_INTERFACE_ID = 5385;
    /**
     * The bank tabs interface id, used when switching an items' tab.
     */
    public static final int BANK_TAB_INTERFACE_ID = 5383;
    /**
     * The bank inventory interface id.
     */
    public static final int INVENTORY_INTERFACE_ID = 5064;

    public Bank(Player player) {
        super(player);
    }

    /**
     * Withdraws an item from the bank.
     *
     * @param player
     * @param item
     * @param slot
     * @param amount
     */
    public static void withdraw(Player player, int item, int slot, int amount, int fromBankTab) {

        if (player.getStatus() == PlayerStatus.BANKING && player.getInterfaceId() == 5292) {

            // The items real tab
            final int itemTab = Bank.getTabForItem(player, item);

            // Check if we're withdrawing the item from the proper tab, but only if we
            // aren't bank searching
            if (itemTab != fromBankTab) {
                if (!player.isSearchingBank()) {
                    return;
                }
            }

            // Make sure we're only withdrawing what we have
            int max_amount = player.getBank(itemTab).getAmount(item);
            if (amount == -1 || amount > max_amount) {
                amount = max_amount;
            }

            if (player.isSearchingBank()) {

                if (!player.getBank(itemTab).contains(item) || !player.getBank(BANK_SEARCH_TAB_INDEX).contains(item)
                        || amount <= 0) {
                    return;
                }

                if (fromBankTab != BANK_SEARCH_TAB_INDEX) {
                    return;
                }

                slot = player.getBank(itemTab).getSlotForItemId(item);

                player.getBank(itemTab).switchItem(player.getInventory(), new Item(item, amount),
                        player.getBank(itemTab).getSlotForItemId(item), false, false);

                if (slot == 0) {
                    Bank.reconfigureTabs(player);
                }

                player.getBank(BANK_SEARCH_TAB_INDEX).refreshItems();

            } else {

                // Withdrawing an item which belongs in another tab from the main tab
                if (player.getCurrentBankTab() == 0 && fromBankTab != 0) {
                    slot = player.getBank(itemTab).getSlotForItemId(item);
                }

                // Make sure the item is in the slot we've found
                if (player.getBank(itemTab).getItems()[slot].getId() != item) {
                    return;
                }

                // Delete placeholder
                if (amount <= 0) {
                    player.getBank(itemTab).getItems()[slot].setId(-1);
                    player.getBank(player.getCurrentBankTab()).sortItems().refreshItems();
                    return;
                }

                // Perform the switch
                player.getBank(itemTab).switchItem(player.getInventory(), new Item(item, amount), slot, false, false);

                // Update all tabs if we removed an item from the first item slot
                if (slot == 0) {
                    Bank.reconfigureTabs(player);
                }

                // Refresh items in our current tab
                player.getBank(player.getCurrentBankTab()).refreshItems();

            }

            // Refresh inventory
            player.getInventory().refreshItems();
        }
    }

    private static final int[] DEPOSIT_BOX_OBJECT_IDS = {9398, 6948};

    public static boolean useItemOnDepositBox(Player player, Item item, int slot, GameObject object) {
        if(Arrays.stream(DEPOSIT_BOX_OBJECT_IDS).noneMatch(id -> id == object.getId())) {
            return false;
        }

        if(player.getInventory().getAmount(item.getId()) == 1) {
            Bank.deposit(player, item.getId(), slot, 1, true);
            return true;
        }

        DialogueChainBuilder builder = new DialogueChainBuilder();

        if(player.getInventory().getAmount(item.getId()) <= 5) {
            builder.add(new OptionDialogue(0, (option) -> {
                if(option == DialogueOption.FIRST_OPTION) {
                    Bank.deposit(player, item.getId(), slot, 1, true);
                } else {
                    Bank.deposit(player, item.getId(), slot, 5, true);
                }
                player.getPacketSender().sendInterfaceRemoval();
            }, "One", "Five"));
        } else if(player.getInventory().getAmount(item.getId()) <= 10) {
            builder.add(new OptionDialogue(0, (option) -> {
                if(option == DialogueOption.FIRST_OPTION) {
                    Bank.deposit(player, item.getId(), slot, 1, true);
                } else if(option == DialogueOption.SECOND_OPTION) {
                    Bank.deposit(player, item.getId(), slot, 5, true);
                } else {
                    Bank.deposit(player, item.getId(), slot, 10, true);
                }
                player.getPacketSender().sendInterfaceRemoval();
            }, "One", "Five", "Ten"));
        } else {
            builder.add(new OptionDialogue(0, (option) -> {
                if(option == DialogueOption.FIRST_OPTION) {
                    Bank.deposit(player, item.getId(), slot, 1, true);
                } else if(option == DialogueOption.SECOND_OPTION) {
                    Bank.deposit(player, item.getId(), slot, 5, true);
                } else if(option == DialogueOption.THIRD_OPTION){
                    Bank.deposit(player, item.getId(), slot, 10, true);
                } else {
                    Bank.deposit(player, item.getId(), slot, player.getInventory().getAmount(item.getId()), true);
                }
                player.getPacketSender().sendInterfaceRemoval();
            }, "One", "Five", "Ten", "All"));
        }

        player.getDialogueManager().start(builder);
        return true;
    }

    public static void deposit(Player player, int item, int slot, int amount) {
        deposit(player, item, slot, amount, false);
    }

    /**
     * Deposits an item to the bank.
     *
     * @param player
     * @param item
     * @param slot
     * @param amount
     */

    public static void deposit(Player player, int item, int slot, int amount, boolean ignore) {
        if (ignore || player.getStatus() == PlayerStatus.BANKING
                && player.getInterfaceId() == 5292 /* Regular bank */
                || player.getInterfaceId() == 4465 /* Bank deposit booth */) {
            if (player.getInventory().getItems()[slot].getId() != item) {
                return;
            }

            if (amount == -1 || amount > player.getInventory().getAmount(item)) {
                amount = player.getInventory().getAmount(item);
            }

            if (amount <= 0) {
                return;
            }

            final int tab = Bank.getTabForItem(player, item);
            if (!player.isSearchingBank()) {
                player.setCurrentBankTab(tab);
            }

            player.getInventory().switchItem(player.getBank(tab), new Item(item, amount), slot, false,
                    !player.isSearchingBank());
            if (player.isSearchingBank()) {
                player.getBank(BANK_SEARCH_TAB_INDEX).refreshItems();
            }

            // Refresh inventory
            player.getInventory().refreshItems();
        }
    }

    /**
     * Searches the bank for an item.
     *
     * @param player
     * @param syntax
     */
    public static void search(Player player, String syntax) {
        if (player.getStatus() == PlayerStatus.BANKING && player.getInterfaceId() == 5292) {

            // Set search fields
            player.setSearchSyntax(syntax);
            player.setSearchingBank(true);

            // Clear search bank tab
            player.getBank(BANK_SEARCH_TAB_INDEX).resetItems();

            // Refill search bank tab
            for (int i = 0; i < TOTAL_BANK_TABS; i++) {
                if (i == BANK_SEARCH_TAB_INDEX) {
                    continue;
                }
                Bank b = player.getBank(i);
                if (b != null) {
                    b.sortItems();
                    for (Item item : b.getValidItems()) {
                        if (item.getAmount() > 0) {
                            addToBankSearch(player, item.clone(), false);
                        }
                    }
                }
            }

            player.setCurrentBankTab(0);

            // Open the search bank tab
            player.getBank(BANK_SEARCH_TAB_INDEX).open();
        }
    }

    public static void exitSearch(Player player, boolean openBank) {
        if (player.getStatus() == PlayerStatus.BANKING && player.getInterfaceId() == 5292) {

            // Set search fields
            player.setSearchSyntax("");
            player.setSearchingBank(false);

            // Clear search bank tab
            player.getBank(BANK_SEARCH_TAB_INDEX).resetItems();

            // Open last tab we had
            if (player.getCurrentBankTab() == BANK_SEARCH_TAB_INDEX) {
                player.setCurrentBankTab(0);
            }

            if (openBank) {
                player.getBank(player.getCurrentBankTab()).open();
            }
        }
    }

    /**
     * Adds an item to the bank search tab
     *
     * @param player
     * @param item
     */
    public static void addToBankSearch(Player player, Item item, boolean refresh) {

        if (player.getBank(BANK_SEARCH_TAB_INDEX).getFreeSlots() == 0) {
            return;
        }

        if (item.getDefinition().getName().toLowerCase().contains(player.getSearchSyntax())) {
            player.getBank(BANK_SEARCH_TAB_INDEX).add(item, refresh);
        }
    }

    /**
     * Removes an item from the bank search tab
     *
     * @param player
     * @param item
     */
    public static void removeFromBankSearch(Player player, Item item, boolean refresh) {

        if (item.getDefinition().isNoted()) {
            item.setId(item.getDefinition().unNote());
        }

        player.getBank(BANK_SEARCH_TAB_INDEX).delete(item, refresh);
    }

    /**
     * Moves an item from one slot to another using the insert method. It will shift
     * all other items to the right.
     *
     * @param player
     * @param fromSlot
     * @param toSlot
     */
    public static void rearrange(Player player, Bank bank, int fromSlot, int toSlot) {
        if (player.insertMode()) {

            int tempFrom = fromSlot;

            for (int tempTo = toSlot; tempFrom != tempTo; ) {
                if (tempFrom > tempTo) {
                    bank.swap(tempFrom, tempFrom - 1);
                    tempFrom--;
                } else if (tempFrom < tempTo) {
                    bank.swap(tempFrom, tempFrom + 1);
                    tempFrom++;
                }
            }

        } else {
            bank.swap(fromSlot, toSlot);
        }

        if (player.getCurrentBankTab() == 0 && !player.isSearchingBank()) {
            player.getBank(0).refreshItems();
        } else {
            bank.refreshItems();
        }

        // Update all tabs if we moved an item from/to the first item slot
        if (fromSlot == 0 || toSlot == 0) {
            Bank.reconfigureTabs(player);
        }
    }

    /**
     * Handles a button pressed in the bank interface.
     *
     * @param player
     * @param button
     * @return
     */
    public static boolean handleButton(Player player, int button, int action) {
        if (player.getInterfaceId() == 32500) {
            // Handle bank settings
            switch (button) {
            case 32503:
                player.getPacketSender().sendInterfaceRemoval();
                break;
            case 32512:
                player.getBank(player.getCurrentBankTab()).open();
                break;
            case 32513:
                player.setPlaceholders(!player.isPlaceholders());
                player.getPacketSender().sendConfig(118, player.isPlaceholders() ? 1 : 0);
                player.getPacketSender().sendMessage(
                        "Placeholders are now " + (player.isPlaceholders() ? "enabled" : "disabled") + ".");
                break;
            }
            return true;
        } else if (player.getInterfaceId() == 5292) {
            if (player.getStatus() == PlayerStatus.BANKING) {
                int tab_select_start = 50070;
                for (int bankId = 0; bankId < TOTAL_BANK_TABS; bankId++) {
                    if (button == tab_select_start + (bankId * 4)) {

                        final boolean searching = player.isSearchingBank();
                        if (searching) {
                            exitSearch(player, false);
                        }

                        // First, check if empty
                        boolean empty = bankId > 0 ? Bank.isEmpty(player.getBank(bankId)) : false;

                        if (action == 1) {
                            // Collapse tab!!!

                            // Cannot collapse main tab
                            if (bankId == 0) {
                                return true;
                            }

                            // Cannot collapse empty tab
                            if (empty) {
                                return true;
                            }

                            ArrayList<Item> items = player.getBank(bankId).getValidItems();

                            // Check if main tab has space
                            if (player.getBank(0).getFreeSlots() < items.size()) {
                                player.getPacketSender()
                                        .sendMessage("You don't have enough free slots in your Main tab to do that.");
                                return true;
                            }

                            // Temporarily disabled note withdrawal...
                            final boolean noteWithdrawal = player.withdrawAsNote();
                            player.setNoteWithdrawal(false);

                            // Move items from tab to main tab
                            for (Item item : items) {
                                player.getBank(bankId).switchItem(player.getBank(0), item.clone(),
                                        player.getBank(bankId).getSlotForItemId(item.getId()), false, false);
                            }

                            // Reactivate note withdrawal if it was active
                            player.setNoteWithdrawal(noteWithdrawal);

                            // Update tabs
                            reconfigureTabs(player);

                            // Update
                            player.getBank(player.getCurrentBankTab()).refreshItems();

                        } else {

                            // Select tab!!!
                            if (!empty || bankId == 0) {

                                player.setCurrentBankTab(bankId);
                                player.getBank(bankId).open();

                            } else {
                                player.getPacketSender().sendMessage("To create a new tab, simply drag an item here.");
                                if (searching) {
                                    player.getBank(player.getCurrentBankTab()).open();
                                }
                            }
                        }
                        return true;
                    }
                }

                switch (button) {
                case 50013:
                    // Show menu
                    player.getPacketSender().sendInterfaceRemoval();
                    player.getPacketSender().sendInterface(32500);
                    break;
                case 5386:
                    player.setNoteWithdrawal(true);
                    break;
                case 5387:
                    player.setNoteWithdrawal(false);
                    break;
                case 8130:
                    player.setInsertMode(false);
                    break;
                case 8131:
                    player.setInsertMode(true);
                    break;
                case 50004:
                    depositItems(player, player.getInventory(), false);
                    break;
                case 50007:
                    depositItems(player, player.getEquipment(), false);
                    break;
                case 5384:
                case 50001:
                    player.getPacketSender().sendInterfaceRemoval();
                    break;
                case 50010:
                    if (player.isSearchingBank()) {
                        exitSearch(player, true);
                        return true;
                    }
                    player.setEnteredSyntaxAction((input) -> {
                        Bank.search(player, input);
                    });
                    player.getPacketSender().sendEnterInputPrompt("What do you wish to search for?");
                    break;
                }
            }
            return true;
        }

        return false;
    }

    /**
     * Deposits items from another container into the bank. Used for depositing
     * inventory/equipment.
     *
     * @param player
     * @param from
     * @param ignoreReqs
     */
    public static void depositItems(Player player, ItemContainer from, boolean ignoreReqs) {

        if (!ignoreReqs) {
            if (player.getStatus() != PlayerStatus.BANKING || player.getInterfaceId() != 5292) {
                return;
            }
        }

        for (Item item : from.getValidItems()) {
            from.switchItem(player.getBank(Bank.getTabForItem(player, item.getId())), item.clone(),
                    from.getSlotForItemId(item.getId()), false, false);
        }

        from.refreshItems();

        if (player.isSearchingBank()) {
            player.getBank(BANK_SEARCH_TAB_INDEX).refreshItems();
        } else {
            player.getBank(player.getCurrentBankTab()).refreshItems();
        }

        if (from instanceof Equipment) {
            WeaponInterfaces.assign(player);
            BonusManager.update(player);
            player.getUpdateFlag().flag(Flag.APPEARANCE);
        }
    }

    /**
     * Is a bank empty?
     *
     * @param bank
     * @return
     */
    public static boolean isEmpty(Bank bank) {
        return bank.sortItems().getValidItems().size() <= 0;
    }

    /**
     * Reconfigures our bank tabs
     *
     * @param player
     */
    public static boolean reconfigureTabs(Player player) {

        boolean updateRequired = false;
        for (int k = 1; k < BANK_SEARCH_TAB_INDEX - 1; k++) {
            if (isEmpty(player.getBank(k)) || updateRequired) {
                player.setBank(k, player.getBank(k + 1));
                player.setBank(k + 1, new Bank(player));
                updateRequired = true;
            }
        }

        // Check if we're in a tab that's empty
        // If so, open the next non-empty tab
        int total_tabs = getTabCount(player);
        if (!player.isSearchingBank()) {
            if (player.getCurrentBankTab() > total_tabs) {
                player.setCurrentBankTab(total_tabs);
                player.getBank(total_tabs).open();
                return true;
            }
        }

        return false;
    }

    /**
     * Gets the amount of filled tabs we have.
     *
     * @param player
     * @return
     */
    public static int getTabCount(Player player) {
        int tabs = 0;
        for (int i = 1; i < TOTAL_BANK_TABS; i++) {
            if (i == BANK_SEARCH_TAB_INDEX) {
                continue;
            }
            if (!isEmpty(player.getBank(i))) {
                tabs++;
            } else
                break;
        }
        return tabs;
    }

    /**
     * Gets the specific tab in which an item is.
     *
     * @param player
     * @param itemID
     * @return
     */
    public static int getTabForItem(Player player, int itemID) {
        if (ItemDefinition.forId(itemID).isNoted()) {
            itemID = ItemDefinition.forId(itemID).unNote();
        }
        for (int k = 0; k < TOTAL_BANK_TABS; k++) {
            if (k == BANK_SEARCH_TAB_INDEX) {
                continue;
            }
            if (player.getBank(k).contains(itemID)) {
                return k;
            }
        }

        // Find empty bank slot
        if (player.getBank(player.getCurrentBankTab()).getFreeSlots() > 0) {
            return player.getCurrentBankTab();
        }
        for (int k = 0; k < TOTAL_BANK_TABS; k++) {
            if (k == BANK_SEARCH_TAB_INDEX) {
                continue;
            }
            if (player.getBank(k).getFreeSlots() > 0) {
                return k;
            }
        }
        return 0;
    }

    public static boolean contains(Player player, Item item) {
        int tab = getTabForItem(player, item.getId());
        return player.getBank(tab).getAmount(item.getId()) >= item.getAmount();
    }

    @Override
    public int capacity() {
        return 352;
    }

    @Override
    public StackType stackType() {
        return StackType.STACKS;
    }

    public Bank open() {

        // Update player status
        getPlayer().setStatus(PlayerStatus.BANKING);
        getPlayer().setEnteredSyntaxAction(null);

        // Sort and refresh items in the container
        sortItems().refreshItems();

        // Send configs
        getPlayer().getPacketSender().sendConfig(115, getPlayer().withdrawAsNote() ? 1 : 0)
                .sendConfig(304, getPlayer().insertMode() ? 1 : 0)
                .sendConfig(117, getPlayer().isSearchingBank() ? 1 : 0)
                .sendConfig(118, getPlayer().isPlaceholders() ? 1 : 0).sendInterfaceSet(5292, 5063);

        // Resets the scroll bar in the interface
        getPlayer().getPacketSender().sendInterfaceScrollReset(BANK_SCROLL_BAR_INTERFACE_ID);

        return this;
    }

    @Override
    public Bank refreshItems() {

        // Reconfigure bank tabs.
        if (reconfigureTabs(getPlayer())) {
            return this;
        }

        // Send capacity information about the current bank we're in
        getPlayer().getPacketSender().sendString(50053, "" + this.getValidItems().size());
        getPlayer().getPacketSender().sendString(50054, "" + this.capacity());

        // Send all bank tabs and their contents
        for (int i = 0; i < TOTAL_BANK_TABS; i++) {
            getPlayer().getPacketSender().sendItemContainer(getPlayer().getBank(i), CONTAINER_START + i);
        }

        // Send inventory
        getPlayer().getPacketSender().sendItemContainer(getPlayer().getInventory(), INVENTORY_INTERFACE_ID);

        // Update bank title
        if (getPlayer().isSearchingBank()) {
            getPlayer().getPacketSender().sendString(5383, "Results for " + getPlayer().getSearchSyntax() + "..")
                    .sendConfig(117, 1);
        } else {
            getPlayer().getPacketSender().sendString(5383, "Bank of " + GameConstants.NAME).sendConfig(117, 0);
        }

        // Send current bank tab being viewed and total tabs!
        int current_tab = getPlayer().isSearchingBank() ? BANK_SEARCH_TAB_INDEX : getPlayer().getCurrentBankTab();
        getPlayer().getPacketSender().sendCurrentBankTab(current_tab);

        return this;
    }

    @Override
    public Bank full() {
        getPlayer().getPacketSender().sendMessage("Not enough space in bank.");
        return this;
    }

    @Override
    public Bank switchItem(ItemContainer to, Item item, int slot, boolean sort, boolean refresh) {

        // Make sure we're actually banking!
        if (getPlayer().getStatus() != PlayerStatus.BANKING || getPlayer().getInterfaceId() != 5292) {
            return this;
        }

        // Make sure we have the item!
        if (getItems()[slot].getId() != item.getId() || !contains(item.getId())) {
            return this;
        }

        // Get the item definition for the item which is being withdrawn
        ItemDefinition def = ItemDefinition.forId(item.getId() + 1);
        if (def == null) {
            return this;
        }

        // Make sure we have enough space in the other container
        if (to.getFreeSlots() <= 0 && (!(to.contains(item.getId()) && item.getDefinition().isStackable()))
                && !(getPlayer().withdrawAsNote() && def != null && def.isNoted() && to.contains(def.getId()))) {
            to.full();
            return this;
        }

        // If bank > inventory and item.amount > inventory.freeslots,
        // change the item amount to the free slots we have in inventory.
        if (item.getAmount() > to.getFreeSlots() && !item.getDefinition().isStackable()) {
            if (to instanceof Inventory) {
                if (getPlayer().withdrawAsNote()) {
                    if (def == null || !def.isNoted())
                        item.setAmount(to.getFreeSlots());
                } else
                    item.setAmount(to.getFreeSlots());
            }
        }

        // Make sure we aren't taking more than we have.
        if (item.getAmount() > getAmount(item.getId())) {
            item.setAmount(getAmount(item.getId()));
        }

        if (to instanceof Inventory) {
            boolean withdrawAsNote = getPlayer().withdrawAsNote() && def != null && def.isNoted()
                    && item.getDefinition() != null && def.getName().equalsIgnoreCase(item.getDefinition().getName());
            int checkId = withdrawAsNote ? item.getId() + 1 : item.getId();
            if (to.getAmount(checkId) + item.getAmount() > Integer.MAX_VALUE
                    || to.getAmount(checkId) + item.getAmount() <= 0) {
            	item.setAmount(Integer.MAX_VALUE - (to.getAmount(item.getId())));
            	if (item.getAmount() <= 0) {
            		getPlayer().getPacketSender()
            		.sendMessage("You cannot withdraw that entire amount into your inventory.");
            		 return this;
            	}
            }
        }

        // Make sure the item is still valid
        if (item.getAmount() <= 0) {
            return this;
        }

        // Delete item from this container
        delete(item, slot, refresh, to);

        // Check if we can actually withdraw the item as a note.
        if (getPlayer().withdrawAsNote()) {
            if (def != null && def.isNoted() && item.getDefinition() != null
                    && def.getName().equalsIgnoreCase(item.getDefinition().getName())
                    && !def.getName().contains("Torva") && !def.getName().contains("Virtus")
                    && !def.getName().contains("Pernix") && !def.getName().contains("Torva"))
                item.setId(item.getId() + 1);
            else
                getPlayer().getPacketSender().sendMessage("This item cannot be withdrawn as a note.");
        }

        // Add the item to the other container
        to.add(item, refresh);

        // Sort this container
        if (sort && getAmount(item.getId()) <= 0)
            sortItems();

        // Refresh containers
        if (refresh) {
            refreshItems();
            to.refreshItems();
        }

        if (getPlayer().isSearchingBank()) {
            removeFromBankSearch(getPlayer(), item.clone(), true);
        }

        return this;
    }
}
