package io.github.glocation87.admintools.menu;

import io.github.glocation87.admintools.AdminToolsPlugin;
import io.github.glocation87.admintools.Text;
import io.papermc.paper.event.player.AsyncChatEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

// Closes the menu and grabs the next chat line as input
public final class ChatPrompt implements Listener {
    private final AdminToolsPlugin plugin;
    private final Map<UUID, Consumer<String>> pending = new HashMap<>();

    public ChatPrompt(AdminToolsPlugin plugin) {
        this.plugin = plugin;
    }

    public void ask(Player player, String question, Consumer<String> answer) {
        player.closeInventory();
        pending.put(player.getUniqueId(), answer);
        Text.send(player, question);
        Text.send(player, "<gray>Type your answer in chat, or <red>cancel</red> to stop.");
    }

    public boolean waiting(Player player) {
        return pending.containsKey(player.getUniqueId());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncChatEvent event) {
        Consumer<String> answer = pending.remove(event.getPlayer().getUniqueId());
        if (answer == null) {
            return;
        }
        event.setCancelled(true);
        String input = Text.plain(event.message()).trim();
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            if (input.equalsIgnoreCase("cancel")) {
                Text.send(event.getPlayer(), "<gray>Cancelled.");
            } else {
                answer.accept(input);
            }
        });
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        pending.remove(event.getPlayer().getUniqueId());
    }
}
