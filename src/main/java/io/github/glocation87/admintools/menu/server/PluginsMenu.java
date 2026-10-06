package io.github.glocation87.admintools.menu.server;

import io.github.glocation87.admintools.AdminToolsPlugin;
import io.github.glocation87.admintools.Text;
import io.github.glocation87.admintools.menu.Icons;
import io.github.glocation87.admintools.menu.ListMenu;
import io.github.glocation87.admintools.menu.Menu;
import io.github.glocation87.admintools.menu.players.NotesMenu;
import io.papermc.paper.plugin.configuration.PluginMeta;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

public final class PluginsMenu extends ListMenu<Plugin> {

    public PluginsMenu(AdminToolsPlugin plugin, Player viewer, Menu parent) {
        super(plugin, viewer, parent, "<green><b>Plugins", 6);
    }

    @Override
    protected List<Plugin> items() {
        List<Plugin> plugins = new ArrayList<>(Arrays.asList(plugin.getServer().getPluginManager().getPlugins()));
        plugins.sort(Comparator.comparing(other -> other.getName().toLowerCase(Locale.ROOT)));
        return plugins;
    }

    @Override
    protected ItemStack icon(Plugin other) {
        PluginMeta meta = other.getPluginMeta();
        List<String> lore = new ArrayList<>();
        lore.add("Version: <white>" + meta.getVersion());
        if (!meta.getAuthors().isEmpty()) {
            lore.add("Authors: <white>" + String.join(", ", meta.getAuthors()));
        }
        if (meta.getDescription() != null) {
            lore.addAll(NotesMenu.wrap(Text.escape(meta.getDescription())));
        }
        if (!meta.getPluginDependencies().isEmpty()) {
            lore.add("Depends: <white>" + String.join(", ", meta.getPluginDependencies()));
        }
        lore.add("");
        if (other == plugin) {
            lore.add("<dark_gray>That is this plugin");
        } else {
            lore.add(other.isEnabled() ? "<yellow>Click to disable" : "<yellow>Click to enable");
        }
        Material icon = other.isEnabled() ? Material.LIME_CONCRETE : Material.RED_CONCRETE;
        return Icons.of(icon, (other.isEnabled() ? "<green>" : "<red>") + other.getName(), lore);
    }

    @Override
    protected void onClick(Plugin other, ClickType type) {
        if (other == plugin) {
            return;
        }
        if (other.isEnabled()) {
            confirm("<red>Disable " + other.getName() + "?", icon(other), () -> {
                plugin.getServer().getPluginManager().disablePlugin(other);
                message("<red>Disabled <white>" + other.getName());
            });
        } else {
            plugin.getServer().getPluginManager().enablePlugin(other);
            message("<green>Enabled <white>" + other.getName());
            refresh();
        }
    }
}
