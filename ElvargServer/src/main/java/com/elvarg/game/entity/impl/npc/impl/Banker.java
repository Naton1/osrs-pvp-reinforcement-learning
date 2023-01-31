package com.elvarg.game.entity.impl.npc.impl;

import com.elvarg.game.entity.impl.npc.NPC;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.Location;
import com.elvarg.game.model.dialogues.builders.impl.BankerDialogue;
import com.elvarg.game.entity.impl.npc.NPCInteraction;

public class Banker extends NPC implements NPCInteraction {

    /**
     * Constructs a Banker.
     *
     * @param id       The npc id.
     * @param position
     */
    public Banker(int id, Location position) {
        super(id, position);
    }

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
