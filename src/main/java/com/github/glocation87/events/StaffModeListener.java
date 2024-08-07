package com.github.glocation87.events;

import com.github.glocation87.AdminToolPlugin;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.ChatColor;

public class StaffModeListener implements Listener {

    private final AdminToolPlugin plugin;

    public StaffModeListener(AdminToolPlugin _plugin) {
        this.plugin = _plugin;
    }

    @EventHandler
    public void onGameModeChange(PlayerGameModeChangeEvent event) {
        Player player = event.getPlayer();
        if (plugin.isPlayerInStaffMode(player)) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "You cannot change game mode while in staff mode.");
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            if (plugin.isPlayerInStaffMode(player)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onEntityPickup(EntityPickupItemEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            if (plugin.isPlayerInStaffMode(player)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (plugin.isPlayerInStaffMode(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockDamaged(BlockDamageEvent event) {
        Player player = event.getPlayer();
        if (plugin.isPlayerInStaffMode(player)) {
            event.setCancelled(true);
        }
    }
}
