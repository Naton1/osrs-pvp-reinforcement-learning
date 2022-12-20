package com.elvarg.game.model.commands;

import com.elvarg.game.entity.impl.player.Player;

/**
 * @author Ynneh | 20/12/2022 - 13:40
 * <https://github.com/drhenny>
 */
public class MusicCommand implements Command {
    @Override
    public void execute(Player player, String command, String[] parts) {
        try {
            int musicId = Integer.valueOf(parts[1]);
            player.getPacketSender().playMusic(musicId);
            player.getPacketSender().sendMessage("Playing musicId="+musicId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean canUse(Player player) {
        return true;
    }
}
