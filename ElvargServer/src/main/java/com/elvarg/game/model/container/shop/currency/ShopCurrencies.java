package com.elvarg.game.model.container.shop.currency;

import com.elvarg.game.model.container.shop.currency.impl.BloodMoneyCurrency;
import com.elvarg.game.model.container.shop.currency.impl.CastleWarsTicketCurrency;
import com.elvarg.game.model.container.shop.currency.impl.CoinsCurrency;
import com.elvarg.game.model.container.shop.currency.impl.PointsCurrency;

public enum ShopCurrencies {
    COINS(new CoinsCurrency()),
    BLOOD_MONEY(new BloodMoneyCurrency()),
    CASTLE_WARS_TICKET(new CastleWarsTicketCurrency()),
    POINTS(new PointsCurrency())
    ;

    private final ShopCurrency currency;

    ShopCurrencies(ShopCurrency currency) {
        this.currency = currency;
    }

    public ShopCurrency get() {
        return currency;
    }
}
