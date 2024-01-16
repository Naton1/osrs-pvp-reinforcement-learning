package com.elvarg.game.model.commands.impl;

import com.elvarg.game.content.sound.SoundManager;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.commands.Command;
import com.elvarg.game.model.rights.PlayerRights;

public class SoundEffectCommand implements Command {

    @Override
    public void execute(Player player, String command, String[] parts) {
        int soundId = Integer.parseInt(parts[1]);
        int delay = parts.length == 3 ? Integer.parseInt(parts[2]) : 0;
        int loopType = parts.length == 4 ? Integer.parseInt(parts[3]) : 0;
        int volume = parts.length == 5 ? Integer.parseInt(parts[4]) : 2;
        SoundManager.sendSound(player, soundId, loopType, delay, volume);
    }

    @Override
    public boolean canUse(Player player) {
        PlayerRights rights = player.getRights();
        return (rights == PlayerRights.OWNER || rights == PlayerRights.DEVELOPER);
    }

}
