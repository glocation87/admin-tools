package io.github.glocation87.admintools.menu.server;

import io.github.glocation87.admintools.AdminToolsPlugin;
import io.github.glocation87.admintools.menu.Icons;
import io.github.glocation87.admintools.menu.ListMenu;
import io.github.glocation87.admintools.menu.Menu;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

// Lag finder: the loaded chunks with the most entities and tile entities
public final class HotChunksMenu extends ListMenu<HotChunksMenu.Score> {

    record Score(World world, int x, int z, int entities, int tiles, List<String> top) {
        int total() {
            return entities + tiles;
        }
    }

    private final List<World> worlds;
    private List<Score> cached;

    public HotChunksMenu(AdminToolsPlugin plugin, Player viewer, Menu parent, List<World> worlds) {
        super(plugin, viewer, parent, "<red><b>Hot Chunks", 5);
        this.worlds = worlds;
    }

    @Override
    protected List<Score> items() {
        if (cached == null) {
            cached = scan();
        }
        return cached;
    }

    private List<Score> scan() {
        List<Score> scores = new ArrayList<>();
        for (World world : worlds) {
            for (Chunk chunk : world.getLoadedChunks()) {
                Entity[] entities = chunk.getEntities();
                int tiles = chunk.getTileEntities(false).length;
                if (entities.length + tiles == 0) {
                    continue;
                }
                scores.add(new Score(world, chunk.getX(), chunk.getZ(), entities.length, tiles, top(entities)));
            }
        }
        scores.sort((a, b) -> Integer.compare(b.total(), a.total()));
        return scores.size() > plugin.settings().hotChunks() ? new ArrayList<>(scores.subList(0, plugin.settings().hotChunks())) : scores;
    }

    private static List<String> top(Entity[] entities) {
        Map<EntityType, Integer> counts = new EnumMap<>(EntityType.class);
        for (Entity entity : entities) {
            counts.merge(entity.getType(), 1, Integer::sum);
        }
        List<Map.Entry<EntityType, Integer>> sorted = new ArrayList<>(counts.entrySet());
        sorted.sort((a, b) -> Integer.compare(b.getValue(), a.getValue()));
        List<String> top = new ArrayList<>();
        for (int i = 0; i < Math.min(3, sorted.size()); i++) {
            top.add(sorted.get(i).getKey().getKey().getKey() + " x" + sorted.get(i).getValue());
        }
        return top;
    }

    @Override
    protected ItemStack icon(Score score) {
        Material icon = score.total() >= 150 ? Material.MAGMA_BLOCK : score.total() >= 60 ? Material.ORANGE_CONCRETE : Material.YELLOW_CONCRETE;
        List<String> lore = new ArrayList<>();
        lore.add("World: <white>" + score.world().getName());
        lore.add("Chunk: <white>" + score.x() + ", " + score.z() + " <gray>(block " + score.x() * 16 + ", " + score.z() * 16 + ")");
        lore.add("Entities: <white>" + score.entities() + " <gray>Tiles: <white>" + score.tiles());
        for (String line : score.top()) {
            lore.add("  <white>" + line);
        }
        lore.add("");
        lore.add("<yellow>Click to teleport there");
        return Icons.of(icon, "<red>Score " + score.total(), lore);
    }

    @Override
    protected void onClick(Score score, ClickType type) {
        int x = score.x() * 16 + 8;
        int z = score.z() * 16 + 8;
        Location top = score.world().getHighestBlockAt(x, z).getLocation().add(0.5, 1, 0.5);
        viewer.teleportAsync(top);
        message("<gray>Teleported to chunk <white>" + score.x() + ", " + score.z());
    }

    @Override
    protected void extras() {
        button(bottom() + 1, Icons.of(Material.SUNFLOWER, "<yellow>Rescan"), () -> {
            cached = null;
            refresh();
        });
    }
}
