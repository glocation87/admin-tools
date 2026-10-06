package io.github.glocation87.admintools.menu.players;

import io.github.glocation87.admintools.AdminToolsPlugin;
import io.github.glocation87.admintools.Durations;
import io.github.glocation87.admintools.Text;
import io.github.glocation87.admintools.data.Punishment;
import io.github.glocation87.admintools.menu.Icons;
import io.github.glocation87.admintools.menu.ListMenu;
import io.github.glocation87.admintools.menu.Menu;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

public final class HistoryMenu extends ListMenu<Punishment> {
    private final OfflinePlayer target;

    public HistoryMenu(AdminToolsPlugin plugin, Player viewer, Menu parent, OfflinePlayer target) {
        super(plugin, viewer, parent, "<aqua><b>History: " + target.getName(), 5);
        this.target = target;
    }

    @Override
    protected List<Punishment> items() {
        return plugin.punishments().history(target.getUniqueId());
    }

    @Override
    protected ItemStack icon(Punishment punishment) {
        Material icon = switch (punishment.type()) {
            case WARN -> Material.YELLOW_WOOL;
            case KICK -> Material.IRON_DOOR;
            case MUTE -> Material.NAME_TAG;
            case BAN -> Material.ANVIL;
        };
        List<String> lore = new ArrayList<>();
        lore.add("Category: <white>" + punishment.category().name().toLowerCase(Locale.ROOT));
        lore.add("By: <white>" + punishment.by() + " <gray>on <white>" + Text.date(punishment.time()));
        if (punishment.type() == Punishment.Type.MUTE || punishment.type() == Punishment.Type.BAN) {
            lore.add("Length: <white>" + (punishment.expiresAt() < 0 ? "permanent" : Durations.format(java.time.Duration.ofMillis(punishment.expiresAt() - punishment.time()))));
            lore.add(punishment.active() ? "<red>Active, " + Durations.remaining(punishment.expiresAt()) + " left" : "<dark_gray>Expired");
        }
        lore.addAll(NotesMenu.wrap(Text.escape(punishment.reason())));
        if (viewer.hasPermission("admintools.admin")) {
            lore.add("");
            lore.add("<dark_gray>Shift + right click to erase");
        }
        String color = punishment.active() ? "<red>" : "<gray>";
        return Icons.of(icon, color + punishment.label(), lore);
    }

    @Override
    protected void onClick(Punishment punishment, ClickType type) {
        if (type.isShiftClick() && type.isRightClick() && viewer.hasPermission("admintools.admin")) {
            plugin.data().removePunishment(target.getUniqueId(), punishment);
            refresh();
        }
    }
}
