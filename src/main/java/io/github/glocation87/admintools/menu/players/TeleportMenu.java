package io.github.glocation87.admintools.menu.players;

import io.github.glocation87.admintools.AdminToolsPlugin;
import io.github.glocation87.admintools.Text;
import io.github.glocation87.admintools.menu.Icons;
import io.github.glocation87.admintools.menu.Menu;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public final class TeleportMenu extends Menu {
    private final Player target;

    public TeleportMenu(AdminToolsPlugin plugin, Player viewer, Menu parent, Player target) {
        super(plugin, viewer, parent, "<dark_purple><b>Teleport: " + target.getName(), 3);
        this.target = target;
    }

    @Override
    protected void draw() {
        border();
        navigation();
        button(10, Icons.of(Material.ENDER_PEARL, "<green>Go To Them", Text.coords(target.getLocation())), () -> {
            viewer.teleportAsync(target.getLocation());
            message("<gray>Teleported to <white>" + target.getName());
        });
        button(11, Icons.of(Material.ENDER_EYE, "<green>Bring Them Here"), () -> {
            target.teleportAsync(viewer.getLocation());
            message("<gray>Brought <white>" + target.getName() + " <gray>to you");
        });
        button(12, Icons.of(Material.ENDER_CHEST, "<light_purple>Swap Places"), () -> {
            Location mine = viewer.getLocation().clone();
            viewer.teleportAsync(target.getLocation()).thenRun(() -> target.teleportAsync(mine));
            message("<gray>Swapped places with <white>" + target.getName());
        });
        Location bed = target.getRespawnLocation();
        button(14, Icons.of(Material.RED_BED, "<yellow>Their Respawn Point", bed == null ? "<red>None set" : Text.coords(bed)), () -> go(bed, "respawn point"));
        Location death = target.getLastDeathLocation();
        button(15, Icons.of(Material.SKELETON_SKULL, "<yellow>Their Last Death", death == null ? "<red>Never died" : Text.coords(death)),
            () -> go(death, "last death"));
        button(16, Icons.of(Material.GRASS_BLOCK, "<yellow>Their World Spawn", target.getWorld().getName()),
            () -> go(target.getWorld().getSpawnLocation(), "world spawn"));
    }

    private void go(Location where, String what) {
        if (where == null || where.getWorld() == null) {
            message("<red>No " + what + " known for " + target.getName());
            return;
        }
        viewer.teleportAsync(where);
        message("<gray>Teleported to <white>" + target.getName() + "<gray>'s " + what);
    }
}
