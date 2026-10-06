package io.github.glocation87.admintools;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;

public final class Text {
    public static final String PREFIX = "<dark_gray>[<gold>Admin</gold>]</dark_gray> ";

    private static final MiniMessage MINI = MiniMessage.miniMessage();
    private static final PlainTextComponentSerializer PLAIN = PlainTextComponentSerializer.plainText();
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneId.systemDefault());

    private Text() {
    }

    public static Component mm(String input, TagResolver... resolvers) {
        return MINI.deserialize(input, resolvers);
    }

    public static Component item(String input, TagResolver... resolvers) {
        return mm(input, resolvers).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE);
    }

    public static String plain(Component component) {
        return component == null ? "" : PLAIN.serialize(component);
    }

    public static String escape(String raw) {
        return MINI.escapeTags(raw);
    }

    public static void send(CommandSender to, String mini, TagResolver... resolvers) {
        to.sendMessage(mm(PREFIX + mini, resolvers));
    }

    public static String number(double value) {
        return String.format(Locale.ROOT, "%.1f", value);
    }

    public static String coords(Location at) {
        String world = at.getWorld() == null ? "?" : at.getWorld().getName();
        return world + " " + at.getBlockX() + ", " + at.getBlockY() + ", " + at.getBlockZ();
    }

    public static String date(long epochMillis) {
        return DATE.format(Instant.ofEpochMilli(epochMillis));
    }

    public static String color(double value, double warn, double bad) {
        if (value >= bad) {
            return "<red>";
        }
        return value >= warn ? "<yellow>" : "<green>";
    }

    // Inverted thresholds, for values like TPS where lower is worse
    public static String colorLow(double value, double warn, double bad) {
        if (value <= bad) {
            return "<red>";
        }
        return value <= warn ? "<yellow>" : "<green>";
    }

    // One bar per sample, colored by how close it is to the thresholds
    public static String history(List<Double> samples, double warn, double bad) {
        StringBuilder out = new StringBuilder();
        for (double sample : samples) {
            out.append(color(sample, warn, bad)).append('|');
        }
        return out.toString();
    }

    public static String gauge(double fraction, int width) {
        int filled = (int) Math.round(Math.max(0, Math.min(1, fraction)) * width);
        return color(fraction, 0.7, 0.9) + "|".repeat(filled) + "<dark_gray>" + "|".repeat(width - filled);
    }
}
