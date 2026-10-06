package io.github.glocation87.admintools.staff;

import io.github.glocation87.admintools.menu.Icons;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

public final class StaffItems {
    private final NamespacedKey key;

    public StaffItems(Plugin plugin) {
        this.key = new NamespacedKey(plugin, "staff_item");
    }

    public ItemStack build(StaffItem item, boolean active) {
        Material material = item.material();
        List<String> lore = new ArrayList<>(item.lore());
        if (item.toggle()) {
            material = active ? item.material() : Material.GRAY_DYE;
            lore.add("");
            lore.add(active ? "<green>On" : "<red>Off");
        }
        ItemStack stack = Icons.of(material, item.title(), lore);
        stack.editMeta(meta -> {
            meta.setEnchantmentGlintOverride(true);
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, item.name());
        });
        return stack;
    }

    public Optional<StaffItem> of(ItemStack stack) {
        if (stack == null || !stack.hasItemMeta()) {
            return Optional.empty();
        }
        String id = stack.getItemMeta().getPersistentDataContainer().get(key, PersistentDataType.STRING);
        if (id == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(StaffItem.valueOf(id));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    public void give(Player player, boolean vanished, boolean spying) {
        for (StaffItem item : StaffItem.values()) {
            boolean active = item == StaffItem.VANISH ? vanished : spying;
            player.getInventory().setItem(item.slot(), build(item, active));
        }
    }

    public void update(Player player, StaffItem item, boolean active) {
        player.getInventory().setItem(item.slot(), build(item, active));
    }
}
