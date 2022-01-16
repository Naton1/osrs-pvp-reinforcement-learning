package com.elvarg.game;

import com.elvarg.game.entity.impl.player.Player;

public class Sounds {
    public static void sendSound(Player player, Sound sound) {
        sendSound(player, sound.getId(), 0, sound.getDelay(), sound.getVolume());
    }

    public static void sendSound(Player player, int soundId, int loopType, int delay, int volume) {
        player.getPacketSender().sendSoundEffect(soundId, loopType, delay, volume);;
    }
}