package com.elvarg.game.entity.impl.playerbot.interaction;

import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.entity.impl.playerbot.PlayerBot;
import com.elvarg.game.entity.impl.playerbot.commands.HoldItems;
import com.elvarg.game.model.Item;
import com.elvarg.game.model.container.ItemContainer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TradingInteraction {

    // The PlayerBot this trading interaction belongs to
    PlayerBot playerBot;

    // Items which this bot is currently attempting to give in a trade
    public List<Item> tradingItems = new ArrayList<>();

    public HashMap<Player, ArrayList<Item>> holdingItems = new HashMap<Player, ArrayList<Item>>();

    public TradingInteraction(PlayerBot _playerBot) {
        this.playerBot = _playerBot;
    }

    // This method is called when a trade window opens between a Player and a PlayerBot
    public void addItemsToTrade(ItemContainer container, Player tradingWith) {
        if (this.tradingItems.size() > 0) {
            // Stage the items the bot is trying to give
            container.addItems(this.tradingItems, true);
        } else if (this.holdingItems.containsKey(tradingWith)) {
            // Return the player back their items
            List<Item> storedItems = this.holdingItems.get(tradingWith);
            container.addItems(storedItems, true);
            playerBot.sendChat("Here's your stuff back, as promised");
        } else {
            // Ask the player to give us something
            playerBot.sendChat("Give me stuff to store for you, " + tradingWith.getUsername());
        }
    }

    public void acceptTradeRequest(Player interact) {
        if (playerBot.getInteractingWith() != null && playerBot.getInteractingWith() != interact) {
            playerBot.sendChat("Sorry, i'm busy rn with " + playerBot.getInteractingWith().getUsername() + ".");
            return;
        }

        playerBot.getTrading().requestTrade(interact);
    }

    public void acceptTrade() {
        playerBot.getTrading().acceptTrade();
    }

    public void receivedItems(ArrayList<Item> itemsReceived, Player fromPlayer) {
        Item first = itemsReceived.get(0);
        playerBot.sendChat("Thanks for giving me some stuff. " + first.getDefinition().getName() + " x " + first.getAmount());

        // Store these items for the Player in memory
        holdingItems.put(fromPlayer, itemsReceived);

        // Clear the PlayerBot's inventory for someone else
        playerBot.getInventory().resetItems().refreshItems();

        // End the current command, if applicable
        if (playerBot.getActiveCommand() instanceof HoldItems) {
            playerBot.stopCommand();
        }
    }
}
