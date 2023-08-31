package com.elvarg.game.content.cannon;

import com.elvarg.util.ItemIdentifiers;
import com.elvarg.util.ObjectIdentifiers;

/**
 * @author Ynneh | 31/08/2023 - 16:21
 * <https://github.com/drhenny>
 */
public enum DwarfCannonParts {

    BASE(ObjectIdentifiers.CANNON_BASE, ItemIdentifiers.CANNON_BASE),
    STAND(ObjectIdentifiers.CANNON_STAND, ItemIdentifiers.CANNON_STAND),
    BARRELS(ObjectIdentifiers.CANNON_BARRELS, ItemIdentifiers.CANNON_BARRELS),
    FURNACE(ObjectIdentifiers.DWARF_MULTICANNON, ItemIdentifiers.CANNON_FURNACE)

    ;

    public int objectId, itemId;

    public int getObjectId() {
        return objectId;
    }

    public int getItemId() {
        return itemId;
    }

    DwarfCannonParts(int objectId, int itemId) {
        this.objectId = objectId;
        this.itemId = itemId;
    }
}
