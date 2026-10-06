package io.github.glocation87.admintools.staff;

import io.github.glocation87.admintools.AdminToolsPlugin;
import io.github.glocation87.admintools.Text;
import io.github.glocation87.admintools.menu.Menu;
import io.github.glocation87.admintools.menu.players.PeekMenu;
import io.github.glocation87.admintools.menu.players.PlayerListMenu;
import io.github.glocation87.admintools.menu.players.PlayerMenu;
import io.papermc.paper.event.player.PrePlayerAttackEntityEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.EquipmentSlot;

public final class StaffListener implements Listener {
    private final AdminToolsPlugin plugin;

    public StaffListener(AdminToolsPlugin plugin) {
        this.plugin = plugin;
    }

    private boolean staff(Player player) {
        return plugin.staffMode().active(player);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (!staff(player)) {
            return;
        }
        event.setCancelled(true);
        if (event.getHand() != EquipmentSlot.HAND || !event.getAction().isRightClick()) {
            return;
        }
        plugin.staffMode().items().of(event.getItem()).ifPresent(item -> use(player, item, event.getClickedBlock()));
    }

    @EventHandler
    public void onInteractEntity(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        if (!staff(player)) {
            return;
        }
        event.setCancelled(true);
        if (event.getHand() == EquipmentSlot.HAND) {
            plugin.staffMode().items().of(player.getInventory().getItemInMainHand())
                .ifPresent(item -> useOnEntity(player, item, event.getRightClicked()));
        }
    }

    @EventHandler
    public void onAttack(PrePlayerAttackEntityEvent event) {
        Player player = event.getPlayer();
        if (!staff(player)) {
            return;
        }
        event.setCancelled(true);
        plugin.staffMode().items().of(player.getInventory().getItemInMainHand())
            .ifPresent(item -> useOnEntity(player, item, event.getAttacked()));
    }

    private void use(Player player, StaffItem item, Block block) {
        switch (item) {
            case PLAYER_LIST -> new PlayerListMenu(plugin, player).open();
            case VANISH -> plugin.vanish().toggle(player);
            case RANDOM_TP -> randomTeleport(player);
            case COMMAND_SPY -> plugin.commandSpy().toggle(player);
            case EXIT -> plugin.staffMode().exit(player);
            default -> {
            }
        }
    }

    private void useOnEntity(Player player, StaffItem item, Entity target) {
        switch (item) {
            case PEEK -> {
                if (target instanceof Player other) {
                    PeekMenu.inventory(plugin, player, null, other).open();
                }
            }
            case FREEZE_WAND -> {
                if (target instanceof Player other) {
                    plugin.freeze().toggle(other, player);
                }
            }
            case PLAYER_LIST -> {
                if (target instanceof Player other) {
                    new PlayerMenu(plugin, player, null, other).open();
                }
            }
            default -> {
            }
        }
    }

    private void randomTeleport(Player player) {
        List<Player> candidates = new ArrayList<>();
        for (Player other : plugin.getServer().getOnlinePlayers()) {
            if (other != player && !staff(other)) {
                candidates.add(other);
            }
        }
        if (candidates.isEmpty()) {
            Text.send(player, "<red>Nobody else is online.");
            return;
        }
        Player target = candidates.get(ThreadLocalRandom.current().nextInt(candidates.size()));
        player.teleportAsync(target.getLocation());
        Text.send(player, "<gray>Teleported to <white>" + target.getName());
    }

    @EventHandler(ignoreCancelled = true)
    public void onBreak(BlockBreakEvent event) {
        if (staff(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent event) {
        if (staff(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player player && staff(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player player && staff(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPickup(EntityPickupItemEvent event) {
        if (event.getEntity() instanceof Player player && staff(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onDrop(PlayerDropItemEvent event) {
        if (staff(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onTarget(EntityTargetEvent event) {
        if (event.getTarget() instanceof Player player && (staff(player) || plugin.vanish().vanished(player))) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onHunger(FoodLevelChangeEvent event) {
        if (event.getEntity() instanceof Player player && staff(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onGameMode(PlayerGameModeChangeEvent event) {
        if (staff(event.getPlayer()) && event.getCause() != PlayerGameModeChangeEvent.Cause.PLUGIN) {
            event.setCancelled(true);
            Text.send(event.getPlayer(), "<red>Leave staff mode first.");
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onSwapHands(PlayerSwapHandItemsEvent event) {
        if (staff(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    // Menus handle their own clicks, anything else is locked down so the staff hotbar stays intact
    @EventHandler(priority = EventPriority.LOW)
    public void onClick(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player player && staff(player)
            && !(event.getView().getTopInventory().getHolder() instanceof Menu)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onDrag(InventoryDragEvent event) {
        if (event.getWhoClicked() instanceof Player player && staff(player)
            && !(event.getView().getTopInventory().getHolder() instanceof Menu)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        plugin.staffMode().restoreCrashed(event.getPlayer());
        plugin.vanish().handleJoin(event.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        plugin.staffMode().exit(event.getPlayer());
        plugin.vanish().handleQuit(event.getPlayer());
    }
}
