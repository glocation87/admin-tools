package com.github.glocation87.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.inventory.Inventory;

import com.github.glocation87.util.CustomInventory;
import com.github.glocation87.util.CustomInventoryHolder;
import com.github.glocation87.manager.PlayerProfileManager;

public class PlayerListManager {

    private static final int SLOTS_COUNT = 5;
    private static final String TITLE = "      §l§k|||§r§l" + "  §lPlayer List  " + "§l§k|||§r§l";
    private static final CustomInventoryHolder HOLDER = new CustomInventoryHolder("player_list");
    private static final List<ItemStack> dummyPlayers;
    private static final Map<Player, Integer> playerPages = new HashMap<>();

    // Initialize dummy players for testing
    static {
        dummyPlayers = new ArrayList<>();
        for (int i = 1; i <= 100; i++) {
            ItemStack playerHead = new ItemStack(Material.PLAYER_HEAD, 1);
            SkullMeta skullMeta = (SkullMeta) playerHead.getItemMeta();
            skullMeta.setDisplayName(ChatColor.YELLOW + "§lDummyPlayer" + i);

            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "UUID: " + ChatColor.GREEN + UUID.randomUUID().toString());
            lore.add(ChatColor.GRAY + "Ping: " + ChatColor.GREEN + (int) (Math.random() * 100) + "ms");
            lore.add(ChatColor.GRAY + "Rank: " + ChatColor.GREEN + "Member");
            skullMeta.setLore(lore);

            playerHead.setItemMeta(skullMeta);
            dummyPlayers.add(playerHead);
        }
    }

    public static void openPlayerListUI(Player player, int page) {
        CustomInventory playerListInventory = new CustomInventory(HOLDER, SLOTS_COUNT, TITLE);

        // Add player heads
        int startIndex = page * playerListInventory.getAvailableSlots();
        int endIndex = Math.min(startIndex + playerListInventory.getAvailableSlots(), dummyPlayers.size());

        for (int i = startIndex; i < endIndex; i++) {
            int slotNumber = 10 + (i - startIndex) % 7 + ((i - startIndex) / 7) * 9;
            playerListInventory.setItem(slotNumber, dummyPlayers.get(i));
        }

        // Add navigation buttons
        if (page > 0) {
            int slotNumber = SLOTS_COUNT * 9 - 9;
            playerListInventory.addInventoryItem(Material.RED_STAINED_GLASS_PANE, ChatColor.GREEN + "Previous Page", slotNumber);
        }

        if (endIndex < dummyPlayers.size()) {
            int slotNumber = SLOTS_COUNT * 9 - 1;
            playerListInventory.addInventoryItem(Material.GREEN_STAINED_GLASS_PANE, ChatColor.GREEN + "Next Page", slotNumber);
        }

        playerPages.put(player, page);
        player.openInventory(playerListInventory.getInventory());
    }

    public static void handleInventoryClick(Player player, InventoryHolder inventoryHolder, int slot) {
        if (!(inventoryHolder instanceof CustomInventoryHolder)) return;

        CustomInventoryHolder holder = (CustomInventoryHolder) inventoryHolder;
        if (!"player_list".equals(holder.getIdentifier())) return;

        Inventory playerInventory = holder.getInventory();
        if (playerInventory == null || slot >= playerInventory.getSize()) {
            System.out.println("Invalid slot: " + slot);
            return;
        }

        ItemStack item = playerInventory.getItem(slot);
        if (item == null) {
            System.out.println("No item found in slot: " + slot);
            return;
        }

        ItemMeta itemMeta = item.getItemMeta();
        String itemName = ChatColor.stripColor(itemMeta.getDisplayName());
        Material itemMaterial = item.getType();
        int currentPage = playerPages.getOrDefault(player, 0);

        if (itemMaterial == Material.PLAYER_HEAD) {
            player.closeInventory();
            PlayerProfileManager.openPlayerMenuUI(player, itemMeta.getDisplayName(), UUID.randomUUID());
        } else if ("Previous Page".equals(itemName) && currentPage > 0) {
            currentPage--;
            playerPages.put(player, currentPage);
            player.closeInventory();
            openPlayerListUI(player, currentPage);
        } else if ("Next Page".equals(itemName) && (currentPage + 1) * 21 < dummyPlayers.size()) {
            currentPage++;
            playerPages.put(player, currentPage);
            player.closeInventory();
            openPlayerListUI(player, currentPage);
        }
    }
}
