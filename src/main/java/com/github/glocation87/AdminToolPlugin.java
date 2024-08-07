package com.github.glocation87;

import com.github.glocation87.commands.StaffCommand;
import com.github.glocation87.manager.InventoryManager;

import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import com.github.glocation87.manager.GamemodeManager;
import com.github.glocation87.events.StaffItemListener;
import com.github.glocation87.events.StaffModeListener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;


public class AdminToolPlugin extends JavaPlugin
{
    private InventoryManager inventoryManager;
    private GamemodeManager gamemodeManager;
    private Set<UUID> staffModePlayers;

    @Override
    public void onEnable() {
        gamemodeManager = new GamemodeManager(this);
        inventoryManager = new InventoryManager(this);
        staffModePlayers = new HashSet<>();

        registerCommands();
        registerEvents();
    }

    @Override
    public void onDisable() {
        getLogger().info("See you again, SpigotMC!");
    }
    private void registerEvents() {
        getServer().getPluginManager().registerEvents(new StaffModeListener(this), this);
        getServer().getPluginManager().registerEvents(new StaffItemListener(this), this);
    }

    private void registerCommands() {
        getCommand("staff").setExecutor(new StaffCommand(this));
    }

    public boolean isPlayerInStaffMode(Player player) {
        return staffModePlayers.contains(player.getUniqueId());
    }

    public void setPlayerStaffMode(Player player, boolean inStaffMode) {
        if (inStaffMode) {
            staffModePlayers.add(player.getUniqueId());
        } else {
            staffModePlayers.remove(player.getUniqueId());
        }
    }

    public boolean isStaffItem(ItemStack item) {
        if (item.hasItemMeta()) {
            NamespacedKey key = new NamespacedKey(plugin, "staff_item");
            PersistentDataContainer container = item.getItemMeta().getPersistentDataContainer();
            if (container.has(key, PersistentDataType.STRING)) {
                String id = container.get(key, PersistentDataType.STRING);
                return id.equals("player_list") 
                    || id.equals("moderation_tool") 
                    || id.equals("teleport");
            }
        }
        return false;
    }

    public InventoryManager getInventoryManager() {
        return inventoryManager;
    }

    public GamemodeManager getGamemodeManager() {
        return gamemodeManager;
    }
}
