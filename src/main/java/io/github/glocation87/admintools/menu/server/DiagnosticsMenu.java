package io.github.glocation87.admintools.menu.server;

import io.github.glocation87.admintools.AdminToolsPlugin;
import io.github.glocation87.admintools.Text;
import io.github.glocation87.admintools.debug.Diagnostics;
import io.github.glocation87.admintools.menu.Icons;
import io.github.glocation87.admintools.menu.Menu;
import java.io.File;
import java.io.IOException;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;

public final class DiagnosticsMenu extends Menu {

    public DiagnosticsMenu(AdminToolsPlugin plugin, Player viewer, Menu parent) {
        super(plugin, viewer, parent, "<red><b>Diagnostics", 3);
    }

    @Override
    public boolean live() {
        return true;
    }

    @Override
    protected void draw() {
        border();
        navigation();
        button(10, Icons.of(Material.COMPOSTER, "<green>Run GC", "Used: <white>" + Diagnostics.usedMb() + " MB <gray>of " + Diagnostics.maxMb() + " MB",
            "", "Forces a garbage collection, expect a hitch"), () -> {
                long freed = plugin.diagnostics().gc();
                message("<green>GC done, freed <white>" + freed + " MB");
                refresh();
            });
        button(11, Icons.of(Material.PAPER, "<yellow>Thread Dump", "Writes every thread's stack", "to plugins/AdminTools/dumps"), () -> {
            try {
                File file = plugin.diagnostics().threadDump();
                message("<green>Wrote <white>" + file.getName());
            } catch (IOException e) {
                message("<red>Could not write the dump: " + Text.escape(e.getMessage()));
            }
        });
        button(12, Icons.of(Material.BOOK, "<yellow>Heap Report", "Memory pools, GC stats and", "per world load, to a file"), () -> {
            try {
                File file = plugin.diagnostics().heapReport();
                message("<green>Wrote <white>" + file.getName());
            } catch (IOException e) {
                message("<red>Could not write the report: " + Text.escape(e.getMessage()));
            }
        });
        button(13, Icons.of(Material.CHEST, "<green>Save Everything", "All worlds and player data"), () -> {
            for (World world : plugin.getServer().getWorlds()) {
                world.save();
            }
            plugin.getServer().savePlayers();
            message("<green>Saved all worlds and players");
        });
        button(14, Icons.of(Material.COMMAND_BLOCK, "<aqua>Reload Config", "Rereads config.yml"), () -> {
            plugin.reload();
            message("<green>Config reloaded");
        });
        button(15, Icons.toggle(plugin.getServer().hasWhitelist(), "<gold>Whitelist", "Only listed players may join"), () -> {
            plugin.getServer().setWhitelist(!plugin.getServer().hasWhitelist());
            refresh();
        });
        button(16, Icons.of(Material.BELL, "<light_purple>Broadcast", "Sends a message to everyone"), () ->
            plugin.prompts().ask(viewer, "<gray>What should everyone see?", text -> {
                plugin.getServer().broadcast(Text.mm("<dark_gray>[<light_purple>Broadcast</light_purple>]</dark_gray> <white>" + Text.escape(text)));
                open();
            }));
    }
}
