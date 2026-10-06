package io.github.glocation87.admintools;

import java.time.Duration;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import org.bukkit.GameMode;
import org.bukkit.configuration.file.FileConfiguration;

public record AdminConfig(GameMode staffGameMode, boolean vanishOnEnter, boolean fly, int refreshTicks,
                          Set<String> frozenAllowedCommands, Duration defaultMute, int hotChunks) {

    public static AdminConfig from(FileConfiguration config) {
        GameMode mode;
        try {
            mode = GameMode.valueOf(config.getString("staff-mode.gamemode", "CREATIVE").toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            mode = GameMode.CREATIVE;
        }
        Set<String> allowed = new HashSet<>();
        for (String command : config.getStringList("freeze.allowed-commands")) {
            allowed.add(command.toLowerCase(Locale.ROOT));
        }
        Duration mute;
        try {
            mute = Durations.parse(config.getString("mute.default-duration", "30m"));
        } catch (IllegalArgumentException e) {
            mute = Duration.ofMinutes(30);
        }
        return new AdminConfig(mode,
            config.getBoolean("staff-mode.vanish-on-enter", true),
            config.getBoolean("staff-mode.fly", true),
            Math.max(1, config.getInt("staff-mode.refresh-ticks", 20)),
            allowed,
            mute,
            Math.max(1, config.getInt("hot-chunks.count", 21)));
    }
}
