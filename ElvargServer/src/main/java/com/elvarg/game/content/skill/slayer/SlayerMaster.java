package com.elvarg.game.content.skill.slayer;

import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.Skill;

public enum SlayerMaster {
    TURAEL(3, 1, new int[][] { { 10, 3 }, { 50, 10 }, { 100, 25 }, { 250, 50 }, { 1000, 75 } }),
    MAZCHNA(20, 2, new int[][] { { 10, 5 }, { 50, 15 }, { 100, 50 }, { 250, 70 }, { 1000, 100 } }),
    VANNAKA(40, 4, new int[][] { { 10, 20 }, { 50, 60 }, { 100, 100 }, { 250, 140 }, { 1000, 200 } }),
    CHAELDAR(70, 10, new int[][] { { 10, 50 }, { 50, 150 }, { 100, 250 }, { 250, 350 }, { 1000, 500 } }),
    NIEVE(85, 12, new int[][] { { 10, 60 }, { 50, 180 }, { 100, 300 }, { 250, 420 }, { 1000, 600 } }),
    DURADEL(100, 15, new int[][] { { 10, 75 }, { 50, 225 }, { 100, 375 }, { 250, 525 }, { 1000, 750 } });

    private final int combatLevel;
    private final int basePoints;
    private final int[][] consecutiveTaskPoints;

    SlayerMaster(int combatLevel, int basePoints, int[][] consecutiveTaskPoints) {
        this.combatLevel = combatLevel;
        this.basePoints = basePoints;
        this.consecutiveTaskPoints = consecutiveTaskPoints;
    }

    public int getCombatLevel() {
        return combatLevel;
    }

    public int getBasePoints() {
        return basePoints;
    }

    public int[][] getConsecutiveTaskPoints() {
        return consecutiveTaskPoints;
    }
    
    public boolean canAssign(Player player) {
        if (player.getSkillManager().getCombatLevel() < combatLevel) {
            return false;
        }
        if (this == SlayerMaster.DURADEL) {
            return player.getSkillManager().getMaxLevel(Skill.SLAYER) >= 50;
        }
        return true;
    }
    
    public static final SlayerMaster[] MASTERS = values();
}
