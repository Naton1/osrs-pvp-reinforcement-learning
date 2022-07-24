package com.elvarg.game.content;

import com.elvarg.game.definition.ItemDefinition;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.entity.impl.playerbot.PlayerBot;
import com.elvarg.game.model.Item;
import com.elvarg.game.model.PlayerStatus;
import com.elvarg.game.model.SecondsTimer;
import com.elvarg.game.model.container.ItemContainer;
import com.elvarg.game.model.container.StackType;
import com.elvarg.game.model.container.impl.Inventory;
import com.elvarg.util.Misc;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles the entire trading system. Should be dupe-free.
 *
 * @author Swiffy
 */

public class Trading {

    public static final int CONTAINER_INTERFACE_ID = 3415;
    public static final int CONTAINER_INVENTORY_INTERFACE = 3321;
    public static final int INVENTORY_CONTAINER_INTERFACE = 3322;
    // Interface data
    private static final int INTERFACE = 3323;
    private static final int CONTAINER_INTERFACE_ID_2 = 3416;
    private static final int CONFIRM_SCREEN_INTERFACE = 3443;

    // Frames data
    private static final int TRADING_WITH_FRAME = 3417;
    private static final int STATUS_FRAME_1 = 3431;
    private static final int STATUS_FRAME_2 = 3535;
    private static final int ITEM_LIST_1_FRAME = 3557;
    private static final int ITEM_LIST_2_FRAME = 3558;
    private static final int ITEM_VALUE_1_FRAME = 24209;
    private static final int ITEM_VALUE_2_FRAME = 24210;

    // Nonstatic
    private final Player player;
    private final ItemContainer container;
    private Player interact;
    private TradeState state = TradeState.NONE;

    // Delays!!
    private SecondsTimer button_delay = new SecondsTimer();
    private SecondsTimer request_delay = new SecondsTimer();

    // Constructor
    public Trading(Player player) {
        this.player = player;

        // The container which will hold all our offered items.
        this.container = new ItemContainer(player) {
            @Override
            public StackType stackType() {
                return StackType.DEFAULT;
            }

            @Override
            public ItemContainer refreshItems() {
                player.getPacketSender().sendInterfaceSet(INTERFACE, CONTAINER_INVENTORY_INTERFACE);
                player.getPacketSender().sendItemContainer(container, CONTAINER_INTERFACE_ID);
                player.getPacketSender().sendItemContainer(player.getInventory(), INVENTORY_CONTAINER_INTERFACE);
                player.getPacketSender().sendItemContainer(interact.getTrading().getContainer(),
                        CONTAINER_INTERFACE_ID_2);
                interact.getPacketSender().sendItemContainer(player.getTrading().getContainer(),
                        CONTAINER_INTERFACE_ID_2);
                return this;
            }

            @Override
            public ItemContainer full() {
                getPlayer().getPacketSender().sendMessage("You cannot trade more items.");
                return this;
            }

            @Override
            public int capacity() {
                return 28;
            }
        };
    }

    public static String listItems(ItemContainer items) {
        String string = "";
        int item_counter = 0;
        List<Item> list = new ArrayList<Item>();
        loop1:
        for (Item item : items.getValidItems()) {
            // Make sure the item isn't already in the list.
            for (Item item_ : list) {
                if (item_.getId() == item.getId()) {
                    continue loop1;
                }
            }
            list.add(new Item(item.getId(), items.getAmount(item.getId())));
        }
        for (Item item : list) {
            if (item_counter > 0) {
                string += "\\n";
            }
            string += item.getDefinition().getName().replaceAll("_", " ");

            String amt = "" + Misc.format(item.getAmount());
            if (item.getAmount() >= 1000000000) {
                amt = "@gre@" + (item.getAmount() / 1000000000) + " billion @whi@(" + Misc.format(item.getAmount())
                        + ")";
            } else if (item.getAmount() >= 1000000) {
                amt = "@gre@" + (item.getAmount() / 1000000) + " million @whi@(" + Misc.format(item.getAmount()) + ")";
            } else if (item.getAmount() >= 1000) {
                amt = "@cya@" + (item.getAmount() / 1000) + "K @whi@(" + Misc.format(item.getAmount()) + ")";
            }
            string += " x @red@" + amt;

            item_counter++;
        }
        if (item_counter == 0) {
            string = "Absolutely nothing!";
        }
        return string;
    }

    /**
     * Validates a player. Basically checks that all specified params add up.
     *
     * @param player
     * @param interact
     * @param playerStatus
     * @param duelStates
     * @return
     */
    private static boolean validate(Player player, Player interact, PlayerStatus playerStatus,
                                    TradeState... tradeState) {
        // Verify player...
        if (player == null || interact == null) {
            return false;
        }

        // Make sure we have proper status
        if (player.getStatus() != playerStatus) {
            return false;
        }

        // Make sure we're interacting with eachother
        if (interact.getStatus() != playerStatus) {
            return false;
        }

        if (player.getTrading().getInteract() == null || player.getTrading().getInteract() != interact) {
            return false;
        }
        if (interact.getTrading().getInteract() == null || interact.getTrading().getInteract() != player) {
            return false;
        }

        // Make sure we have proper duel state.
        boolean found = false;
        for (TradeState duelState : tradeState) {
            if (player.getTrading().getState() == duelState) {
                found = true;
                break;
            }
        }
        if (!found) {
            return false;
        }

        // Do the same for our interact
        found = false;
        for (TradeState duelState : tradeState) {
            if (interact.getTrading().getState() == duelState) {
                found = true;
                break;
            }
        }
        if (!found) {
            return false;
        }
        return true;
    }

    public void requestTrade(Player t_) {
        if (state == TradeState.NONE || state == TradeState.REQUESTED_TRADE) {

            // Make sure to not allow flooding!
            if (!request_delay.finished()) {
                int seconds = request_delay.secondsRemaining();
                player.getPacketSender()
                        .sendMessage("You must wait another " + (seconds == 1 ? "second" : "" + seconds + " seconds")
                                + " before sending more trade requests.");
                return;
            }

            // The other players' current trade state.
            final TradeState t_state = t_.getTrading().getState();

            // Should we initiate the trade or simply send a request?
            boolean initiateTrade = false;

            // Update this instance...
            this.setInteract(t_);
            this.setState(TradeState.REQUESTED_TRADE);

            // Check if target requested a trade with us...
            if (t_state == TradeState.REQUESTED_TRADE) {
                if (t_.getTrading().getInteract() != null && t_.getTrading().getInteract() == player) {
                    initiateTrade = true;
                }
            }

            // Initiate trade for both players with eachother?
            if (initiateTrade) {
                player.getTrading().initiateTrade();
                t_.getTrading().initiateTrade();
            } else {
                player.getPacketSender().sendMessage("You've sent a trade request to " + t_.getUsername() + ".");
                t_.getPacketSender().sendMessage(player.getUsername() + ":tradereq:");

                if (t_ instanceof PlayerBot) {
                    // Player Bots: Automatically accept any trade request
                    ((PlayerBot) t_).getTradingInteraction().acceptTradeRequest(player);
                }
            }

            // Set the request delay to 2 seconds at least.
            request_delay.start(2);
        } else {
            player.getPacketSender().sendMessage("You cannot do that right now.");
        }
    }

    public void initiateTrade() {

        // Update statuses
        player.setStatus(PlayerStatus.TRADING);
        player.getTrading().setState(TradeState.TRADE_SCREEN);

        // Update strings on interface
        player.getPacketSender().sendString(TRADING_WITH_FRAME, "Trading with: @whi@" + interact.getUsername());
        player.getPacketSender().sendString(STATUS_FRAME_1, "")
                .sendString(STATUS_FRAME_2, "Are you sure you want to make this trade?")
                .sendString(ITEM_VALUE_1_FRAME, "0 bm").sendString(ITEM_VALUE_2_FRAME, "0 bm");

        // Reset container
        container.resetItems();

        // Refresh and send container...
        container.refreshItems();

        if (player instanceof PlayerBot) {
            // For Player Bots, auto add items being given to the player
            ((PlayerBot) player).getTradingInteraction().addItemsToTrade(container, interact);
        }
    }

    public void closeTrade() {
        if (state != TradeState.NONE) {

            // Cache the current interact
            final Player interact_ = interact;

            // Return all items...
            for (Item t : container.getValidItems()) {
                container.switchItem(player.getInventory(), t.clone(), false, false);
            }

            // Refresh inventory
            player.getInventory().refreshItems();

            // Reset all attributes...
            resetAttributes();

            // Send decline message
            player.getPacketSender().sendMessage("Trade declined.");
            player.getPacketSender().sendInterfaceRemoval();

            // Reset trade for other player aswell (the cached interact)
            if (interact_ != null) {
                if (interact_.getStatus() == PlayerStatus.TRADING) {
                    if (interact_.getTrading().getInteract() != null
                            && interact_.getTrading().getInteract() == player) {
                        interact_.getPacketSender().sendInterfaceRemoval();
                    }
                }
            }
        }
    }

    public void acceptTrade() {

        // Validate this trade action..
        if (!validate(player, interact, PlayerStatus.TRADING, new TradeState[]{TradeState.TRADE_SCREEN,
                TradeState.ACCEPTED_TRADE_SCREEN, TradeState.CONFIRM_SCREEN, TradeState.ACCEPTED_CONFIRM_SCREEN})) {
            return;
        }

        // Check button delay...
        if (!button_delay.finished()) {
            return;
        }

        // Cache the interact...
        final Player interact_ = interact;

        // Interact's current trade state.
        final TradeState t_state = interact_.getTrading().getState();

        // Check which action to take..
        if (state == TradeState.TRADE_SCREEN) {

            // Verify that the interact can receive all items first..
            int slotsNeeded = 0;
            for (Item t : container.getValidItems()) {
                slotsNeeded += t.getDefinition().isStackable() && interact.getInventory().contains(t.getId()) ? 0 : 1;
            }

            int freeSlots = interact.getInventory().getFreeSlots();
            if (slotsNeeded > freeSlots) {
                player.getPacketSender().sendMessage("")
                        .sendMessage("@or3@" + interact.getUsername() + " will not be able to hold that item.")
                        .sendMessage(
                                "@or3@They have " + freeSlots + " free inventory slot" + (freeSlots == 1 ? "." : "s."));

                interact.getPacketSender()
                        .sendMessage("Trade cannot be accepted, you don't have enough free inventory space.");
                return;
            }

            // Both are in the same state. Do the first-stage accept.
            setState(TradeState.ACCEPTED_TRADE_SCREEN);

            // Update status...
            player.getPacketSender().sendString(STATUS_FRAME_1, "Waiting for other player..");
            interact_.getPacketSender().sendString(STATUS_FRAME_1, "" + player.getUsername() + " has accepted.");

            // Check if both have accepted..
            if (state == TradeState.ACCEPTED_TRADE_SCREEN && t_state == TradeState.ACCEPTED_TRADE_SCREEN) {

                // Technically here, both have accepted.
                // Go into confirm screen!
                player.getTrading().confirmScreen();
                interact_.getTrading().confirmScreen();
            } else {
                if (interact_ instanceof PlayerBot) {
                    ((PlayerBot) interact_).getTradingInteraction().acceptTrade();
                }
            }
        } else if (state == TradeState.CONFIRM_SCREEN) {

            // Both are in the same state. Do the second-stage accept.
            setState(TradeState.ACCEPTED_CONFIRM_SCREEN);

            // Update status...
            player.getPacketSender().sendString(STATUS_FRAME_2,
                    "Waiting for " + interact_.getUsername() + "'s confirmation..");
            interact_.getPacketSender().sendString(STATUS_FRAME_2,
                    "" + player.getUsername() + " has accepted. Do you wish to do the same?");

            // Check if both have accepted..
            if (state == TradeState.ACCEPTED_CONFIRM_SCREEN && t_state == TradeState.ACCEPTED_CONFIRM_SCREEN) {

                // Give items to both players...
                ArrayList<Item> receivingItems = interact_.getTrading().getContainer().getValidItems();
                for (Item item : receivingItems) {
                    player.getInventory().add(item);
                }

                ArrayList<Item> givingItems = player.getTrading().getContainer().getValidItems();
                for (Item item : givingItems) {
                    interact_.getInventory().add(item);
                }

                if (player instanceof PlayerBot && receivingItems.size() > 0) {
                    ((PlayerBot) player).getTradingInteraction().receivedItems(receivingItems, interact_);
                }

                // Reset attributes for both players...
                player.getTrading().resetAttributes();
                interact_.getTrading().resetAttributes();

                // Send interface removal for both players...
                player.getPacketSender().sendInterfaceRemoval();
                interact_.getPacketSender().sendInterfaceRemoval();

                // Send successful trade message!
                player.getPacketSender().sendMessage("Trade accepted!");
                interact_.getPacketSender().sendMessage("Trade accepted!");
            } else {
                if (interact_ instanceof PlayerBot) {
                    ((PlayerBot) interact_).getTradingInteraction().acceptTrade();
                }
            }
        }
        button_delay.start(1);
    }

    private void confirmScreen() {

        // Update state
        player.getTrading().setState(TradeState.CONFIRM_SCREEN);

        // Send new interface
        player.getPacketSender().sendInterfaceSet(CONFIRM_SCREEN_INTERFACE, CONTAINER_INVENTORY_INTERFACE);
        player.getPacketSender().sendItemContainer(player.getInventory(), INVENTORY_CONTAINER_INTERFACE);

        // Send new interface frames
        String this_items = listItems(container);
        String interact_item = listItems(interact.getTrading().getContainer());
        player.getPacketSender().sendString(ITEM_LIST_1_FRAME, this_items);
        player.getPacketSender().sendString(ITEM_LIST_2_FRAME, interact_item);
    }

    // Deposit or withdraw an item....
    public void handleItem(int id, int amount, int slot, ItemContainer from, ItemContainer to) {
        if (player.getInterfaceId() == INTERFACE) {

            // Validate this trade action..
            if (!validate(player, interact, PlayerStatus.TRADING,
                    new TradeState[]{TradeState.TRADE_SCREEN, TradeState.ACCEPTED_TRADE_SCREEN})) {
                return;
            }

            // Check if the trade was previously accepted (and now modified)...
            boolean modified = false;
            if (state == TradeState.ACCEPTED_TRADE_SCREEN) {
                state = TradeState.TRADE_SCREEN;
                modified = true;
            }
            if (interact.getTrading().getState() == TradeState.ACCEPTED_TRADE_SCREEN) {
                interact.getTrading().setState(TradeState.TRADE_SCREEN);
                modified = true;
            }
            if (modified) {
                player.getPacketSender().sendString(STATUS_FRAME_1, "@red@TRADE MODIFIED!");
                interact.getPacketSender().sendString(STATUS_FRAME_1, "@red@TRADE MODIFIED!");
            }

            // Handle the item switch..
            if (state == TradeState.TRADE_SCREEN && interact.getTrading().getState() == TradeState.TRADE_SCREEN) {

                // Check if the item is in the right place
                if (from.getItems()[slot].getId() == id) {

                    // Make sure we can fit that amount in the trade
                    if (from instanceof Inventory) {
                        if (!ItemDefinition.forId(id).isStackable()) {
                            if (amount > container.getFreeSlots()) {
                                amount = container.getFreeSlots();
                            }
                        }
                    }

                    if (amount <= 0) {
                        return;
                    }

                    final Item item = new Item(id, amount);

                    // Do the switch!
                    if (item.getAmount() == 1) {
                        from.switchItem(to, item, slot, false, true);
                    } else {
                        from.switchItem(to, item, false, true);
                    }

                    // Update value frames for both players
                    String plr_value = container.getTotalValue();
                    String other_plr_value = interact.getTrading().getContainer().getTotalValue();
                    player.getPacketSender().sendString(ITEM_VALUE_1_FRAME,
                            Misc.insertCommasToNumber(plr_value) + " bm");
                    player.getPacketSender().sendString(ITEM_VALUE_2_FRAME,
                            Misc.insertCommasToNumber(other_plr_value) + " bm");
                    interact.getPacketSender().sendString(ITEM_VALUE_1_FRAME,
                            Misc.insertCommasToNumber(other_plr_value) + " bm");
                    interact.getPacketSender().sendString(ITEM_VALUE_2_FRAME,
                            Misc.insertCommasToNumber(plr_value) + " bm");

                    if (interact instanceof PlayerBot) {
                        // Automatically accept the trade whenever an item is added by the player
                        interact.getTrading().acceptTrade();
                    }
                }
            } else {
                player.getPacketSender().sendInterfaceRemoval();
            }
        }
    }

    public void resetAttributes() {
        // Reset trade attributes
        setInteract(null);
        setState(TradeState.NONE);

        // Reset player status if it's trading.
        if (player.getStatus() == PlayerStatus.TRADING) {
            player.setStatus(PlayerStatus.NONE);
        }

        // Reset container..
        container.resetItems();

        // Send the new empty container to the interface
        // Just to clear the items there.
        player.getPacketSender().sendItemContainer(container, CONTAINER_INTERFACE_ID);
    }

    public TradeState getState() {
        return state;
    }

    public void setState(TradeState state) {
        this.state = state;
    }

    public SecondsTimer getButtonDelay() {
        return button_delay;
    }

    public Player getInteract() {
        return interact;
    }

    public void setInteract(Player interact) {
        this.interact = interact;
    }

    public ItemContainer getContainer() {
        return container;
    }

    // The possible states during a trade
    private enum TradeState {
        NONE, REQUESTED_TRADE, TRADE_SCREEN, ACCEPTED_TRADE_SCREEN, CONFIRM_SCREEN, ACCEPTED_CONFIRM_SCREEN;
    }
}
