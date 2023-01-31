package com.elvarg.game.definition;

import com.elvarg.game.model.Item;
import com.elvarg.game.model.container.shop.currency.ShopCurrencies;

/**
 * Represents a definition for a shop.
 *
 * @author Professor Oak
 */
public class ShopDefinition {

    private int id;
    private String name = "";
    private Item[] originalStock;
    private ShopCurrencies currency;

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Item[] getOriginalStock() {
        return originalStock;
    }

    public ShopCurrencies getCurrency() {
        return currency;
    }
}
