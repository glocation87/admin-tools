package com.github.glocation87.manager;

import com.github.glocation87.AdminToolPlugin;

import org.bukkit.entity.Player;
import org.bukkit.GameMode;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.attribute.Attribute;

public class GamemodeManager {
    private final AdminToolPlugin plugin;

    public GamemodeManager(AdminToolPlugin _plugin) {
        this.plugin = _plugin;
    }

    public void enableStaffGamemode(Player player) {
        if (plugin.isPlayerInStaffMode(player)) {
            double maxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();

            player.setGameMode(GameMode.SURVIVAL);
            player.setAllowFlight(true);
            player.setFlying(true);
            player.setInvulnerable(true);
            player.setHealth(maxHealth);
            player.setFoodLevel(20);
            player.setSaturation(20); 
        }
    }

    public void disableStaffGamemode(Player player) { 
        player.setGameMode(GameMode.SURVIVAL);
        player.setAllowFlight(false);
        player.setFlying(false);
        player.setInvulnerable(false);
        player.setHealth(player.getMaxHealth());
    }
}
