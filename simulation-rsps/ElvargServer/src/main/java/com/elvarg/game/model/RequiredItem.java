package com.elvarg.game.model;

/**
 * Represents a required item. Used when skilling.
 *
 * @author Professor Oak
 */
public class RequiredItem {

    /**
     * The {@link Item}.
     */
    private final Item item;

    /**
     * Should this item be deleted eventually?
     */
    private final boolean delete;

    public RequiredItem(Item item, boolean delete) {
        this.item = item;
        this.delete = delete;
    }

    public RequiredItem(Item item) {
        this.item = item;
        this.delete = false;
    }

    public Item getItem() {
        return item;
    }

    public boolean isDelete() {
        return delete;
    }
}
