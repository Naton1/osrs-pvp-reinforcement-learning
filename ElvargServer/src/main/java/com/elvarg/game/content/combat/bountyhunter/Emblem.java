package com.elvarg.game.content.combat.bountyhunter;

public enum Emblem {
    MYSTERIOUS_EMBLEM_1(12746, 500),
    MYSTERIOUS_EMBLEM_2(12748, 1000),
    MYSTERIOUS_EMBLEM_3(12749, 1800),
    MYSTERIOUS_EMBLEM_4(12750, 3200),
    MYSTERIOUS_EMBLEM_5(12751, 4800),
    MYSTERIOUS_EMBLEM_6(12752, 6800),
    MYSTERIOUS_EMBLEM_7(12753, 9000),
    MYSTERIOUS_EMBLEM_8(12754, 12000),
    MYSTERIOUS_EMBLEM_9(12755, 16000),
    MYSTERIOUS_EMBLEM_10(12756, 20000),;

    public int id;
    public int value;
    Emblem(int id, int value) {
        this.id = id;
        this.value = value;
    }
}
