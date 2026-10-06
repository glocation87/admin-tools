package io.github.glocation87.admintools.menu.server;

import io.github.glocation87.admintools.AdminToolsPlugin;
import io.github.glocation87.admintools.menu.Icons;
import io.github.glocation87.admintools.menu.ListMenu;
import io.github.glocation87.admintools.menu.Menu;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import java.util.Comparator;
import java.util.List;
import org.bukkit.GameRule;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

public final class GameRulesMenu extends ListMenu<GameRule<?>> {
    private final World world;

    public GameRulesMenu(AdminToolsPlugin plugin, Player viewer, Menu parent, World world) {
        super(plugin, viewer, parent, "<gold><b>Game Rules: " + world.getName(), 6);
        this.world = world;
    }

    @Override
    protected List<GameRule<?>> items() {
        return RegistryAccess.registryAccess().getRegistry(RegistryKey.GAME_RULE).stream()
            .sorted(Comparator.comparing(rule -> rule.key().value()))
            .toList();
    }

    private static String name(GameRule<?> rule) {
        return rule.key().value();
    }

    @Override
    protected ItemStack icon(GameRule<?> rule) {
        Object value = world.getGameRuleValue(rule);
        if (rule.getType() == Boolean.class) {
            boolean on = Boolean.TRUE.equals(value);
            return Icons.of(on ? Material.LIME_DYE : Material.GRAY_DYE, (on ? "<green>" : "<red>") + name(rule), "Now: <white>" + on,
                "Default: <white>" + rule.getDefaultValue(), "", "<yellow>Click to toggle");
        }
        return Icons.of(Material.REPEATER, "<aqua>" + name(rule), "Now: <white>" + value, "Default: <white>" + rule.getDefaultValue(), "",
            "<yellow>Click to type a value");
    }

    @Override
    protected void onClick(GameRule<?> rule, ClickType type) {
        if (rule.getType() == Boolean.class) {
            @SuppressWarnings("unchecked")
            GameRule<Boolean> flag = (GameRule<Boolean>) rule;
            world.setGameRule(flag, !Boolean.TRUE.equals(world.getGameRuleValue(flag)));
            refresh();
            return;
        }
        plugin.prompts().ask(viewer, "<gray>New value for <white>" + name(rule) + "<gray>?", input -> {
            if (!apply(rule, input.trim())) {
                message("<red>That value was rejected.");
            }
            open();
        });
    }

    private <T> boolean apply(GameRule<T> rule, String input) {
        Class<T> type = rule.getType();
        Object value;
        try {
            if (type == Integer.class) {
                value = Integer.parseInt(input);
            } else if (type == Long.class) {
                value = Long.parseLong(input);
            } else if (type == Double.class) {
                value = Double.parseDouble(input);
            } else if (type == Float.class) {
                value = Float.parseFloat(input);
            } else if (type == Boolean.class) {
                value = Boolean.parseBoolean(input);
            } else {
                return false;
            }
        } catch (NumberFormatException e) {
            return false;
        }
        return world.setGameRule(rule, type.cast(value));
    }
}
