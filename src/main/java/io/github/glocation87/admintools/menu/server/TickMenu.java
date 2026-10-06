package io.github.glocation87.admintools.menu.server;

import io.github.glocation87.admintools.AdminToolsPlugin;
import io.github.glocation87.admintools.Text;
import io.github.glocation87.admintools.menu.Icons;
import io.github.glocation87.admintools.menu.Menu;
import org.bukkit.Material;
import org.bukkit.ServerTickManager;
import org.bukkit.entity.Player;

// Wraps the vanilla /tick command so you can single step the whole server while watching it
public final class TickMenu extends Menu {

    public TickMenu(AdminToolsPlugin plugin, Player viewer, Menu parent) {
        super(plugin, viewer, parent, "<red><b>Tick Control", 3);
    }

    @Override
    public boolean live() {
        return true;
    }

    @Override
    protected void draw() {
        border();
        navigation();
        ServerTickManager ticks = plugin.getServer().getServerTickManager();
        set(10, Icons.of(Material.CLOCK, "<yellow>Status",
            "Rate: <white>" + Text.number(ticks.getTickRate()) + " <gray>ticks/s",
            "Frozen: <white>" + ticks.isFrozen() + " <gray>Stepping: <white>" + ticks.isStepping(),
            "Sprinting: <white>" + ticks.isSprinting() + " <gray>Steps left: <white>" + ticks.getFrozenTicksToRun()));
        button(11, Icons.of(ticks.isFrozen() ? Material.BLUE_ICE : Material.ICE, ticks.isFrozen() ? "<aqua>Unfreeze" : "<aqua>Freeze",
            "Players keep moving, everything", "else stands still"), () -> {
                ticks.setFrozen(!ticks.isFrozen());
                refresh();
            });
        button(12, Icons.of(Material.STONE_BUTTON, "<green>Step 1 Tick", "Only while frozen"), () -> step(ticks, 1));
        button(13, Icons.of(Material.OAK_BUTTON, "<green>Step 20 Ticks", "One second, only while frozen"), () -> step(ticks, 20));
        button(14, Icons.of(Material.POLISHED_BLACKSTONE_BUTTON, "<green>Step 100 Ticks", "Five seconds, only while frozen"), () -> step(ticks, 100));
        button(15, Icons.of(Material.REPEATER, "<gold>Tick Rate", "Now: <white>" + Text.number(ticks.getTickRate()), "",
            "Left: slower, right: faster", "Shift: back to 20"), type -> {
                float rate = type.isShiftClick() ? 20 : type.isRightClick() ? ticks.getTickRate() + 5 : ticks.getTickRate() - 5;
                ticks.setTickRate(Math.max(1, Math.min(200, rate)));
                refresh();
            });
        button(16, Icons.of(Material.SUGAR, ticks.isSprinting() ? "<red>Stop Sprint" : "<light_purple>Sprint 1000 Ticks",
            "Runs ticks as fast as the server can"), () -> {
                if (ticks.isSprinting()) {
                    ticks.stopSprinting();
                } else {
                    ticks.requestGameToSprint(1000);
                }
                refresh();
            });
    }

    private void step(ServerTickManager ticks, int count) {
        if (!ticks.stepGameIfFrozen(count)) {
            message("<red>Freeze the game first.");
        }
        refresh();
    }
}
