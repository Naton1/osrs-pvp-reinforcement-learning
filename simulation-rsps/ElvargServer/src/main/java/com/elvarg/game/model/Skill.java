package com.elvarg.game.model;

import com.elvarg.util.Misc;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import java.util.HashMap;
import java.util.Map;

/**
 * This enum represents a skill in the game.
 * Every skill should be added with its
 * proper chatbox level up interface.
 *
 * @author Professor Oak
 */
public enum Skill {

    ATTACK(6247, 8654),
    DEFENCE(6253, 8660),
    STRENGTH(6206, 8657),
    HITPOINTS(6216, 8655),
    RANGED(4443, 8663),
    PRAYER(6242, 8666),
    MAGIC(6211, 8669),
    COOKING(6226, 8665),
    WOODCUTTING(4272, 8671),
    FLETCHING(6231, 8670),
    FISHING(6258, 8662),
    FIREMAKING(4282, 8668),
    CRAFTING(6263, 8667),
    SMITHING(6221, 8659),
    MINING(4416, 8656),
    HERBLORE(6237, 8661),
    AGILITY(4277, 8658),
    THIEVING(4261, 8664),
    SLAYER(12122, 12162),
    FARMING(5267, 13928),
    RUNECRAFTING(4267, 8672),
    CONSTRUCTION(7267, 18801),
    HUNTER(8267, 18829);

    /**
     * The {@link ImmutableSet} which represents the skills that a player can set to a desired level.
     */
    private static final ImmutableSet<Skill> ALLOWED_TO_SET_LEVLES = Sets.immutableEnumSet(ATTACK, DEFENCE, STRENGTH, HITPOINTS, RANGED, PRAYER, MAGIC);
    private static Map<Integer, Skill> skillMap = new HashMap<Integer, Skill>();

    static {
        for (Skill skill : Skill.values()) {
            skillMap.put(skill.button, skill);
        }
    }

    /**
     * The {@link Skill}'s chatbox interface
     * The interface which will be sent
     * on levelup.
     */
    private final int chatboxInterface;
    /**
     * The {@link Skill}'s button in the skills tab
     * interface.
     */
    private final int button;

    /**
     * Constructor
     *
     * @param chatboxInterface
     * @param button
     */
    private Skill(int chatboxInterface, int button) {
        this.chatboxInterface = chatboxInterface;
        this.button = button;
    }

    /**
     * Gets a skill for its button id.
     *
     * @param button The button id.
     * @return The skill with the matching button.
     */
    public static Skill forButton(int button) {
        return skillMap.get(button);
    }

    /**
     * Checks if a skill can be manually set to a level by a player.
     *
     * @return true if the player can set their level in this skill, false otherwise.
     */
    public boolean canSetLevel() {
        return ALLOWED_TO_SET_LEVLES.contains(this);
    }

    /**
     * Gets the {@link Skill}'s chatbox interface.
     *
     * @return The interface which will be sent on levelup.
     */
    public int getChatboxInterface() {
        return chatboxInterface;
    }

    /**
     * Gets the {@link Skill}'s button id.
     *
     * @return The button for this skill.
     */
    public int getButton() {
        return button;
    }

    /**
     * Gets the {@link Skill}'s name.
     *
     * @return The {@link Skill}'s name in a suitable format.
     */
    public String getName() {
        return Misc.formatText(toString().toLowerCase());
    }
}