package io.github.glocation87.admintools.menu.server;

import io.github.glocation87.admintools.AdminToolsPlugin;
import io.github.glocation87.admintools.Text;
import io.github.glocation87.admintools.menu.Icons;
import io.github.glocation87.admintools.menu.Menu;
import java.util.List;
import java.util.Locale;
import org.bukkit.Difficulty;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;

public final class WorldMenu extends Menu {
    private final World world;

    public WorldMenu(AdminToolsPlugin plugin, Player viewer, Menu parent, World world) {
        super(plugin, viewer, parent, "<green><b>" + world.getName(), 4);
        this.world = world;
    }

    @Override
    public boolean live() {
        return true;
    }

    @Override
    protected void draw() {
        border();
        navigation();
        set(4, Icons.of(WorldsMenu.material(world), "<green>" + world.getName(),
            "Players: <white>" + world.getPlayerCount() + " <gray>Entities: <white>" + world.getEntityCount(),
            "Chunks: <white>" + world.getChunkCount() + " <gray>Tiles: <white>" + world.getTileEntityCount(),
            "Time: <white>" + WorldsMenu.clock(world) + " <gray>Weather: <white>" + WorldsMenu.weather(world),
            "Seed: <white>" + world.getSeed(),
            "Border: <white>" + Text.number(world.getWorldBorder().getSize()) + " <gray>Spawn: <white>" + Text.coords(world.getSpawnLocation())));

        button(10, Icons.of(Material.ENDER_PEARL, "<green>Teleport To Spawn"), () -> viewer.teleportAsync(world.getSpawnLocation()));
        button(11, Icons.of(Material.SUNFLOWER, "<yellow>Day"), () -> time(1000));
        button(12, Icons.of(Material.GLOWSTONE, "<yellow>Noon"), () -> time(6000));
        button(13, Icons.of(Material.SOUL_LANTERN, "<yellow>Night"), () -> time(13000));
        button(14, Icons.of(Material.BLACK_CANDLE, "<yellow>Midnight"), () -> time(18000));
        button(15, Icons.of(Material.BLUE_STAINED_GLASS, "<aqua>Clear Weather"), () -> weather(false, false));
        button(16, Icons.of(Material.WATER_BUCKET, "<aqua>Rain"), () -> weather(true, false));

        button(19, Icons.of(Material.LIGHTNING_ROD, "<aqua>Thunder"), () -> weather(true, true));
        button(20, Icons.of(Material.IRON_SWORD, "<red>Difficulty", "Now: <white>" + world.getDifficulty().name().toLowerCase(Locale.ROOT), "",
            "Click to cycle"), () -> {
                Difficulty[] all = Difficulty.values();
                world.setDifficulty(all[(world.getDifficulty().ordinal() + 1) % all.length]);
                refresh();
            });
        button(21, Icons.of(Material.OAK_SIGN, "<gold>Game Rules", "Toggle booleans, type numbers"),
            () -> new GameRulesMenu(plugin, viewer, this, world).open());
        button(22, Icons.of(Material.CREEPER_HEAD, "<green>Entities Here"), () -> new EntitiesMenu(plugin, viewer, this, List.of(world)).open());
        button(23, Icons.of(Material.MAGMA_BLOCK, "<green>Hot Chunks Here"), () -> new HotChunksMenu(plugin, viewer, this, List.of(world)).open());
        button(24, Icons.of(Material.CHEST, "<green>Save World"), () -> {
            world.save();
            message("<green>Saved <white>" + world.getName());
        });
        button(25, Icons.of(Material.BARRIER, "<red>World Border", "Size: <white>" + Text.number(world.getWorldBorder().getSize()), "",
            "Click to type a new size"), () -> plugin.prompts().ask(viewer, "<gray>Border size in blocks?", input -> {
                try {
                    world.getWorldBorder().setSize(Double.parseDouble(input.trim()));
                } catch (IllegalArgumentException e) {
                    message("<red>That is not a size.");
                }
                open();
            }));
    }

    private void time(long ticks) {
        world.setTime(ticks);
        refresh();
    }

    private void weather(boolean storm, boolean thunder) {
        world.setStorm(storm);
        world.setThundering(thunder);
        if (!storm) {
            world.setClearWeatherDuration(20 * 60 * 30);
        }
        refresh();
    }
}
