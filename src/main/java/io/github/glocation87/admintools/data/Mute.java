package io.github.glocation87.admintools.data;

// expiresAt below zero means permanent
public record Mute(long expiresAt, String reason, String by) {

    public boolean expired() {
        return expiresAt >= 0 && System.currentTimeMillis() >= expiresAt;
    }
}
