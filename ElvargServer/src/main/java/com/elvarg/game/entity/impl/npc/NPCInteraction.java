package com.elvarg.game.entity.impl.npc;

import com.elvarg.game.entity.impl.player.Player;

public interface NPCInteraction {
    void firstOptionClick(Player player, NPC npc);
    void secondOptionClick(Player player, NPC npc);
    void thirdOptionClick(Player player, NPC npc);
    void forthOptionClick(Player player, NPC npc);
    void useItemOnNpc(Player player, NPC npc, int itemId, int slot);
}
