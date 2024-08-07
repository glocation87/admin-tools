package com.github.glocation87.events;

import com.github.glocation87.AdminToolPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.entity.Player;


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
    }
}
