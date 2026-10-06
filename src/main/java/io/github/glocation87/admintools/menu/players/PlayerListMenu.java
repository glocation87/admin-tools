package io.github.glocation87.admintools.menu.players;

import io.github.glocation87.admintools.AdminToolsPlugin;
import io.github.glocation87.admintools.Text;
import io.github.glocation87.admintools.menu.Icons;
import io.github.glocation87.admintools.menu.ListMenu;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

public final class PlayerListMenu extends ListMenu<Player> {

    public PlayerListMenu(AdminToolsPlugin plugin, Player viewer) {
        super(plugin, viewer, null, "<dark_purple><b>Players", 6);
    }

    @Override
    public boolean live() {
        return true;
    }

    @Override
    protected List<Player> items() {
        List<Player> online = new ArrayList<>(plugin.getServer().getOnlinePlayers());
        online.sort(Comparator.comparing(player -> player.getName().toLowerCase(Locale.ROOT)));
        return online;
    }

    @Override
    protected ItemStack icon(Player player) {
        List<String> lore = new ArrayList<>();
        lore.add("Ping: " + Text.color(player.getPing(), 100, 250) + player.getPing() + "ms");
        lore.add("World: <white>" + Text.coords(player.getLocation()));
        lore.add("Mode: <white>" + player.getGameMode().name().toLowerCase(Locale.ROOT));
        lore.add("Health: <white>" + Text.number(player.getHealth()) + " <gray>Food: <white>" + player.getFoodLevel());
        List<String> badges = badges(player);
        if (!badges.isEmpty()) {
            lore.add("");
            lore.addAll(badges);
        }
        lore.add("");
        lore.add("<yellow>Click to manage");
        String color = plugin.staffMode().active(player) ? "<gold>" : plugin.freeze().frozen(player) ? "<aqua>" : "<green>";
        return Icons.head(player, color + player.getName(), lore);
    }

    private List<String> badges(Player player) {
        List<String> badges = new ArrayList<>();
        if (plugin.staffMode().active(player)) {
            badges.add("<gold>Staff mode");
        }
        if (plugin.vanish().vanished(player)) {
            badges.add("<dark_gray>Vanished");
        }
        if (plugin.freeze().frozen(player)) {
            badges.add("<aqua>Frozen");
        }
        plugin.mutes().mute(player.getUniqueId()).ifPresent(mute -> badges.add("<red>Muted"));
        if (player.isOp()) {
            badges.add("<red>Operator");
        }
        return badges;
    }

    @Override
    protected void onClick(Player player, ClickType type) {
        if (player.isOnline()) {
            new PlayerMenu(plugin, viewer, this, player).open();
        }
    }

    @Override
    protected void extras() {
        int staff = 0;
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            if (player.hasPermission("admintools.staff")) {
                staff++;
            }
        }
        set(bottom() + 1, Icons.of(Material.GOLDEN_HELMET, "<gold>Staff online: <white>" + staff,
            "In staff mode: <white>" + plugin.staffMode().activeIds().size(),
            "Vanished: <white>" + plugin.vanish().ids().size()));
    }
}
