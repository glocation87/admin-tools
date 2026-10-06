package io.github.glocation87.admintools.staff;

import io.github.glocation87.admintools.AdminToolsPlugin;
import io.github.glocation87.admintools.Text;
import io.papermc.paper.event.player.AsyncChatEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public final class ChatControl implements Listener {
    private final AdminToolsPlugin plugin;
    private final Map<UUID, Long> lastMessage = new HashMap<>();
    private boolean locked;
    private int slowSeconds;

    public ChatControl(AdminToolsPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean locked() {
        return locked;
    }

    public int slowSeconds() {
        return slowSeconds;
    }

    public boolean toggleLock(String by) {
        locked = !locked;
        plugin.getServer().broadcast(Text.mm(Text.PREFIX + (locked ? "<red>Chat has been locked by " : "<green>Chat has been unlocked by ") + by));
        return locked;
    }

    public void slow(int seconds, String by) {
        slowSeconds = Math.max(0, seconds);
        lastMessage.clear();
        plugin.getServer().broadcast(Text.mm(Text.PREFIX + (slowSeconds == 0 ? "<green>Slow chat is off"
            : "<yellow>Slow chat: one message every " + slowSeconds + "s") + " <gray>(" + by + ")"));
    }

    public void clear(String by) {
        Component blank = Component.empty();
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            if (!player.hasPermission(StaffChat.PERMISSION)) {
                for (int i = 0; i < 100; i++) {
                    player.sendMessage(blank);
                }
            }
        }
        plugin.getServer().broadcast(Text.mm(Text.PREFIX + "<gray>Chat was cleared by " + by));
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onChat(AsyncChatEvent event) {
        Player player = event.getPlayer();
        if (player.hasPermission(StaffChat.PERMISSION)) {
            return;
        }
        if (locked) {
            event.setCancelled(true);
            Text.send(player, "<red>Chat is locked right now.");
            return;
        }
        if (slowSeconds > 0) {
            long now = System.currentTimeMillis();
            Long last = lastMessage.get(player.getUniqueId());
            if (last != null && now - last < slowSeconds * 1000L) {
                event.setCancelled(true);
                Text.send(player, "<red>Slow chat, wait " + ((slowSeconds * 1000L - (now - last)) / 1000 + 1) + "s.");
                return;
            }
            lastMessage.put(player.getUniqueId(), now);
        }
    }
}
