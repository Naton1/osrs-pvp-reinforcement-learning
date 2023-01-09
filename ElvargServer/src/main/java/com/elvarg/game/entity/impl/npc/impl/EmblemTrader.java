package com.elvarg.game.entity.impl.npc.impl;

import com.elvarg.game.entity.impl.npc.NPC;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.container.shop.ShopManager;
import com.elvarg.game.model.dialogues.builders.impl.EmblemTraderDialogue;
import com.elvarg.game.entity.impl.npc.NPCInteraction;
import com.elvarg.util.ShopIdentifiers;

public class EmblemTrader implements NPCInteraction {

    @Override
    public void firstOptionClick(Player player, NPC npc) {
        player.getDialogueManager().start(new EmblemTraderDialogue());
    }

    @Override
    public void secondOptionClick(Player player, NPC npc) {
        ShopManager.open(player, ShopIdentifiers.PVP_SHOP);
    }

    @Override
    public void thirdOptionClick(Player player, NPC npc) {
        player.getDialogueManager().start(new EmblemTraderDialogue(), 2);
    }

    @Override
    public void forthOptionClick(Player player, NPC npc) {
        player.getDialogueManager().start(new EmblemTraderDialogue(), 5);
    }

    @Override
    public void useItemOnNpc(Player player, NPC npc, int itemId, int slot) {

    }
}
