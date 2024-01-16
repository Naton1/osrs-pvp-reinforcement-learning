package com.elvarg.game.model.dialogues.entries.impl;

import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.dialogues.DialogueOption;
import com.elvarg.game.model.dialogues.DialogueOptionAction;
import com.elvarg.game.model.dialogues.entries.Dialogue;

public class OptionDialogue extends Dialogue {

    private static final int[] CHATBOX_INTERFACES = { 13760, 2461, 2471, 2482, 2494, };

    private final DialogueOptionAction action;
    private final String title;
    private final String[] options;

    public OptionDialogue(int index, String title, DialogueOptionAction action, String... options) {
        super(index);
        this.title = title;
        this.action = action;
        this.options = options;
    }

    public OptionDialogue(int index, DialogueOptionAction action, String... options) {
        this(index, "Choose an Option", action, options);
    }

    public void execute(DialogueOption option) {
        if (action == null) {
            return;
        }
        action.executeOption(option);
    }

    @Override
    public void send(Player player) {
        send(player, title, options);
    }

    public static void send(Player player, String title, String[] options) {
        int firstChildId = CHATBOX_INTERFACES[options.length - 1];
        player.getPacketSender().sendString(firstChildId - 1, title);
        for (int i = 0; i < options.length; i++) {
            player.getPacketSender().sendString(firstChildId + i, options[i]);
        }
        player.getPacketSender().sendChatboxInterface(firstChildId - 2);
    }
}
