package com.elvarg.game;

import com.elvarg.game.entity.impl.player.Player;

public class Sounds {
    public static void sendSound(Player player, Sound sound) {
        if (player == null || sound == null || player.isPlayerBot()) {
            return;
        }

        sendSound(player, sound.getId(), sound.getLoopType(), sound.getDelay(), sound.getVolume());
    }

    public static void sendSound(Player player, int soundId, int loopType, int delay, int volume) {
        player.getPacketSender().sendSoundEffect(soundId, loopType, delay, volume);
    }
}