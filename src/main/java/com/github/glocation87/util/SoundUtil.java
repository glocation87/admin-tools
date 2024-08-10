package com.github.glocation87.util;

import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class SoundUtil {

    // Prevent instantiation
    private SoundUtil() {}

    /**
     * Plays a sound to a player.
     *
     * @param player The player to play the sound to.
     * @param sound  The sound to play.
     * @param volume The volume of the sound.
     * @param pitch  The pitch of the sound.
     */
    public static void playSound(Player player, Sound sound, float volume, float pitch) {
        player.playSound(player.getLocation(), sound, volume, pitch);
    }

    /**
     * Plays a sound with default volume and pitch (1.0).
     *
     * @param player The player to play the sound to.
     * @param sound  The sound to play.
     */
    public static void playSound(Player player, Sound sound) {
        playSound(player, sound, 1.0f, 1.0f);
    }
}