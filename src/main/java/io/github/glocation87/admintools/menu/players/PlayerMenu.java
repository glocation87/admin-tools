package io.github.glocation87.admintools.menu.players;

import io.github.glocation87.admintools.AdminToolsPlugin;
import io.github.glocation87.admintools.Durations;
import io.github.glocation87.admintools.Text;
import io.github.glocation87.admintools.menu.Icons;
import io.github.glocation87.admintools.menu.Menu;
import io.github.glocation87.admintools.data.Punishment;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public final class PlayerMenu extends Menu {
    private static final GameMode[] MODES = {GameMode.SURVIVAL, GameMode.CREATIVE, GameMode.ADVENTURE, GameMode.SPECTATOR};

    private final Player target;

    public PlayerMenu(AdminToolsPlugin plugin, Player viewer, Menu parent, Player target) {
        super(plugin, viewer, parent, "<dark_purple><b>" + target.getName(), 5);
        this.target = target;
    }

    @Override
    public boolean live() {
        return true;
    }

    @Override
    protected void draw() {
        if (!target.isOnline()) {
            message("<red>" + target.getName() + " went offline.");
            back();
            return;
        }
        border();
        navigation();
        set(4, Icons.head(target, "<green>" + target.getName(), summary()));

        button(10, Icons.of(Material.COMPASS, "<green>Teleport", "Go to them, bring them, visit", "their bed or death point"),
            () -> new TeleportMenu(plugin, viewer, this, target).open());
        button(11, Icons.of(Material.CHEST, "<gold>Inventory", "Live view of what they carry"),
            () -> PeekMenu.inventory(plugin, viewer, this, target).open());
        button(12, Icons.of(Material.ENDER_CHEST, "<dark_purple>Ender Chest", "Live view of their ender chest"),
            () -> PeekMenu.enderChest(plugin, viewer, this, target).open());
        button(13, Icons.of(Material.PAPER, "<aqua>Debug Info", "Connection, position, stats,", "attributes and more"),
            () -> new PlayerDebugMenu(plugin, viewer, this, target).open());
        button(14, Icons.of(Material.POTION, "<light_purple>Potion Effects", "See, remove and give effects"),
            () -> new EffectsMenu(plugin, viewer, this, target).open());
        button(15, Icons.of(Material.WRITABLE_BOOK, "<yellow>Staff Notes", "Notes other staff left about them",
            "Notes: <white>" + plugin.data().notes(target.getUniqueId()).size()), () -> new NotesMenu(plugin, viewer, this, target).open());
        button(16, Icons.of(Material.ENDER_EYE, "<gray>Spectate", "Watch through their eyes", "Leave staff mode to stop"), this::spectate);

        boolean frozen = plugin.freeze().frozen(target);
        button(19, Icons.of(frozen ? Material.PACKED_ICE : Material.BLUE_ICE, frozen ? "<aqua>Thaw" : "<aqua>Freeze",
            frozen ? "They are frozen in place" : "Lock them in place for a chat"), () -> {
                plugin.freeze().toggle(target, viewer);
                refresh();
            });
        button(20, muteIcon(), this::mute);
        button(21, Icons.of(Material.IRON_DOOR, "<red>Kick", "Asks for a reason in chat"), () ->
            plugin.prompts().ask(viewer, "<gray>Reason for kicking <white>" + target.getName() + "<gray>?", reason -> {
                if (!target.isOnline()) {
                    message("<red>They already left.");
                    return;
                }
                plugin.punishments().kick(target, reason, viewer);
                message("<green>Kicked <white>" + target.getName());
            }));
        button(22, Icons.of(Material.ANVIL, "<dark_red>Punish", "Mutes and bans by offense category,", "warnings, history and notes",
            "Record: <white>" + plugin.data().history(target.getUniqueId()).size() + " entries"),
            () -> new PunishMenu(plugin, viewer, this, target).open());
        button(23, Icons.of(Material.SKELETON_SKULL, "<red>Kill"), () ->
            confirm("<red>Kill " + target.getName() + "?", Icons.of(Material.SKELETON_SKULL, "<red>Kill " + target.getName()), () -> {
                target.setHealth(0);
                message("<red>Killed <white>" + target.getName());
            }));
        button(24, Icons.of(Material.GOLDEN_APPLE, "<green>Heal & Feed", "Full health, food, fire out"), () -> {
            AttributeInstance maxHealth = target.getAttribute(Attribute.MAX_HEALTH);
            target.setHealth(maxHealth == null ? 20 : maxHealth.getValue());
            target.setFoodLevel(20);
            target.setSaturation(20);
            target.setFireTicks(0);
            message("<green>Healed <white>" + target.getName());
        });
        button(25, Icons.of(Material.LAVA_BUCKET, "<red>Clear Inventory", "Deletes everything they carry"), () ->
            confirm("<red>Clear inventory?", Icons.of(Material.LAVA_BUCKET, "<red>Clear " + target.getName() + "'s inventory"), () -> {
                target.getInventory().clear();
                message("<red>Cleared <white>" + target.getName() + "<red>'s inventory");
            }));

        button(28, Icons.of(Material.GRASS_BLOCK, "<green>Game Mode", "Now: <white>" + target.getGameMode().name().toLowerCase(Locale.ROOT),
            "", "Left click: next, right click: previous"), type -> {
                int index = 0;
                for (int i = 0; i < MODES.length; i++) {
                    if (MODES[i] == target.getGameMode()) {
                        index = i;
                    }
                }
                index = (index + (type.isRightClick() ? MODES.length - 1 : 1)) % MODES.length;
                target.setGameMode(MODES[index]);
                refresh();
            });
        button(29, Icons.toggle(target.getAllowFlight(), "<aqua>Flight"), () -> {
            target.setAllowFlight(!target.getAllowFlight());
            refresh();
        });
        button(30, Icons.toggle(target.isInvulnerable(), "<gold>God Mode", "They take no damage at all"), () -> {
            target.setInvulnerable(!target.isInvulnerable());
            refresh();
        });
        button(31, Icons.of(Material.LIGHTNING_ROD, "<yellow>Smite", "Harmless lightning, just for effect"), () -> {
            target.getWorld().strikeLightningEffect(target.getLocation());
            message("<yellow>Smote <white>" + target.getName());
        });
        button(32, Icons.toggle(target.isOp(), "<red>Operator", "Server operator status"), () ->
            confirm(target.isOp() ? "<red>Remove op?" : "<red>Grant op?", Icons.of(Material.COMMAND_BLOCK, "<red>" + target.getName()), () -> {
                target.setOp(!target.isOp());
                plugin.staffChat().notice("<yellow>" + viewer.getName() + (target.isOp() ? " <gray>opped <yellow>" : " <gray>de-opped <yellow>")
                    + target.getName(), null);
            }));
        button(33, Icons.of(Material.RED_BED, "<green>Send To Spawn", "World spawn of their current world"), () -> {
            target.teleportAsync(target.getWorld().getSpawnLocation());
            message("<green>Sent <white>" + target.getName() + " <green>to spawn");
        });
        button(34, Icons.of(Material.BOOK, "<aqua>History", "Warns, kicks, mutes and bans",
            "Entries: <white>" + plugin.data().history(target.getUniqueId()).size()), () -> new HistoryMenu(plugin, viewer, this, target).open());
    }

    private List<String> summary() {
        List<String> lore = new ArrayList<>();
        lore.add("Ping: <white>" + target.getPing() + "ms");
        lore.add("Where: <white>" + Text.coords(target.getLocation()));
        lore.add("Mode: <white>" + target.getGameMode().name().toLowerCase(Locale.ROOT));
        lore.add("Health: <white>" + Text.number(target.getHealth()) + " <gray>Food: <white>" + target.getFoodLevel());
        if (plugin.freeze().frozen(target)) {
            lore.add("<aqua>Frozen");
        }
        plugin.mutes().mute(target.getUniqueId()).ifPresent(mute ->
            lore.add("<red>Muted <gray>(" + Durations.remaining(mute.expiresAt()) + "): <white>" + Text.escape(mute.reason())));
        return lore;
    }

    private ItemStack muteIcon() {
        return plugin.mutes().mute(target.getUniqueId())
            .map(mute -> Icons.of(Material.NAME_TAG, "<green>Unmute", "Muted by <white>" + mute.by(),
                "Left: <white>" + Durations.remaining(mute.expiresAt()), "Reason: <white>" + Text.escape(mute.reason())))
            .orElseGet(() -> Icons.of(Material.NAME_TAG, "<red>Mute", "Default " + Durations.format(plugin.settings().defaultMute())
                + ", asks for a reason", "Use the punish menu for other lengths"));
    }

    private void mute() {
        if (plugin.mutes().mute(target.getUniqueId()).isPresent()) {
            plugin.mutes().unmute(target, viewer.getName());
            refresh();
            return;
        }
        plugin.prompts().ask(viewer, "<gray>Reason for muting <white>" + target.getName() + "<gray>?", reason -> {
            plugin.punishments().mute(target, plugin.settings().defaultMute(), reason, viewer, Punishment.Category.CHAT, 0);
            open();
        });
    }

    private void spectate() {
        if (!plugin.staffMode().active(viewer)) {
            message("<red>Spectating needs staff mode.");
            return;
        }
        viewer.closeInventory();
        viewer.setGameMode(GameMode.SPECTATOR);
        viewer.teleportAsync(target.getLocation()).thenRun(() -> viewer.setSpectatorTarget(target));
        message("<gray>Spectating <white>" + target.getName() + "<gray>, leave staff mode to stop");
    }
}
