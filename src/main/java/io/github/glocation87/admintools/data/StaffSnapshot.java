package io.github.glocation87.admintools.data;

import java.util.Base64;
import java.util.Locale;
import org.bukkit.GameMode;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

// Everything staff mode takes away from a player, so it can be handed back
public record StaffSnapshot(ItemStack[] contents, GameMode gameMode, boolean allowFlight, boolean flying) {

    public static StaffSnapshot capture(Player player) {
        ItemStack[] contents = player.getInventory().getContents();
        ItemStack[] copy = new ItemStack[contents.length];
        for (int slot = 0; slot < contents.length; slot++) {
            copy[slot] = contents[slot] == null ? null : contents[slot].clone();
        }
        return new StaffSnapshot(copy, player.getGameMode(), player.getAllowFlight(), player.isFlying());
    }

    public void restore(Player player) {
        player.getInventory().setContents(contents);
        player.setGameMode(gameMode);
        player.setAllowFlight(allowFlight);
        player.setFlying(allowFlight && flying);
    }

    public void write(ConfigurationSection section) {
        section.set("items", Base64.getEncoder().encodeToString(ItemStack.serializeItemsAsBytes(contents)));
        section.set("gamemode", gameMode.name());
        section.set("allow-flight", allowFlight);
        section.set("flying", flying);
    }

    public static StaffSnapshot read(ConfigurationSection section) {
        byte[] bytes = Base64.getDecoder().decode(section.getString("items", ""));
        ItemStack[] contents = bytes.length == 0 ? new ItemStack[41] : ItemStack.deserializeItemsFromBytes(bytes);
        GameMode mode;
        try {
            mode = GameMode.valueOf(section.getString("gamemode", "SURVIVAL").toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            mode = GameMode.SURVIVAL;
        }
        return new StaffSnapshot(contents, mode, section.getBoolean("allow-flight"), section.getBoolean("flying"));
    }
}
