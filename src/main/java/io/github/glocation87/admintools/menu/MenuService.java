package io.github.glocation87.admintools.menu;

import io.github.glocation87.admintools.AdminToolsPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public final class MenuService implements Listener {
    private final AdminToolsPlugin plugin;

    public MenuService(AdminToolsPlugin plugin) {
        this.plugin = plugin;
    }

    // Opening inside a click handler is undefined behaviour, so always defer a tick
    public void open(Player viewer, Menu menu) {
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            if (!viewer.isOnline()) {
                return;
            }
            menu.refresh();
            viewer.openInventory(menu.getInventory());
        });
    }

    public Menu current(Player viewer) {
        Inventory top = viewer.getOpenInventory().getTopInventory();
        InventoryHolder holder = top == null ? null : top.getHolder();
        return holder instanceof Menu menu ? menu : null;
    }

    public void tick() {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            Menu menu = current(player);
            if (menu != null && menu.live()) {
                menu.refresh();
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onClick(InventoryClickEvent event) {
        if (!(event.getView().getTopInventory().getHolder() instanceof Menu menu)) {
            return;
        }
        event.setCancelled(true);
        if (event.getClickedInventory() == menu.getInventory()) {
            menu.click(event.getRawSlot(), event.getClick());
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onDrag(InventoryDragEvent event) {
        if (event.getView().getTopInventory().getHolder() instanceof Menu) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (event.getInventory().getHolder() instanceof Menu menu) {
            menu.onClose();
        }
    }
}
