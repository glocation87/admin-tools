package com.github.glocation87.events;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import com.github.glocation87.AdminToolPlugin;
import com.github.glocation87.manager.PlayerListManager;
import com.github.glocation87.util.CustomInventoryHolder;

public class StaffItemListener implements Listener {

    private final AdminToolPlugin plugin;

    public StaffItemListener(AdminToolPlugin _plugin) {
        this.plugin = _plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player) {
            Player player = (Player) event.getWhoClicked();
            InventoryHolder holder = event.getInventory().getHolder();

            if (holder instanceof CustomInventoryHolder) {
                CustomInventoryHolder inventoryHolderProto = (CustomInventoryHolder) holder;
                switch (inventoryHolderProto.getIdentifier()) {
                    case "player_list":
                        int slot = event.getRawSlot();
                        PlayerListManager.handleInventoryClick(player, holder, slot);
                        break;
                    default:
                        break;
                }
                event.setCancelled(true);
            } else {
                if (plugin.isPlayerInStaffMode(player)) {
                    ItemStack item = event.getCurrentItem();
                    if (item != null && plugin.isStaffItem(item)) {
                        event.setCancelled(true);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getWhoClicked() instanceof Player) {
            Player player = (Player) event.getWhoClicked();
            InventoryHolder holder = event.getInventory().getHolder();

            if (holder instanceof CustomInventoryHolder) {
                event.setCancelled(true);
            } else {
                if (plugin.isPlayerInStaffMode(player)) {
                    event.setCancelled(true);
                }
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