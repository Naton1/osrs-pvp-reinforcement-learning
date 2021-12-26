package com.elvarg.game.model;

import com.elvarg.game.model.container.impl.Equipment;

public enum EquipmentType {
    HOODED_CAPE(Equipment.CAPE_SLOT),
    CAPE(Equipment.CAPE_SLOT),

    SHIELD(Equipment.SHIELD_SLOT),

    GLOVES(Equipment.HANDS_SLOT),

    BOOTS(Equipment.FEET_SLOT),

    AMULET(Equipment.AMULET_SLOT),

    RING(Equipment.RING_SLOT),

    ARROWS(Equipment.AMMUNITION_SLOT),

    COIF(Equipment.HEAD_SLOT),
    HAT(Equipment.HEAD_SLOT),
    MASK(Equipment.HEAD_SLOT),
    MED_HELMET(Equipment.HEAD_SLOT),
    FULL_HELMET(Equipment.HEAD_SLOT),

    BODY(Equipment.BODY_SLOT),//TORSO REMOVAL
    PLATEBODY(Equipment.BODY_SLOT),

    LEGS(Equipment.LEG_SLOT),//REMOVES BOTTOM HALF OF BODY TO FEET IF ITEM HAS NO LEG DATA

    WEAPON(Equipment.WEAPON_SLOT),

    NONE(-1);//DEFAULT/NOTHING IN SLOT

    private final int slot;

    private EquipmentType(int slot) {
        this.slot = slot;
    }

    public int getSlot() {
        return slot;
    }
}
