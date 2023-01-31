package com.elvarg.game.entity.impl.playerbot.interaction;

import com.elvarg.game.GameConstants;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.entity.impl.playerbot.PlayerBot;
import com.elvarg.game.entity.impl.playerbot.commands.BotCommand;
import com.elvarg.game.entity.impl.playerbot.commands.CommandType;
import com.elvarg.game.model.ChatMessage;
import com.elvarg.util.Misc;

import java.util.Arrays;

public class ChatInteraction {

    private static final int SPACE_LENGTH = 1;

    // The PlayerBot this interaction belongs to
    PlayerBot playerBot;

    public ChatInteraction(PlayerBot _playerBot) {
        this.playerBot = _playerBot;
    }

    /**
     * Called when a Player hears a message from a player within speaking distance.
     *
     * @param message
     * @param fromPlayer
     */
    public void heard(ChatMessage message, Player fromPlayer) {
        byte[] textByteArray = message.getText();
        String chatMessage = Misc.textUnpack(textByteArray, textByteArray.length).toLowerCase().trim();
        if(!chatMessage.startsWith(playerBot.getUsername().toLowerCase())) {
            // The player hasn't said the Bot's name, return
            return;
        }

        this.processCommand(chatMessage, fromPlayer, CommandType.PUBLIC_CHAT);
    }

    /**
     * Called when a Player bot receives an in game message via PacketSender.sendMessage()
     *
     * @param message
     */
    public void receivedGameMessage(String message) {
        if (this.playerBot.getInteractingWith() != null) {
            // If this bot is currently interacting with someone, no need to shout
            this.playerBot.getPacketSender().sendPrivateMessage(this.playerBot.getInteractingWith(), message.getBytes(), message.getBytes().length);
        }
    }

    /**
     * Called when a Player Bot receives a private message from a Player
     *
     * @param messageByteArray
     * @param fromPlayer
     */
    public void receivedPrivateMessage(byte[] messageByteArray, Player fromPlayer) {
        String chatMessage = Misc.textUnpack(messageByteArray, messageByteArray.length).toLowerCase().trim();
        this.processCommand(chatMessage, fromPlayer, CommandType.PRIVATE_CHAT);
    }

    /**
     * Method used to search a string for any player bot commands and action any found.
     *
     * @param chatMessage
     * @param fromPlayer
     * @param type
     */
    private void processCommand(String chatMessage, Player fromPlayer, CommandType type) {
        if (chatMessage.contains("stop")) {
            if(this.playerBot.getActiveCommand() != null &&
                    (fromPlayer == this.playerBot.getInteractingWith() || GameConstants.PLAYER_BOT_OVERRIDE.contains(fromPlayer.getRights()))) {
                playerBot.stopCommand();
            }

            // If the player is currently under attack from the Bot, stop combat
            if(this.playerBot.getCombat().getAttacker() == fromPlayer) {
                this.playerBot.getCombat().setUnderAttack(null);
            }

            return;
        }

        BotCommand[] chatCommands = this.playerBot.getChatCommands();
        for (int i = 0; i < chatCommands.length; i++) {
            BotCommand command = chatCommands[i];
            for (String trigger : command.triggers()) {
                if (!chatMessage.contains(trigger)) {
                    // Command wasn't triggered
                    continue;
                }

                if (!Arrays.asList(command.supportedTypes()).contains(type)) {
                    // Command was triggered, but method not supported
                    fromPlayer.getPacketSender().sendMessage("Sorry, this bot command can't be delivered via " + type.getLabel() + ".");
                    return;
                }

                // Get params after trigger
                int clipIndex = chatMessage.indexOf(trigger) + trigger.length() + SPACE_LENGTH;
                String sub = chatMessage.length() > clipIndex ? chatMessage.substring(clipIndex) : chatMessage;
                this.playerBot.startCommand(command, fromPlayer, sub.split(" ", 5));
                return; // Don't process any more commands
            }
        }
    }
}
