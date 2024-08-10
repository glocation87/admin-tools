package com.github.glocation87.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;

import com.github.glocation87.AdminToolPlugin;
import com.github.glocation87.manager.GamemodeManager;
import com.github.glocation87.manager.InventoryManager;

public class StaffCommand implements CommandExecutor, Listener {
    private final AdminToolPlugin plugin;
    private final InventoryManager inventoryManager;
    private final GamemodeManager gamemodeManager;
    private final BossBar bossBar;

    public StaffCommand(AdminToolPlugin _plugin) {
        this.plugin = _plugin;
        this.inventoryManager = _plugin.getInventoryManager();
        this.gamemodeManager = _plugin.getGamemodeManager();

        bossBar = Bukkit.createBossBar(
            ChatColor.GREEN + "§lStaff Mode",
            BarColor.GREEN,
            BarStyle.SOLID
        );
        
        bossBar.setProgress(1.0);
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
        gamemodeManager.enableStaffGamemode(player);
        inventoryManager.cachePlayerInventory(player);
        inventoryManager.applyModerationTools(player);
        bossBar.addPlayer(player);
        plugin.setPlayerStaffMode(player, true);
        player.sendMessage(ChatColor.GREEN + "You have entered staff mode.");
    }

    private void exitStaffMode(Player player) {
        plugin.setPlayerStaffMode(player, false);
        gamemodeManager.disableStaffGamemode(player);
        inventoryManager.revertPlayerInventory(player);
        bossBar.removePlayer(player);
        player.sendMessage(ChatColor.GREEN + "You have exited staff mode.");
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        exitStaffMode(player);
        inventoryManager.clearPlayerCache(player.getUniqueId());
    }

}
