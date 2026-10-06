package io.github.glocation87.admintools.staff;

import io.github.glocation87.admintools.AdminToolsPlugin;
import io.github.glocation87.admintools.Durations;
import io.github.glocation87.admintools.Text;
import io.github.glocation87.admintools.data.Punishment;
import io.github.glocation87.admintools.data.Report;
import io.papermc.paper.ban.BanListType;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.bukkit.OfflinePlayer;
import org.bukkit.ban.ProfileBanList;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class PunishmentService {
    private final AdminToolsPlugin plugin;

    public PunishmentService(AdminToolsPlugin plugin) {
        this.plugin = plugin;
    }

    public List<Punishment> history(UUID id) {
        List<Punishment> list = new ArrayList<>(plugin.data().history(id));
        list.sort((a, b) -> Long.compare(b.time(), a.time()));
        return list;
    }

    public int priors(UUID id, Punishment.Category category) {
        int count = 0;
        for (Punishment punishment : plugin.data().history(id)) {
            if (punishment.category() == category && (punishment.type() == Punishment.Type.MUTE || punishment.type() == Punishment.Type.BAN)) {
                count++;
            }
        }
        return count;
    }

    // Base length for the severity, doubled (by default) for every earlier offense in the category
    public Duration ladder(UUID id, Punishment.Category category, int severity) {
        int priors = priors(id, category);
        if (priors >= plugin.settings().permAfter()) {
            return null;
        }
        List<Duration> ladder = category == Punishment.Category.CHAT ? plugin.settings().chatLadder() : plugin.settings().gameplayLadder();
        Duration base = ladder.get(Math.max(0, Math.min(2, severity - 1)));
        double factor = Math.pow(plugin.settings().escalation(), priors);
        return Duration.ofMillis((long) (base.toMillis() * factor));
    }

    public void warn(OfflinePlayer target, String reason, CommandSender by) {
        record(target, new Punishment(Punishment.Type.WARN, Punishment.Category.OTHER, 0, name(by), System.currentTimeMillis(), 0, reason));
        Player online = target.getPlayer();
        if (online != null) {
            online.showTitle(net.kyori.adventure.title.Title.title(Text.mm("<red><b>WARNING"), Text.mm("<gray>" + Text.escape(reason))));
            Text.send(online, "<red>You have been warned: <white>" + Text.escape(reason));
        }
        plugin.staffChat().notice("<yellow>" + name(by) + " <gray>warned <yellow>" + target.getName() + "<gray>: <white>" + Text.escape(reason), null);
    }

    public void kick(Player target, String reason, CommandSender by) {
        record(target, new Punishment(Punishment.Type.KICK, Punishment.Category.OTHER, 0, name(by), System.currentTimeMillis(), 0, reason));
        target.kick(Text.mm("<red>You were kicked.\n\n<gray>" + Text.escape(reason)));
        plugin.staffChat().notice("<yellow>" + name(by) + " <gray>kicked <yellow>" + target.getName() + "<gray>: <white>" + Text.escape(reason), null);
    }

    public void mute(OfflinePlayer target, Duration duration, String reason, CommandSender by, Punishment.Category category, int severity) {
        long expires = duration == null ? -1 : System.currentTimeMillis() + duration.toMillis();
        record(target, new Punishment(Punishment.Type.MUTE, category, severity, name(by), System.currentTimeMillis(), expires, reason));
        plugin.mutes().mute(target, duration, reason, name(by));
    }

    public void ban(OfflinePlayer target, Duration duration, String reason, CommandSender by, Punishment.Category category, int severity) {
        long expires = duration == null ? -1 : System.currentTimeMillis() + duration.toMillis();
        record(target, new Punishment(Punishment.Type.BAN, category, severity, name(by), System.currentTimeMillis(), expires, reason));
        target.ban(reason, duration, name(by));
        Player online = target.getPlayer();
        if (online != null) {
            online.kick(Text.mm("<red>You are banned for " + Durations.format(duration) + ".\n\n<gray>" + Text.escape(reason)));
        }
        plugin.staffChat().notice("<yellow>" + name(by) + " <gray>banned <yellow>" + target.getName() + " <gray>for " + Durations.format(duration)
            + ": <white>" + Text.escape(reason), null);
    }

    public boolean banned(OfflinePlayer target) {
        return plugin.getServer().getBanList(BanListType.PROFILE).isBanned(target.getPlayerProfile());
    }

    public boolean unban(OfflinePlayer target, CommandSender by) {
        ProfileBanList bans = plugin.getServer().getBanList(BanListType.PROFILE);
        if (!bans.isBanned(target.getPlayerProfile())) {
            return false;
        }
        bans.pardon(target.getPlayerProfile());
        plugin.staffChat().notice("<yellow>" + name(by) + " <gray>unbanned <yellow>" + target.getName(), null);
        return true;
    }

    public void report(Player reporter, Player target, String reason) {
        Report report = new Report(UUID.randomUUID(), reporter.getUniqueId(), reporter.getName(), target.getUniqueId(), target.getName(), reason,
            System.currentTimeMillis());
        plugin.data().addReport(report);
        Text.send(reporter, "<green>Thanks, staff have been notified.");
        plugin.staffChat().notice("<red>Report <gray>from <yellow>" + reporter.getName() + " <gray>about <yellow>" + target.getName()
            + "<gray>: <white>" + Text.escape(reason) + " <dark_gray>(/reports)", null);
    }

    private void record(OfflinePlayer target, Punishment punishment) {
        plugin.data().addPunishment(target.getUniqueId(), punishment);
    }

    private static String name(CommandSender sender) {
        return sender instanceof Player player ? player.getName() : "Console";
    }
}
