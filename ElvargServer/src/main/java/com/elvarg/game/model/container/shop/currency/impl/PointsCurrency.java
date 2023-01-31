package com.elvarg.game.model.container.shop.currency.impl;

import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.container.shop.currency.ShopCurrency;

public class PointsCurrency implements ShopCurrency {

    @Override
    public String getName() {
        return "Points";
    }

    @Override
    public int getAmountForPlayer(Player player) {
        return player.getPoints();
    }

    @Override
    public void decrementForPlayer(Player player, int amount) {
        player.setPoints(player.getPoints() - amount);
    }

    @Override
    public void incrementForPlayer(Player player, int amount) {
        player.setPoints(player.getPoints() + amount);
    }
}
