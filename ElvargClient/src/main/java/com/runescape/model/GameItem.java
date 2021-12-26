package com.runescape.model;

public class GameItem {

    int item;
    int amount;

    public GameItem(int item, int amount) {
        this.item = item;
        this.amount = amount;
    }

    public int getItem() {
        return item;
    }

    public int getAmount() {
        return amount;
    }
}
