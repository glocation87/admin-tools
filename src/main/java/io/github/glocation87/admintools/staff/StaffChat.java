package io.github.glocation87.admintools.staff;

import io.github.glocation87.admintools.AdminToolsPlugin;
import io.github.glocation87.admintools.Text;
import io.papermc.paper.event.player.AsyncChatEvent;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public final class StaffChat implements Listener {
    public static final String PERMISSION = "admintools.staff";

    private final AdminToolsPlugin plugin;
    private final Set<UUID> toggled = new HashSet<>();

    public StaffChat(AdminToolsPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean toggled(Player player) {
        return toggled.contains(player.getUniqueId());
    }

    public boolean toggle(Player player) {
        if (toggled.remove(player.getUniqueId())) {
            Text.send(player, "<gray>Chat goes to everyone again.");
            return false;
        }
        toggled.add(player.getUniqueId());
        Text.send(player, "<gold>Chat now goes to staff only.");
        return true;
    }

    public void send(CommandSender from, String message) {
        String name = from instanceof Player player ? player.getName() : "Console";
        broadcast(Text.mm("<dark_gray>[<gold>SC</gold>]</dark_gray> <yellow>" + name + "<dark_gray>: <white>" + Text.escape(message)));
    }

    // Short staff-only status lines, the actor is skipped because they already got a direct reply
    public void notice(String mini, Player actor) {
        Component line = Text.mm(Text.PREFIX + mini);
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            if (player != actor && player.hasPermission(PERMISSION)) {
                player.sendMessage(line);
            }
        }
        plugin.getServer().getConsoleSender().sendMessage(line);
    }

    private void broadcast(Component line) {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            if (player.hasPermission(PERMISSION)) {
                player.sendMessage(line);
            }
        }
        plugin.getServer().getConsoleSender().sendMessage(line);
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onChat(AsyncChatEvent event) {
        if (toggled(event.getPlayer())) {
            event.setCancelled(true);
            send(event.getPlayer(), Text.plain(event.message()));
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        toggled.remove(event.getPlayer().getUniqueId());
    }
}
