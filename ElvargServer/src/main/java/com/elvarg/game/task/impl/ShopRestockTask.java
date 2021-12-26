package com.elvarg.game.task.impl;

import com.elvarg.game.model.Item;
import com.elvarg.game.model.container.shop.Shop;
import com.elvarg.game.model.container.shop.ShopManager;
import com.elvarg.game.task.Task;
import com.elvarg.util.Misc;

import java.util.ArrayList;
import java.util.List;

public class ShopRestockTask extends Task {

    private final Shop shop;

    public ShopRestockTask(Shop shop) {
        super(10);
        this.shop = shop;
    }

    private static int restockCalc(int overflow, int curr) {
        int missing = overflow - curr;
        int amount = (int) (missing * 0.3);
        if (amount < 1) {
            amount = 1;
        }
        return amount;
    }

    @Override
    protected void execute() {

        List<Integer> items = new ArrayList<Integer>();
        for (Item item : Misc.concat(shop.getCurrentStock(), shop.getOriginalStock())) {
            if (item == null)
                continue;
            int itemId = item.getId();
            if (!items.contains(itemId)) {
                items.add(itemId);
            }
        }

        boolean performedUpdate = false;

        for (int itemId : items) {
            int originalAmount = shop.getAmount(itemId, true);
            int currentAmount = shop.getAmount(itemId, false);

            // If we have too many in stock, delete some..
            if (currentAmount > originalAmount) {
                shop.removeItem(itemId, restockCalc(currentAmount, originalAmount));
                performedUpdate = true;
            }

            // If we have too few in stock, add some..
            else if (currentAmount < originalAmount) {
                if (ShopManager.restocksItem(shop.getId())) {
                    shop.addItem(itemId, restockCalc(originalAmount, currentAmount));
                    performedUpdate = true;
                }
            }
        }

        if (performedUpdate) {
            ShopManager.refresh(shop);
        } else {
            stop();
        }
    }

    @Override
    public void stop() {
    	super.stop();
        shop.setRestocking(false);
    }
}
