package com.elvarg.game.entity.impl.npc.impl;

import com.elvarg.game.entity.impl.npc.NPC;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.dialogues.builders.impl.BankerDialogue;
import com.elvarg.game.model.InteractIds;
import com.elvarg.game.entity.impl.npc.NPCInteraction;

import static com.elvarg.util.NpcIdentifiers.*;

@InteractIds({BANKER, BANKER_2, BANKER_3, BANKER_4, BANKER_5, BANKER_6, BANKER_7, TZHAAR_KET_ZUH})
public class Banker implements NPCInteraction {

    @Override
    public void firstOptionClick(Player player, NPC npc) {
        player.getDialogueManager().start(new BankerDialogue());
    }

    @Override
    public void secondOptionClick(Player player, NPC npc) {
        player.getBank(player.getCurrentBankTab()).open();
    }

    @Override
    public void thirdOptionClick(Player player, NPC npc) {

    }

    @Override
    public void forthOptionClick(Player player, NPC npc) {

    }

    @Override
    public void useItemOnNpc(Player player, NPC npc, int itemId, int slot) {

    }

}
