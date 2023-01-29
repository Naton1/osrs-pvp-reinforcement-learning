package com.elvarg.game.entity.impl.npc.impl;

import com.elvarg.game.content.minigames.impl.CastleWars;
import com.elvarg.game.entity.impl.npc.NPC;
import com.elvarg.game.entity.impl.npc.NPCInteraction;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.Ids;
import com.elvarg.game.model.Item;
import com.elvarg.game.model.Location;
import com.elvarg.game.model.container.shop.Shop;
import com.elvarg.game.model.container.shop.ShopManager;
import com.elvarg.game.model.container.shop.currency.ShopCurrencies;
import com.elvarg.game.model.dialogues.builders.DialogueChainBuilder;
import com.elvarg.game.model.dialogues.entries.impl.*;

import java.util.LinkedHashMap;

import static com.elvarg.util.ItemIdentifiers.*;
import static com.elvarg.util.NpcIdentifiers.*;

@Ids({LANTHUS})
public class Lanthus extends NPC implements NPCInteraction {

    private DialogueChainBuilder dialogueBuilder;

    private static final Shop CASTLE_WARS_SHOP = new Shop("Castle Wars Ticket Exchange", new Item[] {
        new Item(CASTLEWARS_HELM_RED, Shop.INFINITY), new Item(CASTLEWARS_PLATE_RED, Shop.INFINITY), new Item(CASTLEWARS_SWORD_RED, Shop.INFINITY), new Item(CASTLEWARS_SHIELD_RED, Shop.INFINITY), new Item(CASTLEWARS_LEGS_RED, Shop.INFINITY),
            new Item(CASTLEWARS_HELM_WHITE, Shop.INFINITY), new Item(CASTLEWARS_PLATE_WHITE, Shop.INFINITY), new Item(CASTLEWARS_SWORD_WHITE, Shop.INFINITY), new Item(CASTLEWARS_SHIELD_WHITE, Shop.INFINITY), new Item(CASTLEWARS_LEGS_WHITE, Shop.INFINITY),
            new Item(CASTLEWARS_HELM_GOLD, Shop.INFINITY), new Item(CASTLEWARS_PLATE_GOLD, Shop.INFINITY), new Item(CASTLEWARS_SWORD_GOLD, Shop.INFINITY), new Item(CASTLEWARS_SHIELD_GOLD, Shop.INFINITY), new Item(CASTLEWARS_LEGS_GOLD, Shop.INFINITY),
            new Item(CASTLEWARS_HOOD, Shop.INFINITY), new Item(CASTLEWARS_CLOAK, Shop.INFINITY), new Item(CASTLEWARS_HOOD_2, Shop.INFINITY), new Item(CASTLEWARS_CLOAK_2, Shop.INFINITY)
    }, ShopCurrencies.CASTLE_WARS_TICKET.get());

    static {
        ShopManager.shops.put(CASTLE_WARS_SHOP.getId(), CASTLE_WARS_SHOP);
    }

    /**
     * Constructs a Lanthus.
     *
     * @param id       The npc id.
     * @param position
     */
    public Lanthus(int id, Location position) {
        super(id, position);
        this.buildDialogues();

        CastleWars.LOBBY_AREA.setLanthus(this);
    }

    @Override
    public void firstOptionClick(Player player, NPC npc) {
        player.getDialogueManager().start(this.dialogueBuilder, 0);
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

    private void buildDialogues() {
        this.dialogueBuilder  = new DialogueChainBuilder();
        this.dialogueBuilder.add(
                new NpcDialogue(0, LANTHUS, "Good day, how may I help you?"),
                new OptionsDialogue(1, new LinkedHashMap<>() {{
                    put("What is this place?", (player) -> player.getDialogueManager().start(2));
                    put("What do you have for trade?", (player) -> ShopManager.open(player, CASTLE_WARS_SHOP.getId()));
                    put("Do you have a manual? I'd like to learn how to play!", (player) -> player.getDialogueManager().start(4));
                }}),

                new PlayerDialogue(2, "What is this place?"),
                new NpcDialogue(3, LANTHUS,
                        "This is the great Castle Wars arena! Here you can " +
                                "fight for the glory of Saradomin or Zamorak.",
                        (player -> player.getDialogueManager().start(this.dialogueBuilder, 1))),

                new PlayerDialogue(4, "Do you have a manual? I'd like to learn how to play!"),
                new NpcDialogue(5, LANTHUS, "Sure, here you go.", (player) -> {
                    player.getInventory().add(CastleWars.MANUAL);
                    ItemStatementDialogue.send(player, "", new String[] {"Lanthus gives you a Castlewars Manual."}, CastleWars.MANUAL.getId(), 200);
                }),
                new EndDialogue(6)
        );
    }


}
