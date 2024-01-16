package com.elvarg.game.content.skill.skillable.impl;

import com.elvarg.game.definition.ItemDefinition;
import com.elvarg.game.entity.impl.object.GameObject;
import com.elvarg.game.entity.impl.object.ObjectManager;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.*;
import com.elvarg.util.Misc;
import com.elvarg.util.ObjectIdentifiers;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Represents the Cooking skill.
 *
 * @author Professor Oak
 */
public class Cooking extends ItemCreationSkillable {

    /**
     * The animation the player will perform
     */
    protected static final Animation ANIMATION = new Animation(896);

    /**
     * The object we're cooking on.
     */
    private final GameObject object;

    /**
     * The {@link Cookable} we're going to cook.
     */
    private final Cookable cookable;

    public Cooking(GameObject object, Cookable cookable, int amount) {
        super(Arrays.asList(new RequiredItem(new Item(cookable.getRawItem()), true)),
                new Item(cookable.getCookedItem()), amount, Optional.of(new AnimationLoop(ANIMATION, 4)),
                cookable.getLevelReq(), cookable.getXp(), Skill.COOKING);
        this.object = object;
        this.cookable = cookable;
    }

    /**
     * Get's the rate for burning or successfully cooking food.
     *
     * @param player
     *            Player cooking.
     * @param food
     *            Consumables's enum.
     * @return Successfully cook food.
     */
    public static boolean success(Player player, int burnBonus, int levelReq, int stopBurn) {
        if (player.getSkillManager().getCurrentLevel(Skill.COOKING) >= stopBurn) {
            return true;
        }
        double burn_chance = (45.0 - burnBonus);
        double cook_level = player.getSkillManager().getCurrentLevel(Skill.COOKING);
        double lev_needed = (double) levelReq;
        double burn_stop = (double) stopBurn;
        double multi_a = (burn_stop - lev_needed);
        double burn_dec = (burn_chance / multi_a);
        double multi_b = (cook_level - lev_needed);
        burn_chance -= (multi_b * burn_dec);
        double randNum = Misc.getRandomDouble() * 100.0;
        return burn_chance <= randNum;
    }

    // Override finishedCycle because we don't always add the cooked item.
    // Sometimes we burn food.
    @Override
    public void finishedCycle(Player player) {
        // Decrement amount left to cook..
        decrementAmount();

        // Delete raw food..
        player.getInventory().delete(cookable.getRawItem(), 1);

        // Add burnt or cooked item..
        if (success(player, 3, cookable.getLevelReq(), cookable.getStopBurn())) {
            player.getInventory().add(cookable.getCookedItem(), 1);
            player.getPacketSender()
                    .sendMessage("You cook the " + ItemDefinition.forId(cookable.getRawItem()).getName() + ".");
            player.getSkillManager().addExperience(Skill.COOKING, cookable.getXp());
        } else {
            player.getInventory().add(cookable.getBurntItem(), 1);
            player.getPacketSender()
                    .sendMessage("You burn the " + ItemDefinition.forId(cookable.getRawItem()).getName() + ".");
        }
    }

    @Override
    public boolean hasRequirements(Player player) {
        // If we're using a fire, make sure to check it's still there.
        if (object.getId() == ObjectIdentifiers.FIRE_5
                && !ObjectManager.exists(ObjectIdentifiers.FIRE_5, object.getLocation())) {
            return false;
        }

        return super.hasRequirements(player);
    }

    /**
     * Data for the cooking skill.
     */
    public static enum Cookable {
        SHRIMP(317, 315, 7954, 1, 30, 33, "shrimp"),
        ANCHOVIES(321, 319, 323, 1, 30, 34, "anchovies"),
        TROUT(335, 333, 343, 15, 70, 50, "trout"),
        COD(341, 339, 343, 18, 75, 54, "cod"),
        SALMON(331, 329, 343, 25, 90, 58, "salmon"),
        TUNA(359, 361, 367, 30, 100, 58, "tuna"),
        LOBSTER(377, 379, 381, 40, 120, 74, "lobster"),
        BASS(363, 365, 367, 40, 130, 75, "bass"),
        SWORDFISH(371, 373, 375, 45, 140, 86, "swordfish"),
        MONKFISH(7944, 7946, 7948, 62, 150, 91, "monkfish"),
        SHARK(383, 385, 387, 80, 210, 94, "shark"),
        SEA_TURTLE(395, 397, 399, 82, 212, 105, "sea turtle"),
        MANTA_RAY(389, 391, 393, 91, 217, 99, "manta ray"),;

        private static final Map<Integer, Cookable> cookables = new HashMap<Integer, Cookable>();

        static {
            for (Cookable c : Cookable.values()) {
                cookables.put(c.getRawItem(), c);
                cookables.put(c.getCookedItem(), c);
            }
        }

        int rawItem, cookedItem, burntItem, levelReq, xp, stopBurn;
        String name;

        Cookable(int rawItem, int cookedItem, int burntItem, int levelReq, int xp, int stopBurn, String name) {
            this.rawItem = rawItem;
            this.cookedItem = cookedItem;
            this.burntItem = burntItem;
            this.levelReq = levelReq;
            this.xp = xp;
            this.stopBurn = stopBurn;
            this.name = name;
        }

        public static Optional<Cookable> getForItem(int item) {
        	return Optional.ofNullable(cookables.get(item));
        }

        public int getRawItem() {
            return rawItem;
        }

        public int getCookedItem() {
            return cookedItem;
        }

        public int getBurntItem() {
            return burntItem;
        }

        public int getLevelReq() {
            return levelReq;
        }

        public int getXp() {
            return xp;
        }

        public int getStopBurn() {
            return stopBurn;
        }

        public String getName() {
            return name;
        }
    }
}
