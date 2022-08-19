package com.elvarg.game.model.dialogues.builders.impl;

import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.BrokenItem;
import com.elvarg.game.model.dialogues.builders.DynamicDialogueBuilder;
import com.elvarg.game.model.dialogues.entries.impl.ActionDialogue;
import com.elvarg.game.model.dialogues.entries.impl.NpcDialogue;
import com.elvarg.game.model.dialogues.entries.impl.OptionDialogue;
import com.elvarg.util.NpcIdentifiers;

public class ParduDialogue extends DynamicDialogueBuilder {

    @Override
    public void build(Player player) {
        var allBrokenItemCost = BrokenItem.getRepairCost(player);
        if (allBrokenItemCost == 0) {
            add(new NpcDialogue(0, NpcIdentifiers.PERDU, "Hello! Seems like you have no broken items."));
            return;
        }
        add(new NpcDialogue(0, NpcIdentifiers.PERDU, "Hello would you like that I fix all your broken item for " +allBrokenItemCost+" blood money?"));

        add(new OptionDialogue(1, (option) -> {
            switch (option) {
            case FIRST_OPTION:
                player.getDialogueManager().start(2);
                break;
            default:
                player.getPacketSender().sendInterfaceRemoval();
                break;
            }
        }, "Yes Please", "No, thanks..."));

        add(new ActionDialogue(2, () -> {
            var isSuccess = BrokenItem.repair(player);
            if (isSuccess) {
                add(new NpcDialogue(3, NpcIdentifiers.PERDU, "All items repaired!"));
                player.getDialogueManager().start(this, 3);
            } else {
                add(new NpcDialogue(3, NpcIdentifiers.PERDU, "You dont have enough blood money for me to fix your items..."));
                player.getDialogueManager().start(this, 3);
            }
        }));
    }
}
