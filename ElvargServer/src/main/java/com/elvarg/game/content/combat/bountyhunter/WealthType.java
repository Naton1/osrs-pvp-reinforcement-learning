package com.elvarg.game.content.combat.bountyhunter;

import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.Item;
import com.elvarg.util.Misc;

public enum WealthType {
    NO_TARGET("N/A", 876),
    VERY_LOW("V. Low", 877),
    LOW("Low", 878),
    MEDIUM("Medium", 879),
    HIGH("High", 880),
    VERY_HIGH("V. HIGH", 881);;

    public String tooltip;
    public int configId;
    WealthType(String tooltip, int configId) {
        this.tooltip = tooltip;
        this.configId = configId;
    }

    /**
     * Gets a player's wealth type depending on the value of
     * their items.
     *
     * @return
     */
    public static WealthType getWealth(Player player) {
        int wealth = 0;

        for (Item item : Misc.concat(player.getInventory().getItems(), player.getEquipment().getItems())) {
            if (item == null || item.getId() <= 0 || item.getAmount() <= 0 || !item.getDefinition().isDropable() || !item.getDefinition().isTradeable()) {
                continue;
            }
            wealth += item.getDefinition().getValue();
        }
        WealthType type = WealthType.VERY_LOW;
        if (wealth >= Emblem.MYSTERIOUS_EMBLEM_1.value) {
            type = WealthType.LOW;
        }
        if (wealth >= Emblem.MYSTERIOUS_EMBLEM_3.value) {
            type = WealthType.MEDIUM;
        }
        if (wealth >= Emblem.MYSTERIOUS_EMBLEM_6.value) {
            type = WealthType.HIGH;
        }
        if (wealth >= Emblem.MYSTERIOUS_EMBLEM_9.value) {
            type = WealthType.VERY_HIGH;
        }
        return type;
    }
}
