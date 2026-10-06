package io.github.glocation87.admintools.menu.players;

import io.github.glocation87.admintools.AdminToolsPlugin;
import io.github.glocation87.admintools.Text;
import io.github.glocation87.admintools.menu.Icons;
import io.github.glocation87.admintools.menu.Menu;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public final class PlayerDebugMenu extends Menu {
    private final Player target;

    public PlayerDebugMenu(AdminToolsPlugin plugin, Player viewer, Menu parent, Player target) {
        super(plugin, viewer, parent, "<aqua><b>Debug: " + target.getName(), 4);
        this.target = target;
    }

    @Override
    public boolean live() {
        return true;
    }

    @Override
    protected void draw() {
        if (!target.isOnline()) {
            back();
            return;
        }
        border();
        navigation();
        set(10, Icons.of(Material.ENDER_EYE, "<aqua>Connection", connection()));
        button(11, Icons.of(Material.COMPASS, "<aqua>Position", position()), () -> viewer.teleportAsync(target.getLocation()));
        set(12, Icons.of(Material.REDSTONE, "<aqua>State", state()));
        set(13, Icons.of(Material.NAME_TAG, "<aqua>Account", account()));
        set(14, Icons.of(Material.BOOK, "<aqua>Statistics", statistics()));
        set(15, Icons.of(Material.IRON_CHESTPLATE, "<aqua>Attributes", attributes()));
        set(16, Icons.of(Material.CHEST, "<aqua>Inventory", inventory()));
        button(19, Icons.of(Material.POTION, "<aqua>Effects", "Active: <white>" + target.getActivePotionEffects().size(), "", "<yellow>Click to manage"),
            () -> new EffectsMenu(plugin, viewer, this, target).open());
        set(20, Icons.of(Material.SADDLE, "<aqua>Riding", riding()));
    }

    private List<String> connection() {
        List<String> lore = new ArrayList<>();
        InetSocketAddress address = target.getAddress();
        lore.add("Ping: " + Text.color(target.getPing(), 100, 250) + target.getPing() + "ms");
        lore.add("Address: <white>" + (address == null ? "?" : address.getAddress().getHostAddress() + ":" + address.getPort()));
        lore.add("Client: <white>" + (target.getClientBrandName() == null ? "unknown" : target.getClientBrandName()));
        lore.add("Locale: <white>" + target.locale().toLanguageTag());
        lore.add("View distance: <white>" + target.getClientViewDistance() + " <gray>(server sends " + target.getSendViewDistance() + ")");
        return lore;
    }

    private List<String> position() {
        Location at = target.getLocation();
        List<String> lore = new ArrayList<>();
        lore.add("World: <white>" + at.getWorld().getName());
        lore.add("XYZ: <white>" + Text.number(at.getX()) + ", " + Text.number(at.getY()) + ", " + Text.number(at.getZ()));
        lore.add("Yaw/pitch: <white>" + Text.number(at.getYaw()) + " / " + Text.number(at.getPitch()));
        lore.add("Chunk: <white>" + at.getBlockX() / 16 + ", " + at.getBlockZ() / 16);
        lore.add("Biome: <white>" + target.getLocation().getBlock().getBiome().getKey().getKey());
        lore.add("Velocity: <white>" + Text.number(target.getVelocity().length() * 20) + " blocks/s");
        lore.add("");
        lore.add("<yellow>Click to teleport there");
        return lore;
    }

    private List<String> state() {
        List<String> lore = new ArrayList<>();
        lore.add("Mode: <white>" + target.getGameMode().name().toLowerCase(Locale.ROOT));
        lore.add("Health: <white>" + Text.number(target.getHealth()) + " <gray>Food: <white>" + target.getFoodLevel()
            + " <gray>Sat: <white>" + Text.number(target.getSaturation()));
        lore.add("XP: <white>level " + target.getLevel() + " <gray>(" + target.getTotalExperience() + " points)");
        lore.add("Flying: <white>" + target.isFlying() + " <gray>Allowed: <white>" + target.getAllowFlight());
        lore.add("Sneaking: <white>" + target.isSneaking() + " <gray>Sprinting: <white>" + target.isSprinting()
            + " <gray>Gliding: <white>" + target.isGliding());
        lore.add("In water: <white>" + target.isInWater() + " <gray>In lava: <white>" + target.isInLava());
        lore.add("Fire: <white>" + target.getFireTicks() + "t <gray>Freeze: <white>" + target.getFreezeTicks() + "t");
        lore.add("Invulnerable: <white>" + target.isInvulnerable());
        return lore;
    }

    private List<String> account() {
        List<String> lore = new ArrayList<>();
        lore.add("UUID: <white>" + target.getUniqueId());
        lore.add("Op: <white>" + target.isOp() + " <gray>Whitelisted: <white>" + target.isWhitelisted());
        lore.add("First played: <white>" + Text.date(target.getFirstPlayed()));
        lore.add("Frozen: <white>" + plugin.freeze().frozen(target) + " <gray>Muted: <white>" + plugin.mutes().mute(target.getUniqueId()).isPresent());
        lore.add("Staff mode: <white>" + plugin.staffMode().active(target) + " <gray>Vanished: <white>" + plugin.vanish().vanished(target));
        return lore;
    }

    private List<String> statistics() {
        List<String> lore = new ArrayList<>();
        lore.add("Play time: <white>" + Text.number(target.getStatistic(Statistic.PLAY_ONE_MINUTE) / 72000.0) + "h");
        lore.add("Deaths: <white>" + target.getStatistic(Statistic.DEATHS));
        lore.add("Player kills: <white>" + target.getStatistic(Statistic.PLAYER_KILLS) + " <gray>Mob kills: <white>"
            + target.getStatistic(Statistic.MOB_KILLS));
        lore.add("Walked: <white>" + Text.number(target.getStatistic(Statistic.WALK_ONE_CM) / 100000.0) + "km");
        lore.add("Jumps: <white>" + target.getStatistic(Statistic.JUMP));
        lore.add("Damage dealt: <white>" + target.getStatistic(Statistic.DAMAGE_DEALT) / 10);
        return lore;
    }

    private List<String> attributes() {
        List<String> lore = new ArrayList<>();
        for (Attribute attribute : List.of(Attribute.MAX_HEALTH, Attribute.MOVEMENT_SPEED, Attribute.ATTACK_DAMAGE, Attribute.ARMOR,
            Attribute.ARMOR_TOUGHNESS, Attribute.KNOCKBACK_RESISTANCE)) {
            AttributeInstance instance = target.getAttribute(attribute);
            if (instance != null) {
                String name = attribute.getKey().getKey().replace('_', ' ');
                lore.add(name + ": <white>" + Text.number(instance.getValue()) + " <dark_gray>(base " + Text.number(instance.getBaseValue()) + ")");
            }
        }
        return lore;
    }

    private List<String> inventory() {
        int stacks = 0;
        int items = 0;
        for (ItemStack stack : target.getInventory().getStorageContents()) {
            if (stack != null && !stack.isEmpty()) {
                stacks++;
                items += stack.getAmount();
            }
        }
        List<String> lore = new ArrayList<>();
        lore.add("Stacks: <white>" + stacks + " <gray>Items: <white>" + items);
        lore.add("Main hand: <white>" + Text.plain(target.getInventory().getItemInMainHand().displayName()));
        lore.add("Off hand: <white>" + Text.plain(target.getInventory().getItemInOffHand().displayName()));
        for (ItemStack armor : target.getInventory().getArmorContents()) {
            if (armor != null && !armor.isEmpty()) {
                lore.add("Wearing: <white>" + Text.plain(armor.displayName()));
            }
        }
        return lore;
    }

    private List<String> riding() {
        List<String> lore = new ArrayList<>();
        Entity vehicle = target.getVehicle();
        lore.add("Vehicle: <white>" + (vehicle == null ? "none" : vehicle.getType().getKey().getKey()));
        lore.add("Passengers: <white>" + target.getPassengers().size());
        return lore;
    }
}
