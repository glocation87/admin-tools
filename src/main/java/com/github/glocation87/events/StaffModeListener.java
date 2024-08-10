package com.github.glocation87.events;

import org.bukkit.GameMode;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import com.github.glocation87.AdminToolPlugin;
import com.github.glocation87.manager.PlayerListManager;
import com.github.glocation87.manager.PlayerProfileManager;
import com.github.glocation87.util.CustomInventoryHolder;

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
            player.sendMessage("You cannot change game mode while in staff mode.");
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
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player) {
            Player player = (Player) event.getDamager();
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
    public void onEntityTarget(EntityTargetEvent event) {
        Entity target = event.getTarget();
        if (target instanceof Player) {
            Player player = (Player) target;
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
    public void onBlockDamage(BlockDamageEvent event) {
        Player player = event.getPlayer();
        if (plugin.isPlayerInStaffMode(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player) {
            Player player = (Player) event.getWhoClicked();
            Inventory inventory = event.getInventory();
            InventoryHolder holder = inventory.getHolder();

            if (plugin.isPlayerInStaffMode(player)) {
                if (inventory.getType() == InventoryType.CREATIVE || 
                    inventory.getType() == InventoryType.CRAFTING ||
                    inventory.getType() == InventoryType.ENDER_CHEST
                )  {
                    event.setCancelled(true);
                } else if (holder instanceof CustomInventoryHolder) {
                    CustomInventoryHolder inventoryHolder = (CustomInventoryHolder) holder;
                    int slot = event.getRawSlot();
                    switch(inventoryHolder.getIdentifier()) {
                        case "player_list":
                            PlayerListManager.handleInventoryClick(player, holder, slot);
                            break;
                        case "player_main_menu":
                            PlayerProfileManager.handlePlayerMenuClick(player, holder, slot);
                            break;
                        default:
                            break;
                    }
                    event.setCancelled(true); // Prevent clicks in custom inventory slots from affecting other inventories
                } else {
                    ItemStack item = event.getCurrentItem();
                    if (item != null && plugin.isStaffItem(item)) {
                        event.setCancelled(true);
                        player.sendMessage("You cannot interact with other inventories while in staff mode.");
                    }
                }
            }
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getWhoClicked() instanceof Player) {
            Player player = (Player) event.getWhoClicked();
            Inventory inventory = event.getInventory();
            InventoryHolder holder = inventory.getHolder();

            if (plugin.isPlayerInStaffMode(player)) {
                if (inventory.getType() == InventoryType.CREATIVE || 
                    inventory.getType() == InventoryType.CRAFTING ||
                    inventory.getType() == InventoryType.ENDER_CHEST
                ) {
                    event.setCancelled(true);
                } 
                if (holder instanceof CustomInventoryHolder) {
                    event.setCancelled(true); // Prevent dragging in custom inventories
                } else {
                    event.setCancelled(true); // Prevent dragging in other inventories
                    player.sendMessage("You cannot drag items in other inventories while in staff mode.");
                }
            }
        }
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        if (plugin.isPlayerInStaffMode(player)) {
            event.setCancelled(true); // Prevent item dropping in other modes
            player.sendMessage("You cannot drop items while in staff mode.");  
        }
    }
}
