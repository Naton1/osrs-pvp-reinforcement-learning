package com.elvarg.game.model;

import java.util.HashMap;
import java.util.Map;

public enum Skillcape {
    ATTACK(new int[]{9747, 9748, 10639},
            4959, 823, 7),
    DEFENCE(new int[]{9753, 9754, 10641},
            4961, 824, 10),
    STRENGTH(new int[]{9750, 9751, 10640},
            4981, 828, 25),
    CONSTITUTION(new int[]{9768, 9769, 10647},
            14242, 2745, 12),
    RANGED(new int[]{9756, 9757, 10642},
            4973, 832, 12),
    PRAYER(new int[]{9759, 9760, 10643},
            4979, 829, 15),
    MAGIC(new int[]{9762, 9763, 10644},
            4939, 813, 6),
    COOKING(new int[]{9801, 9802, 10658},
            4955, 821, 36),
    WOODCUTTING(new int[]{9807, 9808, 10660},
            4957, 822, 25),
    FLETCHING(new int[]{9783, 9784, 10652},
            4937, 812, 20),
    FISHING(new int[]{9798, 9799, 10657},
            4951, 819, 19),
    FIREMAKING(new int[]{9804, 9805, 10659},
            4975, 831, 14),
    CRAFTING(new int[]{9780, 9781, 10651},
            4949, 818, 15),
    SMITHING(new int[]{9795, 9796, 10656},
            4943, 815, 23),
    MINING(new int[]{9792, 9793, 10655},
            4941, 814, 8),
    HERBLORE(new int[]{9774, 9775, 10649},
            4969, 835, 16),
    AGILITY(new int[]{9771, 9772, 10648},
            4977, 830, 8),
    THIEVING(new int[]{9777, 9778, 10650},
            4965, 826, 16),
    SLAYER(new int[]{9786, 9787, 10653},
            4967, 1656, 8),
    FARMING(new int[]{9810, 9811, 10661},
            4963, -1, 16),
    RUNECRAFTING(new int[]{9765, 9766, 10645},
            4947, 817, 10),
    CONSTRUCTION(new int[]{9789, 9790, 10654},
            4953, 820, 16),
    HUNTER(new int[]{9948, 9949, 10646},
            5158, 907, 14),
    QUEST_POINT(new int[]{9813, 9814, 10662},
            4945, 816, 19);

    private static Map<Integer, Skillcape> dataMap = new HashMap<Integer, Skillcape>();

    static {
        for (Skillcape data : Skillcape.values()) {
            for (Item item : data.item) {
                dataMap.put(item.getId(), data);
            }
        }
    }

    private final Item[] item;
    private final Animation animation;
    private final Graphic graphic;
    private final int delay;

    Skillcape(int[] itemId, int animationId, int graphicId, int delay) {
        item = new Item[itemId.length];
        for (int i = 0; i < itemId.length; i++) {
            item[i] = new Item(itemId[i]);
        }
        animation = new Animation(animationId);
        graphic = new Graphic(graphicId);
        this.delay = delay;
    }

    public static Skillcape forId(int id) {
        return dataMap.get(id);
    }

    public Animation getAnimation() {
        return animation;
    }

    public Graphic getGraphic() {
        return graphic;
    }

    public int getDelay() {
        return delay;
    }
}
