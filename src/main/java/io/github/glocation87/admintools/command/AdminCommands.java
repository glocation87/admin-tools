package io.github.glocation87.admintools.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.glocation87.admintools.AdminToolsPlugin;
import io.github.glocation87.admintools.Durations;
import io.github.glocation87.admintools.Text;
import io.github.glocation87.admintools.data.Punishment;
import io.github.glocation87.admintools.inspect.BlockInspectMenu;
import io.github.glocation87.admintools.inspect.EntityInspectMenu;
import io.github.glocation87.admintools.menu.players.HistoryMenu;
import io.github.glocation87.admintools.menu.players.NotesMenu;
import io.github.glocation87.admintools.menu.players.PeekMenu;
import io.github.glocation87.admintools.menu.players.PlayerListMenu;
import io.github.glocation87.admintools.menu.players.PlayerMenu;
import io.github.glocation87.admintools.menu.players.PunishMenu;
import io.github.glocation87.admintools.menu.players.ReportsMenu;
import io.github.glocation87.admintools.menu.server.DashboardMenu;
import io.github.glocation87.admintools.menu.server.StaffMenu;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.PlayerProfileListResolver;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.function.Predicate;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.OfflinePlayer;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;

public final class AdminCommands {
    private static final String STAFF = "admintools.staff";
    private static final String ADMIN = "admintools.admin";

    private final AdminToolsPlugin plugin;

    public AdminCommands(AdminToolsPlugin plugin) {
        this.plugin = plugin;
    }

    public void register() {
        plugin.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            Commands registrar = event.registrar();
            registrar.register(staff(), "Toggle staff mode");
            registrar.register(simple("admin", ADMIN, player -> new DashboardMenu(plugin, player).open()), "Server dashboard", List.of("dashboard"));
            registrar.register(simple("players", STAFF, player -> new PlayerListMenu(plugin, player).open()), "Player list menu");
            registrar.register(simple("reports", STAFF, player -> new ReportsMenu(plugin, player, null).open()), "Open player reports");
            registrar.register(simple("vanish", STAFF, player -> plugin.vanish().toggle(player)), "Toggle vanish", List.of("v"));
            registrar.register(simple("cspy", STAFF, player -> plugin.commandSpy().toggle(player)), "See the commands players run", List.of("commandspy"));
            registrar.register(chat("sc", STAFF), "Staff chat", List.of("staffchat"));
            registrar.register(inspect(), "Inspect what you are looking at");

            registrar.register(online("freeze", STAFF, (sender, target) -> plugin.freeze().toggle(target, (Player) sender), true), "Freeze a player in place");
            registrar.register(online("tphere", STAFF, (sender, target) -> {
                target.teleportAsync(((Player) sender).getLocation());
                Text.send(sender, "<gray>Brought <white>" + target.getName());
            }, true), "Bring a player to you");
            registrar.register(online("spectate", STAFF, (sender, target) -> {
                Player player = (Player) sender;
                player.setGameMode(GameMode.SPECTATOR);
                player.teleportAsync(target.getLocation()).thenRun(() -> player.setSpectatorTarget(target));
            }, true), "Spectate a player");
            registrar.register(online("invsee", STAFF, (sender, target) -> PeekMenu.inventory(plugin, (Player) sender, null, target).open(), true),
                "Look inside a player's inventory");
            registrar.register(online("ecsee", STAFF, (sender, target) -> PeekMenu.enderChest(plugin, (Player) sender, null, target).open(), true),
                "Look inside a player's ender chest", List.of("endersee"));
            registrar.register(online("manage", STAFF, (sender, target) -> new PlayerMenu(plugin, (Player) sender, null, target).open(), true),
                "Open the player menu");
            registrar.register(online("whois", STAFF, this::whois, false), "Everything about a player");
            registrar.register(online("ping", STAFF, (sender, target) -> Text.send(sender, "<white>" + target.getName() + "<gray>: " + target.getPing() + "ms"), false),
                "Show a player's ping");

            registrar.register(offline("punish", STAFF, (sender, target) -> new PunishMenu(plugin, (Player) sender, null, target).open(), true), "Punish menu");
            registrar.register(offline("history", STAFF, (sender, target) -> new HistoryMenu(plugin, (Player) sender, null, target).open(), true),
                "Punishment history");
            registrar.register(offline("notes", STAFF, (sender, target) -> new NotesMenu(plugin, (Player) sender, null, target).open(), true), "Staff notes");
            registrar.register(offline("seen", STAFF, this::seen, false), "When a player was last on");
            registrar.register(offline("alts", STAFF, this::alts, false), "Accounts that shared an address");
            registrar.register(offline("unmute", STAFF, (sender, target) -> {
                if (!plugin.mutes().unmute(target, name(sender))) {
                    Text.send(sender, "<red>" + target.getName() + " is not muted.");
                }
            }, false), "Unmute a player");
            registrar.register(offline("unban", STAFF, (sender, target) -> {
                if (!plugin.punishments().unban(target, sender)) {
                    Text.send(sender, "<red>" + target.getName() + " is not banned.");
                }
            }, false), "Unban a player");

            registrar.register(reasoned("warn", STAFF, true, (sender, target, reason) -> plugin.punishments().warn(target, reason, sender)), "Warn a player");
            registrar.register(reasoned("kick", STAFF, true, (sender, target, reason) -> {
                if (target.getPlayer() == null) {
                    Text.send(sender, "<red>" + target.getName() + " is not online.");
                } else {
                    plugin.punishments().kick(target.getPlayer(), reason, sender);
                }
            }), "Kick a player");
            registrar.register(timed("mute", (sender, target, duration, reason) ->
                plugin.punishments().mute(target, duration, reason, sender, Punishment.Category.CHAT, 0)), "Mute a player, e.g. /mute Steve 2h spam");
            registrar.register(timed("ban", (sender, target, duration, reason) ->
                plugin.punishments().ban(target, duration, reason, sender, Punishment.Category.GAMEPLAY, 0)), "Ban a player, e.g. /ban Steve 7d x-ray");
            registrar.register(report(), "Report a player to staff");

            registrar.register(self("fly", STAFF, target -> {
                target.setAllowFlight(!target.getAllowFlight());
                return "Flight " + (target.getAllowFlight() ? "<green>on" : "<red>off");
            }), "Toggle flight");
            registrar.register(self("god", STAFF, target -> {
                target.setInvulnerable(!target.isInvulnerable());
                return "God mode " + (target.isInvulnerable() ? "<green>on" : "<red>off");
            }), "Toggle invulnerability");
            registrar.register(self("heal", STAFF, target -> {
                AttributeInstance max = target.getAttribute(Attribute.MAX_HEALTH);
                target.setHealth(max == null ? 20 : max.getValue());
                target.setFireTicks(0);
                return "<green>Healed";
            }), "Restore health");
            registrar.register(self("feed", STAFF, target -> {
                target.setFoodLevel(20);
                target.setSaturation(20);
                return "<green>Fed";
            }), "Restore hunger");
            registrar.register(gamemode(), "Change game mode", List.of("gamemode"));
            for (GameMode mode : GameMode.values()) {
                String alias = "gm" + switch (mode) {
                    case SURVIVAL -> "s";
                    case CREATIVE -> "c";
                    case ADVENTURE -> "a";
                    case SPECTATOR -> "sp";
                };
                registrar.register(self(alias, STAFF, target -> {
                    target.setGameMode(mode);
                    return "Game mode <white>" + mode.name().toLowerCase(Locale.ROOT);
                }), "Switch to " + mode.name().toLowerCase(Locale.ROOT));
            }
            registrar.register(speed(), "Set walk or fly speed, 1 to 10");
            registrar.register(tpall(), "Teleport everyone to you");

            registrar.register(simpleAny("clearchat", STAFF, sender -> plugin.chatControl().clear(name(sender))), "Clear chat for players", List.of("cc"));
            registrar.register(simpleAny("chatlock", STAFF, sender -> plugin.chatControl().toggleLock(name(sender))), "Lock chat for players");
            registrar.register(Commands.literal("slowchat").requires(perm(STAFF))
                .executes(ctx -> run(ctx, () -> plugin.chatControl().slow(0, name(ctx.getSource().getSender()))))
                .then(Commands.argument("seconds", IntegerArgumentType.integer(0, 3600))
                    .executes(ctx -> run(ctx, () -> plugin.chatControl().slow(IntegerArgumentType.getInteger(ctx, "seconds"), name(ctx.getSource().getSender())))))
                .build(), "Limit how often players can chat");
            registrar.register(Commands.literal("broadcast").requires(perm(STAFF))
                .then(Commands.argument("message", StringArgumentType.greedyString()).executes(ctx -> run(ctx, () ->
                    Bukkit.broadcast(Text.mm("<dark_gray>[<light_purple>Broadcast</light_purple>]</dark_gray> <white>"
                        + Text.escape(StringArgumentType.getString(ctx, "message")))))))
                .build(), "Message everyone", List.of("alert"));
            registrar.register(Commands.literal("admintools").requires(perm(ADMIN))
                .then(Commands.literal("reload").executes(ctx -> run(ctx, () -> {
                    plugin.reload();
                    Text.send(ctx.getSource().getSender(), "<green>Config reloaded");
                })))
                .build(), "AdminTools admin");
        });
    }

    private interface OnlineAction {
        void run(CommandSender sender, Player target);
    }

    private interface OfflineAction {
        void run(CommandSender sender, OfflinePlayer target);
    }

    private interface ReasonAction {
        void run(CommandSender sender, OfflinePlayer target, String reason);
    }

    private interface TimedAction {
        void run(CommandSender sender, OfflinePlayer target, Duration duration, String reason);
    }

    private interface SelfAction {
        String run(Player target);
    }

    private static Predicate<CommandSourceStack> perm(String permission) {
        return source -> source.getSender().hasPermission(permission);
    }

    private static String name(CommandSender sender) {
        return sender instanceof Player player ? player.getName() : "Console";
    }

    private static int run(CommandContext<CommandSourceStack> ctx, Runnable action) {
        action.run();
        return Command.SINGLE_SUCCESS;
    }

    // Player only commands answer the console with a hint instead of a stack trace
    private static Player playerOnly(CommandContext<CommandSourceStack> ctx) {
        if (ctx.getSource().getSender() instanceof Player player) {
            return player;
        }
        Text.send(ctx.getSource().getSender(), "<red>Players only.");
        return null;
    }

    private static Player onlineTarget(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        List<Player> players = ctx.getArgument("target", PlayerSelectorArgumentResolver.class).resolve(ctx.getSource());
        return players.isEmpty() ? null : players.getFirst();
    }

    private OfflinePlayer offlineTarget(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        Collection<com.destroystokyo.paper.profile.PlayerProfile> profiles = ctx.getArgument("target", PlayerProfileListResolver.class).resolve(ctx.getSource());
        for (com.destroystokyo.paper.profile.PlayerProfile profile : profiles) {
            UUID id = profile.getId();
            if (id != null) {
                return plugin.getServer().getOfflinePlayer(id);
            }
            if (profile.getName() != null) {
                return plugin.getServer().getOfflinePlayer(profile.getName());
            }
        }
        return null;
    }

    private LiteralArgumentBuilder<CommandSourceStack> base(String name, String permission) {
        return Commands.literal(name).requires(perm(permission));
    }

    private com.mojang.brigadier.tree.LiteralCommandNode<CommandSourceStack> simple(String name, String permission, java.util.function.Consumer<Player> action) {
        return base(name, permission).executes(ctx -> {
            Player player = playerOnly(ctx);
            if (player != null) {
                action.accept(player);
            }
            return Command.SINGLE_SUCCESS;
        }).build();
    }

    private com.mojang.brigadier.tree.LiteralCommandNode<CommandSourceStack> simpleAny(String name, String permission,
                                                                                        java.util.function.Consumer<CommandSender> action) {
        return base(name, permission).executes(ctx -> run(ctx, () -> action.accept(ctx.getSource().getSender()))).build();
    }

    private com.mojang.brigadier.tree.LiteralCommandNode<CommandSourceStack> online(String name, String permission, OnlineAction action, boolean playersOnly) {
        return base(name, permission).then(Commands.argument("target", ArgumentTypes.player()).executes(ctx -> {
            CommandSender sender = ctx.getSource().getSender();
            if (playersOnly && playerOnly(ctx) == null) {
                return Command.SINGLE_SUCCESS;
            }
            Player target = onlineTarget(ctx);
            if (target == null) {
                Text.send(sender, "<red>No such player online.");
            } else {
                action.run(sender, target);
            }
            return Command.SINGLE_SUCCESS;
        })).build();
    }

    private com.mojang.brigadier.tree.LiteralCommandNode<CommandSourceStack> offline(String name, String permission, OfflineAction action, boolean playersOnly) {
        return base(name, permission).then(Commands.argument("target", ArgumentTypes.playerProfiles()).executes(ctx -> {
            CommandSender sender = ctx.getSource().getSender();
            if (playersOnly && playerOnly(ctx) == null) {
                return Command.SINGLE_SUCCESS;
            }
            OfflinePlayer target = offlineTarget(ctx);
            if (target == null) {
                Text.send(sender, "<red>Unknown player.");
            } else {
                action.run(sender, target);
            }
            return Command.SINGLE_SUCCESS;
        })).build();
    }

    private com.mojang.brigadier.tree.LiteralCommandNode<CommandSourceStack> reasoned(String name, String permission, boolean required, ReasonAction action) {
        var target = Commands.argument("target", ArgumentTypes.playerProfiles());
        if (!required) {
            target.executes(ctx -> reasoned(ctx, action, "No reason given"));
        }
        target.then(Commands.argument("reason", StringArgumentType.greedyString())
            .executes(ctx -> reasoned(ctx, action, StringArgumentType.getString(ctx, "reason"))));
        return base(name, permission).then(target).build();
    }

    private int reasoned(CommandContext<CommandSourceStack> ctx, ReasonAction action, String reason) throws CommandSyntaxException {
        OfflinePlayer target = offlineTarget(ctx);
        if (target == null) {
            Text.send(ctx.getSource().getSender(), "<red>Unknown player.");
        } else {
            action.run(ctx.getSource().getSender(), target, reason);
        }
        return Command.SINGLE_SUCCESS;
    }

    // /cmd <target> [duration] [reason], a second word that is not a duration starts the reason
    private com.mojang.brigadier.tree.LiteralCommandNode<CommandSourceStack> timed(String name, TimedAction action) {
        Duration fallback = plugin.settings().defaultMute();
        return base(name, STAFF).then(Commands.argument("target", ArgumentTypes.playerProfiles())
            .executes(ctx -> timed(ctx, action, fallback, "No reason given"))
            .then(Commands.argument("duration", StringArgumentType.word())
                .executes(ctx -> timed(ctx, action, StringArgumentType.getString(ctx, "duration"), fallback, ""))
                .then(Commands.argument("reason", StringArgumentType.greedyString())
                    .executes(ctx -> timed(ctx, action, StringArgumentType.getString(ctx, "duration"), fallback,
                        StringArgumentType.getString(ctx, "reason")))))).build();
    }

    private int timed(CommandContext<CommandSourceStack> ctx, TimedAction action, String first, Duration fallback, String rest) throws CommandSyntaxException {
        try {
            return timed(ctx, action, Durations.parse(first), rest.isEmpty() ? "No reason given" : rest);
        } catch (IllegalArgumentException e) {
            return timed(ctx, action, fallback, (first + " " + rest).trim());
        }
    }

    private int timed(CommandContext<CommandSourceStack> ctx, TimedAction action, Duration duration, String reason) throws CommandSyntaxException {
        OfflinePlayer target = offlineTarget(ctx);
        if (target == null) {
            Text.send(ctx.getSource().getSender(), "<red>Unknown player.");
        } else {
            action.run(ctx.getSource().getSender(), target, duration, reason);
        }
        return Command.SINGLE_SUCCESS;
    }

    // /cmd [target], without a target it applies to yourself
    private com.mojang.brigadier.tree.LiteralCommandNode<CommandSourceStack> self(String name, String permission, SelfAction action) {
        return base(name, permission)
            .executes(ctx -> {
                Player player = playerOnly(ctx);
                if (player != null) {
                    Text.send(player, action.run(player));
                }
                return Command.SINGLE_SUCCESS;
            })
            .then(Commands.argument("target", ArgumentTypes.player()).executes(ctx -> {
                Player target = onlineTarget(ctx);
                if (target != null) {
                    String result = action.run(target);
                    Text.send(target, result);
                    if (ctx.getSource().getSender() != target) {
                        Text.send(ctx.getSource().getSender(), "<white>" + target.getName() + "<gray>: " + result);
                    }
                }
                return Command.SINGLE_SUCCESS;
            })).build();
    }

    private com.mojang.brigadier.tree.LiteralCommandNode<CommandSourceStack> staff() {
        return base("staff", STAFF)
            .executes(ctx -> {
                Player player = playerOnly(ctx);
                if (player != null) {
                    plugin.staffMode().toggle(player);
                }
                return Command.SINGLE_SUCCESS;
            })
            .then(Commands.literal("list").executes(ctx -> {
                if (ctx.getSource().getSender() instanceof Player player) {
                    new StaffMenu(plugin, player, null).open();
                } else {
                    StringBuilder names = new StringBuilder();
                    for (Player online : plugin.getServer().getOnlinePlayers()) {
                        if (online.hasPermission(STAFF)) {
                            names.append(names.isEmpty() ? "" : ", ").append(online.getName());
                        }
                    }
                    Text.send(ctx.getSource().getSender(), "<gray>Staff online: <white>" + (names.isEmpty() ? "nobody" : names));
                }
                return Command.SINGLE_SUCCESS;
            })).build();
    }

    private com.mojang.brigadier.tree.LiteralCommandNode<CommandSourceStack> chat(String name, String permission) {
        return base(name, permission)
            .executes(ctx -> {
                Player player = playerOnly(ctx);
                if (player != null) {
                    plugin.staffChat().toggle(player);
                }
                return Command.SINGLE_SUCCESS;
            })
            .then(Commands.argument("message", StringArgumentType.greedyString()).executes(ctx ->
                run(ctx, () -> plugin.staffChat().send(ctx.getSource().getSender(), StringArgumentType.getString(ctx, "message")))))
            .build();
    }

    private com.mojang.brigadier.tree.LiteralCommandNode<CommandSourceStack> gamemode() {
        LiteralArgumentBuilder<CommandSourceStack> root = base("gm", STAFF);
        for (GameMode mode : GameMode.values()) {
            String lower = mode.name().toLowerCase(Locale.ROOT);
            root.then(Commands.literal(lower)
                .executes(ctx -> {
                    Player player = playerOnly(ctx);
                    if (player != null) {
                        player.setGameMode(mode);
                        Text.send(player, "Game mode <white>" + lower);
                    }
                    return Command.SINGLE_SUCCESS;
                })
                .then(Commands.argument("target", ArgumentTypes.player()).executes(ctx -> {
                    Player target = onlineTarget(ctx);
                    if (target != null) {
                        target.setGameMode(mode);
                        Text.send(ctx.getSource().getSender(), "<white>" + target.getName() + "<gray>: " + lower);
                    }
                    return Command.SINGLE_SUCCESS;
                })));
        }
        return root.build();
    }

    private com.mojang.brigadier.tree.LiteralCommandNode<CommandSourceStack> speed() {
        return base("speed", STAFF).then(Commands.argument("speed", FloatArgumentType.floatArg(0, 10))
            .executes(ctx -> {
                Player player = playerOnly(ctx);
                if (player != null) {
                    Text.send(player, speed(player, FloatArgumentType.getFloat(ctx, "speed")));
                }
                return Command.SINGLE_SUCCESS;
            })
            .then(Commands.argument("target", ArgumentTypes.player()).executes(ctx -> {
                Player target = onlineTarget(ctx);
                if (target != null) {
                    Text.send(ctx.getSource().getSender(), speed(target, FloatArgumentType.getFloat(ctx, "speed")));
                }
                return Command.SINGLE_SUCCESS;
            }))).build();
    }

    // 1 is vanilla, 10 is as fast as the client allows, applies to whichever mode they are in
    private static String speed(Player target, float level) {
        if (target.isFlying()) {
            target.setFlySpeed(Math.min(1, level * 0.1f));
            return "Fly speed <white>" + Text.number(level);
        }
        target.setWalkSpeed(Math.min(1, level * 0.2f));
        return "Walk speed <white>" + Text.number(level);
    }

    private com.mojang.brigadier.tree.LiteralCommandNode<CommandSourceStack> tpall() {
        return base("tpall", ADMIN).executes(ctx -> {
            Player player = playerOnly(ctx);
            if (player != null) {
                int count = 0;
                for (Player other : plugin.getServer().getOnlinePlayers()) {
                    if (other != player) {
                        other.teleportAsync(player.getLocation());
                        count++;
                    }
                }
                Text.send(player, "<gray>Teleported <white>" + count + " <gray>players to you");
            }
            return Command.SINGLE_SUCCESS;
        }).build();
    }

    private com.mojang.brigadier.tree.LiteralCommandNode<CommandSourceStack> report() {
        return Commands.literal("report").then(Commands.argument("target", ArgumentTypes.player())
            .then(Commands.argument("reason", StringArgumentType.greedyString()).executes(ctx -> {
                Player reporter = playerOnly(ctx);
                if (reporter == null) {
                    return Command.SINGLE_SUCCESS;
                }
                Player target = onlineTarget(ctx);
                if (target == null) {
                    Text.send(reporter, "<red>No such player online.");
                } else if (target == reporter) {
                    Text.send(reporter, "<red>You cannot report yourself.");
                } else {
                    plugin.punishments().report(reporter, target, StringArgumentType.getString(ctx, "reason"));
                }
                return Command.SINGLE_SUCCESS;
            }))).build();
    }

    private com.mojang.brigadier.tree.LiteralCommandNode<CommandSourceStack> inspect() {
        return base("inspect", STAFF).executes(ctx -> {
            Player player = playerOnly(ctx);
            if (player == null) {
                return Command.SINGLE_SUCCESS;
            }
            RayTraceResult hit = player.getWorld().rayTrace(player.getEyeLocation(), player.getEyeLocation().getDirection(), 32, org.bukkit.FluidCollisionMode.NEVER,
                true, 0.5, entity -> entity != player);
            if (hit == null) {
                Text.send(player, "<red>Nothing within 32 blocks.");
            } else if (hit.getHitEntity() instanceof Player other) {
                new PlayerMenu(plugin, player, null, other).open();
            } else if (hit.getHitEntity() instanceof Entity entity) {
                new EntityInspectMenu(plugin, player, null, entity).open();
            } else if (hit.getHitBlock() != null) {
                new BlockInspectMenu(plugin, player, null, hit.getHitBlock()).open();
            }
            return Command.SINGLE_SUCCESS;
        }).build();
    }

    private void whois(CommandSender sender, Player target) {
        Text.send(sender, "<gold>" + target.getName() + " <dark_gray>" + target.getUniqueId());
        Text.send(sender, "<gray>Where: <white>" + Text.coords(target.getLocation()) + " <gray>Mode: <white>" + target.getGameMode().name().toLowerCase(Locale.ROOT));
        Text.send(sender, "<gray>Ping: <white>" + target.getPing() + "ms <gray>Client: <white>" + (target.getClientBrandName() == null ? "?" : target.getClientBrandName())
            + " <gray>Address: <white>" + (target.getAddress() == null ? "?" : target.getAddress().getAddress().getHostAddress()));
        Text.send(sender, "<gray>Health: <white>" + Text.number(target.getHealth()) + " <gray>Food: <white>" + target.getFoodLevel()
            + " <gray>Level: <white>" + target.getLevel());
        Text.send(sender, "<gray>Op: <white>" + target.isOp() + " <gray>Flying: <white>" + target.isFlying() + " <gray>Frozen: <white>" + plugin.freeze().frozen(target)
            + " <gray>Muted: <white>" + plugin.mutes().mute(target.getUniqueId()).isPresent());
        Text.send(sender, "<gray>First joined: <white>" + Text.date(target.getFirstPlayed()) + " <gray>Record: <white>"
            + plugin.data().history(target.getUniqueId()).size() + " entries, " + plugin.data().notes(target.getUniqueId()).size() + " notes");
    }

    private void seen(CommandSender sender, OfflinePlayer target) {
        if (target.isOnline()) {
            Text.send(sender, "<white>" + target.getName() + " <gray>is online right now.");
        } else if (target.getLastSeen() == 0) {
            Text.send(sender, "<white>" + target.getName() + " <gray>has never joined.");
        } else {
            Text.send(sender, "<white>" + target.getName() + " <gray>was last seen <white>" + Text.date(target.getLastSeen())
                + " <gray>(" + Durations.format(Duration.ofMillis(System.currentTimeMillis() - target.getLastSeen())) + " ago)");
        }
    }

    private void alts(CommandSender sender, OfflinePlayer target) {
        StringBuilder names = new StringBuilder();
        for (UUID id : plugin.data().alts(target.getUniqueId())) {
            OfflinePlayer alt = plugin.getServer().getOfflinePlayer(id);
            String name = alt.getName() == null ? id.toString() : alt.getName();
            names.append(names.isEmpty() ? "" : ", ").append(alt.isBanned() ? "<red>" : alt.isOnline() ? "<green>" : "<white>").append(name);
        }
        Text.send(sender, "<gray>Addresses: <white>" + plugin.data().addresses(target.getUniqueId()).size());
        Text.send(sender, "<gray>Alts of <white>" + target.getName() + "<gray>: " + (names.isEmpty() ? "<dark_gray>none" : names));
    }
}
