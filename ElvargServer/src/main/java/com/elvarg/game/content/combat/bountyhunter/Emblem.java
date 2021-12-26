package com.elvarg.game.content.combat.bountyhunter;

public enum Emblem {
    MYSTERIOUS_EMBLEM_1(12746, 1000),
    MYSTERIOUS_EMBLEM_2(12748, 2000),
    MYSTERIOUS_EMBLEM_3(12749, 4000),
    MYSTERIOUS_EMBLEM_4(12750, 8000),
    MYSTERIOUS_EMBLEM_5(12751, 15000),
    MYSTERIOUS_EMBLEM_6(12752, 24000),
    MYSTERIOUS_EMBLEM_7(12753, 35000),
    MYSTERIOUS_EMBLEM_8(12754, 50000),
    MYSTERIOUS_EMBLEM_9(12755, 70000),
    MYSTERIOUS_EMBLEM_10(12756, 100000),;

    public int id;
    public int value;
    Emblem(int id, int value) {
        this.id = id;
        this.value = value;
    }
}
