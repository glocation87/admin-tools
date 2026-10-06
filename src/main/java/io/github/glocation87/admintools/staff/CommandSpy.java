package io.github.glocation87.admintools.staff;

import io.github.glocation87.admintools.AdminToolsPlugin;
import io.github.glocation87.admintools.Text;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public final class CommandSpy implements Listener {
    private final AdminToolsPlugin plugin;
    private final Set<UUID> spies = new HashSet<>();

    public CommandSpy(AdminToolsPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean spying(Player player) {
        return spies.contains(player.getUniqueId());
    }

    public boolean toggle(Player player) {
        boolean on = !spies.remove(player.getUniqueId());
        if (on) {
            spies.add(player.getUniqueId());
        }
        Text.send(player, on ? "<gray>Command spy <green>on" : "<gray>Command spy <red>off");
        plugin.staffMode().updateItem(player, StaffItem.COMMAND_SPY, on);
        return on;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        if (spies.isEmpty()) {
            return;
        }
        Component line = Text.mm("<dark_gray>[<gray>Spy</gray>]</dark_gray> <gray>" + event.getPlayer().getName() + "<dark_gray>: <white>"
            + Text.escape(event.getMessage()));
        for (UUID id : spies) {
            Player spy = plugin.getServer().getPlayer(id);
            if (spy != null && spy != event.getPlayer()) {
                spy.sendMessage(line);
            }
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        spies.remove(event.getPlayer().getUniqueId());
    }
}
