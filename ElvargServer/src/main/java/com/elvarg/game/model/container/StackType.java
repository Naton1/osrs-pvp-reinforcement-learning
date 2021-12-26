package com.elvarg.game.model.container;

/**
 * Represents an ItemContainer's stack type,
 *
 * @author relex lawl
 */

public enum StackType {
    /*
     * Default type, items that will not stack, such as inventory items (excluding noted/stackable items).
     */
    DEFAULT,
    /*
     * Stacks type, items that will stack, such as shops or banks.
     */
    STACKS,
}