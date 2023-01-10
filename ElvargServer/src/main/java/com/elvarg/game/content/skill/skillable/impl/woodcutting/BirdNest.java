package com.elvarg.game.content.skill.skillable.impl.woodcutting;
import com.elvarg.game.entity.impl.grounditem.ItemOnGroundManager;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.Item;
import com.elvarg.util.Misc;

import static com.elvarg.util.ItemIdentifiers.*;

/**
 *  Handles Bird's nest dropping and searching with OSRS probabilities
 *  @author syuil (Michael)
 */

public class BirdNest {

    private static final int AMOUNT = 1;
    private static final double SEEDS_NEST_CHANCE = 0.64;
    private static final double GOLD_NEST_CHANCE = 0.32;
    public static final int NEST_DROP_CHANCE = 256;

    public enum Nest {
        RED_BIRD_NEST(BIRD_NEST),
        GREEN_BIRD_NEST(BIRD_NEST_2),
        BLUE_BIRD_NEST(BIRD_NEST_3),
        SEEDS_NEST(BIRD_NEST_4),
        GOLD_BIRD_NEST(BIRD_NEST_5),
        EMPTY(BIRD_NEST_6);

        private final int id;

        Nest(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }

        public static Nest getById(int id) {
            for (Nest nest : values()) {
                if (nest.getId() == id) {
                    return nest;
                }
            }
            return null;
        }
    }

    enum Seed {
        ACORN(5312, "acorn"),
        WILLOW(5313, "willow"),
        MAPLE(5314, "maple"),
        YEW(5315, "yew"),
        MAGIC(5316, "magic"),
        SPIRIT(5317, "spirit"),
        APPLE(5283, "apple"),
        BANANA(5284, "banana"),
        ORANGE(5285, "orange"),
        CURRY(5286, "curry"),
        PINEAPPLE(5287, "pineapple"),
        PAPAYA(5288, "papaya"),
        PALM(5289, "palm"),
        CALQUAT(5290, "calquat");

        private final int id;
        private final String name;

        Seed(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }
    }

    public enum Ring {
        GOLD(1635, "gold"),
        SAPPHIRE(1637, "sapphire"),
        EMERALD(1639, "emerald"),
        RUBY(1641, "ruby"),
        DIAMOND(1643, "diamond");

        private final int id;
        private final String name;

        Ring(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }
    }

    /**
     * Handles the random chance of what nest drops
     */
    public static void handleDropNest(Player player) {
        if (player.getLocation().getZ() > 0) {
            return;
        }
        double random = Math.random();
        Nest nest;
        if (random < SEEDS_NEST_CHANCE) {
            nest = Nest.SEEDS_NEST;
        } else if (random < SEEDS_NEST_CHANCE + GOLD_NEST_CHANCE) {
            nest = Nest.GOLD_BIRD_NEST;
        } else {
            int color = Misc.getRandom(2);
            nest = switch (color) {
                case 1 -> Nest.RED_BIRD_NEST;
                case 2 -> Nest.GREEN_BIRD_NEST;
                default -> Nest.BLUE_BIRD_NEST;
            };
        }
        if (nest != null) {
            ItemOnGroundManager.register(player, new Item(nest.getId(), 1));
            player.getPacketSender().sendMessage("@red@A bird's nest falls out of the tree.");
        }
    }

    /**
     * Handles the searching of each nest with a check for inventory space
     */
    public static void handleSearchNest(Player p, int itemId) {
        Nest nest = Nest.getById(itemId);
        if (nest == null) {
            return;
        }
        if (p.getInventory().getFreeSlots() <= 0) {
            p.getPacketSender().sendMessage("Your inventory is too full to take anything out of the bird's nest.");
            return;
        }
        p.getInventory().delete(itemId, 1);
        p.getInventory().add(Nest.EMPTY.getId(), 1);
        if (nest == Nest.GOLD_BIRD_NEST) {
            searchRingNest(p, itemId);
        } else if (nest == Nest.SEEDS_NEST) {
            searchSeedNest(p, itemId);
        } else {
            searchEggNest(p, itemId);
        }
    }

    public static void searchEggNest(Player player, int itemId) {
        int eggId = 0;
        if (itemId == BIRD_NEST) {
            eggId = BIRDS_EGG;
        } else if (itemId == BIRD_NEST_3) {
            eggId = BIRDS_EGG_2;
        } else if (itemId == BIRD_NEST_2) {
            eggId = BIRDS_EGG_3;
        }
        if (eggId != 0) {
            player.getInventory().add(eggId, AMOUNT);
            player.getPacketSender().sendMessage("You take the bird's egg out of the bird's nest.");
        }
    }

    private static void searchSeedNest(Player player, int itemId) {
        if (itemId != BIRD_NEST_4) {
            return;
        }
        int random = Misc.getRandom(1000);
        Seed seed = null;
        if (random <= 220) {
            seed = Seed.ACORN;
        } else if (random <= 350) {
            seed = Seed.WILLOW;
        } else if (random <= 400) {
            seed = Seed.MAPLE;
        } else if (random <= 430) {
            seed = Seed.YEW;
        } else if (random <= 440) {
            seed = Seed.MAGIC;
        } else if (random <= 600) {
            seed = Seed.APPLE;
        } else if (random <= 700) {
            seed = Seed.BANANA;
        } else if (random <= 790) {
            seed = Seed.ORANGE;
        } else if (random <= 850) {
            seed = Seed.CURRY;
        } else if (random <= 900) {
            seed = Seed.PINEAPPLE;
        } else if (random <= 930) {
            seed = Seed.PAPAYA;
        } else if (random <= 960) {
            seed = Seed.PALM;
        } else if (random <= 980) {
            seed = Seed.CALQUAT;
        } else if (random <= 1000) {
            seed = Seed.SPIRIT;
        }
        if (seed != null) {
            player.getInventory().add(seed.getId(), AMOUNT);
            if (seed == Seed.ACORN) {
                player.getPacketSender().sendMessage("You take an " + seed.getName() + " out of the bird's nest.");
            } else if (seed == Seed.APPLE || seed == Seed.ORANGE) {
                player.getPacketSender().sendMessage("You take an " + seed.getName() + " tree seed out of the bird's nest.");
            } else {
                player.getPacketSender().sendMessage("You take a " + seed.getName() + " tree seed out of the bird's nest.");
            }
        }
    }

    public static void searchRingNest(Player player, int itemId) {
        if (itemId != BIRD_NEST_5) {
            return;
        }
        int random = Misc.getRandom(100);
        Ring ring = null;
        if (random <= 35) {
            ring = Ring.GOLD;
        } else if (random <= 75) {
            ring = Ring.SAPPHIRE;
        } else if (random <= 90) {
            ring = Ring.EMERALD;
        } else if (random <= 98) {
            ring = Ring.RUBY;
        } else if (random <= 100) {
            ring = Ring.DIAMOND;
        }
        if (ring != null) {
                player.getInventory().add(ring.getId(), AMOUNT);
                if (ring == Ring.EMERALD) {
                    player.getPacketSender().sendMessage("You take an " + ring.getName() + " ring out of the bird's nest.");
                } else {
                    player.getPacketSender().sendMessage("You take a " + ring.getName() + " ring out of the bird's nest.");
                }
            }
        }
}