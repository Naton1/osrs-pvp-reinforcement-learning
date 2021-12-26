package com.elvarg.game.definition;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents an npc's definition.
 * Holds its information, such as
 * name and combat level.
 *
 * @author Professor Oak
 */
public class NpcDefinition {

    /**
     * The map containing all our {@link ItemDefinition}s.
     */
    public static final Map<Integer, NpcDefinition> definitions = new HashMap<Integer, NpcDefinition>();

    
    /**
     * The default {@link ItemDefinition} that will be used.
     */
    private static final NpcDefinition DEFAULT = new NpcDefinition();
    
    
    
    private int id;
    private String name;
    private String examine;
    private int size;
    private int walkRadius;
    private boolean attackable;
    private boolean retreats;
    private boolean aggressive;
    private boolean poisonous;
    private int respawn;
    private int maxHit;
    private int hitpoints = 10;
    private int attackSpeed;
    private int attackAnim;
    private int defenceAnim;
    private int deathAnim;
    private int combatLevel;
    private int[] stats;
    private int slayerLevel;
    private int combatFollowDistance;

    /**
     * Attempts to get the {@link ItemDefinition} for the
     * given item.
     *
     * @param item
     * @return
     */
    public static NpcDefinition forId(int item) {
        return definitions.getOrDefault(item, DEFAULT);
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getExamine() {
        return examine;
    }

    public int getSize() {
        return size;
    }

    public int getWalkRadius() {
        return walkRadius;
    }

    public boolean isAttackable() {
        return attackable;
    }

    public boolean doesRetreat() {
        return retreats;
    }

    public boolean isAggressive() {
        return aggressive;
    }

    public boolean isPoisonous() {
        return poisonous;
    }

    public int getRespawn() {
        return respawn;
    }

    public int getMaxHit() {
        return maxHit;
    }

    public int getHitpoints() {
        return hitpoints;
    }

    public int getAttackSpeed() {
        return attackSpeed;
    }

    public int getAttackAnim() {
        return attackAnim;
    }

    public int getDefenceAnim() {
        return defenceAnim;
    }

    public int getDeathAnim() {
        return deathAnim;
    }

    public int getCombatLevel() {
        return combatLevel;
    }

    public int[] getStats() {
        return stats;
    }

    public int getSlayerLevel() {
        return slayerLevel;
    }

    public int getCombatFollowDistance() {
        return combatFollowDistance;
    }
}
