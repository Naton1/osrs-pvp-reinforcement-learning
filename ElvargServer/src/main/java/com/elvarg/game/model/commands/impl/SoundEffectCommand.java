package com.elvarg.game.model.commands.impl;

import com.elvarg.game.Sounds;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.Graphic;
import com.elvarg.game.model.commands.Command;
import com.elvarg.game.model.rights.PlayerRights;

public class SoundEffectCommand implements Command {

    @Override
    public void execute(Player player, String command, String[] parts) {
        int soundId = Integer.parseInt(parts[1]);
        Sounds.sendSound(player, soundId, 0,0,2);
    }

    @Override
    public boolean canUse(Player player) {
        PlayerRights rights = player.getRights();
        return (rights == PlayerRights.OWNER || rights == PlayerRights.DEVELOPER);
    }

}
