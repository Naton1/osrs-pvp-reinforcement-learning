package com.github.naton1.rl.env;

import com.elvarg.game.World;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import lombok.Value;

// Allows for a consistent random seed between two players
public class EnvFightContext {

    private static final Map<String, FightContext> contexts = new ConcurrentHashMap<>();

    // Gets a consistent random per fight for the player
    public static Random getRandom(String player) {
        return getContextRandom(player, "");
    }

    // Gets a consistent random per fight for the player, additionally seeded with the context key
    public static Random getContextRandom(String player, String contextKey) {
        if (!contexts.containsKey(player)) {
            throw new IllegalArgumentException("Unknown: " + player);
        }
        return new Random(contexts.get(player).getRandomSeed() + contextKey.hashCode());
    }

    // Should be called before a fight is started, but can be called again with the same arguments
    // without issue
    public static void register(String player, String target) {
        if (contexts.containsKey(player) != contexts.containsKey(target)) {
            throw new IllegalStateException("One is already registered: " + target + " - " + player);
        }
        if (contexts.containsKey(player) && !contexts.get(player).getTarget().equals(target)) {
            throw new IllegalArgumentException("Player " + player + " has different target than " + target);
        }
        if (contexts.containsKey(target) && !contexts.get(target).getTarget().equals(player)) {
            throw new IllegalArgumentException("Player " + target + " has different target than " + player);
        }
        if (contexts.containsKey(player)) {
            // Already registered
            return;
        }
        final int random = ThreadLocalRandom.current().nextInt();
        contexts.put(player, new FightContext(target, random));
        contexts.put(target, new FightContext(player, random));
    }

    // Should be called when a fight goes into a terminal state due to fight being over (ex. one
    // player dies)
    public static void clear(String player) {
        final FightContext context = contexts.remove(player);
        if (context != null) {
            contexts.remove(context.getTarget());
        }
    }

    // Should be called if a player disconnects. Will clear only if target is logged out as well.
    public static void tryReset(String player) {
        final FightContext context = contexts.get(player);
        if (context != null) {
            if (World.getPlayerByName(context.getTarget()).isEmpty()) {
                clear(player);
            }
        }
    }

    @Value
    private static class FightContext {
        private final String target;
        private final int randomSeed;
    }
}
