package com.elvarg.game.model;

public enum SkullType {

    WHITE_SKULL(0),
    RED_SKULL(1);

    private final int iconId;

    SkullType(int iconId) {
        this.iconId = iconId;
    }

    public int getIconId() {
        return iconId;
    }
}
