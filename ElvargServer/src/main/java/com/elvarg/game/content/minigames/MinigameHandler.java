package com.elvarg.game.content.minigames;

import com.elvarg.game.content.minigames.impl.CastleWars;
import com.elvarg.game.entity.impl.object.GameObject;
import com.elvarg.game.entity.impl.player.Player;

public class MinigameHandler {

    public enum Minigames {
        CASTLEWARS("Castlewars", new CastleWars());

        private final String name;
        private final Minigame minigame;

        private Minigames(final String name, final Minigame minigame) {
            this.name = name;
            this.minigame = minigame;
        }

    }

    /**
     * Handle clicking objects in all Minigames.
     *
     * @param player
     * @param object
     * @return
     */
    public static boolean firstClickObject(Player player, GameObject object) {
        for (MinigameHandler.Minigames minigameRecord : MinigameHandler.Minigames.values()) {
            if (minigameRecord.minigame.firstClickObject(player, object)) {
                return true;
            }
        }

        // Return false if no Minigame handled this Object click
        return false;
    }

    /**
     * Handle clicking buttons in all minigames.
     *
     * @param player
     * @param button
     * @return
     */
    public static boolean handleButtonClick(Player player, int button) {
        for (MinigameHandler.Minigames minigameRecord : MinigameHandler.Minigames.values()) {
            if (minigameRecord.minigame.handleButtonClick(player, button)) {
                return true;
            }
        }

        // Return false if no Minigame handled this Button click
        return false;
    }

    public static void process() {
        for (MinigameHandler.Minigames minigameRecord : MinigameHandler.Minigames.values()) {
            minigameRecord.minigame.process();
        }
    }
}
