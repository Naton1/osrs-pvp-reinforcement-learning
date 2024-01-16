package com.elvarg.game.model.dialogues.entries.impl;

import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.dialogues.DialogueExpression;
import com.elvarg.game.model.dialogues.entries.Dialogue;
import com.elvarg.util.Misc;

public class PlayerDialogue extends Dialogue {

    private static final int[] CHATBOX_INTERFACES = { 971, 976, 982, 989 };
    
    private final String text;
    private final DialogueExpression expression;

    public PlayerDialogue(int index, String text, DialogueExpression expression) {
        super(index);
        this.text = text;
        this.expression = expression;
    }

    public PlayerDialogue(int index, String text) {
        this(index, text, DialogueExpression.CALM);
    }

    @Override
    public void send(Player player) {
        send(player, text, expression);
    }
    
    public static void send(Player player, String text, DialogueExpression expression) {
        String[] lines = Misc.wrapText(text, 53);
        int length = lines.length;
        if (length > 5) {
            length = 5;
        }
        int startDialogueChildId = CHATBOX_INTERFACES[length - 1];
        int headChildId = startDialogueChildId - 2;
        player.getPacketSender().sendPlayerHeadOnInterface(headChildId);
        player.getPacketSender().sendInterfaceAnimation(headChildId, expression.getExpression());
        player.getPacketSender().sendString(startDialogueChildId - 1, player.getUsername());
        for (int i = 0; i < length; i++) {
            player.getPacketSender().sendString(startDialogueChildId + i, lines[i]);
        }
        player.getPacketSender().sendChatboxInterface(startDialogueChildId - 3);
    }
}
