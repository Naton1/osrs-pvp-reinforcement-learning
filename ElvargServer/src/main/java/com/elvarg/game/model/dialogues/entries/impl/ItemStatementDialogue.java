package com.elvarg.game.model.dialogues.entries.impl;

import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.dialogues.entries.Dialogue;
import com.elvarg.util.Misc;

public class ItemStatementDialogue extends Dialogue {

    private static final int[] CHATBOX_INTERFACES = { 4885, 4890, 4896, 4903 };

    private final String title;
    private final String[] text;
    private final int itemId;
    private final int modelZoom;

    public ItemStatementDialogue(int index, String title, String[] text, int itemId, int modelZoom) {
        super(index);
        this.title = title;
        this.text = text;
        this.itemId = itemId;
        this.modelZoom = modelZoom;
    }

    @Override
    public void send(Player player) {
        send(player, title, text, itemId, modelZoom);
    }
    
    public static void send(Player player, String title, String[] statements, int itemId, int modelZoom) {
        int length = statements.length > 5 ? 5 : statements.length;
        int startDialogueChildId = CHATBOX_INTERFACES[length - 1];
        int headChildId = startDialogueChildId - 2;
        player.getPacketSender().sendInterfaceModel(headChildId, itemId, modelZoom);
        player.getPacketSender().sendString(startDialogueChildId - 1, title);
        for (int i = 0; i < statements.length; i++) {
            player.getPacketSender().sendString(startDialogueChildId + i, statements[i]);
        }
        player.getPacketSender().sendChatboxInterface(startDialogueChildId - 3);
    }
}
