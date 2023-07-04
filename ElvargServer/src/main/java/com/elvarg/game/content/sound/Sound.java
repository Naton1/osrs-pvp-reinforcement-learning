package com.elvarg.game.content.sound;

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

    // default/other

    WEAPON(398, 1, 25),

    WEAPON_GODSWORD(390, 1, 25),

    WEAPON_STAFF(394, 1, 25),

    WEAPON_BOW(370, 1, 25),

    WEAPON_BATTLE_AXE(399, 1, 25),

    WEAPON_TWO_HANDER(400, 1, 25),

    WEAPON_SCIMITAR(396, 1, 25),

    WEAPON_WHIP(1080, 1, 25),

    // Special attack

    DRAGON_DAGGER_SPECIAL(385, 1, 25),
    
    DRAGON_SPEAR_SPECIAL(2544),

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

    SLASH_WEB(237, 1, 0),

    FAIL_SLASH_WEB(2548, 1, 0),

    FOOD_EAT(317),

    DRINK(334),

    // Skilling sounds

    COOK_ITEM(357, 1, 0),

    MINING_ORE(1331, 1, 0),

    ANVIL(468, 1, 0),

    BONE_BURY(380, 1, 0),

    FIRST_ATTEMPT(2584, 1, 0),

    TREE_CUT_BEGIN(471, 1, 0),

    TREE_CUTTING(472, 1, 0),

    TREE_EMPTY(473, 1, 0),

    // Cast out Net/Cage/Harpoon

    FISHING(289, 1, 0),

    START_FLY_FISHING(377, 1, 0),

    RUNECRAFTING(481, 1, 0),

    // farming

    RAKE_WEEDS(1323, 1, 0),

    // smelting ore in furnace

    SMELTING_ORE(469, 1, 0),

    SMITHING_ANVIL(468, 1, 0),

    // stunned from thieving

    STUNNED(458, 1, 0),

    PESTLE_MOTAR(373, 1, 0),

    PROSPECTING(431, 1, 0),

    CUT_GEM(464, 1, 0),

    //Skill Level Up Sounds
    //Each are different lengths, so a songDelay in the sendQuickSong method would have to be changed per skill sound
    //Some of these are level up sounds from 50-99, while some are only heard when unlocking certain items/armour
    //Some might not be level up sounds

    LEVEL_UP_RUNECRAFTING_UNKNOWN(66, 1, 0),

    LEVEL_UP_UNKNOWN(67, 1, 0),

    LEVEL_UP_COOKING(68, 1, 0),

    LEVEL_UP_UNKNOWN2(69, 1, 0),

    LEVEL_UP_FIREMAKING(71, 1, 0),

    LEVEL_UP_RUNECRAFTING(72, 1, 0),

    LEVEL_UP_THIEVING(73, 1, 0),

    LEVEL_UP_RANGED(74, 1, 0),

    LEVEL_UP_FLETCHING(77, 1, 0),

    LEVEL_UP_SMITHING(78, 1, 0),

    LEVEL_UP_PRAYER2(79, 1, 0),

    LEVEL_UP_UNKNOWN5(80, 1, 0),

    LEVEL_UP_UNKNOWN6(82, 1, 0),

    LEVEL_UP_PRAYER(83, 1, 0),

    LEVEL_UP_ATTACK_UNKNOWN(84, 1, 0),

    LEVEL_UP_FISHING(85, 1, 0),

    LEVEL_UP_MAGIC_UNKNOWN(86, 1, 0),

    LEVEL_UP_MAGIC(87, 1, 0),

    LEVEL_UP_DEFENCE(88, 1, 0),

    LEVEL_UP_HERBLORE(90, 1, 0),

    LEVEL_UP_ATTACK(91, 1, 0),

    LEVEL_UP_WOODCUTTING(92, 1, 0),

    LEVEL_UP_UNKNOWN7(94, 1, 0),

    LEVEL_UP_UNKNOWN8(95, 1, 0),

    LEVEL_UP_FLETCHING_UNKNOWN(96, 1, 0),

    LEVEL_UP_FISHING_UNKNOWN(98, 1, 0),

    LEVEL_UP_MINING(99, 1, 0),

    LEVEL_UP_CRAFTING(100, 1, 0),

    LEVEL_UP_UNKNOWN9(101, 1, 0),

    LEVEL_UP_MAGIC2(102, 1, 0),

    LEVEL_UP_UNKNOWN10(103, 1, 0),

    LEVEL_UP_UNKNOWN11(104, 1, 0),

    LEVEL_UP_UNKNOWN12(105, 1, 0),

    LEVEL_UP_STRENGTH(107, 1, 0),

    LEVEL_UP_UNKNOWN13(108, 1, 0),

    LEVEL_UP_UNKNOWN14(109, 1, 0),


    //Death Sound
    PLAYER_DEATH_SOUND(75, 1, 0),

    //Prayer
    // RECHARGE_PRAYER(442, 1, 0),
    // PROTECT_PRAYER(444, 1, 0),

    PROTECT_MELEE(433, 1, 0),

    PROTECT_MAGIC(438, 1, 0),

    PROTECT_RANGE(444, 1, 0),

    PROT_MAGE(438, 1, 0),

    NO_PRAY(435, 1, 0),

    PRAYER_TO_LOW(447, 1, 0),

    //Magic

    MAGE_SPLASH(193, 1, 0),

    MAGE_FAIL(941, 1, 0),

    TELEBLOCK_CAST(1185, 1, 0),

    TELEBLOCK_HIT(1183, 1, 0),

    LOW_ALCHEMY(224, 1, 0),

    HIGH_ALCHEMY(223, 1, 0),

    ICE_BLITZ(1110, 1, 0),

    BLOOD_RUSH(984, 1, 0),

    BLOOD_BITZ(985, 1, 0),

    ANCIENT_BLOOD(986, 1, 0),

    BLOOD_RUSH_SPLASH(991, 1, 0),

    WIND_STRIKE(992, 1, 0),

    SUPERHEAT(217, 1, 0),

    SUPERHEAT_FAIL(218, 1, 0),

    BONES_TO_BANNAS(227, 1, 0),

    //Click item

    ITEM_PICKUP(356, 1, 0),

    ITEM_DROP(376, 1, 0),

    //Random Event

    EXPLODING_ROCK(429, 1, 0),

    EXPLODING_ROCK_2(432, 1, 0),

    KISS_FROG(652, 1, 0),

    //Minigame

    DUEL_WON(77, 1, 0),

    DUEL_LOST(76, 1, 0),

    //Objects

    DITCH(2462, 1, 0),

    JUMPING_STONES(455, 1, 0),

    PICKABLE(358, 1, 0),

    OPEN_DOOR(326, 1, 0),

    OPEN_GATE(1328, 1, 0),

    //Items

    EMPTY(334, 1, 0),
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
