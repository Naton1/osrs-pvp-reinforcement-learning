package com.elvarg.game.model.container.shop.currency;

import com.elvarg.game.entity.impl.player.Player;

public interface ShopCurrency {
    /**
     * Gets the name of the currency - as displayed in messages and dialogues.
     *
     * @return
     */
    public abstract String getName();

    /**
     * Gets the total amount of currency currently spendable by the Player.
     *
     * @param player
     * @return
     */
    public abstract int getAmountForPlayer(Player player);

    /**
     * Decrements the currency by a given amount for the Player.
     *
     * @param player
     * @param amount
     */
    public abstract void decrementForPlayer(Player player, int amount);

    /**
     * Decrements the currency by a given amount for the Player.
     *
     * @param player
     * @param amount
     */
    public abstract void incrementForPlayer(Player player, int amount);


}
