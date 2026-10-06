package io.github.glocation87.admintools.menu.server;

import io.github.glocation87.admintools.AdminToolsPlugin;
import io.github.glocation87.admintools.Text;
import io.github.glocation87.admintools.menu.Icons;
import io.github.glocation87.admintools.menu.ListMenu;
import io.github.glocation87.admintools.menu.Menu;
import io.github.glocation87.admintools.staff.StaffChat;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

public final class StaffMenu extends ListMenu<Player> {

    public StaffMenu(AdminToolsPlugin plugin, Player viewer, Menu parent) {
        super(plugin, viewer, parent, "<gold><b>Staff On Duty", 4);
    }

    @Override
    public boolean live() {
        return true;
    }

    @Override
    protected List<Player> items() {
        List<Player> staff = new ArrayList<>();
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            if (player.hasPermission(StaffChat.PERMISSION)) {
                staff.add(player);
            }
        }
        return staff;
    }

    @Override
    protected ItemStack icon(Player player) {
        List<String> lore = new ArrayList<>();
        lore.add("Where: <white>" + Text.coords(player.getLocation()));
        lore.add("Ping: <white>" + player.getPing() + "ms");
        lore.add("Staff mode: <white>" + plugin.staffMode().active(player));
        lore.add("Vanished: <white>" + plugin.vanish().vanished(player));
        lore.add("Command spy: <white>" + plugin.commandSpy().spying(player));
        lore.add("Staff chat: <white>" + plugin.staffChat().toggled(player));
        lore.add("");
        lore.add("<yellow>Click to teleport to them");
        String color = plugin.staffMode().active(player) ? "<gold>" : "<gray>";
        return Icons.head(player, color + player.getName(), lore);
    }

    @Override
    protected void onClick(Player player, ClickType type) {
        if (player.isOnline()) {
            viewer.teleportAsync(player.getLocation());
        }
    }
}
