package com.elvarg.game.model.dialogues.builders.impl;

import com.elvarg.game.content.combat.CombatFactory;
import com.elvarg.game.content.combat.bountyhunter.BountyHunter;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.BrokenItem;
import com.elvarg.game.model.SkullType;
import com.elvarg.game.model.container.shop.ShopManager;
import com.elvarg.game.model.dialogues.DialogueExpression;
import com.elvarg.game.model.dialogues.builders.DynamicDialogueBuilder;
import com.elvarg.game.model.dialogues.entries.impl.ActionDialogue;
import com.elvarg.game.model.dialogues.entries.impl.EndDialogue;
import com.elvarg.game.model.dialogues.entries.impl.NpcDialogue;
import com.elvarg.game.model.dialogues.entries.impl.OptionDialogue;
import com.elvarg.util.NpcIdentifiers;
import com.elvarg.util.ShopIdentifiers;

public class EmblemTraderDialogue extends DynamicDialogueBuilder {

    @Override
    public void build(Player player) {

        add(new OptionDialogue(0, (option) -> {
            switch (option) {
                case FIRST_OPTION:
                    ShopManager.open(player, ShopIdentifiers.PVP_SHOP);
                    break;
                case SECOND_OPTION:
                    _OPTION:
                    player.getDialogueManager().start(2);
                    break;
                case THIRD_OPTION:
                    _OPTION:
                    player.getDialogueManager().start(5);
                    break;
                default:
                    player.getPacketSender().sendInterfaceRemoval();
                    break;
            }
        }, "I Would like to see your goods", "Could I sell my emblems please?", "Give me a skull!", "Eh.. Nothing..."));

        add(new ActionDialogue(2, () -> {
            int value = BountyHunter.getValueForEmblems(player, true);
            if (value == 0) {
                add(new NpcDialogue(3, NpcIdentifiers.EMBLEM_TRADER, "Don't come to me with no emblems.. Go and fight!!", DialogueExpression.ANGRY_1));
                add(new EndDialogue(4));
                player.getDialogueManager().start(this, 3);
            } else {
                add(new NpcDialogue(3, NpcIdentifiers.EMBLEM_TRADER, "Nice! You earned yourself " + value + " blood money!"));
                add(new EndDialogue(4));
                player.getDialogueManager().start(this, 3);
            }
        }));

        add(new OptionDialogue(5, (option) -> {
            switch (option) {
                case FIRST_OPTION:
                    CombatFactory.skull(player, SkullType.WHITE_SKULL, 300);
                    add(new NpcDialogue(6, NpcIdentifiers.EMBLEM_TRADER, "Here you go! Now have some fun!", DialogueExpression.LAUGHING));
                    player.getDialogueManager().start(this, 6);
                    break;
                case SECOND_OPTION:
                    CombatFactory.skull(player, SkullType.RED_SKULL, 300);
                    add(new NpcDialogue(6, NpcIdentifiers.EMBLEM_TRADER, "Here you go! Don't cry if you die!!", DialogueExpression.LAUGHING));
                    player.getDialogueManager().start(this, 6);
                    break;
                default:
                    player.getPacketSender().sendInterfaceRemoval();
                    break;
            }
        }, "Give me white skull!", "Give me red skull! (No item protect)", "Nothing..."));

    }
}
