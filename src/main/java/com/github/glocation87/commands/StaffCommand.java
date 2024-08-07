package com.github.glocation87.commands;

import com.github.glocation87.AdminToolPlugin;
import com.github.glocation87.manager.InventoryManager;
import com.github.glocation87.manager.GamemodeManager;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class StaffCommand implements CommandExecutor, Listener {
    private final AdminToolPlugin plugin;
    private final InventoryManager inventoryManager;
    private final GamemodeManager gamemodeManager;
    

    public StaffCommand(AdminToolPlugin _plugin) {
        this.plugin = _plugin;
        this.inventoryManager = _plugin.getInventoryManager();
        this.gamemodeManager = _plugin.getGamemodeManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (player.isOp()) {
                if (plugin.isPlayerInStaffMode(player)) {
                    exitStaffMode(player);
                } else {
                    enableStaffMode(player);
                }
            } else {
                player.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
            }
        }
        return true;
    }

    private void enableStaffMode(Player player) {
        plugin.setPlayerStaffMode(player, true);
        gamemodeManager.enableStaffGamemode(player);
        inventoryManager.cachePlayerInventory(player);
        inventoryManager.applyModerationTools(player);

        player.sendMessage(ChatColor.GREEN + "You have entered staff mode.");
    }

    private void exitStaffMode(Player player) {
        plugin.setPlayerStaffMode(player, false);
        gamemodeManager.disableStaffGamemode(player);
        inventoryManager.revertPlayerInventory(player);
        player.sendMessage(ChatColor.GREEN + "You have exited staff mode.");
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        exitStaffMode(player);
        inventoryManager.clearPlayerCache(player.getUniqueId());
    }

    
}
