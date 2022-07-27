package com.elvarg.game.model;

import com.elvarg.game.definition.ItemDefinition;
import com.elvarg.game.model.container.impl.Inventory;

import java.util.stream.IntStream;

/**
 * Represents an item and its slot in an inventory.
 *
 * @author Tobias
 */
public class ItemInSlot {

    /**
     * The item id.
     */
    private int id;

    /**
     * The inventory slot index of the item (0-27).
     */
    private int slot;

    /**
     * An ItemInSlot object constructor.
     *
     * @param id     Item id.
     * @param slot Item slot.
     */
    public ItemInSlot(int id, int slot) {
        this.id = id;
        this.slot = slot;
    }

    public ItemInSlot(int id, Inventory inventory) {
        this.id = id;

        // Search player's inventory for this.id
        int[] itemIds = inventory.getItemIdsArray();

        this.slot = IntStream.range(0, itemIds.length)
                .filter(i -> id == itemIds[i])
                .findFirst()
                .orElse(-1);
    }

    /**
     * Gets the item's id.
     */
    public int getId() {
        return id;
    }

    /**
     * Gets the item slot.
     */
    public int getSlot() {
        return slot;
    }
}