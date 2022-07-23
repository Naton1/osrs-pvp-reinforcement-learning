package com.elvarg.game.entity.impl.playerbot.interaction;

import com.elvarg.game.GameConstants;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.entity.impl.playerbot.PlayerBot;
import com.elvarg.game.entity.impl.playerbot.commands.BotCommand;
import com.elvarg.game.model.ChatMessage;
import com.elvarg.util.Misc;

public class ChatInteraction {

    private static final int SPACE_LENGTH = 1;

    // The PlayerBot this interaction belongs to
    PlayerBot playerBot;

    public ChatInteraction(PlayerBot _playerBot) {
        this.playerBot = _playerBot;
    }

    public void heard(ChatMessage message, Player fromPlayer) {
        byte[] textByteArray = message.getText();
        String chatMessage = Misc.textUnpack(textByteArray, textByteArray.length).toLowerCase().trim();
        if(!chatMessage.contains(playerBot.getUsername().toLowerCase())) {
            // The player hasn't said the Bot's name, return
            return;
        }

        if (chatMessage.contains("stop") && this.playerBot.getActiveCommand() != null &&
                (fromPlayer == this.playerBot.getInteractingWith() || GameConstants.PLAYER_BOT_OVERRIDE.contains(fromPlayer.getRights()))) {
            playerBot.stopCommand();
        }

        BotCommand[] chatCommands = this.playerBot.getChatCommands();
        for (int i = 0; i < chatCommands.length; i++) {
            BotCommand command = chatCommands[i];
            for (String trigger : command.triggers()) {
                if (chatMessage.contains(trigger)) {
                    // Get params after trigger
                    int clipIndex = chatMessage.indexOf(trigger) + trigger.length() + SPACE_LENGTH;
                    String sub = chatMessage.length() > clipIndex ? chatMessage.substring(clipIndex) : chatMessage;
                    this.playerBot.startCommand(command, fromPlayer, sub.split(" ", 5));
                    return;
                }
            }
        }

    }
}
