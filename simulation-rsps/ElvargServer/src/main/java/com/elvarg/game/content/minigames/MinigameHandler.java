package com.elvarg.game.content.minigames;

import com.elvarg.game.content.minigames.impl.CastleWars;
import com.elvarg.game.content.minigames.impl.pestcontrol.PestControl;
import com.elvarg.game.content.minigames.impl.pestcontrol.PestControlBoat;
import com.elvarg.game.entity.impl.object.GameObject;
import com.elvarg.game.entity.impl.player.Player;

import java.util.Arrays;
import java.util.stream.Stream;

public class MinigameHandler {

    public enum Minigames {
        CASTLEWARS("Castlewars", new CastleWars()),
        PEST_CONTROL("Pest Control", new PestControl(PestControlBoat.NOVICE));

        private final String name;
        private final Minigame minigame;

        Minigames(final String name, final Minigame minigame) {
            this.name = name;
            this.minigame = minigame;
        }

        /**
         * Gets a Stream of all Minigames, pre-filtered and nullchecked.
         * @return
         */
        public static Stream<Minigames> getAll() {
            return Arrays.stream(Minigames.values()).filter(m -> m.minigame != null);
        }

        /**
         * Return the actual Minigame instance.
         *
         * @return
         */
        public Minigame get() {
            return this.minigame;
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
        return Minigames.getAll().anyMatch(m -> m.minigame.firstClickObject(player, object));
    }

    /**
     * Handle clicking buttons in all minigames.
     *
     * @param player
     * @param button
     * @return
     */
    public static boolean handleButtonClick(Player player, int button) {
        return Minigames.getAll().anyMatch(m -> m.minigame.handleButtonClick(player, button));
    }

    /**
     * Runs the process method for every active minigame.
     */
    public static void process() {
        Minigames.getAll().forEach(m -> m.minigame.process());
    }

    public static void init() {
        Minigames.getAll().forEach(m -> m.minigame.init());
    }
}
