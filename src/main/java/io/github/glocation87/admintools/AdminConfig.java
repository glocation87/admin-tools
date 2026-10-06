package io.github.glocation87.admintools;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.bukkit.GameMode;
import org.bukkit.configuration.file.FileConfiguration;

public record AdminConfig(GameMode staffGameMode, boolean vanishOnEnter, boolean fly, int refreshTicks,
                          Set<String> frozenAllowedCommands, Duration defaultMute, int hotChunks,
                          List<Duration> chatLadder, List<Duration> gameplayLadder, double escalation, int permAfter) {

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
        return new AdminConfig(mode,
            config.getBoolean("staff-mode.vanish-on-enter", true),
            config.getBoolean("staff-mode.fly", true),
            Math.max(1, config.getInt("staff-mode.refresh-ticks", 20)),
            allowed,
            duration(config.getString("mute.default-duration", "30m"), Duration.ofMinutes(30)),
            Math.max(1, config.getInt("hot-chunks.count", 21)),
            ladder(config.getStringList("punish.chat"), "1h", "1d", "7d"),
            ladder(config.getStringList("punish.gameplay"), "1d", "7d", "30d"),
            Math.max(1, config.getDouble("punish.escalation", 2)),
            Math.max(1, config.getInt("punish.perm-after", 4)));
    }

    private static Duration duration(String text, Duration fallback) {
        try {
            Duration parsed = Durations.parse(text);
            return parsed == null ? fallback : parsed;
        } catch (IllegalArgumentException e) {
            return fallback;
        }
    }

    private static List<Duration> ladder(List<String> configured, String... defaults) {
        List<String> source = configured.size() >= 3 ? configured : List.of(defaults);
        List<Duration> out = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            out.add(duration(source.get(i), Durations.parse(defaults[i])));
        }
        return List.copyOf(out);
    }
}
