package io.github.glocation87.admintools.menu.server;

import io.github.glocation87.admintools.AdminToolsPlugin;
import io.github.glocation87.admintools.menu.Icons;
import io.github.glocation87.admintools.menu.ListMenu;
import io.github.glocation87.admintools.menu.Menu;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

public final class EntitiesMenu extends ListMenu<EntitiesMenu.Count> {

    record Count(EntityType type, int total, Map<String, Integer> perWorld) {
    }

    private final List<World> worlds;
    private List<Count> cached;

    public EntitiesMenu(AdminToolsPlugin plugin, Player viewer, Menu parent, List<World> worlds) {
        super(plugin, viewer, parent, "<green><b>Entities", 6);
        this.worlds = worlds;
    }

    @Override
    protected List<Count> items() {
        if (cached == null) {
            cached = count();
        }
        return cached;
    }

    private List<Count> count() {
        Map<EntityType, Map<String, Integer>> byType = new EnumMap<>(EntityType.class);
        for (World world : worlds) {
            for (Entity entity : world.getEntities()) {
                byType.computeIfAbsent(entity.getType(), key -> new LinkedHashMap<>()).merge(world.getName(), 1, Integer::sum);
            }
        }
        List<Count> counts = new ArrayList<>();
        for (Map.Entry<EntityType, Map<String, Integer>> entry : byType.entrySet()) {
            int total = 0;
            for (int value : entry.getValue().values()) {
                total += value;
            }
            counts.add(new Count(entry.getKey(), total, entry.getValue()));
        }
        counts.sort((a, b) -> Integer.compare(b.total(), a.total()));
        return counts;
    }

    static Material iconFor(EntityType type) {
        Material egg = Material.matchMaterial(type.getKey().getKey() + "_spawn_egg");
        if (egg != null) {
            return egg;
        }
        return switch (type) {
            case PLAYER -> Material.PLAYER_HEAD;
            case ITEM -> Material.STICK;
            case EXPERIENCE_ORB -> Material.EXPERIENCE_BOTTLE;
            case ARROW, SPECTRAL_ARROW -> Material.ARROW;
            case ITEM_FRAME, GLOW_ITEM_FRAME -> Material.ITEM_FRAME;
            case ARMOR_STAND -> Material.ARMOR_STAND;
            case PAINTING -> Material.PAINTING;
            case FALLING_BLOCK -> Material.SAND;
            case TNT -> Material.TNT;
            case MINECART, CHEST_MINECART, HOPPER_MINECART, FURNACE_MINECART, TNT_MINECART -> Material.MINECART;
            case TEXT_DISPLAY, ITEM_DISPLAY, BLOCK_DISPLAY -> Material.GLASS;
            default -> Material.EGG;
        };
    }

    @Override
    protected ItemStack icon(Count count) {
        List<String> lore = new ArrayList<>();
        lore.add("Total: <white>" + count.total());
        for (Map.Entry<String, Integer> entry : count.perWorld().entrySet()) {
            lore.add("  " + entry.getKey() + ": <white>" + entry.getValue());
        }
        lore.add("");
        lore.add("<yellow>Left: teleport to the nearest one");
        if (count.type() != EntityType.PLAYER) {
            lore.add("<yellow>Shift + right: remove them all");
        }
        ItemStack icon = Icons.of(iconFor(count.type()), "<green>" + count.type().getKey().getKey().replace('_', ' '), lore);
        icon.setAmount(Math.max(1, Math.min(64, count.total())));
        return icon;
    }

    @Override
    protected void onClick(Count count, ClickType type) {
        if (type.isShiftClick() && type.isRightClick()) {
            if (count.type() == EntityType.PLAYER) {
                return;
            }
            confirm("<red>Remove all " + count.type().getKey().getKey() + "?", icon(count), () -> {
                int removed = 0;
                for (World world : worlds) {
                    for (Entity entity : world.getEntities()) {
                        if (entity.getType() == count.type()) {
                            entity.remove();
                            removed++;
                        }
                    }
                }
                cached = null;
                message("<red>Removed <white>" + removed + " <red>" + count.type().getKey().getKey().toLowerCase(Locale.ROOT));
            });
            return;
        }
        Entity nearest = null;
        double best = Double.MAX_VALUE;
        for (World world : worlds) {
            for (Entity entity : world.getEntities()) {
                if (entity.getType() != count.type() || entity == viewer) {
                    continue;
                }
                double distance = world == viewer.getWorld() ? entity.getLocation().distanceSquared(viewer.getLocation()) : Double.MAX_VALUE / 2;
                if (distance < best) {
                    best = distance;
                    nearest = entity;
                }
            }
        }
        if (nearest == null) {
            message("<red>None left.");
            cached = null;
            refresh();
            return;
        }
        viewer.teleportAsync(nearest.getLocation());
        message("<gray>Teleported to a <white>" + count.type().getKey().getKey());
    }

    @Override
    protected void extras() {
        button(bottom() + 1, Icons.of(Material.SUNFLOWER, "<yellow>Recount"), () -> {
            cached = null;
            refresh();
        });
    }
}
