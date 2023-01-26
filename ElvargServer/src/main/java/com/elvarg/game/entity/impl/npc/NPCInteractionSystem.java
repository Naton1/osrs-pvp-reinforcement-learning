package com.elvarg.game.entity.impl.npc;

import com.elvarg.game.entity.impl.player.Player;

public class NPCInteractionSystem {

    public static boolean handleFirstOption(Player player, NPC npc) {
        if (!NPCInteraction.class.isAssignableFrom(npc.getClass())) {
            return false;
        }

        ((NPCInteraction) npc).firstOptionClick(player, npc);
        return true;
    }

    public static boolean handleSecondOption(Player player, NPC npc) {
        if (!NPCInteraction.class.isAssignableFrom(npc.getClass())) {
            return false;
        }

        ((NPCInteraction) npc).secondOptionClick(player, npc);
        return true;
    }

    public static boolean handleThirdOption(Player player, NPC npc) {
        if (!NPCInteraction.class.isAssignableFrom(npc.getClass())) {
            return false;
        }

        ((NPCInteraction) npc).thirdOptionClick(player, npc);
        return true;
    }

    public static boolean handleForthOption(Player player, NPC npc) {
        if (!NPCInteraction.class.isAssignableFrom(npc.getClass())) {
            return false;
        }

        ((NPCInteraction) npc).forthOptionClick(player, npc);
        return true;
    }

    public static boolean handleUseItem(Player player, NPC npc, int itemId, int slot) {
        if (!NPCInteraction.class.isAssignableFrom(npc.getClass())) {
            return false;
        }

        ((NPCInteraction) npc).useItemOnNpc(player, npc, itemId, slot);
        return true;
    }

}
