package com.github.glocation87.manager;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import com.github.glocation87.AdminToolPlugin;

public class InventoryManager {

    private final AdminToolPlugin plugin;
    private final Map<UUID, PlayerInventoryState> playerInventoryCache = new HashMap<>();

    public InventoryManager(AdminToolPlugin _plugin) {
        this.plugin = _plugin;
    }

    private ItemStack createCustomItem(Material material, String name, String id, ChatColor color, boolean enchant) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(color + "§l" + name);
        if (enchant == true) {
            meta.addEnchant(Enchantment.DURABILITY, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }
        NamespacedKey key = new NamespacedKey(plugin, "staff_item");
        meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, id);
        item.setItemMeta(meta);
        return item;
    }

    private static class PlayerInventoryState {

        private final ItemStack[] contents;
        private final ItemStack[] armorContents;

        public PlayerInventoryState(ItemStack[] _contents, ItemStack[] _armorContents) {
            this.contents = _contents;
            this.armorContents = _armorContents;
        }

        public ItemStack[] getContents() {
            return contents;
        }

        public ItemStack[] getArmorContents() {
            return armorContents;
        }
    }

    public boolean isPlayerCached(UUID playerId) {
        return playerInventoryCache.containsKey(playerId);
    }

    public void clearPlayerCache(UUID playerId) {
        playerInventoryCache.remove(playerId);
    }

    public void cachePlayerInventory(Player player) {
        UUID playerId = player.getUniqueId();
        PlayerInventoryState state = new PlayerInventoryState(player.getInventory().getContents(),
                player.getInventory().getArmorContents());
        playerInventoryCache.put(playerId, state);
        player.getInventory().clear();
    }

    public void revertPlayerInventory(Player player) {
        UUID playerId = player.getUniqueId();
        PlayerInventoryState state = playerInventoryCache.remove(playerId);

        player.getInventory().clear();
        player.getInventory().setContents(state.getContents());
        player.getInventory().setArmorContents(state.getArmorContents());
    }

    public void applyModerationTools(Player player) {
        player.getInventory().setItem(0,
                createCustomItem(Material.ENCHANTED_BOOK, "Player List", "player_list", ChatColor.LIGHT_PURPLE, true));
        player.getInventory().setItem(1,
                createCustomItem(Material.DIAMOND_PICKAXE, "Moderation Tool", "moderation_tool", ChatColor.GREEN,
                        true));
        player.getInventory().setItem(2,
                createCustomItem(Material.COMPASS, "Teleport", "teleport", ChatColor.AQUA, false));
    }
}
