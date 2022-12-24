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

    // Called when a Player hears a message from a player within speaking distance
    public void heard(ChatMessage message, Player fromPlayer) {
        byte[] textByteArray = message.getText();
        String chatMessage = Misc.textUnpack(textByteArray, textByteArray.length).toLowerCase().trim();
        if(!chatMessage.startsWith(playerBot.getUsername().toLowerCase())) {
            // The player hasn't said the Bot's name, return
            return;
        }

        if (chatMessage.contains("stop")) {
            if(this.playerBot.getActiveCommand() != null &&
                    (fromPlayer == this.playerBot.getInteractingWith() || GameConstants.PLAYER_BOT_OVERRIDE.contains(fromPlayer.getRights()))) {
                playerBot.stopCommand();
            }

            // If the player is currently under attack from the Bot, stop combat
            if(this.playerBot.getCombat().getAttacker() == fromPlayer) {
                this.playerBot.getCombat().setUnderAttack(null);
            }
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

    // Called when a Player bot receives an in game message via PacketSender.sendMessage()
    public void receivedGameMessage(String message) {
        if (this.playerBot.getInteractingWith() != null) {
            // If this bot is currently interacting with someone, no need to shout
            this.playerBot.getPacketSender().sendPrivateMessage(this.playerBot.getInteractingWith(), message.getBytes(), message.getBytes().length);
        }
    }
}
