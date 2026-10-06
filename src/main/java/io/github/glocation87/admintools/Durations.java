package io.github.glocation87.admintools;

import java.time.Duration;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Durations {
    private static final Pattern PART = Pattern.compile("(\d+)([smhdw])");

    private Durations() {
    }

    // "1d12h", "30m", "perm". Returns null for permanent, throws on garbage
    public static Duration parse(String input) {
        String text = input.trim().toLowerCase(Locale.ROOT);
        if (text.equals("perm") || text.equals("permanent") || text.equals("forever")) {
            return null;
        }
        Matcher matcher = PART.matcher(text);
        Duration total = Duration.ZERO;
        int end = 0;
        while (matcher.find()) {
            if (matcher.start() != end) {
                throw new IllegalArgumentException("bad duration: " + input);
            }
            long amount = Long.parseLong(matcher.group(1));
            total = total.plus(switch (matcher.group(2)) {
                case "s" -> Duration.ofSeconds(amount);
                case "m" -> Duration.ofMinutes(amount);
                case "h" -> Duration.ofHours(amount);
                case "d" -> Duration.ofDays(amount);
                default -> Duration.ofDays(amount * 7);
            });
            end = matcher.end();
        }
        if (end == 0 || end != text.length()) {
            throw new IllegalArgumentException("bad duration: " + input);
        }
        return total;
    }

    public static String format(Duration duration) {
        if (duration == null) {
            return "permanent";
        }
        long seconds = Math.max(0, duration.toSeconds());
        if (seconds < 60) {
            return seconds + "s";
        }
        StringBuilder out = new StringBuilder();
        long days = seconds / 86400;
        long hours = seconds % 86400 / 3600;
        long minutes = seconds % 3600 / 60;
        if (days > 0) {
            out.append(days).append("d ");
        }
        if (hours > 0) {
            out.append(hours).append("h ");
        }
        if (minutes > 0 && days == 0) {
            out.append(minutes).append("m");
        }
        return out.toString().trim();
    }

    public static String remaining(long expiresAt) {
        return expiresAt < 0 ? "permanent" : format(Duration.ofMillis(expiresAt - System.currentTimeMillis()));
    }
}
