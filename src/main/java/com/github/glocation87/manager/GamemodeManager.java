package com.github.glocation87.manager;

import org.bukkit.GameMode;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;

import com.github.glocation87.AdminToolPlugin;

public class GamemodeManager {

    private final AdminToolPlugin plugin;

    public GamemodeManager(AdminToolPlugin _plugin) {
        this.plugin = _plugin;
    }

    public void enableStaffGamemode(Player player) {
        player.setGameMode(GameMode.CREATIVE);
    }

    public void disableStaffGamemode(Player player) {
        double maxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
        player.setGameMode(GameMode.SURVIVAL);
        player.setHealth(maxHealth);
    }
}
