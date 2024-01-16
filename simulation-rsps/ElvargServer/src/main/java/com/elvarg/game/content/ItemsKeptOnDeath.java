package com.elvarg.game.content;

import com.elvarg.game.content.combat.bountyhunter.Emblem;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.Item;
import com.elvarg.game.model.SkullType;
import com.elvarg.util.Misc;
import com.google.common.collect.Lists;

import java.util.*;

/**
 * Handles items kept on death.
 *
 * @author Swiffy
 */
public class ItemsKeptOnDeath {

    /**
     * Sends the items kept on death interface for a player.
     *
     * @param player Player to send the items kept on death interface for.
     */
    public static void open(Player player) {
        clearInterfaceData(player); //To prevent sending multiple layers of items.
        sendInterfaceData(player); //Send info on the interface.
        player.getPacketSender().sendInterface(17100); //Open the interface.
    }

    /**
     * Sends the items kept on death data for a player.
     *
     * @param player Player to send the items kept on death data for.
     */
    public static void sendInterfaceData(Player player) {

        player.getPacketSender().sendString(17107, "" + getAmountToKeep(player));

        List<Item> toKeep = getItemsToKeep(player);
        for (int i = 0; i < toKeep.size(); i++) {
            player.getPacketSender().sendItemOnInterface(17108 + i, toKeep.get(i).getId(), 0, 1);
        }

        int toSend = 17112;
        for (Item item : Misc.concat(player.getInventory().getItems(), player.getEquipment().getItems())) {
            if (item == null || item.getId() <= 0 || item.getAmount() <= 0 || !item.getDefinition().isTradeable() || toKeep.contains(item)) {
                continue;
            }
            player.getPacketSender().sendItemOnInterface(toSend, item.getId(), 0, item.getAmount());
            toSend++;
        }
    }

    /**
     * Clears the items kept on death interface for a player.
     *
     * @param player Player to clear the items kept on death interface for.
     */
    public static void clearInterfaceData(Player player) {
        for (int i = 17108; i <= 17152; i++)
            player.getPacketSender().clearItemOnInterface(i);
    }

    /**
     * Sets the items to keep on death for a player.
     *
     * @param player Player to set items for.
     */
    public static List<Item> getItemsToKeep(Player player) {
        List<Item> items = Lists.newArrayList();
        for (Item item : Misc.concat(player.getInventory().getItems(), player.getEquipment().getItems())) {
            if (item == null || item.getId() <= 0 || item.getAmount() <= 0 || !item.getDefinition().isTradeable()) {
                continue;
            }

            //Dont keep emblems
            if (Arrays.stream(Emblem.values()).anyMatch(i -> i.id == item.getId()))
                continue;

            items.add(item);
        }
        Collections.sort(items, (item, item2) -> {
            int value1 = item.getDefinition().getValue();
            int value2 = item2.getDefinition().getValue();
            if (value1 == value2) {
                return 0;
            } else if (value1 > value2) {
                return -1;
            } else {
                return 1;
            }
        });
        List<Item> toKeep = Lists.newArrayList();
        int amountToKeep = getAmountToKeep(player);
        for (int i = 0; i < amountToKeep && i < items.size(); i++) {
            toKeep.add(items.get(i));
        }
        return toKeep;
    }

    public static int getAmountToKeep(Player player) {
        if (player.getSkullTimer() > 0) {
            if (player.getSkullType() == SkullType.RED_SKULL) {
                return 0;
            }
        }
        return (player.getSkullTimer() > 0 ? 0 : 3) + (PrayerHandler.isActivated(player, PrayerHandler.PROTECT_ITEM) ? 1 : 0);
    }
}
