package com.elvarg.game.model.container.impl;

import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.container.ItemContainer;
import com.elvarg.game.model.container.StackType;

/**
 * Represents a player's inventory item container.
 *
 * @author relex lawl
 */

public class Inventory extends ItemContainer {

    public static final int INTERFACE_ID = 3214;

    /**
     * The Inventory constructor.
     *
     * @param player The player who's inventory is being represented.
     */
    public Inventory(Player player) {
        super(player);
    }

    @Override
    public int capacity() {
        return 28;
    }

    @Override
    public StackType stackType() {
        return StackType.DEFAULT;
    }

    @Override
    public Inventory refreshItems() {
        getPlayer().getPacketSender().sendItemContainer(this, INTERFACE_ID);
        return this;
    }

    @Override
    public Inventory full() {
        getPlayer().getPacketSender().sendMessage("Not enough space in your inventory.");
        return this;
    }
}
