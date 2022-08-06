package com.elvarg.game.content.combat.bountyhunter;

import com.elvarg.game.entity.impl.npc.NPC;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.container.shop.ShopManager;
import com.elvarg.game.model.dialogues.builders.impl.EmblemTraderDialogue;
import com.elvarg.game.system.InteractIds;
import com.elvarg.game.system.NPCInteraction;
import com.elvarg.util.ShopIdentifiers;

import static com.elvarg.util.NpcIdentifiers.*;

@InteractIds({EMBLEM_TRADER, EMBLEM_TRADER_2, EMBLEM_TRADER_3})
public class EmblemTraderInteraction extends NPCInteraction {

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
}
