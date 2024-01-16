package com.elvarg.game.model.commands.impl;

import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.commands.Command;
import com.elvarg.util.PasswordUtil;

public class ChangePassword implements Command {

    @Override
    public void execute(Player player, String command, String[] parts) {

        // Known exploit
        if (command.contains("\r") || command.contains("\n")) {
            return;
        }

        String pass = command.substring(parts[0].length() + 1);
        if (pass.length() > 3 && pass.length() < 20) {
            player.setPasswordHashWithSalt(PasswordUtil.generatePasswordHashWithSalt(pass));
            player.getPacketSender().sendMessage("Your password is now: " + pass);
        } else {
            player.getPacketSender().sendMessage("Invalid password input.");
        }
    }

    @Override
    public boolean canUse(Player player) {
        return true;
    }
}
