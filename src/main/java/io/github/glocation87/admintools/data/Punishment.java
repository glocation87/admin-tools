package io.github.glocation87.admintools.data;

import java.util.Locale;

// expiresAt below zero is permanent, zero means instant (warn, kick)
public record Punishment(Type type, Category category, int severity, String by, long time, long expiresAt, String reason) {

    public enum Type { WARN, KICK, MUTE, BAN }

    public enum Category { CHAT, GAMEPLAY, OTHER }

    public boolean active() {
        return (type == Type.MUTE || type == Type.BAN) && (expiresAt < 0 || System.currentTimeMillis() < expiresAt);
    }

    public String label() {
        String name = type.name().toLowerCase(Locale.ROOT);
        return severity > 0 ? name + " " + severity : name;
    }
}
