package io.github.glocation87.admintools.menu.players;

import io.github.glocation87.admintools.AdminToolsPlugin;
import io.github.glocation87.admintools.Durations;
import io.github.glocation87.admintools.Text;
import io.github.glocation87.admintools.data.Punishment;
import io.github.glocation87.admintools.menu.Icons;
import io.github.glocation87.admintools.menu.Menu;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

// Mineplex style: pick a category and severity, the ladder works out the length
public final class PunishMenu extends Menu {
    private static final Material[] CHAT_ICONS = {Material.LIME_DYE, Material.ORANGE_DYE, Material.RED_DYE};
    private static final Material[] GAMEPLAY_ICONS = {Material.IRON_SWORD, Material.DIAMOND_SWORD, Material.NETHERITE_SWORD};

    private final OfflinePlayer target;

    public PunishMenu(AdminToolsPlugin plugin, Player viewer, Menu parent, OfflinePlayer target) {
        super(plugin, viewer, parent, "<dark_red><b>Punish: " + target.getName(), 5);
        this.target = target;
    }

    @Override
    protected void draw() {
        border();
        navigation();
        set(4, Icons.head(target, "<red>" + target.getName(), summary()));

        set(10, Icons.of(Material.NAME_TAG, "<gold>Chat Offense", "Spam, toxicity, advertising", "Priors: <white>" + priors(Punishment.Category.CHAT)));
        for (int severity = 1; severity <= 3; severity++) {
            int level = severity;
            button(10 + severity, tier("<gold>Mute severity " + severity, CHAT_ICONS[severity - 1], Punishment.Category.CHAT, severity),
                () -> ask(Punishment.Category.CHAT, level));
        }
        set(19, Icons.of(Material.DIAMOND_SWORD, "<red>Gameplay Offense", "Cheating, griefing, exploiting", "Priors: <white>" + priors(Punishment.Category.GAMEPLAY)));
        for (int severity = 1; severity <= 3; severity++) {
            int level = severity;
            button(19 + severity, tier("<red>Ban severity " + severity, GAMEPLAY_ICONS[severity - 1], Punishment.Category.GAMEPLAY, severity),
                () -> ask(Punishment.Category.GAMEPLAY, level));
        }

        button(28, Icons.of(Material.YELLOW_WOOL, "<yellow>Warn", "Goes on their record, shows a title"), () ->
            plugin.prompts().ask(viewer, "<gray>Reason for the warning?", reason -> {
                plugin.punishments().warn(target, reason, viewer);
                open();
            }));
        Player online = target.getPlayer();
        if (online != null) {
            button(29, Icons.of(Material.IRON_DOOR, "<red>Kick"), () ->
                plugin.prompts().ask(viewer, "<gray>Reason for the kick?", reason -> {
                    if (target.isOnline()) {
                        plugin.punishments().kick(target.getPlayer(), reason, viewer);
                    }
                    open();
                }));
        }
        plugin.mutes().mute(target.getUniqueId()).ifPresent(mute ->
            button(30, Icons.of(Material.LIME_DYE, "<green>Unmute", "By <white>" + mute.by() + "<gray>, " + Durations.remaining(mute.expiresAt()) + " left",
                "Reason: <white>" + Text.escape(mute.reason())), () -> {
                    plugin.mutes().unmute(target, viewer.getName());
                    refresh();
                }));
        if (plugin.punishments().banned(target)) {
            button(31, Icons.of(Material.LIME_WOOL, "<green>Unban"), () -> {
                plugin.punishments().unban(target, viewer);
                refresh();
            });
        }
        button(33, Icons.of(Material.BOOK, "<aqua>History", "Entries: <white>" + plugin.data().history(target.getUniqueId()).size()),
            () -> new HistoryMenu(plugin, viewer, this, target).open());
        button(34, Icons.of(Material.WRITABLE_BOOK, "<yellow>Notes", "Notes: <white>" + plugin.data().notes(target.getUniqueId()).size()),
            () -> new NotesMenu(plugin, viewer, this, target).open());
    }

    private int priors(Punishment.Category category) {
        return plugin.punishments().priors(target.getUniqueId(), category);
    }

    private org.bukkit.inventory.ItemStack tier(String name, Material icon, Punishment.Category category, int severity) {
        Duration length = plugin.punishments().ladder(target.getUniqueId(), category, severity);
        return Icons.of(icon, name, "Length: <white>" + Durations.format(length), "", "<yellow>Click, then type the reason");
    }

    private void ask(Punishment.Category category, int severity) {
        Duration length = plugin.punishments().ladder(target.getUniqueId(), category, severity);
        String what = category == Punishment.Category.CHAT ? "mute" : "ban";
        plugin.prompts().ask(viewer, "<gray>Reason for the " + Durations.format(length) + " " + what + "?", reason -> {
            if (category == Punishment.Category.CHAT) {
                plugin.punishments().mute(target, length, reason, viewer, category, severity);
            } else {
                plugin.punishments().ban(target, length, reason, viewer, category, severity);
            }
            open();
        });
    }

    private List<String> summary() {
        List<String> lore = new ArrayList<>();
        int warns = 0;
        int mutes = 0;
        int bans = 0;
        for (Punishment punishment : plugin.data().history(target.getUniqueId())) {
            switch (punishment.type()) {
                case WARN -> warns++;
                case MUTE -> mutes++;
                case BAN -> bans++;
                default -> {
                }
            }
        }
        lore.add("Warns: <white>" + warns + " <gray>Mutes: <white>" + mutes + " <gray>Bans: <white>" + bans);
        lore.add("Online: <white>" + target.isOnline());
        plugin.mutes().mute(target.getUniqueId()).ifPresent(mute -> lore.add("<red>Muted <gray>" + Durations.remaining(mute.expiresAt()) + " left"));
        if (plugin.punishments().banned(target)) {
            lore.add("<dark_red>Banned");
        }
        return lore;
    }
}
