package com.elvarg.game.content.sound;

import com.elvarg.game.entity.impl.player.Player;
import org.apache.commons.lang.StringUtils;

import java.util.Optional;

public class SoundManager {

    public static void sendSound(Player player, Sound sound) {
        if (player == null || sound == null || player.isPlayerBot()) {
            return;
        }

        sendSound(player, sound.getId(), sound.getLoopType(), sound.getDelay(), sound.getVolume());
    }

    public static void sendSound(Player player, int soundId, int loopType, int delay, int volume) {
        player.getPacketSender().sendSoundEffect(soundId, loopType, delay, volume);
    }

    /**
     * Handles music when the player changes to a new region.
     *
     * @param player
     */
    public static void onRegionChange(Player player) {
        int regionId = player.getRegionId();
        Optional<Music> regionMusic = Music.getForRegion(regionId);

        regionMusic.ifPresent(music -> {
            player.getPacketSender().sendString(42538, StringUtils.capitalize(music.getSongName()));
            player.getPacketSender().playMusic(music.getSongId()); });
    }
}