package com.elvarg.game.model.container.shop.currency.impl;

import com.elvarg.game.definition.ItemDefinition;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.container.shop.currency.ShopCurrency;

public class ItemCurrency implements ShopCurrency {

    ItemDefinition itemDefinition;
    int itemId;

    /**
     * A convenient ShopCurrency that can be extended for any typical Inventory based currency. (Coins, Castlewars tokens, etc...)
     *
     * @param itemId
     */
    public ItemCurrency(int itemId) {
        this.itemId = itemId;
    }

    public ItemDefinition getItemDefinition() {
        if (this.itemDefinition == null) {
            this.itemDefinition = ItemDefinition.forId(this.itemId);
        }

        return this.itemDefinition;
    }

    @Override
    public String getName() {
        return this.getItemDefinition().getName();
    }

    @Override
    public int getAmountForPlayer(Player player) {
        return player.getInventory().getAmount(this.itemDefinition.getId());
    }

    @Override
    public void decrementForPlayer(Player player, int amount) {
        player.getInventory().delete(this.itemDefinition.getId(), amount);
    }

    @Override
    public void incrementForPlayer(Player player, int amount) {
        player.getInventory().add(this.itemDefinition.getId(), amount);
    }
}
