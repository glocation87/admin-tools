package io.github.glocation87.admintools.menu.server;

import io.github.glocation87.admintools.AdminToolsPlugin;
import io.github.glocation87.admintools.Text;
import io.github.glocation87.admintools.debug.Diagnostics;
import io.github.glocation87.admintools.menu.Icons;
import io.github.glocation87.admintools.menu.Menu;
import io.github.glocation87.admintools.menu.players.PlayerListMenu;
import io.github.glocation87.admintools.menu.players.ReportsMenu;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;

public final class DashboardMenu extends Menu {

    public DashboardMenu(AdminToolsPlugin plugin, Player viewer) {
        super(plugin, viewer, null, "<gold><b>Server Dashboard", 5);
    }

    @Override
    public boolean live() {
        return true;
    }

    @Override
    protected void draw() {
        border();
        navigation();
        set(10, Icons.of(Material.CLOCK, "<yellow>TPS", tps()));
        set(11, Icons.of(Material.COMPARATOR, "<yellow>Tick Time", mspt()));
        set(12, Icons.of(Material.BUCKET, "<yellow>Memory", memory()));
        set(13, Icons.of(Material.PLAYER_HEAD, "<yellow>Players", players()));
        set(14, Icons.of(Material.BOOK, "<yellow>Server", server()));
        set(15, Icons.of(Material.ZOMBIE_HEAD, "<yellow>Load", load()));
        int reports = plugin.data().reports().size();
        button(16, Icons.of(reports > 0 ? Material.RED_BANNER : Material.WHITE_BANNER, "<red>Reports", "Open: <white>" + reports, "", "<yellow>Click to handle"),
            () -> new ReportsMenu(plugin, viewer, this).open());

        button(19, Icons.of(Material.GRASS_BLOCK, "<green>Worlds", "Time, weather, difficulty,", "game rules, per world load"),
            () -> new WorldsMenu(plugin, viewer, this).open());
        button(20, Icons.of(Material.CREEPER_HEAD, "<green>Entities", "Counts by type, teleport to them", "or wipe a type"),
            () -> new EntitiesMenu(plugin, viewer, this, Bukkit.getWorlds()).open());
        button(21, Icons.of(Material.MAGMA_BLOCK, "<green>Hot Chunks", "The busiest loaded chunks,", "where the lag lives"),
            () -> new HotChunksMenu(plugin, viewer, this, Bukkit.getWorlds()).open());
        button(22, Icons.of(Material.COMMAND_BLOCK, "<green>Plugins", "Versions, authors, enable and disable"),
            () -> new PluginsMenu(plugin, viewer, this).open());
        button(23, Icons.of(Material.REDSTONE, "<green>Tick Control", "Freeze, step and sprint the game"),
            () -> new TickMenu(plugin, viewer, this).open());
        button(24, Icons.of(Material.REDSTONE_TORCH, "<green>Diagnostics", "GC, thread dumps, heap reports,", "save all, whitelist, broadcast"),
            () -> new DiagnosticsMenu(plugin, viewer, this).open());
        button(25, Icons.of(Material.GOLDEN_HELMET, "<green>Staff", "Who is on duty and where"),
            () -> new StaffMenu(plugin, viewer, this).open());

        button(28, Icons.toggle(plugin.chatControl().locked(), "<gold>Chat Lock", "Only staff can talk while locked"), () -> {
            plugin.chatControl().toggleLock(viewer.getName());
            refresh();
        });
        button(29, Icons.of(Material.SOUL_SAND, "<gold>Slow Chat", "Now: <white>" + plugin.chatControl().slowSeconds() + "s", "",
            "Left: set seconds, right: turn off"), type -> {
                if (type.isRightClick()) {
                    plugin.chatControl().slow(0, viewer.getName());
                    refresh();
                    return;
                }
                plugin.prompts().ask(viewer, "<gray>Seconds between messages?", input -> {
                    try {
                        plugin.chatControl().slow(Integer.parseInt(input.trim()), viewer.getName());
                    } catch (NumberFormatException e) {
                        message("<red>That is not a number.");
                    }
                    open();
                });
            });
        button(30, Icons.of(Material.SPONGE, "<gold>Clear Chat", "Wipes chat for non staff"), () -> plugin.chatControl().clear(viewer.getName()));
        button(33, Icons.of(Material.ENCHANTED_BOOK, "<light_purple>Player List"), () -> new PlayerListMenu(plugin, viewer).open());
    }

    private List<String> tps() {
        List<String> lore = new ArrayList<>();
        try {
            double[] tps = Bukkit.getTPS();
            lore.add("1m: " + Text.colorLow(tps[0], 18, 15) + Text.number(Math.min(20, tps[0])) + " <gray>5m: " + Text.colorLow(tps[1], 18, 15)
                + Text.number(Math.min(20, tps[1])) + " <gray>15m: " + Text.colorLow(tps[2], 18, 15) + Text.number(Math.min(20, tps[2])));
        } catch (UnsupportedOperationException e) {
            lore.add("<red>Not available here");
        }
        lore.add("");
        lore.add("Last 30s, newest right");
        lore.add(Text.history(invert(plugin.stats().tps()), 2, 5));
        return lore;
    }

    // Turns TPS into a lost-ticks number so the shared bar colors read the right way round
    private static List<Double> invert(List<Double> tps) {
        List<Double> out = new ArrayList<>(tps.size());
        for (double value : tps) {
            out.add(Math.max(0, 20 - value));
        }
        return out;
    }

    private List<String> mspt() {
        List<String> lore = new ArrayList<>();
        try {
            double now = Bukkit.getAverageTickTime();
            lore.add("Average: " + Text.color(now, 35, 50) + Text.number(now) + "ms <gray>of 50ms");
            lore.add("Peak 30s: " + Text.color(plugin.stats().peakMspt(), 35, 50) + Text.number(plugin.stats().peakMspt()) + "ms");
            lore.add("Headroom: " + Text.gauge(now / 50, 20));
        } catch (UnsupportedOperationException e) {
            lore.add("<red>Not available here");
        }
        lore.add("");
        lore.add("Last 30s, newest right");
        lore.add(Text.history(plugin.stats().mspt(), 35, 50));
        return lore;
    }

    private List<String> memory() {
        long used = Diagnostics.usedMb();
        long max = Diagnostics.maxMb();
        List<String> lore = new ArrayList<>();
        lore.add("Used: " + Text.color((double) used / max, 0.7, 0.9) + used + " MB <gray>of " + max + " MB");
        lore.add(Text.gauge((double) used / max, 20));
        lore.add("Threads: <white>" + ManagementFactory.getThreadMXBean().getThreadCount());
        return lore;
    }

    private List<String> players() {
        int staff = 0;
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasPermission("admintools.staff")) {
                staff++;
            }
        }
        List<String> lore = new ArrayList<>();
        lore.add("Online: <white>" + Bukkit.getOnlinePlayers().size() + " <gray>of " + Bukkit.getMaxPlayers());
        lore.add("Staff: <white>" + staff + " <gray>in staff mode: <white>" + plugin.staffMode().activeIds().size());
        lore.add("Vanished: <white>" + plugin.vanish().ids().size());
        return lore;
    }

    private List<String> server() {
        List<String> lore = new ArrayList<>();
        lore.add("Version: <white>" + Bukkit.getMinecraftVersion() + " <gray>" + Bukkit.getName());
        lore.add("Java: <white>" + System.getProperty("java.version"));
        lore.add("Uptime: <white>" + uptime());
        lore.add("View distance: <white>" + Bukkit.getViewDistance() + " <gray>simulation: <white>" + Bukkit.getSimulationDistance());
        lore.add("Whitelist: <white>" + Bukkit.hasWhitelist());
        return lore;
    }

    private static String uptime() {
        long seconds = ManagementFactory.getRuntimeMXBean().getUptime() / 1000;
        return seconds / 86400 + "d " + seconds % 86400 / 3600 + "h " + seconds % 3600 / 60 + "m";
    }

    private List<String> load() {
        int entities = 0;
        int chunks = 0;
        int tiles = 0;
        for (World world : Bukkit.getWorlds()) {
            entities += world.getEntityCount();
            chunks += world.getChunkCount();
            tiles += world.getTileEntityCount();
        }
        List<String> lore = new ArrayList<>();
        lore.add("Worlds: <white>" + Bukkit.getWorlds().size());
        lore.add("Entities: <white>" + entities);
        lore.add("Loaded chunks: <white>" + chunks);
        lore.add("Tile entities: <white>" + tiles);
        return lore;
    }
}
