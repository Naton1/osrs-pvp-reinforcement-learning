package com.elvarg.game;

public enum Sound {

    // crafting sounds

    CUTTING(375, 1, 0),

    // cooking sounds

    COOKING_COOK(1039, 1, 10),

    COOKING_BURN(240, 1, 0),


    // runecrafting sounds

    CRAFT_RUNES(207, 0, 0),


    // mining sounds

    MINING_MINE(432, 1, 15),

    MINING_ROCK_GONE(431, 1, 0),

    MINING_ROCK_RESTORE(463, 1, 0),

    MINING_ROCK_EXPLODE(1021, 1, 0),


    // fishing sounds

    FISHING_FISH(379, 1, 10),


    // woodcutting sounds

    WOODCUTTING_CHOP(472, 1, 10),

    WOODCUTTING_TREE_DOWN(473, 1, 0),

    // Getting hit
    FEMALE_GETTING_HIT(818, 1, 25),

    // weapon sounds

    IMP_ATTACKING(10, 1, 25),

    SHOOT_ARROW(370),

    WEAPON(398, 1, 25), // default/other

    WEAPON_GODSWORD(390, 1, 25),

    WEAPON_STAFF(394, 1, 25),

    WEAPON_BOW(370, 1, 25),

    WEAPON_BATTLE_AXE(399, 1, 25),

    WEAPON_TWO_HANDER(400, 1, 25),

    WEAPON_SCIMITAR(396, 1, 25),

    WEAPON_WHIP(1080, 1, 25),

    // Special attack
    DRAGON_DAGGER_SPECIAL(385, 1, 25),

    // Spell sounds

    SPELL_FAIL_SPLASH(193),

    TELEPORT(202),

    ICA_BARRAGE_IMPACT(1125, 1, 0),

    DROP_ITEM(376, 1, 0),
    PICK_UP_ITEM(358, 1, 0),

    SET_UP_BARRICADE(358, 1, 0),

    FIRE_LIGHT(375, 1, 0),
    FIRE_SUCCESSFUL(608, 1, 0),
    FIRE_FIRST_ATTEMPT(2584, 1, 0),
    // Webs
    SLASH_WEB(2548, 1, 0),
    FOOD_EAT(317),
    DRINK(334),
    ;

    private final int id;
    private final int volume;
    private final int delay;

    private final int loopType;

    Sound(int id, int volume, int delay, int loopType) {
        this.id = id;
        this.volume = volume;
        this.delay = delay;
        this.loopType = loopType;
    }

    Sound(int id, int volume, int delay) {
        this(id, volume, delay, 0);
    }

    Sound(int id) {
        this(id, 1, 0, 0);
    }

    public int getId() {
        return id;
    }

    public int getVolume() {
        return volume;
    }

    public int getDelay() {
        return delay;
    }

    public int getLoopType() { return loopType; }
}

/*
https://www.rune-server.ee/runescape-development/rs2-server/configuration/97570-all-my-found-sounds-list-1000-sounds.html
 */
