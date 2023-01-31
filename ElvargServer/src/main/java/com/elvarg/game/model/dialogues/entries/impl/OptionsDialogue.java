package com.elvarg.game.model.dialogues.entries.impl;

import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.dialogues.DialogueOptionsAction;
import com.elvarg.game.model.dialogues.entries.Dialogue;

import java.util.LinkedHashMap;

/**
 * This class allows us to initialise option dialogs easily via lambdas, for example:
 *
 * new OptionsDialogue(0, new LinkedHashMap<>() {{
 * 		put("One", () -> { });
 * 		put("Second option", () -> { });
 * }});
 *
 * This saves us from needing logic inside a single callback to determine which option was pressed.
 */
public class OptionsDialogue extends Dialogue {

    private static final int[] CHATBOX_INTERFACES = { 13760, 2461, 2471, 2482, 2494, };

    private final String title;
    private final LinkedHashMap<String, DialogueOptionsAction> optionsMap;

    public OptionsDialogue(int index, String title, LinkedHashMap<String, DialogueOptionsAction> optionsMap) {
        super(index);
        this.title = title;
        this.optionsMap = optionsMap;
    }

    public OptionsDialogue(int index, LinkedHashMap<String, DialogueOptionsAction> optionsMap) {
        this(index, "Choose an Option", optionsMap);
    }

    public void execute(int optionIndex, Player player) {
        if (optionsMap == null || player == null) {
            return;
        }

        this.getDialogueActionByIndex(optionIndex).execute(player);
    }

    public DialogueOptionsAction getDialogueActionByIndex(int index){
        return this.optionsMap.get( (this.optionsMap.keySet().toArray())[ index ] );
    }

    @Override
    public void send(Player player) {
        send(player, title, this.optionsMap.keySet().toArray(new String[0]));
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
