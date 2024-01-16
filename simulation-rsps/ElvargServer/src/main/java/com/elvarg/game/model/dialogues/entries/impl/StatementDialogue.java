package com.elvarg.game.model.dialogues.entries.impl;

import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.dialogues.entries.Dialogue;
import com.elvarg.util.Misc;

public class StatementDialogue extends Dialogue {

    private static final int[] CHATBOX_INTERFACES = { 356, 359, 363, 368, 374 };

    private final String text;

    public StatementDialogue(int index, String text) {
        super(index);
        this.text = text;
    }

    @Override
    public void send(Player player) {
        send(player, text);
    }
    
    public static void send(Player player, String text) {
        String[] lines = Misc.wrapText(text, 60);
        int length = lines.length > 5 ? 5 : lines.length;
        int chatboxInterface = CHATBOX_INTERFACES[length - 1];
        for (int i = 0; i < length; i++) {
            player.getPacketSender().sendString((chatboxInterface + 1) + i, lines[i]);
        }
        player.getPacketSender().sendChatboxInterface(chatboxInterface);
    }

    public static void send(Player player, String[] lines) {
        int length = lines.length > 5 ? 5 : lines.length;
        int chatboxInterface = CHATBOX_INTERFACES[length - 1];
        for (int i = 0; i < length; i++) {
            player.getPacketSender().sendString((chatboxInterface + 1) + i, lines[i]);
        }
        player.getPacketSender().sendChatboxInterface(chatboxInterface);
    }
}
