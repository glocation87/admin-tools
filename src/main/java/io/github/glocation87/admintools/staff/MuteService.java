package io.github.glocation87.admintools.staff;

import io.github.glocation87.admintools.AdminToolsPlugin;
import io.github.glocation87.admintools.Durations;
import io.github.glocation87.admintools.Text;
import io.github.glocation87.admintools.data.Mute;
import io.papermc.paper.event.player.AsyncChatEvent;
import java.time.Duration;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public final class MuteService implements Listener {
    private static final Set<String> CHAT_COMMANDS = Set.of("msg", "tell", "w", "me", "say", "r", "reply", "teammsg", "tm");

    private final AdminToolsPlugin plugin;

    public MuteService(AdminToolsPlugin plugin) {
        this.plugin = plugin;
    }

    public Optional<Mute> mute(UUID id) {
        return plugin.data().mute(id);
    }

    public void mute(OfflinePlayer target, Duration duration, String reason, String by) {
        long expires = duration == null ? -1 : System.currentTimeMillis() + duration.toMillis();
        plugin.data().mute(target.getUniqueId(), new Mute(expires, reason, by));
        Player online = target.getPlayer();
        if (online != null) {
            Text.send(online, "<red>You have been muted for " + Durations.format(duration) + ": <white>" + Text.escape(reason));
        }
        plugin.staffChat().notice("<yellow>" + by + " <gray>muted <yellow>" + target.getName() + " <gray>for " + Durations.format(duration)
            + ": <white>" + Text.escape(reason), null);
    }

    public boolean unmute(OfflinePlayer target, String by) {
        if (!plugin.data().unmute(target.getUniqueId())) {
            return false;
        }
        Player online = target.getPlayer();
        if (online != null) {
            Text.send(online, "<green>You have been unmuted.");
        }
        plugin.staffChat().notice("<yellow>" + by + " <gray>unmuted <yellow>" + target.getName(), null);
        return true;
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onChat(AsyncChatEvent event) {
        mute(event.getPlayer().getUniqueId()).ifPresent(mute -> {
            event.setCancelled(true);
            deny(event.getPlayer(), mute);
        });
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        String label = event.getMessage().substring(1).split(" ", 2)[0].toLowerCase(Locale.ROOT);
        int colon = label.indexOf(':');
        if (colon >= 0) {
            label = label.substring(colon + 1);
        }
        if (!CHAT_COMMANDS.contains(label)) {
            return;
        }
        mute(event.getPlayer().getUniqueId()).ifPresent(mute -> {
            event.setCancelled(true);
            deny(event.getPlayer(), mute);
        });
    }

    private void deny(Player player, Mute mute) {
        Text.send(player, "<red>You are muted (" + Durations.remaining(mute.expiresAt()) + " left): <white>" + Text.escape(mute.reason()));
    }
}
