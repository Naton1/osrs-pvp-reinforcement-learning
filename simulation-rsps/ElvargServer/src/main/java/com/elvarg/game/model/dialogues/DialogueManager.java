package com.elvarg.game.model.dialogues;

import java.util.HashMap;
import java.util.Map;

import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.dialogues.builders.DynamicDialogueBuilder;
import com.elvarg.game.model.dialogues.builders.DialogueBuilder;
import com.elvarg.game.model.dialogues.builders.impl.TestStaticDialogue;
import com.elvarg.game.model.dialogues.entries.Dialogue;
import com.elvarg.game.model.dialogues.entries.impl.OptionDialogue;
import com.elvarg.game.model.dialogues.entries.impl.OptionsDialogue;

public class DialogueManager {

    public static final Map<Integer, DialogueBuilder> STATIC_DIALOGUES = new HashMap<>();

    static {
        STATIC_DIALOGUES.put(0, new TestStaticDialogue());
    }

    /**
     * Represents the owner of this {@link DialogueManager} instance.
     */
    private final Player player;

    /**
     * A {@link Map} which holds all of the current dialogue entries and indexes.
     */
    private final Map<Integer, Dialogue> dialogues = new HashMap<>();

    /**
     * The current dialogue's index.
     */
    private int index;

    /**
     * Creates a new {@link DialogueManager} for the given {@link Player}.
     * 
     * @param player
     */
    public DialogueManager(Player player) {
        this.player = player;
    }

    /**
     * Resets all of the attributes of the {@link DialogueManager}.
     */
    public void reset() {
        dialogues.clear();
        index = -1;
    }

    /**
     * Advances, starting the next dialogue.
     */
    public void advance() {
        Dialogue current = dialogues.get(index);
        if (current == null) {
            reset();
            player.getPacketSender().sendInterfaceRemoval();
            return;
        }

        DialogueAction continueAction = current.getContinueAction();
        if(continueAction != null) {
            // This dialogue has a custom continue action
            continueAction.execute(player);
            reset();
            return;
        }

        start(index + 1);
    }

    /**
     * Starts the dialogue with the given {@code index}.
     * 
     * @param index
     */
    public void start(int index) {
        this.index = index;
        start();
    }

    /**
     * Starts a {@link DialogueBuilder} with the given {@code index}.
     * 
     * @param id
     */
    public void startStatic(int id) {
        DialogueBuilder builder = STATIC_DIALOGUES.get(id);
        if (builder != null) {
            start(builder);
        }
    }
    
    /**
     * Starts a fresh dynamic dialogue which has been built using a
     * {@link DynamicDialogueBuilder}.
     * 
     * Dynamic dialogues can be used to shape the dialogue around the attributes of
     * the player.
     * 
     * @param builder
     */
    public void start(DialogueBuilder builder) {
        start(builder, 0);
    }

    /**
     * Starts a fresh dynamic dialogue at the given index.
     * 
     * @param builder
     */
    public void start(DialogueBuilder builder, int index) {
        if (builder instanceof DynamicDialogueBuilder) {
            ((DynamicDialogueBuilder) builder).build(player);
        }
        start(builder.getDialogues(), index);
    }

    /**
     * Inserts all of the {@link Dialogue} entries to the map and then starts the
     * dialogue.
     * 
     * @param entries
     */
    private void start(Map<Integer, Dialogue> entries, int index) {
        reset();
        dialogues.putAll(entries);
        start(index);
    }

    /**
     * Starts the dialogue at the current {@code index}.
     */
    private void start() {
        final Dialogue dialogue = dialogues.get(index);
        if (dialogue == null) {
            player.getPacketSender().sendInterfaceRemoval();
            return;
        }
        dialogue.send(player);
    }

    /**
     * Attempts to handle a selected {@link DialogueOption}.
     * 
     * @param player
     * @param option
     */
    public void handleOption(Player player, DialogueOption option) {
        final Dialogue dialogue = dialogues.get(index);
        if (dialogue instanceof OptionsDialogue) {
            ((OptionsDialogue) dialogue).execute(option.ordinal(), player);
            return;
        }
        if (!(dialogue instanceof OptionDialogue)) {
            player.getPacketSender().sendInterfaceRemoval();
            return;
        }
        ((OptionDialogue) dialogue).execute(option);
    }
}
