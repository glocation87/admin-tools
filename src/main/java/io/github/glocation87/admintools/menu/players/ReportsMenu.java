package io.github.glocation87.admintools.menu.players;

import io.github.glocation87.admintools.AdminToolsPlugin;
import io.github.glocation87.admintools.Text;
import io.github.glocation87.admintools.data.Report;
import io.github.glocation87.admintools.menu.Icons;
import io.github.glocation87.admintools.menu.ListMenu;
import io.github.glocation87.admintools.menu.Menu;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

public final class ReportsMenu extends ListMenu<Report> {

    public ReportsMenu(AdminToolsPlugin plugin, Player viewer, Menu parent) {
        super(plugin, viewer, parent, "<red><b>Open Reports", 6);
    }

    @Override
    public boolean live() {
        return true;
    }

    @Override
    protected List<Report> items() {
        List<Report> reports = new ArrayList<>(plugin.data().reports());
        reports.sort((a, b) -> Long.compare(b.time(), a.time()));
        return reports;
    }

    @Override
    protected ItemStack icon(Report report) {
        OfflinePlayer target = plugin.getServer().getOfflinePlayer(report.target());
        List<String> lore = new ArrayList<>();
        lore.add("Reported by: <white>" + report.reporterName());
        lore.add("When: <white>" + Text.date(report.time()));
        lore.add("Online: <white>" + target.isOnline());
        lore.addAll(NotesMenu.wrap(Text.escape(report.reason())));
        lore.add("");
        lore.add("<yellow>Left: teleport to them");
        lore.add("<yellow>Right: punish menu");
        lore.add("<yellow>Shift + right: close the report");
        return Icons.head(target, "<red>" + report.targetName(), lore);
    }

    @Override
    protected void onClick(Report report, ClickType type) {
        OfflinePlayer target = plugin.getServer().getOfflinePlayer(report.target());
        if (type.isShiftClick() && type.isRightClick()) {
            plugin.data().removeReport(report.id());
            message("<gray>Report closed.");
            refresh();
        } else if (type.isRightClick()) {
            new PunishMenu(plugin, viewer, this, target).open();
        } else if (target.getPlayer() != null) {
            viewer.teleportAsync(target.getPlayer().getLocation());
            message("<gray>Teleported to <white>" + report.targetName());
        } else {
            message("<red>" + report.targetName() + " is offline.");
        }
    }
}
