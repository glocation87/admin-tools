package io.github.glocation87.admintools.menu.server;

import io.github.glocation87.admintools.AdminToolsPlugin;
import io.github.glocation87.admintools.menu.Icons;
import io.github.glocation87.admintools.menu.ListMenu;
import io.github.glocation87.admintools.menu.Menu;
import java.util.List;
import java.util.Locale;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

public final class WorldsMenu extends ListMenu<World> {

    public WorldsMenu(AdminToolsPlugin plugin, Player viewer, Menu parent) {
        super(plugin, viewer, parent, "<green><b>Worlds", 4);
    }

    @Override
    public boolean live() {
        return true;
    }

    @Override
    protected List<World> items() {
        return plugin.getServer().getWorlds();
    }

    static Material material(World world) {
        return switch (world.getEnvironment()) {
            case NETHER -> Material.NETHERRACK;
            case THE_END -> Material.END_STONE;
            case NORMAL -> Material.GRASS_BLOCK;
            default -> Material.BEDROCK;
        };
    }

    static String clock(World world) {
        long time = world.getTime();
        return String.format(Locale.ROOT, "%02d:%02d", (time / 1000 + 6) % 24, time % 1000 * 60 / 1000);
    }

    static String weather(World world) {
        if (world.isThundering()) {
            return "thunder";
        }
        return world.hasStorm() ? "rain" : "clear";
    }

    @Override
    protected ItemStack icon(World world) {
        return Icons.of(material(world), "<green>" + world.getName(),
            "Players: <white>" + world.getPlayerCount() + " <gray>Entities: <white>" + world.getEntityCount(),
            "Chunks: <white>" + world.getChunkCount() + " <gray>Tiles: <white>" + world.getTileEntityCount(),
            "Time: <white>" + clock(world) + " <gray>Weather: <white>" + weather(world),
            "Difficulty: <white>" + world.getDifficulty().name().toLowerCase(Locale.ROOT),
            "", "<yellow>Click to manage");
    }

    @Override
    protected void onClick(World world, ClickType type) {
        new WorldMenu(plugin, viewer, this, world).open();
    }
}
