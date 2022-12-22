package com.elvarg.game.content.quests;

import com.elvarg.game.entity.impl.npc.NPC;
import com.elvarg.game.entity.impl.player.Player;

public interface Quest {
    int questTabButtonId();
    int questTabStringId();
    int completeStatus();
    int questPointsReward();
    void showQuestLog(Player player, int currentStatus);
    boolean firstClickNpc(Player player, NPC npc);
}
