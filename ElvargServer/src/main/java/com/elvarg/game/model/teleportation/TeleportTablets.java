package com.elvarg.game.model.teleportation;

import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.Item;
import com.elvarg.game.model.Location;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;

public class TeleportTablets {

    public static boolean init(Player player, int itemId) {
        Optional<TeleportTablet> tab = TeleportTablet.getTab(itemId);

        // Checks if the tab isn't present, if not perform nothing
        if (!tab.isPresent()) {
            return false;
        }

        //Handle present tab..
        if (player.getInventory().contains(tab.get().getTab())) {
            if (TeleportHandler.checkReqs(player, tab.get().getPosition())) {
                TeleportHandler.teleport(player, tab.get().getPosition(), TeleportType.TELE_TAB, false);
                player.getInventory().delete(tab.get().getTab(), 1);
            }
        }

        return true;
    }

    /**
     * Teleport Tablet data storage.
     *
     * @author Dennis
     */
    public enum TeleportTablet {
        HOME(1, new Location(3222, 3222, 0)),
        LUMBRIDGE(8008, new Location(3222, 3218, 0)),
        FALADOR(8009, new Location(2965, 3379, 0)),
        CAMELOT(8010, new Location(2757, 3477, 0)),
        ARDY(8011, new Location(2661, 3305, 0)),
        WATCH(8012, new Location(2549, 3112, 0)),
        VARROCK(8007, new Location(3213, 3424, 0));

        /**
         * The {@value #tab_set} storing
         */
        private static Set<TeleportTablet> tab_set = Collections.unmodifiableSet(EnumSet.allOf(TeleportTablet.class));
        /**
         * The {@link Item} id of the teleport tablet.
         */
        private final int tabId;
        /**
         * The specified {@link Location} that the teleport tablet will send the {@link Player} upon interaction.
         */
        private final Location position;

        /**
         * TabData constructor
         *
         * @param tabId
         * @param position
         */
        private TeleportTablet(int tabId, Location position) {
            this.tabId = tabId;
            this.position = position;
        }

        /**
         * Gets the teleport tablet from the {@value #tab_set} stream.
         *
         * @param tabId
         * @return tabId
         */
        public static Optional<TeleportTablet> getTab(int tabId) {
            return tab_set.stream().filter(tabs -> tabs.getTab() == tabId).findFirst();
        }

        /**
         * Gets the {@link #tabId} and returns as its initial value.
         *
         * @return tabId
         */
        public int getTab() {
            return tabId;
        }

        /**
         * Gets the {@link #tabId} and returns as its initial value.
         *
         * @return position
         */
        public Location getPosition() {
            return position;
        }
    }
}
