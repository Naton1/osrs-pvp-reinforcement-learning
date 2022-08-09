package com.elvarg.game.model.dialogues.builders.impl;

import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.BrokenItem;
import com.elvarg.game.model.dialogues.builders.DynamicDialogueBuilder;
import com.elvarg.game.model.dialogues.entries.impl.ActionDialogue;
import com.elvarg.game.model.dialogues.entries.impl.NpcDialogue;
import com.elvarg.game.model.dialogues.entries.impl.OptionDialogue;
import com.elvarg.util.NpcIdentifiers;

public class BankerDialogue extends DynamicDialogueBuilder {

    @Override
    public void build(Player player) {
        add(new NpcDialogue(0, NpcIdentifiers.BANKER, "Hello would you like to open the bank?"));

        add(new OptionDialogue(1, (option) -> {
            switch (option) {
            case FIRST_OPTION:
                player.getBank(player.getCurrentBankTab()).open();
                break;
            default:
                player.getPacketSender().sendInterfaceRemoval();
                break;
            }
        }, "Yes Please", "No, thanks..."));
    }
}
