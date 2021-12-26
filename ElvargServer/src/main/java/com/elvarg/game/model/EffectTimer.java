package com.elvarg.game.model;

public enum EffectTimer {

    VENGEANCE(157),
    FREEZE(158),
    ANTIFIRE(159),
    OVERLOAD(160),
    TELE_BLOCK(161);

    private int clientSprite;

    EffectTimer(int clientSprite) {
        this.clientSprite = clientSprite;
    }

    public int getClientSprite() {
        return clientSprite;
    }

    public void setClientSprite(int sprite) {
        this.clientSprite = sprite;
    }
}
