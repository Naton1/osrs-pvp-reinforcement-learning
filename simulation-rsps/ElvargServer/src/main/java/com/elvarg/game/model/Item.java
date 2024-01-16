package com.elvarg.game.model;

import com.elvarg.game.content.minigames.impl.Barrows;
import com.elvarg.game.definition.ItemDefinition;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;

/**
 * Represents an item.
 *
 * @author Professor Oak
 */

@DynamoDbBean
public class Item {

    /**
     * The item id.
     */
    private int id;
    /**
     * Amount of the item.
     */
    private int amount;

    /**
     * An Item object constructor.
     *
     * @param id     Item id.
     * @param amount Item amount.
     */
    public Item(int id, int amount) {
        this.id = id;
        this.amount = amount;
    }

    /**
     * An Item object constructor.
     *
     * @param id Item id.
     */
    public Item(int id) {
        this(id, 1);
    }

    /**
     * Gets the item's id.
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the item's id.
     *
     * @param id New item id.
     */
    public Item setId(int id) {
        this.id = id;
        return this;
    }

    /**
     * Gets the amount of the item.
     */
    public int getAmount() {
        return amount;
    }

    /**
     * Sets the amount of the item.
     */
    public Item setAmount(int amount) {
        this.amount = amount;
        return this;
    }

    /**
     * Checks if this item is valid or not.
     *
     * @return
     */
    public boolean isValid() {
        return id > 0 && amount > 0;
    }

    /**
     * Increment the amount by 1.
     */
    public void incrementAmount() {
        if ((amount + 1) > Integer.MAX_VALUE) {
            return;
        }
        amount++;
    }

    /**
     * Decrement the amount by 1.
     */
    public void decrementAmount() {
        if ((amount - 1) < 0) {
            return;
        }
        amount--;
    }

    /**
     * Increment the amount by the specified amount.
     */
    public void incrementAmountBy(int amount) {
        if ((this.amount + amount) > Integer.MAX_VALUE) {
            this.amount = Integer.MAX_VALUE;
        } else {
            this.amount += amount;
        }
    }

    /**
     * Decrement the amount by the specified amount.
     */
    public void decrementAmountBy(int amount) {
        if ((this.amount - amount) < 1) {
            this.amount = 0;
        } else {
            this.amount -= amount;
        }
    }

    public int getIdOnDropOrDeath() {
        if (getDefinition().isBarrows())
            return Barrows.getBrokenId(this);
        return id;
    }

    public ItemDefinition getDefinition() {
        return ItemDefinition.forId(id);
    }

    @Override
    public Item clone() {
        return new Item(id, amount);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Item))
            return false;
        Item item = (Item) o;
        return item.getId() == this.getId() && item.getAmount() == this.getAmount();
    }
}