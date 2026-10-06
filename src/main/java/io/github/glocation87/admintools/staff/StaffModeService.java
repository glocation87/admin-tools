package io.github.glocation87.admintools.staff;

import io.github.glocation87.admintools.AdminToolsPlugin;
import io.github.glocation87.admintools.Text;
import io.github.glocation87.admintools.data.StaffSnapshot;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.kyori.adventure.bossbar.BossBar;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

public final class StaffModeService {
    private final AdminToolsPlugin plugin;
    private final StaffItems items;
    private final VanishService vanish;
    private final CommandSpy spy;
    private final StaffChat chat;
    private final Set<UUID> active = new HashSet<>();
    private final BossBar bar = BossBar.bossBar(Text.mm("<green><b>Staff Mode"), 1f, BossBar.Color.GREEN, BossBar.Overlay.PROGRESS);

    public StaffModeService(AdminToolsPlugin plugin, StaffItems items, VanishService vanish, CommandSpy spy, StaffChat chat) {
        this.plugin = plugin;
        this.items = items;
        this.vanish = vanish;
        this.spy = spy;
        this.chat = chat;
    }

    public boolean active(Player player) {
        return active.contains(player.getUniqueId());
    }

    public boolean active(UUID id) {
        return active.contains(id);
    }

    public Set<UUID> activeIds() {
        return Collections.unmodifiableSet(active);
    }

    public void toggle(Player player) {
        if (active(player)) {
            exit(player);
        } else {
            enter(player);
        }
    }

    public void enter(Player player) {
        if (active(player)) {
            return;
        }
        player.closeInventory();
        plugin.data().snapshot(player.getUniqueId(), StaffSnapshot.capture(player));
        player.getInventory().clear();
        player.setItemOnCursor(null);
        player.setGameMode(plugin.settings().staffGameMode());
        if (plugin.settings().fly()) {
            player.setAllowFlight(true);
            player.setFlying(true);
        }
        active.add(player.getUniqueId());
        items.give(player, vanish.vanished(player), spy.spying(player));
        player.showBossBar(bar);
        if (plugin.settings().vanishOnEnter() && !vanish.vanished(player)) {
            vanish.vanish(player);
        }
        Text.send(player, "<green>Staff mode on.");
        chat.notice("<yellow>" + player.getName() + " <gray>entered staff mode", player);
    }

    public void exit(Player player) {
        if (!active.remove(player.getUniqueId())) {
            return;
        }
        player.closeInventory();
        player.hideBossBar(bar);
        if (vanish.vanished(player)) {
            vanish.reveal(player);
        }
        if (player.getGameMode() == GameMode.SPECTATOR && player.getSpectatorTarget() != null) {
            player.setSpectatorTarget(null);
        }
        player.getInventory().clear();
        player.setItemOnCursor(null);
        StaffSnapshot snapshot = plugin.data().snapshot(player.getUniqueId());
        if (snapshot != null) {
            snapshot.restore(player);
        } else {
            player.setGameMode(GameMode.SURVIVAL);
        }
        plugin.data().clearSnapshot(player.getUniqueId());
        Text.send(player, "<yellow>Staff mode off.");
        chat.notice("<yellow>" + player.getName() + " <gray>left staff mode", player);
    }

    // A crash or kill -9 mid session leaves the snapshot on disk, hand it back on the next login
    public void restoreCrashed(Player player) {
        StaffSnapshot snapshot = plugin.data().snapshot(player.getUniqueId());
        if (snapshot == null || active(player)) {
            return;
        }
        player.getInventory().clear();
        snapshot.restore(player);
        plugin.data().clearSnapshot(player.getUniqueId());
        Text.send(player, "<yellow>Your inventory from your last staff session was restored.");
    }

    public void exitAll() {
        for (UUID id : new ArrayList<>(active)) {
            Player player = plugin.getServer().getPlayer(id);
            if (player != null) {
                exit(player);
            }
        }
    }

    public void updateItem(Player player, StaffItem item, boolean state) {
        if (active(player)) {
            items.update(player, item, state);
        }
    }

    public StaffItems items() {
        return items;
    }
}
