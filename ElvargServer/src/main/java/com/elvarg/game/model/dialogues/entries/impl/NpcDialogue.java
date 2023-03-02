package com.elvarg.game.model.dialogues.entries.impl;

import com.elvarg.game.definition.NpcDefinition;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.dialogues.DialogueAction;
import com.elvarg.game.model.dialogues.DialogueExpression;
import com.elvarg.game.model.dialogues.entries.Dialogue;
import com.elvarg.util.Misc;

public class NpcDialogue extends Dialogue {

    private static final int[] CHATBOX_INTERFACES = { 4885, 4890, 4896, 4903 };

    private final int npcId;
    private final String text;
    private final DialogueExpression expression;

    public NpcDialogue(int index, int npcId, String text, DialogueExpression expression) {
        super(index);
        this.npcId = npcId;
        this.text = text;
        this.expression = expression;
    }

    public NpcDialogue(int index, int npcId, String text) {
        this(index, npcId, text, DialogueExpression.CALM);
    }

    public NpcDialogue(int index, int npcId, String text, DialogueExpression expression, DialogueAction continueAction) {
        this(index, npcId, text, expression);
        this.setContinueAction(continueAction);
    }

    public NpcDialogue(int index, int npcId, String text, DialogueAction continueAction) {
        this(index, npcId, text, DialogueExpression.CALM);
        this.setContinueAction(continueAction);
    }

    @Override
    public void send(Player player) {
        send(player, npcId, text, expression);
    }
    
    public static void send(Player player, int npcId, String text, DialogueExpression expression) {
        String[] lines = Misc.wrapText(text, 53);
        int length = lines.length;
        if (length > 5) {
            length = 5;
        }
        int startDialogueChildId = CHATBOX_INTERFACES[length - 1];
        int headChildId = startDialogueChildId - 2;
        player.getPacketSender().sendNpcHeadOnInterface(npcId, headChildId);
        player.getPacketSender().sendInterfaceAnimation(headChildId, expression.getExpression());
        player.getPacketSender().sendString(startDialogueChildId - 1,
                NpcDefinition.forId(npcId) != null ? NpcDefinition.forId(npcId).getName().replaceAll("_", " ") : "");
        for (int i = 0; i < length; i++) {
            player.getPacketSender().sendString(startDialogueChildId + i, lines[i]);
        }
        player.getPacketSender().sendChatboxInterface(startDialogueChildId - 3);
    }

    public static void sendStatement(Player player, int npcId, String[] lines, DialogueExpression expression) {
        int length = lines.length;
        if (length > 5) {
            length = 5;
        }
        int startDialogueChildId = CHATBOX_INTERFACES[length - 1];
        int headChildId = startDialogueChildId - 2;
        player.getPacketSender().sendNpcHeadOnInterface(npcId, headChildId);
        player.getPacketSender().sendInterfaceAnimation(headChildId, expression.getExpression());
        player.getPacketSender().sendString(startDialogueChildId - 1,
                NpcDefinition.forId(npcId) != null ? NpcDefinition.forId(npcId).getName().replaceAll("_", " ") : "");
        for (int i = 0; i < length; i++) {
            player.getPacketSender().sendString(startDialogueChildId + i, lines[i]);
        }
        player.getPacketSender().sendChatboxInterface(startDialogueChildId - 3);
    }
}
