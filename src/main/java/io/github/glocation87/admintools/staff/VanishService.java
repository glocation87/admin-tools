package io.github.glocation87.admintools.staff;

import io.github.glocation87.admintools.AdminToolsPlugin;
import io.github.glocation87.admintools.Text;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.bukkit.entity.Player;

public final class VanishService {
    private final AdminToolsPlugin plugin;
    private final Set<UUID> vanished = new HashSet<>();

    public VanishService(AdminToolsPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean vanished(Player player) {
        return vanished.contains(player.getUniqueId());
    }

    public Set<UUID> ids() {
        return Collections.unmodifiableSet(vanished);
    }

    public boolean toggle(Player player) {
        if (vanished(player)) {
            reveal(player);
            return false;
        }
        vanish(player);
        return true;
    }

    public void vanish(Player player) {
        if (!vanished.add(player.getUniqueId())) {
            return;
        }
        for (Player other : plugin.getServer().getOnlinePlayers()) {
            if (other != player && !canSee(other)) {
                other.hidePlayer(plugin, player);
            }
        }
        Text.send(player, "<gray>You are now invisible to players.");
        plugin.staffMode().updateItem(player, StaffItem.VANISH, true);
    }

    public void reveal(Player player) {
        if (!vanished.remove(player.getUniqueId())) {
            return;
        }
        for (Player other : plugin.getServer().getOnlinePlayers()) {
            other.showPlayer(plugin, player);
        }
        Text.send(player, "<gray>You are visible again.");
        plugin.staffMode().updateItem(player, StaffItem.VANISH, false);
    }

    public void handleJoin(Player joiner) {
        if (canSee(joiner)) {
            return;
        }
        for (UUID id : vanished) {
            Player hidden = plugin.getServer().getPlayer(id);
            if (hidden != null) {
                joiner.hidePlayer(plugin, hidden);
            }
        }
    }

    public void handleQuit(Player player) {
        vanished.remove(player.getUniqueId());
    }

    public void tick() {
        for (UUID id : vanished) {
            Player player = plugin.getServer().getPlayer(id);
            if (player != null) {
                player.sendActionBar(Text.mm("<gray>You are <green>vanished"));
            }
        }
    }

    public boolean canSee(Player viewer) {
        return viewer.hasPermission("admintools.vanish.see");
    }
}
