package com.elvarg.game.model.dialogues.builders.impl;

import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.MagicSpellbook;
import com.elvarg.game.model.dialogues.builders.DynamicDialogueBuilder;
import com.elvarg.game.model.dialogues.entries.impl.OptionDialogue;

public class SpellBookDialogue extends DynamicDialogueBuilder {

    @Override
    public void build(Player player) {
        add(new OptionDialogue(0, (option) -> {
            switch (option) {
                case FIRST_OPTION:
                    player.getPacketSender().sendInterfaceRemoval();
                    MagicSpellbook.changeSpellbook(player, MagicSpellbook.NORMAL);
                    break;
                case SECOND_OPTION:
                    player.getPacketSender().sendInterfaceRemoval();
                    MagicSpellbook.changeSpellbook(player, MagicSpellbook.ANCIENT);
                    break;
                case THIRD_OPTION:
                    player.getPacketSender().sendInterfaceRemoval();
                    MagicSpellbook.changeSpellbook(player, MagicSpellbook.LUNAR);
                    break;
                default:
                    player.getPacketSender().sendInterfaceRemoval();
                    break;
            }
        }, "Normal", "Ancient", "Lunar"));

    }
}
