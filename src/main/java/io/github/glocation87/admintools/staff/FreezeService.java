package io.github.glocation87.admintools.staff;

import io.github.glocation87.admintools.AdminToolsPlugin;
import io.github.glocation87.admintools.Text;
import java.time.Duration;
import java.util.Locale;
import java.util.UUID;
import net.kyori.adventure.title.Title;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public final class FreezeService implements Listener {
    private static final Title FROZEN = Title.title(Text.mm("<aqua><b>FROZEN"), Text.mm("<gray>Stay put, a staff member wants a word"),
        Title.Times.times(Duration.ofMillis(200), Duration.ofSeconds(5), Duration.ofMillis(500)));

    private final AdminToolsPlugin plugin;

    public FreezeService(AdminToolsPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean frozen(UUID id) {
        return plugin.data().frozen(id);
    }

    public boolean frozen(Player player) {
        return frozen(player.getUniqueId());
    }

    public boolean toggle(Player target, Player by) {
        if (frozen(target)) {
            thaw(target, by);
            return false;
        }
        freeze(target, by);
        return true;
    }

    public void freeze(Player target, Player by) {
        plugin.data().frozen(target.getUniqueId(), true);
        target.showTitle(FROZEN);
        Text.send(target, "<aqua>You have been frozen by " + by.getName() + ". Do not log out.");
        Text.send(by, "<aqua>Froze <white>" + target.getName());
        plugin.staffChat().notice("<yellow>" + by.getName() + " <gray>froze <yellow>" + target.getName(), by);
    }

    public void thaw(Player target, Player by) {
        plugin.data().frozen(target.getUniqueId(), false);
        target.clearTitle();
        Text.send(target, "<green>You have been unfrozen.");
        Text.send(by, "<green>Thawed <white>" + target.getName());
        plugin.staffChat().notice("<yellow>" + by.getName() + " <gray>thawed <yellow>" + target.getName(), by);
    }

    @EventHandler(ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        if (event.hasChangedPosition() && frozen(event.getPlayer())) {
            Location stay = event.getFrom().clone();
            stay.setYaw(event.getTo().getYaw());
            stay.setPitch(event.getTo().getPitch());
            event.setTo(stay);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        if (!frozen(event.getPlayer())) {
            return;
        }
        String label = event.getMessage().substring(1).split(" ", 2)[0].toLowerCase(Locale.ROOT);
        int colon = label.indexOf(':');
        if (colon >= 0) {
            label = label.substring(colon + 1);
        }
        if (!plugin.settings().frozenAllowedCommands().contains(label)) {
            event.setCancelled(true);
            Text.send(event.getPlayer(), "<red>You cannot use commands while frozen.");
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player victim && frozen(victim)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onAttack(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player attacker && frozen(attacker)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBreak(BlockBreakEvent event) {
        if (frozen(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent event) {
        if (frozen(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        if (frozen(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onDrop(PlayerDropItemEvent event) {
        if (frozen(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        if (frozen(event.getPlayer())) {
            plugin.staffChat().notice("<red>" + event.getPlayer().getName() + " logged out while frozen", null);
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (frozen(event.getPlayer())) {
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> event.getPlayer().showTitle(FROZEN), 20L);
        }
    }
}
