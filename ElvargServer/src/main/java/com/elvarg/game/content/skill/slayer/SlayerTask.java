package com.elvarg.game.content.skill.slayer;

import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.util.Misc;

/**
 * Represents all of the possible Slayer tasks a player can be assigned.
 * 
 * @author Professor Oak
 */
public enum SlayerTask {
    BANSHEES("in the Slayer Tower", 15, 50, 15, 8, new SlayerMaster[] { SlayerMaster.TURAEL, SlayerMaster.MAZCHNA },
            new String[] { "banshee", "twisted banshee" }),
    BATS("in the Taverly Dungeon", 15, 50, 1, 7, new SlayerMaster[] { SlayerMaster.TURAEL, SlayerMaster.MAZCHNA },
            new String[] { "bat", "giant bat" }),
    CHICKENS("in Lumbridge", 15, 50, 1, 6, new SlayerMaster[] { SlayerMaster.TURAEL },
            new String[] { "chicken", "mounted terrorbird gnome", "terrorbird", "rooster", }),
    BEARS("outside Varrock", 15, 50, 1, 7, new SlayerMaster[] { SlayerMaster.TURAEL, SlayerMaster.MAZCHNA },
            new String[] { "black bear", "grizzly bear", "grizzly bear cub", "bear cub", "callisto" }),
    CAVE_BUGS("Lumbridge dungeon", 10, 20, 7, 8, new SlayerMaster[] { SlayerMaster.TURAEL, SlayerMaster.MAZCHNA },
            new String[] { "cave bug" }),
    CAVE_CRAWLERS("Lumbridge dungeon", 15, 50, 10, 8, new SlayerMaster[] { SlayerMaster.TURAEL, SlayerMaster.MAZCHNA },
            new String[] { "cave crawler" }),    
    CAVE_SLIME("Lumbridge dungeon", 10, 20, 17, 8, new SlayerMaster[] { SlayerMaster.TURAEL, SlayerMaster.MAZCHNA },
            new String[] { "cave slime" }),    
    COWS("Lumbridge", 15, 50, 1, 8, new SlayerMaster[] { SlayerMaster.TURAEL },
            new String[] { "cow", "cow calf" }),
    CRAWLING_HANDS("in the Slayer Tower", 15, 50, 5, 8, new SlayerMaster[] { SlayerMaster.TURAEL, SlayerMaster.MAZCHNA },
            new String[] { "crawling hand" }),
    DESERT_LIZARDS("in the desert", 15, 50, 22, 8, new SlayerMaster[] { SlayerMaster.TURAEL, SlayerMaster.MAZCHNA },
            new String[] { "lizard", "small lizard", "desert lizard" }),
    DOGS("", 15, 50, 1, 7, new SlayerMaster[] { SlayerMaster.TURAEL, SlayerMaster.MAZCHNA },
            new String[] { "dog", "jackal", "guard dog", "wild dog" }),
    DWARVES("", 15, 50, 1, 7, new SlayerMaster[] { SlayerMaster.TURAEL }, new String[] {"dwarf", "dwarf gang member", "chaos dwarf"}),
    GHOSTS("", 15, 50, 1, 7, new SlayerMaster[] { SlayerMaster.TURAEL, SlayerMaster.MAZCHNA },
            new String[] { "ghost", "tortured soul" }),
    GOBLINS("", 15, 50, 1, 7, new SlayerMaster[] { SlayerMaster.TURAEL },
            new String[] { "goblin", "cave goblin guard" }),
    ICEFIENDS("", 15, 50, 1, 8, new SlayerMaster[] { SlayerMaster.TURAEL }, new String[] { "icefiend" }),
    KALPHITES("", 15, 50, 1, 6, new SlayerMaster[] { SlayerMaster.TURAEL, SlayerMaster.MAZCHNA },
            new String[] { "kalphite worker", "kalphite soldier", "kalphite guardian", "kalphite queen" }),
    MINOTAURS("", 10, 20, 1, 7, new SlayerMaster[] { SlayerMaster.TURAEL }, new String[] { "minotaur" }),
    MONKEYS("", 10, 20, 1, 7, new SlayerMaster[] { SlayerMaster.TURAEL },
            new String[] { "monkey", "karmjan monkey", "monkey guard", "monkey archer", "zombie monkey" }),
    RATS("", 15, 50, 1, 7, new SlayerMaster[] { SlayerMaster.TURAEL },
            new String[] { "rat", "giant rat", "dungeon rat", "brine rat" }),
    SCORPIONS("", 15, 50, 1, 7, new SlayerMaster[] { SlayerMaster.TURAEL, SlayerMaster.MAZCHNA },
            new String[] { "scorpion", "king scorpion", "poison scorpion", "pit scorpion", "scorpia" }),
    SKELETONS("", 15, 50, 1, 7, new SlayerMaster[] { SlayerMaster.TURAEL, SlayerMaster.MAZCHNA },
            new String[] { "skeleton", "skeleton mage", "vet'ion" }),
    SPIDERS("", 15, 50, 1, 6, new SlayerMaster[] { SlayerMaster.TURAEL },
            new String[] { "spider", "giant spider", "shadow spider", "giant crypt spider", "venenatis" }),
    WOLVES("", 15, 50, 1, 7, new SlayerMaster[] { SlayerMaster.TURAEL, SlayerMaster.MAZCHNA },
            new String[] { "wolf", "white wolf", "big wolf" }),
    ZOMBIES("", 15, 50, 1, 7, new SlayerMaster[] { SlayerMaster.TURAEL, SlayerMaster.MAZCHNA }, new String[] { "zombie", "undead one" }),
    CATABLEPONS("", 15, 50, 1, 8, new SlayerMaster[] { SlayerMaster.MAZCHNA }, new String[] { "catablepon" }),
    COCKATRICES("", 15, 50, 25, 8, new SlayerMaster[] { SlayerMaster.MAZCHNA }, new String[] { "cockatrice" }),
    EARTH_WARRIORS("", 15, 50, 1, 6, new SlayerMaster[] { SlayerMaster.MAZCHNA }, new String[] { "earth warrior" }),
    FLESH_CRAWLERS("", 15, 50, 1, 7, new SlayerMaster[] { SlayerMaster.MAZCHNA }, new String[] { "flesh crawler" }),
    GHOULS("", 15, 50, 1, 7, new SlayerMaster[] { SlayerMaster.MAZCHNA }, new String[] { "ghoul" }),
    HILL_GIANTS("", 15, 50, 1, 7, new SlayerMaster[] { SlayerMaster.MAZCHNA }, new String[] { "hill giant" }),
    HOBGOBLINS("", 15, 50, 1, 7, new SlayerMaster[] { SlayerMaster.MAZCHNA }, new String[] { "hob goblin" }),
    
    ROCKSLUGS("", 15, 50, 1, 8, new SlayerMaster[] { SlayerMaster.MAZCHNA }, new String[] { "rockslug" }),
    ;

    private final String hint;
    private final int minimumAmount;
    private final int maximumAmount;
    private final int slayerLevel;
    private final int weight;
    private final SlayerMaster[] masters;
    private final String[] npcNames;

    SlayerTask(String hint, int minimumAmount, int maximumAmount, int slayerLevel, int weight,
            SlayerMaster[] masters, String[] npcNames) {
        this.hint = hint;
        this.minimumAmount = minimumAmount;
        this.maximumAmount = maximumAmount;
        this.slayerLevel = slayerLevel;
        this.weight = weight;
        this.masters = masters;
        this.npcNames = npcNames;
    }

    public String getHint() {
        return hint;
    }

    public int getMinimumAmount() {
        return minimumAmount;
    }

    public int getMaximumAmount() {
        return maximumAmount;
    }

    public int getSlayerLevel() {
        return slayerLevel;
    }

    public int getWeight() {
        return weight;
    }

    public SlayerMaster[] getMasters() {
        return masters;
    }

    public String[] getNpcNames() {
        return npcNames;
    }
    
    @Override
    public String toString() {
        return Misc.ucFirst(name().toLowerCase().replaceAll("_", ""));
    }

    public boolean isUnlocked(Player player) {
        return true;
    }

    public static final SlayerTask[] VALUES = values();
}
