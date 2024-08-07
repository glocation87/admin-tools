package com.github.glocation87.events;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import com.github.glocation87.AdminToolPlugin;
import com.github.glocation87.manager.PlayerListManager;

public class PlayerInteractListener implements Listener {
    private final AdminToolPlugin plugin;

    public PlayerInteractListener(AdminToolPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        String playerAction = event.getAction().toString();
        ItemStack playerItem = event.getItem();

        if (plugin.isPlayerInStaffMode(player)) {
            if (playerAction.contains("RIGHT_CLICK") && plugin.isStaffItem(playerItem)) {
                switch (plugin.getItemId(playerItem)) {
                    case "player_list":
                        PlayerListManager.openPlayerListUI(player, 1);
                        break;
                    default:
                        break;
                }
            }

            event.setCancelled(true);
        }
    }
}
