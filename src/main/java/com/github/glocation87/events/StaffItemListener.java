package com.github.glocation87.events;

import com.github.glocation87.AdminToolPlugin;


import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;


public class StaffItemListener implements Listener {

    private final AdminToolPlugin plugin;

    public StaffItemListener(AdminToolPlugin _plugin) {
        this.plugin = _plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player) {
            Player player = (Player) event.getWhoClicked();
            if (plugin.isPlayerInStaffMode(player)) {
                ItemStack item = event.getCurrentItem();
                if (item != null && plugin.isStaffItem(item)) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getWhoClicked() instanceof Player) {
            Player player = (Player) event.getWhoClicked();
            if (plugin.isPlayerInStaffMode(player)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        if (plugin.isPlayerInStaffMode(player)) {
            ItemStack item = event.getItemDrop().getItemStack();
            if (plugin.isStaffItem(item)) {
                event.setCancelled(true);
            }
        }
    }
}