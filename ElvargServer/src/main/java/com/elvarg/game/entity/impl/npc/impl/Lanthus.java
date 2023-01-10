package com.elvarg.game.entity.impl.npc.impl;

import com.elvarg.game.entity.impl.npc.NPC;
import com.elvarg.game.entity.impl.npc.NPCInteraction;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.Ids;
import com.elvarg.game.model.Item;
import com.elvarg.game.model.container.shop.Shop;
import com.elvarg.game.model.container.shop.ShopManager;
import com.elvarg.game.model.container.shop.currency.ShopCurrencies;

import static com.elvarg.util.ItemIdentifiers.*;
import static com.elvarg.util.NpcIdentifiers.LANTHUS;

@Ids(LANTHUS)
public class Lanthus implements NPCInteraction {

    private static final Shop CASTLE_WARS_SHOP = new Shop("Castle Wars Ticket Exchange", new Item[] {
        new Item(CASTLEWARS_HELM_RED, Shop.INFINITY), new Item(CASTLEWARS_PLATE_RED, Shop.INFINITY), new Item(CASTLEWARS_SWORD_RED, Shop.INFINITY), new Item(CASTLEWARS_SHIELD_RED, Shop.INFINITY), new Item(CASTLEWARS_LEGS_RED, Shop.INFINITY),
            new Item(CASTLEWARS_HELM_WHITE, Shop.INFINITY), new Item(CASTLEWARS_PLATE_WHITE, Shop.INFINITY), new Item(CASTLEWARS_SWORD_WHITE, Shop.INFINITY), new Item(CASTLEWARS_SHIELD_WHITE, Shop.INFINITY), new Item(CASTLEWARS_LEGS_WHITE, Shop.INFINITY),
            new Item(CASTLEWARS_HELM_GOLD, Shop.INFINITY), new Item(CASTLEWARS_PLATE_GOLD, Shop.INFINITY), new Item(CASTLEWARS_SWORD_GOLD, Shop.INFINITY), new Item(CASTLEWARS_SHIELD_GOLD, Shop.INFINITY), new Item(CASTLEWARS_LEGS_GOLD, Shop.INFINITY),
            new Item(CASTLEWARS_HOOD, Shop.INFINITY), new Item(CASTLEWARS_CLOAK, Shop.INFINITY), new Item(CASTLEWARS_HOOD_2, Shop.INFINITY), new Item(CASTLEWARS_CLOAK_2, Shop.INFINITY)
    }, ShopCurrencies.CASTLE_WARS_TICKET.get());

    static {
        ShopManager.shops.put(CASTLE_WARS_SHOP.getId(), CASTLE_WARS_SHOP);
    }

    @Override
    public void firstOptionClick(Player player, NPC npc) {

    }

    @Override
    public void secondOptionClick(Player player, NPC npc) {

    }

    @Override
    public void thirdOptionClick(Player player, NPC npc) {
        // Kinda weird that "Trade" is third option click and not second.. Ah well...
        ShopManager.open(player, CASTLE_WARS_SHOP.getId());
    }

    @Override
    public void forthOptionClick(Player player, NPC npc) {

    }

    @Override
    public void useItemOnNpc(Player player, NPC npc, int itemId, int slot) {

    }
}
