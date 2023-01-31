package com.elvarg.game.model.container.shop;

import com.elvarg.game.model.Item;
import com.elvarg.game.model.container.shop.currency.ShopCurrencies;
import com.elvarg.game.model.container.shop.currency.ShopCurrency;
import com.elvarg.game.model.container.shop.currency.impl.CoinsCurrency;

import java.util.ArrayList;
import java.util.List;

public class Shop {

    /**
     * The tax modifier which applies for players
     * selling items.
     */
    public static final double SALES_TAX_MODIFIER = 0.85;
    /**
     * The max amount of items a shop can have.
     */
    public static final int MAX_SHOP_ITEMS = 1000;
    /**
     * The max amount of items a shop can have.
     */
    public static final int MAX_SHOPS = 5000;
    /**
     * The shop interface id.
     */
    public static final int INTERFACE_ID = 3824;
    /**
     * The starting interface child id of items.
     */
    public static final int ITEM_CHILD_ID = 3900;
    /**
     * The interface child id of the shop's name.
     */
    public static final int NAME_INTERFACE_CHILD_ID = 3901;
    /**
     * The inventory interface id, used to set the items right click values
     * to 'sell'.
     */
    public static final int INVENTORY_INTERFACE_ID = 3823;
    /**
     * The scrollbar interface id
     */
    public static final int SCROLL_BAR_INTERFACE_ID = 29995;
    /**
     * The item amount which shows as infinity.
     */
    public static final int INFINITY = 2000000000;

    /**
     * The default currency for shops.
     */
    public static final ShopCurrency CURRENCY_COINS = ShopCurrencies.COINS.get();

    private final int id;
    private final String name;
    private final Item[] originalStock;
    private final Item[] currentStock = new Item[MAX_SHOP_ITEMS];
    private boolean restocking;
    private ShopCurrency currency;

    public Shop(int id, String name, Item[] originalStock) {
        this.id = id;
        this.name = name;
        this.originalStock = originalStock;
        for (int i = 0; i < originalStock.length; i++) {
            this.currentStock[i] = originalStock[i].clone();
        }
        this.currency = CURRENCY_COINS;
    }

    /**
     * Allows for creation of a Shop with a defined currency.
     * @param id
     * @param name
     * @param originalStock
     */
    public Shop(int id, String name, Item[] originalStock, ShopCurrency currency) {
        this.id = id;
        this.name = name;
        this.originalStock = originalStock;
        for (int i = 0; i < originalStock.length; i++) {
            this.currentStock[i] = originalStock[i].clone();
        }
        this.currency = currency;
    }

    /**
     * Allows for creation of a Shop without an explicit Id and with a currency.
     *
     * @param name
     * @param originalStock
     */
    public Shop(String name, Item[] originalStock, ShopCurrency currency) {
       this(ShopManager.generateUnusedId(), name, originalStock, currency);
    }

    public void removeItem(int itemId, int amount) {
        for (int i = 0; i < currentStock.length; i++) {
            Item item = currentStock[i];
            if (item == null)
                continue;
            if (item.getId() == itemId) {
                item.setAmount(item.getAmount() - amount);
                if (item.getAmount() <= 1) {
                    if (ShopManager.deletesItems(id)) {
                        currentStock[i] = null;
                    } else {
                        item.setAmount(1);
                    }
                }
                break;
            }
        }
    }

    public void addItem(int itemId, int amount) {
        boolean found = false;
        for (Item item : currentStock) {
            if (item == null)
                continue;
            if (item.getId() == itemId) {
                long amt = item.getAmount() + amount;
                if (amt < Integer.MAX_VALUE) {
                    item.setAmount(item.getAmount() + amount);
                    found = true;
                    break;
                }
            }
        }
        if (!found) {
            for (int i = 0; i < currentStock.length; i++) {
                if (currentStock[i] == null) {
                    currentStock[i] = new Item(itemId, amount);
                    break;
                }
            }
        }
    }

    public boolean isFull() {
        int amount = 0;
        for (Item item : currentStock) {
            if (item == null)
                continue;
            amount++;
        }
        return (amount >= MAX_SHOP_ITEMS);
    }

    public int getAmount(int itemId, boolean fromOriginalStock) {
        if (!fromOriginalStock) {
            for (Item item : currentStock) {
                if (item == null)
                    continue;
                if (item.getId() == itemId) {
                    return item.getAmount();
                }
            }
        } else {
            for (Item item : originalStock) {
                if (item.getId() == itemId) {
                    return item.getAmount();
                }
            }
        }
        return 0;
    }

    public List<Item> getCurrentStockList() {
        List<Item> list = new ArrayList<Item>();
        for (Item item : currentStock) {
            if (item == null)
                continue;
            list.add(item);
        }
        return list;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public ShopCurrency getCurrency() {
        return this.currency;
    }

    public Item[] getCurrentStock() {
        return currentStock;
    }

    public Item[] getOriginalStock() {
        return originalStock;
    }

    public boolean isRestocking() {
        return restocking;
    }

    public void setRestocking(boolean restocking) {
        this.restocking = restocking;
    }
}
