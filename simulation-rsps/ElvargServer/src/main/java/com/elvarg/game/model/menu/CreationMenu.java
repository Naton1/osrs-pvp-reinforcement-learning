package com.elvarg.game.model.menu;

import java.util.List;

/**
 * A parent class used to handle the creation menu
 * chatbox interface. In the interface, an item is
 * displayed and the player can choose the amount
 * to "create".
 *
 * @author Professor Oak
 */
public class CreationMenu {
    
    /**
     * The title of this {@link CreationMenu}.
     */
    private final String title;
    
    /**
     * The items which can be created through this
     * {@link CreationMenu}.
     */
    private final List<Integer> items;
    
    /**
     * The {@link CreationMenuAction} which will be executed when the player has
     * selected an item and the amount to create.
     */
    private final CreationMenuAction action;

    /**
     * Creates a new {@link CreationMenu}.
     *
     * @param player The owner.
     * @param title  The title.
     * @param action The action to execute upon selecting amount.
     */
    public CreationMenu(String title, List<Integer> items, CreationMenuAction action) {
        this.title = title;
        this.items = items;
        this.action = action;
    }
    
    /**
     * Executes the action.
     * @param itemId
     * @param amount
     */
    public void execute(int itemId, int amount) {
        if (!items.contains(itemId)) {
            return;
        }
        action.execute(itemId, amount);
    }

    /**
     * Gets the title.
     *
     * @return
     */
    public String getTitle() {
        return title;
    }
    
    /**
     * Gets the items.
     * @return
     */    
    public List<Integer> getItems() {
        return items;
    }

    /**
     * Gets the action.
     *
     * @return
     */
    public CreationMenuAction getAction() {
        return action;
    }

    /**
     * Represents a CreationMenu action.
     *
     * @author Professor Oak
     */
    public interface CreationMenuAction {

        /**
         * This method will execute when a player clicks
         * on an item in the creation menu chatbox
         * interface.
         *
         * @param item   The item clicked on.
         * @param amount The amount selected.
         */
        public abstract void execute(int item, int amount);
    }
}