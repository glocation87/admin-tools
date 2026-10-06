package io.github.glocation87.admintools.inspect;

import io.github.glocation87.admintools.AdminToolsPlugin;
import io.github.glocation87.admintools.Text;
import io.github.glocation87.admintools.menu.Icons;
import io.github.glocation87.admintools.menu.Menu;
import io.github.glocation87.admintools.menu.players.EffectsMenu;
import io.github.glocation87.admintools.menu.server.EntitiesMenu;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.entity.Tameable;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;

public final class EntityInspectMenu extends Menu {
    private final Entity entity;

    public EntityInspectMenu(AdminToolsPlugin plugin, Player viewer, Menu parent, Entity entity) {
        super(plugin, viewer, parent, "<aqua><b>" + name(entity), 5);
        this.entity = entity;
    }

    private static String name(Entity entity) {
        return entity.customName() == null ? entity.getType().getKey().getKey().replace('_', ' ') : Text.plain(entity.customName());
    }

    @Override
    public boolean live() {
        return true;
    }

    @Override
    protected void draw() {
        if (!entity.isValid()) {
            message("<red>That entity is gone.");
            back();
            return;
        }
        border();
        navigation();
        set(4, Icons.of(EntitiesMenu.iconFor(entity.getType()), "<aqua>" + name(entity), "Type: <white>" + entity.getType().getKey().getKey(),
            "Id: <white>" + entity.getEntityId() + " <gray>UUID: <white>" + entity.getUniqueId()));

        set(10, Icons.of(Material.NAME_TAG, "<aqua>Identity", identity()));
        button(11, Icons.of(Material.COMPASS, "<aqua>Position", position()), () -> viewer.teleportAsync(entity.getLocation()));
        if (entity instanceof LivingEntity living) {
            button(12, Icons.of(Material.GOLDEN_APPLE, "<aqua>Health", health(living)), () -> new EffectsMenu(plugin, viewer, this, living).open());
            set(14, Icons.of(Material.IRON_CHESTPLATE, "<aqua>Equipment", equipment(living)));
        }
        if (entity instanceof Mob mob) {
            set(13, Icons.of(Material.ZOMBIE_HEAD, "<aqua>Brain", brain(mob)));
        }
        set(16, Icons.of(Material.MAP, "<aqua>Surroundings", "Entities within 16 blocks: <white>" + entity.getNearbyEntities(16, 16, 16).size(),
            "Chunk: <white>" + entity.getChunk().getX() + ", " + entity.getChunk().getZ(), "Ticking: <white>" + entity.isTicking()));

        if (entity instanceof LivingEntity living) {
            button(19, Icons.toggle(living.hasAI(), "<gold>AI", "Off and it stands still like a statue"), () -> {
                living.setAI(!living.hasAI());
                refresh();
            });
        }
        if (entity instanceof Mob mob) {
            button(20, Icons.toggle(mob.isAware(), "<gold>Aware", "Off and it ignores the world", "but still animates"), () -> {
                mob.setAware(!mob.isAware());
                refresh();
            });
        }
        button(21, Icons.toggle(entity.isGlowing(), "<gold>Glowing", "Outline through walls"), () -> {
            entity.setGlowing(!entity.isGlowing());
            refresh();
        });
        button(22, Icons.toggle(entity.isInvulnerable(), "<gold>Invulnerable"), () -> {
            entity.setInvulnerable(!entity.isInvulnerable());
            refresh();
        });
        button(23, Icons.toggle(entity.isSilent(), "<gold>Silent"), () -> {
            entity.setSilent(!entity.isSilent());
            refresh();
        });
        button(24, Icons.toggle(entity.hasGravity(), "<gold>Gravity"), () -> {
            entity.setGravity(!entity.hasGravity());
            refresh();
        });
        button(25, Icons.toggle(entity.isPersistent(), "<gold>Persistent", "Never despawns"), () -> {
            entity.setPersistent(!entity.isPersistent());
            refresh();
        });

        button(28, Icons.of(Material.ENDER_PEARL, "<green>Teleport To It"), () -> viewer.teleportAsync(entity.getLocation()));
        button(29, Icons.of(Material.ENDER_EYE, "<green>Bring It Here"), () -> entity.teleportAsync(viewer.getLocation()));
        if (entity instanceof LivingEntity living) {
            button(30, Icons.of(Material.SKELETON_SKULL, "<red>Kill", "Dies properly, drops loot"), () -> {
                living.setHealth(0);
                message("<red>Killed the " + name(entity));
            });
        }
        button(31, Icons.of(Material.BARRIER, "<red>Remove", "Vanishes without drops"), () ->
            confirm("<red>Remove " + name(entity) + "?", Icons.of(Material.BARRIER, "<red>Remove"), () -> {
                entity.remove();
                message("<red>Removed the " + name(entity));
            }));
        button(32, Icons.of(Material.FIRE_CHARGE, "<gold>Ignite 5s"), () -> entity.setFireTicks(100));
        button(33, Icons.of(Material.SLIME_BALL, "<light_purple>Launch", "A little hop upwards"), () ->
            entity.setVelocity(entity.getVelocity().setY(1.2)));
    }

    private List<String> identity() {
        List<String> lore = new ArrayList<>();
        lore.add("Ticks lived: <white>" + entity.getTicksLived() + " <gray>(" + entity.getTicksLived() / 20 + "s)");
        lore.add("Spawned by: <white>" + entity.getEntitySpawnReason().name().toLowerCase(Locale.ROOT));
        lore.add("Persistent: <white>" + entity.isPersistent() + " <gray>Dead: <white>" + entity.isDead());
        if (entity instanceof LivingEntity living) {
            lore.add("Despawns far away: <white>" + living.getRemoveWhenFarAway());
        }
        if (!entity.getScoreboardTags().isEmpty()) {
            lore.add("Tags: <white>" + String.join(", ", entity.getScoreboardTags()));
        }
        List<String> keys = new ArrayList<>();
        for (NamespacedKey key : entity.getPersistentDataContainer().getKeys()) {
            keys.add(key.toString());
        }
        lore.add("Plugin data: <white>" + (keys.isEmpty() ? "none" : String.join(", ", keys)));
        return lore;
    }

    private List<String> position() {
        Location at = entity.getLocation();
        List<String> lore = new ArrayList<>();
        lore.add("World: <white>" + at.getWorld().getName());
        lore.add("XYZ: <white>" + Text.number(at.getX()) + ", " + Text.number(at.getY()) + ", " + Text.number(at.getZ()));
        lore.add("Yaw/pitch: <white>" + Text.number(at.getYaw()) + " / " + Text.number(at.getPitch()));
        lore.add("Velocity: <white>" + Text.number(entity.getVelocity().length() * 20) + " blocks/s");
        lore.add("Vehicle: <white>" + (entity.getVehicle() == null ? "none" : entity.getVehicle().getType().getKey().getKey())
            + " <gray>Passengers: <white>" + entity.getPassengers().size());
        lore.add("Seen by: <white>" + entity.getTrackedBy().size() + " players");
        lore.add("");
        lore.add("<yellow>Click to teleport there");
        return lore;
    }

    private List<String> health(LivingEntity living) {
        List<String> lore = new ArrayList<>();
        AttributeInstance max = living.getAttribute(Attribute.MAX_HEALTH);
        lore.add("Health: <white>" + Text.number(living.getHealth()) + " <gray>of " + (max == null ? "?" : Text.number(max.getValue())));
        lore.add("No damage ticks: <white>" + living.getNoDamageTicks());
        EntityDamageEvent last = living.getLastDamageCause();
        lore.add("Last damage: <white>" + (last == null ? "none" : last.getCause().name().toLowerCase(Locale.ROOT) + " " + Text.number(last.getFinalDamage())));
        lore.add("Fire: <white>" + living.getFireTicks() + "t <gray>Freeze: <white>" + living.getFreezeTicks() + "t");
        lore.add("Effects: <white>" + living.getActivePotionEffects().size());
        lore.add("");
        lore.add("<yellow>Click to manage effects");
        return lore;
    }

    private List<String> brain(Mob mob) {
        List<String> lore = new ArrayList<>();
        lore.add("Target: <white>" + (mob.getTarget() == null ? "none" : mob.getTarget().getName()));
        lore.add("AI: <white>" + mob.hasAI() + " <gray>Aware: <white>" + mob.isAware());
        lore.add("Leashed: <white>" + mob.isLeashed());
        if (mob instanceof Ageable ageable) {
            lore.add("Adult: <white>" + ageable.isAdult() + " <gray>Age: <white>" + ageable.getAge());
        }
        if (mob instanceof Tameable tameable) {
            lore.add("Tamed: <white>" + tameable.isTamed() + " <gray>Owner: <white>"
                + (tameable.getOwner() == null ? "none" : tameable.getOwner().getName()));
        }
        return lore;
    }

    private List<String> equipment(LivingEntity living) {
        List<String> lore = new ArrayList<>();
        EntityEquipment equipment = living.getEquipment();
        if (equipment == null) {
            lore.add("<dark_gray>None");
            return lore;
        }
        lore.add("Main hand: <white>" + describe(equipment.getItemInMainHand()));
        lore.add("Off hand: <white>" + describe(equipment.getItemInOffHand()));
        lore.add("Head: <white>" + describe(equipment.getHelmet()));
        lore.add("Chest: <white>" + describe(equipment.getChestplate()));
        lore.add("Legs: <white>" + describe(equipment.getLeggings()));
        lore.add("Feet: <white>" + describe(equipment.getBoots()));
        return lore;
    }

    private static String describe(ItemStack item) {
        return item == null || item.isEmpty() ? "empty" : Text.plain(item.displayName()) + " x" + item.getAmount();
    }
}
